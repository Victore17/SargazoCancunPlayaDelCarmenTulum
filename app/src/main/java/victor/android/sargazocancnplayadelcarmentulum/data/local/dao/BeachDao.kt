package victor.android.sargazocancnplayadelcarmentulum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity

@Dao
interface BeachDao {

    @Query("SELECT * FROM beaches ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<BeachEntity>>

    @Query("SELECT * FROM beaches WHERE is_favorite = 1 ORDER BY name COLLATE NOCASE")
    fun observeFavorites(): Flow<List<BeachEntity>>

    @Query("SELECT * FROM beaches WHERE id = :beachId")
    fun observeById(beachId: Long): Flow<BeachEntity?>

    /** Inserta playas; las que ya existen con el mismo nombre se ignoran. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(beaches: List<BeachEntity>)

    @Query("UPDATE beaches SET is_favorite = :isFavorite WHERE id = :beachId")
    suspend fun updateFavorite(beachId: Long, isFavorite: Boolean)

    @Query("UPDATE beaches SET notifications_enabled = :enabled WHERE id = :beachId")
    suspend fun updateNotificationsEnabled(beachId: Long, enabled: Boolean)
}
