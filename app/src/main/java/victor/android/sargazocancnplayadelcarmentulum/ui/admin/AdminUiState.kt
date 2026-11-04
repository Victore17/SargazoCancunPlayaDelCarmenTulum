package victor.android.sargazocancnplayadelcarmentulum.ui.admin

import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

/** Estado de la sesión tal como lo muestra la pantalla. */
sealed interface AdminSessionUi {
    data object Checking : AdminSessionUi
    data object SignedOut : AdminSessionUi
    data class NotAdmin(val email: String) : AdminSessionUi
    data class Admin(val email: String) : AdminSessionUi
}

/** Una playa en la revisión antes de publicar. */
data class ReviewRow(
    val name: String,
    val level: SargassumLevel,
    /** true si el nivel salió de la imagen; false si la playa no se encontró en ella. */
    val detected: Boolean,
    /** false para playas nuevas que no tienen coordenadas ("sin ubicación"). */
    val hasLocation: Boolean,
) {
    /** Playa nueva: se leyó un nombre que no está en el catálogo. */
    val isNewBeach: Boolean get() = detected && !hasLocation

    /** Playa del catálogo que no apareció en la imagen y todavía no tiene nivel. */
    val isMissing: Boolean get() = !detected && level == SargassumLevel.NO_DATA

    /** Requiere que el administrador la revise antes de publicar. */
    val needsReview: Boolean get() = isNewBeach || isMissing
}

/** Paso actual del flujo de publicación. */
sealed interface AdminStep {
    data object PickImage : AdminStep
    data object Processing : AdminStep

    data class Review(
        val imageUri: String,
        val date: LocalDate,
        /** false si no se pudo leer la fecha en la imagen y se usó la de hoy. */
        val dateDetected: Boolean,
        val rows: List<ReviewRow>,
        val unlabeledDotCount: Int,
        val isPublishing: Boolean = false,
    ) : AdminStep {
        val detectedCount: Int get() = rows.count { it.detected }
        val newBeaches: List<ReviewRow> get() = rows.filter { it.isNewBeach }
        val missingCount: Int get() = rows.count { it.isMissing }

        /** true si hay nombres nuevos, playas que no aparecieron o círculos sin nombre. */
        val needsAttention: Boolean
            get() = newBeaches.isNotEmpty() || missingCount > 0 || unlabeledDotCount > 0
    }

    data class Published(val date: LocalDate, val beachCount: Int) : AdminStep
}

/** Mensajes para el usuario (el texto está en strings.xml). */
enum class AdminMessage {
    INVALID_CREDENTIALS,
    NETWORK_ERROR,
    SIGN_IN_ERROR,
    GOOGLE_NOT_CONFIGURED,
    NO_GOOGLE_ACCOUNT,
    IMAGE_READ_ERROR,
    NOT_A_SEMAFORO,
    NOTHING_TO_PUBLISH,
    PUBLISH_ERROR,
}

data class AdminUiState(
    val session: AdminSessionUi = AdminSessionUi.Checking,
    val isSigningIn: Boolean = false,
    val step: AdminStep = AdminStep.PickImage,
    val message: AdminMessage? = null,
)
