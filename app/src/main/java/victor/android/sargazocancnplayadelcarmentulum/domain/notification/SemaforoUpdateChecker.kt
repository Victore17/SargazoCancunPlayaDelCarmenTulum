package victor.android.sargazocancnplayadelcarmentulum.domain.notification

import kotlinx.coroutines.flow.first
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SettingsRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Resultado de revisar si hay un semáforo nuevo. */
enum class UpdateCheckResult {
    /** No hay un semáforo más reciente que el guardado. */
    NO_NEW_DATA,

    /** Hay un semáforo nuevo: ya se guardó en Room y se avisó según los ajustes. */
    NEW_DATA,
}

/**
 * Revisa si hay un semáforo nuevo publicado y avisa al usuario según sus ajustes.
 * También hace la limpieza del historial.
 */
class SemaforoUpdateChecker @Inject constructor(
    private val semaforoRepository: SemaforoRepository,
    private val beachRepository: BeachRepository,
    private val settingsRepository: SettingsRepository,
    private val notifier: SemaforoNotifier,
    private val clock: Clock,
) {

    /**
     * Descarga los semáforos publicados y compara la fecha del más reciente con la guardada en Room.
     * Si es más nueva, manda la notificación general y la de cambios en favoritas, según los ajustes.
     *
     * La primera vez (cuando todavía no hay ningún semáforo guardado) no se avisa, para no molestar
     * con información que el usuario aún no tenía forma de ver.
     *
     * Lanza una excepción si no se pudo consultar el servidor.
     */
    suspend fun check(): UpdateCheckResult {
        val before = beachRepository.observeBeachStatuses().first()
        val previousDate = before.latestDate()

        semaforoRepository.refresh()

        val after = beachRepository.observeBeachStatuses().first()
        val newDate = after.latestDate()
        if (previousDate == null || newDate == null || !newDate.isAfter(previousDate)) {
            return UpdateCheckResult.NO_NEW_DATA
        }

        val settings = settingsRepository.observeSettings().first()
        if (settings.allNotificationsEnabled) {
            if (settings.generalUpdateNotificationsEnabled) {
                notifier.notifyGeneralUpdate()
            }
            if (settings.favoriteChangeNotificationsEnabled) {
                val changes = favoriteChanges(before, after)
                if (changes.isNotEmpty()) notifier.notifyFavoriteChanges(changes)
            }
        }
        return UpdateCheckResult.NEW_DATA
    }

    /**
     * Borra los registros de nivel con más de dos meses. Las playas (y sus favoritas) y los ajustes
     * no se tocan.
     */
    suspend fun cleanOldRecords() {
        beachRepository.deleteLevelRecordsBefore(LocalDate.now(clock).minusMonths(HISTORY_MONTHS))
    }

    /** Favoritas con notificaciones activadas cuyo nivel cambió entre los dos semáforos. */
    private fun favoriteChanges(
        before: List<BeachStatus>,
        after: List<BeachStatus>,
    ): List<FavoriteLevelChange> {
        val previousLevels = before.associate { it.beach.id to it.level }
        return after.mapNotNull { status ->
            if (!status.beach.isFavorite || !status.beach.notificationsEnabled) return@mapNotNull null
            val previous = previousLevels[status.beach.id] ?: return@mapNotNull null
            // Sin dato no hay con qué comparar: la playa no aparecía en uno de los dos semáforos.
            if (previous == SargassumLevel.NO_DATA || status.level == SargassumLevel.NO_DATA) {
                return@mapNotNull null
            }
            if (previous == status.level) return@mapNotNull null
            FavoriteLevelChange(status.beach.name, previous, status.level)
        }
    }

    private fun List<BeachStatus>.latestDate(): LocalDate? = mapNotNull { it.date }.maxOrNull()

    private companion object {
        const val HISTORY_MONTHS = 2L
    }
}
