package dev.talhabayhan.lexi.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoTest {
    @Test
    fun `brand name is Lexi`() {
        assertEquals("Lexi", AppInfo.NAME)
    }

    @Test
    fun `version info is present`() {
        assertEquals("1.0.0", AppInfo.VERSION_NAME)
        assertEquals(1, AppInfo.BUILD_NUMBER)
    }
}
