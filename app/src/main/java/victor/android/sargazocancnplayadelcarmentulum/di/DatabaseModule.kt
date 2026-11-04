package victor.android.sargazocancnplayadelcarmentulum.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import victor.android.sargazocancnplayadelcarmentulum.data.local.Migrations
import victor.android.sargazocancnplayadelcarmentulum.data.local.SargazoDatabase
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.BeachDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.LevelRecordDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.SettingsDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.seed.SeedDatabaseCallback
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        seedCallback: SeedDatabaseCallback,
    ): SargazoDatabase =
        Room.databaseBuilder(context, SargazoDatabase::class.java, SargazoDatabase.NAME)
            .addCallback(seedCallback)
            .addMigrations(*Migrations.ALL)
            .build()

    @Provides
    fun provideBeachDao(database: SargazoDatabase): BeachDao = database.beachDao()

    @Provides
    fun provideLevelRecordDao(database: SargazoDatabase): LevelRecordDao = database.levelRecordDao()

    @Provides
    fun provideSettingsDao(database: SargazoDatabase): SettingsDao = database.settingsDao()
}
