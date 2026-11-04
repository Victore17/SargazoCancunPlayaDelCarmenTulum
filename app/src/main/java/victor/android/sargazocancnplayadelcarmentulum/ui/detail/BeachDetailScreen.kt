package victor.android.sargazocancnplayadelcarmentulum.ui.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AirConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.CompassDirection
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SeaConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.WeatherCondition
import victor.android.sargazocancnplayadelcarmentulum.ui.components.DataSourceNotice
import victor.android.sargazocancnplayadelcarmentulum.ui.components.LevelDot
import victor.android.sargazocancnplayadelcarmentulum.ui.components.LevelLabel
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.color
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.labelRes
import victor.android.sargazocancnplayadelcarmentulum.ui.util.formatLong
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Detalle de una playa: nivel actual, historial, clima y mar, y botón de favorita. */
@Composable
fun BeachDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BeachDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BeachDetailContent(
        uiState = uiState,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onRetry = viewModel::loadConditions,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeachDetailContent(
    uiState: BeachDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.beach?.name ?: stringResource(R.string.screen_beach_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                actions = {
                    if (uiState.beach != null) {
                        FavoriteButton(isFavorite = uiState.isFavorite, onClick = onToggleFavorite)
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            uiState.notFound -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.detail_not_found),
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center,
                )
            }
            else -> DetailList(uiState = uiState, onRetry = onRetry, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(if (isFavorite) R.drawable.ic_nav_favorite else R.drawable.ic_favorite_border),
            contentDescription = stringResource(
                if (isFavorite) R.string.detail_favorite_remove else R.string.detail_favorite_add,
            ),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailList(
    uiState: BeachDetailUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { CurrentLevelCard(uiState) }
        item { ConditionsCard(uiState.conditions, onRetry) }
        item { HistoryCard(uiState.history) }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(R.string.detail_weather_credit), style = MaterialTheme.typography.bodySmall)
                DataSourceNotice()
            }
        }
    }
}

/** Nivel actual con su color y la fecha de actualización. */
@Composable
private fun CurrentLevelCard(uiState: BeachDetailUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LevelDot(uiState.currentLevel, size = 48.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.detail_current_level), style = MaterialTheme.typography.labelMedium)
                Text(
                    text = stringResource(uiState.currentLevel.labelRes),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (uiState.currentLevel == SargassumLevel.NO_DATA) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                if (uiState.updatedOn != null) {
                    Text(
                        text = stringResource(R.string.detail_updated, uiState.updatedOn.formatLong()),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(stringResource(R.string.detail_no_level), style = MaterialTheme.typography.bodySmall)
                }
                if (uiState.beach?.location == null) {
                    Text(
                        text = stringResource(R.string.detail_no_location),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConditionsCard(conditions: ConditionsUi, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.detail_conditions_title), style = MaterialTheme.typography.titleMedium)
            when (conditions) {
                ConditionsUi.Loading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(stringResource(R.string.detail_conditions_loading))
                }
                ConditionsUi.NoLocation -> Text(stringResource(R.string.detail_conditions_no_location))
                ConditionsUi.Error -> {
                    Text(stringResource(R.string.detail_conditions_error), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) { Text(stringResource(R.string.detail_retry)) }
                }
                is ConditionsUi.Loaded -> ConditionsValues(conditions.conditions)
            }
        }
    }
}

