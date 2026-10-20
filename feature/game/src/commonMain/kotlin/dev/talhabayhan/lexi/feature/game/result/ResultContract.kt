package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.share.ShareCard
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/** The result screen (`06-result`): the finished game from [ResultRoute] plus the review cards read by id. */
@Immutable
internal data class ResultUiState(
    val level: Level,
    val mode: GameMode,
    val reason: GameOverReason,
    val score: Int,
    val longestStreak: Int,
    /** Remaining whole seconds (rounded up like the game clock); null in untimed modes. */
    val remainingSeconds: Int?,
    val correct: Int,
    val wrong: Int,
    val passed: Int,
    /** Cards that went into box 0 of the notebook ("7 kelime defterine eklendi"); 0 when nothing was stored. */
    val addedToNotebook: Int,
    /** "Tekrar edilecekler" rows to show (all review rows in wheel order). */
    val rows: ImmutableList<ReviewRowUi> = persistentListOf(),
    /** Rows grouped by outcome: Doğru, Yanlış, Pas, Süre bitti. */
    val groups: ImmutableList<ReviewGroupUi> = persistentListOf(),
    /** Groups opened with "Daha fazla göster" (all their rows shown); the others show [ReviewGroupUi.PREVIEW_ROWS]. */
    val expandedGroups: ImmutableSet<ReviewOutcome> = persistentSetOf(),
    /** True until the review cards are loaded. */
    val isLoading: Boolean = true,
    /** The review cards could not be read; the list shows an empty state with "Tekrar dene". */
    val reviewError: Boolean = false,
    /** The share card (`12-share-card`) of this game; null until the streak is read. */
    val shareCard: ShareCard? = null,
    /** A share is running: the card is being captured or handed to the share sheet; further taps are ignored. */
    val isSharing: Boolean = false,
) {
    /** TEKRAR OYNA is offered; not after a Daily (one scored attempt per day, `GAME_RULES.md` §5). */
    val canPlayAgain: Boolean get() = mode != GameMode.DAILY

    /**
     * The mode counts points. Unscored modes (Practice, Review) score 0 everywhere (`GAME_RULES.md` §6), so the header
     * shows the correct answers instead of "0 puan" (DESIGN-DEBT: no design for their result).
     */
    val isScored: Boolean get() = mode.isScored
}

@Immutable
internal data class ReviewGroupUi(val outcome: ReviewOutcome, val rows: ImmutableList<ReviewRowUi>) {
    companion object {
        /** Rows a collapsed group shows; the rest are behind "Daha fazla göster". */
        const val PREVIEW_ROWS: Int = 5
    }
}

@Immutable
internal data class ReviewRowUi(
    val letter: Char,
    /** The answer as shown and spoken ("chair"). */
    val word: String,
    val clue: String,
    val example: String,
    val outcome: ReviewOutcome,
    val level: Level? = null,
    val cardId: String = "",
    val isAddedToNotebook: Boolean = false,
)

internal sealed interface ResultIntent {
    /** TEKRAR OYNA: a new game with the same level, mode and category. */
    data object PlayAgain : ResultIntent

    /** Paylaş: starts a share; the screen then captures the card ([SharePng] or [ShareCaptureFailed]). */
    data object Share : ResultIntent

    /** The captured card ([png]) and the share text, for the system share sheet. */
    class SharePng(val png: ByteArray, val text: String) : ResultIntent

    /** The card could not be rendered to an image. */
    data object ShareCaptureFailed : ResultIntent

    /** "Ana menüye dön". */
    data object Home : ResultIntent

    /** "Daha fazla göster" / "Daha az göster" under a group: shows all its rows, or only the first ones again. */
    data class ToggleGroup(val outcome: ReviewOutcome) : ResultIntent

    /** Speaker button of a review row. */
    data class Speak(val word: String) : ResultIntent

    /** "Tekrar dene" after the review cards could not be read. */
    data object Retry : ResultIntent

    /** Per-row "Deftere ekle" for time-up words. */
    data class AddToNotebook(val cardId: String) : ResultIntent
}

internal sealed interface ResultEffect {
    /** The share sheet could not be opened ("Paylaşım başlatılamadı."). */
    data object ShareFailed : ResultEffect
}
