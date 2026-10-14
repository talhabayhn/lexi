package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.modifier.hardShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.platform.haptics.HapticKind
import dev.talhabayhan.lexi.core.platform.sound.SoundEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Shared press behaviour of every tappable Lexi element (`docs/design/motion.md` #1): on press the element
 * translates by its [shadow] offset while the shadow collapses to 0 (80 ms, CSS ease-out); on release it returns
 * (80 ms). A quick tap still plays the full press before returning. Reduced motion: no translation, the shadow
 * disappears instantly while pressed (a shape change only).
 *
 * On click it plays [haptic] through `LocalHaptics` and [sound] through `LocalSoundPlayer` (pass `null` to skip),
 * then calls [onClick]. No ripple: the press translation is the indication. Elements without a shadow do not move.
 *
 * As in CSS (`box-sizing: border-box`), [border] is part of the element: a fixed size includes it and the content is
 * inset by its width, so a design `padding: 0 20px` is `Modifier.padding(horizontal = 20.dp)` on the content.
 * Layout and touch target come from [modifier]: interactive elements must be at least
 * `LexiTheme.dimens.minTouchTarget` on each side. Pass an [interactionSource] to derive extra pressed visuals
 * (e.g. a key turning yellow).
 */
@Composable
public fun PressableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = LexiTheme.shapes.square,
    color: Color = LexiTheme.colors.white,
    border: BorderStroke? = BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
    shadow: LexiShadow? = LexiTheme.shadows.s5,
    haptic: HapticKind? = HapticKind.Light,
    sound: SoundEffect? = SoundEffect.Tap,
    role: Role = Role.Button,
    onClickLabel: String? = null,
    interactionSource: MutableInteractionSource? = null,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val motion = LexiTheme.motion
    val pressProgress = rememberPressProgress(source, enabled, motion)
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    PressableSurfaceLayout(
        pressProgress = { pressProgress.value },
        translateOnPress = !motion.reducedMotion,
        shape = shape,
        color = color,
        border = border,
        shadow = shadow,
        contentAlignment = contentAlignment,
        modifier = modifier.clickable(
            interactionSource = source,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
        ) {
            haptic?.let(haptics::perform)
            sound?.let(sounds::play)
            onClick()
        },
        content = content,
    )
}

/**
 * Stateless visuals of [PressableSurface] at a given [pressProgress] (0 = idle, 1 = fully pressed); used directly by
 * screenshot tests to pin the pressed state.
 */
@Composable
internal fun PressableSurfaceLayout(
    pressProgress: () -> Float,
    translateOnPress: Boolean,
    shape: Shape,
    color: Color,
    border: BorderStroke?,
    shadow: LexiShadow?,
    contentAlignment: Alignment,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val pressTranslation = if (shadow != null && translateOnPress) {
        Modifier.graphicsLayer {
            val shift = shadow.offset.toPx() * pressProgress()
            translationX = shift
            translationY = shift
        }
    } else {
        Modifier
    }
    val shadowModifier = if (shadow != null) {
        Modifier.hardShadow(shadow.offset, shadow.color, shape, visibleFraction = { 1f - pressProgress() })
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(pressTranslation)
            .then(shadowModifier)
            .clip(shape)
            .background(color)
            // CSS box model: the border takes layout space, content starts inside it.
            .then(if (border != null) Modifier.border(border, shape).padding(border.width) else Modifier),
        contentAlignment = contentAlignment,
        content = content,
    )
}

/** 80 ms CSS ease-out per direction (`.lx-press` in `source/15-motion.dc.html`); instant under reduced motion. */
internal fun pressAnimationSpec(motion: LexiMotion): FiniteAnimationSpec<Float> =
    if (motion.reducedMotion) snap() else tween(durationMillis = motion.pressDurationMillis, easing = motion.press)

@Composable
private fun rememberPressProgress(source: InteractionSource, enabled: Boolean, motion: LexiMotion): State<Float> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(source, motion) {
        val spec = pressAnimationSpec(motion)
        var pressJob: Job? = null
        source.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    pressJob?.cancel()
                    pressJob = launch { progress.animateTo(1f, spec) }
                }

                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    val pressing = pressJob
                    pressJob = launch {
                        // Let a quick tap finish its press so it is always visible, then return.
                        pressing?.join()
                        progress.animateTo(0f, spec)
                    }
                }
            }
        }
    }
    LaunchedEffect(enabled) {
        if (!enabled) progress.snapTo(0f)
    }
    return progress.asState()
}

@Preview
@Composable
private fun PressableSurfacePreview() {
    LexiTheme(reducedMotion = false) {
        Box(Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20)) {
            PressableSurface(onClick = {}, color = LexiTheme.colors.red, modifier = Modifier.height(52.dp)) {
                Text(
                    text = "Birincil",
                    style = LexiTheme.typography.button16,
                    color = LexiTheme.colors.white,
                    modifier = Modifier.padding(horizontal = LexiTheme.dimens.gap20),
                )
            }
        }
    }
}
