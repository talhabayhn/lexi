package dev.talhabayhan.lexi.core.domain.rating

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RatingPromptPolicyTest {
    private val defaultToday = 1000L

    private class FakeClock(var epochDay: Long = 1000L) : AppClock {
        override fun nowMs(): Long = epochDay * 86_400_000L
        override fun epochDay(atMs: Long): Long = epochDay
        override fun msUntilNextDay(atMs: Long): Long = 86_400_000L
    }

    private fun check(
        state: RatingPromptState = RatingPromptState(),
        sessionCount: Int = 5,
        completedWheels: Int = 3,
        mode: GameMode = GameMode.CLASSIC,
        reason: GameOverReason = GameOverReason.ALL_ANSWERED,
        correct: Int = 20,
        streakDays: Int = 3,
        todayEpochDay: Long = defaultToday,
        enabled: Boolean = true,
    ): Boolean {
        val clock = FakeClock(todayEpochDay)
        val policy = RatingPromptPolicy(clock, enabled = enabled)
        return policy.shouldPrompt(
            state = state,
            sessionCount = sessionCount,
            completedWheels = completedWheels,
            mode = mode,
            reason = reason,
            correct = correct,
            streakDays = streakDays,
        )
    }

    @Test
    fun `default enabled flag is false`() {
        assertFalse(RatingPromptPolicy.RATING_PROMPT_ENABLED)
        assertFalse(check(enabled = RatingPromptPolicy.RATING_PROMPT_ENABLED))
    }

    @Test
    fun `happy path with good moment prompts when enabled`() {
        assertTrue(check(correct = 18, streakDays = 0))
        assertTrue(check(correct = 10, streakDays = 3))
    }

    @Test
    fun `never in the first session`() {
        assertFalse(check(sessionCount = 1))
        assertTrue(check(sessionCount = 2))
    }

    @Test
    fun `never with fewer than 3 completed wheels`() {
        assertFalse(check(completedWheels = 2))
        assertTrue(check(completedWheels = 3))
    }

    @Test
    fun `never during Daily wheel`() {
        assertFalse(check(mode = GameMode.DAILY))
    }

    @Test
    fun `never during level test`() {
        assertFalse(check(mode = GameMode.LEVEL_TEST))
    }

    @Test
    fun `never during duel`() {
        assertFalse(check(mode = GameMode.DUEL))
    }

    @Test
    fun `never right after wrong answer in hardcore`() {
        assertFalse(check(reason = GameOverReason.WRONG_IN_HARDCORE))
    }

    @Test
    fun `not a good moment when correct under 18 and streak under 3`() {
        assertFalse(check(correct = 17, streakDays = 2))
    }

    @Test
    fun `never twice within 120 days`() {
        val state = RatingPromptState(lastPromptDay = defaultToday - 60)
        assertFalse(check(state = state, todayEpochDay = defaultToday))

        val stateAfter120 = RatingPromptState(lastPromptDay = defaultToday - 120)
        assertTrue(check(state = stateAfter120, todayEpochDay = defaultToday))
    }

    @Test
    fun `not now asks again no sooner than 30 days`() {
        val state = RatingPromptState(lastRefusalDay = defaultToday - 10)
        assertFalse(check(state = state, todayEpochDay = defaultToday))

        val stateAfter30 = RatingPromptState(lastRefusalDay = defaultToday - 30)
        assertTrue(check(state = stateAfter30, todayEpochDay = defaultToday))
    }

    @Test
    fun `stops permanently after 3 refusals`() {
        val state2 = RatingPromptState(refusalCount = 2, lastRefusalDay = defaultToday - 40)
        assertTrue(check(state = state2, todayEpochDay = defaultToday))

        val state3 = RatingPromptState(refusalCount = 3, lastRefusalDay = defaultToday - 40)
        assertFalse(check(state = state3, todayEpochDay = defaultToday))
    }

    @Test
    fun `never again after user rated`() {
        val state = RatingPromptState(hasRated = true)
        assertFalse(check(state = state))
    }
}
