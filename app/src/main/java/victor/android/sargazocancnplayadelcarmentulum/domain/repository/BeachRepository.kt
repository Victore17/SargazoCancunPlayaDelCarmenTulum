package victor.android.sargazocancnplayadelcarmentulum.domain.repository

import kotlinx.coroutines.flow.Flow
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import java.time.LocalDate

/** Acceso a las playas y a su historial de niveles de sargazo. */
interface BeachRepository {

    fun observeBeaches(): Flow<List<Beach>>

    fun observeFavoriteBeaches(): Flow<List<Beach>>

    fun observeBeach(beachId: Long): Flow<Beach?>

    fun observeLevelHistory(beachId: Long): Flow<List<LevelRecord>>

    suspend fun setFavorite(beachId: Long, isFavorite: Boolean)

    suspend fun setNotificationsEnabled(beachId: Long, enabled: Boolean)

    suspend fun saveLevelRecords(records: List<LevelRecord>)

    /** Borra los registros anteriores a [date]. Las playas (y sus favoritas) nunca se borran. */
    suspend fun deleteLevelRecordsBefore(date: LocalDate)
}
