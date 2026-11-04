package victor.android.sargazocancnplayadelcarmentulum.ui.shell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminSession
import javax.inject.Inject

/** Estado de la estructura principal: si se muestra la opción oculta de administración. */
data class AppShellUiState(
    val showAdminDestination: Boolean = false,
)

/**
 * ViewModel de la estructura principal de la app (barra de navegación).
 *
 * La opción "Admin" aparece al tocar 7 veces seguidas "Ajustes", o si ya hay una sesión
 * iniciada. Esconderla solo evita que se vea por accidente: la seguridad real está en el
 * inicio de sesión y en las reglas de Firestore.
 */
@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val timeSource: ElapsedTimeSource,
    authRepository: AdminAuthRepository,
) : ViewModel() {

    private val tapDetector = SecretTapDetector()
    private val unlocked = savedStateHandle.getStateFlow(KEY_UNLOCKED, false)

    val uiState: StateFlow<AppShellUiState> =
        combine(unlocked, authRepository.observeSession()) { isUnlocked, session ->
            AppShellUiState(showAdminDestination = isUnlocked || session is AdminSession.SignedIn)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppShellUiState(showAdminDestination = savedStateHandle[KEY_UNLOCKED] ?: false),
        )

    /** Se llama en cada toque de "Ajustes". Devuelve true si con este toque se desbloqueó la opción. */
    fun onSettingsTapped(): Boolean {
        if (unlocked.value) return false
        val justUnlocked = tapDetector.onTap(timeSource.nowMillis())
        if (justUnlocked) savedStateHandle[KEY_UNLOCKED] = true
        return justUnlocked
    }

    /** Un toque en otra opción corta la secuencia. */
    fun onOtherDestinationTapped() = tapDetector.reset()

    private companion object {
        const val KEY_UNLOCKED = "admin_unlocked"
    }
}
