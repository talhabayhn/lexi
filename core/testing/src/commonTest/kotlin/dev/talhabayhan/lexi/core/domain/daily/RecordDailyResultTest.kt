package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RecordDailyResultTest {
    private val clock = FakeAppClock()
    private val daily = FakeDailyRepository()
    private val recordDaily = RecordDailyResult(daily, clock)

    @Test
    fun `records first daily attempt of the day for a level`() = runTest {
        val today = clock.epochDay(clock.nowMs())
        val recorded = recordDaily(Level.A1, score = 300, timeMs = 75_000)

        assertTrue(recorded)
        val stored = daily.get(today, Level.A1)
        assertNotNull(stored)
        assertEquals(today, stored.epochDay)
        assertEquals(Level.A1, stored.level)
        assertEquals(300, stored.score)
        assertEquals(75_000L, stored.timeMs)
    }

    @Test
    fun `rejects second daily attempt of the same day for the same level`() = runTest {
        assertTrue(recordDaily(Level.A1, score = 300, timeMs = 75_000))
        assertFalse(recordDaily(Level.A1, score = 999, timeMs = 50_000))

        val today = clock.epochDay(clock.nowMs())
        val stored = daily.get(today, Level.A1)
        assertNotNull(stored)
        assertEquals(300, stored.score) // Unchanged
    }

    @Test
    fun `allows recording different levels on the same day`() = runTest {
        assertTrue(recordDaily(Level.A1, score = 300, timeMs = 75_000))
        assertTrue(recordDaily(Level.B1, score = 320, timeMs = 80_000))

        val today = clock.epochDay(clock.nowMs())
        assertNotNull(daily.get(today, Level.A1))
        assertNotNull(daily.get(today, Level.B1))
    }
}
