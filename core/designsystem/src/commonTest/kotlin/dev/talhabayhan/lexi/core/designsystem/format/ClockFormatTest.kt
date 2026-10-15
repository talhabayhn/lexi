package dev.talhabayhan.lexi.core.designsystem.format

import kotlin.test.Test
import kotlin.test.assertEquals

class ClockFormatTest {
    @Test
    fun `formats minutes and two digit seconds`() {
        assertEquals("3:00", formatClock(180))
        assertEquals("1:47", formatClock(107))
        assertEquals("0:05", formatClock(5))
        assertEquals("5:00", formatClock(300))
    }
}
