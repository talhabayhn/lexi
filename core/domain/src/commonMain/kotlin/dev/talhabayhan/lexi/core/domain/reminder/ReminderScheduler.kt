package dev.talhabayhan.lexi.core.domain.reminder

/**
 * Local notification scheduling (`ARCHITECTURE.md` §7: WorkManager/AlarmManager on Android,
 * `UNUserNotificationCenter` on iOS). The plan comes from [RescheduleReminders]; implementations only deliver it.
 */
public interface ReminderScheduler {
    /** Cancels every reminder scheduled before and schedules [items] (at their local day and minute). */
    public suspend fun replaceAll(items: List<ScheduledReminder>)

    /** Cancels every scheduled reminder. */
    public suspend fun cancelAll()
}

/**
 * A reminder with resolved texts. [id] is [FIRST_ID] + its index in the plan, so ids stay within
 * [FIRST_ID] until [FIRST_ID] + [ReminderPlanner.PLAN_DAYS] and an implementation can cancel them all by id.
 */
public data class ScheduledReminder(
    val id: Int,
    val epochDay: Long,
    val minuteOfDay: Int,
    val title: String,
    val body: String,
) {
    public companion object {
        public const val FIRST_ID: Int = 7_100
    }
}
