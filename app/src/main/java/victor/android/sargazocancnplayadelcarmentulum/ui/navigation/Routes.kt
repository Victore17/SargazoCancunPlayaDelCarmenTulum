package victor.android.sargazocancnplayadelcarmentulum.ui.navigation

import kotlinx.serialization.Serializable

// Rutas tipadas de Navigation Compose.

@Serializable
data object MapRoute

@Serializable
data object BeachListRoute

@Serializable
data object FavoritesRoute

@Serializable
data object SettingsRoute

/** Información de la app: fuente de los datos, aviso de que no es oficial y créditos. */
@Serializable
data object AboutRoute

/** Pantalla oculta de administración (7 toques en "Ajustes"). */
@Serializable
data object AdminRoute

@Serializable
data class BeachDetailRoute(val beachId: Long)
