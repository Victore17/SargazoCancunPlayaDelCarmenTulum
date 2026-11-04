package victor.android.sargazocancnplayadelcarmentulum.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachReading
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import java.time.LocalDate

class SemaforoDocumentMapperTest {

    private val date = LocalDate.of(2026, 11, 3)

    @Test
    fun `ida y vuelta conserva fecha y niveles`() {
        val report = SemaforoReport(
            date = date,
            readings = listOf(
                BeachReading("Punta Molas", SargassumLevel.EXCESSIVE),
                BeachReading("Isla Contoy", SargassumLevel.NO_SARGASSUM),
            ),
        )

        assertEquals(report, SemaforoDocumentMapper.fromFields(SemaforoDocumentMapper.toFields(report)))
    }

    @Test
    fun `el id del documento y el campo date son la fecha ISO`() {
        val fields = SemaforoDocumentMapper.toFields(SemaforoReport(date, emptyList()))

        assertEquals("2026-11-03", SemaforoDocumentMapper.documentId(date))
        assertEquals("2026-11-03", fields[SemaforoDocumentMapper.FIELD_DATE])
    }

    @Test
    fun `las playas sin dato no se publican`() {
        val report = SemaforoReport(
            date = date,
            readings = listOf(
                BeachReading("Akumal", SargassumLevel.NO_DATA),
                BeachReading("Tankah", SargassumLevel.LOW),
            ),
        )

        val beaches = SemaforoDocumentMapper.toFields(report)[SemaforoDocumentMapper.FIELD_BEACHES] as List<*>

        assertEquals(listOf(mapOf("name" to "Tankah", "level" to "LOW")), beaches)
    }

    @Test
    fun `documento mal formado devuelve null`() {
        assertNull(SemaforoDocumentMapper.fromFields(emptyMap()))
        assertNull(SemaforoDocumentMapper.fromFields(mapOf("date" to "ayer", "beaches" to emptyList<Any>())))
        assertNull(SemaforoDocumentMapper.fromFields(mapOf("date" to "2026-11-03")))
    }

    @Test
    fun `playas invalidas se ignoran sin perder las demas`() {
        val fields = mapOf(
            "date" to "2026-11-03",
            "beaches" to listOf(
                mapOf("name" to "Tankah", "level" to "LOW"),
                mapOf("name" to "Sin nivel"),
                mapOf("name" to "Nivel raro", "level" to "PURPLE"),
                mapOf("name" to "   ", "level" to "LOW"),
                "no es un mapa",
            ),
        )

        val report = SemaforoDocumentMapper.fromFields(fields)

        assertEquals(listOf(BeachReading("Tankah", SargassumLevel.LOW)), report?.readings)
    }
}
