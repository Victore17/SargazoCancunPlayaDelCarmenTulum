package victor.android.sargazocancnplayadelcarmentulum.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel

/** Color de cada nivel, tomado de la leyenda del semáforo oficial. */
val SargassumLevel.color: Color
    get() = when (this) {
        SargassumLevel.NO_SARGASSUM -> Color(0xFF02B0F1)
        SargassumLevel.LOW -> Color(0xFF00AF50)
        SargassumLevel.MODERATE -> Color(0xFFFFE600)
        SargassumLevel.ABUNDANT -> Color(0xFFFDA800)
        SargassumLevel.EXCESSIVE -> Color(0xFFE5231A)
        SargassumLevel.NO_DATA -> Color(0xFF9E9E9E)
    }

/** Nombre visible del nivel. */
@get:StringRes
val SargassumLevel.labelRes: Int
    get() = when (this) {
        SargassumLevel.NO_SARGASSUM -> R.string.level_no_sargassum
        SargassumLevel.LOW -> R.string.level_low
        SargassumLevel.MODERATE -> R.string.level_moderate
        SargassumLevel.ABUNDANT -> R.string.level_abundant
        SargassumLevel.EXCESSIVE -> R.string.level_excessive
        SargassumLevel.NO_DATA -> R.string.level_no_data
    }
