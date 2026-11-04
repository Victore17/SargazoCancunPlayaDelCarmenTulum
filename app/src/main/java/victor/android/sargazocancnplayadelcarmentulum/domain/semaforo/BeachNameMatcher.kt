package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import java.text.Normalizer

/**
 * Busca el nombre oficial de una playa a partir del texto leído por ML Kit,
 * tolerando errores de lectura ("Cancin Playa Marlin" → "Cancún Playa Marlín").
 */
class BeachNameMatcher(knownNames: Collection<String>) {

    private val known: List<Pair<String, String>> = knownNames.map { normalize(it) to it }

    /** Nombre oficial que corresponde a [text], o null si no se parece a ninguno. */
    fun match(text: String): String? {
        val normalized = normalize(text)
        if (normalized.length < MIN_LENGTH) return null

        known.firstOrNull { it.first == normalized }?.let { return it.second }

        var best: String? = null
        var bestScore = 0.0
        for ((knownNormalized, original) in known) {
            val score = maxOf(
                similarity(normalized, knownNormalized),
                // El renglón puede traer texto extra pegado al final (por ejemplo, el pie de la imagen).
                if (normalized.length > knownNormalized.length) {
                    similarity(normalized.take(knownNormalized.length), knownNormalized) - PREFIX_PENALTY
                } else {
                    0.0
                },
            )
            if (score > bestScore) {
                bestScore = score
                best = original
            }
        }
        return best.takeIf { bestScore >= MIN_SIMILARITY }
    }

    companion object {
        private const val MIN_LENGTH = 3
        /** Con 0.8 "Playa Inventada" se confundía con "Playa Encantada"; los errores reales de OCR dan ≥ 0.9. */
        private const val MIN_SIMILARITY = 0.85
        private const val PREFIX_PENALTY = 0.05

        /** Minúsculas, sin acentos ni signos, con un solo espacio entre palabras. */
        fun normalize(text: String): String =
            removeAccents(text).replace(Regex("[^a-z0-9]+"), " ").trim()

        /** Como [normalize], pero conserva "/" y "|" (separadores de la fecha). */
        fun normalizeKeepingSlashes(text: String): String =
            removeAccents(text).replace(Regex("[^a-z0-9/|]+"), " ").trim()

        private fun removeAccents(text: String): String =
            Normalizer.normalize(text, Normalizer.Form.NFD)
                .replace(Regex("\\p{Mn}+"), "")
                .lowercase()

        /** Similitud de 0 a 1 basada en la distancia de edición (Levenshtein). */
        fun similarity(a: String, b: String): Double {
            if (a.isEmpty() && b.isEmpty()) return 1.0
            val distance = levenshtein(a, b)
            return 1.0 - distance.toDouble() / maxOf(a.length, b.length)
        }

        private fun levenshtein(a: String, b: String): Int {
            var previous = IntArray(b.length + 1) { it }
            var current = IntArray(b.length + 1)
            for (i in 1..a.length) {
                current[0] = i
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    current[j] = minOf(current[j - 1] + 1, previous[j] + 1, previous[j - 1] + cost)
                }
                val swap = previous
                previous = current
                current = swap
            }
            return previous[b.length]
        }
    }
}
