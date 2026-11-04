package victor.android.sargazocancnplayadelcarmentulum.ui.shell

import android.os.SystemClock
import javax.inject.Inject

/** Fuente de tiempo en milisegundos (inyectable para poder probar el contador de toques). */
fun interface ElapsedTimeSource {
    fun nowMillis(): Long
}

class SystemElapsedTimeSource @Inject constructor() : ElapsedTimeSource {
    override fun nowMillis(): Long = SystemClock.elapsedRealtime()
}

/**
 * Cuenta toques seguidos. Devuelve true en el toque número [requiredTaps], siempre que entre
 * cada toque y el anterior pasen como máximo [maxIntervalMillis]. Si pasa más tiempo, la
 * cuenta vuelve a empezar.
 */
class SecretTapDetector(
    private val requiredTaps: Int = 7,
    private val maxIntervalMillis: Long = 1_500,
) {
    private var count = 0
    private var lastTapMillis = Long.MIN_VALUE

    fun onTap(nowMillis: Long): Boolean {
        val isConsecutive = lastTapMillis != Long.MIN_VALUE && nowMillis - lastTapMillis <= maxIntervalMillis
        count = if (isConsecutive) count + 1 else 1
        lastTapMillis = nowMillis
        if (count >= requiredTaps) {
            reset()
            return true
        }
        return false
    }

    fun reset() {
        count = 0
        lastTapMillis = Long.MIN_VALUE
    }
}
