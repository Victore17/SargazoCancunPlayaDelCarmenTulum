package victor.android.sargazocancnplayadelcarmentulum.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.BeachDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.LevelRecordDao
import victor.android.sargazocancnplayadelcarmentulum.data.mapper.toDomain
import victor.android.sargazocancnplayadelcarmentulum.data.mapper.toEntity
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import java.time.LocalDate
import javax.inject.Inject

class BeachRepositoryImpl @Inject constructor(
    private val beachDao: BeachDao,
    private val levelRecordDao: LevelRecordDao,
) : BeachRepository {

    override fun observeBeaches(): Flow<List<Beach>> =
        beachDao.observeAll().map { beaches -> beaches.map { it.toDomain() } }

    override fun observeBeachStatuses(): Flow<List<BeachStatus>> =
        beachDao.observeAllWithLatestLevel().map { rows -> rows.map { it.toDomain() } }

    override fun observeFavoriteBeaches(): Flow<List<Beach>> =
        beachDao.observeFavorites().map { beaches -> beaches.map { it.toDomain() } }

    override fun observeBeach(beachId: Long): Flow<Beach?> =
        beachDao.observeById(beachId).map { it?.toDomain() }

    override fun observeLevelHistory(beachId: Long): Flow<List<LevelRecord>> =
        levelRecordDao.observeForBeach(beachId).map { records -> records.map { it.toDomain() } }

    override suspend fun setFavorite(beachId: Long, isFavorite: Boolean) =
        beachDao.updateFavorite(beachId, isFavorite)

    override suspend fun setNotificationsEnabled(beachId: Long, enabled: Boolean) =
        beachDao.updateNotificationsEnabled(beachId, enabled)

    override suspend fun saveLevelRecords(records: List<LevelRecord>) =
        levelRecordDao.upsertAll(records.map { it.toEntity() })

    override suspend fun deleteLevelRecordsBefore(date: LocalDate) =
        levelRecordDao.deleteBefore(date.toEpochDay())
}
