package victor.android.sargazocancnplayadelcarmentulum.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.testutil.FakeSettingsDao

class SettingsRepositoryImplTest {

    private val settingsDao = FakeSettingsDao()
    private val repository = SettingsRepositoryImpl(settingsDao)

    @Test
    fun observeSettings_withoutRow_emitsDefaults() = runTest {
        repository.observeSettings().test {
            assertEquals(AppSettings(), awaitItem())
        }
    }

    @Test
    fun updateSettings_masterSwitchOff_keepsIndividualSwitches() = runTest {
        repository.updateSettings { it.copy(favoriteChangeNotificationsEnabled = false) }
        repository.updateSettings { it.copy(allNotificationsEnabled = false) }

        repository.observeSettings().test {
            assertEquals(
                AppSettings(
                    allNotificationsEnabled = false,
                    generalUpdateNotificationsEnabled = true,
                    favoriteChangeNotificationsEnabled = false,
                ),
                awaitItem(),
            )

            repository.updateSettings { it.copy(allNotificationsEnabled = true) }
            assertEquals(
                AppSettings(
                    allNotificationsEnabled = true,
                    generalUpdateNotificationsEnabled = true,
                    favoriteChangeNotificationsEnabled = false,
                ),
                awaitItem(),
            )
        }
    }
}
