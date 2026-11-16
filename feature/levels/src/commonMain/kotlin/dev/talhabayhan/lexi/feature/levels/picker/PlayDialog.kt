package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMorphKeys
import dev.talhabayhan.lexi.core.designsystem.motion.morphAnchor
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.feature.levels.generated.resources.Res
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_close
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_meta
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_mode_classic
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_mode_classic_hint
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_mode_hardcore
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_mode_hardcore_hint
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_mode_label
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_mode_practice
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_mode_practice_hint
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_play
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_time_label
import dev.talhabayhan.lexi.feature.levels.generated.resources.dialog_time_none
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_mixed_fallback
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The mode / time dialog (`video-flow-notes.md` §3, `cats_v3.png`) over the scrim. **Phase 2 placeholder
 * presentation:** it fades and scales in from 0.92 (`FlowMotion.liteFlipStartScale` over `dialogGrow`, `mechanical`);
 * phase 3 replaces it with the card flip (#13–#15). Reduced motion: a cross-fade. The OYNA button carries the
 * letter morph's source anchor ([FlowMorphKeys.PLAY_BUTTON]).
 */
@Composable
internal fun PlayDialog(state: CategoryPickerUiState, turkish: Boolean, onIntent: (CategoryPickerIntent) -> Unit) {
    val motion = LexiTheme.motion
    // Keeps the last card while the dialog animates out.
    var shown by remember { mutableStateOf(state.dialog) }
    if (state.dialog != null) shown = state.dialog
    val visible = state.dialog != null
    AnimatedVisibility(visible = visible, enter = fadeIn(scrimSpec(motion)), exit = fadeOut(scrimSpec(motion))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(LexiTheme.colors.scrim)
                .clearAndSetSemantics {}
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    onIntent(CategoryPickerIntent.CloseDialog)
                },
        )
    }
    AnimatedVisibility(
        visible = visible,
        enter = panelEnter(motion),
        exit = panelExit(motion),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).lexiContentWidth(),
            contentAlignment = Alignment.Center,
        ) {
            shown?.let { card -> DialogPanel(card, state, turkish, onIntent) }
        }
    }
}

private fun scrimSpec(motion: LexiMotion) = tween<Float>(
    if (motion.reducedMotion) motion.flow.reducedCrossfadeMillis else motion.flow.scrimIn.durationMillis,
    easing = motion.linear,
)

private fun panelEnter(motion: LexiMotion): EnterTransition {
    val flow = motion.flow
    if (motion.reducedMotion) return fadeIn(tween(flow.reducedCrossfadeMillis, easing = motion.linear))
    val spec = tween<Float>(flow.dialogGrow.durationMillis, easing = motion.mechanical)
    return fadeIn(spec) + scaleIn(spec, initialScale = flow.liteFlipStartScale)
}

private fun panelExit(motion: LexiMotion): ExitTransition {
    val flow = motion.flow
    if (motion.reducedMotion) return fadeOut(tween(flow.reducedCrossfadeMillis, easing = motion.linear))
    val spec = tween<Float>(flow.dialogCollapse.durationMillis, easing = motion.turn)
    return fadeOut(spec) + scaleOut(spec, targetScale = flow.liteFlipStartScale)
}

@Composable
private fun DialogPanel(
    card: PickerCategoryUi,
    state: CategoryPickerUiState,
    turkish: Boolean,
    onIntent: (CategoryPickerIntent) -> Unit,
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val typography = LexiTheme.typography
    val name = (if (turkish) card.nameTr else card.nameEn)
        .ifEmpty { if (card.isMixed) stringResource(Res.string.picker_mixed_fallback) else "" }
    Column(
        modifier = Modifier
            .padding(horizontal = dimens.screenPaddingHorizontal, vertical = dimens.gap16)
            .fillMaxWidth()
            .lexiSurface(
                colors.cream,
                BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = LexiTheme.shadows.s6,
            )
            // Swallows taps on the panel itself (they would otherwise reach the scrim and close it); no semantics.
            .pointerInput(Unit) { detectTapGestures { } }
            .semantics {
                paneTitle = name
                isTraversalGroup = true
            }
            .verticalScroll(rememberScrollState())
            .padding(dimens.gap16),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavSquareButton(
                icon = LexiIcons.Back,
                contentDescription = stringResource(Res.string.dialog_close),
                onClick = { onIntent(CategoryPickerIntent.CloseDialog) },
                size = IconButtonDefaults.NavSizeLarge,
            )
            Column {
                Text(name, style = typography.heading22, color = colors.ink)
                Text(
                    stringResource(Res.string.dialog_meta, state.level.name, card.total, card.learned),
                    style = typography.caption12SemiBold,
                    color = colors.ink,
                )
            }
        }
        SectionLabel(Res.string.dialog_mode_label)
        val modeIndex = PickerModes.indexOf(state.mode).coerceAtLeast(0)
        SegmentedControl(
            segments = PickerModes.map { Segment(stringResource(it.titleRes())) },
            selectedIndex = modeIndex,
            onSelect = { onIntent(CategoryPickerIntent.SelectMode(PickerModes[it])) },
            height = LexiTheme.dimens.minTouchTargetAccessible,
            role = Role.RadioButton,
            titleStyle = typography.bodyStrong16,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(state.mode.hintRes()),
            style = typography.body14,
            color = colors.ink,
            modifier = Modifier.padding(top = dimens.gap8),
        )
        SectionLabel(Res.string.dialog_time_label)
        SegmentedControl(
            segments = state.timeOptions.map { Segment(formatClock(it)) },
            selectedIndex = state.timeOptions.indexOf(state.timeSeconds),
            onSelect = { onIntent(CategoryPickerIntent.SelectTime(state.timeOptions[it])) },
            height = LexiTheme.dimens.minTouchTargetAccessible,
            role = Role.RadioButton,
            enabled = state.timeEnabled,
            titleStyle = typography.bodyStrong16,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!state.timeEnabled) {
            Text(
                stringResource(Res.string.dialog_time_none),
                style = typography.caption12SemiBold,
                color = colors.inkMuted,
                modifier = Modifier.padding(top = dimens.gap6),
            )
        }
        LexiButton(
            onClick = { onIntent(CategoryPickerIntent.Play) },
            modifier = Modifier
                .padding(top = dimens.gap18)
                .fillMaxWidth()
                .morphAnchor(FlowMorphKeys.PLAY_BUTTON),
        ) {
            Text(stringResource(Res.string.dialog_play), style = typography.button24, color = colors.white)
            Icon(
                LexiIcons.Play,
                contentDescription = null,
                tint = colors.white,
                modifier = Modifier.padding(start = dimens.gap10).size(IconButtonDefaults.IconSizeMedium),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: StringResource) {
    Text(
        stringResource(text),
        style = LexiTheme.typography.label12,
        color = LexiTheme.colors.inkMuted,
        modifier = Modifier.padding(top = LexiTheme.dimens.gap16, bottom = LexiTheme.dimens.gap6),
    )
}

private fun GameMode.titleRes(): StringResource = when (this) {
    GameMode.PRACTICE -> Res.string.dialog_mode_practice
    GameMode.HARDCORE -> Res.string.dialog_mode_hardcore
    else -> Res.string.dialog_mode_classic
}

private fun GameMode.hintRes(): StringResource = when (this) {
    GameMode.PRACTICE -> Res.string.dialog_mode_practice_hint
    GameMode.HARDCORE -> Res.string.dialog_mode_hardcore_hint
    else -> Res.string.dialog_mode_classic_hint
}
