package victor.android.sargazocancnplayadelcarmentulum.data.remote.weather

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Las respuestas son las reales que devolvió Open-Meteo el 03/11/2026. */
class OpenMeteoDtosTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `lee la respuesta del clima (con campos que la app ignora)`() {
        val body = """
            {"latitude":20.632689,"longitude":-87.09384,"generationtime_ms":0.16,"utc_offset_seconds":-18000,
             "timezone":"America/Cancun","elevation":7.0,
             "current_units":{"time":"iso8601","temperature_2m":"°C"},
             "current":{"time":"2026-11-03T23:15","interval":900,"temperature_2m":26.3,
              "apparent_temperature":30.2,"relative_humidity_2m":78,"weather_code":0,
              "wind_speed_10m":6.7,"wind_direction_10m":106,"is_day":0}}
        """.trimIndent()

        val current = json.decodeFromString<ForecastResponse>(body).current

        assertEquals(26.3, current?.temperature ?: 0.0, 0.0)
        assertEquals(30.2, current?.apparentTemperature ?: 0.0, 0.0)
        assertEquals(78.0, current?.humidity ?: 0.0, 0.0)
        assertEquals(0, current?.weatherCode)
        assertEquals(6.7, current?.windSpeed ?: 0.0, 0.0)
        assertEquals(106.0, current?.windDirection ?: 0.0, 0.0)
        assertEquals("2026-11-03T23:15", current?.time)
    }

    @Test
    fun `lee la respuesta del mar`() {
        val body = """
            {"latitude":20.625,"longitude":-87.04166,
             "current":{"time":"2026-11-03T23:15","interval":900,"wave_height":0.28,
              "wave_direction":105,"wave_period":5.00,"sea_surface_temperature":29.4}}
        """.trimIndent()

        val current = json.decodeFromString<MarineResponse>(body).current

        assertEquals(0.28, current?.waveHeight ?: 0.0, 0.0)
        assertEquals(105.0, current?.waveDirection ?: 0.0, 0.0)
        assertEquals(5.0, current?.wavePeriod ?: 0.0, 0.0)
        assertEquals(29.4, current?.seaSurfaceTemperature ?: 0.0, 0.0)
    }

    @Test
    fun `un punto en tierra trae el oleaje en null`() {
        val body = """{"current":{"time":"2026-11-03T23:15","wave_height":null}}"""

        val current = json.decodeFromString<MarineResponse>(body).current

        assertNull(current?.waveHeight)
        assertNull(current?.seaSurfaceTemperature)
    }

    @Test
    fun `respuesta sin current no truena`() {
        assertNull(json.decodeFromString<ForecastResponse>("{}").current)
        assertNull(json.decodeFromString<MarineResponse>("""{"latitude":1.0}""").current)
    }
}
