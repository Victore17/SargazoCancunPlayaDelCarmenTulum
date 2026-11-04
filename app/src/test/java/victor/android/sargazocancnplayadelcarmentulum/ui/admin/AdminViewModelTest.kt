package victor.android.sargazocancnplayadelcarmentulum.ui.admin

import android.content.Context
import android.content.ContextWrapper
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachReading
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.AdminSession
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.GoogleTokenResult
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.ImageReadException
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SignInResult
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.SemaforoAnalysis
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeAdminAuthRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeGoogleIdTokenProvider
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSemaforoImageReader
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSemaforoRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.MainDispatcherRule
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AdminViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 4)
    private val semaforoDate = LocalDate.of(2026, 11, 3)
    private val clock = Clock.fixed(Instant.parse("2026-11-04T15:00:00Z"), ZoneOffset.UTC)

    // El proveedor falso no usa el contexto, así que basta con un Context simulado sin comportamiento.
    private val context: Context = ContextWrapper(null)
    private val auth = FakeAdminAuthRepository()
    private val googleProvider = FakeGoogleIdTokenProvider()
    private val beachRepository = FakeBeachRepository().apply {
        beaches.value = listOf(
            beach(1, "Punta Molas", GeoPoint(20.59, -86.73)),
            beach(2, "Tankah", GeoPoint(20.25, -87.39)),
            beach(3, "Akumal Media Luna", GeoPoint(20.40, -87.31)),
        )
    }
    private val semaforoRepository = FakeSemaforoRepository()
    private val imageReader = FakeSemaforoImageReader()

    private fun viewModel() = AdminViewModel(auth, googleProvider, beachRepository, semaforoRepository, imageReader, clock)

    private fun signedInAdmin(): AdminViewModel {
        auth.session.value = AdminSession.SignedIn("admin@example.com", isAdmin = true)
        return viewModel()
    }

    private val successAnalysis = SemaforoAnalysis.Success(
        date = semaforoDate,
        readings = listOf(
            BeachReading("Punta Molas", SargassumLevel.EXCESSIVE),
            BeachReading("Playa Inventada", SargassumLevel.LOW),
        ),
        newBeachNames = listOf("Playa Inventada"),
        unlabeledDotCount = 2,
    )

    // --- Inicio de sesión con Google ---

    @Test
    fun `sin sesion muestra el boton de Google`() {
        assertEquals(AdminSessionUi.SignedOut, viewModel().uiState.value.session)
    }

    @Test
    fun `iniciar sesion con Google correcta muestra la cuenta de admin`() {
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        val state = viewModel.uiState.value
        assertEquals(AdminSessionUi.Admin("admin@example.com"), state.session)
        assertFalse(state.isSigningIn)
        assertNull(state.message)
        assertEquals(listOf("token-de-google"), auth.signInTokens)
    }

    @Test
    fun `si Firebase rechaza el token muestra el error`() {
        auth.signInResult = SignInResult.INVALID_CREDENTIALS
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        assertEquals(AdminMessage.INVALID_CREDENTIALS, viewModel.uiState.value.message)
        assertEquals(AdminSessionUi.SignedOut, viewModel.uiState.value.session)
    }

    @Test
    fun `sin conexion al iniciar sesion muestra el error de red`() {
        auth.signInResult = SignInResult.NETWORK_ERROR
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        assertEquals(AdminMessage.NETWORK_ERROR, viewModel.uiState.value.message)
    }

    @Test
    fun `si la persona cierra el selector de cuentas no hay error ni sesion`() {
        googleProvider.result = GoogleTokenResult.Cancelled
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        assertNull(viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isSigningIn)
        assertTrue(auth.signInTokens.isEmpty())
    }

    @Test
    fun `sin cuenta de Google en el celular avisa`() {
        googleProvider.result = GoogleTokenResult.NoAccount
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        assertEquals(AdminMessage.NO_GOOGLE_ACCOUNT, viewModel.uiState.value.message)
        assertTrue(auth.signInTokens.isEmpty())
    }

    @Test
    fun `si Google no esta configurado en Firebase avisa sin cerrar la app`() {
        googleProvider.result = GoogleTokenResult.NotConfigured
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        assertEquals(AdminMessage.GOOGLE_NOT_CONFIGURED, viewModel.uiState.value.message)
        assertTrue(auth.signInTokens.isEmpty())
    }

    @Test
    fun `error de Google muestra el error generico`() {
        googleProvider.result = GoogleTokenResult.Error
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        assertEquals(AdminMessage.SIGN_IN_ERROR, viewModel.uiState.value.message)
    }

    @Test
    fun `cuenta de Google sin permiso de admin no puede publicar`() {
        auth.isAdminOnSignIn = false
        auth.emailOnSignIn = "otro@example.com"
        val viewModel = viewModel()

        viewModel.signInWithGoogle(context)

        assertEquals(AdminSessionUi.NotAdmin("otro@example.com"), viewModel.uiState.value.session)
    }

    @Test
    fun `cerrar sesion vuelve al boton de Google`() {
        val viewModel = signedInAdmin()

        viewModel.signOut()

        assertEquals(AdminSessionUi.SignedOut, viewModel.uiState.value.session)
        assertEquals(AdminStep.PickImage, viewModel.uiState.value.step)
    }

    // --- Foto y revisión ---

    @Test
    fun `foto valida pasa a la revision con detectadas primero y faltantes en sin dato`() = runTest {
        imageReader.result = successAnalysis
        val viewModel = signedInAdmin()

        viewModel.uiState.test {
            assertEquals(AdminStep.PickImage, awaitItem().step)
            viewModel.onImagePicked("content://foto")

            val review = expectMostRecentItem().step as AdminStep.Review
            assertEquals(semaforoDate, review.date)
            assertTrue(review.dateDetected)
            assertEquals(2, review.unlabeledDotCount)
            assertEquals(
                listOf(
                    ReviewRow("Punta Molas", SargassumLevel.EXCESSIVE, detected = true, hasLocation = true),
                    ReviewRow("Playa Inventada", SargassumLevel.LOW, detected = true, hasLocation = false),
                    ReviewRow("Akumal Media Luna", SargassumLevel.NO_DATA, detected = false, hasLocation = true),
                    ReviewRow("Tankah", SargassumLevel.NO_DATA, detected = false, hasLocation = true),
                ),
                review.rows,
            )
        }
        // Al lector se le pasan los nombres de las playas guardadas.
        assertEquals(listOf("Punta Molas", "Tankah", "Akumal Media Luna"), imageReader.lastKnownNames)
    }

    @Test
    fun `sin fecha en la imagen se usa la de hoy y se avisa`() {
        imageReader.result = successAnalysis.copy(date = null)
        val viewModel = signedInAdmin()

        viewModel.onImagePicked("content://foto")

        val review = viewModel.uiState.value.step as AdminStep.Review
        assertEquals(today, review.date)
        assertFalse(review.dateDetected)
    }

    @Test
    fun `imagen que no es semaforo muestra el error y vuelve a elegir foto`() {
        imageReader.result = SemaforoAnalysis.NotASemaforo
        val viewModel = signedInAdmin()

        viewModel.onImagePicked("content://foto")

        assertEquals(AdminStep.PickImage, viewModel.uiState.value.step)
        assertEquals(AdminMessage.NOT_A_SEMAFORO, viewModel.uiState.value.message)
    }

    @Test
    fun `imagen que no se puede leer muestra el error sin cerrar la app`() {
        imageReader.error = ImageReadException("dañada")
        val viewModel = signedInAdmin()

        viewModel.onImagePicked("content://foto")

        assertEquals(AdminStep.PickImage, viewModel.uiState.value.step)
        assertEquals(AdminMessage.IMAGE_READ_ERROR, viewModel.uiState.value.message)
    }

    @Test
    fun `se puede corregir el nivel y la fecha antes de publicar`() {
        imageReader.result = successAnalysis
        val viewModel = signedInAdmin()
        viewModel.onImagePicked("content://foto")

        viewModel.onLevelChange("Punta Molas", SargassumLevel.ABUNDANT)
        viewModel.onLevelChange("Tankah", SargassumLevel.LOW)
        viewModel.onDateChange(LocalDate.of(2026, 11, 2))

        val review = viewModel.uiState.value.step as AdminStep.Review
        val levels = review.rows.associate { it.name to it.level }
        assertEquals(SargassumLevel.ABUNDANT, levels["Punta Molas"])
        assertEquals(SargassumLevel.LOW, levels["Tankah"])
        assertEquals(LocalDate.of(2026, 11, 2), review.date)
    }

    // --- Publicar ---

    @Test
    fun `publicar sube solo las playas con nivel`() {
        imageReader.result = successAnalysis
        val viewModel = signedInAdmin()
        viewModel.onImagePicked("content://foto")
        viewModel.onLevelChange("Tankah", SargassumLevel.MODERATE)

        viewModel.publish()

        assertEquals(
            listOf(
                SemaforoReport(
                    date = semaforoDate,
                    readings = listOf(
                        BeachReading("Punta Molas", SargassumLevel.EXCESSIVE),
                        BeachReading("Playa Inventada", SargassumLevel.LOW),
                        BeachReading("Tankah", SargassumLevel.MODERATE),
                    ),
                ),
            ),
            semaforoRepository.published,
        )
        assertEquals(AdminStep.Published(semaforoDate, beachCount = 3), viewModel.uiState.value.step)
    }

    @Test
    fun `si falla la publicacion se queda en la revision con el error`() {
        imageReader.result = successAnalysis
        semaforoRepository.publishError = IOException("sin permiso")
        val viewModel = signedInAdmin()
        viewModel.onImagePicked("content://foto")

        viewModel.publish()

        val state = viewModel.uiState.value
        val review = state.step as AdminStep.Review
        assertFalse(review.isPublishing)
        assertEquals(AdminMessage.PUBLISH_ERROR, state.message)
    }

    @Test
    fun `sin ninguna playa con nivel no se publica`() {
        imageReader.result = successAnalysis.copy(readings = emptyList(), newBeachNames = emptyList())
        val viewModel = signedInAdmin()
        viewModel.onImagePicked("content://foto")

        viewModel.publish()

        assertTrue(semaforoRepository.published.isEmpty())
        assertEquals(AdminMessage.NOTHING_TO_PUBLISH, viewModel.uiState.value.message)
    }

    @Test
    fun `despues de publicar se puede subir otra foto`() {
        imageReader.result = successAnalysis
        val viewModel = signedInAdmin()
        viewModel.onImagePicked("content://foto")
        viewModel.publish()

        viewModel.startOver()

        assertEquals(AdminStep.PickImage, viewModel.uiState.value.step)
    }

    @Test
    fun `el mensaje se borra al mostrarse`() {
        imageReader.result = SemaforoAnalysis.NotASemaforo
        val viewModel = signedInAdmin()
        viewModel.onImagePicked("content://foto")

        viewModel.onMessageShown()

        assertNull(viewModel.uiState.value.message)
    }

    private fun beach(id: Long, name: String, location: GeoPoint?) = Beach(
        id = id,
        name = name,
        location = location,
        imagePosition = null,
        isFavorite = false,
        notificationsEnabled = true,
    )
}
