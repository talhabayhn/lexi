package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.talhabayhan.lexi.core.designsystem.component.WheelChipMotion
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.feature.game.GameEffect
import kotlinx.coroutines.delay

/**
 * Transient motion, haptics and sounds of the game screen (ADR 0019 §3, `motion.md` #2–#8). Effects start short
 * animations that begin and end on the resting look from `GameUiState`; their values are read in the layout / layer /
 * draw phase only. Feedback ([GameFeedback]) fires when an animation starts, so sound and motion stay in sync.
 * Reduced motion: no translation, scale or rotation (fly, pop, ring, shake, turn, arrow and burst are skipped); colour
 * and shape changes come from the state anyway; the combo badge appears in place; haptics and sounds still fire.
 *
 * - #2 [LetterFlight]: the letter flies from its key to its cell ([GameMotionLayer]); the cell keeps its look from
 *   before (empty, the red "next" border) until the letter lands. A completed answer (#3/#4/#5/#6) ends all flights.
 * - #3 pop + ring and #5 turn + morph + arrow: [chipMotion] for `LetterWheel`.
 * - #4 shake: [slotsOffset] of the answer slots (their red comes from the state's wrong cells).
 * - #6: the current square jumps with the state; the centre letter slides inside `LetterWheel`.
 * - #7 [ComboAnimation]: burst and badge in the HUD row next to the score ([GameMotionLayer]).
 * - #8: only feedback here; colours and the per-second counter pulse live in `TimerBar`.
 */
@Stable
internal class GameMotion(
    private val motion: LexiMotion,
    private val haptics: Haptics,
    private val sounds: SoundPlayer,
) {
    private val shake = Animatable(0f)

    /** #4 horizontal offset of the answer slots. */
    val slotsOffset: Dp get() = shake.value.dp

    /** #2 letters in the air, oldest first. */
    val flights: List<LetterFlight> get() = mutableFlights
    private val mutableFlights = mutableStateListOf<LetterFlight>()

    private val chips = mutableStateMapOf<Char, ChipAnimation>()

    /** #7 the combo on screen, if any. */
    var combo: ComboAnimation? by mutableStateOf(null)
        private set

    // Placement of the elements the overlay animates between (not state: read when an animation starts).
    private var overlay: LayoutCoordinates? = null
    private var hud: LayoutCoordinates? = null
    private val keys = HashMap<Int, LayoutCoordinates>()
    private val cells = HashMap<Int, LayoutCoordinates>()

    fun onOverlayPlaced(coordinates: LayoutCoordinates) {
        overlay = coordinates
    }

    fun onHudPlaced(coordinates: LayoutCoordinates) {
        hud = coordinates
    }

    fun onKeyPlaced(index: Int, coordinates: LayoutCoordinates) {
        keys[index] = coordinates
    }

    fun onCellPlaced(index: Int, coordinates: LayoutCoordinates) {
        cells[index] = coordinates
    }

    /** Answer cells whose letter is still in the air (#2). Reads state: call it where the cells are composed. */
    fun flyingCells(): Set<Int> = mutableFlights.mapTo(HashSet()) { it.cellIndex }

    /** #3 / #5 motion of the wheel chip of [letter], null at rest. */
    fun chipMotion(letter: Char): WheelChipMotion? = chips[letter]

    /** The HUD bar (pause, timer, score) in the overlay's coordinates, null before both are placed. */
    fun hudBounds(): Rect? = bounds(hud)

    suspend fun play(effect: GameEffect) {
        GameFeedback.of(effect)?.let { feedback ->
            feedback.haptic?.let(haptics::perform)
            feedback.sound?.let(sounds::play)
        }
        when (effect) {
            is GameEffect.LetterPlaced -> fly(effect)

            is GameEffect.Correct -> {
                mutableFlights.clear()
                animateChip(effect.letter, ChipAnimation.Kind.Correct, motion.correctDurationMillis)
            }

            is GameEffect.Wrong -> {
                mutableFlights.clear()
                shakeSlots()
            }

            is GameEffect.Passed -> {
                mutableFlights.clear()
                animateChip(effect.letter, ChipAnimation.Kind.Pass, motion.passDurationMillis)
            }

            is GameEffect.Advanced -> mutableFlights.clear()

            is GameEffect.ComboReached -> showCombo(effect.streak)

            is GameEffect.TimeWarning,
            is GameEffect.NavigateToResult,
            GameEffect.Abandoned,
            GameEffect.NavigateBack,
            GameEffect.NavigateToSettings,
            -> Unit
        }
    }

    /** #2: 220 ms on `fly`, travel then settle (see [LetterFlight]). */
    private suspend fun fly(effect: GameEffect.LetterPlaced) {
        if (motion.reducedMotion) return
        val from = bounds(keys[effect.keyIndex]) ?: return
        val to = bounds(cells[effect.cellIndex]) ?: return
        val flight = LetterFlight(effect.letter, effect.cellIndex, from, to, motion)
        mutableFlights += flight
        try {
            flight.time.animateTo(1f, tween(motion.keyFlyDurationMillis, easing = motion.linear))
        } finally {
            mutableFlights.remove(flight)
        }
    }

    /** #3 / #5 on one chip; a newer animation of the same chip replaces the older one. */
    private suspend fun animateChip(letter: Char, kind: ChipAnimation.Kind, durationMillis: Int) {
        if (motion.reducedMotion) return
        val animation = ChipAnimation(kind, motion)
        chips[letter] = animation
        try {
            animation.time.animateTo(1f, tween(durationMillis, easing = motion.linear))
        } finally {
            if (chips[letter] === animation) chips.remove(letter)
        }
    }

    /** #4: −12, 12, −9, 9, −4, 0dp, evenly spaced over 360 ms. */
    private suspend fun shakeSlots() {
        if (motion.reducedMotion) return
        val offsets = motion.wrongShakeOffsets
        val step = motion.wrongDurationMillis / offsets.size
        shake.snapTo(0f)
        shake.animateTo(
            targetValue = offsets.last().value,
            animationSpec = keyframes {
                durationMillis = step * offsets.size
                offsets.forEachIndexed { index, offset -> offset.value at step * (index + 1) }
            },
        )
    }

    /** #7: burst + badge pop (420 ms), hold, shrink; under reduced motion the badge just shows for pop + hold. */
    private suspend fun showCombo(streak: Int) {
        val animation = ComboAnimation(streak, motion)
        combo = animation
        try {
            if (motion.reducedMotion) {
                delay((motion.comboDurationMillis + motion.comboBadgeHoldMillis).toLong())
            } else {
                val total = animation.totalMillis
                animation.time.animateTo(total, tween(total.toInt(), easing = motion.linear))
            }
        } finally {
            if (combo === animation) combo = null
        }
    }

    private fun bounds(coordinates: LayoutCoordinates?): Rect? {
        val root = overlay?.takeIf { it.isAttached } ?: return null
        val target = coordinates?.takeIf { it.isAttached } ?: return null
        return root.localBoundingBoxOf(target, clipBounds = false)
    }
}

