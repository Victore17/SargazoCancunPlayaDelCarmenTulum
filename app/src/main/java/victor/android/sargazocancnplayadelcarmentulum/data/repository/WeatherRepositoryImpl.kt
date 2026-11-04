package victor.android.sargazocancnplayadelcarmentulum.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import victor.android.sargazocancnplayadelcarmentulum.data.mapper.parseObservationTime
import victor.android.sargazocancnplayadelcarmentulum.data.mapper.toDomain
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.ForecastApiService
import victor.android.sargazocancnplayadelcarmentulum.data.remote.weather.MarineApiService
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val forecastApi: ForecastApiService,
    private val marineApi: MarineApiService,
) : WeatherRepository {

    override suspend fun getConditions(location: GeoPoint): BeachConditions = coroutineScope {
        // Las dos consultas van en paralelo; si una falla se muestra la otra.
        val airCall = async { runCatchingNonCancellation { forecastApi.getCurrentWeather(location.latitude, location.longitude) } }
        val seaCall = async { runCatchingNonCancellation { marineApi.getCurrentSea(location.latitude, location.longitude) } }
        val airResult = airCall.await()
        val seaResult = seaCall.await()

        val air = airResult.getOrNull()?.current?.toDomain()
        val sea = seaResult.getOrNull()?.current?.toDomain()

        // Si las dos consultas fallaron, se lanza el primer error para que la pantalla avise.
        if (airResult.isFailure && seaResult.isFailure) throw airResult.exceptionOrNull()!!
        // Si respondieron pero sin ningún dato útil, también es un error.
        if (air == null && sea == null) throw NoConditionsException()

        BeachConditions(
            air = air,
            sea = sea,
            observedAt = parseObservationTime(airResult.getOrNull()?.current?.time)
                ?: parseObservationTime(seaResult.getOrNull()?.current?.time),
        )
    }

    /** Como runCatching, pero deja pasar la cancelación de la corrutina. */
    private inline fun <T> runCatchingNonCancellation(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/** Open-Meteo respondió, pero sin ningún dato del clima ni del mar para esa ubicación. */
class NoConditionsException : Exception("Open-Meteo no devolvió datos para esta ubicación")
