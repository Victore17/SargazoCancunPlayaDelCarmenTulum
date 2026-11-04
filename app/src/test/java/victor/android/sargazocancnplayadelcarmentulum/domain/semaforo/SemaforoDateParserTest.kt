package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class SemaforoDateParserTest {

    private val expected = LocalDate.of(2026, 11, 3)

    @Test
    fun `fecha como aparece en la imagen`() {
        assertEquals(expected, SemaforoDateParser.parse(listOf("03/Noviembre /2026")))
    }

    @Test
    fun `fecha con errores de lectura`() {
        // Así la leyó un OCR: barra al inicio y la diagonal leída como "1".
        assertEquals(expected, SemaforoDateParser.parse(listOf("|03/Noviembre 12026 | 423")))
        assertEquals(expected, SemaforoDateParser.parse(listOf("3 / NOVIEMBRE / 2026")))
        assertEquals(expected, SemaforoDateParser.parse(listOf("03/Nov/2026")))
    }

    @Test
    fun `fecha partida en dos renglones`() {
        assertEquals(expected, SemaforoDateParser.parse(listOf("Excesivo", "03/Noviembre", "/2026")))
    }

    @Test
    fun `todos los meses en espanol`() {
        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
        )
        months.forEachIndexed { index, month ->
            assertEquals(LocalDate.of(2026, index + 1, 15), SemaforoDateParser.parse(listOf("15/$month/2026")))
        }
    }

    @Test
    fun `sin fecha valida devuelve null`() {
        assertNull(SemaforoDateParser.parse(listOf("Total de Playas 100")))
        assertNull(SemaforoDateParser.parse(listOf("31/Febrero/2026")))
        assertNull(SemaforoDateParser.parse(listOf("03/Brumario/2026")))
        assertNull(SemaforoDateParser.parse(emptyList()))
    }
}
