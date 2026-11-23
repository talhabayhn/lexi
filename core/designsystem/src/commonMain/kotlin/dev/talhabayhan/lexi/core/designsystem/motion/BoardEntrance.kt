package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.fadeWithShadowRoom
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/**
 * The game board's entrance under the letter morph (`motion.md` #16): each piece fades in and rises (or pops from a
 * scale) on its [MotionStep], counted from the start of the ink's shrink ([MorphOverlayState.boardClockMillis]).
 * Before the shrink the pieces are hidden under the ink; after the morph (or without one) they are at rest. Values
 * are read only in the draw phase (`graphicsLayer` for the move, [fadeWithShadowRoom] for the fade), so the entrance
 * never recomposes or re-lays out the board. Its end state is derived, not animated: once the morph is over or
 * cancelled ([MorphOverlayState.boardClockMillis]) every piece is at rest, whatever happened to the frames before.
 * In tier Lite ([MorphOverlayState.boardStagger] false) a group enters together.
 */
@Stable
public class BoardEntrance internal constructor(
    private val state: MorphOverlayState,
    private val density: Density,
    private val mechanical: Easing,
    private val pop: Easing,
    /** Room for the pieces' hard shadows while they fade (`LexiShadows.fadeOutsets`, [fadeWithShadowRoom]). */
    public val shadowRoom: LayerOutsets = LayerOutsets.Zero,
) {
    /**
     * 0..1 progress of [step] for the [index]-th piece of a group [staggerMillis] apart, on `mechanical`, on `pop`
     * ([popEasing]) or unchanged ([linear]: the prototype's fades of the ring chips and the décor).
     */
    public fun progress(
        step: MotionStep,
        index: Int = 0,
        staggerMillis: Int = 0,
        popEasing: Boolean = false,
        linear: Boolean = false,
    ): Float {
        val clock = state.boardClockMillis()
        if (clock == Long.MAX_VALUE) return 1f
        val stagger = if (state.boardStagger) staggerMillis else 0
        val fraction = step.staggered(index, stagger).progressAt(clock)
        return when {
            linear -> fraction
            popEasing -> pop.transform(fraction)
            else -> mechanical.transform(fraction)
        }
    }

    private data class PieceKey(
        val step: MotionStep,
        val index: Int,
        val staggerMillis: Int,
        val offset: Dp,
        val startScale: Float,
        val popEasing: Boolean,
        val linear: Boolean,
    )

    private val pieces = HashMap<PieceKey, Modifier>()

    /**
     * A piece that fades in and moves [offset] (vertical; negative = from above) back to rest, or scales from
     * [startScale] to 1 ([popEasing] for the centre circle's pop; [linear] for a plain fade). The same arguments return
     * the same [Modifier] instance, so a recomposition of the board (a key tap, a timer tick) skips the piece.
     */
    public fun piece(
        step: MotionStep,
        index: Int = 0,
        staggerMillis: Int = 0,
        offset: Dp = 0.dp,
        startScale: Float = 1f,
        popEasing: Boolean = false,
        linear: Boolean = false,
    ): Modifier = pieces.getOrPut(PieceKey(step, index, staggerMillis, offset, startScale, popEasing, linear)) {
        val offsetPx = with(density) { offset.toPx() }
        Modifier
            .graphicsLayer {
                val p = progress(step, index, staggerMillis, popEasing, linear)
                translationY = (1f - p) * offsetPx
                if (startScale != 1f) {
                    val scale = startScale + (1f - startScale) * p
                    scaleX = scale
                    scaleY = scale
                }
            }
            .fadeWithShadowRoom(shadowRoom) { progress(step, index, staggerMillis, popEasing, linear) }
    }
}

/**
 * The board entrance for a screen composed **while** a letter morph runs (the game opened under the ink); null when
 * the screen was opened without one (restored game, review, daily, tests): its pieces are simply there.
 */
@Composable
public fun rememberBoardEntrance(): BoardEntrance? {
    val state = LocalMorphOverlay.current ?: return null
    val motion = LexiTheme.motion
    val density = LocalDensity.current
    val entering = remember(state) { state.isRunning }
    if (!entering) return null
    val shadowRoom = LexiTheme.shadows.fadeOutsets
    return remember(state, density, motion, shadowRoom) {
        BoardEntrance(state, density, motion.mechanical, motion.pop, shadowRoom)
    }
}
