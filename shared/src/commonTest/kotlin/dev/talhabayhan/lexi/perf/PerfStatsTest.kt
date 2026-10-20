package dev.talhabayhan.lexi.perf

import kotlin.test.Test
import kotlin.test.assertEquals

class PerfStatsTest {
    private val ms = 1_000_000L

    private fun frames(vararg gapsMs: Long): List<Long> =
        gapsMs.fold(listOf(0L)) { acc, gap -> acc + (acc.last() + gap * ms) }

    @Test
    fun refreshIntervalIsTheMedianGap() {
        assertEquals(8 * ms, refreshInterval(frames(8, 8, 9, 8, 30)))
    }

    @Test
    fun refreshIntervalFallsBackWhenTooFewFrames() {
        assertEquals(FALLBACK_INTERVAL_NANOS, refreshInterval(frames(8)))
    }

    @Test
    fun segmentCountsDroppedAndLongFrames() {
        // interval 10 ms: 15 ms is not dropped (limit is > 15), 16 is dropped, 60 is dropped and long.
        val stats = segmentStats(frames(40, 10, 15, 16, 60, 10), 10 * ms)
        assertEquals(6, stats.frames)
        assertEquals(60 * ms, stats.maxGapNanos)
        assertEquals(3, stats.dropped)
        assertEquals(1, stats.long)
        assertEquals(40 * ms, stats.firstGapNanos)
    }

    @Test
    fun emptySegmentIsAllZero() {
        assertEquals(SegmentStats(0, 0, 0, 0, 0), segmentStats(listOf(5L), 10 * ms))
    }

    @Test
    fun summaryGroupsByLabelInFirstSeenOrder() {
        fun stat(max: Long, dropped: Int, long: Int) = SegmentStats(10, max * ms, dropped, long, max * ms)
        val result = summarize(
            listOf(
                "a" to stat(30, 1, 0),
                "b" to stat(10, 0, 0),
                "a" to stat(70, 2, 1),
                "a" to stat(20, 0, 0),
            ),
        )
        assertEquals(listOf("a", "b"), result.map { it.label })
        assertEquals(LabelSummary("a", 3, 30 * ms, 70 * ms, 3, 1), result[0])
    }

    @Test
    fun formatsMillisWithOneDecimal() {
        assertEquals("12.3", formatMillis(12_340_000L))
        assertEquals("0.0", formatMillis(0L))
        assertEquals("8.3", formatMillis(8_333_333L))
        assertEquals("16.7", formatMillis(FALLBACK_INTERVAL_NANOS))
    }

    @Test
    fun segmentJsonMatchesTheDocumentedLine() {
        val json = segmentJson("Home→Settings", 0, SegmentStats(30, 25 * ms, 2, 0, 18_500_000L))
        assertEquals(
            """{"label":"Home→Settings","loop":0,"frames":30,"maxGapMs":25.0,"dropped":2,"long":0,"firstGapMs":18.5}""",
            json,
        )
    }
}
