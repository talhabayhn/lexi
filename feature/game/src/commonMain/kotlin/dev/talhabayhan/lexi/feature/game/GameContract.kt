package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
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
    /** Keys, backspace, pass and the joker button are accepted (playing, not paused, no wrong-answer reveal). */
    val inputEnabled: Boolean = false,
    /** The jokers in sheet order (`05-joker-sheet`) with count, cost and whether each can be used right now. */
    val jokers: ImmutableList<JokerUi> = persistentListOf(),
    /** Jokers one rewarded ad grants (`GameConfig.rewardedAdJokerReward`), for the sheet's ad button. */
    val jokerAdReward: Int = 0,
    /** The joker sheet is open; the clock keeps running (`GAME_RULES.md` §5). */
    val isJokerSheetOpen: Boolean = false,
    /** The pause dialog asks to confirm giving up ("Bırak"). Only while [isPaused]. */
    val isQuitConfirmation: Boolean = false,
)

/** One joker of the sheet. [enabled] = `GameEngine.canUseJoker` (inventory, one per slot, mode, level, cost). */
@Immutable
internal data class JokerUi(val type: JokerType, val count: Int, val cost: JokerCostUi, val enabled: Boolean)

/** What a joker costs in the current mode (`GameConfig.jokerCost`): "−10 sn", "−5 puan" or free. */
@Immutable
internal sealed interface JokerCostUi {
    data class Seconds(val seconds: Int) : JokerCostUi

    data class Points(val points: Int) : JokerCostUi

    data object Free : JokerCostUi
}

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
    /** The example sentence with the answer blanked, once the "Örnek cümle" joker was used on this slot. */
    val example: String? = null,
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

    /** Joker button: opens the joker sheet (`05-joker-sheet`); the clock keeps running. */
    data object OpenJokers : GameIntent

    /** Close button, scrim tap or back on the joker sheet. */
    data object CloseJokers : GameIntent

    /** A joker row of the sheet: `GameEvent.UseJoker`; the sheet closes when the joker was applied. */
    data class UseJoker(val type: JokerType) : GameIntent

    data object Pause : GameIntent

    data object Resume : GameIntent

    /** "Bırak" in the pause dialog: asks to confirm giving up. */
    data object RequestQuit : GameIntent

    /** "Vazgeç" in the quit confirmation: back to the pause dialog. */
    data object CancelQuit : GameIntent

    /** "Bırak" in the quit confirmation: gives up (`GameEvent.Forfeit`). */
    data object Forfeit : GameIntent

    /**
     * System back (both platforms): closes the joker sheet, leaves the quit confirmation, pauses a running game
     * (the pause dialog opens); on the pause dialog it does nothing, so the clock never restarts without a tap.
     */
    data object Back : GameIntent

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
    /**
     * #2: [letter] went from key [keyIndex] (index in [GameTurnUi.keys]) into answer cell [cellIndex] (letters only,
     * spaces are not cells). Also sent for a letter placed by the "Harf aç" joker (from the key it used).
     */
    data class LetterPlaced(val cellIndex: Int, val keyIndex: Int, val letter: Char) : GameEffect

    data class Correct(val letter: Char, val points: Int) : GameEffect

    data class Wrong(val letter: Char) : GameEffect

    data class Passed(val letter: Char) : GameEffect

    data class Advanced(val letter: Char) : GameEffect

    data class ComboReached(val streak: Int) : GameEffect

    data class TimeWarning(val secondsLeft: Int) : GameEffect

    /** The finished game is recorded (`FinishGame`); show the result. */
    data class NavigateToResult(val result: ResultRoute) : GameEffect

    /** The game can't be played (a restored game's card is no longer in the content); leave the screen. */
    data object Abandoned : GameEffect
}
