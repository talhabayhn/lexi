package dev.talhabayhan.lexi.core.domain.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Engine input (`GAME_RULES.md` §12). Events that don't apply in the current state (e.g. a key tap while paused)
 * are ignored: the state is returned unchanged with no effects. Serializable so event logs can be replayed.
 */
@Serializable
public sealed interface GameEvent {
    /** Starts the game; [timestampMs] is only recorded (wall clock for the session record). */
    @Serializable
    @SerialName("start")
    public data class Start(val timestampMs: Long) : GameEvent

    /** Index into the current slot's keys. */
    @Serializable
    @SerialName("key")
    public data class KeyTapped(val keyIndex: Int) : GameEvent

    @Serializable
    @SerialName("backspace")
    public data object Backspace : GameEvent

    @Serializable
    @SerialName("pass")
    public data object Pass : GameEvent

    @Serializable
    @SerialName("joker")
    public data class UseJoker(@SerialName("joker") val type: JokerType) : GameEvent

    /** Time passed since the previous tick (a delta, not a timestamp). The only way time enters the engine. */
    @Serializable
    @SerialName("tick")
    public data class Tick(val elapsedMs: Long) : GameEvent

    @Serializable
    @SerialName("pause")
    public data object Pause : GameEvent

    @Serializable
    @SerialName("resume")
    public data object Resume : GameEvent

    /** Give up: ends the game (also before Start); every remaining letter counts as WRONG. */
    @Serializable
    @SerialName("forfeit")
    public data object Forfeit : GameEvent
}
