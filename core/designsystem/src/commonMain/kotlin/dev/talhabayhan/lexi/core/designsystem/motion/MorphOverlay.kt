package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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

/**
 * State of the letter morph (`motion.md` #16) drawn by a [MorphOverlayHost] above the NavHost. [start] begins it from
 * the anchor `sourceKey` (OYNA button); the ink then holds the full screen until [markReady] is called (game data
 * loaded) *and* the anchor `targetKey` (wheel marker) is laid out, and only then shrinks into it. The game clock must
 * start from [onFinished] of [MorphOverlayHost], never earlier.
 */
@Stable
public class MorphOverlayState(public val anchors: MorphAnchors = MorphAnchors()) {
    internal var run: MorphRun? by mutableStateOf(null)
        private set
    internal var elapsedMillis by mutableLongStateOf(0L)
    internal var shrinkStartMillis: Long? by mutableStateOf(null)

    /** Target window rect when the shrink started; used if the marker's anchor goes away mid-shrink. */
    internal var shrinkTargetInWindow: Rect? by mutableStateOf(null)
    internal var ready by mutableStateOf(false)
        private set

    public val isRunning: Boolean get() = run != null

    /** Starts a morph; false (nothing happens) when one runs already or [sourceKey] has no laid-out anchor. */
    public fun start(sourceKey: Any, targetKey: Any, letter: String): Boolean {
        if (run != null) return false
        val source = anchors[sourceKey] ?: return false
        run = MorphRun(source, targetKey, letter)
        elapsedMillis = 0L
        shrinkStartMillis = null
        shrinkTargetInWindow = null
        ready = false
        return true
    }

    /** The game is ready (data loaded, board composed under the ink): the shrink may start. */
    public fun markReady() {
        if (run != null) ready = true
    }

    internal fun finish() {
        run = null
        shrinkStartMillis = null
        shrinkTargetInWindow = null
        ready = false
    }
}

/** One morph: window rect of the source when it started, the target anchor key and the big letter. */
internal class MorphRun(val sourceInWindow: Rect, val targetKey: Any, val letter: String)

/**
 * Hosts [content] (the NavHost) and draws the letter morph above it. Provides [LocalMorphAnchors] so
 * `Modifier.morphAnchor(key)` works on any screen below. All per-frame values are read in `graphicsLayer` /
 * `drawBehind` lambdas, so a running morph never recomposes or re-lays out anything: the ink is one full-screen layer
 * scaled and translated onto the current rectangle, the letter one text layer measured once and scaled.
 * While a morph runs the host swallows all touches (phase 3 must also block system back).
 *
 * [frameMonitor] (optional, fed by the host itself) measures only the moving parts — expand + letter pop, and the
 * shrink — not the hold, during which the NavHost builds the game screen under the ink; a slow first composition of
 * the game must not downgrade the whole session to [MotionTier.Lite]. If the host leaves the composition mid-morph the
 * state is reset and [onFinished] is not called.
 */
@Composable
public fun MorphOverlayHost(
    state: MorphOverlayState,
    modifier: Modifier = Modifier,
    frameMonitor: FrameMonitor? = null,
    onFinished: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val motion = LexiTheme.motion
    val colors = LexiTheme.colors
    val timeline = remember(motion, colors) {
        LetterMorphTimeline(motion.flow, motion.mechanical, motion.pop, colors.red, colors.ink)
    }
    val hostOrigin = remember { mutableStateOf(Offset.Zero) }
    val run = state.run

    val currentOnFinished by rememberUpdatedState(onFinished)
    if (frameMonitor != null) FrameMonitorEffect(frameMonitor)

    LaunchedEffect(run) {
        if (run == null) return@LaunchedEffect
        val flow = motion.flow
        val expandEnd = maxOf(flow.inkExpand.endMillis, flow.letterIn.endMillis).toLong()
        var watching = false
        fun watch(on: Boolean) {
            if (on == watching || frameMonitor == null) return
            if (on) frameMonitor.beginTransition() else frameMonitor.endTransition()
            watching = on
        }
        var completed = false
        try {
            val startMillis = withFrameMillis { it }
            while (!completed) {
                withFrameMillis { now ->
                    val elapsed = now - startMillis
                    state.elapsedMillis = elapsed
                    if (state.shrinkStartMillis == null && state.ready) {
                        val target = state.anchors[run.targetKey]
                        if (target != null && elapsed >= flow.inkShrink.delayMillis) {
                            state.shrinkTargetInWindow = target
                            state.shrinkStartMillis = timeline.shrinkStartFor(elapsed)
                        }
                    }
                    watch(elapsed in flow.inkExpand.delayMillis..expandEnd || state.shrinkStartMillis != null)
                    completed = timeline.isFinished(elapsed, state.shrinkStartMillis)
                }
            }
        } finally {
            watch(false)
            state.finish()
        }
        if (completed) currentOnFinished()
    }

    Box(modifier.fillMaxSize().onGloballyPositioned { hostOrigin.value = it.positionInWindow() }) {
        CompositionLocalProvider(LocalMorphAnchors provides state.anchors) { content() }
        if (run != null) {
            LetterMorphLayer(
                letter = run.letter,
                frame = { screen ->
                    val origin = hostOrigin.value
                    timeline.frameAt(
                        elapsedMillis = state.elapsedMillis,
                        source = run.sourceInWindow.translate(-origin),
                        screen = screen,
                        target = (state.anchors[run.targetKey] ?: state.shrinkTargetInWindow)?.translate(-origin),
                        shrinkStartMillis = state.shrinkStartMillis,
                    )
                },
            )
            // Swallow every touch so nothing under the ink (dialog, game board) reacts before the clock starts.
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
                alpha = if (f.visible) 1f else 0f
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
                alpha = if (f.visible) f.letterAlpha else 0f
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
