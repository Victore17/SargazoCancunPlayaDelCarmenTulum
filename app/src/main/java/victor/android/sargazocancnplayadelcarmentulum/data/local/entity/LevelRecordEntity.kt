package victor.android.sargazocancnplayadelcarmentulum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

/**
 * Registro de nivel de sargazo de una playa en una fecha (tabla "level_records").
 * Solo puede haber un registro por playa y fecha.
 */
@Entity(
    tableName = "level_records",
    foreignKeys = [
        ForeignKey(
            entity = BeachEntity::class,
            parentColumns = ["id"],
            childColumns = ["beach_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["beach_id", "date"], unique = true)],
)
data class LevelRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "beach_id")
    val beachId: Long,
    val level: SargassumLevel,
    val date: LocalDate,
)
