package victor.android.sargazocancnplayadelcarmentulum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Ajustes de la app (tabla "settings"). Siempre hay una sola fila, con id [SINGLE_ROW_ID]. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID,
    @ColumnInfo(name = "all_notifications_enabled")
    val allNotificationsEnabled: Boolean,
    @ColumnInfo(name = "general_update_notifications_enabled")
    val generalUpdateNotificationsEnabled: Boolean,
    @ColumnInfo(name = "favorite_change_notifications_enabled")
    val favoriteChangeNotificationsEnabled: Boolean,
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
