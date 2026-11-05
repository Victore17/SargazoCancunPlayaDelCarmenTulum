package victor.android.sargazocancnplayadelcarmentulum.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.MapRegion
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.ui.components.LevelDot
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.color
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.labelRes
import victor.android.sargazocancnplayadelcarmentulum.ui.util.formatLong
import java.time.LocalDate

/** Pantalla principal: mapa de OpenStreetMap con un círculo de color por playa. */
@Composable
fun MapScreen(
    onBeachClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MapContent(
        uiState = uiState,
        onBeachClick = onBeachClick,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
private fun MapContent(
    uiState: MapUiState,
    onBeachClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (LocalInspectionMode.current) {
            // MapLibre no se puede dibujar en la vista previa del IDE.
            Box(modifier = Modifier.fillMaxSize())
        } else {
            BeachMap(beaches = uiState.beaches, onBeachClick = onBeachClick, modifier = Modifier.fillMaxSize())
        }
        MapInfoCard(
            uiState = uiState,
            onRefresh = onRefresh,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
        )
    }
}

/**
 * Tarjeta inferior. Compacta por defecto para no tapar el mapa: fecha del semáforo, leyenda y la
 * atribución de OpenStreetMap (siempre visible). "Ver más" muestra la fuente de los datos, el
 * aviso de que no es una app oficial y las playas sin ubicación.
 */
@Composable
private fun MapInfoCard(
    uiState: MapUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = uiState.latestDate?.let { stringResource(R.string.map_semaforo_date, it.formatLong()) }
                        ?: stringResource(R.string.map_no_semaforo),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (uiState.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.padding(start = 12.dp).size(16.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = onRefresh) { Text(stringResource(R.string.map_refresh)) }
                }
            }
            if (uiState.showSyncError) {
                Text(
                    text = stringResource(R.string.map_sync_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
            LevelLegend()
            if (expanded) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (uiState.withoutLocationCount > 0) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.map_beaches_without_location,
                                uiState.withoutLocationCount,
                                uiState.withoutLocationCount,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.map_attribution),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f, fill = false),
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(stringResource(if (expanded) R.string.map_show_less else R.string.map_show_more))
                }
            }
        }
    }
}

/** Leyenda compacta: solo los círculos de color, en el orden del semáforo. */
@Composable
private fun LevelLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LEGEND_ORDER.forEach { level ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                LevelDot(level, size = 12.dp)
                Text(
                    text = stringResource(level.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

private val LEGEND_ORDER = listOf(
    SargassumLevel.EXCESSIVE,
    SargassumLevel.ABUNDANT,
    SargassumLevel.MODERATE,
    SargassumLevel.LOW,
    SargassumLevel.NO_SARGASSUM,
)

/** Mapa con la cámara restringida a [MapRegion] y una capa de círculos por nivel. */
@Composable
private fun BeachMap(
    beaches: List<MapBeach>,
    onBeachClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // El contenido del estilo (capas) se guarda dentro de MapState: se leen estados
    // actualizados para que el mapa cambie cuando llegan nuevos datos.
    val beachesByLevel by rememberUpdatedState(remember(beaches) { beaches.groupBy { it.level } })
    val currentOnBeachClick by rememberUpdatedState(onBeachClick)

    val mapState = rememberMapState(
        baseStyle = OpenStreetMapStyle.baseStyle,
        initialCameraPosition = CameraPosition(
            center = MapRegion.initialCenter.toPosition(),
            zoom = MapRegion.INITIAL_ZOOM.toDouble(),
        ),
    ) {
        BeachLayers(beachesByLevel = beachesByLevel, onBeachClick = { currentOnBeachClick(it) })
    }
    val mapDescription = stringResource(R.string.map_content_description)

    MaplibreMap(
        modifier = modifier.semantics { contentDescription = mapDescription },
        state = mapState,
        cameraConstraints = CameraConstraints {
            boundingBox = BoundingBox(
                southwest = MapRegion.bounds.southwest.toPosition(),
                northeast = MapRegion.bounds.northeast.toPosition(),
            )
            minZoom = MapRegion.MIN_ZOOM.toDouble()
            maxZoom = MapRegion.MAX_ZOOM.toDouble()
        },
    )
}

/** Una capa de círculos por nivel (se dibuja dentro del estilo del mapa). */
@Composable
private fun BeachLayers(
    beachesByLevel: Map<SargassumLevel, List<MapBeach>>,
    onBeachClick: (Long) -> Unit,
) {
    // Primero "sin dato" (gris) y al final rojo, para que los niveles altos queden encima.
    DRAW_ORDER.forEach { level ->
        val levelBeaches = beachesByLevel[level].orEmpty()
        key(level) {
            val source = rememberGeoJsonSource(
                data = GeoJsonData.Features(levelBeaches.toFeatureCollection()),
            )
            CircleLayer(
                id = "beaches-${level.name.lowercase()}",
                source = source,
                color = const(level.color),
                radius = const(7.dp),
                strokeColor = const(Color.White),
                strokeWidth = const(1.5.dp),
                onClick = { features ->
                    val beachId = features.firstOrNull()?.properties?.get(PROPERTY_ID)?.jsonPrimitive?.longOrNull
                    if (beachId != null) {
                        onBeachClick(beachId)
                        ClickResult.Consume
                    } else {
                        ClickResult.Pass
                    }
                },
            )
        }
    }
}

private val DRAW_ORDER = listOf(
    SargassumLevel.NO_DATA,
    SargassumLevel.NO_SARGASSUM,
    SargassumLevel.LOW,
    SargassumLevel.MODERATE,
    SargassumLevel.ABUNDANT,
    SargassumLevel.EXCESSIVE,
)

private const val PROPERTY_ID = "id"

private fun List<MapBeach>.toFeatureCollection() = FeatureCollection(
    map { beach ->
        Feature(
            geometry = Point(Position(longitude = beach.longitude, latitude = beach.latitude)),
            properties = buildJsonObject { put(PROPERTY_ID, beach.id) },
        )
    },
)

/** MapLibre usa el orden (longitud, latitud). */
private fun GeoPoint.toPosition() = Position(longitude = longitude, latitude = latitude)

@Preview(showBackground = true)
@Composable
private fun MapContentPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        MapContent(
            uiState = MapUiState(isLoading = false, latestDate = LocalDate.of(2026, 11, 3), withoutLocationCount = 2),
            onBeachClick = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapContentSyncErrorPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        MapContent(
            uiState = MapUiState(isLoading = false, showSyncError = true),
            onBeachClick = {},
            onRefresh = {},
        )
    }
}
