package victor.android.sargazocancnplayadelcarmentulum.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import victor.android.sargazocancnplayadelcarmentulum.data.auth.CredentialManagerGoogleIdTokenProvider
import victor.android.sargazocancnplayadelcarmentulum.data.auth.FirebaseAdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.data.image.MlKitSemaforoImageReader
import victor.android.sargazocancnplayadelcarmentulum.data.remote.FirestoreSemaforoDataSource
import victor.android.sargazocancnplayadelcarmentulum.data.remote.SemaforoRemoteDataSource
import victor.android.sargazocancnplayadelcarmentulum.data.repository.BeachRepositoryImpl
import victor.android.sargazocancnplayadelcarmentulum.data.repository.SemaforoRepositoryImpl
import victor.android.sargazocancnplayadelcarmentulum.data.repository.SettingsRepositoryImpl
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleIdTokenProvider
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoImageReader
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SettingsRepository
import victor.android.sargazocancnplayadelcarmentulum.ui.shell.ElapsedTimeSource
import victor.android.sargazocancnplayadelcarmentulum.ui.shell.SystemElapsedTimeSource
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

    @Binds
    @Singleton
    abstract fun bindSemaforoRepository(impl: SemaforoRepositoryImpl): SemaforoRepository

    @Binds
    abstract fun bindSemaforoRemoteDataSource(impl: FirestoreSemaforoDataSource): SemaforoRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAdminAuthRepository(impl: FirebaseAdminAuthRepository): AdminAuthRepository

    @Binds
    abstract fun bindGoogleIdTokenProvider(impl: CredentialManagerGoogleIdTokenProvider): GoogleIdTokenProvider

    @Binds
    abstract fun bindSemaforoImageReader(impl: MlKitSemaforoImageReader): SemaforoImageReader

    @Binds
    abstract fun bindElapsedTimeSource(impl: SystemElapsedTimeSource): ElapsedTimeSource
}
