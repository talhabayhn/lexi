package dev.talhabayhan.lexi.core.domain.goal

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DailyGoalProgressTest {

    @Test
    fun `the shown count is the done count up to the goal`() {
        assertEquals(0, DailyGoalProgress(done = 0, goal = 3, reached = false).shown)
        assertEquals(2, DailyGoalProgress(done = 2, goal = 3, reached = false).shown)
        assertEquals(3, DailyGoalProgress(done = 3, goal = 3, reached = true).shown)
    }

    @Test
    fun `wheels beyond the goal are capped for display but still counted and reached`() {
        val sessions = List(5) { SessionRecord(epochDay = 1L, completed = true, correct = 10, mode = GameMode.CLASSIC) }

        val progress = DailyGoal.progress(today = 1L, sessions = sessions, goal = 3)

        assertEquals(3, progress.shown)
        assertEquals(5, progress.done)
        assertTrue(progress.reached)
    }
}
