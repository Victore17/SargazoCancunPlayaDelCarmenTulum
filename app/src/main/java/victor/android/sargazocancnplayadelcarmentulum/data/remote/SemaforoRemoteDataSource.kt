package victor.android.sargazocancnplayadelcarmentulum.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import java.time.LocalDate
import javax.inject.Inject

/** Semáforos publicados en el servidor. */
interface SemaforoRemoteDataSource {

    /** Semáforos con fecha igual o posterior a [date], del más antiguo al más reciente. */
    suspend fun fetchSince(date: LocalDate): List<SemaforoReport>

    suspend fun publish(report: SemaforoReport)
}

/** Implementación con Cloud Firestore. Las reglas de seguridad solo dejan escribir al administrador. */
class FirestoreSemaforoDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : SemaforoRemoteDataSource {

    override suspend fun fetchSince(date: LocalDate): List<SemaforoReport> =
        firestore.collection(SemaforoDocumentMapper.COLLECTION)
            .whereGreaterThanOrEqualTo(SemaforoDocumentMapper.FIELD_DATE, date.toString())
            .orderBy(SemaforoDocumentMapper.FIELD_DATE, Query.Direction.ASCENDING)
            // Siempre del servidor: si no hay conexión falla y la app muestra lo guardado en Room.
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { document -> document.data?.let(SemaforoDocumentMapper::fromFields) }

    override suspend fun publish(report: SemaforoReport) {
        val fields = SemaforoDocumentMapper.toFields(report) +
            (SemaforoDocumentMapper.FIELD_PUBLISHED_AT to FieldValue.serverTimestamp())
        firestore.collection(SemaforoDocumentMapper.COLLECTION)
            .document(SemaforoDocumentMapper.documentId(report.date))
            .set(fields)
            .await()
    }
}