@Composable
private fun ConditionsValues(conditions: BeachConditions) {
    val air = conditions.air
    if (air != null) {
        AirRows(air)
    } else {
        Text(stringResource(R.string.detail_conditions_no_air))
    }
    val sea = conditions.sea
    if (sea != null) {
        SeaRows(sea)
    } else {
        Text(stringResource(R.string.detail_conditions_no_sea))
    }
    conditions.observedAt?.let {
        Text(
            text = stringResource(R.string.detail_observed_at, it.format(timeFormatter)),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun AirRows(air: AirConditions) {
    ValueRow(R.string.detail_label_temperature, stringResource(R.string.detail_value_celsius, air.temperatureCelsius))
    Text(stringResource(air.weather.labelRes), style = MaterialTheme.typography.bodyMedium)
    air.apparentTemperatureCelsius?.let {
        ValueRow(R.string.detail_label_feels_like, stringResource(R.string.detail_value_celsius, it))
    }
    air.humidityPercent?.let {
        ValueRow(R.string.detail_label_humidity, stringResource(R.string.detail_value_percent, it))
    }
    if (air.windSpeedKmh != null) {
        val windText = air.windDirectionDegrees?.let { degrees ->
            stringResource(
                R.string.detail_value_wind,
                air.windSpeedKmh,
                stringResource(CompassDirection.fromDegrees(degrees).labelRes),
            )
        } ?: stringResource(R.string.detail_value_wind_speed, air.windSpeedKmh)
        ValueRow(R.string.detail_label_wind, windText)
    }
}

@Composable
private fun SeaRows(sea: SeaConditions) {
    sea.seaState?.let { ValueRow(R.string.detail_label_sea_state, stringResource(it.labelRes)) }
    if (sea.waveHeightMeters != null) {
        val text = if (sea.wavePeriodSeconds != null) {
            stringResource(R.string.detail_value_waves_period, sea.waveHeightMeters, sea.wavePeriodSeconds)
        } else {
            stringResource(R.string.detail_value_waves, sea.waveHeightMeters)
        }
        ValueRow(R.string.detail_label_waves, text)
    }
    sea.seaTemperatureCelsius?.let {
        ValueRow(R.string.detail_label_sea_temperature, stringResource(R.string.detail_value_celsius, it))
    }
}

@Composable
private fun ValueRow(@StringRes labelRes: Int, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

@Composable
private fun HistoryCard(history: List<LevelRecord>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.detail_history_title), style = MaterialTheme.typography.titleMedium)
            if (history.isEmpty()) {
                Text(stringResource(R.string.detail_history_empty), style = MaterialTheme.typography.bodyMedium)
            } else {
                // Tira de colores: una marca por registro, del más antiguo al más reciente.
                LevelStrip(history.reversed().map { it.level })
                history.forEach { record -> HistoryRow(record) }
            }
        }
    }
}

@Composable
private fun LevelStrip(levels: List<SargassumLevel>) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
        levels.forEach { level ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .background(level.color, RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
private fun HistoryRow(record: LevelRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(record.date.formatLong(), style = MaterialTheme.typography.bodyMedium)
        LevelLabel(record.level)
    }
}

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

// --- Vistas previas ---

private val previewBeach = Beach(
    id = 1,
    name = "Punta Molas",
    location = GeoPoint(20.59, -86.72),
    imagePosition = null,
    isFavorite = true,
    notificationsEnabled = true,
)

private val previewHistory = listOf(
    LevelRecord(1, 1, SargassumLevel.EXCESSIVE, LocalDate.of(2026, 11, 3)),
    LevelRecord(2, 1, SargassumLevel.ABUNDANT, LocalDate.of(2026, 11, 2)),
    LevelRecord(3, 1, SargassumLevel.MODERATE, LocalDate.of(2026, 11, 1)),
)

private val previewConditions = BeachConditions(
    air = AirConditions(26.3, 30.2, 78, WeatherCondition.PARTLY_CLOUDY, 6.7, 106.0),
    sea = SeaConditions(0.28, 105.0, 5.0, 29.4),
    observedAt = LocalDateTime.of(2026, 11, 3, 23, 15),
)

@Preview(showBackground = true)
@Composable
private fun BeachDetailLoadedPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        BeachDetailContent(
            uiState = BeachDetailUiState(
                isLoading = false,
                beach = previewBeach,
                currentLevel = SargassumLevel.EXCESSIVE,
                updatedOn = LocalDate.of(2026, 11, 3),
                history = previewHistory,
                conditions = ConditionsUi.Loaded(previewConditions),
            ),
            onBack = {},
            onToggleFavorite = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BeachDetailErrorPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        BeachDetailContent(
            uiState = BeachDetailUiState(
                isLoading = false,
                beach = previewBeach.copy(isFavorite = false, location = null),
                conditions = ConditionsUi.Error,
            ),
            onBack = {},
            onToggleFavorite = {},
            onRetry = {},
        )
    }
}
