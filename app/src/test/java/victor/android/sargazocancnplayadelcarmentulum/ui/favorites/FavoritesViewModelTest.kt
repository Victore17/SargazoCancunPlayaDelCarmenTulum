package victor.android.sargazocancnplayadelcarmentulum.ui.favorites

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

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBeachRepository()

    private val statuses = listOf(
        beachStatus(1, "Tulum", SargassumLevel.EXCESSIVE, isFavorite = true),
        beachStatus(2, "Akumal", SargassumLevel.LOW),
        beachStatus(3, "Chemuyil", SargassumLevel.NO_DATA, isFavorite = true, date = null),
    )

    @Test
    fun `sin favoritas la lista esta vacia`() = runTest {
        val viewModel = FavoritesViewModel(repository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertTrue(state.favorites.isEmpty())
        }
    }

    @Test
    fun `muestra solo las favoritas, con su nivel, ordenadas por nombre`() = runTest {
        val viewModel = FavoritesViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            repository.statuses.value = statuses
            val state = awaitItem()

            assertEquals(listOf("Chemuyil", "Tulum"), state.favorites.map { it.beach.name })
            assertEquals(
                listOf(SargassumLevel.NO_DATA, SargassumLevel.EXCESSIVE),
                state.favorites.map { it.level },
            )
        }
    }

    @Test
    fun `quitar una favorita la saca de la pantalla`() = runTest {
        repository.beaches.value = statuses.map { it.beach }
        val viewModel = FavoritesViewModel(repository)

        viewModel.removeFavorite(statuses[0])

        assertFalse(repository.beaches.value.first { it.id == 1L }.isFavorite)
        assertTrue(repository.beaches.value.first { it.id == 3L }.isFavorite)
    }
}
