package victor.android.sargazocancnplayadelcarmentulum.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.CurrentMarineDto
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.CurrentWeatherDto
import victor.android.sargazocancnplayadelcarmentulum.domain.model.WeatherCondition
import victor.android.sargazocancnplayadelcarmentulum.testutil.WeatherSamples
import java.time.LocalDateTime

class WeatherMappersTest {

    @Test
    fun `el clima se convierte con todos sus datos`() {
        val air = WeatherSamples.airDto.toDomain()

        assertEquals(26.3, air?.temperatureCelsius ?: 0.0, 0.0)
        assertEquals(30.2, air?.apparentTemperatureCelsius ?: 0.0, 0.0)
        assertEquals(78, air?.humidityPercent)
        assertEquals(WeatherCondition.CLEAR, air?.weather)
        assertEquals(6.7, air?.windSpeedKmh ?: 0.0, 0.0)
        assertEquals(106.0, air?.windDirectionDegrees ?: 0.0, 0.0)
    }

    @Test
    fun `sin temperatura no hay clima`() {
        assertNull(CurrentWeatherDto(weatherCode = 0, windSpeed = 5.0).toDomain())
    }

    @Test
    fun `la humedad decimal se redondea`() {
        assertEquals(79, WeatherSamples.airDto.copy(humidity = 78.6).toDomain()?.humidityPercent)
    }

    @Test
    fun `el mar se convierte con todos sus datos`() {
        val sea = WeatherSamples.seaDto.toDomain()

        assertEquals(0.28, sea?.waveHeightMeters ?: 0.0, 0.0)
        assertEquals(105.0, sea?.waveDirectionDegrees ?: 0.0, 0.0)
        assertEquals(5.0, sea?.wavePeriodSeconds ?: 0.0, 0.0)
        assertEquals(29.4, sea?.seaTemperatureCelsius ?: 0.0, 0.0)
    }

    @Test
    fun `mar sin ningun dato (punto en tierra) es null`() {
        assertNull(CurrentMarineDto(time = "2026-11-03T23:15").toDomain())
    }

    @Test
    fun `mar con solo temperatura si cuenta`() {
        val sea = CurrentMarineDto(seaSurfaceTemperature = 28.2).toDomain()

        assertEquals(28.2, sea?.seaTemperatureCelsius ?: 0.0, 0.0)
        assertNull(sea?.waveHeightMeters)
    }

    @Test
    fun `hora de la medicion sin segundos ni zona`() {
        assertEquals(LocalDateTime.of(2026, 11, 3, 23, 15), parseObservationTime("2026-11-03T23:15"))
    }

    @Test
    fun `hora invalida o faltante es null`() {
        assertNull(parseObservationTime(null))
        assertNull(parseObservationTime("ayer por la noche"))
    }
}
