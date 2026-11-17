package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipChoreography
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipFrame
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipMachine
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipPhase
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMorphOverlay
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.motion.rememberMotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import kotlinx.coroutines.flow.first

/**
 * Motion #13–#15 of "Çarkını seç": one [DialogFlipMachine] ticked by the frame clock. The only state written per frame
 * is [version], read inside `graphicsLayer` / draw lambdas ([frame], [itemProgress], [dialogRect]), so a frame never
 * recomposes or relayouts; composition reads [phase], which changes a few times per run.
 *
 * [card] is the last card the dialog was opened for (kept while the dialog animates out). Card rectangles and the
 * panel's own rectangle are captured in root coordinates when they are laid out (before their animated layers).
 */
@Stable
internal class PickerDialogFlip(private val machine: DialogFlipMachine) {
    private var version by mutableLongStateOf(0L)
    private var cachedVersion = -1L
    private var cachedFrame: DialogFlipFrame? = null

    private val phaseState = derivedStateOf {
        version
        machine.phase
    }

    val phase: DialogFlipPhase get() = phaseState.value

    /** The dialog (or its scrim) is on screen. */
    val dialogShown: Boolean get() = phase != DialogFlipPhase.Closed

    /** The dialog is at rest and its controls take input. */
    val dialogTakesInput: Boolean get() = phase == DialogFlipPhase.Open

    /** A run is playing (the card and the panel carry their animated layers). */
    val animating: Boolean get() = phase == DialogFlipPhase.Opening || phase == DialogFlipPhase.Closing

    /** The tier latched by the current (or last) run. */
    val tier: MotionTier get() = machine.tier

    val isRunning: Boolean get() = machine.isRunning

    var card: PickerCategoryUi? by mutableStateOf(null)
        internal set

    /** Category cards' rectangles in root coordinates, written on layout (plain map: read in the draw phase only). */
    val cardRects: MutableMap<CategoryId, Rect> = HashMap()

    /** The panel's laid-out rectangle in root coordinates. */
    var panelRect: Rect? by mutableStateOf(null)

    fun frame(): DialogFlipFrame {
        val current = version
        val cached = cachedFrame
        if (cached != null && cachedVersion == current) return cached
        return machine.frame().also {
            cachedFrame = it
            cachedVersion = current
        }
    }

    fun itemProgress(index: Int): Float {
        version
        return machine.itemProgress(index)
    }

    /**
     * The panel's current rectangle in its own coordinates ([size] = its laid-out size): the tapped card's rectangle
     * (lifted by [liftPx]) at grow 0, the panel at grow 1. Null when the dialog does not grow from the card: tier Lite or
     * Minimal, or a rectangle that is not known (the caller then shows the Lite look).
     */
    fun dialogRect(size: Size, liftPx: Float): Rect? {
        val grow = frame().dialogGrow
        if (machine.tier != MotionTier.Full) return null
        val panel = panelRect ?: return null
        val from = card?.let { cardRects[it.id] } ?: return null
        val start = from.translate(-panel.left, -panel.top + liftPx)
        return lerp(start, Rect(Offset.Zero, size), grow)
    }

    fun snapTo(open: Boolean, tier: MotionTier) {
        machine.snapTo(open, tier)
        version++
    }

    fun request(open: Boolean, tier: MotionTier) {
        if (open) machine.open(tier) else machine.close(tier)
        version++
    }

    /** Ticks the machine by frame deltas until it rests; the first frame of a run is its time 0. */
    suspend fun runFrames() {
        var last = NO_FRAME
        while (machine.isRunning) {
            withFrameMillis { now ->
                machine.tick(if (last == NO_FRAME) 0L else now - last)
                last = now
                version++
            }
        }
    }

    private companion object {
        const val NO_FRAME = Long.MIN_VALUE
    }
}

/** A laid-out element's rectangle in root coordinates. */
internal fun LayoutCoordinates.rectInRoot(): Rect = Rect(positionInRoot(), size.toSize())

/**
 * The picker's dialog flip for [dialog] (the VM's open card, null = closed). A screen that starts with a dialog (process
 * restore) shows it at rest; later changes play the open / close run in the tier of the moment (latched per run), or
 * reverse a running one. Each run is registered with the session's frame monitor.
 */
@Composable
internal fun rememberPickerDialogFlip(dialog: PickerCategoryUi?): PickerDialogFlip {
    val motion = LexiTheme.motion
    val tier = rememberMotionTier()
    val open = dialog != null
    val flip = remember {
        PickerDialogFlip(
            DialogFlipMachine(DialogFlipChoreography(motion.flow, motion.mechanical, motion.turn, motion.pop)),
        ).also {
            it.card = dialog
            it.snapTo(open, tier)
        }
    }
    if (dialog != null && flip.card != dialog) flip.card = dialog
    val currentTier by rememberUpdatedState(tier)
    val monitor = LocalMotionPolicy.current.frameMonitor
    val morph = LocalMorphOverlay.current
    LaunchedEffect(flip, open) {
        // OYNA closes the dialog and starts the morph in the same frame, in either order: decide one frame later.
        if (!open && morph != null) withFrameMillis { }
        if (!open && morph != null && morph.isRunning) {
            // OYNA: the dialog stays at rest under the letter morph's ink (no close competing with the expand, nothing
            // measured); once the morph is over (the game covers this screen, or it was cancelled) it closes as usual.
            snapshotFlow { morph.isRunning }.first { !it }
        }
        flip.request(open, currentTier)
        if (!flip.isRunning) return@LaunchedEffect
        monitor.beginTransition()
        try {
            flip.runFrames()
        } finally {
            monitor.endTransition()
        }
    }
    return flip
}