/**
 * #2: one letter in the air from [from] (its key) to [to] (its cell), in overlay pixels. Over `keyFlyDurationMillis`
 * it travels on `fly` while growing to `keyFlyPeakScale` (the first `keyFlyTravelFraction`), then settles back to scale
 * 1 on the cell (`aFly`). Its size goes from the key's to the cell's, so it lands exactly on the cell.
 */
@Stable
internal class LetterFlight(
    val letter: Char,
    val cellIndex: Int,
    private val from: Rect,
    private val to: Rect,
    private val motion: LexiMotion,
) {
    /** Linear time, 0..1. */
    internal val time = Animatable(0f)

    val bounds: Rect get() = lerp(from, to, travel(time.value))

    val scale: Float
        get() {
            val t = time.value
            val split = motion.keyFlyTravelFraction
            return if (t < split) {
                lerp(1f, motion.keyFlyPeakScale, motion.fly.at(t / split))
            } else {
                lerp(motion.keyFlyPeakScale, 1f, motion.fly.at((t - split) / (1f - split)))
            }
        }

    private fun travel(t: Float): Float = motion.fly.at(t / motion.keyFlyTravelFraction)
}

/**
 * #3 / #5 motion of one chip over linear time (0..1 of its duration).
 * - Correct (`aPop` / `aRing`): scale 1 → 1.45 → 1 on `pop` (peak at 1/3, back at 2/3); the ring grows 0.6 → 1 → 1.8
 *   on CSS ease-out while its opacity goes 0 → 1 → 0.
 * - Pass (`aPass` / `aArrow`): turn + morph on `turn`; the arrow slides on `turn` and is fully visible at
 *   `passArrowPeakFraction`.
 */
