package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlinx.coroutines.flow.first

/**
 * State of the letter morph (`motion.md` #16) drawn by a [MorphOverlayHost] above the NavHost, sequenced by a
 * [LetterMorphMachine]. [start] begins it from the anchor `sourceKey` (OYNA button); the ink covers the screen
 * ([covered], [awaitCovered]: navigate to the game under it), then holds until [markReady] is called (game loaded)
 * *and* the anchor `targetKey` (wheel marker) is laid out, and only then shrinks into it while the board comes in
 * ([boardClockMillis]). [cancel] fades it out. The game clock waits for [isRunning] to turn false.
 */
@Stable
public class MorphOverlayState(public val anchors: MorphAnchors = MorphAnchors()) {
    internal var run: MorphRun? by mutableStateOf(null)
        private set

    /** Frame-time snapshot of the machine, written once per frame by the host (draw-phase readers observe it). */
    internal var elapsedMillis by mutableLongStateOf(0L)
    internal var phase: LetterMorphPhase by mutableStateOf(LetterMorphPhase.Idle)

    /** Target window rect when the shrink started; used if the marker's anchor goes away mid-shrink. */
    internal var shrinkTargetInWindow: Rect? by mutableStateOf(null)
    internal var ready by mutableStateOf(false)
        private set
    internal var cancelRequested by mutableStateOf(false)
        private set

    /** Created by the host with the theme's [FlowMotion]; null outside a host. */
    internal var machine: LetterMorphMachine? = null

    /** A morph runs (from [start] until the board has landed or the cancel fade is over). */
    public val isRunning: Boolean get() = run != null

    /** The ink covers the whole screen right now: the game may be navigated to under it. */
    public val covered: Boolean
        get() = run != null && (phase == LetterMorphPhase.Holding || phase == LetterMorphPhase.Waiting)

    /**
     * Starts a morph in [tier]; false (nothing happens) when one runs already, the tier is [MotionTier.Minimal] (no
     * morph: the caller cross-fades) or [sourceKey] has no laid-out anchor.
     */
    public fun start(sourceKey: Any, targetKey: Any, letter: String, tier: MotionTier = MotionTier.Full): Boolean {
        if (run != null || tier == MotionTier.Minimal) return false
        val source = anchors[sourceKey] ?: return false
        run = MorphRun(source, targetKey, letter, tier)
        elapsedMillis = 0L
        phase = LetterMorphPhase.Expanding
        shrinkTargetInWindow = null
        ready = false
        cancelRequested = false
        return true
    }

    /** The game is ready (data loaded, board composed under the ink): the shrink may start. */
    public fun markReady() {
        if (run != null) ready = true
    }

    /** Leaves the morph (system back, app in the background, the game could not open): the overlay fades out. */
    public fun cancel() {
        if (run != null) cancelRequested = true
    }

    /**
     * Suspends until the ink covers the screen (true) or the morph ends or is cancelled before that (false). The
     * navigation to the game waits for it, so the game never slides in visibly and a cancelled morph never navigates.
     */
    public suspend fun awaitCovered(): Boolean {
        val outcome = snapshotFlow {
            when {
                run == null || cancelRequested -> false
                covered -> true
                else -> null
            }
        }.first { it != null }
        return outcome == true
    }

    /**
     * The board entrance's clock for screens composed while a morph runs: 0 until the shrink starts (pieces wait,
     * hidden under the ink), then ms since the shrink start; [Long.MAX_VALUE] once the morph is over or cancelled
     * (everything at rest). Reads snapshot state: call it in draw-phase lambdas.
     */
    public fun boardClockMillis(): Long {
        if (run == null || phase == LetterMorphPhase.Cancelling) return Long.MAX_VALUE
        // Observed, so the readers redraw every frame; the machine holds this frame's values.
        elapsedMillis
        return machine?.boardElapsedMillis ?: 0L
    }

    /** Full: board pieces enter one after another; Lite: groups enter together (latched at the shrink start). */
    public val boardStagger: Boolean get() = machine?.boardStagger ?: true

