package victor.android.sargazocancnplayadelcarmentulum.data.local

import androidx.room.TypeConverter
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

/** Conversores de tipos para Room. */
class Converters {

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun levelToString(level: SargassumLevel?): String? = level?.name

    /** Un valor desconocido (por ejemplo, de una versión futura) se trata como "sin dato". */
    @TypeConverter
    fun stringToLevel(value: String?): SargassumLevel? = value?.let {
        SargassumLevel.entries.firstOrNull { level -> level.name == it } ?: SargassumLevel.NO_DATA
    }
}
