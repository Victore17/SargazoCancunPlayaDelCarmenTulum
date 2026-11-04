package victor.android.sargazocancnplayadelcarmentulum.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import victor.android.sargazocancnplayadelcarmentulum.R

/**
 * Destinos de la barra de navegación inferior.
 *
 * @property hidden si es true, solo aparece cuando se desbloquea (opción de administración).
 */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    val hidden: Boolean = false,
) {
    MAP(MapRoute, R.string.nav_map, R.drawable.ic_nav_map),
    BEACH_LIST(BeachListRoute, R.string.nav_beach_list, R.drawable.ic_nav_list),
    FAVORITES(FavoritesRoute, R.string.nav_favorites, R.drawable.ic_nav_favorite),
    SETTINGS(SettingsRoute, R.string.nav_settings, R.drawable.ic_nav_settings),
    ADMIN(AdminRoute, R.string.nav_admin, R.drawable.ic_nav_admin, hidden = true),
}
