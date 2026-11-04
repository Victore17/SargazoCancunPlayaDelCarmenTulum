package victor.android.sargazocancnplayadelcarmentulum.data.remote

import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachReading
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Conversión entre [SemaforoReport] y el documento de Firestore.
 *
 * Estructura del documento `semaforos/{yyyy-MM-dd}`:
 * ```
 * {
 *   date: "2026-11-03",
 *   publishedAt: <hora del servidor>,
 *   beaches: [ { name: "Punta Molas", level: "EXCESSIVE" }, … ]
 * }
 * ```
 * La fecha se guarda como texto ISO para poder filtrar por rango comparando textos.
 */
object SemaforoDocumentMapper {

    const val COLLECTION = "semaforos"
    const val FIELD_DATE = "date"
    const val FIELD_PUBLISHED_AT = "publishedAt"
    const val FIELD_BEACHES = "beaches"
    const val FIELD_NAME = "name"
    const val FIELD_LEVEL = "level"

    /** Id del documento: la fecha en formato ISO. */
    fun documentId(date: LocalDate): String = date.toString()

    /** Campos del documento (sin publishedAt, que lo pone el servidor). */
    fun toFields(report: SemaforoReport): Map<String, Any> = mapOf(
        FIELD_DATE to report.date.toString(),
        FIELD_BEACHES to report.readings
            .filter { it.level != SargassumLevel.NO_DATA }
            .map { mapOf(FIELD_NAME to it.beachName, FIELD_LEVEL to it.level.name) },
    )

    /** Lee un documento. Devuelve null si está mal formado; las playas inválidas se ignoran. */
    fun fromFields(fields: Map<String, Any?>): SemaforoReport? {
        val date = (fields[FIELD_DATE] as? String)?.let(::parseDate) ?: return null
        val beaches = fields[FIELD_BEACHES] as? List<*> ?: return null
        val readings = beaches.mapNotNull { item ->
            val beach = item as? Map<*, *> ?: return@mapNotNull null
            val name = (beach[FIELD_NAME] as? String)?.trim()?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            val level = SargassumLevel.entries.firstOrNull { it.name == beach[FIELD_LEVEL] }
                ?: return@mapNotNull null
            BeachReading(beachName = name, level = level)
        }
        return SemaforoReport(date = date, readings = readings)
    }

    private fun parseDate(text: String): LocalDate? = try {
        LocalDate.parse(text)
    } catch (_: DateTimeParseException) {
        null
    }
}
