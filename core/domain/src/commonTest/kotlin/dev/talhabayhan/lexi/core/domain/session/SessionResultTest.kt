package dev.talhabayhan.lexi.core.domain.session

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionResultTest {
    @Test
    fun toRecord_keepsWhatStatsNeeds() {
        val session = SessionResult(
            mode = GameMode.CLASSIC,
            level = Level.A1,
            category = null,
            seed = 1,
            startedAtMs = 0,
            endedAtMs = 180_000,
            epochDay = 20_000,
            score = 120,
            correct = 12,
            wrong = 4,
            passed = 2,
            completed = true,
        )

        assertEquals(SessionRecord(epochDay = 20_000, completed = true, correct = 12), session.toRecord())
    }
}
