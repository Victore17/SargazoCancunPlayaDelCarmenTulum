package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Círculo de color encontrado en la imagen. Coordenadas en píxeles. */
data class ColorDot(
    val level: SargassumLevel,
    val centerX: Int,
    val centerY: Int,
    val diameter: Int,
) {
    val left: Int get() = centerX - diameter / 2
    val right: Int get() = centerX + diameter / 2
}

/**
 * Busca los círculos de color del semáforo: agrupa píxeles vecinos del mismo nivel
 * (componentes conexas) y se queda con los grupos que parecen círculos con anillo blanco.
 *
 * Así se descartan la leyenda y la gráfica de pastel (demasiado grandes), los huecos de las
 * letras (demasiado pequeños) y el mar (no tiene anillo blanco).
 */
object ColorDotDetector {

    /** Área mínima y máxima de un círculo, como fracción del área de la imagen. */
    private const val MIN_AREA_FRACTION = 6e-5
    private const val MAX_AREA_FRACTION = 1.2e-3
    private const val MIN_ASPECT = 0.75
    private const val MAX_ASPECT = 1.33
    private const val MIN_FILL = 0.6
    private const val RING_SAMPLES = 16

    /**
     * Basta con medio anillo blanco: en los círculos sobre la costa, el lado del mar tiene el
     * anillo tenue (calibrado con Punta Caracol y Puerto Morelos Norte en la imagen real).
     */
    private const val MIN_LIGHT_RING_SAMPLES = 8

    fun detect(pixels: PixelSource): List<ColorDot> {
        val width = pixels.width
        val height = pixels.height
        val total = width * height
        if (total == 0) return emptyList()

        // Nivel de cada píxel (ordinal + 1; 0 = no es color del semáforo).
        val classes = ByteArray(total)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val level = LevelColorClassifier.classify(pixels.argb(x, y))
                if (level != null) classes[y * width + x] = (level.ordinal + 1).toByte()
            }
        }

        val minArea = maxOf(20.0, total * MIN_AREA_FRACTION)
        val maxArea = total * MAX_AREA_FRACTION
        val visited = BooleanArray(total)
        val queue = IntArray(total)
        val dots = mutableListOf<ColorDot>()

        for (start in 0 until total) {
            val cls = classes[start]
            if (cls.toInt() == 0 || visited[start]) continue

            // Recorrido en anchura de la componente conexa (vecinos en 4 direcciones).
            var head = 0
            var tail = 0
            queue[tail++] = start
            visited[start] = true
            var area = 0
            var minX = width
            var maxX = 0
            var minY = height
            var maxY = 0
            var sumX = 0L
            var sumY = 0L
            while (head < tail) {
                val p = queue[head++]
                val x = p % width
                val y = p / width
                area++
                sumX += x
                sumY += y
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                if (x > 0) tail = visit(p - 1, cls, classes, visited, queue, tail)
                if (x < width - 1) tail = visit(p + 1, cls, classes, visited, queue, tail)
                if (y > 0) tail = visit(p - width, cls, classes, visited, queue, tail)
                if (y < height - 1) tail = visit(p + width, cls, classes, visited, queue, tail)
            }

            if (area < minArea || area > maxArea) continue
            val boxWidth = maxX - minX + 1
            val boxHeight = maxY - minY + 1
            val aspect = boxWidth.toDouble() / boxHeight
            val fill = area.toDouble() / (boxWidth * boxHeight)
            if (aspect !in MIN_ASPECT..MAX_ASPECT || fill < MIN_FILL) continue
            if (!hasLightRing(pixels, minX, maxX, minY, maxY)) continue

            dots += ColorDot(
                level = SargassumLevel.entries[cls - 1],
                centerX = (sumX / area).toInt(),
                centerY = (sumY / area).toInt(),
                diameter = maxOf(boxWidth, boxHeight),
            )
        }
        return dots
    }

    private fun visit(
        index: Int,
        cls: Byte,
        classes: ByteArray,
        visited: BooleanArray,
        queue: IntArray,
        tail: Int,
    ): Int {
        if (visited[index] || classes[index] != cls) return tail
        visited[index] = true
        queue[tail] = index
        return tail + 1
    }

    /** Revisa puntos alrededor del círculo, un poco por fuera de su borde, buscando el anillo blanco. */
    private fun hasLightRing(pixels: PixelSource, minX: Int, maxX: Int, minY: Int, maxY: Int): Boolean {
        val centerX = (minX + maxX) / 2.0
        val centerY = (minY + maxY) / 2.0
        val radiusX = (maxX - minX) / 2.0 + 1.5
        val radiusY = (maxY - minY) / 2.0 + 1.5
        var lightSamples = 0
        for (i in 0 until RING_SAMPLES) {
            val angle = i * 2 * PI / RING_SAMPLES
            val isLight = (0..3).any { extra ->
                val x = (centerX + (radiusX + extra) * cos(angle)).roundToInt()
                val y = (centerY + (radiusY + extra) * sin(angle)).roundToInt()
                x in 0 until pixels.width && y in 0 until pixels.height &&
                    LevelColorClassifier.isLight(pixels.argb(x, y))
            }
            if (isLight) lightSamples++
        }
        return lightSamples >= MIN_LIGHT_RING_SAMPLES
    }
}
