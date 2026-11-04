package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel

/** Colores medidos en la imagen oficial del 03/11/2026. */
class LevelColorClassifierTest {

    private fun rgb(r: Int, g: Int, b: Int): Int = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    @Test
    fun `circulos del semaforo se clasifican en su nivel`() {
        assertEquals(SargassumLevel.EXCESSIVE, LevelColorClassifier.classify(rgb(231, 35, 24))) // Punta Molas
        assertEquals(SargassumLevel.ABUNDANT, LevelColorClassifier.classify(rgb(239, 185, 23))) // Playa Xhanan
        assertEquals(SargassumLevel.ABUNDANT, LevelColorClassifier.classify(rgb(244, 175, 29))) // Cancún Playa Coral
        assertEquals(SargassumLevel.MODERATE, LevelColorClassifier.classify(rgb(232, 231, 29))) // Puerto Morelos Norte
        assertEquals(SargassumLevel.LOW, LevelColorClassifier.classify(rgb(36, 172, 90))) // Cabo Catoche
        assertEquals(SargassumLevel.NO_SARGASSUM, LevelColorClassifier.classify(rgb(19, 169, 219))) // Isla Contoy
        assertEquals(SargassumLevel.NO_SARGASSUM, LevelColorClassifier.classify(rgb(41, 171, 215))) // Chiquilá
    }

    @Test
    fun `colores de la leyenda se clasifican en su nivel`() {
        assertEquals(SargassumLevel.EXCESSIVE, LevelColorClassifier.classify(rgb(255, 3, 1)))
        assertEquals(SargassumLevel.ABUNDANT, LevelColorClassifier.classify(rgb(253, 192, 0)))
        assertEquals(SargassumLevel.MODERATE, LevelColorClassifier.classify(rgb(255, 254, 1)))
        assertEquals(SargassumLevel.LOW, LevelColorClassifier.classify(rgb(0, 175, 80)))
        assertEquals(SargassumLevel.NO_SARGASSUM, LevelColorClassifier.classify(rgb(2, 176, 241)))
    }

    @Test
    fun `fondo blanco, gris y mar claro no son niveles`() {
        assertNull(LevelColorClassifier.classify(rgb(255, 255, 255)))
        assertNull(LevelColorClassifier.classify(rgb(128, 128, 128)))
        assertNull(LevelColorClassifier.classify(rgb(0, 0, 0)))
        assertNull(LevelColorClassifier.classify(rgb(184, 222, 228))) // mar claro
        assertNull(LevelColorClassifier.classify(rgb(110, 201, 230))) // mar poco saturado
    }

    @Test
    fun `nunca devuelve sin dato`() {
        val samples = listOf(rgb(231, 35, 24), rgb(36, 172, 90), rgb(19, 169, 219), rgb(200, 0, 200))
        samples.forEach { assertTrue(LevelColorClassifier.classify(it) != SargassumLevel.NO_DATA) }
    }

    @Test
    fun `isLight detecta el anillo blanco`() {
        assertTrue(LevelColorClassifier.isLight(rgb(255, 255, 255)))
        assertTrue(LevelColorClassifier.isLight(rgb(185, 239, 245))) // anillo de Isla Contoy
        assertFalse(LevelColorClassifier.isLight(rgb(42, 192, 237))) // mar saturado
        assertFalse(LevelColorClassifier.isLight(rgb(36, 172, 90)))
    }

    @Test
    fun `toHsv calcula tono, saturacion y brillo`() {
        val red = LevelColorClassifier.toHsv(rgb(255, 0, 0))
        assertEquals(0f, red.hue, 0.01f)
        assertEquals(1f, red.saturation, 0.01f)
        assertEquals(1f, red.value, 0.01f)

        val blue = LevelColorClassifier.toHsv(rgb(0, 0, 255))
        assertEquals(240f, blue.hue, 0.01f)

        val gray = LevelColorClassifier.toHsv(rgb(100, 100, 100))
        assertEquals(0f, gray.saturation, 0.01f)
    }
}
