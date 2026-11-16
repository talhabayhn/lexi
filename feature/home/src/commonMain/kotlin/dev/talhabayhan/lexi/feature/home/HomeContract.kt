package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings

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
internal data class DailyGiftNotice(val type: DailyGiftNoticeType, val amount: Int)

/**
 * What the video flow's Home shows (`video-flow-notes.md` §1, `home_v5.png`). The zero values are real ones (nothing
 * played yet); they are replaced as the repositories emit. No streak anywhere on Home.
 */
@Immutable
internal data class HomeUiState(
    /** "toplam puan": the scores of every stored game added up. */
    val totalScore: Long = 0L,
    /** Notebook cards due today ("24 tekrar" on the Defter tile). */
    val dueReviews: Int = 0,
    /** State of the daily wheel card (Available, AlreadyPlayed, Unavailable). */
    val dailyState: DailyCardState = DailyCardState.Available,
    /** Daily joker gift notice (toast) when GrantDailyJokers gives a gift or streak bonus. */
    val dailyGiftNotice: DailyGiftNotice? = null,
    /** Names of the exam packs playable in this build, in display order; the Sınavlar tile lists them. */
    val examPackNames: List<String> = emptyList(),
    /** The level "Çarkını seç" opens on: the onboarding's choice, then the last level chip picked. */
    val selectedLevel: Level = Level.A1,
    /** The time last chosen for Classic / Hardcore, for the play card's "3 dk". */
    val classicTimeLimitSeconds: Int = Settings.DEFAULT_CLASSIC_TIME_LIMIT_SECONDS,
)

internal sealed interface HomeIntent {
    /** Periodic refresh (on resume and once per minute while STARTED). */
    data object Refresh : HomeIntent

    /** Dismisses the daily joker gift notice. */
    data object DismissNotice : HomeIntent

    /** The gift toast is on screen: it counts as consumed once Home is left. */
    data object NoticeShown : HomeIntent

    /** Home stopped being resumed: a shown gift notice is dropped so it never re-appears. */
    data object Left : HomeIntent

    /** The screen is in the foreground again: "today" may have changed since the last time. */
    data object Resumed : HomeIntent

    /**
     * Home came to the foreground (every STARTED, app foreground included). Only when a real game was completed since
     * the previous one, i.e. back from a game's result, may the one-time reminder pre-prompt open
     * ([HomeEffect.NavigateToReminderPrePrompt]).
     */
    data object Entered : HomeIntent

    /** The play card: opens "Çarkını seç" on the selected level. */
    data object Play : HomeIntent

    /** The daily wheel card: opens the daily wheel when available. */
    data object OpenDaily : HomeIntent

    /** "Ayarlar" in the bottom bar. */
    data object OpenSettings : HomeIntent

    /** The Defter tile. */
    data object OpenNotebook : HomeIntent

    /** "İstatistik" in the bottom bar. */
    data object OpenStats : HomeIntent

    /** The Sınavlar tile: the exam packs page. */
    data object OpenExamPacks : HomeIntent

    /**
     * "Davet et" in the bottom bar: shares [message] (the invite text with the landing link, in the app language)
     * through the system share sheet.
     */
    data class Invite(val message: String) : HomeIntent

    /** The "İki kişi, tek telefon" box: the hot-seat duel setup. */
    data object OpenDuel : HomeIntent
}

internal sealed interface HomeEffect {
    /** "Çarkını seç" on [level]. */
    data class NavigateToCategoryPicker(val level: Level) : HomeEffect

    data object NavigateToDaily : HomeEffect

    data object NavigateToNotebook : HomeEffect

    data object NavigateToStats : HomeEffect

    data object NavigateToSettings : HomeEffect

    data object NavigateToDuel : HomeEffect

    data object NavigateToExamPacks : HomeEffect

    /** The proactive reminder pre-prompt: after the first real game, back on Home (`ShouldShowReminderPrePrompt`). */
    data object NavigateToReminderPrePrompt : HomeEffect
}