    internal fun finish() {
        run = null
        phase = LetterMorphPhase.Idle
        shrinkTargetInWindow = null
        ready = false
        cancelRequested = false
        machine?.reset()
    }
}

/** One morph: window rect of the source when it started, the target anchor key, the big letter and the tier. */
internal class MorphRun(val sourceInWindow: Rect, val targetKey: Any, val letter: String, val tier: MotionTier)

/** The [MorphOverlayState] of the nearest [MorphOverlayHost]; null outside one. */
public val LocalMorphOverlay: ProvidableCompositionLocal<MorphOverlayState?> = staticCompositionLocalOf { null }

/**
 * Hosts [content] (the NavHost) and draws the letter morph above it. Provides [LocalMorphAnchors] (so
 * `Modifier.morphAnchor(key)` works on any screen below) and [LocalMorphOverlay]. A [LetterMorphMachine] fed from the
 * frame clock sequences it; all per-frame values are read in `graphicsLayer` / `drawBehind` lambdas, so a running
 * morph never recomposes or re-lays out anything: the ink is one full-screen layer scaled and translated onto the
 * current rectangle, the letter one text layer measured once and scaled. While a morph runs the host swallows all
 * touches (until the board has landed); system back and the app going to the background are the caller's to turn
 * into [MorphOverlayState.cancel] (the host has no navigation or lifecycle dependency).
 *
 * [frameMonitor] (optional, fed by the host itself) measures only the moving parts — expand + letter pop, and the
 * shrink — not the hold, during which the NavHost builds the game screen under the ink; a slow first composition of
 * the game must not downgrade the whole session to [MotionTier.Lite]. [tier] is read every frame (a downgrade applies
 * at the machine's next latch point). If the host leaves the composition mid-morph the state is reset and neither
 * callback is called. [onFinished]: the board has landed (start the clock); [onCancelled]: the cancel fade is over.
 */
@Composable
public fun MorphOverlayHost(
    state: MorphOverlayState,
    modifier: Modifier = Modifier,
    frameMonitor: FrameMonitor? = null,
    tier: () -> MotionTier = { MotionTier.Full },
    onFinished: () -> Unit = {},
    onCancelled: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val motion = LexiTheme.motion
    val colors = LexiTheme.colors
    val timeline = remember(motion, colors) {
        LetterMorphTimeline(motion.flow, motion.mechanical, motion.pop, colors.red, colors.ink)
    }
    val machine = remember(motion) { LetterMorphMachine(motion.flow) }
    state.machine = machine
    val hostOrigin = remember { mutableStateOf(Offset.Zero) }
    val run = state.run

    val currentOnFinished by rememberUpdatedState(onFinished)
    val currentOnCancelled by rememberUpdatedState(onCancelled)
    val currentTier by rememberUpdatedState(tier)
    if (frameMonitor != null) FrameMonitorEffect(frameMonitor)

    LaunchedEffect(run) {
        if (run == null) return@LaunchedEffect
        var watching = false
        fun watch(on: Boolean) {
            if (on == watching || frameMonitor == null) return
            if (on) frameMonitor.beginTransition() else frameMonitor.endTransition()
            watching = on
        }
        var outcome = LetterMorphPhase.Idle
        try {
            withFrameMillis { now -> machine.start(now, run.tier) }
            while (machine.isActive) {
                withFrameMillis { now ->
                    machine.setTier(currentTier())
                    machine.setTargetLaidOut(state.anchors[run.targetKey] != null)
                    if (state.ready) machine.markReady()
                    if (state.cancelRequested) machine.cancel()
                    val wasShrinking = machine.shrinkStartMillis != null
                    val phase = machine.tick(now)
                    if (!wasShrinking && machine.shrinkStartMillis != null) {
                        state.shrinkTargetInWindow = state.anchors[run.targetKey]
                    }
                    state.elapsedMillis = machine.elapsedMillis
                    state.phase = phase
                    watch(phase == LetterMorphPhase.Expanding || phase == LetterMorphPhase.Shrinking)
                }
            }
            outcome = machine.phase
        } finally {
            watch(false)
            // Also when the host leaves the composition mid-morph: nothing stays on screen.
            if (machine.isActive) machine.cancel()
            state.finish()
        }
        when (outcome) {
            LetterMorphPhase.Finished -> currentOnFinished()
            LetterMorphPhase.Cancelled -> currentOnCancelled()
            else -> Unit
        }
    }

    Box(modifier.fillMaxSize().onGloballyPositioned { hostOrigin.value = it.positionInWindow() }) {
        CompositionLocalProvider(
            LocalMorphAnchors provides state.anchors,
            LocalMorphOverlay provides state,
        ) { content() }
        if (run != null) {
            LetterMorphLayer(
                letter = run.letter,
                frame = { screen ->
                    val origin = hostOrigin.value
                    // Observed so each frame redraws; the machine holds the values of this frame.
                    val elapsed = state.elapsedMillis
                    timeline.frameAt(
                        elapsedMillis = elapsed,
                        source = run.sourceInWindow.translate(-origin),
                        screen = screen,
                        target = (state.anchors[run.targetKey] ?: state.shrinkTargetInWindow)?.translate(-origin),
                        shrinkStartMillis = machine.shrinkStartMillis,
                        letterPop = machine.letterPop,
                        pulse = machine.pulseScale(),
                        overlayAlpha = machine.overlayAlpha(),
                    )
                },
            )
            // Swallow every touch until the board has landed, so nothing under the ink reacts before the clock starts.
            Box(
                Modifier.matchParentSize().pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) awaitPointerEvent().changes.forEach { it.consume() }
                    }
                },
            )
        }
    }
}

