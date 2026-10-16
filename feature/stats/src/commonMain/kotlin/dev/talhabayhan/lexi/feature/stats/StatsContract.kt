package dev.talhabayhan.lexi.feature.stats

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Words learned on one of the last seven days; [weekday] is 0 = Monday ... 6 = Sunday. */
@Immutable
internal data class DayWordsUi(val weekday: Int, val words: Int)

/** One level row: cards learned of the level's total. */
@Immutable
internal data class LevelStatUi(val level: Level, val learned: Int, val total: Int) {
    val fraction: Float get() = if (total <= 0) 0f else learned.toFloat() / total
}

/** A hardest letter and its accuracy (percent, rounded half up). */
@Immutable
internal data class HardLetterUi(val letter: Char, val percent: Int)

@Immutable
internal data class StatsUiState(
    /** False until the first read, so "empty" and "error" are only reported afterwards. */
    val isLoaded: Boolean = false,
    /** The statistics could not be read. */
    val hasError: Boolean = false,
    /** Nothing played yet: no stored game and no answered card. */
    val isEmpty: Boolean = false,
    val learnedWords: Int = 0,
    /** Null while nothing was answered. */
    val accuracyPercent: Int? = null,
    val longestStreak: Int = 0,
    val completedWheels: Int = 0,
    /** The last seven days, oldest first; the last one is today. */
    val week: ImmutableList<DayWordsUi> = persistentListOf(),
    /** Levels that have cards, in level order. */
    val levels: ImmutableList<LevelStatUi> = persistentListOf(),
    /** At most four, hardest first; empty until a letter has enough answers. */
    val hardLetters: ImmutableList<HardLetterUi> = persistentListOf(),
) {
    val weekTotal: Int get() = week.sumOf { it.words }
}

internal sealed interface StatsIntent {
    data object Back : StatsIntent

    /** "Tekrar dene" after the statistics could not be read. */
    data object Retry : StatsIntent
}

internal sealed interface StatsEffect {
    data object NavigateBack : StatsEffect
}
