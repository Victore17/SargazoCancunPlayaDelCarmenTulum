package victor.android.sargazocancnplayadelcarmentulum.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.ImagePosition

class MappersTest {

    @Test
    fun beachEntity_withCoordinates_mapsLocationAndImagePosition() {
        val entity = BeachEntity(
            id = 3, name = "Playa Delfines",
            latitude = 21.06, longitude = -86.78,
            imageX = 0.25f, imageY = 0.75f,
            isFavorite = true, notificationsEnabled = false,
        )

        val beach = entity.toDomain()

        assertEquals(GeoPoint(21.06, -86.78), beach.location)
        assertEquals(ImagePosition(0.25f, 0.75f), beach.imagePosition)
        assertEquals(entity, beach.toEntity())
    }

    @Test
    fun beachEntity_withoutCoordinates_hasNoLocation() {
        val beach = BeachEntity(name = "Playa Nueva", latitude = 21.0, longitude = null).toDomain()

        assertNull(beach.location)
        assertNull(beach.imagePosition)
    }

    @Test
    fun settings_roundTrip() {
        val settings = AppSettings(
            allNotificationsEnabled = false,
            generalUpdateNotificationsEnabled = true,
            favoriteChangeNotificationsEnabled = false,
        )
        assertEquals(settings, settings.toEntity().toDomain())
    }
}
