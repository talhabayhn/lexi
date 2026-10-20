package dev.talhabayhan.lexi.core.platform.reminder

import android.content.Context
import dev.talhabayhan.lexi.core.domain.reminder.ReminderScheduler
import dev.talhabayhan.lexi.core.domain.reminder.ScheduledReminder
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [ReminderScheduler] on AlarmManager (ADR 0021). [channelName] is the localized notification channel name; it is
 * stored with the plan so [ReminderAlarmReceiver] can create the channel without the app's resources running. The
 * AlarmManager binder calls (up to ~45 per call) and SharedPreferences writes run on [io], never on the main thread.
 */
public class AndroidReminderScheduler(
    context: Context,
    private val clock: AppClock,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val channelName: suspend () -> String,
) : ReminderScheduler {
    private val context = context.applicationContext
    private val store = ReminderPlanStore(this.context)

    override suspend fun replaceAll(items: List<ScheduledReminder>) {
        val name = channelName()
        withContext(io) {
            ReminderAlarms.cancelAll(context)
            store.channelName = name
            ReminderNotifier.ensureChannel(context, name)
            store.save(items)
            ReminderAlarms.arm(context, items, clock.nowMs())
        }
    }

    override suspend fun cancelAll() {
        withContext(io) {
            ReminderAlarms.cancelAll(context)
            store.save(emptyList())
        }
    }

    public companion object {
        /** Re-arms the stored plan (boot, clock or time zone change, app update); past entries are skipped. */
        public fun rearmStoredPlan(context: Context, nowMs: Long = System.currentTimeMillis()) {
            val app = context.applicationContext
            val plan = ReminderPlanStore(app).load()
            ReminderAlarms.cancelAll(app)
            ReminderAlarms.arm(app, plan, nowMs)
        }
    }
}
