package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.joker.JokerGiftConfig
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.random.Seeds
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
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
    private val cards: CardRepository,
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
        val today = clock.epochDay(now)
        val wordSlots = plan.withIndex().filter { (_, p) -> p.carriesWord(today) }.map { it.index }
        val words = if (inputs.showWord) pickWords(inputs, today, wordSlots.size) else emptyList()
        return plan.mapIndexed { index, planned ->
            val slot = wordSlots.indexOf(index)
            val word = slot.takeIf { it >= 0 }?.let(words::getOrNull)
            val text = if (word != null) {
                texts.wordText(
                    word = ReminderWordPicker.displayWord(word.answer),
                    meaning = ReminderWordPicker.displayMeaning(word.clue),
                    index = slot,
                )
            } else {
                texts.text(planned.variant, planned.count)
            }
            ScheduledReminder(
                id = ScheduledReminder.FIRST_ID + index,
                epochDay = planned.epochDay,
                minuteOfDay = planned.minuteOfDay,
                title = text.title,
                body = text.body,
            )
        }
    }

    /** Daily and review reminders of the next [ReminderWordPicker.WINDOW_DAYS] days show a word. */
    private fun PlannedReminder.carriesWord(today: Long): Boolean =
        (variant.isDaily || variant.isReview) && epochDay < today + ReminderWordPicker.WINDOW_DAYS

    /**
     * Up to [slots] showable cards, due notebook cards first, none twice. The seed depends on [today] only, so
     * rebuilding on the same day with the same progress gives the same words. A failing content read shows no words
     * (the regular texts are used).
     */
    private suspend fun pickWords(inputs: Inputs, today: Long, slots: Int): List<Card> {
        if (slots == 0 || inputs.answeredIds.isEmpty()) return emptyList()
        val due = inputs.dueWords.filter { it.second <= today }.map { it.first }
        val ids = ReminderWordPicker.pick(
            due = due,
            answered = inputs.answeredIds,
            count = slots + ReminderWordPicker.SPARE_CANDIDATES,
            seed = Seeds.stableHash("reminder-words:$today"),
        )
        val order = ids.withIndex().associate { (i, id) -> id to i }
        return try {
            cards.cards(ids).filter(ReminderWordPicker::isShowable).sortedBy { order[it.id] }.take(slots)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            emptyList()
        }
    }

    /** Only what the plan depends on, so unrelated setting or progress changes don't reschedule. */
    private data class Inputs(
        val enabled: Boolean,
        val minuteOfDay: Int,
        val reviewReminders: Boolean,
        val playDays: List<Long>,
        val dueDays: List<Long>,
        val showWord: Boolean,
        /** Ids of answered cards (`seen > 0`) when [showWord], else empty: only a new word reschedules. */
        val answeredIds: List<CardId>,
        /** Notebook cards that can fall due, with their due day, when [showWord]. */
        val dueWords: List<Pair<CardId, Long>>,
    ) {
        constructor(settings: Settings, playDays: List<Long>, cards: List<CardProgress>) : this(
            enabled = settings.dailyReminder,
            minuteOfDay = settings.reminderMinuteOfDay,
            reviewReminders = settings.reviewReminders,
            playDays = playDays,
            dueDays = cards.dueDays(),
            showWord = settings.reminderShowWord,
            answeredIds = if (settings.reminderShowWord) {
                cards.filter { it.seen > 0 }.map { it.cardId }.sortedBy { it.value }
            } else {
                emptyList()
            },
            dueWords = if (settings.reminderShowWord) {
                cards.mapNotNull { p -> p.dueEpochDay?.takeIf { p.box != null && !p.mastered }?.let { p.cardId to it } }
                    .sortedBy { it.first.value }
            } else {
                emptyList()
            },
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
