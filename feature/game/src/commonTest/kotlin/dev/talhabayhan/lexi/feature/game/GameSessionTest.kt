package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.SavedGameCodec
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.core.testing.testWheel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class GameSessionTest {
    private val engine = GameEngine(GameConfig())
    private val cards = listOf(testCard("APPLE"), testCard("BREAD"), testCard("CHAIR"))

    private fun session() =
        GameSession.start(engine, engine.newGame(SEED, GameMode.CLASSIC, Level.A1, testWheel(cards)))

    private fun GameSession.type(word: String) = keyTaps(state, word).forEach { dispatch(it) }

    @Test
    fun `replaying the saved log rebuilds the live state`() {
        val session = session()
        session.dispatch(GameEvent.Start(1_000L))
        session.dispatch(GameEvent.Tick(100L))
        session.dispatch(GameEvent.Tick(250L))
        session.type("APP")
        session.dispatch(GameEvent.Backspace)
        session.dispatch(GameEvent.Pass)
        session.type("BRAED")
        session.dispatch(GameEvent.Tick(700L))
        session.dispatch(GameEvent.Tick(900L))
        session.dispatch(GameEvent.Pause)

        val decoded = checkNotNull(SavedGameCodec.decodeOrNull(SavedGameCodec.encode(session.saved)))

        assertEquals(session.state, checkNotNull(decoded.restore(cards, engine)))
        assertEquals(session.state, checkNotNull(GameSession.restore(engine, decoded, cards)).state)
    }

    @Test
    fun `ignored events are not logged and keep the same state`() {
        val session = session()
        session.dispatch(GameEvent.Start(0L))
        session.dispatch(GameEvent.Pause)
        val before = session.state
        val logged = session.saved.events

        val transition = session.dispatch(GameEvent.KeyTapped(0))

        assertSame(before, transition.state)
        assertEquals(logged, session.saved.events)
    }

    @Test
    fun `consecutive ticks are coalesced in the log`() {
        val session = session()
        session.dispatch(GameEvent.Start(0L))
        repeat(10) { session.dispatch(GameEvent.Tick(100L)) }

        assertEquals(listOf(GameEvent.Start(0L), GameEvent.Tick(1_000L)), session.saved.events)
    }

    @Test
    fun `a saved game whose card disappeared cannot be restored`() {
        val session = session()
        session.dispatch(GameEvent.Start(0L))

        assertNull(GameSession.restore(engine, session.saved, cards.drop(1)))
    }

    private companion object {
        const val SEED = 99L
    }
}
