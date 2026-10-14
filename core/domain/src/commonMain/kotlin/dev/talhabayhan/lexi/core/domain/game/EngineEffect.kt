package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.CardId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One-off engine output (`GAME_RULES.md` §12), mapped by the ViewModel to sound, haptics and animation. The engine
 * never plays anything itself; the screen renders from the state.
 */
@Serializable
public sealed interface EngineEffect {
    /** A letter went into answer cell [cellIndex] (spaces are not cells). */
    @Serializable
    @SerialName("letterPlaced")
    public data class LetterPlaced(val cellIndex: Int, val letter: Char) : EngineEffect

    @Serializable
    @SerialName("correct")
    public data class Correct(val letter: Char, val card: CardId, val points: Int) : EngineEffect

    /** The correct answer is now revealed for `GameConfig.revealMs`. */
    @Serializable
    @SerialName("wrong")
    public data class Wrong(val letter: Char, val card: CardId) : EngineEffect

    @Serializable
    @SerialName("passed")
    public data class Passed(val letter: Char) : EngineEffect

    /** [toLetter] is the new current slot; [lap] starts at 1 and grows each time the wheel wraps after Z. */
    @Serializable
    @SerialName("advanced")
    public data class Advanced(val toLetter: Char, val lap: Int) : EngineEffect

    @Serializable
    @SerialName("combo")
    public data class ComboReached(val streak: Int) : EngineEffect

    /** Emitted once per whole second while ≤ `GameConfig.timeWarningSeconds` remain (10, 9, … 1). */
    @Serializable
    @SerialName("timeWarning")
    public data class TimeWarning(val secondsLeft: Int) : EngineEffect

    @Serializable
    @SerialName("jokerApplied")
    public data class JokerApplied(@SerialName("joker") val type: JokerType) : EngineEffect

    /** Duel: [player] (0-based) is up next; the handover card is shown until `MatchEvent.BeginTurn`. */
    @Serializable
    @SerialName("turnChanged")
    public data class TurnChanged(val player: Int) : EngineEffect

    @Serializable
    @SerialName("gameOver")
    public data class GameOver(val summary: GameSummary) : EngineEffect

    /** Duel: both games are over (or one player forfeited). */
    @Serializable
    @SerialName("matchOver")
    public data class MatchOver(val result: MatchResult) : EngineEffect
}
