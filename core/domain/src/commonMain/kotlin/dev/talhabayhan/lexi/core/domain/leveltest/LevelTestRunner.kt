package dev.talhabayhan.lexi.core.domain.leveltest

import dev.talhabayhan.lexi.core.domain.game.EngineEffect
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.game.Transition
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable

/**
 * A running level test (`GAME_RULES.md` §9). Created by [LevelTestRunner.start], changed only by
 * [LevelTestRunner.reduce].
 */
@Serializable
public data class LevelTestRun(
    val test: LevelTestState,
    /** The one-slot game of the card being shown; null once the test is over (or was unavailable). */
    val game: GameState?,
    /**
     * Slots of the answered cards in asked order with their final state (CORRECT, WRONG or PASSED); `asked[i]` is
     * `test.answers[i]`. A card cut short by time up isn't answered and isn't here.
     */
    val asked: List<Slot>,
    /** Remaining time of the shared clock (⚙ `levelTestTimeLimitSeconds`). */
    val remainingMs: Long,
    /** The placement, set when the test is over. */
    val result: Level?,
) {
    public val isFinished: Boolean get() = result != null

    /** The pool had no card of the start level: no question was asked and there is no result (caller falls back). */
    public val isUnavailable: Boolean get() = result == null && game == null
}

/**
 * Pure reducer for the level test (`GAME_RULES.md` §9) on top of [GameEngine]: each card from [LevelTest.nextCard]
 * is one game of mode LEVEL_TEST on a one-slot [Wheel] (seed = the test seed, no jokers) that starts with the shared
 * clock's remaining time ([GameEvent.Start.remainingMs]). A correct answer ends the card at once, a wrong one after
 * its answer reveal, a pass at once (no second lap); the answer goes to [LevelTest.record] and the next card starts,
 * until [LevelTest.isFinished] or time up (result = [LevelTest.result] on the answers so far).
 *
 * Effects are the card games' effects except `GameOver` (a card ending is not the test ending; check
 * [LevelTestRun.isFinished]) and the `Advanced` of a passed card's second lap. Start, Forfeit (the caller discards
 * the run) and UseJoker events are ignored, as is everything once the run is finished or unavailable.
 */
public class LevelTestRunner(private val engine: GameEngine = GameEngine()) {
    private val limitMs: Long = checkNotNull(engine.config.timeLimitMs(GameMode.LEVEL_TEST))

    /** Starts the test with [seed]; [timestampMs] is only recorded in the card games' `startedAtMs`. */
    public fun start(seed: Long, pool: List<Card>, timestampMs: Long): Transition<LevelTestRun> {
        val test = LevelTest.start(seed)
        val empty = LevelTestRun(test = test, game = null, asked = emptyList(), remainingMs = limitMs, result = null)
        val card = LevelTest.nextCard(test, pool) ?: return Transition(empty, emptyList())
        val effects = mutableListOf<EngineEffect>()
        val run = empty.startCard(card, timestampMs, effects)
        return Transition(run, effects)
    }

    public fun reduce(run: LevelTestRun, pool: List<Card>, event: GameEvent): Transition<LevelTestRun> {
        val game = run.game
        val ignored = game == null || run.isFinished || event is GameEvent.Start || event == GameEvent.Forfeit ||
            event is GameEvent.UseJoker
        if (ignored) return Transition(run, emptyList())
        val transition = engine.reduce(checkNotNull(game), event)
        val after = transition.state
        if (after === game) return Transition(run, emptyList())

        val effects = transition.effects.filterNot { it is EngineEffect.GameOver }.toMutableList()
        val passed = transition.effects.any { it is EngineEffect.Passed }
        val clock = checkNotNull(after.remainingMs)
        val playing = run.copy(game = after, remainingMs = clock)
        val next = when {
            passed -> {
                effects.removeAll { it is EngineEffect.Advanced }
                playing.answered(after, SlotState.PASSED, pool, effects, leftoverMs = 0L)
            }

            after.status != GameStatus.FINISHED -> playing

            after.summary?.reason == GameOverReason.TIME_UP -> playing.finish()

            else -> {
                val state = after.wheel.slots.single().state
                playing.answered(after, state, pool, effects, leftoverMs = leftoverOf(game, event))
            }
        }
        return Transition(next, effects)
    }

    /**
     * Tick time left over after the answer reveal that ended a wrong card (the card's game drops it when it ends);
     * it is charged to the next card so ticks stay additive (`GAME_RULES.md` §12).
     */
    private fun leftoverOf(before: GameState, event: GameEvent): Long =
        if (event is GameEvent.Tick && before.isRevealing && !before.isPaused) {
            maxOf(0L, event.elapsedMs - before.revealRemainingMs)
        } else {
            0L
        }

    private fun LevelTestRun.answered(
        game: GameState,
        state: SlotState,
        pool: List<Card>,
        effects: MutableList<EngineEffect>,
        leftoverMs: Long,
    ): LevelTestRun {
        val card = checkNotNull(game.wheel.slots.single().card)
        val recorded = copy(
            test = LevelTest.record(test, card, correct = state == SlotState.CORRECT),
            asked = asked + Slot(card.letter, card, state),
        )
        val next = if (LevelTest.isFinished(recorded.test, pool)) null else LevelTest.nextCard(recorded.test, pool)
        if (next == null) return recorded.finish()
        val started = recorded.startCard(next, game.startedAtMs ?: 0L, effects)
        return if (leftoverMs > 0L) started.tick(leftoverMs, pool, effects) else started
    }

    private fun LevelTestRun.tick(ms: Long, pool: List<Card>, effects: MutableList<EngineEffect>): LevelTestRun {
        val transition = reduce(this, pool, GameEvent.Tick(ms))
        effects += transition.effects
        return transition.state
    }

    private fun LevelTestRun.startCard(
        card: Card,
        timestampMs: Long,
        effects: MutableList<EngineEffect>,
    ): LevelTestRun {
        val game = engine.newGame(
            seed = test.seed,
            mode = GameMode.LEVEL_TEST,
            level = card.level,
            wheel = Wheel(listOf(Slot(card.letter, card, SlotState.TODO))),
            jokers = JokerInventory(),
        )
        val started = engine.reduce(game, GameEvent.Start(timestampMs, remainingMs = remainingMs))
        effects += started.effects.filterNot { it is EngineEffect.GameOver }
        val run = copy(game = started.state, remainingMs = checkNotNull(started.state.remainingMs))
        return if (started.state.status == GameStatus.FINISHED) run.finish() else run
    }

    private fun LevelTestRun.finish(): LevelTestRun = copy(game = null, result = LevelTest.result(test))
}
