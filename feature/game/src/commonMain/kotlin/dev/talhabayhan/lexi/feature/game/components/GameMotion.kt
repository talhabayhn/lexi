package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.feature.game.GameEffect

/**
 * Transient motion of the game screen (ADR 0019 §3): effects start short animations that begin and end on the resting
 * look from `GameUiState`; values are read in the layer/draw phase only. Under reduced motion transforms are skipped.
 *
 * Hook point of the motion task (motion.md #1–#8): [play] is where the remaining animations, haptics
 * (`LocalHaptics`) and sounds (`LocalSoundPlayer`) are triggered, in sync with the animation.
 */
@Stable
internal class GameMotion(private val motion: LexiMotion) {
    private val shake = Animatable(0f)

    /** #4 horizontal offset of the answer slots. */
    val slotsOffset: Dp get() = shake.value.dp

    suspend fun play(effect: GameEffect) {
        when (effect) {
            is GameEffect.Wrong -> shakeSlots()

            // #2 fly, #3 pop, #5 pass, #6 advance, #7 combo, #8 tick: motion task (M4).
            is GameEffect.LetterPlaced,
            is GameEffect.Correct,
            is GameEffect.Passed,
            is GameEffect.Advanced,
            is GameEffect.ComboReached,
            is GameEffect.TimeWarning,
            GameEffect.NavigateToResult,
            GameEffect.Abandoned,
            -> Unit
        }
    }

    /** motion.md #4: −12, 12, −9, 9, −4, 0dp, evenly spaced over 360 ms. */
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
}

@Composable
internal fun rememberGameMotion(motion: LexiMotion): GameMotion = remember(motion) { GameMotion(motion) }
