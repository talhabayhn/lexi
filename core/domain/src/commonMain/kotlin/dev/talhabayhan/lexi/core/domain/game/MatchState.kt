package dev.talhabayhan.lexi.core.domain.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class MatchPhase {
    /** The "Sıra: <name>" card is shown; nothing runs until [MatchEvent.BeginTurn]. */
    HANDOVER,
    PLAYING,
    FINISHED,
}

@Serializable
public enum class MatchEndReason {
    /** Both wheels are finished (completed or clock at 0). */
    ALL_FINISHED,
    FORFEIT,
}

/** [winner] is the 0-based player index, or null for a draw. Per-player details are in their [GameSummary]. */
@Serializable
public data class MatchResult(val reason: MatchEndReason, val winner: Int?)

/** Hot-seat duel (`GAME_RULES.md` §8): one [GameState] per player plus turn info. Online rooms reuse it (v1.1). */
@Serializable
public data class MatchState(
    val seed: Long,
    val players: List<GameState>,
    val activePlayer: Int = 0,
    val phase: MatchPhase = MatchPhase.HANDOVER,
    val result: MatchResult? = null,
)

@Serializable
public sealed interface MatchEvent {
    /** The active player dismissed the handover card; their game starts on their first turn. */
    @Serializable
    @SerialName("beginTurn")
    public data class BeginTurn(val timestampMs: Long) : MatchEvent

    /** A game event of the active player ([GameEvent.Start] is ignored: turns start with [BeginTurn]). */
    @Serializable
    @SerialName("play")
    public data class Play(val event: GameEvent) : MatchEvent
}
