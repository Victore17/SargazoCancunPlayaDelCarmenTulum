package victor.android.sargazocancnplayadelcarmentulum.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
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
import victor.android.sargazocancnplayadelcarmentulum.ui.components.LevelDot
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.labelRes

/** Los niveles que se pueden filtrar: los de un semáforo, sin "Sin dato". */
private val filterLevels = SargassumLevel.entries.filter { it != SargassumLevel.NO_DATA }

/** Lista de playas del semáforo con buscador y filtro por color. */
@Composable
fun BeachListScreen(
    onBeachClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BeachListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BeachListContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onToggleLevel = viewModel::toggleLevel,
        onClearFilters = viewModel::clearFilters,
        onBeachClick = onBeachClick,
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = modifier,
    )
}

@Composable
private fun BeachListContent(
    uiState: BeachListUiState,
    onQueryChange: (String) -> Unit,
    onToggleLevel: (SargassumLevel) -> Unit,
    onClearFilters: () -> Unit,
    onBeachClick: (Long) -> Unit,
    onToggleFavorite: (BeachStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            !uiState.hasSemaforo -> MessageBox(stringResource(R.string.list_no_semaforo))
            else -> {
                SearchField(query = uiState.query, onQueryChange = onQueryChange)
                LevelFilters(selected = uiState.selectedLevels, onToggle = onToggleLevel)
                if (uiState.beaches.isEmpty()) {
                    MessageBox(
                        message = stringResource(R.string.list_no_results),
                        actionLabel = stringResource(R.string.list_clear_filters),
                        onAction = onClearFilters,
                    )
                } else {
                    Text(
                        text = pluralStringResource(
                            R.plurals.list_result_count,
                            uiState.beaches.size,
                            uiState.beaches.size,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    BeachItems(
                        beaches = uiState.beaches,
                        onBeachClick = onBeachClick,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.list_search_hint)) },
        leadingIcon = {
            Icon(painter = painterResource(R.drawable.ic_search), contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.list_search_clear),
                    )
                }
            }
        },
    )
}

@Composable
private fun LevelFilters(
    selected: Set<SargassumLevel>,
    onToggle: (SargassumLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filterLevels) { level ->
            FilterChip(
                selected = level in selected,
                onClick = { onToggle(level) },
                label = { Text(stringResource(level.labelRes)) },
                leadingIcon = { LevelDot(level = level, size = 14.dp) },
            )
        }
    }
}

@Composable
private fun BeachItems(
    beaches: List<BeachStatus>,
    onBeachClick: (Long) -> Unit,
    onToggleFavorite: (BeachStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(beaches, key = { it.beach.id }) { status ->
            BeachRow(
                status = status,
                onClick = { onBeachClick(status.beach.id) },
                onToggleFavorite = { onToggleFavorite(status) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun MessageBox(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        if (actionLabel != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

private fun previewBeaches() = listOf(
    BeachStatus(
        Beach(1, "Playa Delfines", GeoPoint(21.05, -86.78), null, isFavorite = true, notificationsEnabled = true),
        SargassumLevel.EXCESSIVE,
        null,
    ),
    BeachStatus(
        Beach(2, "Playa nueva", null, null, isFavorite = false, notificationsEnabled = true),
        SargassumLevel.LOW,
        null,
    ),
)

@Preview(showBackground = true)
@Composable
private fun BeachListScreenPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        BeachListContent(
            uiState = BeachListUiState(
                isLoading = false,
                hasSemaforo = true,
                selectedLevels = setOf(SargassumLevel.LOW),
                beaches = previewBeaches(),
            ),
            onQueryChange = {},
            onToggleLevel = {},
            onClearFilters = {},
            onBeachClick = {},
            onToggleFavorite = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BeachListEmptyPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        BeachListContent(
            uiState = BeachListUiState(isLoading = false, hasSemaforo = false),
            onQueryChange = {},
            onToggleLevel = {},
            onClearFilters = {},
            onBeachClick = {},
            onToggleFavorite = {},
        )
    }
}
