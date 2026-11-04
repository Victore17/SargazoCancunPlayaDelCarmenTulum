package victor.android.sargazocancnplayadelcarmentulum.ui.map

import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.maplibre.compose.style.BaseStyle

/**
 * Estilo de MapLibre con los mosaicos públicos de OpenStreetMap. No necesita cuenta ni clave.
 * Respetar la política de uso de mosaicos: uso ligero y atribución visible.
 */
object OpenStreetMapStyle {
    const val ATTRIBUTION = "© OpenStreetMap contributors"
    private const val TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
    private const val SOURCE_ID = "osm"

    val baseStyle: BaseStyle = BaseStyle.Json {
        put("version", 8)
        putJsonObject("sources") {
            putJsonObject(SOURCE_ID) {
                put("type", "raster")
                putJsonArray("tiles") { add(TILE_URL) }
                put("tileSize", 256)
                put("maxzoom", 19)
                put("attribution", ATTRIBUTION)
            }
        }
        putJsonArray("layers") {
            add(
                kotlinx.serialization.json.buildJsonObject {
                    put("id", "osm-raster")
                    put("type", "raster")
                    put("source", SOURCE_ID)
                },
            )
        }
    }
}
