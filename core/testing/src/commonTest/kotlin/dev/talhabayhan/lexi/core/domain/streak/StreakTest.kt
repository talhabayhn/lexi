package dev.talhabayhan.lexi.core.domain.streak

import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreakTest {
    private val clock = FakeAppClock()
    private val streaks = FakeStreakRepository()
    private val getStreakStatus = GetStreakStatus(streaks, clock)

    @Test
    fun `increments on first completed game of a local day`() = runTest {
        val today = clock.epochDay(clock.nowMs())

        // Initial: 0 streak
        assertEquals(0, Stats.currentStreak(streaks.days, today))
        assertEquals(0, StreakSemantics.homeBadgeStreak(streaks.days, today))

        // First game on today
        streaks.days = listOf(today)
        val status1 = getStreakStatus.current()
        assertEquals(1, status1.currentStreak)
        assertTrue(status1.playedToday)

        // Second game on today does not double-increment
        streaks.days = listOf(today) // idempotent
        val status2 = getStreakStatus.current()
        assertEquals(1, status2.currentStreak)

        // Next day completed
        val tomorrow = today + 1
        clock.advanceBy(FakeAppClock.MS_PER_DAY)
        streaks.days = listOf(today, tomorrow)
        val status3 = getStreakStatus.current()
        assertEquals(2, status3.currentStreak)
        assertTrue(status3.playedToday)
    }

    @Test
    fun `resets after a missed day`() = runTest {
        val today = clock.epochDay(clock.nowMs())

        // Played yesterday and day before
        streaks.days = listOf(today - 2, today - 1)

        // Today is not played yet: streak is still alive from yesterday
        val statusToday = getStreakStatus.current()
        assertEquals(2, statusToday.currentStreak)
        assertFalse(statusToday.playedToday)

        // Two days pass with no game played (today was missed)
        clock.advanceBy(FakeAppClock.MS_PER_DAY * 2)
        val afterMissed = getStreakStatus.current()
        assertEquals(0, afterMissed.currentStreak)
        assertFalse(afterMissed.playedToday)

        // Playing now starts a new streak of 1
        val newToday = clock.epochDay(clock.nowMs())
        streaks.days = streaks.days + newToday
        val statusRecovered = getStreakStatus.current()
        assertEquals(1, statusRecovered.currentStreak)
        assertTrue(statusRecovered.playedToday)
    }

    @Test
    fun `home badge displays correct streak value`() = runTest {
        val today = 20_000L
        assertEquals(0, StreakSemantics.homeBadgeStreak(emptyList(), today))
        assertEquals(1, StreakSemantics.homeBadgeStreak(listOf(today), today))
        assertEquals(1, StreakSemantics.homeBadgeStreak(listOf(today - 1), today), "yesterday was played, today open")
        assertEquals(0, StreakSemantics.homeBadgeStreak(listOf(today - 2), today), "missed yesterday and today")
        assertEquals(7, StreakSemantics.homeBadgeStreak((today - 6..today).toList(), today))
    }

    @Test
    fun `combo celebration and badge text semantics from GAME_RULES`() {
        assertEquals(setOf(3, 5, 10), StreakSemantics.COMBO_CELEBRATIONS)
        assertEquals("×3 SERİ", StreakSemantics.comboBadgeText(3))
        assertEquals("×5 SERİ", StreakSemantics.comboBadgeText(5))
        assertEquals("×10 SERİ", StreakSemantics.comboBadgeText(10))
    }

    @Test
    fun `play before and after midnight increments streak consecutively`() = runTest {
        // Instant at 23:59:50 local time on day D
        val msPerDay = FakeAppClock.MS_PER_DAY
        val dayD = 20_000L
        val justBeforeMidnight = dayD * msPerDay + msPerDay - 10_000L
        val justAfterMidnight = dayD * msPerDay + msPerDay + 10_000L

        clock.currentMs = justBeforeMidnight
        val epochBefore = clock.epochDay(justBeforeMidnight)
        assertEquals(dayD, epochBefore)
        streaks.days = listOf(epochBefore)

        clock.currentMs = justAfterMidnight
        val epochAfter = clock.epochDay(justAfterMidnight)
        assertEquals(dayD + 1, epochAfter)
        streaks.days = listOf(epochBefore, epochAfter)

        val status = getStreakStatus.current()
        assertEquals(2, status.currentStreak)
        assertTrue(status.playedToday)
    }

    @Test
    fun `travelling timezones uses stored local date at play time`() = runTest {
        // Tokyo: UTC+9 (9 * 3600 * 1000 ms offset)
        val tokyoOffset = 9 * 3600 * 1000L
        val tokyoClock = FakeAppClock(utcOffsetMs = tokyoOffset)
        // 2026-10-17 20:00:00 Tokyo time
        val tokyoInstant = 20_743L * FakeAppClock.MS_PER_DAY - tokyoOffset + 20 * 3600 * 1000L
        tokyoClock.currentMs = tokyoInstant
        val tokyoLocalDay = tokyoClock.epochDay(tokyoInstant)
        assertEquals(20_743L, tokyoLocalDay)

        // Played in Tokyo: stored day 20_743
        val streakRepo = FakeStreakRepository(listOf(tokyoLocalDay))

        // London: UTC+0
        // 10 hours later, the player is in London where it is still local day 20_743 (16:00:00)
        val londonOffset = 0L
        val londonClock = FakeAppClock(utcOffsetMs = londonOffset)
        val londonInstant1 = tokyoInstant + 10 * 3600 * 1000L
        londonClock.currentMs = londonInstant1
        val londonLocalDay1 = londonClock.epochDay(londonInstant1)
        assertEquals(20_743L, londonLocalDay1)

        // Playing again on the same local date in London is recognized as same day (no duplicate streak increment)
        val londonStatus1 = GetStreakStatus(streakRepo, londonClock).current()
        assertEquals(1, londonStatus1.currentStreak)
        assertTrue(londonStatus1.playedToday)

        // Next morning in London (local day 20_744)
        val londonInstant2 = londonInstant1 + 16 * 3600 * 1000L
        londonClock.currentMs = londonInstant2
        val londonLocalDay2 = londonClock.epochDay(londonInstant2)
        assertEquals(20_744L, londonLocalDay2)

        // Player completes a game in London next morning: consecutive local days -> streak increments to 2
        streakRepo.days = streakRepo.days + londonLocalDay2
        val londonStatus2 = GetStreakStatus(streakRepo, londonClock).current()
        assertEquals(2, londonStatus2.currentStreak)
        assertTrue(londonStatus2.playedToday)
    }
}
