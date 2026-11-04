package victor.android.sargazocancnplayadelcarmentulum.domain.model

/** Coordenadas geográficas de una playa. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

/**
 * Posición de una playa dentro de la imagen del semáforo,
 * expresada en coordenadas relativas (0.0 a 1.0) para no depender del tamaño de la imagen.
 */
data class ImagePosition(
    val x: Float,
    val y: Float,
)

/**
 * Playa que aparece en el semáforo.
 *
 * @property location null cuando la playa se leyó del semáforo pero no tiene coordenadas conocidas
 * ("sin ubicación").
 * @property imagePosition null mientras no se haya ubicado la playa en la imagen.
 */
data class Beach(
    val id: Long,
    val name: String,
    val location: GeoPoint?,
    val imagePosition: ImagePosition?,
    val isFavorite: Boolean,
    val notificationsEnabled: Boolean,
)
