package victor.android.sargazocancnplayadelcarmentulum.domain.notification

import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel

/** Cambio de nivel de una playa favorita entre dos semáforos. */
data class FavoriteLevelChange(
    val beachName: String,
    val previous: SargassumLevel,
    val current: SargassumLevel,
) {
    /** true si ahora hay más sargazo que antes. */
    val hasMoreSargassum: Boolean
        get() = current.severity > previous.severity
}

/**
 * Orden de gravedad de los niveles, de menos a más sargazo.
 * [SargassumLevel.NO_DATA] no es un nivel real y no se compara.
 */
internal val SargassumLevel.severity: Int
    get() = when (this) {
        SargassumLevel.NO_DATA -> -1
        SargassumLevel.NO_SARGASSUM -> 0
        SargassumLevel.LOW -> 1
        SargassumLevel.MODERATE -> 2
        SargassumLevel.ABUNDANT -> 3
        SargassumLevel.EXCESSIVE -> 4
    }

/** Muestra las notificaciones de la app. La implementación de Android está en la capa data. */
interface SemaforoNotifier {

    /** Notificación 1: hay un semáforo nuevo para revisar. */
    fun notifyGeneralUpdate()

    /** Notificación 2: cambios de nivel en las favoritas. Todos los cambios van en una sola notificación. */
    fun notifyFavoriteChanges(changes: List<FavoriteLevelChange>)
}
