package victor.android.sargazocancnplayadelcarmentulum.ui.map

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeBeachRepository
import victor.android.sargazocancnplayadelcarmentulum.testutil.MainDispatcherRule

class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBeachRepository()

    @Test
    fun uiState_countsOnlyBeachesWithLocation() = runTest {
        val viewModel = MapViewModel(repository)

        viewModel.uiState.test {
            assertEquals(MapUiState(isLoading = false, knownBeachCount = 0), awaitItem())

            repository.beaches.value = listOf(
                beach(1, GeoPoint(21.06, -86.78)),
                beach(2, GeoPoint(20.39, -87.31)),
                beach(3, location = null),
            )
            assertEquals(MapUiState(isLoading = false, knownBeachCount = 2), awaitItem())
        }
    }

    @Test
    fun initialState_isLoading() {
        assertEquals(true, MapViewModel(repository).uiState.value.isLoading)
    }

    private fun beach(id: Long, location: GeoPoint?) = Beach(
        id = id,
        name = "Playa $id",
        location = location,
        imagePosition = null,
        isFavorite = false,
        notificationsEnabled = true,
    )
}
