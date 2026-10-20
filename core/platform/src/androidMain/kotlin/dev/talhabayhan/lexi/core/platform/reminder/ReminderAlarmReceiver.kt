package dev.talhabayhan.lexi.core.platform.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/** Posts the reminder when its alarm fires (the tap opens the launcher activity). */
public class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ReminderAlarms.EXTRA_ID, -1)
        val title = intent.getStringExtra(ReminderAlarms.EXTRA_TITLE)
        val body = intent.getStringExtra(ReminderAlarms.EXTRA_BODY)
        if (id < 0 || title == null || body == null) return
        ReminderNotifier.post(context, id, title, body)
    }
}

internal object ReminderNotifier {
    const val CHANNEL_ID: String = "reminders"

    fun ensureChannel(context: Context, name: String) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    /** Returns whether the notification was posted (false: notifications are off or the permission is missing). */
    fun post(context: Context, id: Int, title: String, body: String): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        ensureChannel(context, ReminderPlanStore(context).channelName)
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val tap = launch?.let {
            val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            PendingIntent.getActivity(context, id, it, flags)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon(context))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(tap)
            .build()
        @Suppress("MissingPermission") // areNotificationsEnabled() above covers POST_NOTIFICATIONS on API 33+
        manager.notify(id, notification)
        return true
    }

    /** `ic_stat_reminder` of the app (monochrome); the launcher icon if the app does not ship it. */
    private fun smallIcon(context: Context): Int {
        val res = context.resources.getIdentifier("ic_stat_reminder", "drawable", context.packageName)
        return if (res != 0) res else context.applicationInfo.icon
    }
}
