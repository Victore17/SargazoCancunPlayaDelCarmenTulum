package victor.android.sargazocancnplayadelcarmentulum.ui.map

/** Estado de la pantalla del mapa. En la etapa 2 se agregan las playas a dibujar. */
data class MapUiState(
    val isLoading: Boolean = true,
    val knownBeachCount: Int = 0,
)
