package victor.android.sargazocancnplayadelcarmentulum.domain.repository

import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint

/** Clima y estado del mar de una ubicación. */
interface WeatherRepository {

    /**
     * Consulta el clima y el mar en [location]. Si solo una de las dos consultas falla, el
     * resultado trae la otra. Lanza una excepción si fallan las dos (por ejemplo, sin conexión).
     */
    suspend fun getConditions(location: GeoPoint): BeachConditions
}
