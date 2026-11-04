package victor.android.sargazocancnplayadelcarmentulum.domain.repository

import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport

/** Semáforos publicados: se leen de Firestore y se guarda una copia en Room. */
interface SemaforoRepository {

    /**
     * Descarga los semáforos de los últimos dos meses y los guarda en Room.
     * Lanza una excepción si no hay conexión o Firestore no responde.
     */
    suspend fun refresh()

    /** Publica [report] en Firestore (solo el administrador) y lo guarda en Room. */
    suspend fun publish(report: SemaforoReport)
}
