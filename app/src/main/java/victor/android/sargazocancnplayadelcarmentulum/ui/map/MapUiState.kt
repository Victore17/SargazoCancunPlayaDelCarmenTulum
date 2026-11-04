package victor.android.sargazocancnplayadelcarmentulum.ui.map

/**
 * Estado de la pantalla del mapa. En la etapa 3 se agregan las playas con su color.
 *
 * @property knownBeachCount playas con ubicación conocida en el mapa.
 */
data class MapUiState(
    val isLoading: Boolean = true,
    val knownBeachCount: Int = 0,
)
