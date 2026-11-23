package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.talhabayhan.lexi.core.designsystem.modifier.fadeWithShadowRoom
import dev.talhabayhan.lexi.core.designsystem.motion.BoardEntrance
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMotion
import dev.talhabayhan.lexi.core.designsystem.motion.MotionStep
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.motion.rememberMotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.GameTurnUi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * `motion.md` #17, the turn change (video flow): the previous clue slides out 30dp to the left (`turn`) and the new one
 * in from 30dp right (`mechanical`); the previous answer slots fade (linear), the previous keys stay until the re-deal,
 * then the new slots and keys rise 12dp and fade in one after another (`mechanical`; tier Lite: together).
 *
 * Driven by the state, not by an effect: the engine publishes the next turn before its `Correct` / `Advanced` effects,
 * so the previous turn only exists in the composition that first shows the new one. That composition creates the swap
 * with the previous turn ([outgoing], composed as decorative "ghosts" until [ghostsShown] turns false) and its first
 * frame already draws the start of the motion. Plays on every turn change after the first (correct, pass, after a
 * wrong answer's reveal), like the centre letter (#6). All per-frame values are read in `graphicsLayer` lambdas.
 */
@Stable
internal class TurnSwap(
    val outgoing: GameTurnUi,
    private val flow: FlowMotion,
    private val motion: LexiMotion,
    private val stagger: Boolean,
    private val clueOffsetPx: Float,
    private val itemOffsetPx: Float,
    cells: Int,
    keys: Int,
    /** Keeps the hard shadows of the fading clue card, slots and keys drawn through their fades. */
    internal val shadowRoom: LayerOutsets = LayerOutsets.Zero,
) {
    /** Milliseconds since the new turn was first drawn. */
    internal val time = Animatable(0f)

    /** The previous turn's slots, keys and clue are still composed (decorative). */
    var ghostsShown: Boolean by mutableStateOf(true)
        private set

    private val slotStagger = if (stagger) flow.boardSlotStaggerMillis else 0
    private val keyStagger = if (stagger) flow.boardKeyStaggerMillis else 0

    /** The last of the previous turn's pieces has left. */
    internal val ghostMillis: Int =
        maxOf(flow.redealSlotsOut.endMillis, flow.redealKey.delayMillis, flow.clueOut.endMillis)

    internal val totalMillis: Int = maxOf(
        flow.clueIn.endMillis,
        flow.redealSlot.staggered(max(cells - 1, 0), slotStagger).endMillis,
        flow.redealKey.staggered(max(keys - 1, 0), keyStagger).endMillis,
        ghostMillis,
    )

    suspend fun run() {
        // Already landed by the fail-safe (ON_PAUSE before this started): never play time backwards.
        if (time.value >= totalMillis) {
            finish()
            return
        }
        try {
            time.animateTo(ghostMillis.toFloat(), tween(ghostMillis, easing = motion.linear))
            ghostsShown = false
            time.animateTo(totalMillis.toFloat(), tween(totalMillis - ghostMillis, easing = motion.linear))
        } finally {
            // Interrupted (the app went to the background, the screen left): end at rest, never mid-fade.
            withContext(NonCancellable) { finish() }
        }
    }

    /** Jumps to the end state: the previous turn gone, the new clue, slots and keys fully drawn. Idempotent. */
    suspend fun finish() {
        ghostsShown = false
        if (time.value < totalMillis) time.snapTo(totalMillis.toFloat())
    }

    private fun progress(step: MotionStep, easing: Easing): Float =
        easing.transform(step.progressAt(time.value.toLong()))

    /** The previous clue: out to the left while fading (`turn`). */
    val outgoingClue: Modifier = Modifier
        .graphicsLayer { translationX = -clueOffsetPx * progress(flow.clueOut, motion.turn) }
        .fadeWithShadowRoom(shadowRoom) { 1f - progress(flow.clueOut, motion.turn) }

    /** The new clue: hidden until [FlowMotion.clueIn], then in from the right (`mechanical`). */
    val incomingClue: Modifier = Modifier
        .graphicsLayer { translationX = clueOffsetPx * (1f - progress(flow.clueIn, motion.mechanical)) }
        .fadeWithShadowRoom(shadowRoom) { progress(flow.clueIn, motion.mechanical) }

    /** The previous answer slots fade out (linear). */
    val outgoingSlots: Modifier =
        Modifier.fadeWithShadowRoom(shadowRoom) { 1f - progress(flow.redealSlotsOut, motion.linear) }

    /** The previous keys stay until the new ones are dealt. */
    val outgoingKeys: Modifier = Modifier.graphicsLayer {
        alpha = if (time.value < flow.redealKey.delayMillis) 1f else 0f
    }

    fun slotProgress(index: Int): Float = progress(flow.redealSlot.staggered(index, slotStagger), motion.mechanical)

    fun keyProgress(index: Int): Float = progress(flow.redealKey.staggered(index, keyStagger), motion.mechanical)

    /**
     * Whether a tap on the new key [index] counts: not while the previous keys still show (they are decorative) and
     * not before this key has started to appear, so an invisible key is never typed. Read at tap time.
     */
    fun keyTakesInput(index: Int): Boolean = !ghostsShown && keyProgress(index) > 0f

    val itemOffset: Float get() = itemOffsetPx
}

/**
 * The [TurnSwap] for [turn], created in the composition that first shows a new turn letter; null for the first turn,
 * without a turn and in tier Minimal (reduced motion: the new turn is just there). Kept until the next change.
 */
@Composable
internal fun rememberTurnSwap(turn: GameTurnUi?): TurnSwap? {
    val motion = LexiTheme.motion
    val tier = rememberMotionTier()
    val density = LocalDensity.current
    val previous = rememberPrevious(turn)
    val shadowRoom = LexiTheme.shadows.fadeOutsets
    val swap = remember(turn?.letter) {
        if (turn == null || previous == null || previous.letter == turn.letter || tier == MotionTier.Minimal) {
            null
        } else {
            val flow = motion.flow
            TurnSwap(
                outgoing = previous,
                flow = flow,
                motion = motion,
                stagger = tier == MotionTier.Full,
                clueOffsetPx = with(density) { flow.clueOffset.toPx() },
                itemOffsetPx = with(density) { flow.boardItemOffset.toPx() },
                cells = turn.words.sumOf { it.size },
                keys = turn.keys.size,
                shadowRoom = shadowRoom,
            )
        }
    }
    LaunchedEffect(swap) { swap?.run() }
    // Fail-safe: a turn change interrupted by the background ends at rest (`motion.md` § Fail-safes).
    val scope = rememberCoroutineScope()
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { swap?.let { scope.launch { it.finish() } } }
    return swap
}

/**
 * #17 score count-up from [from] to [to] (`mechanical`, [FlowMotion.scoreCountUp]) with a pop 1.25 → 1 (`pop`,
 * [FlowMotion.scorePop]); read in the draw phase ([text]) and the layer phase ([scale]).
 */
@Stable
internal class ScoreCount(
    private val from: Int,
    private val to: Int,
    private val flow: FlowMotion,
    private val motion: LexiMotion,
) {
    internal val time = Animatable(0f)

    private val totalMillis = maxOf(flow.scoreCountUp.endMillis, flow.scorePop.endMillis)

    suspend fun run() {
        time.animateTo(totalMillis.toFloat(), tween(totalMillis, easing = motion.linear))
    }

    /** The value to draw, or null once the count has ended (the badge then shows the state's value). */
    fun text(): String? = value()?.toString()

    /** The counted value on screen now, or null once the count has ended. */
    fun value(): Int? {
        val t = time.value.toLong()
        if (t >= flow.scoreCountUp.endMillis) return null
        val p = motion.mechanical.transform(flow.scoreCountUp.progressAt(t))
        return lerp(from.toFloat(), to.toFloat(), p).roundToInt()
    }

    /** 1 until the pop starts, then 1.25 → 1 on `pop` (its overshoot dips below 1 before it settles). */
    fun scale(): Float {
        val t = time.value.toLong()
        if (t < flow.scorePop.delayMillis) return 1f
        return lerp(flow.scorePopScale, 1f, motion.pop.transform(flow.scorePop.progressAt(t)))
    }
}

/**
 * The [ScoreCount] for a badge showing [score]: created in the composition where the value rises (a correct answer);
 * null at first, after a drop (joker cost, restart) and in tier Minimal.
 */
@Composable
internal fun rememberScoreCount(score: Int): ScoreCount? {
    val motion = LexiTheme.motion
    val tier = rememberMotionTier()
    val previous = rememberPrevious(score)
    val running = remember { ScoreCountHolder() }
    val count = remember(score) {
        if (previous == null || score <= previous || tier == MotionTier.Minimal) {
            null
        } else {
            // A second rise while a count runs continues from the number on screen (no jump back).
            val from = running.count?.value() ?: previous
            ScoreCount(from = from, to = score, flow = motion.flow, motion = motion)
        }
    }
    SideEffect { running.count = count }
    LaunchedEffect(count) { count?.run() }
    return count
}

/** The value [current] had in the previous composition (null in the first). Not state: it never recomposes. */
@Composable
private fun <T : Any> rememberPrevious(current: T?): T? {
    val holder = remember { PreviousHolder<T>() }
    val previous = holder.value
    SideEffect { holder.value = current }
    return previous
}

private class ScoreCountHolder {
    var count: ScoreCount? = null
}

private class PreviousHolder<T : Any> {
    var value: T? = null
}

/**
 * [dealtPiece] per index, built once per entrance / re-deal and reused: the same [Modifier] instance comes back on every
 * recomposition (a key tap, a timer tick), so the cells and keys skip it instead of updating and re-recording each
 * piece. Same pixels and timing as calling [dealtPiece] directly.
 */
@Composable
internal fun rememberDealtPieces(
    entrance: BoardEntrance?,
    entranceStep: MotionStep,
    entranceStagger: Int,
    swap: TurnSwap?,
    redeal: TurnSwap.(Int) -> Float,
    itemOffsetPx: Float,
): (index: Int) -> Modifier = remember(entrance, entranceStep, entranceStagger, swap, redeal, itemOffsetPx) {
    val pieces = HashMap<Int, Modifier>()
    val piece: (Int) -> Modifier = { index ->
        pieces.getOrPut(index) {
            dealtPiece(index, entrance, entranceStep, entranceStagger, swap, redeal, itemOffsetPx)
        }
    }
    piece
}

/**
 * One `graphicsLayer` (the rise) and one [fadeWithShadowRoom] (the fade, with room for the piece's hard shadow) per
 * answer cell or key, combining the board entrance (#16: [entranceStep], [entranceStagger]) and the re-deal (#17:
 * [redeal]); both rise [BoardEntrance]'s 12dp and fade in. Without either it adds nothing.
 */
internal fun dealtPiece(
    index: Int,
    entrance: BoardEntrance?,
    entranceStep: MotionStep,
    entranceStagger: Int,
    swap: TurnSwap?,
    redeal: TurnSwap.(Int) -> Float,
    itemOffsetPx: Float,
): Modifier = if (entrance == null && swap == null) {
    Modifier
} else {
    Modifier
        .graphicsLayer {
            val entered = entrance?.progress(entranceStep, index, entranceStagger) ?: 1f
            val dealt = swap?.redeal(index) ?: 1f
            translationY = ((1f - entered) + (1f - dealt)) * itemOffsetPx
        }
        .fadeWithShadowRoom(entrance?.shadowRoom ?: swap?.shadowRoom ?: LayerOutsets.Zero) {
            (entrance?.progress(entranceStep, index, entranceStagger) ?: 1f) * (swap?.redeal(index) ?: 1f)
        }
}
