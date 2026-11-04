package victor.android.sargazocancnplayadelcarmentulum.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SettingsRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.normalizeForSearch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val beachRepository: BeachRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeSettings(),
        beachRepository.observeFavoriteBeaches(),
    ) { settings, favorites ->
        SettingsUiState(
            isLoading = false,
            settings = settings,
            favorites = favorites.sortedBy { normalizeForSearch(it.name) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    /**
     * Interruptor general. Solo cambia su propio valor: los demás interruptores se conservan para
     * restaurar la configuración anterior al volver a encenderlo.
     */
    fun onAllNotificationsChanged(enabled: Boolean) {
        update { it.copy(allNotificationsEnabled = enabled) }
    }

    fun onGeneralUpdateChanged(enabled: Boolean) {
        update { it.copy(generalUpdateNotificationsEnabled = enabled) }
    }

    fun onFavoriteChangesChanged(enabled: Boolean) {
        update { it.copy(favoriteChangeNotificationsEnabled = enabled) }
    }

    fun onBeachNotificationsChanged(beachId: Long, enabled: Boolean) {
        viewModelScope.launch { beachRepository.setNotificationsEnabled(beachId, enabled) }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.updateSettings(transform) }
    }
}
