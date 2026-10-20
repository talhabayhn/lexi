package dev.talhabayhan.lexi.core.platform.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Re-arms the stored reminder plan after a reboot, a clock or time zone change, or an app update. */
public class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> AndroidReminderScheduler.rearmStoredPlan(context)
        }
    }
}
