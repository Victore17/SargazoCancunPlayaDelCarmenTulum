package victor.android.sargazocancnplayadelcarmentulum.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import victor.android.sargazocancnplayadelcarmentulum.ui.navigation.SargazoNavHost
import victor.android.sargazocancnplayadelcarmentulum.ui.navigation.TopLevelDestination
import victor.android.sargazocancnplayadelcarmentulum.ui.shell.AppShellViewModel

/** Estructura principal de la app: barra de navegación inferior y contenido. */
@Composable
fun SargazoAppContent(viewModel: AppShellViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val destinations = TopLevelDestination.entries.filter { !it.hidden || uiState.showAdminDestination }
    val showBottomBar = destinations.any { currentDestination.isOn(it) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination.isOn(destination),
                            onClick = {
                                val target = when {
                                    destination != TopLevelDestination.SETTINGS -> {
                                        viewModel.onOtherDestinationTapped()
                                        destination
                                    }
                                    // Séptimo toque seguido en "Ajustes": se abre la opción oculta.
                                    viewModel.onSettingsTapped() -> TopLevelDestination.ADMIN
                                    else -> destination
                                }
                                navController.navigateToTopLevel(target)
                            },
                            icon = {
                                Icon(painterResource(destination.iconRes), contentDescription = null)
                            },
                            label = { Text(stringResource(destination.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        SargazoNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
