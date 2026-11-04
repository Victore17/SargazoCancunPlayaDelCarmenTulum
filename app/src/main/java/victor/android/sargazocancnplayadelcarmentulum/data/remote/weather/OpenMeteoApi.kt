package victor.android.sargazocancnplayadelcarmentulum.data.remote.weather

import retrofit2.http.GET
import retrofit2.http.Query

/** Open-Meteo no necesita clave de API. Uso gratuito para proyectos sin fines comerciales. */
interface ForecastApiService {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("timezone") timezone: String = TIMEZONE,
    ): ForecastResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m"
    }
}

interface MarineApiService {

    @GET("v1/marine")
    suspend fun getCurrentSea(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("timezone") timezone: String = TIMEZONE,
    ): MarineResponse

    companion object {
        const val BASE_URL = "https://marine-api.open-meteo.com/"
        const val CURRENT_FIELDS = "wave_height,wave_direction,wave_period,sea_surface_temperature"
    }
}

/** Hora de Quintana Roo, para que la hora de la medición coincida con la de la playa. */
const val TIMEZONE = "America/Cancun"
