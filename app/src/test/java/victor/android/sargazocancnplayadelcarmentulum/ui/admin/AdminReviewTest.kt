package victor.android.sargazocancnplayadelcarmentulum.ui.admin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

class AdminReviewTest {

    private val known = ReviewRow("Tankah", SargassumLevel.LOW, detected = true, hasLocation = true)
    private val newBeach = ReviewRow("Playa Inventada", SargassumLevel.LOW, detected = true, hasLocation = false)
    private val missing = ReviewRow("Akumal", SargassumLevel.NO_DATA, detected = false, hasLocation = true)
    private val missingResolved = missing.copy(level = SargassumLevel.MODERATE)

    private fun review(vararg rows: ReviewRow, unlabeled: Int = 0) = AdminStep.Review(
        imageUri = "content://foto",
        date = LocalDate.of(2026, 11, 3),
        dateDetected = true,
        rows = rows.toList(),
        unlabeledDotCount = unlabeled,
    )

    @Test
    fun `playa conocida y detectada no necesita revision`() {
        assertFalse(known.needsReview)
        assertFalse(known.isNewBeach)
        assertFalse(known.isMissing)
    }

    @Test
    fun `nombre nuevo necesita revision`() {
        assertTrue(newBeach.isNewBeach)
        assertTrue(newBeach.needsReview)
    }

    @Test
    fun `playa que no aparecio necesita revision hasta que se le asigna nivel`() {
        assertTrue(missing.isMissing)
        assertTrue(missing.needsReview)
        assertFalse(missingResolved.isMissing)
        assertFalse(missingResolved.needsReview)
    }

    @Test
    fun `revision limpia no pide atencion`() {
        assertFalse(review(known, missingResolved).needsAttention)
    }

    @Test
    fun `cada tipo de problema pide atencion`() {
        assertTrue(review(known, newBeach).needsAttention)
        assertTrue(review(known, missing).needsAttention)
        assertTrue(review(known, unlabeled = 2).needsAttention)
    }

    @Test
    fun `cuenta nombres nuevos, faltantes y detectadas`() {
        val review = review(known, newBeach, missing, missingResolved)

        assertEquals(listOf("Playa Inventada"), review.newBeaches.map { it.name })
        assertEquals(1, review.missingCount)
        assertEquals(2, review.detectedCount)
    }
}
