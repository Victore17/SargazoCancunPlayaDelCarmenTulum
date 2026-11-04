package victor.android.sargazocancnplayadelcarmentulum.domain.model

import java.time.LocalDate

/** Nivel de sargazo registrado para una playa en una fecha del semáforo. */
data class LevelRecord(
    val id: Long = 0,
    val beachId: Long,
    val level: SargassumLevel,
    val date: LocalDate,
)
