package victor.android.sargazocancnplayadelcarmentulum.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun localDate_roundTripsThroughEpochDay() {
        val date = LocalDate.of(2026, 7, 15)
        val stored = converters.localDateToEpochDay(date)
        assertEquals(date, converters.epochDayToLocalDate(stored))
    }

    @Test
    fun nullValues_stayNull() {
        assertNull(converters.localDateToEpochDay(null))
        assertNull(converters.epochDayToLocalDate(null))
        assertNull(converters.levelToString(null))
        assertNull(converters.stringToLevel(null))
    }

    @Test
    fun everyLevel_roundTripsThroughString() {
        SargassumLevel.entries.forEach { level ->
            assertEquals(level, converters.stringToLevel(converters.levelToString(level)))
        }
    }

    @Test
    fun unknownLevelString_becomesNoData() {
        assertEquals(SargassumLevel.NO_DATA, converters.stringToLevel("PURPLE"))
    }
}
