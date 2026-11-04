package victor.android.sargazocancnplayadelcarmentulum.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleIdTokenProvider
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleTokenResult
import javax.inject.Inject

/**
 * Pide el token de Google con Credential Manager.
 *
 * El ID de cliente web lo genera el plugin google-services en el recurso `default_web_client_id`
 * a partir de google-services.json. Si Google no está activado en Firebase, ese recurso no existe
 * y se devuelve [GoogleTokenResult.NotConfigured] en lugar de cerrar la app.
 */
class CredentialManagerGoogleIdTokenProvider @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
) : GoogleIdTokenProvider {

    override suspend fun requestIdToken(activityContext: Context): GoogleTokenResult {
        val webClientId = webClientId() ?: return GoogleTokenResult.NotConfigured
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    // false: muestra todas las cuentas del celular, no solo las usadas antes.
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build(),
            )
            .build()
        return try {
            val credential = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
                .credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleTokenResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                GoogleTokenResult.Error
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: GetCredentialCancellationException) {
            GoogleTokenResult.Cancelled
        } catch (_: NoCredentialException) {
            GoogleTokenResult.NoAccount
        } catch (_: GetCredentialException) {
            GoogleTokenResult.Error
        } catch (_: GoogleIdTokenParsingException) {
            GoogleTokenResult.Error
        }
    }

    /** Se busca por nombre para que el proyecto compile aunque el recurso todavía no exista. */
    private fun webClientId(): String? {
        val id = appContext.resources.getIdentifier("default_web_client_id", "string", appContext.packageName)
        return if (id == 0) null else appContext.getString(id).takeIf { it.isNotBlank() }
    }
}
