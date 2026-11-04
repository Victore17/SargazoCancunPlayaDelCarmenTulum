package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import kotlin.math.abs
import kotlin.math.max

/** Un renglón de texto con el círculo de color que le corresponde. */
data class LabeledDot(val line: TextLine, val dot: ColorDot)

/**
 * Empareja cada renglón de texto con su círculo de color.
 *
 * En el semáforo cada nombre tiene su círculo a la izquierda o a la derecha, a la misma altura.
 * A veces el círculo queda un poco arriba o abajo del texto. Se calculan todas las parejas
 * posibles con una distancia y se eligen de la más cercana a la más lejana, sin repetir
 * renglón ni círculo.
 */
object LabelDotMatcher {

    /** Distancia horizontal máxima entre el texto y el círculo, en alturas de renglón. */
    private const val MAX_GAP_IN_LINE_HEIGHTS = 2.5

    /** Peso de la diferencia vertical: un círculo desalineado cuenta como más lejano. */
    private const val VERTICAL_WEIGHT = 2.0

    fun match(lines: List<TextLine>, dots: List<ColorDot>): List<LabeledDot> {
        data class Candidate(val line: TextLine, val dot: ColorDot, val score: Double)

        val candidates = buildList {
            for (line in lines) {
                if (line.height <= 0) continue
                for (dot in dots) {
                    val score = score(line, dot) ?: continue
                    add(Candidate(line, dot, score))
                }
            }
        }.sortedBy { it.score }

        val usedLines = HashSet<TextLine>()
        val usedDots = HashSet<ColorDot>()
        val result = mutableListOf<LabeledDot>()
        for (candidate in candidates) {
            if (candidate.line in usedLines || candidate.dot in usedDots) continue
            usedLines += candidate.line
            usedDots += candidate.dot
            result += LabeledDot(candidate.line, candidate.dot)
        }
        return result
    }

    /** Distancia entre un renglón y un círculo, o null si no pueden ser pareja. */
    private fun score(line: TextLine, dot: ColorDot): Double? {
        val lineHeight = max(line.height, dot.diameter / 2).toDouble()
        val dy = abs(dot.centerY - line.centerY).toDouble()
        if (dy > lineHeight * 1.2 + dot.diameter / 4.0) return null

        val gap = when {
            dot.centerX < line.left -> (line.left - dot.right).toDouble()
            dot.centerX > line.right -> (dot.left - line.right).toDouble()
            // El círculo cae dentro de la caja: solo vale si está pegado a uno de sus extremos
            // (ML Kit a veces incluye el círculo como si fuera una letra).
            dot.centerX - line.left <= dot.diameter || line.right - dot.centerX <= dot.diameter -> 0.0
            else -> return null
        }
        val maxGap = lineHeight * MAX_GAP_IN_LINE_HEIGHTS
        if (gap > maxGap) return null
        return max(gap, 0.0) + dy * VERTICAL_WEIGHT
    }
}
