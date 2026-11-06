package dev.talhabayhan.lexi.feature.game.duel

import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable

/** The duel setup (`17-duel-setup`): names, one shared level, the clock per player. */
@Serializable
data object DuelSetupRoute

/**
 * A hot-seat duel (`18-duel-handover`, `19-duel-game`, `GAME_RULES.md` §8). [player1] / [player2] are the names as
 * typed ("" = the default "1. Oyuncu"); they stay on the device. The level is an enum *name* (see `GameRoute`).
 */
@Serializable
data class DuelRoute(val level: String, val clockSeconds: Int, val player1: String, val player2: String) {
    constructor(level: Level, clockSeconds: Int, names: List<String>) :
        this(level.name, clockSeconds, names[0], names[1])

    internal val duelLevel: Level get() = Level.valueOf(level)

    internal val names: List<String> get() = listOf(player1, player2)
}

/**
 * The duel result (`20-duel-result`). The finished match travels in the route as flat primitives (nested data has no
 * built-in nav type on iOS); it is never stored (`GameConfig.srsExcludedModes`). [end] is a [DuelEnd] name, [winner]
 * the 0-based winner or -1 for a draw, `slotsN` one [DuelSlotOutcome.code] per playable letter in wheel order.
 */
@Serializable
data class DuelResultRoute(
    val level: String,
    val clockSeconds: Int,
    val end: String,
    val winner: Int,
    val name1: String,
    val name2: String,
    val correct1: Int,
    val correct2: Int,
    val wrong1: Int,
    val wrong2: Int,
    val passed1: Int,
    val passed2: Int,
    val remainingMs1: Long,
    val remainingMs2: Long,
    val slots1: String,
    val slots2: String,
) {
    internal val outcome: DuelOutcome
        get() = DuelOutcome(
            level = Level.valueOf(level),
            clockSeconds = clockSeconds,
            end = DuelEnd.valueOf(end),
            winner = winner.takeIf { it >= 0 },
            players = listOf(
                DuelPlayerOutcome(name1, correct1, wrong1, passed1, remainingMs1, DuelSlotOutcome.parse(slots1)),
                DuelPlayerOutcome(name2, correct2, wrong2, passed2, remainingMs2, DuelSlotOutcome.parse(slots2)),
            ),
        )

    companion object {
        internal fun of(outcome: DuelOutcome): DuelResultRoute {
            val (first, second) = outcome.players
            return DuelResultRoute(
                level = outcome.level.name,
                clockSeconds = outcome.clockSeconds,
                end = outcome.end.name,
                winner = outcome.winner ?: -1,
                name1 = first.name,
                name2 = second.name,
                correct1 = first.correct,
                correct2 = second.correct,
                wrong1 = first.wrong,
                wrong2 = second.wrong,
                passed1 = first.passed,
                passed2 = second.passed,
                remainingMs1 = first.remainingMs,
                remainingMs2 = second.remainingMs,
                slots1 = first.slots.joinToString("") { it.code.toString() },
                slots2 = second.slots.joinToString("") { it.code.toString() },
            )
        }
    }
}

/** How the match ended: the header reason of `20-duel-result`. */
internal enum class DuelEnd {
    /** Both wheels were completed: "ÇARK TAMAM". */
    ALL_ANSWERED,

    /** Both wheels are finished, at least one by its clock: "SÜRE BİTTİ". */
    TIME_UP,

    /** A player gave up: "PES ETTİ"; the other player wins (`GAME_RULES.md` §8). */
    FORFEIT,
}

/** Per-slot outcome of a player's wheel for the result strip. */
internal enum class DuelSlotOutcome(val code: Char) {
    CORRECT('c'),
    WRONG('w'),
    PASSED('p'),

    /** Not answered when the wheel ended (clock at 0). */
    OPEN('o'),
    ;

    companion object {
        fun parse(codes: String): List<DuelSlotOutcome> = codes.mapNotNull { code ->
            entries.firstOrNull {
                it.code ==
                    code
            }
        }
    }
}

internal data class DuelPlayerOutcome(
    /** As typed; "" = the default name. */
    val name: String,
    val correct: Int,
    val wrong: Int,
    val passed: Int,
    val remainingMs: Long,
    val slots: List<DuelSlotOutcome>,
)

/** The finished duel; [winner] is the 0-based player index, null for a draw. */
internal data class DuelOutcome(
    val level: Level,
    val clockSeconds: Int,
    val end: DuelEnd,
    val winner: Int?,
    val players: List<DuelPlayerOutcome>,
)
