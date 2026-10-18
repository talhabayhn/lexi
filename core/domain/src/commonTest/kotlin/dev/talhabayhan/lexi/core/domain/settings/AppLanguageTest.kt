package dev.talhabayhan.lexi.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppLanguageTest {
    @Test
    fun `language enum maps to the right locale tags`() {
        assertNull(AppLanguage.SYSTEM.localeTag)
        assertEquals("tr", AppLanguage.TR.localeTag)
        assertEquals("en", AppLanguage.EN.localeTag)
    }
}
