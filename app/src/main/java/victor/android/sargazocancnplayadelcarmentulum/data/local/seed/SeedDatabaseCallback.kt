package victor.android.sargazocancnplayadelcarmentulum.data.local.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.BeachDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.SettingsDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.SettingsEntity
import victor.android.sargazocancnplayadelcarmentulum.di.ApplicationScope
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import javax.inject.Inject
import javax.inject.Provider

/**
 * Llena la base de datos la primera vez que se crea: playas conocidas y ajustes por defecto.
 * Se usan [Provider] para evitar una dependencia circular con la base de datos.
 */
class SeedDatabaseCallback @Inject constructor(
    private val beachDao: Provider<BeachDao>,
    private val settingsDao: Provider<SettingsDao>,
    @param:ApplicationScope private val scope: CoroutineScope,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            beachDao.get().insertAll(KnownBeaches.all)
            val defaults = AppSettings()
            settingsDao.get().upsert(
                SettingsEntity(
                    allNotificationsEnabled = defaults.allNotificationsEnabled,
                    generalUpdateNotificationsEnabled = defaults.generalUpdateNotificationsEnabled,
                    favoriteChangeNotificationsEnabled = defaults.favoriteChangeNotificationsEnabled,
                ),
            )
        }
    }
}
