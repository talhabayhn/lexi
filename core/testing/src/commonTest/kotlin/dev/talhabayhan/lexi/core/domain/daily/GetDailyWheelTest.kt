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
    private val cards = FakeCardRepository(Level.entries.flatMap { testAlphabetPool(it) })
    private val contentVersion = FakeContentVersionRepository(1L)
    private val daily = FakeDailyRepository()
    private val getDailyWheel = GetDailyWheel(cards, contentVersion, daily, clock)

    @Test
    fun `returns Ready with deterministic seed and wheel when unplayed`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val state = getDailyWheel()

        assertIs<DailyPlayState.Ready>(state)
        assertEquals(today, state.epochDay)
        assertEquals(DailySeed.seed(today, 1L), state.seed)
        assertEquals(26, state.wheel.playableSlots.size)
    }

    @Test
    fun `returns AlreadyPlayed when daily attempt was recorded today`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val result = DailyResult(epochDay = today, level = null, score = 250, timeMs = 90_000)
        daily.record(result)

        val state = getDailyWheel()

        assertIs<DailyPlayState.AlreadyPlayed>(state)
        assertEquals(today, state.epochDay)
        assertEquals(result, state.result)
    }

    @Test
    fun `returns Ready for next day after today was played`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val result = DailyResult(epochDay = today, level = null, score = 250, timeMs = 90_000)
        daily.record(result)

        clock.advanceBy(FakeAppClock.MS_PER_DAY)
        val state = getDailyWheel()

        assertIs<DailyPlayState.Ready>(state)
        assertEquals(today + 1, state.epochDay)
    }

    @Test
    fun `returns NoCards when pool is empty`() = runTest {
        cards.cards = emptyList()
        val state = getDailyWheel()
        assertEquals(DailyPlayState.NoCards, state)
    }
}
