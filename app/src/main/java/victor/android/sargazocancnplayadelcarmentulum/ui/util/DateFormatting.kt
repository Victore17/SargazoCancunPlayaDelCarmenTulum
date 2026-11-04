package victor.android.sargazocancnplayadelcarmentulum.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val longDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-MX"))

/** Fecha en español, por ejemplo "3 de noviembre de 2026". */
fun LocalDate.formatLong(): String = format(longDateFormatter)
