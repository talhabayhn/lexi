package dev.talhabayhan.lexi.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoTest {
    @Test
    fun `brand name is Lexi`() {
        assertEquals("Lexi", AppInfo.NAME)
    }
}
