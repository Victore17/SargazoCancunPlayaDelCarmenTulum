package victor.android.sargazocancnplayadelcarmentulum.domain.repository

import android.content.Context

/** Resultado de pedirle a Google un token de identidad. */
sealed interface GoogleTokenResult {
    data class Token(val idToken: String) : GoogleTokenResult

    /** La persona cerró el selector de cuentas. */
    data object Cancelled : GoogleTokenResult

    /** No hay ninguna cuenta de Google en el celular. */
    data object NoAccount : GoogleTokenResult

    /** Falta activar Google en Firebase (el google-services.json no trae el ID de cliente web). */
    data object NotConfigured : GoogleTokenResult

    data object Error : GoogleTokenResult
}

/** Abre el selector de cuentas de Google y devuelve el token de identidad. */
interface GoogleIdTokenProvider {

    /** [activityContext] debe ser la Activity: Credential Manager la necesita para mostrar el selector. */
    suspend fun requestIdToken(activityContext: Context): GoogleTokenResult
}
