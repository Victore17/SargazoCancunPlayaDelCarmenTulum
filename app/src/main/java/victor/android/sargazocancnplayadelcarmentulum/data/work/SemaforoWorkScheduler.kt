package victor.android.sargazocancnplayadelcarmentulum.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Programa la revisión periódica de semáforos nuevos. */
class SemaforoWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Revisa cada 3 a 6 horas: el periodo es de 6 horas y la ventana flexible, de 3, así que
     * Android puede ejecutarla en cualquier momento de las últimas 3 horas de cada periodo.
     * Si ya estaba programada se conserva, para no reiniciar el reloj en cada apertura de la app.
     */
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<SemaforoCheckWorker>(
            repeatInterval = REPEAT_HOURS,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = FLEX_HOURS,
            flexTimeIntervalUnit = TimeUnit.HOURS,
        )
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val WORK_NAME = "semaforo_check"
        const val REPEAT_HOURS = 6L
        const val FLEX_HOURS = 3L
    }
}
