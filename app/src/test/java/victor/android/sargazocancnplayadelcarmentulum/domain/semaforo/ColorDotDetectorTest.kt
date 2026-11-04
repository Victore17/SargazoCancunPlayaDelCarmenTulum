package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.SemaforoFixture
import kotlin.math.abs
import kotlin.math.hypot

class ColorDotDetectorTest {

    private val white = 0xFFFFFFFF.toInt()
    private val green = 0xFF24AC5A.toInt()
    private val seaBlue = 0xFF2AC0ED.toInt() // azul saturado del mar, sin anillo blanco

    private companion object {
        const val SIZE = 1000
    }

    /**
     * Imagen sintética de [size]×[size] con fondo [background] y círculos de color.
     * Los tamaños válidos de círculo son relativos al área de la imagen, por eso se usa un
     * tamaño parecido al de un semáforo real (≈ 1000 px).
     */
    private fun syntheticImage(
        size: Int,
        background: Int,
        circles: List<Triple<Int, Int, Int>>,
        circleColor: Int,
        ringColor: Int? = white,
    ): PixelSource {
        val pixels = IntArray(size * size) { background }
        for ((cx, cy, radius) in circles) {
            for (y in 0 until size) for (x in 0 until size) {
                val d = hypot((x - cx).toDouble(), (y - cy).toDouble())
                when {
                    d <= radius -> pixels[y * size + x] = circleColor
                    ringColor != null && d <= radius + 5 -> pixels[y * size + x] = ringColor
                }
            }
        }
        return ArgbArrayPixelSource(size, size, pixels)
    }

    @Test
    fun `imagen real - encuentra los circulos de cada color`() {
        val dots = ColorDotDetector.detect(SemaforoFixture.loadImage())
        val counts = dots.groupingBy { it.level }.eachCount()

        assertEquals(SemaforoFixture.expectedDotCounts, counts)
        assertEquals(101, dots.size) // 100 playas + el bulbo del termómetro
    }

    @Test
    fun `imagen real - Punta Molas es el circulo rojo de Cozumel`() {
        val dots = ColorDotDetector.detect(SemaforoFixture.loadImage())
        val red = dots.filter { it.level == SargassumLevel.EXCESSIVE }

        assertTrue(red.any { abs(it.centerX - 782) <= 3 && abs(it.centerY - 882) <= 3 })
    }

    @Test
    fun `circulo con anillo blanco se detecta con su centro`() {
        val image = syntheticImage(SIZE, white, listOf(Triple(500, 400, 10)), green)

        val dots = ColorDotDetector.detect(image)

        assertEquals(1, dots.size)
        assertEquals(SargassumLevel.LOW, dots.single().level)
        assertTrue(abs(dots.single().centerX - 500) <= 1)
        assertTrue(abs(dots.single().centerY - 400) <= 1)
    }

    @Test
    fun `mancha azul sin anillo blanco (mar) se descarta`() {
        // Mar menos saturado alrededor, como en la imagen real: no es color del semáforo ni blanco.
        val paleSea = 0xFF6EC9E6.toInt()
        val image = syntheticImage(SIZE, paleSea, listOf(Triple(500, 500, 10)), seaBlue, ringColor = null)

        assertTrue(ColorDotDetector.detect(image).isEmpty())
    }

    @Test
    fun `manchas muy grandes (leyenda o pastel) o muy pequenas (letras) se descartan`() {
        val big = syntheticImage(SIZE, white, listOf(Triple(500, 500, 60)), green)
        val tiny = syntheticImage(SIZE, white, listOf(Triple(500, 500, 2)), green)

        assertTrue(ColorDotDetector.detect(big).isEmpty())
        assertTrue(ColorDotDetector.detect(tiny).isEmpty())
    }

    @Test
    fun `imagen vacia no tiene circulos`() {
        assertTrue(ColorDotDetector.detect(ArgbArrayPixelSource(0, 0, IntArray(0))).isEmpty())
    }
}
