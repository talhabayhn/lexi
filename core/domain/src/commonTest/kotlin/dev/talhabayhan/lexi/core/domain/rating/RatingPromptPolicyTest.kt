package dev.talhabayhan.lexi.core.domain.rating

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RatingPromptPolicyTest {
    private val today = 1000L
    private val installed = today - 3

    private fun game(mode: GameMode? = GameMode.CLASSIC, completed: Boolean = true) =
        SessionRecord(epochDay = installed, completed = completed, correct = 10, mode = mode)

    private val five = List(5) { game() }

    private fun check(
        sessions: List<SessionRecord> = five,
        installDay: Long? = installed,
        lastRequestDay: Long? = null,
        todayEpochDay: Long = today,
    ) = RatingPromptPolicy.shouldRequest(sessions, installDay, lastRequestDay, todayEpochDay)

    @Test
    fun `thresholds are five real games three days and 120 days`() {
        assertEquals(5, RatingPromptPolicy.MIN_REAL_GAMES)
        assertEquals(3L, RatingPromptPolicy.MIN_DAYS_SINCE_INSTALL)
        assertEquals(120L, RatingPromptPolicy.PERIOD_DAYS)
    }

    @Test
    fun `happy path asks`() {
        assertTrue(check())
    }

    @Test
    fun `needs five completed real games`() {
        assertFalse(check(sessions = five.drop(1)))
        assertTrue(check(sessions = five + game(completed = false) + game()))
    }

    @Test
    fun `level test and duel games do not count`() {
        val fillers = List(3) { game(GameMode.LEVEL_TEST) } + List(3) { game(GameMode.DUEL) } + game(mode = null)
        assertFalse(check(sessions = five.drop(1) + fillers))
    }

    @Test
    fun `never in the first days after install`() {
        assertFalse(check(installDay = today)) // first session
        assertFalse(check(installDay = today - 2))
        assertTrue(check(installDay = today - 3))
    }

    @Test
    fun `unknown install day never asks`() {
        assertFalse(check(installDay = null))
    }

    @Test
    fun `not right after a failure or forfeit`() {
        assertFalse(check(sessions = five + game(completed = false)))
        assertTrue(check(sessions = five + game(completed = false) + game()))
    }

    @Test
    fun `a trailing level test or duel does not decide the latest game`() {
        assertFalse(check(sessions = five + game(completed = false) + game(GameMode.LEVEL_TEST)))
        assertTrue(check(sessions = five + game(GameMode.DUEL, completed = false)))
    }

    @Test
    fun `at most once per 120 days`() {
        assertFalse(check(lastRequestDay = today))
        assertFalse(check(lastRequestDay = today - 119))
        assertTrue(check(lastRequestDay = today - 120))
    }

    @Test
    fun `no games no request`() {
        assertFalse(check(sessions = emptyList()))
    }
}
