package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.flow.first

/**
 * The proactive reminder pre-prompt (`docs/copy/COPY.md` §1 `reminder_preprompt_*`, owner decision M7): offered after
 * the player's second completed real game (never the first), while the reminder is off and the OS permission isn't
 * granted. At most twice in total: if the first one is answered "Şimdi değil" (or just closed), one more may follow
 * [SECOND_ASK_DELAY_MS] later, and only while the OS dialog can still be shown. Never at app start, during onboarding
 * or the level test (neither counts as a real game).
 */
public object ReminderPrePromptPolicy {
    /** Completed real games needed before the first ask. */
    public const val MIN_REAL_GAMES: Int = 2

    /** Maximum asks in the app's lifetime. */
    public const val MAX_ASKS: Int = 2

    /** Wait between the first and the second ask: 7 days. */
    public const val SECOND_ASK_DELAY_MS: Long = 7L * 24 * 60 * 60 * 1000

    /** Modes that don't count as a real game (onboarding's level test; hot-seat duel isn't recorded either). */
    private val NOT_REAL = setOf(GameMode.LEVEL_TEST, GameMode.DUEL)

    public fun isRealGame(record: SessionRecord): Boolean =
        record.completed && record.mode != null && record.mode !in NOT_REAL

    public fun shouldShow(
        settings: Settings,
        sessions: Collection<SessionRecord>,
        permission: PermissionStatus,
        nowMs: Long,
    ): Boolean = canAsk(settings, permission, nowMs) && sessions.count(::isRealGame) >= MIN_REAL_GAMES

    /** Everything but the game count: switch, permission, ask budget and the 7-day wait. */
    internal fun canAsk(settings: Settings, permission: PermissionStatus, nowMs: Long): Boolean {
        if (settings.dailyReminder || permission == PermissionStatus.GRANTED) return false
        return when (settings.reminderPrePromptAsks) {
            0 -> true

            1 ->
                permission == PermissionStatus.CAN_ASK &&
                    nowMs - settings.reminderPrePromptLastAskMs >= SECOND_ASK_DELAY_MS

            else -> false
        }
    }
}

/** Whether to show the reminder pre-prompt now ([ReminderPrePromptPolicy]). */
public class ShouldShowReminderPrePrompt(
    private val settings: SettingsRepository,
    private val sessions: SessionRepository,
    private val permission: NotificationPermission,
    private val clock: AppClock,
) {
    public suspend operator fun invoke(): Boolean {
        val current = settings.settings.first()
        // Cheap checks first: the permission call and the session query are skipped once the budget is spent.
        if (current.dailyReminder || current.reminderPrePromptAsks >= ReminderPrePromptPolicy.MAX_ASKS) return false
        return ReminderPrePromptPolicy.shouldShow(
            current,
            sessions.observeRecords().first(),
            permission.status(),
            clock.nowMs(),
        )
    }

    /**
     * Completed real games so far. Home compares it between two of its appearances: only a grown count means the player
     * came back from a real game (not an app start, an app foreground or a return from another screen).
     */
    public suspend fun realGameCount(): Int =
        sessions.observeRecords().first().count(ReminderPrePromptPolicy::isRealGame)
}

/**
 * The player's answer on the pre-prompt. Any answer counts as an ask (once, also after the screen counted it on entry). "Yes" asks the OS; when granted the reminder is
 * turned on at [ReminderTimes.DEFAULT_MINUTE] and [reschedule] runs. Returns the permission status after a "yes"
 * (null after "not now"), so the UI can show `reminder_denied_hint` when it's [PermissionStatus.BLOCKED].
 */
public class AnswerReminderPrePrompt(
    private val settings: SettingsRepository,
    private val permission: NotificationPermission,
    private val reschedule: RescheduleReminders,
    private val clock: AppClock,
) {
    public suspend operator fun invoke(accepted: Boolean): PermissionStatus? {
        settings.recordReminderPrePromptAsked(clock.nowMs())
        if (!accepted) return null
        val status = permission.request()
        if (status == PermissionStatus.GRANTED) {
            settings.update { it.copy(dailyReminder = true, reminderMinuteOfDay = ReminderTimes.DEFAULT_MINUTE) }
            reschedule()
        }
        return status
    }
}
