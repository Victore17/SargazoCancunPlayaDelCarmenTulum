package victor.android.sargazocancnplayadelcarmentulum.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.filterBeaches
import javax.inject.Inject

@HiltViewModel
class BeachListViewModel @Inject constructor(
    private val beachRepository: BeachRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedLevels = MutableStateFlow<Set<SargassumLevel>>(emptySet())

    val uiState: StateFlow<BeachListUiState> = combine(
        beachRepository.observeBeachStatuses(),
        query,
        selectedLevels,
    ) { statuses, query, levels ->
        // La lista sale del semáforo: las playas que no aparecen en él (sin dato) no se muestran.
        val inSemaforo = statuses.filter { it.level != SargassumLevel.NO_DATA }
        BeachListUiState(
            isLoading = false,
            hasSemaforo = inSemaforo.isNotEmpty(),
            query = query,
            selectedLevels = levels,
            beaches = inSemaforo.filterBeaches(query, levels),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BeachListUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    /** Marca o desmarca un color del filtro. */
    fun toggleLevel(level: SargassumLevel) {
        selectedLevels.update { if (level in it) it - level else it + level }
    }

    fun clearFilters() {
        query.value = ""
        selectedLevels.value = emptySet()
    }

    fun toggleFavorite(status: BeachStatus) {
        viewModelScope.launch {
            beachRepository.setFavorite(status.beach.id, !status.beach.isFavorite)
        }
    }
}
