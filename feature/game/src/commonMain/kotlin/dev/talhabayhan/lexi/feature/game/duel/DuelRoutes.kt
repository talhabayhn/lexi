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
 * the 0-based winner or -1 for a draw, `slotsN` one [DuelSlotOutcome.code] per playable letter in wheel order and
 * `cardsN` the card id of each of those letters (same order; empty in a route saved by a build before 1.0.12). Card ids
 * only, like `ResultRoute`: the answer review reads clue and example from the content by id.
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
    val cards1: List<String> = emptyList(),
    val cards2: List<String> = emptyList(),
) {
    internal val outcome: DuelOutcome
        get() = DuelOutcome(
            level = Level.valueOf(level),
            clockSeconds = clockSeconds,
            end = DuelEnd.valueOf(end),
            winner = winner.takeIf { it >= 0 },
            players = listOf(
                DuelPlayerOutcome(
                    name1,
                    correct1,
                    wrong1,
                    passed1,
                    remainingMs1,
                    DuelSlotOutcome.parse(slots1),
                    cards1,
                ),
                DuelPlayerOutcome(
                    name2,
                    correct2,
                    wrong2,
                    passed2,
                    remainingMs2,
                    DuelSlotOutcome.parse(slots2),
                    cards2,
                ),
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
                cards1 = first.cards,
                cards2 = second.cards,
            )
        }
    }
}

/**
 * One player's answer review after a duel (opened from a player card of `20-duel-result`; needs design, it reuses the
 * review list of `06-result`). [player] is 0-based, [name] as typed ("" = the default name), [end] a [DuelEnd] name.
 * The card ids of the letters the player answered correctly, wrongly or passed, each in wheel order; letters the
 * player never reached (the clock ran out, a forfeit) are left out.
 */
@Serializable
data class DuelReviewRoute(
    val level: String,
    val end: String,
    val player: Int,
    val name: String,
    val correctCards: List<String> = emptyList(),
    val wrongCards: List<String> = emptyList(),
    val passedCards: List<String> = emptyList(),
) {
    internal val duelLevel: Level get() = Level.valueOf(level)

    internal val duelEnd: DuelEnd get() = DuelEnd.valueOf(end)

    internal companion object {
        /** The review of [player] (0-based) in the finished [outcome]. */
        fun of(outcome: DuelOutcome, player: Int): DuelReviewRoute {
            val played = outcome.players[player]
            val pairs = played.slots.zip(played.cards).filter { (_, card) -> card.isNotEmpty() }
            fun cardsWith(slot: DuelSlotOutcome) = pairs.filter { it.first == slot }.map { it.second }
            return DuelReviewRoute(
                level = outcome.level.name,
                end = outcome.end.name,
                player = player,
                name = played.name,
                correctCards = cardsWith(DuelSlotOutcome.CORRECT),
                wrongCards = cardsWith(DuelSlotOutcome.WRONG),
                passedCards = cardsWith(DuelSlotOutcome.PASSED),
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
    /** The card id of each of [slots], same order (empty when the route predates 1.0.12). */
    val cards: List<String> = emptyList(),
)

/** The finished duel; [winner] is the 0-based player index, null for a draw. */
internal data class DuelOutcome(
    val level: Level,
    val clockSeconds: Int,
    val end: DuelEnd,
    val winner: Int?,
    val players: List<DuelPlayerOutcome>,
)
