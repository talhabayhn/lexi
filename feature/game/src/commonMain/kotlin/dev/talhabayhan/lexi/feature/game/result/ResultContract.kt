package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
    /** "Tekrar edilecekler" rows to show (wrong and passed cards in wheel order). */
    val rows: ImmutableList<ReviewRowUi> = persistentListOf(),
    /** Rows behind "+N kelime daha · Tümünü gör →". */
    val hiddenRows: Int = 0,
    /** True until the review cards are loaded. */
    val isLoading: Boolean = true,
) {
    /** TEKRAR OYNA is offered; not after a Daily (one scored attempt per day, `GAME_RULES.md` §5). */
    val canPlayAgain: Boolean get() = mode != GameMode.DAILY
}

@Immutable
internal data class ReviewRowUi(
    val letter: Char,
    /** The answer as shown and spoken ("chair"). */
    val word: String,
    val clue: String,
    val example: String,
    val outcome: ReviewOutcome,
)

internal sealed interface ResultIntent {
    /** TEKRAR OYNA: a new game with the same level, mode and category. */
    data object PlayAgain : ResultIntent

    /** Paylaş: the share card is M6. */
    data object Share : ResultIntent

    /** "Ana menüye dön". */
    data object Home : ResultIntent

    /** "+N kelime daha · Tümünü gör →": shows every review row. */
    data object ShowAll : ResultIntent

    /** Speaker button of a review row. */
    data class Speak(val word: String) : ResultIntent
}
