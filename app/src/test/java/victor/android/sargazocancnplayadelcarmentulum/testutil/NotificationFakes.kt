package victor.android.sargazocancnplayadelcarmentulum.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.notification.FavoriteLevelChange
import victor.android.sargazocancnplayadelcarmentulum.domain.notification.SemaforoNotifier
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SettingsRepository

/** Notificador falso: guarda lo que se pidió mostrar. */
class FakeSemaforoNotifier : SemaforoNotifier {
    var generalCount = 0
    val favoriteNotifications = mutableListOf<List<FavoriteLevelChange>>()

    override fun notifyGeneralUpdate() {
        generalCount++
    }

    override fun notifyFavoriteChanges(changes: List<FavoriteLevelChange>) {
        favoriteNotifications += changes
    }
}

/** Repositorio de ajustes falso, en memoria. */
class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    val settings = MutableStateFlow(initial)

    override fun observeSettings(): Flow<AppSettings> = settings

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        settings.value = transform(settings.value)
    }
}
