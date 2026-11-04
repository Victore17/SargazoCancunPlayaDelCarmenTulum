package victor.android.sargazocancnplayadelcarmentulum.ui.list

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.MainDispatcherRule
import victor.android.sargazocancnplayadelcarmentulum.testutil.beachStatus

class BeachListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBeachRepository()

    private fun viewModel() = BeachListViewModel(repository)

    private val statuses = listOf(
        beachStatus(1, "Tulum", SargassumLevel.EXCESSIVE),
        beachStatus(2, "Akumal", SargassumLevel.LOW),
        beachStatus(3, "Xcacel", SargassumLevel.LOW),
        beachStatus(4, "Sin semaforo", SargassumLevel.NO_DATA, date = null),
    )

    @Test
    fun `sin semaforo la lista esta vacia y lo indica`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertFalse(state.hasSemaforo)
            assertTrue(state.beaches.isEmpty())
        }
    }

    @Test
    fun `muestra solo las playas del semaforo ordenadas por nombre`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            repository.statuses.value = statuses
            val state = awaitItem()

            assertTrue(state.hasSemaforo)
            assertEquals(listOf("Akumal", "Tulum", "Xcacel"), state.beaches.map { it.beach.name })
        }
    }

    @Test
    fun `el buscador filtra por nombre`() = runTest {
        val viewModel = viewModel()
        repository.statuses.value = statuses

        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("xca")
            val state = expectMostRecentItem()

            assertEquals("xca", state.query)
            assertEquals(listOf("Xcacel"), state.beaches.map { it.beach.name })
            assertTrue(state.hasActiveFilters)
        }
    }

    @Test
    fun `el filtro por color marca y desmarca niveles`() = runTest {
        val viewModel = viewModel()
        repository.statuses.value = statuses

        viewModel.uiState.test {
            awaitItem()
            viewModel.toggleLevel(SargassumLevel.LOW)
            val filtered = expectMostRecentItem()
            assertEquals(setOf(SargassumLevel.LOW), filtered.selectedLevels)
            assertEquals(listOf("Akumal", "Xcacel"), filtered.beaches.map { it.beach.name })

            viewModel.toggleLevel(SargassumLevel.LOW)
            val all = expectMostRecentItem()
            assertTrue(all.selectedLevels.isEmpty())
            assertEquals(3, all.beaches.size)
        }
    }

    @Test
    fun `quitar filtros restaura la lista completa`() = runTest {
        val viewModel = viewModel()
        repository.statuses.value = statuses

        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("tulum")
            viewModel.toggleLevel(SargassumLevel.LOW)
            assertTrue(expectMostRecentItem().beaches.isEmpty())

            viewModel.clearFilters()
            val state = expectMostRecentItem()
            assertFalse(state.hasActiveFilters)
            assertEquals(3, state.beaches.size)
        }
    }

    @Test
    fun `toggleFavorite agrega y quita de favoritas`() = runTest {
        repository.beaches.value = statuses.map { it.beach }
        repository.statuses.value = statuses
        val viewModel = viewModel()

        viewModel.toggleFavorite(statuses[0])
        assertTrue(repository.beaches.value.first { it.id == 1L }.isFavorite)

        viewModel.toggleFavorite(statuses[0].copy(beach = statuses[0].beach.copy(isFavorite = true)))
        assertFalse(repository.beaches.value.first { it.id == 1L }.isFavorite)
    }
}
