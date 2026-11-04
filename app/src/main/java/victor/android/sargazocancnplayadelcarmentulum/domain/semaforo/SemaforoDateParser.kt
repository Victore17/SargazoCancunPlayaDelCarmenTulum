package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import java.time.DateTimeException
import java.time.LocalDate

/**
 * Lee la fecha del semáforo, que viene como "03/Noviembre /2026".
 * Tolera errores comunes de lectura: espacios de más, acentos y la diagonal leída como "1" o "|".
 */
object SemaforoDateParser {

    private val months = listOf(
        "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic",
    )

    private val pattern = Regex("""(\d{1,2})\s*[/|\\-]?\s*([a-z]{3,})\s*[/|\\-]?\s*[|l]?(\d{4,5})""")

    fun parse(lines: List<String>): LocalDate? {
        // Se une todo por si ML Kit partió la fecha en varios renglones.
        val candidates = lines + lines.joinToString(" ")
        for (line in candidates) {
            val text = BeachNameMatcher.normalizeKeepingSlashes(line)
            for (match in pattern.findAll(text)) {
                val (dayText, monthText, yearText) = match.destructured
                val month = monthOf(monthText) ?: continue
                // "12026" sale cuando la diagonal se lee como "1": se toman los últimos 4 dígitos.
                val year = yearText.takeLast(4).toInt()
                if (year !in 2020..2100) continue
                try {
                    return LocalDate.of(year, month, dayText.toInt())
                } catch (_: DateTimeException) {
                    continue
                }
            }
        }
        return null
    }

    private fun monthOf(text: String): Int? {
        val index = months.indexOfFirst { text.startsWith(it) }
        return if (index >= 0) index + 1 else null
    }
}
