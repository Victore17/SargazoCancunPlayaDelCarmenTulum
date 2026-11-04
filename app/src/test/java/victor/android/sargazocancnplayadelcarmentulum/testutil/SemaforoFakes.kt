package victor.android.sargazocancnplayadelcarmentulum.testutil

import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import victor.android.sargazocancnplayadelcarmentulum.data.remote.SemaforoRemoteDataSource
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminSession
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleIdTokenProvider
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleTokenResult
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoImageReader
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SignInResult
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.SemaforoAnalysis
import java.time.LocalDate

/** Firestore falso: guarda los semáforos en memoria. */
class FakeSemaforoRemoteDataSource : SemaforoRemoteDataSource {
    val reports = mutableListOf<SemaforoReport>()
    var error: Exception? = null
    var lastFetchSince: LocalDate? = null

    override suspend fun fetchSince(date: LocalDate): List<SemaforoReport> {
        error?.let { throw it }
        lastFetchSince = date
        return reports.filter { it.date >= date }.sortedBy { it.date }
    }

    override suspend fun publish(report: SemaforoReport) {
        error?.let { throw it }
        reports.removeAll { it.date == report.date }
        reports += report
    }
}

/** Repositorio de semáforos falso para probar ViewModels. */
class FakeSemaforoRepository : SemaforoRepository {
    var refreshCount = 0
    var refreshError: Exception? = null
    var publishError: Exception? = null
    val published = mutableListOf<SemaforoReport>()

    /** Si no es null, refresh() espera a que se complete (para probar el estado "actualizando"). */
    var refreshGate: CompletableDeferred<Unit>? = null

    /** Se ejecuta durante refresh(): sirve para simular que llegan datos nuevos a Room. */
    var onRefresh: (() -> Unit)? = null

    override suspend fun refresh() {
        refreshCount++
        onRefresh?.invoke()
        refreshGate?.await()
        refreshError?.let { throw it }
    }

    override suspend fun publish(report: SemaforoReport) {
        publishError?.let { throw it }
        published += report
    }
}

/** Inicio de sesión de Firebase falso. */
class FakeAdminAuthRepository(
    initial: AdminSession = AdminSession.SignedOut,
) : AdminAuthRepository {
    val session = MutableStateFlow(initial)
    var signInResult = SignInResult.SUCCESS
    var isAdminOnSignIn = true
    var emailOnSignIn = "admin@example.com"
    val signInTokens = mutableListOf<String>()

    override fun observeSession(): Flow<AdminSession> = session

    override suspend fun signInWithGoogle(idToken: String): SignInResult {
        signInTokens += idToken
        if (signInResult == SignInResult.SUCCESS) {
            session.value = AdminSession.SignedIn(email = emailOnSignIn, isAdmin = isAdminOnSignIn)
        }
        return signInResult
    }

    override suspend fun signOut() {
        session.value = AdminSession.SignedOut
    }
}

/** Selector de cuentas de Google falso: devuelve el resultado configurado. */
class FakeGoogleIdTokenProvider : GoogleIdTokenProvider {
    var result: GoogleTokenResult = GoogleTokenResult.Token("token-de-google")
    var requestCount = 0

    override suspend fun requestIdToken(activityContext: Context): GoogleTokenResult {
        requestCount++
        return result
    }
}

/** Lector de imagen falso: devuelve el análisis configurado o lanza el error configurado. */
class FakeSemaforoImageReader : SemaforoImageReader {
    var result: SemaforoAnalysis = SemaforoAnalysis.NotASemaforo
    var error: Exception? = null
    var lastKnownNames: Collection<String> = emptyList()

    override suspend fun read(imageUri: String, knownBeachNames: Collection<String>): SemaforoAnalysis {
        lastKnownNames = knownBeachNames
        error?.let { throw it }
        return result
    }
}
