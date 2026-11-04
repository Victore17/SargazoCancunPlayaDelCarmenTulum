package victor.android.sargazocancnplayadelcarmentulum.ui.favorites

import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus

/** Estado de la pantalla de favoritas: cada una con su nivel actual. */
data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favorites: List<BeachStatus> = emptyList(),
)
