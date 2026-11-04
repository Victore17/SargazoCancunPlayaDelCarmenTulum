package victor.android.sargazocancnplayadelcarmentulum.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import java.time.LocalDate

/** Repositorio falso para probar ViewModels sin base de datos. */
class FakeBeachRepository : BeachRepository {
    val beaches = MutableStateFlow<List<Beach>>(emptyList())

    override fun observeBeaches(): Flow<List<Beach>> = beaches
    override fun observeFavoriteBeaches(): Flow<List<Beach>> =
        beaches.map { list -> list.filter { it.isFavorite } }
    override fun observeBeach(beachId: Long): Flow<Beach?> =
        beaches.map { list -> list.firstOrNull { it.id == beachId } }
    override fun observeLevelHistory(beachId: Long): Flow<List<LevelRecord>> = emptyFlow()
    override suspend fun setFavorite(beachId: Long, isFavorite: Boolean) = Unit
    override suspend fun setNotificationsEnabled(beachId: Long, enabled: Boolean) = Unit
    override suspend fun saveLevelRecords(records: List<LevelRecord>) = Unit
    override suspend fun deleteLevelRecordsBefore(date: LocalDate) = Unit
}
