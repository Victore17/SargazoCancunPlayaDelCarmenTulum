package victor.android.sargazocancnplayadelcarmentulum.testutil

import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import java.time.LocalDate

/** Crea un [BeachStatus] de prueba. */
fun beachStatus(
    id: Long,
    name: String = "Playa $id",
    level: SargassumLevel = SargassumLevel.LOW,
    isFavorite: Boolean = false,
    location: GeoPoint? = GeoPoint(20.0, -87.0),
    date: LocalDate? = LocalDate.of(2026, 11, 3),
) = BeachStatus(
    beach = Beach(
        id = id,
        name = name,
        location = location,
        imagePosition = null,
        isFavorite = isFavorite,
        notificationsEnabled = true,
    ),
    level = level,
    date = date,
)
