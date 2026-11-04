package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.SemaforoFixture

class LabelDotMatcherTest {

    @Test
    fun `imagen real - cada nombre recibe el color que se ve junto a el`() {
        val dots = ColorDotDetector.detect(SemaforoFixture.loadImage())

        val matches = LabelDotMatcher.match(SemaforoFixture.beachLines.map { it.first }, dots)
        val levelByName = matches.associate { it.line.text to it.dot.level }

        SemaforoFixture.beachLines.forEach { (line, expected) ->
            assertEquals("Nivel de ${line.text}", expected, levelByName[line.text])
        }
    }

    @Test
    fun `imagen real - el termometro no se asigna a ningun nombre`() {
        val dots = ColorDotDetector.detect(SemaforoFixture.loadImage())

        val matches = LabelDotMatcher.match(SemaforoFixture.allLines, dots)

        assertTrue(matches.none { it.dot.centerX in 880..900 && it.dot.centerY in 110..140 })
    }

    @Test
    fun `circulo a la izquierda o a la derecha del texto`() {
        val left = ColorDot(SargassumLevel.LOW, centerX = 90, centerY = 107, diameter = 16)
        val right = ColorDot(SargassumLevel.EXCESSIVE, centerX = 315, centerY = 207, diameter = 16)
        val lines = listOf(
            TextLine("Playa A", 100, 100, 200, 115),
            TextLine("Playa B", 200, 200, 300, 215),
        )

        val matches = LabelDotMatcher.match(lines, listOf(left, right)).associate { it.line.text to it.dot }

        assertEquals(left, matches["Playa A"])
        assertEquals(right, matches["Playa B"])
    }

    @Test
    fun `cada circulo se usa una sola vez, gana el nombre mas cercano`() {
        val dot = ColorDot(SargassumLevel.MODERATE, centerX = 90, centerY = 107, diameter = 16)
        val near = TextLine("Cerca", 100, 100, 200, 115)
        val far = TextLine("Lejos", 120, 100, 220, 115)

        val matches = LabelDotMatcher.match(listOf(far, near), listOf(dot))

        assertEquals(1, matches.size)
        assertEquals("Cerca", matches.single().line.text)
    }

    @Test
    fun `circulo lejano o a otra altura no se empareja`() {
        val line = TextLine("Playa", 100, 100, 200, 115)
        val tooFar = ColorDot(SargassumLevel.LOW, centerX = 400, centerY = 107, diameter = 16)
        val otherRow = ColorDot(SargassumLevel.LOW, centerX = 90, centerY = 200, diameter = 16)

        assertTrue(LabelDotMatcher.match(listOf(line), listOf(tooFar, otherRow)).isEmpty())
    }

    @Test
    fun `circulo dentro de la caja pegado al borde si cuenta`() {
        val line = TextLine("o Punta Molas", 770, 870, 894, 896)
        val dot = ColorDot(SargassumLevel.EXCESSIVE, centerX = 782, centerY = 882, diameter = 24)

        val match = LabelDotMatcher.match(listOf(line), listOf(dot)).singleOrNull()

        assertEquals(dot, match?.dot)
    }

    @Test
    fun `circulo en medio del texto no cuenta`() {
        val line = TextLine("Un texto muy largo", 100, 100, 400, 115)
        val dot = ColorDot(SargassumLevel.LOW, centerX = 250, centerY = 107, diameter = 16)

        assertNull(LabelDotMatcher.match(listOf(line), listOf(dot)).singleOrNull())
    }
}
