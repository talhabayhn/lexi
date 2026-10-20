package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlinx.coroutines.flow.first

/**
 * The proactive reminder pre-prompt (`docs/copy/COPY.md` §1 `reminder_preprompt_*`, owner decision M7): shown at most
 * once in the app's lifetime, only after the player has completed a real game, while the reminder is off and the OS
 * permission isn't granted. Never at app start or during onboarding.
 */
public object ReminderPrePromptPolicy {
    /** Modes that don't count as a real game (onboarding's level test; hot-seat duel isn't recorded either). */
    private val NOT_REAL = setOf(GameMode.LEVEL_TEST, GameMode.DUEL)

    public fun isRealGame(record: SessionRecord): Boolean =
        record.completed && record.mode != null && record.mode !in NOT_REAL

    public fun shouldShow(
        settings: Settings,
        sessions: Collection<SessionRecord>,
        permission: PermissionStatus,
    ): Boolean = !settings.reminderPrePromptShown &&
        !settings.dailyReminder &&
        permission != PermissionStatus.GRANTED &&
        sessions.any(::isRealGame)
}

/** Whether to show the reminder pre-prompt now ([ReminderPrePromptPolicy]). */
public class ShouldShowReminderPrePrompt(
    private val settings: SettingsRepository,
    private val sessions: SessionRepository,
    private val permission: NotificationPermission,
) {
    public suspend operator fun invoke(): Boolean {
        val current = settings.settings.first()
        // Cheap checks first: the permission call and the session query are skipped once the prompt was shown.
        if (current.reminderPrePromptShown || current.dailyReminder) return false
        return ReminderPrePromptPolicy.shouldShow(current, sessions.observeRecords().first(), permission.status())
    }

    /**
     * Completed real games so far. Home compares it between two of its appearances: only a grown count means the player
     * came back from a real game (not an app start, an app foreground or a return from another screen).
     */
    public suspend fun realGameCount(): Int =
        sessions.observeRecords().first().count(ReminderPrePromptPolicy::isRealGame)
}

/**
 * The player's answer on the pre-prompt. Any answer marks it shown. "Yes" asks the OS; when granted the reminder is
 * turned on at [ReminderTimes.DEFAULT_MINUTE] and [reschedule] runs. Returns the permission status after a "yes"
 * (null after "not now"), so the UI can show `reminder_denied_hint` when it's [PermissionStatus.BLOCKED].
 */
public class AnswerReminderPrePrompt(
    private val settings: SettingsRepository,
    private val permission: NotificationPermission,
    private val reschedule: RescheduleReminders,
) {
    public suspend operator fun invoke(accepted: Boolean): PermissionStatus? {
        settings.setReminderPrePromptShown()
        if (!accepted) return null
        val status = permission.request()
        if (status == PermissionStatus.GRANTED) {
            settings.update { it.copy(dailyReminder = true, reminderMinuteOfDay = ReminderTimes.DEFAULT_MINUTE) }
            reschedule()
        }
        return status
    }
}
