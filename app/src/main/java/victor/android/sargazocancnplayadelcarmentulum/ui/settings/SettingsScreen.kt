package victor.android.sargazocancnplayadelcarmentulum.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/** Ajustes de notificaciones. */
@Composable
fun SettingsScreen(
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onAllChanged = viewModel::onAllNotificationsChanged,
        onGeneralUpdateChanged = viewModel::onGeneralUpdateChanged,
        onFavoriteChangesChanged = viewModel::onFavoriteChangesChanged,
        onBeachChanged = viewModel::onBeachNotificationsChanged,
        onAboutClick = onAboutClick,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onAllChanged: (Boolean) -> Unit,
    onGeneralUpdateChanged: (Boolean) -> Unit,
    onFavoriteChangesChanged: (Boolean) -> Unit,
    onBeachChanged: (Long, Boolean) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val settings = uiState.settings
    val detailEnabled = uiState.areDetailSwitchesEnabled
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            SectionTitle(R.string.settings_section_general)
            SwitchRow(
                title = stringResource(R.string.settings_all_title),
                description = stringResource(R.string.settings_all_description),
                checked = settings.allNotificationsEnabled,
                enabled = true,
                onCheckedChange = onAllChanged,
            )
            HorizontalDivider()
            SwitchRow(
                title = stringResource(R.string.settings_general_update_title),
                description = stringResource(R.string.settings_general_update_description),
                checked = settings.generalUpdateNotificationsEnabled,
                enabled = detailEnabled,
                onCheckedChange = onGeneralUpdateChanged,
            )
            SwitchRow(
                title = stringResource(R.string.settings_favorite_changes_title),
                description = stringResource(R.string.settings_favorite_changes_description),
                checked = settings.favoriteChangeNotificationsEnabled,
                enabled = detailEnabled,
                onCheckedChange = onFavoriteChangesChanged,
            )
            HorizontalDivider()
            SectionTitle(R.string.settings_section_favorites)
        }
        if (uiState.favorites.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.settings_favorites_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        } else {
            items(uiState.favorites, key = { it.id }) { beach ->
                SwitchRow(
                    title = beach.name,
                    description = null,
                    checked = beach.notificationsEnabled,
                    enabled = detailEnabled,
                    onCheckedChange = { onBeachChanged(beach.id, it) },
                )
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_about_title)) },
                supportingContent = { Text(stringResource(R.string.settings_about_description)) },
                trailingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(onClick = onAboutClick),
            )
        }
    }
}

@Composable
private fun SectionTitle(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

/** Fila con título, descripción opcional e interruptor. */
@Composable
private fun SwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(it) } },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        },
    )
}

private val previewFavorites = listOf(
    Beach(1, "Playa Delfines", null, null, true, true),
    Beach(2, "Playa Norte", null, null, true, false),
)

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        SettingsContent(
            uiState = SettingsUiState(isLoading = false, favorites = previewFavorites),
            onAllChanged = {},
            onGeneralUpdateChanged = {},
            onFavoriteChangesChanged = {},
            onBeachChanged = { _, _ -> },
            onAboutClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsAllOffPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        SettingsContent(
            uiState = SettingsUiState(
                isLoading = false,
                settings = AppSettings(allNotificationsEnabled = false),
                favorites = previewFavorites,
            ),
            onAllChanged = {},
            onGeneralUpdateChanged = {},
            onFavoriteChangesChanged = {},
            onBeachChanged = { _, _ -> },
            onAboutClick = {},
        )
    }
}
