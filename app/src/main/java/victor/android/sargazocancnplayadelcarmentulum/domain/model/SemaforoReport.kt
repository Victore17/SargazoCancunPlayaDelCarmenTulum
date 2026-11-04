package victor.android.sargazocancnplayadelcarmentulum.domain.model

import java.time.LocalDate

/** Nivel de una playa en un semáforo. [beachName] es el nombre oficial, tal como aparece en la imagen. */
data class BeachReading(
    val beachName: String,
    val level: SargassumLevel,
)

/** Semáforo de un día: lo que el administrador publica y lo que todas las apps leen. */
data class SemaforoReport(
    val date: LocalDate,
    val readings: List<BeachReading>,
)

/** Playa con el nivel del semáforo más reciente ([level] es [SargassumLevel.NO_DATA] si no aparece). */
data class BeachStatus(
    val beach: Beach,
    val level: SargassumLevel,
    val date: LocalDate?,
)