@Stable
internal class ChipAnimation(private val kind: Kind, private val motion: LexiMotion) : WheelChipMotion {
    enum class Kind { Correct, Pass }

    internal val time = Animatable(0f)

    override val popScale: Float
        get() {
            if (kind != Kind.Correct) return 1f
            val t = time.value
            val peak = motion.correctPeakFraction
            val end = motion.correctPopEndFraction
            return when {
                t < peak -> lerp(1f, motion.correctPopScale, motion.pop.at(t / peak))
                t < end -> lerp(motion.correctPopScale, 1f, motion.pop.at((t - peak) / (end - peak)))
                else -> 1f
            }
        }

    override val ringScale: Float
        get() {
            val t = time.value
            val peak = motion.correctPeakFraction
            return if (t < peak) {
                lerp(motion.correctRingStartScale, 1f, motion.easeOut.at(t / peak))
            } else {
                lerp(1f, motion.correctRingEndScale, motion.easeOut.at((t - peak) / (1f - peak)))
            }
        }

    override val ringAlpha: Float
        get() {
            if (kind != Kind.Correct) return 0f
            val t = time.value
            val peak = motion.correctPeakFraction
            return if (t < peak) {
                motion.easeOut.at(t / peak)
            } else {
                1f - motion.easeOut.at((t - peak) / (1f - peak))
            }
        }

    override val passProgress: Float
        get() = if (kind == Kind.Pass) motion.turn.at(time.value) else 1f

    override val arrowProgress: Float
        get() = if (kind == Kind.Pass) motion.turn.at(time.value) else 0f

    override val arrowAlpha: Float
        get() {
            if (kind != Kind.Pass) return 0f
            val t = time.value
            val peak = motion.passArrowPeakFraction
            return if (t < peak) t / peak else 1f - (t - peak) / (1f - peak)
        }
}

/**
 * #7 over elapsed milliseconds: the burst runs for `comboDurationMillis`; the badge pops in the same time (scale
 * 0 → 1.3 → 1, rotation −20° → 6° → −4° on `pop`), holds for `comboBadgeHoldMillis` and shrinks to 0 in
 * `comboBadgeExitMillis` (rotation back to 0). Under reduced motion the badge rests at scale 1, −4°.
 */
@Stable
internal class ComboAnimation(val streak: Int, private val motion: LexiMotion) {
    internal val totalMillis: Float =
        (motion.comboDurationMillis + motion.comboBadgeHoldMillis + motion.comboBadgeExitMillis).toFloat()

    internal val time = Animatable(0f)

    /** The burst is shown (not under reduced motion). */
    val bursts: Boolean get() = !motion.reducedMotion

    /** Burst progress, 0..1. */
    val burstProgress: Float get() = (time.value / motion.comboDurationMillis).coerceIn(0f, 1f)

    val badgeScale: Float
        get() = badge(peak = motion.comboBadgePeakScale, rest = 1f, exit = 0f, from = 0f)

    val badgeRotation: Float
        get() = badge(
            peak = motion.comboBadgePeakRotation,
            rest = motion.comboBadgeRestRotation,
            exit = 0f,
            from = motion.comboBadgeStartRotation,
        )

    /** One badge value through its keyframes: [from] → [peak] → [rest] (pop), hold, → [exit]. */
    private fun badge(peak: Float, rest: Float, exit: Float, from: Float): Float {
        if (motion.reducedMotion) return rest
        val ms = time.value
        val pop = motion.comboDurationMillis.toFloat()
        val peakAt = pop * motion.comboBadgePeakFraction
        val exitAt = pop + motion.comboBadgeHoldMillis
        return when {
            ms < peakAt -> lerp(from, peak, motion.pop.at(ms / peakAt))
            ms < pop -> lerp(peak, rest, motion.pop.at((ms - peakAt) / (pop - peakAt)))
            ms < exitAt -> rest
            else -> lerp(rest, exit, motion.pop.at(((ms - exitAt) / motion.comboBadgeExitMillis).coerceAtMost(1f)))
        }
    }
}

/** The easing's value at linear progress [fraction] (clamped to 0..1). */
private fun Easing.at(fraction: Float): Float = transform(fraction.coerceIn(0f, 1f))

@Composable
internal fun rememberGameMotion(motion: LexiMotion): GameMotion {
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    return remember(motion, haptics, sounds) { GameMotion(motion, haptics, sounds) }
}
