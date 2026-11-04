package victor.android.sargazocancnplayadelcarmentulum.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import victor.android.sargazocancnplayadelcarmentulum.data.local.SargazoDatabase
import victor.android.sargazocancnplayadelcarmentulum.data.work.SemaforoCheckWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * SOLO DEBUG. Sirve para probar las notificaciones sin esperar horas:
 * 1) borra de Room el semáforo más reciente (queda el anterior) y
 * 2) programa una revisión única que corre 10 segundos después.
 *
 * Uso: adb shell am broadcast -n victor.android.sargazocancnplayadelcarmentulum/.debug.DebugCheckTriggerReceiver
 */
@AndroidEntryPoint
class DebugCheckTriggerReceiver : BroadcastReceiver() {

    @Inject
    lateinit var database: SargazoDatabase

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = database.openHelper.writableDatabase
                val dates = db.query("SELECT COUNT(DISTINCT date) FROM level_records").use {
                    if (it.moveToFirst()) it.getInt(0) else 0
                }
                if (dates < 2) {
                    Log.w(TAG, "Hay $dates semáforo(s) en Room; se necesitan al menos 2. No se hizo nada.")
                    return@launch
                }
                db.execSQL("DELETE FROM level_records WHERE date = (SELECT MAX(date) FROM level_records)")
                Log.i(TAG, "Semáforo más reciente borrado de Room. Revisión en $DELAY_SECONDS s.")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<SemaforoCheckWorker>()
                        .setInitialDelay(DELAY_SECONDS, TimeUnit.SECONDS)
                        .build(),
                )
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "DebugCheckTrigger"
        const val DELAY_SECONDS = 10L
    }
}