/**
 * Stateless drawing of one [LetterMorphFrame], given the layer's own rect (0, 0, width, height). Used by
 * [MorphOverlayHost]; previews and screenshot tests call it with a fixed frame.
 */
@Composable
public fun BoxScope.LetterMorphLayer(letter: String, frame: (screen: Rect) -> LetterMorphFrame) {
    val flow = LexiTheme.motion.flow
    val letterStyle = LexiTheme.typography.wheelLetter.copy(
        fontSize = flow.letterStartSize.sp,
        color = LexiTheme.colors.yellow,
    )
    // Ink: a full-size layer moved and scaled onto the frame's rect (transform only, no re-layout).
    Box(
        Modifier
            .matchParentSize()
            .graphicsLayer {
                val f = frame(Rect(Offset.Zero, size))
                alpha = if (f.visible) f.overlayAlpha else 0f
                transformOrigin = TransformOrigin(0f, 0f)
                translationX = f.inkRect.left
                translationY = f.inkRect.top
                scaleX = if (size.width > 0f) f.inkRect.width / size.width else 0f
                scaleY = if (size.height > 0f) f.inkRect.height / size.height else 0f
            }
            .drawBehind { drawRect(frame(Rect(Offset.Zero, size)).inkColor) },
    )
    // Big letter: measured once at the start size, centred on the ink rect and scaled.
    Box(
        Modifier
            .matchParentSize()
            .graphicsLayer {
                val f = frame(Rect(Offset.Zero, size))
                alpha = if (f.visible) f.letterAlpha * f.overlayAlpha else 0f
                translationX = f.inkRect.center.x - size.width / 2f
                translationY = f.inkRect.center.y - size.height / 2f
                scaleX = f.letterScale
                scaleY = f.letterScale
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(letter, style = letterStyle, softWrap = false)
    }
}

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LetterMorphLayerPreview() {
    LexiTheme(reducedMotion = false) {
        val motion = LexiTheme.motion
        val colors = LexiTheme.colors
        val timeline = LetterMorphTimeline(motion.flow, motion.mechanical, motion.pop, colors.red, colors.ink)
        Box(Modifier.fillMaxSize().drawBehind { drawRect(colors.cream) }) {
            LetterMorphLayer("A") { screen ->
                // Roughly where the dialog's OYNA button sits, as parts of the screen.
                val source = Rect(
                    screen.width * 0.05f,
                    screen.height * 0.83f,
                    screen.width * 0.95f,
                    screen.height * 0.93f,
                )
                timeline.frameAt(motion.flow.inkExpand.delayMillis + 160L, source, screen, null, null)
            }
        }
    }
}
