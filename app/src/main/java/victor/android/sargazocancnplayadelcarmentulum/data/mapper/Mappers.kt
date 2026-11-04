package victor.android.sargazocancnplayadelcarmentulum.data.mapper

import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachWithLevelEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.LevelRecordEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.SettingsEntity
import victor.android.sargazocancnplayadelcarmentulum.domain.model.AppSettings
import victor.android.sargazocancnplayadelcarmentulum.domain.model.Beach
import victor.android.sargazocancnplayadelcarmentulum.domain.model.BeachStatus
import victor.android.sargazocancnplayadelcarmentulum.domain.model.GeoPoint
import victor.android.sargazocancnplayadelcarmentulum.domain.model.ImagePosition
import victor.android.sargazocancnplayadelcarmentulum.domain.model.LevelRecord
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel

// Conversión entre entidades de Room y modelos de dominio.

fun BeachEntity.toDomain(): Beach = Beach(
    id = id,
    name = name,
    location = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
    imagePosition = if (imageX != null && imageY != null) ImagePosition(imageX, imageY) else null,
    isFavorite = isFavorite,
    notificationsEnabled = notificationsEnabled,
)

fun Beach.toEntity(): BeachEntity = BeachEntity(
    id = id,
    name = name,
    latitude = location?.latitude,
    longitude = location?.longitude,
    imageX = imagePosition?.x,
    imageY = imagePosition?.y,
    isFavorite = isFavorite,
    notificationsEnabled = notificationsEnabled,
)

/** Una playa que no aparece en el semáforo más reciente queda como [SargassumLevel.NO_DATA]. */
fun BeachWithLevelEntity.toDomain(): BeachStatus = BeachStatus(
    beach = beach.toDomain(),
    level = level ?: SargassumLevel.NO_DATA,
    date = levelDate,
)

fun LevelRecordEntity.toDomain(): LevelRecord = LevelRecord(
    id = id,
    beachId = beachId,
    level = level,
    date = date,
)

fun LevelRecord.toEntity(): LevelRecordEntity = LevelRecordEntity(
    id = id,
    beachId = beachId,
    level = level,
    date = date,
)

fun SettingsEntity.toDomain(): AppSettings = AppSettings(
    allNotificationsEnabled = allNotificationsEnabled,
    generalUpdateNotificationsEnabled = generalUpdateNotificationsEnabled,
    favoriteChangeNotificationsEnabled = favoriteChangeNotificationsEnabled,
)

fun AppSettings.toEntity(): SettingsEntity = SettingsEntity(
    allNotificationsEnabled = allNotificationsEnabled,
    generalUpdateNotificationsEnabled = generalUpdateNotificationsEnabled,
    favoriteChangeNotificationsEnabled = favoriteChangeNotificationsEnabled,
)
