package victor.android.sargazocancnplayadelcarmentulum.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BeachConditionsTest {

    @Test
    fun `codigos WMO se traducen al cielo`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWmoCode(0))
        assertEquals(WeatherCondition.MAINLY_CLEAR, WeatherCondition.fromWmoCode(1))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWmoCode(2))
        assertEquals(WeatherCondition.OVERCAST, WeatherCondition.fromWmoCode(3))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(45))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(48))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCondition.fromWmoCode(53))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(63))
        assertEquals(WeatherCondition.RAIN_SHOWERS, WeatherCondition.fromWmoCode(81))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(99))
    }

    @Test
    fun `codigo desconocido o faltante es desconocido`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(null))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(1234))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(73)) // nieve
    }

    @Test
    fun `direccion del viento en los ocho puntos`() {
        assertEquals(CompassDirection.N, CompassDirection.fromDegrees(0.0))
        assertEquals(CompassDirection.NE, CompassDirection.fromDegrees(45.0))
        assertEquals(CompassDirection.E, CompassDirection.fromDegrees(90.0))
        assertEquals(CompassDirection.SE, CompassDirection.fromDegrees(135.0))
        assertEquals(CompassDirection.S, CompassDirection.fromDegrees(180.0))
        assertEquals(CompassDirection.SW, CompassDirection.fromDegrees(225.0))
        assertEquals(CompassDirection.W, CompassDirection.fromDegrees(270.0))
        assertEquals(CompassDirection.NW, CompassDirection.fromDegrees(315.0))
    }

    @Test
    fun `el viento de 106 grados viene del este (dato real de Playa del Carmen)`() {
        assertEquals(CompassDirection.E, CompassDirection.fromDegrees(106.0))
    }

    @Test
    fun `limites entre puntos cardinales`() {
        assertEquals(CompassDirection.N, CompassDirection.fromDegrees(22.4))
        assertEquals(CompassDirection.NE, CompassDirection.fromDegrees(22.5))
        assertEquals(CompassDirection.NW, CompassDirection.fromDegrees(337.4))
        assertEquals(CompassDirection.N, CompassDirection.fromDegrees(337.5))
    }

    @Test
    fun `grados fuera de 0 a 360 se normalizan`() {
        assertEquals(CompassDirection.N, CompassDirection.fromDegrees(360.0))
        assertEquals(CompassDirection.E, CompassDirection.fromDegrees(450.0))
        assertEquals(CompassDirection.W, CompassDirection.fromDegrees(-90.0))
    }

    @Test
    fun `estado del mar segun la altura de las olas`() {
        assertEquals(SeaState.CALM, SeaState.fromWaveHeight(0.0))
        assertEquals(SeaState.CALM, SeaState.fromWaveHeight(0.49))
        assertEquals(SeaState.SLIGHT, SeaState.fromWaveHeight(0.5))
        assertEquals(SeaState.SLIGHT, SeaState.fromWaveHeight(1.24))
        assertEquals(SeaState.MODERATE, SeaState.fromWaveHeight(1.25))
        assertEquals(SeaState.MODERATE, SeaState.fromWaveHeight(2.49))
        assertEquals(SeaState.ROUGH, SeaState.fromWaveHeight(2.5))
        assertEquals(SeaState.ROUGH, SeaState.fromWaveHeight(6.0))
    }

    @Test
    fun `el estado del mar de las condiciones sale de la altura de olas`() {
        assertEquals(SeaState.CALM, SeaConditions(0.28, 105.0, 5.0, 29.4).seaState)
        assertNull(SeaConditions(null, null, null, 28.0).seaState)
    }
}
