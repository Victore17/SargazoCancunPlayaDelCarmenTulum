package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.local.seed.KnownBeaches
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.SemaforoFixture
import java.time.LocalDate

class SemaforoParserTest {

    private val parser = SemaforoParser(KnownBeaches.names)
    private val image = SemaforoFixture.loadImage()

    @Test
    fun `imagen real - lee fecha y nivel de cada playa`() {
        val analysis = parser.parse(SemaforoFixture.allLines, image)

        check(analysis is SemaforoAnalysis.Success)
        assertEquals(LocalDate.of(2026, 11, 3), analysis.date)
        val levelByName = analysis.readings.associate { it.beachName to it.level }
        SemaforoFixture.beachLines.forEach { (line, expected) ->
            assertEquals("Nivel de ${line.text}", expected, levelByName[line.text])
        }
        assertEquals(SemaforoFixture.beachLines.size, analysis.readings.size)
        assertTrue(analysis.newBeachNames.isEmpty())
    }

    @Test
    fun `imagen real - nombres mal leidos se corrigen al nombre oficial`() {
        val lines = SemaforoFixture.otherLines + listOf(
            TextLine("Cancin Playa Coral", 559, 514, 695, 530),
            TextLine("laya Chumul", 766, 985, 847, 1000),
        )

        val analysis = parser.parse(lines, image)

        check(analysis is SemaforoAnalysis.Success)
        assertEquals(
            mapOf("Cancún Playa Coral" to SargassumLevel.ABUNDANT, "Playa Chumul" to SargassumLevel.MODERATE),
            analysis.readings.associate { it.beachName to it.level },
        )
    }

    @Test
    fun `imagen real - nombre desconocido junto a un circulo es playa nueva`() {
        val lines = SemaforoFixture.otherLines + TextLine("Playa Inventada", 797, 870, 894, 896)

        val analysis = parser.parse(lines, image)

        check(analysis is SemaforoAnalysis.Success)
        assertEquals(listOf("Playa Inventada"), analysis.newBeachNames)
        assertEquals(SargassumLevel.EXCESSIVE, analysis.readings.single().level)
    }

    @Test
    fun `imagen real - circulos sin nombre se cuentan`() {
        val analysis = parser.parse(SemaforoFixture.allLines, image)

        check(analysis is SemaforoAnalysis.Success)
        // 101 círculos detectados menos los 20 nombres de prueba.
        assertEquals(101 - SemaforoFixture.beachLines.size, analysis.unlabeledDotCount)
    }

    @Test
    fun `sin palabras de la leyenda no es un semaforo`() {
        val lines = SemaforoFixture.beachLines.map { it.first }

        assertEquals(SemaforoAnalysis.NotASemaforo, parser.parse(lines, image))
    }

    @Test
    fun `imagen sin circulos no es un semaforo`() {
        val blank = ArgbArrayPixelSource(100, 100, IntArray(100 * 100) { 0xFFFFFFFF.toInt() })

        assertEquals(SemaforoAnalysis.NotASemaforo, parser.parse(SemaforoFixture.allLines, blank))
    }
}
