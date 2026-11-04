package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.text.Normalizer
import java.util.Locale

private val accentMarks = Regex("\\p{Mn}+")

/** Minúsculas y sin acentos, para que "tulúm" encuentre "Tulum". */
fun normalizeForSearch(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(accentMarks, "")
        .lowercase(Locale.ROOT)
        .trim()

/**
 * Filtra las playas por texto y por niveles. Una búsqueda vacía o un conjunto de niveles vacío
 * no filtran nada. El resultado queda ordenado por nombre.
 */
fun List<BeachStatus>.filterBeaches(query: String, levels: Set<SargassumLevel>): List<BeachStatus> {
    val normalizedQuery = normalizeForSearch(query)
    return filter { status ->
        (levels.isEmpty() || status.level in levels) &&
            (normalizedQuery.isEmpty() || normalizeForSearch(status.beach.name).contains(normalizedQuery))
    }.sortedBy { normalizeForSearch(it.beach.name) }
}
