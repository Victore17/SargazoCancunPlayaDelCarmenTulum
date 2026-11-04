package victor.android.sargazocancnplayadelcarmentulum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

/** Resultado de consulta: una playa con su nivel en el semáforo más reciente (null si no aparece). */
data class BeachWithLevelEntity(
    @Embedded
    val beach: BeachEntity,
    val level: SargassumLevel?,
    @ColumnInfo(name = "level_date")
    val levelDate: LocalDate?,
)
