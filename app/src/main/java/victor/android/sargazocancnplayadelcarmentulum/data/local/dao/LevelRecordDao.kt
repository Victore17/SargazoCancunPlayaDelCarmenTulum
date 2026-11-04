package victor.android.sargazocancnplayadelcarmentulum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.LevelRecordEntity
import java.time.LocalDate

@Dao
interface LevelRecordDao {

    @Query("SELECT * FROM level_records WHERE beach_id = :beachId ORDER BY date DESC")
    fun observeForBeach(beachId: Long): Flow<List<LevelRecordEntity>>

    /**
     * Guarda registros. Si ya hay uno de la misma playa y fecha, se reemplaza.
     * (Se usa REPLACE y no @Upsert porque el conflicto es con el índice único
     * de playa y fecha, no con la llave primaria.)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<LevelRecordEntity>)

    /** Fecha del semáforo más reciente guardado, o null si no hay ninguno. */
    @Query("SELECT MAX(date) FROM level_records")
    suspend fun latestDate(): LocalDate?

    /** [epochDay] es la fecha límite expresada como días desde 1970-01-01. */
    @Query("DELETE FROM level_records WHERE date < :epochDay")
    suspend fun deleteBefore(epochDay: Long)
}
