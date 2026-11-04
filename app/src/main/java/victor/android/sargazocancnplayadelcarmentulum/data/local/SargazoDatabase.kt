package victor.android.sargazocancnplayadelcarmentulum.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.BeachDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.LevelRecordDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.SettingsDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.LevelRecordEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.SettingsEntity

@Database(
    entities = [BeachEntity::class, LevelRecordEntity::class, SettingsEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SargazoDatabase : RoomDatabase() {

    abstract fun beachDao(): BeachDao

    abstract fun levelRecordDao(): LevelRecordDao

    abstract fun settingsDao(): SettingsDao

    companion object {
        const val NAME = "sargazo.db"
    }
}
