package victor.android.sargazocancnplayadelcarmentulum.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachReading
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminSession
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.BeachRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleIdTokenProvider
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleTokenResult
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoImageReader
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoRepository
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SignInResult
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.SemaforoAnalysis
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Pantalla oculta de administración: iniciar sesión con Google, elegir la foto del semáforo,
 * revisar los niveles detectados y publicarlos en Firestore.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authRepository: AdminAuthRepository,
    private val googleIdTokenProvider: GoogleIdTokenProvider,
    private val beachRepository: BeachRepository,
    private val semaforoRepository: SemaforoRepository,
    private val imageReader: SemaforoImageReader,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _uiState.update { it.copy(session = session.toUi()) }
            }
        }
    }

    /**
     * Abre el selector de cuentas de Google y, con el token que entrega, inicia sesión en Firebase.
     * [activityContext] solo se usa durante esta llamada (no se guarda).
     */
    fun signInWithGoogle(activityContext: Context) {
        if (_uiState.value.isSigningIn) return
        _uiState.update { it.copy(isSigningIn = true, message = null) }
        viewModelScope.launch {
            val message = when (val token = googleIdTokenProvider.requestIdToken(activityContext)) {
                is GoogleTokenResult.Token -> when (authRepository.signInWithGoogle(token.idToken)) {
                    SignInResult.SUCCESS -> null
                    SignInResult.INVALID_CREDENTIALS -> AdminMessage.INVALID_CREDENTIALS
                    SignInResult.NETWORK_ERROR -> AdminMessage.NETWORK_ERROR
                    SignInResult.UNKNOWN_ERROR -> AdminMessage.SIGN_IN_ERROR
                }
                // Si cierra el selector no es un error: no se muestra nada.
                GoogleTokenResult.Cancelled -> null
                GoogleTokenResult.NoAccount -> AdminMessage.NO_GOOGLE_ACCOUNT
                GoogleTokenResult.NotConfigured -> AdminMessage.GOOGLE_NOT_CONFIGURED
                GoogleTokenResult.Error -> AdminMessage.SIGN_IN_ERROR
            }
            _uiState.update { it.copy(isSigningIn = false, message = message) }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
        _uiState.update { AdminUiState(session = it.session) }
    }

    /** Se eligió una foto: se analiza y se pasa a la revisión. */
    fun onImagePicked(imageUri: String) {
        if (_uiState.value.step is AdminStep.Processing) return
        _uiState.update { it.copy(step = AdminStep.Processing, message = null) }
        viewModelScope.launch {
            val beaches = beachRepository.observeBeaches().first()
            val analysis = try {
                imageReader.read(imageUri, beaches.map { it.name })
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // ImageReadException u otro error inesperado al leer la imagen: no se cierra la app.
                showError(AdminMessage.IMAGE_READ_ERROR)
                return@launch
            }

            when (analysis) {
                SemaforoAnalysis.NotASemaforo -> showError(AdminMessage.NOT_A_SEMAFORO)
                is SemaforoAnalysis.Success -> {
                    val locatedNames = beaches.filter { it.location != null }.mapTo(HashSet()) { it.name }
                    _uiState.update {
                        it.copy(
                            step = AdminStep.Review(
                                imageUri = imageUri,
                                date = analysis.date ?: LocalDate.now(clock),
                                dateDetected = analysis.date != null,
                                rows = buildReviewRows(analysis, beaches.map { b -> b.name }, locatedNames),
                                unlabeledDotCount = analysis.unlabeledDotCount,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun onLevelChange(beachName: String, level: SargassumLevel) = updateReview { review ->
        review.copy(rows = review.rows.map { if (it.name == beachName) it.copy(level = level) else it })
    }

    fun onDateChange(date: LocalDate) = updateReview { it.copy(date = date, dateDetected = true) }

    fun publish() {
        val review = _uiState.value.step as? AdminStep.Review ?: return
        if (review.isPublishing) return
        val readings = review.rows
            .filter { it.level != SargassumLevel.NO_DATA }
            .map { BeachReading(beachName = it.name, level = it.level) }
        if (readings.isEmpty()) {
            _uiState.update { it.copy(message = AdminMessage.NOTHING_TO_PUBLISH) }
            return
        }

        updateReview { it.copy(isPublishing = true) }
        _uiState.update { it.copy(message = null) }
        viewModelScope.launch {
            try {
                withTimeout(PUBLISH_TIMEOUT_MILLIS) {
                    semaforoRepository.publish(SemaforoReport(date = review.date, readings = readings))
                }
                _uiState.update {
                    it.copy(step = AdminStep.Published(date = review.date, beachCount = readings.size))
                }
            } catch (e: TimeoutCancellationException) {
                publishFailed()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                publishFailed()
            }
        }
    }

    /** Vuelve a empezar para subir otra foto. */
    fun startOver() = _uiState.update { it.copy(step = AdminStep.PickImage, message = null) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    private fun publishFailed() {
        updateReview { it.copy(isPublishing = false) }
        _uiState.update { it.copy(message = AdminMessage.PUBLISH_ERROR) }
    }

    private fun showError(message: AdminMessage) =
        _uiState.update { it.copy(step = AdminStep.PickImage, message = message) }

    private fun updateReview(transform: (AdminStep.Review) -> AdminStep.Review) = _uiState.update { state ->
        val review = state.step as? AdminStep.Review ?: return@update state
        state.copy(step = transform(review))
    }

    private fun AdminSession.toUi(): AdminSessionUi = when (this) {
        AdminSession.SignedOut -> AdminSessionUi.SignedOut
        is AdminSession.SignedIn -> if (isAdmin) AdminSessionUi.Admin(email) else AdminSessionUi.NotAdmin(email)
    }

    companion object {
        private const val PUBLISH_TIMEOUT_MILLIS = 20_000L

        /**
         * Filas de la revisión: primero las playas detectadas en la imagen (en su orden),
         * después las del catálogo que no se encontraron, con "Sin dato" para que el
         * administrador pueda ponerles nivel a mano.
         */
        fun buildReviewRows(
            analysis: SemaforoAnalysis.Success,
            knownNames: List<String>,
            locatedNames: Set<String>,
        ): List<ReviewRow> {
            val detected = analysis.readings.map { reading ->
                ReviewRow(
                    name = reading.beachName,
                    level = reading.level,
                    detected = true,
                    hasLocation = reading.beachName in locatedNames,
                )
            }
            val detectedNames = detected.mapTo(HashSet()) { it.name }
            val missing = knownNames
                .filter { it !in detectedNames }
                .sorted()
                .map { name ->
                    ReviewRow(name = name, level = SargassumLevel.NO_DATA, detected = false, hasLocation = name in locatedNames)
                }
            return detected + missing
        }
    }
}
