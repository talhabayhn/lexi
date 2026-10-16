package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level

/**
 * What `02-home` shows. The zero values are real ones (nothing played yet); they are replaced as the repositories
 * emit.
 */
@Immutable
internal data class HomeUiState(
    /** Consecutive days with a completed wheel, ending today (or yesterday while today is still open). */
    val streakDays: Int = 0,
    /** Notebook cards due today ("N tekrar bekliyor"). */
    val dueReviews: Int = 0,
    /** Cards ever answered correctly ("N kelime öğrenildi"). */
    val learnedWords: Int = 0,
    /** Day of the month and month (1–12) of today, for the daily card. */
    val dailyDay: Int = 1,
    val dailyMonth: Int = 1,
    /** Level and mode on the OYNA badge. TODO(M5): the last played choice, once it is persisted. */
    val playLevel: Level = Level.A1,
    val playMode: GameMode = GameMode.CLASSIC,
)

internal sealed interface HomeIntent {
    /** The screen is in the foreground again: "today" may have changed since the last time. */
    data object Resumed : HomeIntent

    /** OYNA: opens the level and mode select. */
    data object Play : HomeIntent

    /** TODO(M6): daily wheel. */
    data object OpenDaily : HomeIntent

    /** TODO(M7): settings. */
    data object OpenSettings : HomeIntent

    /** The Kelime Defterim tile. */
    data object OpenNotebook : HomeIntent

    /** The Istatistikler tile. */
    data object OpenStats : HomeIntent

    /** TODO(M5): practice mode. */
    data object OpenPractice : HomeIntent

    /** TODO(M7): exam packs. */
    data object OpenExamPacks : HomeIntent
}

internal sealed interface HomeEffect {
    data object NavigateToLevelSelect : HomeEffect

    data object NavigateToNotebook : HomeEffect

    data object NavigateToStats : HomeEffect
}
