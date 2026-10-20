package dev.talhabayhan.lexi.core.platform.reminder

import dev.talhabayhan.lexi.core.domain.reminder.ReminderScheduler
import dev.talhabayhan.lexi.core.domain.reminder.ScheduledReminder
import kotlinx.datetime.LocalDate
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * [ReminderScheduler] on `UNUserNotificationCenter` (ADR 0021): one non-repeating calendar trigger per entry, in the
 * device's local time zone. Only requests whose id starts with [ID_PREFIX] are ever removed.
 */
public class IosReminderScheduler : ReminderScheduler {
    private val center: UNUserNotificationCenter get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun replaceAll(items: List<ScheduledReminder>) {
        removeOurPending()
        for (item in items) add(item)
    }

    override suspend fun cancelAll() {
        removeOurPending()
    }

    private suspend fun removeOurPending() {
        val ids = suspendCoroutine<List<String>> { cont ->
            center.getPendingNotificationRequestsWithCompletionHandler { requests ->
                cont.resume(
                    requests.orEmpty()
                        .mapNotNull { (it as? UNNotificationRequest)?.identifier }
                        .filter { it.startsWith(ID_PREFIX) },
                )
            }
        }
        if (ids.isNotEmpty()) center.removePendingNotificationRequestsWithIdentifiers(ids)
    }

    private suspend fun add(item: ScheduledReminder) {
        val content = UNMutableNotificationContent().apply {
            setTitle(item.title)
            setBody(item.body)
            setSound(UNNotificationSound.defaultSound)
        }
        val date = LocalDate.fromEpochDays(item.epochDay)
        // No time zone on the components: the trigger fires at this wall-clock time in whatever zone the device is in.
        val components = NSDateComponents().apply {
            setYear(date.year.toLong())
            setMonth((date.month.ordinal + 1).toLong())
            setDay(date.day.toLong())
            setHour((item.minuteOfDay / MINUTES_PER_HOUR).toLong())
            setMinute((item.minuteOfDay % MINUTES_PER_HOUR).toLong())
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier("$ID_PREFIX${item.id}", content, trigger)
        suspendCoroutine<Unit> { cont ->
            // A failed request (e.g. permission revoked meanwhile) is dropped; the next reschedule retries.
            center.addNotificationRequest(request) { _ -> cont.resume(Unit) }
        }
    }

    private companion object {
        const val ID_PREFIX = "lexi.reminder."
        const val MINUTES_PER_HOUR = 60
    }
}
