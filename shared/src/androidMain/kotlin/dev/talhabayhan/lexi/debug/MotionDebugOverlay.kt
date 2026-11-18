package dev.talhabayhan.lexi.debug

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.motion.FrameDowngrade
import dev.talhabayhan.lexi.core.designsystem.motion.FrameMonitor
import dev.talhabayhan.lexi.core.designsystem.motion.FrameMonitorEffect
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTierReason
import dev.talhabayhan.lexi.core.designsystem.motion.rememberMotionSignals
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/**
 * Developer switch for the video flow's motion tiers (`video-flow-notes.md` §5), shown only by the Android debug build
 * (`androidApp/src/debug` `DebugTools`; release-like builds compile the no-op twin in `src/nodebug`, so this is
 * unreferenced there and R8 removes it: `docs/PERF.md` § Hand-test build has the dex check). Two small chips under
 * the status bar: the first cycles the session's tier Auto → Full → Lite → Minimal
 * ([dev.talhabayhan.lexi.core.designsystem.motion.MotionPolicy.forcedTier]) and shows the current tier with its reason
 * ([tierLabel], e.g. "Lite: slow transitions x2 · good 1/3"); the second shows a frame-time graph (last [GRAPH_FRAMES]
 * frames, the 16.7 ms and 24 ms lines, P95 and the worst frame). Not user-facing: no string resources.
 */
@Composable
public fun MotionDebugOverlay() {
    val policy = LocalMotionPolicy.current
    FrameMonitorEffect(policy.frameMonitor)
    val signals = rememberMotionSignals()
    val tier = policy.tier(signals)
    val reason = policy.tierReason(signals)
    var graph by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(CHIP_GAP)) {
                val forced = policy.forcedTier
                DebugChip("${forced?.name ?: "auto"} · ${tierLabel(tier, reason, policy.frameMonitor)}") {
                    policy.forcedTier = when (forced) {
                        null -> MotionTier.Full
                        MotionTier.Full -> MotionTier.Lite
                        MotionTier.Lite -> MotionTier.Minimal
                        MotionTier.Minimal -> null
                    }
                }
                DebugChip(if (graph) "frames on" else "frames off") { graph = !graph }
            }
            if (graph) FrameTimeGraph()
        }
    }
}

/** "<tier>: <reason>", plus the frame monitor's progress toward the next downgrade or recovery. */
internal fun tierLabel(tier: MotionTier, reason: MotionTierReason, monitor: FrameMonitor): String = when (reason) {
    MotionTierReason.Forced -> "${tier.name}: forced"

    MotionTierReason.ReducedMotion -> "${tier.name}: reduced motion"

    MotionTierReason.LowRamDevice -> "${tier.name}: low RAM device"

    MotionTierReason.LowPowerMode -> "${tier.name}: low power mode"

    MotionTierReason.SlowFrames -> {
        val why = when (val downgrade = monitor.downgrade) {
            is FrameDowngrade.SlowTransitions -> "slow transitions x${downgrade.count}"
            is FrameDowngrade.LongStreak -> "${downgrade.frames} slow frames in a row"
            null -> "slow frames"
        }
        "${tier.name}: $why · good ${monitor.goodTransitionsInARow}/${monitor.goodTransitionsToRecover}"
    }

    MotionTierReason.Default -> buildString {
        append("${tier.name}: default")
        if (monitor.recoveries > 0) append(" · recovered x${monitor.recoveries}")
        if (monitor.slowTransitions > 0) {
            append(" · slow ${monitor.slowTransitions}/${monitor.slowTransitionsToDowngrade}")
        }
    }
}

@Composable
private fun DebugChip(label: String, onClick: () -> Unit) {
    val colors = LexiTheme.colors
    Text(
        label,
        fontSize = CHIP_TEXT,
        color = colors.cream,
        modifier = Modifier
            .background(colors.ink.copy(alpha = CHIP_ALPHA))
            .clickable(onClick = onClick)
            .padding(horizontal = CHIP_PADDING_H, vertical = CHIP_PADDING_V),
    )
}

/** Frame intervals from the frame clock; the bars redraw each frame, the summary text every [SUMMARY_EVERY] frames. */
@Composable
private fun FrameTimeGraph() {
    val colors = LexiTheme.colors
    val frames = remember { FloatArray(GRAPH_FRAMES) }
    var head by remember { mutableIntStateOf(0) }
    var summary by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        var last = 0L
        var count = 0
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    frames[head % GRAPH_FRAMES] = (now - last) / NANOS_PER_MS
                    head++
                    if (++count % SUMMARY_EVERY == 0) {
                        val sorted = frames.copyOf(minOf(head, GRAPH_FRAMES)).sorted()
                        val p95 = sorted[((sorted.size - 1) * P95).toInt()]
                        summary = "p95 %.1f ms · max %.1f ms".format(p95, sorted.last())
                    }
                }
                last = now
            }
        }
    }
    Column(Modifier.background(colors.ink.copy(alpha = CHIP_ALPHA)).padding(CHIP_PADDING_V)) {
        Text(summary, fontSize = CHIP_TEXT, color = colors.cream)
        Canvas(Modifier.width(GRAPH_WIDTH).height(GRAPH_HEIGHT)) {
            val barWidth = size.width / GRAPH_FRAMES
            val scale = size.height / GRAPH_MAX_MS
            val newest = head
            for (i in 0 until GRAPH_FRAMES) {
                val index = newest - GRAPH_FRAMES + i
                if (index < 0) continue
                val ms = frames[index % GRAPH_FRAMES]
                val color = when {
                    ms > SLOW_MS -> colors.red
                    ms > BUDGET_MS -> colors.yellow
                    else -> colors.cream
                }
                val h = minOf(ms, GRAPH_MAX_MS) * scale
                drawRect(color, Offset(i * barWidth, size.height - h), Size(barWidth, h))
            }
            drawLine(
                colors.yellow,
                Offset(0f, size.height - BUDGET_MS * scale),
                Offset(
                    size.width,
                    size.height - BUDGET_MS * scale,
                ),
            )
            drawLine(
                colors.red,
                Offset(0f, size.height - SLOW_MS * scale),
                Offset(
                    size.width,
                    size.height - SLOW_MS * scale,
                ),
            )
        }
    }
}

private const val GRAPH_FRAMES = 120
private const val SUMMARY_EVERY = 30
private const val NANOS_PER_MS = 1_000_000f
private const val P95 = 0.95f
private const val BUDGET_MS = 16.7f
private const val SLOW_MS = 24f
private const val GRAPH_MAX_MS = 50f
private const val CHIP_ALPHA = 0.75f
private val CHIP_TEXT = 10.sp
private val CHIP_GAP = 4.dp
private val CHIP_PADDING_H = 6.dp
private val CHIP_PADDING_V = 2.dp
private val GRAPH_WIDTH = 240.dp
private val GRAPH_HEIGHT = 60.dp
