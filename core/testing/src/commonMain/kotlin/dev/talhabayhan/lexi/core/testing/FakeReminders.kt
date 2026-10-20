package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.reminder.NotificationPermission
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import dev.talhabayhan.lexi.core.domain.reminder.ReminderScheduler
import dev.talhabayhan.lexi.core.domain.reminder.ReminderText
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTextProvider
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant
import dev.talhabayhan.lexi.core.domain.reminder.ScheduledReminder

/** Holds what is "scheduled"; counts the calls. While [failure] is set, every call throws it (and counts). */
public class FakeReminderScheduler : ReminderScheduler {
    /** Thrown by every call while set, e.g. an AlarmManager `SecurityException`. */
    public var failure: Exception? = null

    /** Currently scheduled reminders. */
    public var scheduled: List<ScheduledReminder> = emptyList()
        private set

    public var replaceCalls: Int = 0
        private set

    public var cancelCalls: Int = 0
        private set

    override suspend fun replaceAll(items: List<ScheduledReminder>) {
        replaceCalls++
        failure?.let { throw it }
        scheduled = items
    }

    override suspend fun cancelAll() {
        cancelCalls++
        failure?.let { throw it }
        scheduled = emptyList()
    }
}

/** Settable permission; [request] moves [CAN_ASK][PermissionStatus.CAN_ASK] to [answer]. */
public class FakeNotificationPermission(
    public var current: PermissionStatus = PermissionStatus.CAN_ASK,
    /** What the OS dialog returns. */
    public var answer: PermissionStatus = PermissionStatus.GRANTED,
) : NotificationPermission {
    public var requestCalls: Int = 0
        private set

    public var settingsOpened: Int = 0
        private set

    override suspend fun status(): PermissionStatus = current

    override suspend fun request(): PermissionStatus {
        requestCalls++
        if (current == PermissionStatus.CAN_ASK) current = answer
        return current
    }

    override fun openSystemSettings() {
        settingsOpened++
    }
}

/** Texts that show the variant and count: title `notif_daily_1_title`, body `notif_daily_1_body:5`. */
public class FakeReminderTextProvider : ReminderTextProvider {
    override suspend fun text(variant: ReminderVariant, count: Int?): ReminderText =
        ReminderText(title = "${variant.key}_title", body = "${variant.key}_body" + (count?.let { ":$it" } ?: ""))
}
