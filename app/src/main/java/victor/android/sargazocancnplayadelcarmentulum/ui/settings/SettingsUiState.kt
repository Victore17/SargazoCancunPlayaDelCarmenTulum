package victor.android.sargazocancnplayadelcarmentulum.ui.settings

import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach

/**
 * Estado de la pantalla de ajustes.
 *
 * Los interruptores de [settings] y de [favorites] conservan su valor aunque el interruptor general
 * esté apagado; la pantalla solo los deshabilita ([areDetailSwitchesEnabled]).
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val favorites: List<Beach> = emptyList(),
) {
    /** Los interruptores individuales solo se pueden mover con el interruptor general encendido. */
    val areDetailSwitchesEnabled: Boolean get() = settings.allNotificationsEnabled
}
