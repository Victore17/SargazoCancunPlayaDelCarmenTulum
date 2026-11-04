package victor.android.sargazocancnplayadelcarmentulum.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachReading
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachDao
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeLevelRecordDao
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSemaforoRemoteDataSource
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class SemaforoRepositoryImplTest {

    private val today = LocalDate.of(2026, 11, 3)
    private val clock = Clock.fixed(Instant.parse("2026-11-03T15:00:00Z"), ZoneOffset.UTC)

    private val levelRecordDao = FakeLevelRecordDao()
    private val beachDao = FakeBeachDao(
        initial = listOf(
            BeachEntity(id = 1, name = "Punta Molas", latitude = 20.59, longitude = -86.728),
            BeachEntity(id = 2, name = "Tankah", latitude = 20.25, longitude = -87.39, isFavorite = true),
        ),
        levelRecordDao = levelRecordDao,
    )
    private val remote = FakeSemaforoRemoteDataSource()
    private val repository = SemaforoRepositoryImpl(remote, beachDao, levelRecordDao, clock)
    private val beachRepository = BeachRepositoryImpl(beachDao, levelRecordDao)

    @Test
    fun `refresh pide solo los ultimos dos meses`() = runTest {
        repository.refresh()

        assertEquals(LocalDate.of(2026, 9, 3), remote.lastFetchSince)
    }

    @Test
    fun `refresh guarda los niveles en Room y el mapa ve el mas reciente`() = runTest {
        remote.reports += SemaforoReport(today.minusDays(1), listOf(BeachReading("Punta Molas", SargassumLevel.LOW)))
        remote.reports += SemaforoReport(today, listOf(BeachReading("Punta Molas", SargassumLevel.EXCESSIVE)))

        repository.refresh()

        beachRepository.observeBeachStatuses().test {
            val byName = awaitItem().associateBy { it.beach.name }
            assertEquals(SargassumLevel.EXCESSIVE, byName.getValue("Punta Molas").level)
            assertEquals(today, byName.getValue("Punta Molas").date)
            // Tankah no aparece en el semáforo más reciente.
            assertEquals(SargassumLevel.NO_DATA, byName.getValue("Tankah").level)
        }
        beachRepository.observeLevelHistory(1).test {
            assertEquals(listOf(today, today.minusDays(1)), awaitItem().map { it.date })
        }
    }

    @Test
    fun `playa del catalogo que falta se crea con coordenadas`() = runTest {
        remote.reports += SemaforoReport(today, listOf(BeachReading("Isla Contoy", SargassumLevel.NO_SARGASSUM)))

        repository.refresh()

        val contoy = beachDao.getAll().single { it.name == "Isla Contoy" }
        assertTrue(contoy.latitude != null && contoy.longitude != null)
    }

    @Test
    fun `playa nueva se crea sin ubicacion`() = runTest {
        remote.reports += SemaforoReport(today, listOf(BeachReading("Playa Inventada", SargassumLevel.LOW)))

        repository.refresh()

        val nueva = beachDao.getAll().single { it.name == "Playa Inventada" }
        assertNull(nueva.latitude)
        assertNull(nueva.longitude)
        assertEquals(1, levelRecordDao.records.value.count { it.beachId == nueva.id })
    }

    @Test
    fun `publish sube a Firestore y guarda en Room`() = runTest {
        val report = SemaforoReport(today, listOf(BeachReading("Tankah", SargassumLevel.MODERATE)))

        repository.publish(report)

        assertEquals(listOf(report), remote.reports)
        assertEquals(SargassumLevel.MODERATE, levelRecordDao.records.value.single { it.beachId == 2L }.level)
    }

    @Test
    fun `publicar dos veces la misma fecha reemplaza el nivel`() = runTest {
        repository.publish(SemaforoReport(today, listOf(BeachReading("Tankah", SargassumLevel.MODERATE))))
        repository.publish(SemaforoReport(today, listOf(BeachReading("Tankah", SargassumLevel.LOW))))

        val records = levelRecordDao.records.value.filter { it.beachId == 2L }
        assertEquals(1, records.size)
        assertEquals(SargassumLevel.LOW, records.single().level)
    }

    @Test
    fun `sin dato no se guarda como registro`() = runTest {
        repository.publish(SemaforoReport(today, listOf(BeachReading("Tankah", SargassumLevel.NO_DATA))))

        assertTrue(levelRecordDao.records.value.isEmpty())
    }

    @Test
    fun `si falla la red no se toca lo guardado`() = runTest {
        repository.publish(SemaforoReport(today, listOf(BeachReading("Tankah", SargassumLevel.LOW))))
        remote.error = IOException("sin conexión")

        val result = runCatching { repository.refresh() }

        assertTrue(result.isFailure)
        assertEquals(1, levelRecordDao.records.value.size)
        // La favorita sigue siendo favorita.
        assertTrue(beachDao.getAll().single { it.name == "Tankah" }.isFavorite)
    }
}
