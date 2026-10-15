package dev.talhabayhan.lexi.core.domain.session

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.KeyState
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.testWheel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// Lives in core:testing (not core:domain) because it needs the repository fakes, and core:domain must not depend on
// core:testing (module rules: core:testing already depends on core:domain).
class FinishGameTest {
    private val config = GameConfig()
    private val engine = GameEngine(config)
    private val clock = FakeAppClock(currentMs = END_MS)
    private val sessions = FakeSessionRepository(config)
    private val progress = FakeProgressRepository(
        listOf(CardProgress(CardId("a1-chair"), box = 1, dueEpochDay = 0L, seen = 1, wrong = 1, lastSeenMs = 1L)),
    )
    private val finishGame = FinishGame(sessions, progress, clock, config)

    /** APPLE correct, BREAD passed, CHAIR (notebook box 1) correct, BREAD wrong on lap 2: completed. */
    private fun finishedGame(mode: GameMode = GameMode.CLASSIC): GameState {
        var state = engine.newGame(SEED, mode, Level.A1, testWheel("APPLE", "BREAD", "CHAIR"))
        fun send(event: GameEvent) {
            state = engine.reduce(state, event).state
        }

        fun type(word: String) = word.forEach { letter ->
            val keys = checkNotNull(state.currentInput).keys
            val index = keys.indices.first { keys[it].letter == letter && keys[it].state == KeyState.AVAILABLE }
            send(GameEvent.KeyTapped(index))
        }
        send(GameEvent.Start(timestampMs = START_MS))
        type("APPLE")
        send(GameEvent.Tick(10_000L))
        send(GameEvent.Pass)
        type("CHAIR")
        type("DREAB")
        send(GameEvent.Tick(config.revealMs))
        return state
    }

    @Test
    fun `finishing a game records the session with end time and day from the clock`() = runTest {
        val game = finishedGame()

        val result = finishGame(game, CategoryId("food-drink"))

        val expected = SessionResult(
            mode = GameMode.CLASSIC,
            level = Level.A1,
            category = CategoryId("food-drink"),
            seed = SEED,
            startedAtMs = START_MS,
            endedAtMs = END_MS,
            epochDay = clock.epochDay(END_MS),
            score = checkNotNull(game.summary).score,
            correct = 2,
            wrong = 1,
            passed = 0,
            completed = true,
        )
        assertEquals(expected, result)
        assertEquals(listOf(expected), sessions.sessions)
    }

    @Test
    fun `finishing a game updates the notebook from the final slot states`() = runTest {
        finishGame(finishedGame(), category = null)

        val day = clock.epochDay(END_MS)
        // Missed BREAD enters box 0, due today; CHAIR moves up from box 1; first-time correct APPLE stays outside.
        assertEquals(0, progress.progress.getValue(CardId("a1-bread")).box)
        assertEquals(day, progress.progress.getValue(CardId("a1-bread")).dueEpochDay)
        assertEquals(2, progress.progress.getValue(CardId("a1-chair")).box)
        assertEquals(null, progress.progress.getValue(CardId("a1-apple")).box)
        assertEquals(END_MS, progress.progress.getValue(CardId("a1-apple")).lastSeenMs)
        assertEquals(1, progress.saveCalls)
    }

    @Test
    fun `finishing the same game twice records it only once`() = runTest {
        val game = finishedGame()

        val first = finishGame(game, category = null)
        clock.advanceBy(60_000L)
        val second = finishGame(game, category = null)

        assertEquals(first, second)
        assertEquals(1, sessions.recordCalls)
        assertEquals(1, progress.saveCalls)
    }

    @Test
    fun `a failed record is retried by the next call and the notebook is updated once`() = runTest {
        val game = finishedGame()
        sessions.failingRecords = 1

        assertFailsWith<IllegalStateException> { finishGame(game, category = null) }
        assertEquals(0, progress.saveCalls)
        val result = finishGame(game, category = null)

        assertEquals(listOf(result), sessions.sessions)
        assertEquals(2, sessions.recordCalls)
        assertEquals(1, progress.saveCalls)
        // A third call writes nothing again.
        finishGame(game, category = null)
        assertEquals(2, sessions.recordCalls)
        assertEquals(1, progress.saveCalls)
    }

    @Test
    fun `one FinishGame instance refuses a different game`() = runTest {
        finishGame(finishedGame(), category = null)

        assertFailsWith<IllegalStateException> { finishGame(finishedGame(GameMode.PRACTICE), category = null) }
    }

    @Test
    fun `an unfinished game is not recorded`() = runTest {
        val playing = engine.reduce(
            engine.newGame(SEED, GameMode.CLASSIC, Level.A1, testWheel("APPLE")),
            GameEvent.Start(START_MS),
        ).state

        assertFailsWith<IllegalArgumentException> { finishGame(playing, category = null) }
        assertEquals(0, sessions.recordCalls)
    }

    @Test
    fun `hot-seat duel games change neither sessions nor the notebook as srsExcludedModes`() = runTest {
        val before = progress.progress

        finishGame(finishedGame(GameMode.DUEL), category = null)

        assertTrue(sessions.sessions.isEmpty())
        assertEquals(before, progress.progress)
        assertEquals(0, progress.saveCalls)
    }

    @Test
    fun `a forfeit before start uses the end time as start time`() = runTest {
        val game = engine.reduce(
            engine.newGame(SEED, GameMode.CLASSIC, Level.A1, testWheel("APPLE")),
            GameEvent.Forfeit,
        ).state

        val result = finishGame(game, category = null)

        assertEquals(END_MS, result.startedAtMs)
        assertEquals(1, result.wrong)
    }

    @Test
    fun `the day is the end day in the player's time zone`() = runTest {
        // 22:30 UTC on day N is already day N + 1 at UTC+3.
        val atMs = 20_740L * FakeAppClock.MS_PER_DAY + 81_000_000L
        val istanbul = FakeAppClock(currentMs = atMs, utcOffsetMs = 3 * 3_600_000L)

        val result = FinishGame(sessions, progress, istanbul, config)(finishedGame(), category = null)

        assertEquals(20_741L, result.epochDay)
    }

    private companion object {
        const val SEED = 42L
        const val START_MS = 1_000L
        const val END_MS = FakeAppClock.DEFAULT_NOW_MS
    }
}
