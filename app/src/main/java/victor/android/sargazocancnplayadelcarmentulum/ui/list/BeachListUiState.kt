package victor.android.sargazocancnplayadelcarmentulum.ui.list

import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel

/**
 * Estado de la lista de playas del semáforo.
 *
 * @property hasSemaforo false si todavía no hay ninguna playa con nivel (no hay semáforo guardado).
 * @property selectedLevels niveles marcados en el filtro; vacío significa "todos".
 * @property beaches playas que cumplen el buscador y el filtro, ordenadas por nombre.
 */
data class BeachListUiState(
    val isLoading: Boolean = true,
    val hasSemaforo: Boolean = false,
    val query: String = "",
    val selectedLevels: Set<SargassumLevel> = emptySet(),
    val beaches: List<BeachStatus> = emptyList(),
) {
    val hasActiveFilters: Boolean get() = query.isNotBlank() || selectedLevels.isNotEmpty()
}
