package victor.android.sargazocancnplayadelcarmentulum.ui.detail

import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachConditions
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

/** Estado de la consulta del clima y el mar. */
sealed interface ConditionsUi {
    data object Loading : ConditionsUi

    /** La playa no tiene coordenadas: no se puede consultar el clima. */
    data object NoLocation : ConditionsUi

    data class Loaded(val conditions: BeachConditions) : ConditionsUi

    /** No hubo conexión o el servicio no respondió. */
    data object Error : ConditionsUi
}

/**
 * Estado de la pantalla de detalle.
 *
 * @property beach null mientras carga o si la playa no existe ([isLoading] distingue los dos casos).
 * @property currentLevel nivel del registro más reciente de la playa; [SargassumLevel.NO_DATA] si no tiene.
 * @property updatedOn fecha de ese registro, o null si la playa no tiene ninguno.
 * @property history registros de los últimos dos meses, del más reciente al más antiguo.
 */
data class BeachDetailUiState(
    val isLoading: Boolean = true,
    val beach: Beach? = null,
    val currentLevel: SargassumLevel = SargassumLevel.NO_DATA,
    val updatedOn: LocalDate? = null,
    val history: List<LevelRecord> = emptyList(),
    val conditions: ConditionsUi = ConditionsUi.Loading,
) {
    val isFavorite: Boolean get() = beach?.isFavorite == true
    val notFound: Boolean get() = !isLoading && beach == null
}
