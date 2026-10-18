package dev.talhabayhan.lexi.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppInfoTest {
    @Test
    fun `brand name is Lexi`() {
        assertEquals("Lexi", AppInfo.NAME)
    }

    @Test
    fun `version info is present`() {
        assertTrue(AppInfo.VERSION_NAME.isNotBlank())
        assertTrue(AppInfo.BUILD_NUMBER > 0)
    }
}
