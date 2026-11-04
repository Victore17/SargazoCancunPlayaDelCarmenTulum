package victor.android.sargazocancnplayadelcarmentulum.ui.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretTapDetectorTest {

    private val detector = SecretTapDetector(requiredTaps = 7, maxIntervalMillis = 1_500)

    /** Toca [count] veces con [intervalMillis] entre toques y devuelve los resultados. */
    private fun tap(count: Int, startMillis: Long = 0, intervalMillis: Long = 300): List<Boolean> =
        (0 until count).map { detector.onTap(startMillis + it * intervalMillis) }

    @Test
    fun `siete toques seguidos desbloquean en el septimo`() {
        val results = tap(7)

        assertEquals(List(6) { false } + true, results)
    }

    @Test
    fun `seis toques no desbloquean`() {
        assertTrue(tap(6).none { it })
    }

    @Test
    fun `una pausa larga reinicia la cuenta`() {
        tap(5)
        // Pausa de 2 segundos: la cuenta vuelve a empezar.
        val afterPause = tap(count = 6, startMillis = 5 * 300 + 2_000)

        assertTrue(afterPause.none { it })
        assertTrue(detector.onTap(5 * 300 + 2_000 + 6 * 300))
    }

    @Test
    fun `reset corta la secuencia`() {
        tap(6)
        detector.reset()

        assertFalse(detector.onTap(6 * 300))
    }

    @Test
    fun `despues de desbloquear la cuenta empieza de nuevo`() {
        tap(7)

        assertTrue(tap(count = 6, startMillis = 10_000).none { it })
    }
}
