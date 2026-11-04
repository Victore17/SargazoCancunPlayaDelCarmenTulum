package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.local.seed.KnownBeaches

class BeachNameMatcherTest {

    private val matcher = BeachNameMatcher(KnownBeaches.names)

    @Test
    fun `nombre exacto o sin acentos`() {
        assertEquals("Cancún Playa Marlín", matcher.match("Cancún Playa Marlín"))
        assertEquals("Cancún Playa Marlín", matcher.match("cancun playa marlin"))
        assertEquals("Chiquilá", matcher.match("CHIQUILA"))
    }

    @Test
    fun `errores tipicos de lectura (leidos por OCR en la imagen real)`() {
        assertEquals("Cancún Playa Marlín", matcher.match("Cancin Playa Marlin"))
        assertEquals("Cancún Playa Delfines", matcher.match("Canciin Playa Delfines"))
        assertEquals("Playa Chumul", matcher.match("laya Chumul"))
        assertEquals("Isla de la Pasión", matcher.match("Isla de la Pasién"))
        assertEquals("Isla Mujeres Playa Mia", matcher.match("sla Mujeres Playa Mia"))
    }

    @Test
    fun `nombres parecidos no se confunden`() {
        assertEquals("Playa Mia", matcher.match("Playa Mia"))
        assertEquals("Isla Mujeres Playa Mia", matcher.match("Isla Mujeres Playa Mia"))
        assertEquals("Punta Sur", matcher.match("Punta Sur"))
        assertEquals("Isla Mujeres Punta Sur", matcher.match("Isla Mujeres Punta Sur"))
        assertEquals("Punta Caracol", matcher.match("Punta Caracol"))
        assertEquals("Cancún Playa Caracol", matcher.match("Cancún Playa Caracol"))
    }

    @Test
    fun `texto extra pegado al final`() {
        // En la imagen real, "Arco Maya" queda en el mismo renglón que el pie de la imagen.
        assertEquals("Arco Maya", matcher.match("Arco Maya Disefio Contenido Técnico:"))
        assertEquals("Playacar Terminal del Ferry", matcher.match("Playacar Terminal del Ferry ."))
    }

    @Test
    fun `nombre nuevo parecido a uno conocido no se confunde`() {
        assertNull(matcher.match("Playa Inventada")) // parecida a "Playa Encantada"
    }

    @Test
    fun `texto que no es playa no coincide`() {
        assertNull(matcher.match("Temperatura"))
        assertNull(matcher.match("Total de Playas"))
        assertNull(matcher.match("km/hr"))
        assertNull(matcher.match("ab"))
        assertNull(matcher.match(""))
    }

    @Test
    fun `normalize quita acentos y signos`() {
        assertEquals("playa colosio calle 72", BeachNameMatcher.normalize("  Playa Colosio (Calle 72) "))
        assertEquals("xcacel xcacelito", BeachNameMatcher.normalize("Xcacel-Xcacelito"))
    }

    @Test
    fun `similarity entre 0 y 1`() {
        assertEquals(1.0, BeachNameMatcher.similarity("abc", "abc"), 0.0)
        assertEquals(0.0, BeachNameMatcher.similarity("abc", "xyz"), 0.0)
        assertEquals(0.75, BeachNameMatcher.similarity("abcd", "abce"), 0.001)
    }
}
