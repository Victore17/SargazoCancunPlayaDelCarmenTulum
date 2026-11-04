package victor.android.sargazocancnplayadelcarmentulum.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.WeatherRepository
import javax.inject.Inject

/**
 * ViewModel del detalle de una playa. El id llega en los argumentos de la ruta
 * ([victor.android.sargazocancnplayadelcarmentulum.ui.navigation.BeachDetailRoute]).
 */
@HiltViewModel
class BeachDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val beachRepository: BeachRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val beachId: Long = savedStateHandle.get<Long>(BEACH_ID_KEY) ?: INVALID_ID

    private val conditions = MutableStateFlow<ConditionsUi>(ConditionsUi.Loading)

    val uiState: StateFlow<BeachDetailUiState> = combine(
        beachRepository.observeBeach(beachId),
        beachRepository.observeLevelHistory(beachId),
        conditions,
    ) { beach, history, conditionsUi ->
        // La lista llega ordenada del más reciente al más antiguo: el primero es el nivel actual.
        val latest = history.firstOrNull()
        BeachDetailUiState(
            isLoading = false,
            beach = beach,
            currentLevel = latest?.level ?: SargassumLevel.NO_DATA,
            updatedOn = latest?.date,
            history = history,
            conditions = conditionsUi,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BeachDetailUiState(),
    )

    init {
        loadConditions()
    }

    /** Consulta el clima y el mar. Se vuelve a llamar con el botón "Reintentar". */
    fun loadConditions() {
        conditions.value = ConditionsUi.Loading
        viewModelScope.launch {
            val location = beachRepository.observeBeach(beachId).first()?.location
            conditions.value = if (location == null) {
                ConditionsUi.NoLocation
            } else {
                try {
                    ConditionsUi.Loaded(weatherRepository.getConditions(location))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Sin conexión o servicio caído: se avisa en la pantalla, la app no se cierra.
                    ConditionsUi.Error
                }
            }
        }
    }

    /** Agrega o quita la playa de favoritas. */
    fun toggleFavorite() {
        val beach = uiState.value.beach ?: return
        viewModelScope.launch { beachRepository.setFavorite(beach.id, !beach.isFavorite) }
    }

    companion object {
        /** Nombre del argumento en la ruta tipada (propiedad de BeachDetailRoute). */
        const val BEACH_ID_KEY = "beachId"
        private const val INVALID_ID = -1L
    }
}
