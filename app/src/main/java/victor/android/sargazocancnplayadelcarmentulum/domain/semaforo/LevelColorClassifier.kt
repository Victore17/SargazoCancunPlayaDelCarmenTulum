package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import kotlin.math.max
import kotlin.math.min

/** Color en HSV: tono en grados (0 a 360), saturación y brillo de 0 a 1. */
data class Hsv(val hue: Float, val saturation: Float, val value: Float)

/**
 * Convierte el color de un píxel del semáforo en un nivel de sargazo.
 *
 * Los rangos se calibraron con la imagen oficial del 03/11/2026:
 * - Rojo (excesivo): tono ≈ 0–5°.
 * - Naranja (abundante): tono ≈ 40–46°.
 * - Amarillo (moderado): tono ≈ 56–60°.
 * - Verde (bajo): tono ≈ 144–148°.
 * - Azul (sin sargazo): tono ≈ 193–197°, muy saturado. El mar tiene el mismo tono, por eso el
 *   detector de círculos además exige el anillo blanco alrededor de cada círculo.
 */
object LevelColorClassifier {

    private const val MIN_SATURATION = 0.6f
    private const val MIN_BLUE_SATURATION = 0.72f
    private const val MIN_VALUE = 0.45f

    /** Nivel del color [argb] o null si no es un color del semáforo (fondo, texto, mar claro…). */
    fun classify(argb: Int): SargassumLevel? {
        val (hue, saturation, value) = toHsv(argb)
        if (saturation < MIN_SATURATION || value < MIN_VALUE) return null
        return when {
            hue < 20f || hue >= 340f -> SargassumLevel.EXCESSIVE
            hue >= 30f && hue < 51f -> SargassumLevel.ABUNDANT
            hue >= 51f && hue < 75f -> SargassumLevel.MODERATE
            hue >= 110f && hue < 170f -> SargassumLevel.LOW
            hue >= 185f && hue < 210f && saturation >= MIN_BLUE_SATURATION -> SargassumLevel.NO_SARGASSUM
            else -> null
        }
    }

    /** true si el píxel es blanco o casi blanco (el anillo que rodea cada círculo). */
    fun isLight(argb: Int): Boolean {
        val (_, saturation, value) = toHsv(argb)
        return saturation < 0.4f && value > 0.8f
    }

    fun toHsv(argb: Int): Hsv {
        val r = (argb shr 16 and 0xFF) / 255f
        val g = (argb shr 8 and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC
        val hue = when {
            delta == 0f -> 0f
            maxC == r -> 60f * (((g - b) / delta).mod(6f))
            maxC == g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }
        val saturation = if (maxC == 0f) 0f else delta / maxC
        return Hsv(hue = hue, saturation = saturation, value = maxC)
    }
}
