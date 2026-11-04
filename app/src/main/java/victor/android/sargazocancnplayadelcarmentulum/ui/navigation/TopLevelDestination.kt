package victor.android.sargazocancnplayadelcarmentulum.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import victor.android.sargazocancnplayadelcarmentulum.R

/** Destinos de la barra de navegación inferior. */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    MAP(MapRoute, R.string.nav_map, R.drawable.ic_nav_map),
    BEACH_LIST(BeachListRoute, R.string.nav_beach_list, R.drawable.ic_nav_list),
    FAVORITES(FavoritesRoute, R.string.nav_favorites, R.drawable.ic_nav_favorite),
    SETTINGS(SettingsRoute, R.string.nav_settings, R.drawable.ic_nav_settings),
}
