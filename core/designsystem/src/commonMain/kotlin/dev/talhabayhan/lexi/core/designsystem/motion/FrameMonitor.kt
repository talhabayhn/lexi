package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.flow.collectLatest

/**
 * Watches frame times while flow transitions run and decides whether the device is really struggling
 * (`docs/design/video-flow-notes.md` § Performance). Frames are fed by [FrameMonitorEffect], which reads Compose's
 * frame clock (`withFrameNanos`: Choreographer on Android, CADisplayLink on iOS) only while a transition is
 * registered, so an idle app requests no extra frames. Not thread-safe: call it on the UI thread.
 *
 * A *transition* is one span from the first [beginTransition] to the matching last [endTransition] (overlapping
 * registrations share it). A slow frame comes more than [slowFrameNanos] after the previous one; a transition is
 * *slow* when it has [slowFramesInARow] slow frames in a row and *good* when it has at least [minFramesToJudge] frames
 * and no such streak (shorter ones, e.g. cancelled at once, are not judged).
 *
 * - **Warm-up:** the first transition of each key in the monitor's lifetime (one app process) is never judged: its
 *   frames pay for JIT, class loading and first shader use, not for the device. A key stays cold until one of its
 *   transitions reached [minFramesToJudge] frames (one cancelled at once warms nothing up). Everything in a span with
 *   a cold key is warm-up from the moment that key joins; frames before the join were already counted.
 * - **Downgrade** ([downgrade] set): [slowTransitionsToDowngrade] slow transitions, or one run of
 *   [longStreakFrames] slow frames (at once, mid-transition). [goodTransitionsToRecover] good transitions in a row
 *   while still undowngraded clear the slow count, so old evidence fades.
 * - **Recovery** ([downgrade] cleared): [goodTransitionsToRecover] good transitions in a row; a slow one restarts the
 *   count. A later downgrade needs fresh evidence.
 * - **Field measurement** ([onTransitionMeasured]): when a span ends, its keys and its worst frame (the longest gap
 *   between two frames, warm-up frames included) are reported, for `transition_frames` (`TransitionTelemetry`).
 */
