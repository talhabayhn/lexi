package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.flow.collectLatest

/**
 * Watches frame times while a flow transition runs and reports a slow streak: [slowFramesInARow] consecutive frames
 * more than [slowFrameNanos] apart (default 3 × > 24 ms). Frames are fed by [FrameMonitorEffect], which reads
 * Compose's frame clock (`withFrameNanos`: Choreographer on Android, CADisplayLink on iOS) only while a transition is
 * registered, so an idle app requests no extra frames. Not thread-safe: call it on the UI thread.
 *
 * The streak is sticky for the monitor's lifetime (one app session): [MotionPolicy] then stays in [MotionTier.Lite].
 */
public class FrameMonitor(
    private val slowFrameNanos: Long = DEFAULT_SLOW_FRAME_NANOS,
    private val slowFramesInARow: Int = DEFAULT_SLOW_FRAMES_IN_A_ROW,
) {
    private var activeTransitions by mutableIntStateOf(0)
    private var lastFrameNanos = NO_FRAME
    private var slowCount = 0

    /** True once a slow streak happened during a transition; never resets. */
    public var slowStreakDetected: Boolean by mutableStateOf(false)
        private set

    /** Whether at least one transition is registered. */
    public val isWatching: Boolean get() = activeTransitions > 0

    /** Registers a transition (overlapping ones are counted); frames are measured until the matching [endTransition]. */
    public fun beginTransition() {
        if (activeTransitions == 0) resetCount()
        activeTransitions++
    }

    public fun endTransition() {
        if (activeTransitions == 0) return
        activeTransitions--
        if (activeTransitions == 0) resetCount()
    }

    /** One frame at [frameTimeNanos] (frame clock time); ignored while no transition is registered. */
    public fun onFrame(frameTimeNanos: Long) {
        if (!isWatching) {
            lastFrameNanos = frameTimeNanos
            return
        }
        val previous = lastFrameNanos
        lastFrameNanos = frameTimeNanos
        if (previous == NO_FRAME) return
        if (frameTimeNanos - previous > slowFrameNanos) {
            slowCount++
            if (slowCount >= slowFramesInARow) slowStreakDetected = true
        } else {
            slowCount = 0
        }
    }

    private fun resetCount() {
        lastFrameNanos = NO_FRAME
        slowCount = 0
    }

    public companion object {
        public const val DEFAULT_SLOW_FRAME_NANOS: Long = 24_000_000L
        public const val DEFAULT_SLOW_FRAMES_IN_A_ROW: Int = 3
        private const val NO_FRAME = Long.MIN_VALUE
    }
}

/** Feeds [monitor] from the composition's frame clock while it has a registered transition. */
@Composable
public fun FrameMonitorEffect(monitor: FrameMonitor) {
    LaunchedEffect(monitor) {
        snapshotFlow { monitor.isWatching }.collectLatest { watching ->
            while (watching) {
                withFrameNanos(monitor::onFrame)
            }
        }
    }
}
