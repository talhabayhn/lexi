package dev.talhabayhan.lexi.core.domain.rating

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeReviewRequester
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequestInAppReviewTest {
    private val clock = FakeAppClock()
    private val day = 86_400_000L
    private val settings = FakeSettingsRepository()
    private val sessions = FakeSessionRepository()
    private val analytics = FakeAnalyticsTracker()
    private val requester = FakeReviewRequester()
    private val request = RequestInAppReview(settings, sessions, requester, analytics, clock)

    /** Every helper session is its own game (a repeated mode + seed + start is the same game, stored once). */
    private var nextSeed = 1L

    private suspend fun play(times: Int, completed: Boolean = true, mode: GameMode = GameMode.CLASSIC) = repeat(times) {
        sessions.record(
            SessionResult(
                mode = mode,
                level = Level.A1,
                category = null,
                seed = nextSeed++,
                startedAtMs = clock.nowMs() - 60_000L,
                endedAtMs = clock.nowMs(),
                epochDay = clock.epochDay(clock.nowMs()),
                score = 100,
                correct = 10,
                wrong = 1,
                passed = 1,
                completed = completed,
            ),
        )
    }

    @Test
    fun `asks once and then waits out the window`() = runTest {
        settings.recordFirstLaunch(clock.nowMs())
        play(5)
        clock.advanceBy(3 * day)
        assertTrue(request())
        assertFalse(request())
        play(5)
        clock.advanceBy(119 * day)
        assertFalse(request())
        assertEquals(1, requester.requests)
        clock.advanceBy(day)
        assertTrue(request())
        assertEquals(2, requester.requests)
    }

    @Test
    fun `records the request time and the analytics event without parameters`() = runTest {
        settings.recordFirstLaunch(clock.nowMs())
        play(5)
        clock.advanceBy(3 * day)
        request()
        assertEquals(clock.nowMs(), settings.settings.first().ratingLastRequestMs)
        val event = analytics.events.single()
        assertEquals(AnalyticsEvent.RatingPromptRequested, event)
        assertEquals("rating_prompt_requested", event.name)
        assertTrue(event.params.isEmpty())
    }

    @Test
    fun `an unavailable api is neither remembered nor tracked`() = runTest {
        val unavailable = FakeReviewRequester(available = false)
        val useCase = RequestInAppReview(settings, sessions, unavailable, analytics, clock)
        settings.recordFirstLaunch(clock.nowMs())
        play(5)
        clock.advanceBy(3 * day)
        assertFalse(useCase())
        assertEquals(0L, settings.settings.first().ratingLastRequestMs)
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun `no request after a failed last game or too early`() = runTest {
        settings.recordFirstLaunch(clock.nowMs())
        play(5)
        assertFalse(request()) // installed today
        clock.advanceBy(3 * day)
        play(1, completed = false)
        assertFalse(request())
        assertEquals(0, requester.requests)
    }

    @Test
    fun `an upgrader without a stored first launch counts from the oldest game`() = runTest {
        play(5)
        clock.advanceBy(3 * day)
        assertTrue(request())
    }

    @Test
    fun `record first launch keeps the first value`() = runTest {
        settings.recordFirstLaunch(1_000L)
        settings.recordFirstLaunch(9_000L)
        assertEquals(1_000L, settings.settings.first().firstLaunchMs)
    }
}
