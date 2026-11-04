package victor.android.sargazocancnplayadelcarmentulum.domain.model

/**
 * Ajustes de notificaciones de la app.
 *
 * [allNotificationsEnabled] es el interruptor general: al apagarlo no se pierden los valores de
 * los demás interruptores, así se puede restaurar la configuración anterior al volver a encenderlo.
 */
data class AppSettings(
    val allNotificationsEnabled: Boolean = true,
    val generalUpdateNotificationsEnabled: Boolean = true,
    val favoriteChangeNotificationsEnabled: Boolean = true,
)
