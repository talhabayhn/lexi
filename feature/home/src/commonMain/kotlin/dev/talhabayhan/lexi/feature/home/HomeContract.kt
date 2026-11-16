package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.goal.DailyGoalProgress
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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

/** Words learned of a level's [total] cards ("412 / 850 kelime"); 0 / 0 while the content is read or unreadable. */
@Immutable
internal data class LevelProgressUi(val level: Level, val learned: Int, val total: Int) {
    val fraction: Float get() = if (total <= 0) 0f else (learned.toFloat() / total).coerceIn(0f, 1f)
}

/** One option of the category sheet: a category of the selected level with its learned words. */
@Immutable
internal data class CategoryUi(
    val id: CategoryId,
    val nameTr: String,
    val nameEn: String,
    val learned: Int,
    val total: Int,
) {
    val isComplete: Boolean get() = total > 0 && learned >= total
}

/** The selected level's categories as the sheet shows them. */
@Immutable
internal sealed interface CategoriesState {
    /** Being read (also right after a level change). */
    data object Loading : CategoriesState

    /** Read; empty for a level without categories (NAT): the category row is hidden. */
    data class Loaded(val categories: ImmutableList<CategoryUi>) : CategoriesState

    /** The content could not be read: the row shows Karışık, the sheet offers "Tekrar dene". */
    data object Error : CategoriesState
}

/**
 * What `02c-home-v2` shows. The zero values are real ones (nothing played yet); they are replaced as the repositories
 * emit. The selection (level, mode, category) is the stored one from the first frame (`home-v2-notes.md` §2).
 */
@Immutable
internal data class HomeUiState(
    /** Consecutive days with a completed wheel, ending today (or yesterday while today is still open). 0 hides it. */
    val streakDays: Int = 0,
    /** Notebook cards due today: the notebook key's badge (hidden at 0, "99+" above 99). */
    val dueReviews: Int = 0,
    /** Day of the month and month (1–12) of today, for the daily card. */
    val dailyDay: Int = 1,
    val dailyMonth: Int = 1,
    /** State of the daily wheel card (Available, AlreadyPlayed, Unavailable). */
    val dailyState: DailyCardState = DailyCardState.Available,
    /** Daily goal progress for today ("bugünkü hedef 0/1"). */
    val dailyGoalProgress: DailyGoalProgress? = null,
    /** Daily joker gift notice (toast) when GrantDailyJokers gives a gift or streak bonus. */
    val dailyGiftNotice: DailyGiftNotice? = null,
    /** The six levels with their progress, in chip order. */
    val levels: ImmutableList<LevelProgressUi> = Level.entries.map { LevelProgressUi(it, 0, 0) }.toImmutableList(),
    val selectedLevel: Level = Level.A1,
    val selectedMode: GameMode = GameMode.CLASSIC,
    /** The selected category of [selectedLevel]; null = Karışık. */
    val selectedCategory: CategoryId? = null,
    val categories: CategoriesState = CategoriesState.Loading,
    val categorySheetOpen: Boolean = false,
    /** Classic total time, the mode control's "3:00" (the player's setting). */
    val classicTimeLimitSeconds: Int = Settings.DEFAULT_CLASSIC_TIME_LIMIT_SECONDS,
    /** Slots of a regular wheel, for OYNA's badge "26 HARF". */
    val letterCount: Int = ('A'..'Z').count(),
) {
    val selectedProgress: LevelProgressUi get() = levels.first { it.level == selectedLevel }

    /** The category row is hidden when the level has none (NAT, or an empty list once read). */
    val showCategoryRow: Boolean
        get() = selectedLevel != Level.NAT && (categories as? CategoriesState.Loaded)?.categories?.isEmpty() != true

    /** The selected category as shown (null = Karışık; also while the list is read or unreadable). */
    val selectedCategoryUi: CategoryUi?
        get() = (categories as? CategoriesState.Loaded)?.categories?.firstOrNull { it.id == selectedCategory }

    /**
     * The category OYNA plays: the stored one while the list is still read (content never changes, it was valid when
     * stored), Karışık when the list could not be read or the level has none.
     */
    val playCategory: CategoryId?
        get() = when {
            selectedLevel == Level.NAT -> null
            categories is CategoriesState.Loading -> selectedCategory
            else -> selectedCategoryUi?.id
        }
}

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

    /** A level chip: selects the level and resets the category to Karışık (saved). */
    data class SelectLevel(val level: Level) : HomeIntent

    /** A mode cell (saved). */
    data class SelectMode(val mode: GameMode) : HomeIntent

    /** The category row: opens the sheet. */
    data object OpenCategories : HomeIntent

    /** Scrim, close button or system back on the sheet. */
    data object CloseCategories : HomeIntent

    /** A sheet option: null = Karışık (saved); closes the sheet. */
    data class SelectCategory(val category: CategoryId?) : HomeIntent

    /** "Tekrar dene" in the sheet after the categories could not be read. */
    data object RetryCategories : HomeIntent

    /** OYNA: starts a game of the selection. */
    data object Play : HomeIntent

    /** The daily wheel card: opens the daily wheel when available. */
    data object OpenDaily : HomeIntent

    /** The gear icon. */
    data object OpenSettings : HomeIntent

    /** The Defter key. */
    data object OpenNotebook : HomeIntent

    /** The İstatistik key. */
    data object OpenStats : HomeIntent

    /** The Sınavlar key: the exam packs page. */
    data object OpenExamPacks : HomeIntent

    /** The Düello key: the hot-seat duel setup. */
    data object OpenDuel : HomeIntent
}

internal sealed interface HomeEffect {
    /** OYNA: a game of [level], [mode] and [category] (null = the level's mixed wheel). */
    data class StartGame(val level: Level, val mode: GameMode, val category: CategoryId?) : HomeEffect

    data object NavigateToDaily : HomeEffect

    data object NavigateToNotebook : HomeEffect

    data object NavigateToStats : HomeEffect

    data object NavigateToSettings : HomeEffect

    data object NavigateToDuel : HomeEffect

    data object NavigateToExamPacks : HomeEffect

    /** The proactive reminder pre-prompt: after the first real game, back on Home (`ShouldShowReminderPrePrompt`). */
    data object NavigateToReminderPrePrompt : HomeEffect
}
