package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import org.junit.Assert.assertEquals
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.beachStatus

class BeachSearchTest {

    private val beaches = listOf(
        beachStatus(1, "Tulum", SargassumLevel.EXCESSIVE),
        beachStatus(2, "Álamo", SargassumLevel.LOW),
        beachStatus(3, "Playa del Niño", SargassumLevel.LOW),
        beachStatus(4, "Chacmuchuch", SargassumLevel.MODERATE),
    )

    @Test
    fun `normaliza acentos y mayusculas`() {
        assertEquals("playa del nino", normalizeForSearch("  Playa del Niño "))
    }

    @Test
    fun `sin busqueda ni niveles devuelve todas ordenadas por nombre sin importar acentos`() {
        val result = beaches.filterBeaches("", emptySet())

        assertEquals(listOf("Álamo", "Chacmuchuch", "Playa del Niño", "Tulum"), result.map { it.beach.name })
    }

    @Test
    fun `la busqueda ignora acentos y mayusculas`() {
        assertEquals(listOf(2L), beaches.filterBeaches("alamo", emptySet()).map { it.beach.id })
        assertEquals(listOf(3L), beaches.filterBeaches("NIÑO", emptySet()).map { it.beach.id })
        assertEquals(listOf(3L), beaches.filterBeaches("nino", emptySet()).map { it.beach.id })
    }

    @Test
    fun `el filtro deja solo los niveles marcados`() {
        val result = beaches.filterBeaches("", setOf(SargassumLevel.LOW, SargassumLevel.EXCESSIVE))

        assertEquals(listOf(2L, 3L, 1L), result.map { it.beach.id })
    }

    @Test
    fun `busqueda y filtro se combinan`() {
        val result = beaches.filterBeaches("playa", setOf(SargassumLevel.LOW))

        assertEquals(listOf(3L), result.map { it.beach.id })
        assertEquals(emptyList<Long>(), beaches.filterBeaches("tulum", setOf(SargassumLevel.LOW)).map { it.beach.id })
    }
}
