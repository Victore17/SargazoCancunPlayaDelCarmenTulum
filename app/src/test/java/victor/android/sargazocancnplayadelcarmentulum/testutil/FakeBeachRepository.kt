package victor.android.sargazocancnplayadelcarmentulum.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import java.time.LocalDate

/** Repositorio falso para probar ViewModels sin base de datos. */
class FakeBeachRepository : BeachRepository {
    val beaches = MutableStateFlow<List<Beach>>(emptyList())
    val statuses = MutableStateFlow<List<BeachStatus>>(emptyList())

    /** Historial de niveles por id de playa. */
    val histories = MutableStateFlow<Map<Long, List<LevelRecord>>>(emptyMap())

    override fun observeBeaches(): Flow<List<Beach>> = beaches
    override fun observeBeachStatuses(): Flow<List<BeachStatus>> = statuses
    override fun observeFavoriteBeaches(): Flow<List<Beach>> =
        beaches.map { list -> list.filter { it.isFavorite } }
    override fun observeBeach(beachId: Long): Flow<Beach?> =
        beaches.map { list -> list.firstOrNull { it.id == beachId } }
    override fun observeLevelHistory(beachId: Long): Flow<List<LevelRecord>> =
        histories.map { it[beachId].orEmpty().sortedByDescending { record -> record.date } }
    override suspend fun setFavorite(beachId: Long, isFavorite: Boolean) {
        beaches.value = beaches.value.map { if (it.id == beachId) it.copy(isFavorite = isFavorite) else it }
    }
    override suspend fun setNotificationsEnabled(beachId: Long, enabled: Boolean) = Unit
    override suspend fun saveLevelRecords(records: List<LevelRecord>) = Unit
    override suspend fun deleteLevelRecordsBefore(date: LocalDate) = Unit
}
