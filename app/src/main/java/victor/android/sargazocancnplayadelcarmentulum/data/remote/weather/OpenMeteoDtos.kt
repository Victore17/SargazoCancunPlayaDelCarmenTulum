package victor.android.sargazocancnplayadelcarmentulum.data.remote.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Respuestas de Open-Meteo. Todos los campos son opcionales: la API devuelve null cuando no
// tiene dato (por ejemplo, el oleaje en un punto de tierra).

@Serializable
data class ForecastResponse(
    val current: CurrentWeatherDto? = null,
)

@Serializable
data class CurrentWeatherDto(
    val time: String? = null,
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerialName("relative_humidity_2m") val humidity: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("wind_speed_10m") val windSpeed: Double? = null,
    @SerialName("wind_direction_10m") val windDirection: Double? = null,
)

@Serializable
data class MarineResponse(
    val current: CurrentMarineDto? = null,
)

@Serializable
data class CurrentMarineDto(
    val time: String? = null,
    @SerialName("wave_height") val waveHeight: Double? = null,
    @SerialName("wave_direction") val waveDirection: Double? = null,
    @SerialName("wave_period") val wavePeriod: Double? = null,
    @SerialName("sea_surface_temperature") val seaSurfaceTemperature: Double? = null,
)
