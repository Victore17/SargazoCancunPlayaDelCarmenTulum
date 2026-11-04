package victor.android.sargazocancnplayadelcarmentulum.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.BeachDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.LevelRecordDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.SettingsDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.LevelRecordEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.SettingsEntity

/** DAO de playas en memoria que imita el comportamiento de Room. */
class FakeBeachDao(initial: List<BeachEntity> = emptyList()) : BeachDao {
    val beaches = MutableStateFlow(initial)

    override fun observeAll(): Flow<List<BeachEntity>> =
        beaches.map { list -> list.sortedBy { it.name.lowercase() } }

    override fun observeFavorites(): Flow<List<BeachEntity>> =
        observeAll().map { list -> list.filter { it.isFavorite } }

    override fun observeById(beachId: Long): Flow<BeachEntity?> =
        beaches.map { list -> list.firstOrNull { it.id == beachId } }

    override suspend fun insertAll(beaches: List<BeachEntity>) {
        this.beaches.update { current ->
            var nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            val newOnes = beaches
                .filter { new -> current.none { it.name == new.name } }
                .map { it.copy(id = nextId++) }
            current + newOnes
        }
    }

    override suspend fun updateFavorite(beachId: Long, isFavorite: Boolean) = beaches.update { list ->
        list.map { if (it.id == beachId) it.copy(isFavorite = isFavorite) else it }
    }

    override suspend fun updateNotificationsEnabled(beachId: Long, enabled: Boolean) =
        beaches.update { list ->
            list.map { if (it.id == beachId) it.copy(notificationsEnabled = enabled) else it }
        }
}

/** DAO de registros de nivel en memoria (único por playa y fecha, como en Room). */
class FakeLevelRecordDao : LevelRecordDao {
    val records = MutableStateFlow<List<LevelRecordEntity>>(emptyList())

    override fun observeForBeach(beachId: Long): Flow<List<LevelRecordEntity>> =
        records.map { list -> list.filter { it.beachId == beachId }.sortedByDescending { it.date } }

    override suspend fun upsertAll(records: List<LevelRecordEntity>) {
        this.records.update { current ->
            records.fold(current) { acc, record ->
                acc.filterNot { it.beachId == record.beachId && it.date == record.date } + record
            }
        }
    }

    override suspend fun deleteBefore(epochDay: Long) = records.update { list ->
        list.filter { it.date.toEpochDay() >= epochDay }
    }
}

/** DAO de ajustes en memoria. */
class FakeSettingsDao(initial: SettingsEntity? = null) : SettingsDao {
    val settings = MutableStateFlow(initial)

    override fun observe(): Flow<SettingsEntity?> = settings
    override suspend fun get(): SettingsEntity? = settings.value
    override suspend fun upsert(settings: SettingsEntity) {
        this.settings.value = settings
    }
}
