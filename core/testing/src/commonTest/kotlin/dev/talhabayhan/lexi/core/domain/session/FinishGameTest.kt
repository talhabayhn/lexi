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
import dev.talhabayhan.lexi.core.domain.srs.Srs
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
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
        type("PPLE") // A, B and C are given (STARTS slots).
        send(GameEvent.Tick(10_000L))
        send(GameEvent.Pass)
        type("HAIR")
        type("RDEA")
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

    // Regression (ADR 0019 Open M7): a restored finished game gets a new FinishGame instance.
    @Test
    fun `a game already recorded by an earlier instance is neither recorded nor applied to the notebook again`() =
        runTest {
            val game = finishedGame()
            finishGame(game, category = null)
            val afterFirst = progress.progress

            clock.advanceBy(60_000L)
            FinishGame(sessions, progress, clock, config)(game, category = null)

            assertEquals(1, sessions.sessions.size)
            assertEquals(afterFirst, progress.progress)
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

    @Test
    fun `finishing a daily game that crossed midnight saves under the start date`() = runTest {
        val startDay = 20_740L
        val finishDay = 20_741L
        val startMs = startDay * FakeAppClock.MS_PER_DAY + FakeAppClock.MS_PER_DAY - 60_000L // 23:59
        val endMs = finishDay * FakeAppClock.MS_PER_DAY + 60_000L // 00:01 next day

        clock.currentMs = endMs
        val daily = FakeDailyRepository()
        val finish = FinishGame(sessions, progress, clock, config, daily = daily)

        // Game started before midnight
        var state = engine.newGame(SEED, GameMode.DAILY, Level.A1, testWheel("APPLE", "BREAD", "CHAIR"))
        state = engine.reduce(state, GameEvent.Start(timestampMs = startMs)).state
        // End the game
        state = engine.reduce(state, GameEvent.Forfeit).state

        val result = finish(state, category = null)
        assertEquals(finishDay, result.epochDay) // session ended on finishDay

        // But daily result must be saved under startDay
        val dailyAtStart = daily.get(startDay)
        val dailyAtFinish = daily.get(finishDay)
        assertTrue(dailyAtStart != null, "daily must be saved under start day")
        assertEquals(startDay, dailyAtStart.epochDay)
        assertEquals(null, dailyAtFinish, "daily must not be saved under finish day")
    }

    @Test
    fun `a daily game is filed under the wheel's day and replaces the attempt stored at start`() = runTest {
        val wheelDay = 20_740L
        val daily = FakeDailyRepository(listOf(DailyResult(epochDay = wheelDay, score = 0, timeMs = 0L)))
        // Loaded at 23:59 of the wheel's day, started and finished on the next day.
        val startMs = (wheelDay + 1) * FakeAppClock.MS_PER_DAY + 60_000L
        clock.currentMs = startMs + 90_000L
        var state = engine.newGame(SEED, GameMode.DAILY, Level.A1, testWheel("APPLE", "BREAD"))
        state = engine.reduce(state, GameEvent.Start(timestampMs = startMs)).state
        state = engine.reduce(state, GameEvent.Forfeit).state

        FinishGame(sessions, progress, clock, config, daily = daily)(state, category = null, dailyEpochDay = wheelDay)

        assertEquals(DailyResult(epochDay = wheelDay, score = 0, timeMs = 90_000L), daily.get(wheelDay))
        assertEquals(null, daily.get(wheelDay + 1))
    }

    @Test
    fun `finishing a daily game without DailyRepository fails loudly`() = runTest {
        val finish = FinishGame(sessions, progress, clock, config, daily = null)
        val game = finishedGame(GameMode.DAILY)
        assertFailsWith<IllegalStateException> {
            finish(game, category = null)
        }
    }

    /** One BREAD slot answered wrong ("BRDEA"), then the reveal ends the game. */
    private fun wrongAnswerGame(mode: GameMode): GameState {
        var state = engine.newGame(SEED, mode, Level.A1, testWheel("BREAD"))
        fun send(event: GameEvent) {
            state = engine.reduce(state, event).state
        }
        send(GameEvent.Start(timestampMs = START_MS))
        "RDEA".forEach { letter ->
            val keys = checkNotNull(state.currentInput).keys
            send(
                GameEvent.KeyTapped(
                    keys.indices.first {
                        keys[it].letter == letter &&
                            keys[it].state == KeyState.AVAILABLE
                    },
                ),
            )
        }
        send(GameEvent.Tick(config.revealMs))
        return state
    }

    // Regression (notebook "Zor" audit): every recording mode must count a wrong answer in the row the notebook reads.
    @Test
    fun `a wrong answer increments wrong and enters the notebook in every recording mode`() = runTest {
        val recording = GameMode.entries - config.srsExcludedModes
        assertEquals(
            setOf(GameMode.CLASSIC, GameMode.PRACTICE, GameMode.HARDCORE, GameMode.DAILY, GameMode.REVIEW),
            recording.toSet(),
        )
        recording.forEach { mode ->
            val store =
                FakeProgressRepository(listOf(CardProgress(CardId("a1-bread"), box = 2, dueEpochDay = 0L, wrong = 1)))
            val finish = FinishGame(FakeSessionRepository(config), store, clock, config, daily = FakeDailyRepository())

            finish(wrongAnswerGame(mode), category = null)

            val bread = store.get(listOf(CardId("a1-bread"))).getValue(CardId("a1-bread"))
            assertEquals(2, bread.wrong, "wrong count in $mode")
            assertEquals(0, bread.box, "box in $mode")
            assertTrue(bread.wrong >= Srs.HARD_WRONGS, "hard in $mode")
        }
    }

    @Test
    fun `duel and level test answers never touch the wrong count`() = runTest {
        config.srsExcludedModes.forEach { mode ->
            val store =
                FakeProgressRepository(listOf(CardProgress(CardId("a1-bread"), box = 2, dueEpochDay = 0L, wrong = 1)))

            FinishGame(FakeSessionRepository(config), store, clock, config)(wrongAnswerGame(mode), category = null)

            assertEquals(1, store.progress.getValue(CardId("a1-bread")).wrong, "wrong count in $mode")
            assertEquals(0, store.saveCalls, "saves in $mode")
        }
    }

    private companion object {
        const val SEED = 42L
        const val START_MS = 1_000L
        const val END_MS = FakeAppClock.DEFAULT_NOW_MS
    }
}
