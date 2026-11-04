package victor.android.sargazocancnplayadelcarmentulum.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import victor.android.sargazocancnplayadelcarmentulum.ui.admin.AdminScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.detail.BeachDetailScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.favorites.FavoritesScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.list.BeachListScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.map.MapScreen
import victor.android.sargazocancnplayadelcarmentulum.ui.settings.SettingsScreen

@Composable
fun SargazoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val openBeachDetail: (Long) -> Unit = { beachId ->
        navController.navigate(BeachDetailRoute(beachId))
    }

    NavHost(
        navController = navController,
        startDestination = MapRoute,
        modifier = modifier,
    ) {
        composable<MapRoute> { MapScreen(onBeachClick = openBeachDetail) }
        composable<BeachListRoute> { BeachListScreen(onBeachClick = openBeachDetail) }
        composable<FavoritesRoute> { FavoritesScreen(onBeachClick = openBeachDetail) }
        composable<SettingsRoute> { SettingsScreen() }
        composable<AdminRoute> { AdminScreen() }
        composable<BeachDetailRoute> { entry ->
            BeachDetailScreen(beachId = entry.toRoute<BeachDetailRoute>().beachId)
        }
    }
}
