package victor.android.sargazocancnplayadelcarmentulum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Playa (tabla "beaches").
 *
 * Latitud y longitud son opcionales: una playa nueva leída del semáforo puede no tener
 * coordenadas conocidas. La posición en la imagen se guarda en coordenadas relativas (0.0 a 1.0).
 */
@Entity(
    tableName = "beaches",
    indices = [Index(value = ["name"], unique = true)],
)
data class BeachEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "image_x")
    val imageX: Float? = null,
    @ColumnInfo(name = "image_y")
    val imageY: Float? = null,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean = true,
)
