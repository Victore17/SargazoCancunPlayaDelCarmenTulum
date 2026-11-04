package victor.android.sargazocancnplayadelcarmentulum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachWithLevelEntity

@Dao
interface BeachDao {

    @Query("SELECT * FROM beaches ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<BeachEntity>>

    @Query("SELECT * FROM beaches ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<BeachEntity>

    @Query("SELECT * FROM beaches WHERE is_favorite = 1 ORDER BY name COLLATE NOCASE")
    fun observeFavorites(): Flow<List<BeachEntity>>

    @Query("SELECT * FROM beaches WHERE id = :beachId")
    fun observeById(beachId: Long): Flow<BeachEntity?>

    /**
     * Cada playa con su nivel en el semáforo más reciente (la fecha más alta guardada).
     * Las playas que no aparecen en ese semáforo traen level y level_date en null.
     */
    @Query(
        """
        SELECT beaches.*, level_records.level AS level, level_records.date AS level_date
        FROM beaches
        LEFT JOIN level_records
            ON level_records.beach_id = beaches.id
            AND level_records.date = (SELECT MAX(date) FROM level_records)
        ORDER BY beaches.name COLLATE NOCASE
        """,
    )
    fun observeAllWithLatestLevel(): Flow<List<BeachWithLevelEntity>>

    /** Inserta playas; las que ya existen con el mismo nombre se ignoran. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(beaches: List<BeachEntity>)

    @Query("UPDATE beaches SET is_favorite = :isFavorite WHERE id = :beachId")
    suspend fun updateFavorite(beachId: Long, isFavorite: Boolean)

    @Query("UPDATE beaches SET notifications_enabled = :enabled WHERE id = :beachId")
    suspend fun updateNotificationsEnabled(beachId: Long, enabled: Boolean)
}
