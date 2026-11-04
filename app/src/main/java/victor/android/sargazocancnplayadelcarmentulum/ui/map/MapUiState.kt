package victor.android.sargazocancnplayadelcarmentulum.ui.map

import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

/** Playa que se dibuja en el mapa como un círculo del color de su nivel. */
data class MapBeach(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val level: SargassumLevel,
)

/**
 * Estado de la pantalla del mapa.
 *
 * @property latestDate fecha del semáforo más reciente guardado, o null si todavía no hay ninguno.
 * @property withoutLocationCount playas del semáforo sin coordenadas (no salen en el mapa).
 * @property showSyncError true si no se pudo descargar de Firestore (se muestra lo guardado en Room).
 */
data class MapUiState(
    val isLoading: Boolean = true,
    val beaches: List<MapBeach> = emptyList(),
    val latestDate: LocalDate? = null,
    val withoutLocationCount: Int = 0,
    val isRefreshing: Boolean = false,
    val showSyncError: Boolean = false,
)
