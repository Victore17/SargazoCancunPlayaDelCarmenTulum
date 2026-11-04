package victor.android.sargazocancnplayadelcarmentulum.domain.model

import java.time.LocalDateTime

/** Condiciones del cielo, según el código meteorológico WMO que entrega Open-Meteo. */
enum class WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    RAIN,
    RAIN_SHOWERS,
    THUNDERSTORM,
    UNKNOWN,
    ;

    companion object {
        /** Traduce el código WMO (https://open-meteo.com/en/docs) a una condición simple. */
        fun fromWmoCode(code: Int?): WeatherCondition = when (code) {
            0 -> CLEAR
            1 -> MAINLY_CLEAR
            2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            in 51..57 -> DRIZZLE
            in 61..67 -> RAIN
            in 80..82 -> RAIN_SHOWERS
            in 95..99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}

/** Punto cardinal (8 direcciones). */
enum class CompassDirection {
    N, NE, E, SE, S, SW, W, NW,
    ;

    companion object {
        /** Dirección para [degrees] (0° = norte, 90° = este). Acepta valores fuera de 0–360. */
        fun fromDegrees(degrees: Double): CompassDirection {
            val normalized = ((degrees % 360) + 360) % 360
            val index = ((normalized + 22.5) / 45).toInt() % 8
            return entries[index]
        }
    }
}

/** Estado del mar según la altura de las olas. */
enum class SeaState {
    CALM,
    SLIGHT,
    MODERATE,
    ROUGH,
    ;

    companion object {
        fun fromWaveHeight(meters: Double): SeaState = when {
            meters < 0.5 -> CALM
            meters < 1.25 -> SLIGHT
            meters < 2.5 -> MODERATE
            else -> ROUGH
        }
    }
}

/** Clima en el aire. [windDirectionDegrees] es de dónde viene el viento. */
data class AirConditions(
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double?,
    val humidityPercent: Int?,
    val weather: WeatherCondition,
    val windSpeedKmh: Double?,
    val windDirectionDegrees: Double?,
)

/** Estado del mar. Cualquier dato puede faltar (por ejemplo, en lagunas o puntos en tierra). */
data class SeaConditions(
    val waveHeightMeters: Double?,
    val waveDirectionDegrees: Double?,
    val wavePeriodSeconds: Double?,
    val seaTemperatureCelsius: Double?,
) {
    val seaState: SeaState? get() = waveHeightMeters?.let(SeaState::fromWaveHeight)
}

/**
 * Clima y mar de una playa en este momento. [air] o [sea] es null cuando esa consulta
 * falló o no devolvió datos; nunca son los dos null (en ese caso el repositorio lanza un error).
 */
data class BeachConditions(
    val air: AirConditions?,
    val sea: SeaConditions?,
    val observedAt: LocalDateTime?,
)
