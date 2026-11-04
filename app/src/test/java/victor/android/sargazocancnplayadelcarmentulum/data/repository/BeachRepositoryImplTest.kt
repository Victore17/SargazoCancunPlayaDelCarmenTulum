package victor.android.sargazocancnplayadelcarmentulum.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachDao
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeLevelRecordDao
import java.time.LocalDate

class BeachRepositoryImplTest {

    private val beachDao = FakeBeachDao(
        listOf(
            BeachEntity(id = 1, name = "Playa Delfines", latitude = 21.06, longitude = -86.78),
            BeachEntity(id = 2, name = "Akumal", latitude = 20.39, longitude = -87.31),
        ),
    )
    private val levelRecordDao = FakeLevelRecordDao()
    private val repository = BeachRepositoryImpl(beachDao, levelRecordDao)

    @Test
    fun observeBeaches_emitsDomainBeachesSortedByName() = runTest {
        repository.observeBeaches().test {
            assertEquals(listOf("Akumal", "Playa Delfines"), awaitItem().map { it.name })
        }
    }

    @Test
    fun setFavorite_updatesFavoritesFlow() = runTest {
        repository.observeFavoriteBeaches().test {
            assertTrue(awaitItem().isEmpty())

            repository.setFavorite(beachId = 1, isFavorite = true)
            assertEquals(listOf(1L), awaitItem().map { it.id })

            repository.setFavorite(beachId = 1, isFavorite = false)
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun setNotificationsEnabled_updatesBeach() = runTest {
        repository.observeBeach(2).test {
            assertTrue(awaitItem()!!.notificationsEnabled)
            repository.setNotificationsEnabled(beachId = 2, enabled = false)
            assertEquals(false, awaitItem()!!.notificationsEnabled)
        }
    }

    @Test
    fun observeBeach_unknownId_emitsNull() = runTest {
        repository.observeBeach(99).test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun saveLevelRecords_historyIsNewestFirst_andSameDateIsReplaced() = runTest {
        val day1 = LocalDate.of(2026, 7, 1)
        val day2 = LocalDate.of(2026, 7, 2)

        repository.saveLevelRecords(
            listOf(
                LevelRecord(beachId = 1, level = SargassumLevel.LOW, date = day1),
                LevelRecord(beachId = 1, level = SargassumLevel.ABUNDANT, date = day2),
                LevelRecord(beachId = 2, level = SargassumLevel.MODERATE, date = day1),
            ),
        )
        repository.saveLevelRecords(
            listOf(LevelRecord(beachId = 1, level = SargassumLevel.EXCESSIVE, date = day2)),
        )

        repository.observeLevelHistory(1).test {
            val history = awaitItem()
            assertEquals(listOf(day2, day1), history.map { it.date })
            assertEquals(SargassumLevel.EXCESSIVE, history.first().level)
        }
    }

    @Test
    fun deleteLevelRecordsBefore_keepsRecentRecordsAndBeaches() = runTest {
        val old = LocalDate.of(2026, 1, 1)
        val recent = LocalDate.of(2026, 6, 1)
        repository.setFavorite(beachId = 1, isFavorite = true)
        repository.saveLevelRecords(
            listOf(
                LevelRecord(beachId = 1, level = SargassumLevel.LOW, date = old),
                LevelRecord(beachId = 1, level = SargassumLevel.ABUNDANT, date = recent),
            ),
        )

        repository.deleteLevelRecordsBefore(recent.minusMonths(2))

        repository.observeLevelHistory(1).test {
            assertEquals(listOf(recent), awaitItem().map { it.date })
        }
        repository.observeFavoriteBeaches().test {
            assertEquals(listOf(1L), awaitItem().map { it.id })
        }
    }
}
