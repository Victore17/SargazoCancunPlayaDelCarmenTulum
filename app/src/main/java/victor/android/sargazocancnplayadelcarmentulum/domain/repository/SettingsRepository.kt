package victor.android.sargazocancnplayadelcarmentulum.domain.repository

import kotlinx.coroutines.flow.Flow
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings

/** Acceso a los ajustes de la app. */
interface SettingsRepository {

    fun observeSettings(): Flow<AppSettings>

    /** Aplica [transform] a los ajustes actuales y guarda el resultado. */
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
}
