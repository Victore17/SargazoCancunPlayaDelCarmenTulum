package victor.android.sargazocancnplayadelcarmentulum

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import victor.android.sargazocancnplayadelcarmentulum.data.work.SemaforoWorkScheduler
import javax.inject.Inject

/** Clase Application: punto de entrada de Hilt y de la revisión periódica de semáforos. */
@HiltAndroidApp
class SargazoApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workScheduler: SemaforoWorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        workScheduler.schedule()
    }
}
