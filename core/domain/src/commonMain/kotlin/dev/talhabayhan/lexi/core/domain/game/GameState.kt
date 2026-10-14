package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable

@Serializable
public enum class GameStatus { NOT_STARTED, PLAYING, FINISHED }

@Serializable
public enum class GameOverReason {
    /** No TODO or PASSED slot left (the wheel is completed). */
    ALL_ANSWERED,
    TIME_UP,
    WRONG_IN_HARDCORE,
    FORFEIT,
}

/** A letter in an answer cell. Revealed letters (Reveal letter joker) are locked: Backspace skips them. */
@Serializable
public data class PlacedLetter(val letter: Char, val keyIndex: Int, val revealed: Boolean)

/**
 * Input of one slot. It is kept when the slot is passed, so typed letters, disabled keys and used jokers are still
 * there when the slot comes back on a later lap.
 */
@Serializable
public data class SlotInput(
    val keys: List<LetterKey>,
    /** One cell per answer letter (spaces excluded); null = empty. */
    val cells: List<PlacedLetter?>,
    /** Cell indices of non-revealed letters, in placement order (Backspace removes the last one). */
    val placementOrder: List<Int> = emptyList(),
    val jokersUsed: Set<JokerType> = emptySet(),
    /** The slot was passed before; if the game ends while it is CURRENT again, it still counts as PASSED. */
    val wasPassed: Boolean = false,
) {
    public val isFull: Boolean get() = cells.all { it != null }

    public val typed: String get() = cells.joinToString("") { it?.letter?.toString().orEmpty() }
}

/** Result shown on the result screen and stored as the session (`GAME_RULES.md` §6, §10). */
@Serializable
public data class GameSummary(
    val reason: GameOverReason,
    /** Final score: points − joker costs + time bonus, × Hardcore multiplier. */
    val score: Int,
    val timeBonus: Int,
    val completed: Boolean,
    val correct: Int,
    val wrong: Int,
    val passed: Int,
    val skipped: Int,
    /** TODO or CURRENT slots when the game ended. */
    val unanswered: Int,
    val longestStreak: Int,
    val remainingMs: Long?,
    val correctCards: List<CardId>,
    /** WRONG or PASSED cards ("Tekrar edilecekler"), in wheel order. */
    val missedCards: List<CardId>,
)

/** Full state of one player's game. Created by [GameEngine.newGame], changed only by [GameEngine.reduce]. */
@Serializable
public data class GameState(
    val seed: Long,
    val mode: GameMode,
    val level: Level,
    val wheel: Wheel,
    val jokers: JokerInventory,
    val status: GameStatus = GameStatus.NOT_STARTED,
    val isPaused: Boolean = false,
    val startedAtMs: Long? = null,
    /** Index into `wheel.slots`; stays on a WRONG slot while its answer is revealed. */
    val currentIndex: Int? = null,
    val lap: Int = 1,
    /** Input per slot index, created when a slot first becomes current. */
    val inputs: Map<Int, SlotInput> = emptyMap(),
    /** > 0 while the correct answer of a WRONG slot is shown; input is ignored and the clock stands still. */
    val revealRemainingMs: Long = 0L,
    /** Null in untimed modes. */
    val remainingMs: Long? = null,
    /** Running score (≥ 0) without time bonus and Hardcore multiplier. */
    val score: Int = 0,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val summary: GameSummary? = null,
) {
    public val currentSlot: Slot? get() = currentIndex?.let(wheel.slots::get)

    public val currentInput: SlotInput? get() = currentIndex?.let(inputs::get)

    public val isRevealing: Boolean get() = revealRemainingMs > 0
}

/** Result of one reducer step. */
public data class Transition<out S>(val state: S, val effects: List<EngineEffect>)
