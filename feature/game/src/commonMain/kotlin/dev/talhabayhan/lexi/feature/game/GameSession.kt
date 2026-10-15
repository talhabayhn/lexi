package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.SavedGame
import dev.talhabayhan.lexi.core.domain.game.Transition
import dev.talhabayhan.lexi.core.domain.model.Card

/**
 * One running game (ADR 0019 §2): the [GameEngine], the current [state] and the append-only log of *applied* events
 * as a [SavedGame] (consecutive ticks coalesced), so replaying [saved] always rebuilds [state]. Ignored events
 * (same state instance, no effects) are not logged. Not thread-safe: the ViewModel uses it on the main thread.
 */
internal class GameSession private constructor(private val engine: GameEngine, initial: GameState, saved: SavedGame) {
    var state: GameState = initial
        private set

    /** Snapshot for process-death restore: initial wheel + applied events. */
    var saved: SavedGame = saved
        private set

    fun dispatch(event: GameEvent): Transition<GameState> {
        val transition = engine.reduce(state, event)
        if (!transition.isIgnored(before = state)) {
            state = transition.state
            saved = saved.append(event)
        }
        return transition
    }

    companion object {
        /**
         * An ignored event: the engine returned the very same state and no effects (`GameEngine.reduce`). Applied
         * events are logged and reach the screen; the ViewModel uses the same rule, so no effect is ever dropped.
         */
        fun Transition<GameState>.isIgnored(before: GameState): Boolean = state === before && effects.isEmpty()

        /** A new game from `GameEngine.newGame` (before `Start`). */
        fun start(engine: GameEngine, initial: GameState): GameSession =
            GameSession(engine, initial, SavedGame.of(initial))

        /** Replays [saved] over [cards]; null when the content changed and the game can't be rebuilt. */
        fun restore(engine: GameEngine, saved: SavedGame, cards: Collection<Card>): GameSession? =
            saved.restore(cards, engine)?.let { GameSession(engine, it, saved) }
    }
}
