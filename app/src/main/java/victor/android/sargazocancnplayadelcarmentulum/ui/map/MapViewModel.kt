package victor.android.sargazocancnplayadelcarmentulum.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    beachRepository: BeachRepository,
) : ViewModel() {

    val uiState: StateFlow<MapUiState> = beachRepository.observeBeaches()
        .map { beaches ->
            MapUiState(
                isLoading = false,
                knownBeachCount = beaches.count { it.location != null },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState(),
        )
}
