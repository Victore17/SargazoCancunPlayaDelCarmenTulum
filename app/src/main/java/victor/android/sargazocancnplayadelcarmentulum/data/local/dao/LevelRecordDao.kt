package victor.android.sargazocancnplayadelcarmentulum.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.LevelRecordEntity

@Dao
interface LevelRecordDao {

    @Query("SELECT * FROM level_records WHERE beach_id = :beachId ORDER BY date DESC")
    fun observeForBeach(beachId: Long): Flow<List<LevelRecordEntity>>

    @Upsert
    suspend fun upsertAll(records: List<LevelRecordEntity>)

    /** [epochDay] es la fecha límite expresada como días desde 1970-01-01. */
    @Query("DELETE FROM level_records WHERE date < :epochDay")
    suspend fun deleteBefore(epochDay: Long)
}
