package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import dev.talhabayhan.lexi.core.testing.testAlphabetPool
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetDailyWheelTest {
    private val clock = FakeAppClock()
    private val cards = FakeCardRepository(testAlphabetPool(Level.A1))
    private val contentVersion = FakeContentVersionRepository(1L)
    private val daily = FakeDailyRepository()
    private val getDailyWheel = GetDailyWheel(cards, contentVersion, daily, clock)

    @Test
    fun `returns Ready with deterministic seed and wheel when unplayed`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val state = getDailyWheel(Level.A1)

        assertIs<DailyPlayState.Ready>(state)
        assertEquals(today, state.epochDay)
        assertEquals(Level.A1, state.level)
        assertEquals(DailySeed.seed(today, Level.A1, 1L), state.seed)
        assertEquals(26, state.wheel.playableSlots.size)
    }

    @Test
    fun `returns AlreadyPlayed when daily attempt was recorded for that level`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val result = DailyResult(epochDay = today, level = Level.A1, score = 250, timeMs = 90_000)
        daily.record(result)

        val state = getDailyWheel(Level.A1)

        assertIs<DailyPlayState.AlreadyPlayed>(state)
        assertEquals(today, state.epochDay)
        assertEquals(Level.A1, state.level)
        assertEquals(result, state.result)
    }

    @Test
    fun `returns Ready for another level even if one level was already played`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val resultA1 = DailyResult(epochDay = today, level = Level.A1, score = 250, timeMs = 90_000)
        daily.record(resultA1)

        cards.cards = testAlphabetPool(Level.A1) + testAlphabetPool(Level.B1)
        val stateB1 = getDailyWheel(Level.B1)

        assertIs<DailyPlayState.Ready>(stateB1)
        assertEquals(Level.B1, stateB1.level)
    }

    @Test
    fun `returns NoCards when pool is empty for that level`() = runTest {
        cards.cards = emptyList()
        val state = getDailyWheel(Level.A1)
        assertEquals(DailyPlayState.NoCards, state)
    }
}
