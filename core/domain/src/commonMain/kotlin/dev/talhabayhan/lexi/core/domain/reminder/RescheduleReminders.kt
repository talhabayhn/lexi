package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.joker.JokerGiftConfig
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.repository.StreakRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps the scheduled reminders equal to [ReminderPlanner]'s plan: reminder settings + streak days + notebook due
 * days ⇒ plan ⇒ texts ⇒ [ReminderScheduler]. Disabled, or enabled without a granted OS permission ⇒ everything is
 * cancelled. Call [invoke] on app start/resume and after a permission answer (always reschedules: the OS may have
 * dropped alarms); collect [run] in an app-lifetime scope to follow input changes (debounced, skips unchanged plans).
 * Neither throws (except cancellation): a failing store, query or OS call (DataStore I/O, SQLDelight, an AlarmManager
 * `SecurityException`) skips that reschedule, and [run] re-subscribes after a back-off. There is no logging or crash
 * reporting abstraction yet, so failures are dropped silently; the next start/resume or input change tries again.
 */
public class RescheduleReminders(
    private val settings: SettingsRepository,
    private val streaks: StreakRepository,
    private val progress: ProgressRepository,
    private val permission: NotificationPermission,
    private val scheduler: ReminderScheduler,
    private val texts: ReminderTextProvider,
    private val clock: AppClock,
    private val streakBonusEvery: Int = JokerGiftConfig().streakBonusEvery,
) {
    private val mutex = Mutex()
    private var lastScheduled: List<ScheduledReminder>? = null

    /** Rebuilds the schedule from the current stored values. */
    public suspend operator fun invoke() {
        ignoringFailures {
            val inputs = Inputs(settings.settings.first(), streaks.observeDays().first(), progress.observeAll().first())
            apply(inputs, force = true)
        }
    }

    /**
     * Never returns: reschedules [debounceMs] after the last change of a reminder setting, streak days or due days.
     * A failing input flow is collected again after [retryDelayMs] (doubling up to [MAX_RETRY_DELAY_MS]).
     */
    @OptIn(FlowPreview::class)
    public suspend fun run(debounceMs: Long = DEFAULT_DEBOUNCE_MS, retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS) {
        combine(settings.settings, streaks.observeDays(), progress.observeAll()) { s, days, cards ->
            Inputs(s, days, cards)
        }
            .retryWhen { cause, attempt ->
                if (cause is CancellationException) return@retryWhen false
                delay(backOff(retryDelayMs, attempt))
                true
            }
            .distinctUntilChanged()
            .debounce(debounceMs)
            .collect { input -> ignoringFailures { apply(input, force = false) } }
    }

    private suspend fun apply(inputs: Inputs, force: Boolean) = mutex.withLock {
        val allowed = inputs.enabled && permission.status() == PermissionStatus.GRANTED
        val items = if (allowed) build(inputs) else emptyList()
        if (!force && items == lastScheduled) return@withLock
        // Unknown until the scheduler call succeeds: a failed call must not make the next identical plan a no-op.
        lastScheduled = null
        if (items.isEmpty()) scheduler.cancelAll() else scheduler.replaceAll(items)
        lastScheduled = items
    }

    private inline fun ignoringFailures(block: () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            // No logging/crash-reporting abstraction exists yet: drop it, the next start/resume or change retries.
        }
    }

    private suspend fun build(inputs: Inputs): List<ScheduledReminder> {
        val now = clock.nowMs()
        val plan = ReminderPlanner.plan(
            ReminderInput(
                enabled = inputs.enabled,
                minuteOfDay = inputs.minuteOfDay,
                reviewReminders = inputs.reviewReminders,
                today = clock.epochDay(now),
                nowMinuteOfDay = clock.minuteOfDay(now),
                playDays = inputs.playDays,
                reviewDueDays = inputs.dueDays,
                streakBonusEvery = streakBonusEvery,
            ),
        )
        return plan.mapIndexed { index, planned ->
            val text = texts.text(planned.variant, planned.count)
            ScheduledReminder(
                id = ScheduledReminder.FIRST_ID + index,
                epochDay = planned.epochDay,
                minuteOfDay = planned.minuteOfDay,
                title = text.title,
                body = text.body,
            )
        }
    }

    /** Only what the plan depends on, so unrelated setting or progress changes don't reschedule. */
    private data class Inputs(
        val enabled: Boolean,
        val minuteOfDay: Int,
        val reviewReminders: Boolean,
        val playDays: List<Long>,
        val dueDays: List<Long>,
    ) {
        constructor(settings: Settings, playDays: List<Long>, cards: List<CardProgress>) : this(
            enabled = settings.dailyReminder,
            minuteOfDay = settings.reminderMinuteOfDay,
            reviewReminders = settings.reviewReminders,
            playDays = playDays,
            dueDays = cards.dueDays(),
        )
    }

    public companion object {
        public const val DEFAULT_DEBOUNCE_MS: Long = 500L
        public const val DEFAULT_RETRY_DELAY_MS: Long = 1_000L
        public const val MAX_RETRY_DELAY_MS: Long = 5 * 60_000L

        private fun backOff(base: Long, attempt: Long): Long =
            (base shl attempt.coerceAtMost(MAX_SHIFT).toInt()).coerceAtMost(MAX_RETRY_DELAY_MS)

        private const val MAX_SHIFT = 16L

        /** Due day of every notebook card that can fall due (mirrors `CardProgress.isDue`). */
        internal fun List<CardProgress>.dueDays(): List<Long> =
            mapNotNull { p -> p.dueEpochDay?.takeIf { p.box != null && !p.mastered } }.sorted()
    }
}
