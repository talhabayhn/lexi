package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Resting look of the game screen, mapped from the engine state by [GameUiMapper] (ADR 0019). It never contains the
 * running time (see [GameClockUi]), so ticks don't re-emit it. Texts are domain values (rule, part of speech, level)
 * that the screen turns into string resources; [GameTurnUi.clue] is content.
 */
@Immutable
internal data class GameUiState(
    /** True until the wheel is loaded and the game has started. */
    val isLoading: Boolean = true,
    /** Playable wheel letters in A–Z order (SKIPPED letters are not shown, `GAME_RULES.md` §1). */
    val wheel: ImmutableList<WheelLetterUi> = persistentListOf(),
    /** Running score (without time bonus). */
    val score: Int = 0,
    /** The current slot; after game over the last one stays visible until the screen leaves. */
    val turn: GameTurnUi? = null,
    val isPaused: Boolean = false,
    /** Keys, backspace and pass are accepted (playing, not paused, no wrong-answer reveal). */
    val inputEnabled: Boolean = false,
)

@Immutable
internal data class GameTurnUi(
    val letter: Char,
    val rule: LetterRule,
    val clue: String,
    val partOfSpeech: PartOfSpeech,
    val level: Level,
    /** Letters to type (spaces excluded). */
    val letterCount: Int,
    /** Answer cells per word; spaces of multi-word answers are fixed gaps between the words. */
    val words: ImmutableList<ImmutableList<AnswerCellUi>>,
    val keys: ImmutableList<KeyUi>,
    /** The correct answer of a wrong guess is shown (`GameConfig.revealMs`). */
    val isRevealing: Boolean,
)

@Immutable
internal data class AnswerCellUi(val letter: Char?, val state: AnswerSlotState)

@Immutable
internal data class KeyUi(val letter: Char, val state: LetterKeyState)

/** The game clock, a separate flow that changes every tick; only the HUD timer reads it (draw phase). */
@Immutable
internal data class GameClockUi(
    val isTimed: Boolean,
    val remainingMs: Long,
    /** Remaining part of the time limit, 0..1 (timer fill). */
    val fraction: Float,
    /** Remaining whole seconds, rounded up like the engine's time warning. */
    val wholeSeconds: Int,
    /** Last `GameConfig.timeWarningSeconds` seconds (motion.md #8). */
    val isWarning: Boolean,
) {
    companion object {
        val Untimed: GameClockUi =
            GameClockUi(isTimed = false, remainingMs = 0L, fraction = 0f, wholeSeconds = 0, isWarning = false)
    }
}

internal sealed interface GameIntent {
    data class KeyTapped(val index: Int) : GameIntent

    data object Backspace : GameIntent

    data object Pass : GameIntent

    /** Joker button; the joker sheet (`05-joker-sheet`) is a later M4 task. */
    data object OpenJokers : GameIntent

    data object Pause : GameIntent

    data object Resume : GameIntent

    /** "Bırak" in the pause dialog: gives up (`GameEvent.Forfeit`). */
    data object Forfeit : GameIntent

    /** Screen left RESUMED (ON_PAUSE): charge the time so far, stop the clock, pause. */
    data object Background : GameIntent

    /** Screen RESUMED: starts a loaded game the first time; a paused game stays paused. */
    data object Foreground : GameIntent
}

/**
 * One-off events. Motion effects only trigger transient motion (motion.md #2–#8) that starts and ends on the resting
 * look of [GameUiState]; navigation effects leave the screen.
 */
internal sealed interface GameEffect {
    data class LetterPlaced(val cellIndex: Int) : GameEffect

    data class Correct(val letter: Char, val points: Int) : GameEffect

    data class Wrong(val letter: Char) : GameEffect

    data class Passed(val letter: Char) : GameEffect

    data class Advanced(val letter: Char) : GameEffect

    data class ComboReached(val streak: Int) : GameEffect

    data class TimeWarning(val secondsLeft: Int) : GameEffect

    /** The finished game is recorded (`FinishGame`); show the result. */
    data object NavigateToResult : GameEffect

    /** The game can't be played (a restored game's card is no longer in the content); leave the screen. */
    data object Abandoned : GameEffect
}
