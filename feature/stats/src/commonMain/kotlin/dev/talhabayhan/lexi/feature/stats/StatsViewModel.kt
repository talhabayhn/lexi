package dev.talhabayhan.lexi.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Statistics (`09-stats`, `GAME_RULES.md` §11): the `Stats` calculators over the stored card progress and sessions.
 * The card content (level and letter of every card, level totals) never changes while the app runs, so it is read
 * once. Days are epoch days of the player's time zone.
 */
internal class StatsViewModel(
    private val progress: ProgressRepository,
    private val sessions: SessionRepository,
    private val cards: CardRepository,
    private val appClock: AppClock,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val mutableState = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<StatsEffect>(Channel.BUFFERED)
    val effects: Flow<StatsEffect> = effectChannel.receiveAsFlow()

    /** The running read; restarted by "Tekrar dene". */
    private var observing: Job? = null

    init {
        observe()
    }

    private fun observe() {
        observing = viewModelScope.launch {
            val content = loadContent()
            if (content == null) {
                mutableState.value = StatsUiState(isLoaded = true, hasError = true)
                return@launch
            }
            combine(progress.observeAll(), sessions.observeRecords()) { answered, records ->
                present(content, answered, records, appClock.epochDay(appClock.nowMs()))
            }
                // A broken read ends in the error state: there is nothing sensible to show without the numbers.
                .catch { e ->
                    crashReporter.recordException(e, "stats not read")
                    mutableState.value = StatsUiState(isLoaded = true, hasError = true)
                }
                .collect { mutableState.value = it }
        }
    }

    private class Content(
        val totals: Map<Level, Int>,
        val levelOf: Map<CardId, Level>,
        val letterOf: Map<CardId, Char>,
    )

    private suspend fun loadContent(): Content? = try {
        val index = cards.contentIndex()
        Content(totals = index.totals, levelOf = index.levelOf, letterOf = index.letterOf)
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        crashReporter.recordException(e, "stats content not read")
        null
    }

    private fun present(
        content: Content,
        answered: List<CardProgress>,
        records: List<SessionRecord>,
        today: Long,
    ): StatsUiState {
        val accuracy = Stats.accuracy(answered)
        val byLevel = Stats.levelProgress(answered, content.levelOf, content.totals)
        return StatsUiState(
            isLoaded = true,
            isEmpty = records.isEmpty() && accuracy.answered == 0,
            learnedWords = Stats.learnedWords(answered),
            accuracyPercent = accuracy.percent,
            longestStreak = Stats.longestStreak(records),
            completedWheels = Stats.completedWheels(records),
            week = Stats.wordsPerDay(records, today).map { DayWordsUi(weekdayOf(it.epochDay), it.words) }
                .toImmutableList(),
            levels = Level.entries
                .mapNotNull { level ->
                    byLevel[level]?.takeIf { it.total > 0 }?.let { LevelStatUi(level, it.learned, it.total) }
                }
                .toImmutableList(),
            hardLetters = Stats.hardestLetters(answered, content.letterOf)
                .mapNotNull { entry -> entry.percent?.let { HardLetterUi(entry.letter, it) } }
                .take(HARD_LETTERS_SHOWN)
                .toImmutableList(),
        )
    }

    fun onIntent(intent: StatsIntent) {
        when (intent) {
            StatsIntent.Back -> effectChannel.trySend(StatsEffect.NavigateBack)

            StatsIntent.Retry -> if (mutableState.value.hasError) {
                observing?.cancel()
                mutableState.value = StatsUiState()
                observe()
            }
        }
    }

    private companion object {
        const val HARD_LETTERS_SHOWN = 4

        /** 1970-01-01 (epoch day 0) was a Thursday; 0 = Monday. */
        fun weekdayOf(epochDay: Long): Int = (epochDay + 3).mod(7L).toInt()
    }
}
