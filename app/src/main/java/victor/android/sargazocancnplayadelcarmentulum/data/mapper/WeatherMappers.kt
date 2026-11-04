package victor.android.sargazocancnplayadelcarmentulum.data.mapper

import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.CurrentMarineDto
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.CurrentWeatherDto
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AirConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SeaConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.WeatherCondition
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

// Conversión de las respuestas de Open-Meteo a modelos de dominio.

/** Sin temperatura no hay clima que mostrar: devuelve null. */
fun CurrentWeatherDto.toDomain(): AirConditions? {
    val temperature = temperature ?: return null
    return AirConditions(
        temperatureCelsius = temperature,
        apparentTemperatureCelsius = apparentTemperature,
        humidityPercent = humidity?.roundToInt(),
        weather = WeatherCondition.fromWmoCode(weatherCode),
        windSpeedKmh = windSpeed,
        windDirectionDegrees = windDirection,
    )
}

/** Si no trae ningún dato del mar (punto en tierra), devuelve null. */
fun CurrentMarineDto.toDomain(): SeaConditions? {
    if (waveHeight == null && wavePeriod == null && seaSurfaceTemperature == null) return null
    return SeaConditions(
        waveHeightMeters = waveHeight,
        waveDirectionDegrees = waveDirection,
        wavePeriodSeconds = wavePeriod,
        seaTemperatureCelsius = seaSurfaceTemperature,
    )
}

/** Open-Meteo entrega la hora como "2026-11-03T23:15" (sin segundos ni zona). */
fun parseObservationTime(time: String?): LocalDateTime? = try {
    time?.let(LocalDateTime::parse)
} catch (_: DateTimeParseException) {
    null
}
