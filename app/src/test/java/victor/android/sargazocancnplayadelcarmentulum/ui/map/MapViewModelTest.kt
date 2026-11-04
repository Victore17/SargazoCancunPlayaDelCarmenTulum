package victor.android.sargazocancnplayadelcarmentulum.ui.map

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSemaforoRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.MainDispatcherRule
import java.io.IOException
import java.time.LocalDate

class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val beachRepository = FakeBeachRepository()
    private val semaforoRepository = FakeSemaforoRepository()
    private val today = LocalDate.of(2026, 11, 3)

    private fun viewModel() = MapViewModel(beachRepository, semaforoRepository)

    @Test
    fun `al abrir el mapa descarga los semaforos`() {
        viewModel()

        assertEquals(1, semaforoRepository.refreshCount)
    }

    @Test
    fun `el mapa muestra cada playa con ubicacion y su nivel`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(MapUiState(isLoading = false), awaitItem())

            beachRepository.statuses.value = listOf(
                status(1, GeoPoint(20.59, -86.73), SargassumLevel.EXCESSIVE, today),
                status(2, GeoPoint(20.25, -87.39), SargassumLevel.NO_DATA, null),
                status(3, location = null, level = SargassumLevel.LOW, date = today),
            )
            val state = awaitItem()
            assertEquals(
                listOf(
                    MapBeach(1, "Playa 1", 20.59, -86.73, SargassumLevel.EXCESSIVE),
                    MapBeach(2, "Playa 2", 20.25, -87.39, SargassumLevel.NO_DATA),
                ),
                state.beaches,
            )
            assertEquals(today, state.latestDate)
            assertEquals(1, state.withoutLocationCount)
        }
    }

    @Test
    fun `sin semaforos no hay fecha`() {
        val state = MapViewModel.buildState(
            listOf(status(1, GeoPoint(20.0, -87.0), SargassumLevel.NO_DATA, null)),
        )

        assertNull(state.latestDate)
        assertEquals(0, state.withoutLocationCount)
    }

    @Test
    fun `si falla la descarga se avisa y se puede reintentar`() = runTest {
        semaforoRepository.refreshError = IOException("sin conexión")
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem().showSyncError)

            semaforoRepository.refreshError = null
            viewModel.refresh()
            assertFalse(expectMostRecentItem().showSyncError)
        }
        assertEquals(2, semaforoRepository.refreshCount)
    }

    @Test
    fun `mientras descarga muestra actualizando y no repite la descarga`() = runTest {
        val gate = CompletableDeferred<Unit>()
        semaforoRepository.refreshGate = gate
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem().isRefreshing)
            viewModel.refresh()
            assertEquals(1, semaforoRepository.refreshCount)

            gate.complete(Unit)
            assertFalse(awaitItem().isRefreshing)
        }
    }

    @Test
    fun `estado inicial esta cargando`() {
        assertTrue(viewModel().uiState.value.isLoading)
    }

    private fun status(id: Long, location: GeoPoint?, level: SargassumLevel, date: LocalDate?) = BeachStatus(
        beach = Beach(
            id = id,
            name = "Playa $id",
            location = location,
            imagePosition = null,
            isFavorite = false,
            notificationsEnabled = true,
        ),
        level = level,
        date = date,
    )
}
