package victor.android.sargazocancnplayadelcarmentulum.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.ui.components.BeachRow
import victor.android.sargazocancnplayadelcarmentulum.ui.components.DataSourceNotice
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/** Playas favoritas con su nivel actual. */
@Composable
fun FavoritesScreen(
    onBeachClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesContent(
        uiState = uiState,
        onBeachClick = onBeachClick,
        onRemoveFavorite = viewModel::removeFavorite,
        modifier = modifier,
    )
}

@Composable
private fun FavoritesContent(
    uiState: FavoritesUiState,
    onBeachClick: (Long) -> Unit,
    onRemoveFavorite: (BeachStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        uiState.favorites.isEmpty() -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.favorites_empty_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.favorites_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        else -> LazyColumn(modifier = modifier.fillMaxSize()) {
            items(uiState.favorites, key = { it.beach.id }) { status ->
                BeachRow(
                    status = status,
                    onClick = { onBeachClick(status.beach.id) },
                    onToggleFavorite = { onRemoveFavorite(status) },
                )
                HorizontalDivider()
            }
            item {
                DataSourceNotice(modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        FavoritesContent(
            uiState = FavoritesUiState(
                isLoading = false,
                favorites = listOf(
                    BeachStatus(
                        Beach(1, "Playa Delfines", GeoPoint(21.05, -86.78), null, true, true),
                        SargassumLevel.EXCESSIVE,
                        null,
                    ),
                ),
            ),
            onBeachClick = {},
            onRemoveFavorite = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesEmptyPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        FavoritesContent(
            uiState = FavoritesUiState(isLoading = false),
            onBeachClick = {},
            onRemoveFavorite = {},
        )
    }
}
