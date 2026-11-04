package victor.android.sargazocancnplayadelcarmentulum.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.MapRegion
import victor.android.sargazocancnplayadelcarmentulum.ui.components.DataSourceNotice
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.SargazoCancúnPlayaDelCarmenTulumTheme

/** Pantalla principal: mapa de OpenStreetMap limitado a la zona de playas. */
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
    Box(modifier = modifier.fillMaxSize()) {
        if (LocalInspectionMode.current) {
            // MapLibre no se puede dibujar en la vista previa del IDE.
            Box(modifier = Modifier.fillMaxSize())
        } else {
            BeachMap(modifier = Modifier.fillMaxSize())
        }
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DataSourceNotice()
                Text(
                    text = stringResource(R.string.map_attribution),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/** Mapa con la cámara restringida a [MapRegion]. */
@Composable
private fun BeachMap(modifier: Modifier = Modifier) {
    val mapState = rememberMapState(
        baseStyle = OpenStreetMapStyle.baseStyle,
        initialCameraPosition = CameraPosition(
            center = MapRegion.initialCenter.toPosition(),
            zoom = MapRegion.INITIAL_ZOOM.toDouble(),
        ),
    )
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

/** MapLibre usa el orden (longitud, latitud). */
private fun GeoPoint.toPosition() = Position(longitude = longitude, latitude = latitude)

@Preview(showBackground = true)
@Composable
private fun MapContentPreview() {
    SargazoCancúnPlayaDelCarmenTulumTheme {
        MapContent(uiState = MapUiState(isLoading = false, knownBeachCount = 31))
    }
}
