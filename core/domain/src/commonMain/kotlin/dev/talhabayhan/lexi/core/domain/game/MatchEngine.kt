package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.Seeds

/**
 * Pure reducer for the hot-seat duel (`GAME_RULES.md` §8) on top of [GameEngine]. Each player has an own wheel and
 * clock; events go to the active player only, so the other clock stands still. A correct answer keeps the turn; a
 * pass, a wrong answer (once its reveal is over) or a finished game hands over to the other player, unless that
 * player is already finished.
 */
public class MatchEngine(public val config: GameConfig = GameConfig()) {
    private val games = GameEngine(config)

    /**
     * [wheels] are the players' wheels, generated with `SeededRandom(playerSeed(seed, i)).derive("wheel")`. Jokers
     * are not allowed in the duel, so players start with an empty inventory.
     */
    public fun newMatch(seed: Long, level: Level, wheels: List<Wheel>): MatchState {
        require(wheels.size == PLAYERS) { "a duel has $PLAYERS players, got ${wheels.size} wheels" }
        val players = wheels.mapIndexed { index, wheel ->
            games.newGame(seed = playerSeed(seed, index), mode = GameMode.DUEL, level = level, wheel = wheel)
        }
        return MatchState(seed = seed, players = players)
    }

    public fun reduce(state: MatchState, event: MatchEvent): Transition<MatchState> {
        val effects = mutableListOf<EngineEffect>()
        val next = when {
            state.phase == MatchPhase.FINISHED -> state

            event is MatchEvent.BeginTurn -> if (state.phase ==
                MatchPhase.HANDOVER
            ) {
                state.beginTurn(event, effects)
            } else {
                state
            }

            state.phase == MatchPhase.HANDOVER -> state

            event is MatchEvent.Play -> state.play(event.event, effects)

            else -> state
        }
        return if (effects.isEmpty() && next == state) Transition(state, emptyList()) else Transition(next, effects)
    }

    private fun MatchState.beginTurn(event: MatchEvent.BeginTurn, fx: MutableList<EngineEffect>): MatchState {
        val player = players[activePlayer]
        val playing = copy(phase = MatchPhase.PLAYING)
        if (player.status != GameStatus.NOT_STARTED) return playing
        val started = games.reduce(player, GameEvent.Start(event.timestampMs))
        fx += started.effects
        val next = playing.withActive(started.state)
        return if (started.state.status == GameStatus.FINISHED) next.endTurn(fx) else next
    }

    private fun MatchState.play(event: GameEvent, fx: MutableList<EngineEffect>): MatchState {
        if (event is GameEvent.Start) return this
        val before = players[activePlayer]
        val transition = games.reduce(before, event)
        val after = transition.state
        if (after === before) return this
        fx += transition.effects
        val next = withActive(after)
        if (event == GameEvent.Forfeit && after.status == GameStatus.FINISHED) {
            return next.finish(MatchResult(MatchEndReason.FORFEIT, winner = other(activePlayer)), fx)
        }
        val revealOver =
            !after.isRevealing && (before.isRevealing || transition.effects.any { it is EngineEffect.Wrong })
        val passed = transition.effects.any { it is EngineEffect.Passed }
        return if (after.status == GameStatus.FINISHED || passed || revealOver) next.endTurn(fx) else next
    }

    private fun MatchState.endTurn(fx: MutableList<EngineEffect>): MatchState {
        if (players.all { it.status == GameStatus.FINISHED }) {
            val (first, second) = players.map { checkNotNull(it.summary) }
            return finish(MatchResult(MatchEndReason.ALL_FINISHED, winner = winnerOf(first, second)), fx)
        }
        val next = other(activePlayer)
        if (players[next].status == GameStatus.FINISHED) return this
        fx += EngineEffect.TurnChanged(next)
        return copy(activePlayer = next, phase = MatchPhase.HANDOVER)
    }

    private fun MatchState.finish(result: MatchResult, fx: MutableList<EngineEffect>): MatchState {
        fx += EngineEffect.MatchOver(result)
        return copy(phase = MatchPhase.FINISHED, result = result)
    }

    private fun MatchState.withActive(game: GameState): MatchState =
        copy(players = players.toMutableList().also { it[activePlayer] = game })

    public companion object {
        public const val PLAYERS: Int = 2

        /** Seed of player [index] (0-based): `Seeds.derive(seed, "player:<index + 1>")` (`GAME_RULES.md` §13). */
        public fun playerSeed(seed: Long, index: Int): Long = Seeds.derive(seed, "player:${index + 1}")

        /** More correct wins; tie → fewer wrong; tie → more remaining time; else null (draw). */
        public fun winnerOf(first: GameSummary, second: GameSummary): Int? {
            val order = compareBy<GameSummary>({ it.correct }, { -it.wrong }, { it.remainingMs ?: 0L })
            val comparison = order.compare(first, second)
            return when {
                comparison > 0 -> 0
                comparison < 0 -> 1
                else -> null
            }
        }

        private fun other(player: Int): Int = (player + 1) % PLAYERS
    }
}
