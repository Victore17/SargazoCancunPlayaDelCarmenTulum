package victor.android.sargazocancnplayadelcarmentulum.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AirConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.WeatherCondition
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeWeatherRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.MainDispatcherRule
import java.io.IOException
import java.time.LocalDate

class BeachDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val beachRepository = FakeBeachRepository()
    private val weatherRepository = FakeWeatherRepository()

    private val punta = beach(1, "Punta Molas", GeoPoint(20.59, -86.72))
    private val conditions = BeachConditions(
        air = AirConditions(26.3, 30.2, 78, WeatherCondition.CLEAR, 6.7, 106.0),
        sea = null,
        observedAt = null,
    )

    private fun viewModel(beachId: Long = 1) = BeachDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(BeachDetailViewModel.BEACH_ID_KEY to beachId)),
        beachRepository = beachRepository,
        weatherRepository = weatherRepository,
    )

    private fun setUp(vararg beaches: Beach, history: List<LevelRecord> = emptyList()) {
        beachRepository.beaches.value = beaches.toList()
        beachRepository.histories.value = mapOf(1L to history)
        weatherRepository.conditions = conditions
    }

    @Test
    fun `nivel actual y fecha salen del registro mas reciente`() = runTest {
        setUp(
            punta,
            history = listOf(
                record(1, SargassumLevel.LOW, LocalDate.of(2026, 11, 1)),
                record(2, SargassumLevel.EXCESSIVE, LocalDate.of(2026, 11, 3)),
                record(3, SargassumLevel.MODERATE, LocalDate.of(2026, 11, 2)),
            ),
        )

        viewModel().uiState.test {
            val state = expectMostRecentItem()
            assertEquals(SargassumLevel.EXCESSIVE, state.currentLevel)
            assertEquals(LocalDate.of(2026, 11, 3), state.updatedOn)
            // El historial va del más reciente al más antiguo.
            assertEquals(
                listOf(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 1)),
                state.history.map { it.date },
            )
        }
    }

    @Test
    fun `playa sin registros queda en sin dato y sin fecha`() = runTest {
        setUp(punta)

        viewModel().uiState.test {
            val state = expectMostRecentItem()
            assertEquals(SargassumLevel.NO_DATA, state.currentLevel)
            assertNull(state.updatedOn)
            assertTrue(state.history.isEmpty())
            assertFalse(state.notFound)
        }
    }

    @Test
    fun `playa que no existe se marca como no encontrada`() = runTest {
        setUp(punta)

        viewModel(beachId = 99).uiState.test {
            assertTrue(expectMostRecentItem().notFound)
        }
    }

    @Test
    fun `argumento de ruta faltante tambien es no encontrada`() = runTest {
        setUp(punta)
        val viewModel = BeachDetailViewModel(SavedStateHandle(), beachRepository, weatherRepository)

        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().notFound)
        }
    }

    @Test
    fun `consulta el clima en la ubicacion de la playa`() = runTest {
        setUp(punta)

        viewModel().uiState.test {
            assertEquals(ConditionsUi.Loaded(conditions), expectMostRecentItem().conditions)
        }
        assertEquals(listOf(GeoPoint(20.59, -86.72)), weatherRepository.requests)
    }

    @Test
    fun `mientras consulta muestra cargando`() = runTest {
        setUp(punta)
        val gate = CompletableDeferred<Unit>()
        weatherRepository.gate = gate

        viewModel().uiState.test {
            assertEquals(ConditionsUi.Loading, expectMostRecentItem().conditions)

            gate.complete(Unit)
            assertEquals(ConditionsUi.Loaded(conditions), awaitItem().conditions)
        }
    }

    @Test
    fun `playa sin ubicacion no consulta el clima`() = runTest {
        setUp(beach(1, "Playa Nueva", location = null))

        viewModel().uiState.test {
            assertEquals(ConditionsUi.NoLocation, expectMostRecentItem().conditions)
        }
        assertTrue(weatherRepository.requests.isEmpty())
    }

    @Test
    fun `si falla la red muestra error y no se cierra`() = runTest {
        setUp(punta)
        weatherRepository.error = IOException("sin internet")

        viewModel().uiState.test {
            assertEquals(ConditionsUi.Error, expectMostRecentItem().conditions)
        }
    }

    @Test
    fun `reintentar vuelve a consultar y muestra el clima`() = runTest {
        setUp(punta)
        weatherRepository.error = IOException("sin internet")
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ConditionsUi.Error, expectMostRecentItem().conditions)

            weatherRepository.error = null
            viewModel.loadConditions()

            assertEquals(ConditionsUi.Loaded(conditions), expectMostRecentItem().conditions)
        }
        assertEquals(2, weatherRepository.requests.size)
    }

    @Test
    fun `el boton de favorita agrega y quita la playa`() = runTest {
        setUp(punta)
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isFavorite)

            viewModel.toggleFavorite()
            assertTrue(expectMostRecentItem().isFavorite)

            viewModel.toggleFavorite()
            assertFalse(expectMostRecentItem().isFavorite)
        }
    }

    @Test
    fun `el estado inicial esta cargando`() {
        setUp(punta)

        assertTrue(viewModel().uiState.value.isLoading)
    }

    private fun beach(id: Long, name: String, location: GeoPoint?) = Beach(
        id = id,
        name = name,
        location = location,
        imagePosition = null,
        isFavorite = false,
        notificationsEnabled = true,
    )

    private fun record(id: Long, level: SargassumLevel, date: LocalDate) =
        LevelRecord(id = id, beachId = 1, level = level, date = date)
}