public class FrameMonitor(
    private val slowFrameNanos: Long = DEFAULT_SLOW_FRAME_NANOS,
    private val slowFramesInARow: Int = DEFAULT_SLOW_FRAMES_IN_A_ROW,
    private val longStreakFrames: Int = DEFAULT_LONG_STREAK_FRAMES,
    public val slowTransitionsToDowngrade: Int = DEFAULT_SLOW_TRANSITIONS_TO_DOWNGRADE,
    public val goodTransitionsToRecover: Int = DEFAULT_GOOD_TRANSITIONS_TO_RECOVER,
    private val minFramesToJudge: Int = DEFAULT_MIN_FRAMES_TO_JUDGE,
) {
    private var activeTransitions by mutableIntStateOf(0)
    private val warmedUpKeys = mutableSetOf<String>()
    private var lastFrameNanos = NO_FRAME
    private var slowRun = 0

    // The transition being measured: cold keys it registered (warm-up while not empty) and the frames seen so far.
    private val coldKeys = mutableSetOf<String>()
    private var longestRun = 0
    private var measuredFrames = 0
    private val spanKeys = mutableSetOf<String>()
    private var worstFrameNanos = 0L

    /** Told about every span that measured at least one frame, when it ends (after it is judged; failures ignored). */
    public var onTransitionMeasured: TransitionFramesListener? = null

    /** Why frames currently hold the session in [MotionTier.Lite]; null while they allow [MotionTier.Full]. */
    public var downgrade: FrameDowngrade? by mutableStateOf(null)
        private set

    /** Slow transitions counted toward the next downgrade (reset by a downgrade, a recovery or good transitions). */
    public var slowTransitions: Int by mutableIntStateOf(0)
        private set

    /** Good transitions in a row since the last slow one. */
    public var goodTransitionsInARow: Int by mutableIntStateOf(0)
        private set

    /** How many times this session went back from a frame downgrade to [MotionTier.Full]. */
    public var recoveries: Int by mutableIntStateOf(0)
        private set

    public val isDowngraded: Boolean get() = downgrade != null

    /** Whether at least one transition is registered. */
    public val isWatching: Boolean get() = activeTransitions > 0

    /**
     * Registers a transition of [key] (a stable name per kind of motion, e.g. `"picker.flip.open"`); frames are
     * measured until the matching [endTransition]. The first registration of a key is the warm-up.
     */
    public fun beginTransition(key: String) {
        if (activeTransitions == 0) startTransition()
        if (key !in warmedUpKeys) coldKeys += key
        spanKeys += key
        activeTransitions++
    }

    public fun endTransition() {
        if (activeTransitions == 0) return
        activeTransitions--
        if (activeTransitions == 0) {
            judgeTransition()
            resetRun()
            // After judging: a failing listener can never skip the downgrade rules (it is told nothing else).
            if (measuredFrames > 0) {
                runCatching {
                    onTransitionMeasured?.onTransitionMeasured(spanKeys.toSet(), worstFrameNanos, measuredFrames)
                }
            }
        }
    }

    /** One frame at [frameTimeNanos] (frame clock time); ignored while no transition is registered. */
    public fun onFrame(frameTimeNanos: Long) {
        val previous = lastFrameNanos
        // Two feeders (host + rememberMotionTier) report the same frame twice: count it once.
        if (frameTimeNanos == previous) return
        lastFrameNanos = frameTimeNanos
        if (!isWatching || previous == NO_FRAME) return
        measuredFrames++
        worstFrameNanos = maxOf(worstFrameNanos, frameTimeNanos - previous)
        if (coldKeys.isNotEmpty()) return
        if (frameTimeNanos - previous > slowFrameNanos) {
            slowRun++
            longestRun = maxOf(longestRun, slowRun)
            if (slowRun >= longStreakFrames && downgrade == null) downgradeTo(FrameDowngrade.LongStreak(slowRun))
        } else {
            slowRun = 0
        }
    }

    private fun startTransition() {
        resetRun()
        coldKeys.clear()
        spanKeys.clear()
        longestRun = 0
        measuredFrames = 0
        worstFrameNanos = 0L
    }

    private fun judgeTransition() {
        if (coldKeys.isNotEmpty()) {
            if (measuredFrames >= minFramesToJudge) warmedUpKeys += coldKeys
            return
        }
        when {
            longestRun >= slowFramesInARow -> onSlowTransition()
            measuredFrames >= minFramesToJudge -> onGoodTransition()
        }
    }

    private fun onSlowTransition() {
        goodTransitionsInARow = 0
        // Already downgraded (also by a long streak in this very transition): nothing more to prove.
        if (downgrade != null) return
        slowTransitions++
        if (slowTransitions >= slowTransitionsToDowngrade) downgradeTo(FrameDowngrade.SlowTransitions(slowTransitions))
    }

    private fun onGoodTransition() {
        goodTransitionsInARow++
        if (goodTransitionsInARow < goodTransitionsToRecover) return
        goodTransitionsInARow = 0
        slowTransitions = 0
        if (downgrade != null) {
            downgrade = null
            recoveries++
        }
    }

    private fun downgradeTo(reason: FrameDowngrade) {
        downgrade = reason
        slowTransitions = 0
        goodTransitionsInARow = 0
    }

    private fun resetRun() {
        lastFrameNanos = NO_FRAME
        slowRun = 0
    }

    public companion object {
        public const val DEFAULT_SLOW_FRAME_NANOS: Long = 24_000_000L
        public const val DEFAULT_SLOW_FRAMES_IN_A_ROW: Int = 3
        public const val DEFAULT_LONG_STREAK_FRAMES: Int = 5
        public const val DEFAULT_SLOW_TRANSITIONS_TO_DOWNGRADE: Int = 2
        public const val DEFAULT_GOOD_TRANSITIONS_TO_RECOVER: Int = 3
        public const val DEFAULT_MIN_FRAMES_TO_JUDGE: Int = 4
        private const val NO_FRAME = Long.MIN_VALUE
    }
}

/** Receives each measured [FrameMonitor] span: its transition [keys], its worst frame gap and its frame count. */
public fun interface TransitionFramesListener {
    public fun onTransitionMeasured(keys: Set<String>, worstFrameNanos: Long, frames: Int)
}

/** What made a [FrameMonitor] downgrade the session to [MotionTier.Lite]. */
@Immutable
public sealed interface FrameDowngrade {
    /** [count] separate transitions each had a slow streak. */
    public data class SlowTransitions(val count: Int) : FrameDowngrade

    /** One transition had [frames] slow frames in a row. */
    public data class LongStreak(val frames: Int) : FrameDowngrade
}

/**
 * Feeds [monitor] from the composition's frame clock while it has a registered transition. Safe to compose more than
 * once for the same monitor (each frame is counted once).
 */
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
