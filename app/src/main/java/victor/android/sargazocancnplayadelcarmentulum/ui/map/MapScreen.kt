package victor.android.sargazocancnplayadelcarmentulum.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.ui.components.DataSourceNotice
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/** Pantalla del mapa. Por ahora es provisional; el mapa real se construye en la etapa 2. */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MapContent(uiState = uiState, modifier = modifier)
}

@Composable
private fun MapContent(
    uiState: MapUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.screen_map_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        if (uiState.isLoading) {
            CircularProgressIndicator()
            Text(stringResource(R.string.map_loading))
        } else {
            Text(
                text = pluralStringResource(
                    R.plurals.map_known_beaches,
                    uiState.knownBeachCount,
                    uiState.knownBeachCount,
                ),
                textAlign = TextAlign.Center,
            )
        }
        DataSourceNotice()
    }
}

@Preview(showBackground = true)
@Composable
private fun MapContentPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        MapContent(uiState = MapUiState(isLoading = false, knownBeachCount = 31))
    }
}
