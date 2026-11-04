package victor.android.sargazocancnplayadelcarmentulum.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import victor.android.sargazocancnplayadelcarmentulum.data.repository.BeachRepositoryImpl
import victor.android.sargazocancnplayadelcarmentulum.data.repository.SettingsRepositoryImpl
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBeachRepository(impl: BeachRepositoryImpl): BeachRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
