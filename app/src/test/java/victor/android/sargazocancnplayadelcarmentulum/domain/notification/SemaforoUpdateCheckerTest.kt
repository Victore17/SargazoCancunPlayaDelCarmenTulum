package victor.android.sargazocancnplayadelcarmentulum.domain.notification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSemaforoNotifier
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSemaforoRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSettingsRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.beachStatus
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SemaforoUpdateCheckerTest {

    private val oldDate = LocalDate.of(2026, 11, 1)
    private val newDate = LocalDate.of(2026, 11, 3)

    private val beaches = FakeBeachRepository()
    private val semaforoRepository = FakeSemaforoRepository()
    private val settings = FakeSettingsRepository()
    private val notifier = FakeSemaforoNotifier()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneId.of("UTC"))
    private val checker = SemaforoUpdateChecker(semaforoRepository, beaches, settings, notifier, clock)

    private fun status(
        id: Long,
        level: SargassumLevel,
        date: LocalDate? = oldDate,
        favorite: Boolean = false,
        notifications: Boolean = true,
    ): BeachStatus {
        val base = beachStatus(id = id, level = level, isFavorite = favorite, date = date)
        return base.copy(beach = base.beach.copy(notificationsEnabled = notifications))
    }

    /** Simula que al descargar llegan los niveles [after] a Room. */
    private fun arrive(vararg after: BeachStatus) {
        semaforoRepository.onRefresh = { beaches.statuses.value = after.toList() }
    }

    @Test
    fun `new semaforo sends the general notification`() = runTest {
        beaches.statuses.value = listOf(status(1, SargassumLevel.LOW))
        arrive(status(1, SargassumLevel.LOW, newDate))

        val result = checker.check()

        assertEquals(UpdateCheckResult.NEW_DATA, result)
        assertEquals(1, notifier.generalCount)
        assertTrue(notifier.favoriteNotifications.isEmpty())
    }

    @Test
    fun `same date sends nothing`() = runTest {
        beaches.statuses.value = listOf(status(1, SargassumLevel.LOW, favorite = true))
        arrive(status(1, SargassumLevel.EXCESSIVE, oldDate, favorite = true))

        val result = checker.check()

        assertEquals(UpdateCheckResult.NO_NEW_DATA, result)
        assertEquals(1, semaforoRepository.refreshCount)
        assertEquals(0, notifier.generalCount)
        assertTrue(notifier.favoriteNotifications.isEmpty())
    }

    @Test
    fun `first semaforo ever does not notify`() = runTest {
        beaches.statuses.value = listOf(status(1, SargassumLevel.NO_DATA, date = null))
        arrive(status(1, SargassumLevel.LOW, newDate))

        val result = checker.check()

        assertEquals(UpdateCheckResult.NO_NEW_DATA, result)
        assertEquals(0, notifier.generalCount)
    }

    @Test
    fun `favorite with more sargassum is reported`() = runTest {
        beaches.statuses.value = listOf(status(1, SargassumLevel.LOW, favorite = true, notifications = true))
        arrive(status(1, SargassumLevel.ABUNDANT, newDate, favorite = true))

        checker.check()

        val change = notifier.favoriteNotifications.single()
        assertEquals(
            listOf(FavoriteLevelChange("Playa 1", SargassumLevel.LOW, SargassumLevel.ABUNDANT)),
            change,
        )
        assertTrue(change.single().hasMoreSargassum)
    }

    @Test
    fun `favorite with less sargassum is reported as less`() = runTest {
        beaches.statuses.value = listOf(status(1, SargassumLevel.EXCESSIVE, favorite = true))
        arrive(status(1, SargassumLevel.MODERATE, newDate, favorite = true))

        checker.check()

        assertEquals(false, notifier.favoriteNotifications.single().single().hasMoreSargassum)
    }

    @Test
    fun `several favorites changes go in a single notification`() = runTest {
        beaches.statuses.value = listOf(
            status(1, SargassumLevel.LOW, favorite = true),
            status(2, SargassumLevel.EXCESSIVE, favorite = true),
            status(3, SargassumLevel.MODERATE, favorite = true),
        )
        arrive(
            status(1, SargassumLevel.MODERATE, newDate, favorite = true),
            status(2, SargassumLevel.LOW, newDate, favorite = true),
            status(3, SargassumLevel.MODERATE, newDate, favorite = true),
        )

        checker.check()

        assertEquals(1, notifier.favoriteNotifications.size)
        assertEquals(listOf("Playa 1", "Playa 2"), notifier.favoriteNotifications.single().map { it.beachName })
    }

    @Test
    fun `non favorites and favorites with notifications off are ignored`() = runTest {
        beaches.statuses.value = listOf(
            status(1, SargassumLevel.LOW, favorite = false),
            status(2, SargassumLevel.LOW, favorite = true, notifications = false),
        )
        arrive(
            status(1, SargassumLevel.EXCESSIVE, newDate, favorite = false),
            status(2, SargassumLevel.EXCESSIVE, newDate, favorite = true, notifications = false),
        )

        checker.check()

        assertTrue(notifier.favoriteNotifications.isEmpty())
    }

    @Test
    fun `favorite without data in either semaforo is ignored`() = runTest {
        beaches.statuses.value = listOf(
            status(1, SargassumLevel.NO_DATA, favorite = true),
            status(2, SargassumLevel.LOW, favorite = true),
        )
        arrive(
            status(1, SargassumLevel.LOW, newDate, favorite = true),
            status(2, SargassumLevel.NO_DATA, newDate, favorite = true),
        )

        checker.check()

        assertTrue(notifier.favoriteNotifications.isEmpty())
    }

    @Test
    fun `all notifications off sends nothing`() = runTest {
        settings.settings.value = AppSettings(allNotificationsEnabled = false)
        beaches.statuses.value = listOf(status(1, SargassumLevel.LOW, favorite = true))
        arrive(status(1, SargassumLevel.EXCESSIVE, newDate, favorite = true))

        val result = checker.check()

        assertEquals(UpdateCheckResult.NEW_DATA, result)
        assertEquals(0, notifier.generalCount)
        assertTrue(notifier.favoriteNotifications.isEmpty())
    }

    @Test
    fun `general switch off still sends favorite changes`() = runTest {
        settings.settings.value = AppSettings(generalUpdateNotificationsEnabled = false)
        beaches.statuses.value = listOf(status(1, SargassumLevel.LOW, favorite = true))
        arrive(status(1, SargassumLevel.EXCESSIVE, newDate, favorite = true))

        checker.check()

        assertEquals(0, notifier.generalCount)
        assertEquals(1, notifier.favoriteNotifications.size)
    }

    @Test
    fun `favorite changes switch off still sends the general update`() = runTest {
        settings.settings.value = AppSettings(favoriteChangeNotificationsEnabled = false)
        beaches.statuses.value = listOf(status(1, SargassumLevel.LOW, favorite = true))
        arrive(status(1, SargassumLevel.EXCESSIVE, newDate, favorite = true))

        checker.check()

        assertEquals(1, notifier.generalCount)
        assertTrue(notifier.favoriteNotifications.isEmpty())
    }

    @Test
    fun `network error is propagated and nothing is sent`() = runTest {
        beaches.statuses.value = listOf(status(1, SargassumLevel.LOW))
        semaforoRepository.refreshError = java.io.IOException("sin conexión")

        val error = runCatching { checker.check() }.exceptionOrNull()

        assertTrue(error is java.io.IOException)
        assertEquals(0, notifier.generalCount)
    }

    @Test
    fun `cleanup deletes records older than two months`() = runTest {
        assertNull(beaches.lastDeletedBefore)

        checker.cleanOldRecords()

        assertEquals(LocalDate.of(2026, 9, 3), beaches.lastDeletedBefore)
    }
}
