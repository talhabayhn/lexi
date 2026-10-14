package dev.talhabayhan.lexi.core.designsystem.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class CssLineHeightTest {

    @Test
    fun `normal line height rounds ascent and descent like chrome`() {
        assertEquals(18f, cssNormalLineHeight(12f))
        assertEquals(19f, cssNormalLineHeight(13f))
        assertEquals(20f, cssNormalLineHeight(14f))
        assertEquals(22f, cssNormalLineHeight(15f))
        assertEquals(41f, cssNormalLineHeight(28f))
    }
}
