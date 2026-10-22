package dev.talhabayhan.lexi.core.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExamPackTest {
    private fun pack(isPro: Boolean) = ExamPack(ExamPackId("p"), name = "P", description = "", isPro = isPro, sort = 0)

    @Test
    fun `a pro pack is locked only without pro`() {
        assertTrue(pack(isPro = true).isLocked(hasPro = false))
        assertFalse(pack(isPro = true).isLocked(hasPro = true))
    }

    @Test
    fun `a free pack is never locked`() {
        assertFalse(pack(isPro = false).isLocked(hasPro = false))
        assertFalse(pack(isPro = false).isLocked(hasPro = true))
    }
}
