package victor.android.sargazocancnplayadelcarmentulum.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.local.seed.KnownBeaches

class MapRegionTest {

    @Test
    fun `todas las playas conocidas caen dentro de los limites del mapa`() {
        val outside = KnownBeaches.all.filterNot {
            GeoPoint(it.latitude!!, it.longitude!!) in MapRegion.bounds
        }
        assertTrue("Playas fuera de los límites: ${outside.map { it.name }}", outside.isEmpty())
    }

    @Test
    fun `la vista inicial esta dentro de los limites`() {
        assertTrue(MapRegion.initialCenter in MapRegion.bounds)
    }

    @Test
    fun `puntos lejanos quedan fuera de los limites`() {
        assertFalse(GeoPoint(19.43, -99.13) in MapRegion.bounds) // Ciudad de México
        assertFalse(GeoPoint(25.77, -80.19) in MapRegion.bounds) // Miami
    }

    @Test
    fun `los limites tienen suroeste al sur y oeste del noreste`() {
        val b = MapRegion.bounds
        assertTrue(b.southwest.latitude < b.northeast.latitude)
        assertTrue(b.southwest.longitude < b.northeast.longitude)
    }

    @Test
    fun `el zoom minimo es menor que el inicial y el maximo`() {
        assertTrue(MapRegion.MIN_ZOOM <= MapRegion.INITIAL_ZOOM)
        assertTrue(MapRegion.INITIAL_ZOOM < MapRegion.MAX_ZOOM)
    }
}
