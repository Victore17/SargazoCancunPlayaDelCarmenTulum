package victor.android.sargazocancnplayadelcarmentulum.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoRepository
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    beachRepository: BeachRepository,
    private val semaforoRepository: SemaforoRepository,
) : ViewModel() {

    private data class SyncState(val isRefreshing: Boolean = false, val failed: Boolean = false)

    private val syncState = MutableStateFlow(SyncState())

    val uiState: StateFlow<MapUiState> =
        combine(beachRepository.observeBeachStatuses(), syncState) { statuses, sync ->
            buildState(statuses).copy(isRefreshing = sync.isRefreshing, showSyncError = sync.failed)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState(),
        )

    init {
        refresh()
    }

    /** Descarga los semáforos publicados. Si falla, el mapa sigue mostrando lo guardado en Room. */
    fun refresh() {
        if (syncState.value.isRefreshing) return
        syncState.value = SyncState(isRefreshing = true)
        viewModelScope.launch {
            val failed = try {
                semaforoRepository.refresh()
                false
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                true
            }
            syncState.update { SyncState(isRefreshing = false, failed = failed) }
        }
    }

    companion object {
        /** Solo se dibujan las playas con ubicación; las demás se cuentan como "sin ubicación". */
        fun buildState(statuses: List<BeachStatus>): MapUiState {
            val latestDate = statuses.mapNotNull { it.date }.maxOrNull()
            val reported = statuses.filter { it.level != SargassumLevel.NO_DATA }
            return MapUiState(
                isLoading = false,
                beaches = statuses.mapNotNull { status ->
                    val location = status.beach.location ?: return@mapNotNull null
                    MapBeach(
                        id = status.beach.id,
                        name = status.beach.name,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        level = status.level,
                    )
                },
                latestDate = latestDate,
                withoutLocationCount = reported.count { it.beach.location == null },
            )
        }
    }
}
