package victor.android.sargazocancnplayadelcarmentulum.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.ForecastApiService
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.MarineApiService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // Open-Meteo puede agregar campos nuevos sin avisar: se ignoran.
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("User-Agent", "SargazoApp/1.0 (Android)").build())
        }
        .build()

    @Provides
    @Singleton
    fun provideForecastApi(client: OkHttpClient, json: Json): ForecastApiService =
        retrofit(ForecastApiService.BASE_URL, client, json).create(ForecastApiService::class.java)

    @Provides
    @Singleton
    fun provideMarineApi(client: OkHttpClient, json: Json): MarineApiService =
        retrofit(MarineApiService.BASE_URL, client, json).create(MarineApiService::class.java)

    private fun retrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
