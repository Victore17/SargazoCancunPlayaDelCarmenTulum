package victor.android.sargazocancnplayadelcarmentulum.ui.shell

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminSession
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeAdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.MainDispatcherRule

class AppShellViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private var now = 0L
    private val timeSource = ElapsedTimeSource { now }
    private val auth = FakeAdminAuthRepository()

    private fun viewModel(savedState: SavedStateHandle = SavedStateHandle()) =
        AppShellViewModel(savedState, timeSource, auth)

    private fun AppShellViewModel.tapSettings(times: Int): List<Boolean> = (1..times).map {
        now += 300
        onSettingsTapped()
    }

    @Test
    fun `la opcion de admin empieza oculta`() = runTest {
        viewModel().uiState.test {
            assertFalse(awaitItem().showAdminDestination)
        }
    }

    @Test
    fun `siete toques en ajustes muestran la opcion de admin`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertFalse(awaitItem().showAdminDestination)

            val results = viewModel.tapSettings(7)

            assertEquals(List(6) { false } + true, results)
            assertTrue(awaitItem().showAdminDestination)
        }
    }

    @Test
    fun `tocar otra opcion corta la secuencia`() {
        val viewModel = viewModel()

        viewModel.tapSettings(4)
        viewModel.onOtherDestinationTapped()

        assertTrue(viewModel.tapSettings(6).none { it })
    }

    @Test
    fun `con sesion iniciada la opcion se muestra sin toques`() = runTest {
        auth.session.value = AdminSession.SignedIn(email = "admin@example.com", isAdmin = true)

        viewModel().uiState.test {
            assertTrue(awaitItem().showAdminDestination)
        }
    }

    @Test
    fun `el desbloqueo sobrevive a recrear el ViewModel`() = runTest {
        val savedState = SavedStateHandle()
        viewModel(savedState).tapSettings(7)

        viewModel(savedState).uiState.test {
            assertTrue(awaitItem().showAdminDestination)
        }
    }

    @Test
    fun `ya desbloqueada, mas toques no vuelven a desbloquear`() {
        val viewModel = viewModel()
        viewModel.tapSettings(7)

        assertTrue(viewModel.tapSettings(7).none { it })
    }
}
