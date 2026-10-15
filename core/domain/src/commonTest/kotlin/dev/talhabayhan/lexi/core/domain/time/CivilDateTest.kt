package dev.talhabayhan.lexi.core.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals

class CivilDateTest {
    @Test
    fun `epoch day 0 is 1970-01-01`() {
        assertEquals(CivilDate(1970, 1, 1), civilDateOfEpochDay(0))
    }

    @Test
    fun `the design's sample day`() {
        // 2026-10-14: the daily card in 02-home shows 14 and EKIM.
        assertEquals(CivilDate(2026, 10, 14), civilDateOfEpochDay(20_740))
        assertEquals(CivilDate(2026, 10, 15), civilDateOfEpochDay(20_741))
    }

    @Test
    fun `leap days and year ends`() {
        assertEquals(CivilDate(2024, 2, 29), civilDateOfEpochDay(19_782))
        assertEquals(CivilDate(2024, 3, 1), civilDateOfEpochDay(19_783))
        assertEquals(CivilDate(1999, 12, 31), civilDateOfEpochDay(10_956))
        assertEquals(CivilDate(2000, 1, 1), civilDateOfEpochDay(10_957))
    }

    @Test
    fun `days before 1970`() {
        assertEquals(CivilDate(1969, 12, 31), civilDateOfEpochDay(-1))
    }
}
