package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.Level

/** The line under the score of `20-duel-result`: why the winner won, or that someone gave up. */
internal enum class DuelExplanation {
    /** Decided by the correct count (and draws): no line. */
    None,

    /** Equal correct counts, fewer wrong answers wins. */
    TiebreakWrong,

    /** Equal correct and wrong counts, more time left wins. */
    TiebreakTime,

    /** A player gave up. */
    Forfeit,
}

/** One player's card on the result. [remainingSeconds] is rounded up like the handover card's clock. */
@Immutable
internal data class DuelResultPlayerUi(
    /** As typed; "" = the default name. */
    val name: String,
    val correct: Int,
    val wrong: Int,
    val passed: Int,
    val remainingSeconds: Int,
    val slots: List<DuelSlotOutcome>,
    val isWinner: Boolean,
)

/** What `20-duel-result` shows, derived from the finished match without any text. */
@Immutable
internal data class DuelResultUi(
    val level: Level,
    val end: DuelEnd,
    /** 0-based winner; null for a draw. */
    val winner: Int?,
    val players: List<DuelResultPlayerUi>,
    val explanation: DuelExplanation,
    /** After a forfeit: the 0-based player who gave up (the other one than the winner). */
    val forfeitedPlayer: Int?,
) {
    companion object {
        private const val MILLIS_PER_SECOND = 1_000L

        fun from(outcome: DuelOutcome): DuelResultUi {
            val players = outcome.players.mapIndexed { index, player ->
                DuelResultPlayerUi(
                    name = player.name,
                    correct = player.correct,
                    wrong = player.wrong,
                    passed = player.passed,
                    remainingSeconds = (
                        (player.remainingMs.coerceAtLeast(0L) + MILLIS_PER_SECOND - 1) /
                            MILLIS_PER_SECOND
                        ).toInt(),
                    slots = player.slots,
                    isWinner = outcome.winner == index,
                )
            }
            val winner = outcome.winner
            val forfeited = if (outcome.end == DuelEnd.FORFEIT && winner != null) 1 - winner else null
            val explanation = when {
                forfeited != null -> DuelExplanation.Forfeit

                winner == null -> DuelExplanation.None

                else -> {
                    val (first, second) = outcome.players
                    when {
                        first.correct != second.correct -> DuelExplanation.None
                        first.wrong != second.wrong -> DuelExplanation.TiebreakWrong
                        else -> DuelExplanation.TiebreakTime
                    }
                }
            }
            return DuelResultUi(outcome.level, outcome.end, winner, players, explanation, forfeited)
        }
    }
}
