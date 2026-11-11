package dev.talhabayhan.lexi.feature.game.duel

import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.MatchEndReason
import dev.talhabayhan.lexi.core.domain.game.MatchState
import dev.talhabayhan.lexi.core.domain.game.SlotState

/** The finished match as the result screen shows it (`20-duel-result`). */
internal object DuelOutcomes {
    /**
     * [timeLimitMs] is the clock per player the engine played with (also the outcome's clock, so "play again" never
     * repeats a stale route value). After a forfeit the other player's game may still be running (or not
     * started): their counts come from the wheel and their clock stands where it was.
     */
    fun of(match: MatchState, route: DuelRoute, timeLimitMs: Long): DuelOutcome {
        val result = checkNotNull(match.result) { "the match is not finished" }
        val players = match.players.mapIndexed { index, game ->
            val summary = game.summary
            val slots = game.wheel.slots
            val playable = slots.filter { it.isPlayable }
            DuelPlayerOutcome(
                name = route.names[index],
                correct = summary?.correct ?: slots.count { it.state == SlotState.CORRECT },
                wrong = summary?.wrong ?: slots.count { it.state == SlotState.WRONG },
                passed = summary?.passed ?: slots.count { it.state == SlotState.PASSED },
                remainingMs = (summary?.remainingMs ?: game.remainingMs ?: timeLimitMs).coerceAtLeast(0L),
                slots = playable.map {
                    when (it.state) {
                        SlotState.CORRECT -> DuelSlotOutcome.CORRECT
                        SlotState.WRONG -> DuelSlotOutcome.WRONG
                        SlotState.PASSED -> DuelSlotOutcome.PASSED
                        SlotState.TODO, SlotState.CURRENT, SlotState.SKIPPED -> DuelSlotOutcome.OPEN
                    }
                },
                cards = playable.map { checkNotNull(it.card).id.value },
            )
        }
        val end = when {
            result.reason == MatchEndReason.FORFEIT -> DuelEnd.FORFEIT
            match.players.all { it.summary?.reason == GameOverReason.ALL_ANSWERED } -> DuelEnd.ALL_ANSWERED
            else -> DuelEnd.TIME_UP
        }
        return DuelOutcome(
            level = match.players.first().level,
            clockSeconds = (timeLimitMs / MS_PER_SECOND).toInt(),
            end = end,
            winner = result.winner,
            players = players,
        )
    }
}

private const val MS_PER_SECOND = 1_000L
