package victor.android.sargazocancnplayadelcarmentulum.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel

/** Color de cada nivel, igual al del semáforo oficial. */
val SargassumLevel.color: Color
    get() = when (this) {
        SargassumLevel.LOW -> Color(0xFF2E9E44)
        SargassumLevel.MODERATE -> Color(0xFFF2C200)
        SargassumLevel.HIGH -> Color(0xFFF07C00)
        SargassumLevel.VERY_HIGH -> Color(0xFFD32F2F)
        SargassumLevel.NO_DATA -> Color(0xFF1E6FD9)
    }

/** Nombre visible del nivel. */
@get:StringRes
val SargassumLevel.labelRes: Int
    get() = when (this) {
        SargassumLevel.LOW -> R.string.level_low
        SargassumLevel.MODERATE -> R.string.level_moderate
        SargassumLevel.HIGH -> R.string.level_high
        SargassumLevel.VERY_HIGH -> R.string.level_very_high
        SargassumLevel.NO_DATA -> R.string.level_no_data
    }
