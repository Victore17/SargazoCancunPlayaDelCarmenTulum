package victor.android.sargazocancnplayadelcarmentulum.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.SettingsDao
import victor.android.sargazocancnplayadelcarmentulum.data.mapper.toDomain
import victor.android.sargazocancnplayadelcarmentulum.data.mapper.toEntity
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
) : SettingsRepository {

    /** Si todavía no hay fila de ajustes, se usan los valores por defecto. */
    override fun observeSettings(): Flow<AppSettings> =
        settingsDao.observe().map { it?.toDomain() ?: AppSettings() }

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = settingsDao.get()?.toDomain() ?: AppSettings()
        settingsDao.upsert(transform(current).toEntity())
    }
}
