package victor.android.sargazocancnplayadelcarmentulum.ui.detail

import androidx.annotation.StringRes
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.model.CompassDirection
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SeaState
import victor.android.sargazocancnplayadelcarmentulum.domain.model.WeatherCondition

// Textos (strings.xml) de los modelos de clima y mar.

@get:StringRes
val WeatherCondition.labelRes: Int
    get() = when (this) {
        WeatherCondition.CLEAR -> R.string.weather_clear
        WeatherCondition.MAINLY_CLEAR -> R.string.weather_mainly_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
        WeatherCondition.OVERCAST -> R.string.weather_overcast
        WeatherCondition.FOG -> R.string.weather_fog
        WeatherCondition.DRIZZLE -> R.string.weather_drizzle
        WeatherCondition.RAIN -> R.string.weather_rain
        WeatherCondition.RAIN_SHOWERS -> R.string.weather_rain_showers
        WeatherCondition.THUNDERSTORM -> R.string.weather_thunderstorm
        WeatherCondition.UNKNOWN -> R.string.weather_unknown
    }

@get:StringRes
val SeaState.labelRes: Int
    get() = when (this) {
        SeaState.CALM -> R.string.sea_state_calm
        SeaState.SLIGHT -> R.string.sea_state_slight
        SeaState.MODERATE -> R.string.sea_state_moderate
        SeaState.ROUGH -> R.string.sea_state_rough
    }

@get:StringRes
val CompassDirection.labelRes: Int
    get() = when (this) {
        CompassDirection.N -> R.string.compass_n
        CompassDirection.NE -> R.string.compass_ne
        CompassDirection.E -> R.string.compass_e
        CompassDirection.SE -> R.string.compass_se
        CompassDirection.S -> R.string.compass_s
        CompassDirection.SW -> R.string.compass_sw
        CompassDirection.W -> R.string.compass_w
        CompassDirection.NW -> R.string.compass_nw
    }
