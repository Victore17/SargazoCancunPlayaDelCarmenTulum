package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachReading
import java.time.LocalDate

/** Resultado de analizar una imagen del semáforo. */
sealed interface SemaforoAnalysis {

    /** La imagen no parece un semáforo de sargazo (faltan las palabras de la leyenda o los círculos). */
    data object NotASemaforo : SemaforoAnalysis

    /**
     * @property date fecha leída en la imagen, o null si no se pudo leer.
     * @property readings playas reconocidas con su nivel, en el orden en que aparecen.
     * @property newBeachNames nombres junto a un círculo que no están en el catálogo (playas nuevas).
     * @property unlabeledDotCount círculos a los que no se les encontró nombre.
     */
    data class Success(
        val date: LocalDate?,
        val readings: List<BeachReading>,
        val newBeachNames: List<String>,
        val unlabeledDotCount: Int,
    ) : SemaforoAnalysis
}

/**
 * Analiza la imagen del semáforo a partir del texto leído por ML Kit ([lines]) y de los píxeles.
 * No depende de Android, por eso se puede probar con la imagen real en pruebas unitarias.
 *
 * @param knownBeachNames nombres oficiales de las playas conocidas (catálogo).
 */
class SemaforoParser(knownBeachNames: Collection<String>) {

    private val nameMatcher = BeachNameMatcher(knownBeachNames)
    private val legendKeywords = LEGEND_KEYWORDS.map(BeachNameMatcher::normalize)

    fun parse(lines: List<TextLine>, pixels: PixelSource): SemaforoAnalysis {
        if (!looksLikeSemaforo(lines)) return SemaforoAnalysis.NotASemaforo

        val dots = ColorDotDetector.detect(pixels)
        if (dots.size < MIN_DOTS) return SemaforoAnalysis.NotASemaforo

        // Solo interesan renglones que puedan ser nombres: se quitan la leyenda y los números.
        val candidateLines = lines.filter { isPossibleBeachName(it.text) }
        val labeled = LabelDotMatcher.match(candidateLines, dots)

        val readings = LinkedHashMap<String, BeachReading>()
        val newNames = LinkedHashSet<String>()
        for ((line, dot) in labeled.sortedWith(compareBy({ it.dot.centerY }, { it.dot.centerX }))) {
            val known = nameMatcher.match(line.text)
            val name = known ?: cleanName(line.text)
            if (name.isBlank() || name in readings) continue
            if (known == null) newNames += name
            readings[name] = BeachReading(beachName = name, level = dot.level)
        }

        return SemaforoAnalysis.Success(
            date = SemaforoDateParser.parse(lines.map { it.text }),
            readings = readings.values.toList(),
            newBeachNames = newNames.toList(),
            unlabeledDotCount = dots.size - labeled.size,
        )
    }

    /** La imagen es un semáforo si se leen al menos [MIN_LEGEND_KEYWORDS] palabras de la leyenda. */
    private fun looksLikeSemaforo(lines: List<TextLine>): Boolean {
        val allText = " " + lines.joinToString(" ") { BeachNameMatcher.normalize(it.text) } + " "
        val found = legendKeywords.count { keyword -> " $keyword " in allText }
        return found >= MIN_LEGEND_KEYWORDS
    }

    private fun isPossibleBeachName(text: String): Boolean {
        val normalized = BeachNameMatcher.normalize(text)
        if (normalized.count { it.isLetter() } < 3) return false
        return legendKeywords.none { it == normalized }
    }

    /** Limpia un nombre nuevo: quita símbolos sueltos al inicio y al final (puntos, el círculo leído como "o"…). */
    private fun cleanName(text: String): String =
        text.trim()
            .replace(Regex("^[^\\p{L}\\d(]+"), "")
            .replace(Regex("[^\\p{L}\\d)]+$"), "")
            .replace(Regex("\\s+"), " ")

    companion object {
        /** Palabras de la leyenda del semáforo oficial. */
        val LEGEND_KEYWORDS = listOf(
            "Excesivo", "Abundante", "Moderado", "Bajo", "Sin Sargazo", "Total de Playas",
        )
        private const val MIN_LEGEND_KEYWORDS = 3
        private const val MIN_DOTS = 5
    }
}
