package victor.android.sargazocancnplayadelcarmentulum.domain.repository

import kotlinx.coroutines.flow.Flow

/** Estado de la sesión en la pantalla oculta de administración. */
sealed interface AdminSession {
    data object SignedOut : AdminSession

    /** [isAdmin] es true si la cuenta tiene permiso para publicar (documento admins/{uid} en Firestore). */
    data class SignedIn(val email: String, val isAdmin: Boolean) : AdminSession
}

/** Resultado de un intento de inicio de sesión. */
enum class SignInResult { SUCCESS, INVALID_CREDENTIALS, NETWORK_ERROR, UNKNOWN_ERROR }

/** Inicio de sesión del administrador con Firebase Authentication (cuenta de Google). */
interface AdminAuthRepository {

    fun observeSession(): Flow<AdminSession>

    /** Inicia sesión en Firebase con el token de identidad que entregó Google. */
    suspend fun signInWithGoogle(idToken: String): SignInResult

    suspend fun signOut()
}
