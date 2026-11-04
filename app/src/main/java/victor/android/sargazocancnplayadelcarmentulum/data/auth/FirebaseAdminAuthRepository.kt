package victor.android.sargazocancnplayadelcarmentulum.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminSession
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SignInResult
import javax.inject.Inject

/**
 * Sesión del administrador con Firebase Authentication (cuenta de Google).
 *
 * Una cuenta solo puede publicar si existe el documento `admins/{uid}` en Firestore
 * (se crea a mano en la consola). Las reglas de seguridad aplican la misma condición,
 * así que aunque alguien encuentre la pantalla oculta no puede escribir.
 */
class FirebaseAdminAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AdminAuthRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSession(): Flow<AdminSession> =
        currentUser()
            .distinctUntilChanged { old, new -> old?.uid == new?.uid }
            .flatMapLatest { user ->
                flow {
                    if (user == null) {
                        emit(AdminSession.SignedOut)
                    } else {
                        emit(AdminSession.SignedIn(email = user.email.orEmpty(), isAdmin = isAdmin(user.uid)))
                    }
                }
            }

    override suspend fun signInWithGoogle(idToken: String): SignInResult = try {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        SignInResult.SUCCESS
    } catch (e: CancellationException) {
        throw e
    } catch (_: FirebaseAuthInvalidCredentialsException) {
        SignInResult.INVALID_CREDENTIALS
    } catch (_: FirebaseAuthInvalidUserException) {
        SignInResult.INVALID_CREDENTIALS
    } catch (_: FirebaseNetworkException) {
        SignInResult.NETWORK_ERROR
    } catch (_: Exception) {
        SignInResult.UNKNOWN_ERROR
    }

    override suspend fun signOut() = auth.signOut()

    private fun currentUser(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Las reglas solo dejan a cada usuario leer su propio documento de admins. */
    private suspend fun isAdmin(uid: String): Boolean = try {
        firestore.collection(ADMINS_COLLECTION).document(uid).get().await().exists()
    } catch (e: CancellationException) {
        throw e
    } catch (_: FirebaseFirestoreException) {
        false
    }

    private companion object {
        const val ADMINS_COLLECTION = "admins"
    }
}
