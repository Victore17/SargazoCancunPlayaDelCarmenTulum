package victor.android.sargazocancnplayadelcarmentulum.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.ForecastResponse
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.MarineResponse
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SeaState
import victor.android.sargazocancnplayadelcarmentulum.domain.model.WeatherCondition
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeForecastApi
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeMarineApi
import java.io.IOException
import java.net.UnknownHostException
import java.time.LocalDateTime

class WeatherRepositoryImplTest {

    private val forecastApi = FakeForecastApi()
    private val marineApi = FakeMarineApi()
    private val repository = WeatherRepositoryImpl(forecastApi, marineApi)
    private val location = GeoPoint(20.6277, -87.0677)

    @Test
    fun `junta el clima y el mar`() = runTest {
        val conditions = repository.getConditions(location)

        assertEquals(26.3, conditions.air?.temperatureCelsius ?: 0.0, 0.0)
        assertEquals(WeatherCondition.CLEAR, conditions.air?.weather)
        assertEquals(SeaState.CALM, conditions.sea?.seaState)
        assertEquals(LocalDateTime.of(2026, 11, 3, 23, 15), conditions.observedAt)
    }

    @Test
    fun `consulta las dos APIs con las coordenadas de la playa`() = runTest {
        repository.getConditions(location)

        assertEquals(listOf(20.6277 to -87.0677), forecastApi.requests)
        assertEquals(listOf(20.6277 to -87.0677), marineApi.requests)
    }

    @Test
    fun `si falla el mar muestra solo el clima`() = runTest {
        marineApi.error = IOException("sin respuesta")

        val conditions = repository.getConditions(location)

        assertNotNull(conditions.air)
        assertNull(conditions.sea)
    }

    @Test
    fun `si falla el clima muestra solo el mar`() = runTest {
        forecastApi.error = IOException("sin respuesta")

        val conditions = repository.getConditions(location)

        assertNull(conditions.air)
        assertNotNull(conditions.sea)
        assertEquals(LocalDateTime.of(2026, 11, 3, 23, 15), conditions.observedAt)
    }

    @Test
    fun `si fallan las dos lanza el error de red`() = runTest {
        forecastApi.error = UnknownHostException("sin internet")
        marineApi.error = UnknownHostException("sin internet")

        try {
            repository.getConditions(location)
            fail("Debía lanzar el error")
        } catch (e: UnknownHostException) {
            assertEquals("sin internet", e.message)
        }
    }

    @Test
    fun `punto en tierra sin oleaje igual muestra el clima`() = runTest {
        marineApi.response = MarineResponse(current = null)

        val conditions = repository.getConditions(location)

        assertNotNull(conditions.air)
        assertNull(conditions.sea)
    }

    @Test
    fun `si no responde ningun dato lanza NoConditionsException`() = runTest {
        forecastApi.response = ForecastResponse(current = null)
        marineApi.response = MarineResponse(current = null)

        try {
            repository.getConditions(location)
            fail("Debía lanzar NoConditionsException")
        } catch (e: NoConditionsException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }
}
