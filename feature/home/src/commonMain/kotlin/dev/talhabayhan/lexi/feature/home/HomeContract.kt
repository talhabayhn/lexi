package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.goal.DailyGoalProgress
import dev.talhabayhan.lexi.core.domain.model.Level

/**
 * State of the daily wheel card on the home screen.
 */
@Immutable
internal sealed interface DailyCardState {
    /** The daily wheel is available to play today. */
    data object Available : DailyCardState

    /** The daily wheel has already been played today (shows today's score and hours and minutes until next wheel). */
    data class AlreadyPlayed(val score: Int, val hoursUntilNext: Int = 0, val minutesUntilNext: Int = 0) :
        DailyCardState

    /** No playable cards exist for this level in the pool. */
    data object Unavailable : DailyCardState
}

/**
 * Type of free joker gift notice shown on home (`GAME_RULES.md` §7, D10).
 */
@Immutable
internal enum class DailyGiftNoticeType {
    Daily,
    Streak,
    Combined,
}

@Immutable
internal data class DailyGiftNotice(
    val type: DailyGiftNoticeType,
    val amount: Int,
)

/**
 * What `02-home` shows. The zero values are real ones (nothing played yet); they are replaced as the repositories
 * emit.
 */
@Immutable
internal data class HomeUiState(
    /** Consecutive days with a completed wheel, ending today (or yesterday while today is still open). 0 hides badge. */
    val streakDays: Int = 0,
    /** Notebook cards due today ("N tekrar bekliyor"). */
    val dueReviews: Int = 0,
    /** Cards ever answered correctly ("N kelime öğrenildi"). */
    val learnedWords: Int = 0,
    /** Day of the month and month (1–12) of today, for the daily card. */
    val dailyDay: Int = 1,
    val dailyMonth: Int = 1,
    /** State of the daily wheel card (Available, AlreadyPlayed, Unavailable). */
    val dailyState: DailyCardState = DailyCardState.Available,
    /** Level and mode on the OYNA badge. TODO(M5): the last played choice, once it is persisted. */
    val playLevel: Level = Level.A1,
    val playMode: GameMode = GameMode.CLASSIC,
    /** Daily goal progress for today ("Bugün {done}/{goal}"). */
    val dailyGoalProgress: DailyGoalProgress? = null,
    /** Daily joker gift notice (toast) when GrantDailyJokers gives a gift or streak bonus. */
    val dailyGiftNotice: DailyGiftNotice? = null,
)

internal sealed interface HomeIntent {
    /** Periodic refresh (on resume and once per minute while STARTED). */
    data object Refresh : HomeIntent

    /** Dismisses the daily joker gift notice. */
    data object DismissNotice : HomeIntent

    /** The screen is in the foreground again: "today" may have changed since the last time. */
    data object Resumed : HomeIntent

    /** OYNA: opens the level and mode select. */
    data object Play : HomeIntent

    /** The daily wheel card: opens the daily wheel when available. */
    data object OpenDaily : HomeIntent

    /** The gear icon. */
    data object OpenSettings : HomeIntent

    /** The Kelime Defterim tile. */
    data object OpenNotebook : HomeIntent

    /** The Istatistikler tile. */
    data object OpenStats : HomeIntent

    /** The practice tile: the level select on Practice mode. */
    data object OpenPractice : HomeIntent

    /** The exam packs tile: a "coming soon" page until M7. */
    data object OpenExamPacks : HomeIntent
}

internal sealed interface HomeEffect {
    data object NavigateToLevelSelect : HomeEffect

    data object NavigateToDaily : HomeEffect

    data object NavigateToNotebook : HomeEffect

    data object NavigateToStats : HomeEffect

    data object NavigateToSettings : HomeEffect

    data object NavigateToPractice : HomeEffect

    data object NavigateToExamPacks : HomeEffect
}
