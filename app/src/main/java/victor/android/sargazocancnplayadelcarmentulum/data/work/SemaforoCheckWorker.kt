package victor.android.sargazocancnplayadelcarmentulum.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import victor.android.sargazocancnplayadelcarmentulum.domain.notification.SemaforoUpdateChecker

/**
 * Tarea periódica: revisa si hay un semáforo nuevo, avisa al usuario y limpia el historial viejo.
 */
@HiltWorker
class SemaforoCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val checker: SemaforoUpdateChecker,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val checkFailed = try {
            checker.check()
            false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin internet o Firestore no respondió: se vuelve a intentar más tarde.
            true
        }

        // La limpieza es local, así que se hace aunque haya fallado la red.
        try {
            checker.cleanOldRecords()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Se intentará de nuevo en la próxima revisión.
        }

        return if (checkFailed && runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
