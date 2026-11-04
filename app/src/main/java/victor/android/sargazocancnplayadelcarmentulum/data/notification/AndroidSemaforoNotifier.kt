package victor.android.sargazocancnplayadelcarmentulum.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import victor.android.sargazocancnplayadelcarmentulum.MainActivity
import victor.android.sargazocancnplayadelcarmentulum.R
import victor.android.sargazocancnplayadelcarmentulum.domain.notification.FavoriteLevelChange
import victor.android.sargazocancnplayadelcarmentulum.domain.notification.SemaforoNotifier
import victor.android.sargazocancnplayadelcarmentulum.ui.theme.labelRes
import javax.inject.Inject

/** Notificaciones de Android: un canal para la actualización general y otro para las favoritas. */
class AndroidSemaforoNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : SemaforoNotifier {

    private val manager = NotificationManagerCompat.from(context)

    override fun notifyGeneralUpdate() {
        val notification = baseBuilder(GENERAL_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_general_title))
            .setContentText(context.getString(R.string.notification_general_text))
            .build()
        show(GENERAL_NOTIFICATION_ID, notification)
    }

    override fun notifyFavoriteChanges(changes: List<FavoriteLevelChange>) {
        if (changes.isEmpty()) return
        val builder = baseBuilder(FAVORITES_CHANNEL_ID)
        if (changes.size == 1) {
            val line = changeLine(changes.first())
            builder
                .setContentTitle(context.getString(R.string.notification_favorites_title))
                .setContentText(line)
                .setStyle(NotificationCompat.BigTextStyle().bigText(line))
        } else {
            val summary = context.resources.getQuantityString(
                R.plurals.notification_favorites_summary,
                changes.size,
                changes.size,
            )
            val inbox = NotificationCompat.InboxStyle()
            changes.forEach { inbox.addLine(changeLine(it)) }
            builder
                .setContentTitle(context.getString(R.string.notification_favorites_title))
                .setContentText(summary)
                .setStyle(inbox)
        }
        show(FAVORITES_NOTIFICATION_ID, builder.build())
    }

    private fun changeLine(change: FavoriteLevelChange): String = context.getString(
        if (change.hasMoreSargassum) R.string.notification_change_more else R.string.notification_change_less,
        change.beachName,
        context.getString(change.previous.labelRes),
        context.getString(change.current.labelRes),
    )

    private fun baseBuilder(channelId: String): NotificationCompat.Builder {
        ensureChannels()
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun show(id: Int, notification: android.app.Notification) {
        if (!canPostNotifications()) return
        manager.notify(id, notification)
    }

    /** En Android 13 o superior hace falta que el usuario haya aceptado el permiso. */
    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return manager.areNotificationsEnabled()
    }

    private fun ensureChannels() {
        manager.createNotificationChannel(
            channel(GENERAL_CHANNEL_ID, R.string.notification_channel_general, R.string.notification_channel_general_description),
        )
        manager.createNotificationChannel(
            channel(FAVORITES_CHANNEL_ID, R.string.notification_channel_favorites, R.string.notification_channel_favorites_description),
        )
    }

    private fun channel(id: String, nameRes: Int, descriptionRes: Int): NotificationChannelCompat =
        NotificationChannelCompat.Builder(id, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(nameRes))
            .setDescription(context.getString(descriptionRes))
            .build()

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val GENERAL_CHANNEL_ID = "semaforo_general"
        const val FAVORITES_CHANNEL_ID = "semaforo_favorites"
        const val GENERAL_NOTIFICATION_ID = 1
        const val FAVORITES_NOTIFICATION_ID = 2
    }
}
