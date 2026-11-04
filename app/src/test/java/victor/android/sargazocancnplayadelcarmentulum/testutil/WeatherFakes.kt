package victor.android.sargazocancnplayadelcarmentulum.testutil

import kotlinx.coroutines.CompletableDeferred
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.CurrentMarineDto
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.CurrentWeatherDto
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.ForecastApiService
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.ForecastResponse
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.MarineApiService
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.MarineResponse
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.WeatherRepository

/** Respuestas de ejemplo con los valores reales que devolvió Open-Meteo para Playa del Carmen. */
object WeatherSamples {
    val airDto = CurrentWeatherDto(
        time = "2026-11-03T23:15",
        temperature = 26.3,
        apparentTemperature = 30.2,
        humidity = 78.0,
        weatherCode = 0,
        windSpeed = 6.7,
        windDirection = 106.0,
    )
    val seaDto = CurrentMarineDto(
        time = "2026-11-03T23:15",
        waveHeight = 0.28,
        waveDirection = 105.0,
        wavePeriod = 5.0,
        seaSurfaceTemperature = 29.4,
    )
}

/** API del clima falsa: devuelve la respuesta configurada o lanza el error configurado. */
class FakeForecastApi : ForecastApiService {
    var response = ForecastResponse(current = WeatherSamples.airDto)
    var error: Exception? = null
    val requests = mutableListOf<Pair<Double, Double>>()

    override suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        current: String,
        timezone: String,
    ): ForecastResponse {
        requests += latitude to longitude
        error?.let { throw it }
        return response
    }
}

/** API del mar falsa. */
class FakeMarineApi : MarineApiService {
    var response = MarineResponse(current = WeatherSamples.seaDto)
    var error: Exception? = null
    val requests = mutableListOf<Pair<Double, Double>>()

    override suspend fun getCurrentSea(
        latitude: Double,
        longitude: Double,
        current: String,
        timezone: String,
    ): MarineResponse {
        requests += latitude to longitude
        error?.let { throw it }
        return response
    }
}

/** Repositorio del clima falso para probar ViewModels. */
class FakeWeatherRepository : WeatherRepository {
    var conditions: BeachConditions? = null
    var error: Exception? = null
    val requests = mutableListOf<GeoPoint>()

    /** Si no es null, getConditions() espera a que se complete (para probar el estado "cargando"). */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getConditions(location: GeoPoint): BeachConditions {
        requests += location
        gate?.await()
        error?.let { throw it }
        return requireNotNull(conditions) { "Falta configurar FakeWeatherRepository.conditions" }
    }
}
