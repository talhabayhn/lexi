package dev.talhabayhan.lexi.perf

private const val NANOS_PER_MILLI = 1_000_000L
private const val DROPPED_FACTOR = 1.5
private const val LONG_FRAME_NANOS = 50 * NANOS_PER_MILLI

/** Used when the idle window held too few frames to measure the display's refresh interval (60 Hz). */
internal const val FALLBACK_INTERVAL_NANOS = 16_666_667L

/** Frame statistics of one tour segment; see [segmentStats]. */
internal data class SegmentStats(
    val frames: Int,
    val maxGapNanos: Long,
    val dropped: Int,
    val long: Int,
    val firstGapNanos: Long,
)

/** Totals of all loops of one label; see [summarize]. */
internal data class LabelSummary(
    val label: String,
    val loops: Int,
    val medianMaxGapNanos: Long,
    val maxGapNanos: Long,
    val dropped: Int,
    val long: Int,
)

private fun gaps(frameNanos: List<Long>): List<Long> = (1 until frameNanos.size).map {
    frameNanos[it] - frameNanos[it - 1]
}

/** Median gap between consecutive frames (the display's refresh interval when the app is idle). */
internal fun refreshInterval(idleFrameNanos: List<Long>): Long {
    val sorted = gaps(idleFrameNanos).sorted()
    if (sorted.size < 2) return FALLBACK_INTERVAL_NANOS
    return sorted[sorted.size / 2]
}

/**
 * [frameNanos]: timestamps of the last frame before the trigger followed by every frame of the segment. A gap longer
 * than 1.5 × [intervalNanos] counts as dropped, one over 50 ms as long; the first gap is the cost of the
 * destination's first frame.
 */
internal fun segmentStats(frameNanos: List<Long>, intervalNanos: Long): SegmentStats {
    val gaps = gaps(frameNanos)
    return SegmentStats(
        frames = gaps.size,
        maxGapNanos = gaps.maxOrNull() ?: 0L,
        dropped = gaps.count { it > intervalNanos * DROPPED_FACTOR },
        long = gaps.count { it > LONG_FRAME_NANOS },
        firstGapNanos = gaps.firstOrNull() ?: 0L,
    )
}

/** Median and max of the per-loop max gap, sums of dropped and long frames, per label in first-seen order. */
internal fun summarize(segments: List<Pair<String, SegmentStats>>): List<LabelSummary> =
    segments.groupBy({ it.first }, { it.second }).map { (label, stats) ->
        val maxGaps = stats.map { it.maxGapNanos }.sorted()
        LabelSummary(
            label = label,
            loops = stats.size,
            medianMaxGapNanos = maxGaps[maxGaps.size / 2],
            maxGapNanos = maxGaps.last(),
            dropped = stats.sumOf { it.dropped },
            long = stats.sumOf { it.long },
        )
    }

/** Milliseconds with one decimal ("12.3"), without locale-dependent formatting. */
internal fun formatMillis(nanos: Long): String {
    val tenths = (nanos + NANOS_PER_MILLI / 20) / (NANOS_PER_MILLI / 10)
    return "${tenths / 10}.${tenths % 10}"
}

private fun escape(text: String): String = text.replace("\\", "\\\\").replace("\"", "\\\"")

internal fun segmentJson(label: String, loop: Int, stats: SegmentStats): String {
    val name = escape(label)
    val max = formatMillis(stats.maxGapNanos)
    val first = formatMillis(stats.firstGapNanos)
    return """{"label":"$name","loop":$loop,"frames":${stats.frames},"maxGapMs":$max,""" +
        """"dropped":${stats.dropped},"long":${stats.long},"firstGapMs":$first}"""
}

internal fun summaryJson(
    summaries: List<LabelSummary>,
    intervalNanos: Long,
    platform: String,
    reducedMotion: Boolean,
): String {
    val labels = summaries.joinToString(",") {
        val name = escape(it.label)
        val median = formatMillis(it.medianMaxGapNanos)
        val max = formatMillis(it.maxGapNanos)
        """{"label":"$name","loops":${it.loops},"medianMaxGapMs":$median,"maxGapMs":$max,""" +
            """"dropped":${it.dropped},"long":${it.long}}"""
    }
    return """{"platform":"$platform","refreshIntervalMs":${formatMillis(intervalNanos)},""" +
        """"reducedMotion":$reducedMotion,"labels":[$labels]}"""
}
