package dev.talhabayhan.lexi.core.platform.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.talhabayhan.lexi.core.domain.reminder.ReminderPlanner
import dev.talhabayhan.lexi.core.domain.reminder.ScheduledReminder
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

/**
 * Plan persisted as local date + minute + texts (ADR 0021), so the alarms can be re-armed after a reboot, a clock or
 * time zone change and an app update without running the planner.
 */
internal class ReminderPlanStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(): List<ScheduledReminder> {
        val raw = prefs.getString(KEY_PLAN, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                ScheduledReminder(
                    id = o.getInt("id"),
                    epochDay = o.getLong("day"),
                    minuteOfDay = o.getInt("minute"),
                    title = o.getString("title"),
                    body = o.getString("body"),
                )
            }
        } catch (_: org.json.JSONException) {
            emptyList()
        }
    }

    fun save(items: List<ScheduledReminder>) {
        val array = JSONArray()
        for (item in items) {
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("day", item.epochDay)
                    .put("minute", item.minuteOfDay)
                    .put("title", item.title)
                    .put("body", item.body),
            )
        }
        prefs.edit().putString(KEY_PLAN, array.toString()).apply()
    }

    var channelName: String
        get() = prefs.getString(KEY_CHANNEL, null) ?: DEFAULT_CHANNEL_NAME
        set(value) {
            prefs.edit().putString(KEY_CHANNEL, value).apply()
        }

    private companion object {
        const val PREFS = "lexi_reminders"
        const val KEY_PLAN = "plan"
        const val KEY_CHANNEL = "channel_name"
    }
}

internal const val DEFAULT_CHANNEL_NAME = "Reminders"

/** AlarmManager side of the reminders: inexact `setAndAllowWhileIdle`, no exact-alarm permission needed. */
internal object ReminderAlarms {
    /** Wall-clock instant of [item] in the current time zone. */
    fun triggerAtMs(item: ScheduledReminder, zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.ofEpochDay(item.epochDay)
            .atTime(item.minuteOfDay / MINUTES_PER_HOUR, item.minuteOfDay % MINUTES_PER_HOUR)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

    /** Arms every entry that is still in the future; past entries are skipped. Returns the armed ids. */
    fun arm(context: Context, items: List<ScheduledReminder>, nowMs: Long): List<Int> {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return emptyList()
        val armed = mutableListOf<Int>()
        for (item in items) {
            val at = triggerAtMs(item)
            if (at <= nowMs) continue
            val intent = intentFor(context, item.id).putExtra(EXTRA_TITLE, item.title).putExtra(EXTRA_BODY, item.body)
            val pending = PendingIntent.getBroadcast(
                context,
                item.id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            armed += item.id
        }
        return armed
    }

    /** Cancels every id a plan can use, whether or not it is armed. */
    fun cancelAll(context: Context) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        for (id in ScheduledReminder.FIRST_ID until ScheduledReminder.FIRST_ID + ReminderPlanner.PLAN_DAYS) {
            val pending = PendingIntent.getBroadcast(
                context,
                id,
                intentFor(context, id),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
            ) ?: continue
            alarms.cancel(pending)
            pending.cancel()
        }
    }

    private fun intentFor(context: Context, id: Int): Intent =
        Intent(context, ReminderAlarmReceiver::class.java).setAction(ACTION_REMINDER).putExtra(EXTRA_ID, id)

    const val ACTION_REMINDER: String = "dev.talhabayhan.lexi.action.REMINDER"
    const val EXTRA_ID: String = "id"
    const val EXTRA_TITLE: String = "title"
    const val EXTRA_BODY: String = "body"
    private const val MINUTES_PER_HOUR = 60
}
