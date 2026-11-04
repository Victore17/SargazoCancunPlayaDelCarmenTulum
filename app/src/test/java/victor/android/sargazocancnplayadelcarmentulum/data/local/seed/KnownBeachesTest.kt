package victor.android.sargazocancnplayadelcarmentulum.data.local.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KnownBeachesTest {

    private val beaches = KnownBeaches.all

    @Test
    fun list_hasTheHundredBeachesOfTheOfficialSemaforo() {
        assertEquals(100, beaches.size)
    }

    @Test
    fun names_areUniqueIgnoringCase() {
        val names = beaches.map { it.name.trim().lowercase() }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun everyBeach_hasCoordinatesInsideTheHolboxTulumCozumelArea() {
        // Etapa 3: el semáforo oficial incluye Holbox, Chiquilá, Cabo Catoche e Isla Contoy (hasta 21.6° N).
        beaches.forEach { beach ->
            val lat = requireNotNull(beach.latitude) { "${beach.name} sin latitud" }
            val lng = requireNotNull(beach.longitude) { "${beach.name} sin longitud" }
            assertTrue("${beach.name}: latitud $lat fuera de zona", lat in 20.0..21.7)
            assertTrue("${beach.name}: longitud $lng fuera de zona", lng in -87.6..-86.6)
        }
    }

    @Test
    fun seedBeaches_startWithDefaultFlags() {
        beaches.forEach { beach ->
            assertEquals(0L, beach.id)
            assertTrue(!beach.isFavorite)
            assertTrue(beach.notificationsEnabled)
        }
    }
}
