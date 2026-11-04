package victor.android.sargazocancnplayadelcarmentulum.ui.settings

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSettingsRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.MainDispatcherRule

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository = FakeSettingsRepository()
    private val beachRepository = FakeBeachRepository()

    private fun beach(id: Long, name: String, favorite: Boolean, notifications: Boolean = true) =
        Beach(id, name, null, null, favorite, notifications)

    private fun viewModel() = SettingsViewModel(settingsRepository, beachRepository)

    @Test
    fun `muestra los ajustes y solo las favoritas ordenadas por nombre`() = runTest {
        beachRepository.beaches.value = listOf(
            beach(1, "Tulum", favorite = true),
            beach(2, "Akumal", favorite = false),
            beach(3, "Chemuyil", favorite = true, notifications = false),
        )

        viewModel().uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(listOf("Chemuyil", "Tulum"), state.favorites.map { it.name })
            assertTrue(state.settings.allNotificationsEnabled)
        }
    }

    @Test
    fun `apagar el interruptor general deshabilita los demas sin cambiar su valor`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onAllNotificationsChanged(false)
            val state = expectMostRecentItem()

            assertFalse(state.settings.allNotificationsEnabled)
            assertFalse(state.areDetailSwitchesEnabled)
            assertTrue(state.settings.generalUpdateNotificationsEnabled)
            assertTrue(state.settings.favoriteChangeNotificationsEnabled)
        }
    }

    @Test
    fun `al volver a encender el general se restaura la configuracion anterior`() = runTest {
        beachRepository.beaches.value = listOf(
            beach(1, "Tulum", favorite = true),
            beach(2, "Akumal", favorite = true),
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onGeneralUpdateChanged(false)
            viewModel.onBeachNotificationsChanged(2, false)
            viewModel.onAllNotificationsChanged(false)
            viewModel.onAllNotificationsChanged(true)
            val state = expectMostRecentItem()

            assertTrue(state.areDetailSwitchesEnabled)
            assertFalse(state.settings.generalUpdateNotificationsEnabled)
            assertTrue(state.settings.favoriteChangeNotificationsEnabled)
            assertTrue(state.favorites.first { it.id == 1L }.notificationsEnabled)
            assertFalse(state.favorites.first { it.id == 2L }.notificationsEnabled)
        }
    }

    @Test
    fun `cambia el interruptor de cambios en favoritas`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onFavoriteChangesChanged(false)
            val state = expectMostRecentItem()

            assertFalse(state.settings.favoriteChangeNotificationsEnabled)
            assertTrue(state.settings.generalUpdateNotificationsEnabled)
        }
    }

    @Test
    fun `cambia el interruptor de una playa favorita`() = runTest {
        beachRepository.beaches.value = listOf(beach(1, "Tulum", favorite = true))
        val viewModel = viewModel()

        viewModel.onBeachNotificationsChanged(1, false)

        assertFalse(beachRepository.beaches.value.first().notificationsEnabled)
    }
}
