package victor.android.sargazocancnplayadelcarmentulum.domain.model

/** Rectángulo geográfico definido por sus esquinas suroeste y noreste. */
data class GeoBounds(
    val southwest: GeoPoint,
    val northeast: GeoPoint,
) {
    operator fun contains(point: GeoPoint): Boolean =
        point.latitude in southwest.latitude..northeast.latitude &&
            point.longitude in southwest.longitude..northeast.longitude
}

/**
 * Zona que cubre el mapa: de Holbox y Cabo Catoche a Tulum, incluyendo Isla Mujeres y Cozumel.
 * La cámara no puede salir de [bounds] y no se puede alejar más de [MIN_ZOOM].
 */
object MapRegion {
    val bounds = GeoBounds(
        southwest = GeoPoint(latitude = 19.95, longitude = -87.60),
        northeast = GeoPoint(latitude = 21.70, longitude = -86.50),
    )

    /** Vista inicial: centrada en la costa, de modo que se vea toda la zona. */
    val initialCenter = GeoPoint(latitude = 20.85, longitude = -87.05)
    const val INITIAL_ZOOM = 7.5f
    const val MIN_ZOOM = 7f
    const val MAX_ZOOM = 11f
}
