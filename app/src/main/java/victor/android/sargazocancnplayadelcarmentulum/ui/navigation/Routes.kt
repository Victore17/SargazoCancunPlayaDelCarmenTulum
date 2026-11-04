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

/** Pantalla oculta de administración (7 toques en "Ajustes"). */
@Serializable
data object AdminRoute

@Serializable
data class BeachDetailRoute(val beachId: Long)
