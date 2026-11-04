package victor.android.sargazocancnplayadelcarmentulum.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.normalizeForSearch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val beachRepository: BeachRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = beachRepository.observeBeachStatuses()
        .map { statuses ->
            FavoritesUiState(
                isLoading = false,
                favorites = statuses
                    .filter { it.beach.isFavorite }
                    .sortedBy { normalizeForSearch(it.beach.name) },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

    /** Quita la playa de favoritas (en esta pantalla todas lo son). */
    fun removeFavorite(status: BeachStatus) {
        viewModelScope.launch { beachRepository.setFavorite(status.beach.id, false) }
    }
}
