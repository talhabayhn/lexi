package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.component.FlowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.component.WithCappedFontScale
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipChoreography
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipMachine
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipPhase
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMorphKeys
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMotion
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMorphAnchors
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMorphOverlay
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.motion.morphAnchor
import dev.talhabayhan.lexi.core.designsystem.motion.perspective
import dev.talhabayhan.lexi.core.designsystem.text.WordSafeText
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
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
 * The mode / time dialog (`video-flow-notes.md` §3, `cats_v3.png`) over the scrim, with motion #14 / #15 from [flip]:
 * Full, the panel turns in on Y from the tapped card's rectangle while growing to its own (its frame is drawn at the
 * current rectangle, the content stays laid out at the final size, anchored top-left and clipped, as in the
 * prototype); Lite, it scales 0.92 → 1 and fades; reduced motion, a cross-fade. Everything moving is applied in
 * layer / draw lambdas. The controls take input once the dialog rests; its back arrow, the scrim and system back
 * always close it (reversing a running open). While it closes, a tap on the same card reopens it and other taps are
 * ignored. The OYNA button carries the letter morph's source anchor ([FlowMorphKeys.PLAY_BUTTON]).
 */
@Composable
internal fun PlayDialog(
    state: CategoryPickerUiState,
    turkish: Boolean,
    onIntent: (CategoryPickerIntent) -> Unit,
    flip: PickerDialogFlip,
) {
    val card = flip.card ?: return
    if (!flip.dialogShown) return
    val scrim = LexiTheme.colors.scrim
    val closing = flip.phase == DialogFlipPhase.Closing
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind { drawRect(scrim, alpha = flip.frame().scrimAlpha) }
            .clearAndSetSemantics { testTag = PlayDialogTags.SCRIM }
            .then(
                if (closing) {
                    Modifier
                } else {
                    Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onIntent(CategoryPickerIntent.CloseDialog)
                    }
                },
            ),
    )
    Box(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).lexiContentWidth(),
        contentAlignment = Alignment.Center,
    ) {
        DialogPanel(card, state, turkish, onIntent, flip)
    }
    if (closing) ReopenCatcher(card, flip, onIntent)
}

/**
 * One-time warm-up of the dialog (`video-flow-notes.md` § Phase 5): the real [DialogPanel] is composed and drawn once,
 * posed mid-turn of the open flip (the Y rotation with the flip's perspective, the grown frame with its hard shadow,
 * the rows, the segmented controls, OYNA), so the first card the player taps does not pay for the first composition of
 * these classes or the first compile of their GPU shaders. The caller places it **behind** the picker's opaque
 * background for a few frames while the picker is idle; it has no semantics, takes no input and is not a morph anchor.
 */
@Composable
internal fun PickerDialogWarmUp(card: PickerCategoryUi, state: CategoryPickerUiState, turkish: Boolean) {
    val motion = LexiTheme.motion
    val flip = remember {
        PickerDialogFlip(
            DialogFlipMachine(DialogFlipChoreography(motion.flow, motion.mechanical, motion.turn, motion.pop)),
        ).also {
            it.card = card
            it.cardRects[card.id] = Rect(Offset(WARM_UP_CARD_X, WARM_UP_CARD_Y), Size(WARM_UP_CARD_W, WARM_UP_CARD_H))
            it.request(open = true, tier = MotionTier.Full)
            it.advanceBy(WARM_UP_POSE_MILLIS)
        }
    }
    CompositionLocalProvider(LocalMorphAnchors provides null, LocalMorphOverlay provides null) {
        Box(
            Modifier.fillMaxSize().windowInsetsPadding(
                WindowInsets.safeDrawing,
            ).lexiContentWidth().clearAndSetSemantics {
            },
        ) {
            Box(Modifier.align(Alignment.Center)) { DialogPanel(card, state, turkish, {}, flip) }
        }
    }
}

// A card-sized rectangle near the top left and a pose a little past the middle of the open run: the panel is turned,
// partly grown and already visible, which draws every layer the real flip draws.
private const val WARM_UP_CARD_X = 24f
private const val WARM_UP_CARD_Y = 260f
private const val WARM_UP_CARD_W = 160f
private const val WARM_UP_CARD_H = 190f
private const val WARM_UP_POSE_MILLIS = 330L

/** Test tags of the dialog's parts that have no text of their own. */
internal object PlayDialogTags {
    const val SCRIM = "playDialogScrim"
}

/**
 * While the dialog closes (#15) it covers everything: a tap on the card it came from reopens it (the same timeline
 * plays forwards again, with the card's light haptic), any other tap is ignored until the grid takes input again.
 */
@Composable
private fun ReopenCatcher(card: PickerCategoryUi, flip: PickerDialogFlip, onIntent: (CategoryPickerIntent) -> Unit) {
    val haptics = LocalHaptics.current
    val coordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }
    Box(
        Modifier
            .fillMaxSize()
            .onPlaced { coordinates[0] = it }
            .pointerInput(card.id) {
                detectTapGestures { offset ->
                    val local = coordinates[0]?.takeIf { it.isAttached } ?: return@detectTapGestures
                    val rect = flip.cardRects[card.id] ?: return@detectTapGestures
                    if (rect.contains(local.localToRoot(offset))) {
                        haptics.perform(HapticKind.Light)
                        onIntent(CategoryPickerIntent.OpenCategory(card.id))
                    }
                }
            },
    )
}

/**
 * The panel's look while #14 / #15 runs. Outer layer: turn on Y around the current rectangle's centre, the Lite scale
 * and the dialog's alpha. Drawn: the hard shadow, fill and border at the current rectangle, the content clipped to it.
 * Inner layer: the content follows the rectangle's top-left and fades with `contentAlpha`. At rest the panel uses
 * `lexiSurface` instead, so the resting look is the static one.
 */
private fun Modifier.flipPanel(
    flip: PickerDialogFlip,
    flow: FlowMotion,
    fill: Color,
    borderWidth: Dp,
    borderColor: Color,
    shadow: LexiShadow,
): Modifier = this
    .graphicsLayer {
        val frame = flip.frame()
        val rect = flip.dialogRect(size, flow.cardLiftOffset.toPx())
        // No card rectangle (Lite, Minimal or unknown): the Lite look, a scale from the panel's centre.
        val fallbackShown = if (rect == null && flip.tier == MotionTier.Full) frame.dialogGrow else 1f
        val scale = frame.dialogScale *
            (flow.liteFlipStartScale + (1f - flow.liteFlipStartScale) * fallbackShown)
        val current = rect ?: Rect(Offset.Zero, size)
        if (size.width > 0f && size.height > 0f) {
            transformOrigin = TransformOrigin(current.center.x / size.width, current.center.y / size.height)
        }
        scaleX = scale
        scaleY = scale
        rotationY = if (rect != null) frame.dialogTurnDegrees else 0f
        perspective(flow.flipPerspective)
        alpha = frame.dialogAlpha * fallbackShown.coerceIn(0f, 1f)
    }
    .drawWithContent {
        val rect = flip.dialogRect(size, flow.cardLiftOffset.toPx()) ?: Rect(Offset.Zero, size)
        val shift = shadow.offset.toPx()
        val line = borderWidth.toPx()
        drawRect(shadow.color, rect.topLeft + Offset(shift, shift), rect.size)
        drawRect(fill, rect.topLeft, rect.size)
        clipRect(rect.left + line, rect.top + line, rect.right - line, rect.bottom - line) {
            this@drawWithContent.drawContent()
        }
        drawRect(borderColor, rect.topLeft, Size(rect.width, line))
        drawRect(borderColor, Offset(rect.left, rect.bottom - line), Size(rect.width, line))
        drawRect(borderColor, rect.topLeft, Size(line, rect.height))
        drawRect(borderColor, Offset(rect.right - line, rect.top), Size(line, rect.height))
    }
    .graphicsLayer {
        val rect = flip.dialogRect(size, flow.cardLiftOffset.toPx())
        translationX = rect?.left ?: 0f
        translationY = rect?.top ?: 0f
        alpha = flip.frame().contentAlpha
    }
    .padding(borderWidth)

/** Row [index] of the panel rises [FlowMotion.dialogItemOffset] and fades in (#14); nothing at rest. */
private fun Modifier.flipItem(flip: PickerDialogFlip, index: Int, animating: Boolean, offset: Dp): Modifier =
    if (!animating) {
        this
    } else {
        graphicsLayer {
            val progress = flip.itemProgress(index)
            alpha = progress.coerceIn(0f, 1f)
            translationY = (1f - progress) * offset.toPx()
        }
    }

/** Swallows pointer input before the children see it unless [enabled] (read on each event). */
private fun Modifier.inputWhen(enabled: () -> Boolean): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (!enabled()) event.changes.forEach { it.consume() }
        }
    }
}

@Composable
private fun DialogPanel(
    card: PickerCategoryUi,
    state: CategoryPickerUiState,
    turkish: Boolean,
    onIntent: (CategoryPickerIntent) -> Unit,
    flip: PickerDialogFlip,
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val typography = LexiTheme.typography
    val flow = LexiTheme.motion.flow
    val name = (if (turkish) card.nameTr else card.nameEn)
        .ifEmpty { if (card.isMixed) stringResource(Res.string.picker_mixed_fallback) else "" }
    val animating = flip.animating
    val rise = flow.dialogItemOffset
    val noTimer = stringResource(Res.string.dialog_time_none)
    // The choices and OYNA act only once the dialog rests (the back arrow always closes it).
    val settled: (CategoryPickerIntent) -> Unit = { if (flip.dialogTakesInput) onIntent(it) }
    Column(
        modifier = Modifier
            .padding(horizontal = dimens.screenPaddingHorizontal, vertical = dimens.gap16)
            .fillMaxWidth()
            .onGloballyPositioned { flip.panelRect = it.rectInRoot() }
            .then(
                if (animating) {
                    Modifier.flipPanel(
                        flip,
                        flow,
                        colors.cream,
                        LexiTheme.borders.regular,
                        colors.ink,
                        LexiTheme.shadows.s6,
                    )
                } else {
                    Modifier.lexiSurface(
                        colors.cream,
                        BorderStroke(LexiTheme.borders.regular, colors.ink),
                        shadow = LexiTheme.shadows.s6,
                    )
                },
            )
            // Swallows taps on the panel itself (they would otherwise reach the scrim and close it); no semantics.
            .pointerInput(Unit) { detectTapGestures { } }
            .semantics {
                paneTitle = name
                isTraversalGroup = true
            }
            .padding(dimens.gap16),
    ) {
        // The back arrow and OYNA stay pinned; the choices between them scroll when the panel does not fit (small
        // phones, large fonts).
        DialogHeader(
            name = name,
            meta = stringResource(Res.string.dialog_meta, state.level.name, card.total, card.learned),
            onClose = { onIntent(CategoryPickerIntent.CloseDialog) },
            modifier = Modifier.flipItem(flip, ROW_HEADER, animating, rise),
        )
        Column(
            Modifier
                .weight(1f, fill = false)
                .inputWhen { flip.dialogTakesInput }
                .verticalScroll(rememberScrollState()),
        ) {
            Column(Modifier.flipItem(flip, ROW_MODE, animating, rise)) {
                SectionLabel(Res.string.dialog_mode_label)
                val modeIndex = PickerModes.indexOf(state.mode).coerceAtLeast(0)
                WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
                    SegmentedControl(
                        segments = PickerModes.map { Segment(stringResource(it.titleRes())) },
                        selectedIndex = modeIndex,
                        onSelect = { settled(CategoryPickerIntent.SelectMode(PickerModes[it])) },
                        height = LexiTheme.dimens.minTouchTargetAccessible,
                        role = Role.RadioButton,
                        titleStyle = typography.bodyStrong16,
                        fitTitle = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            ModeHint(state.mode, Modifier.flipItem(flip, ROW_HINT, animating, rise).padding(top = dimens.gap8))
            Column(Modifier.flipItem(flip, ROW_TIME, animating, rise)) {
                SectionLabel(Res.string.dialog_time_label)
                WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
                    SegmentedControl(
                        segments = state.timeOptions.map { Segment(formatClock(it)) },
                        selectedIndex = state.timeOptions.indexOf(state.timeSeconds),
                        onSelect = { settled(CategoryPickerIntent.SelectTime(state.timeOptions[it])) },
                        height = LexiTheme.dimens.minTouchTargetAccessible,
                        role = Role.RadioButton,
                        enabled = state.timeEnabled,
                        titleStyle = typography.bodyStrong16,
                        fitTitle = true,
                        // Practice dims the row where it is; the description above is its only visible text, so the
                        // reason is just spoken (no second line, nothing below the row moves).
                        modifier = Modifier.fillMaxWidth().then(
                            if (state.timeEnabled) Modifier else Modifier.semantics { contentDescription = noTimer },
                        ),
                    )
                }
            }
        }
        WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
            LexiButton(
                onClick = { settled(CategoryPickerIntent.Play) },
                modifier = Modifier
                    .padding(top = dimens.gap18)
                    .fillMaxWidth()
                    .flipItem(flip, ROW_PLAY, animating, rise)
                    .inputWhen { flip.dialogTakesInput }
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
}

/** Back arrow, the card's name and "B2 · 20 kelime · 6 öğrenildi"; capped at 1.3× so a word never breaks. */
@Composable
private fun DialogHeader(name: String, meta: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavSquareButton(
            icon = LexiIcons.Back,
            contentDescription = stringResource(Res.string.dialog_close),
            onClick = onClose,
            size = IconButtonDefaults.NavSizeLarge,
        )
        WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
            Column {
                WordSafeText(name, typography.heading22, colors.ink)
                WordSafeText(meta, typography.caption12SemiBold, colors.ink)
            }
        }
    }
}

/**
 * The mode's one-sentence description. All three sentences are laid out in the same cell (the others transparent and
 * hidden from readers), so the cell is as tall as the longest of them in the current language and font scale and
 * switching modes moves nothing below it.
 */
@Composable
private fun ModeHint(selected: GameMode, modifier: Modifier = Modifier) {
    Box(modifier) {
        PickerModes.forEach { mode ->
            Text(
                stringResource(mode.hintRes()),
                style = LexiTheme.typography.body14,
                color = LexiTheme.colors.ink,
                modifier = Modifier.fillMaxWidth().then(
                    if (mode == selected) Modifier else Modifier.alpha(0f).clearAndSetSemantics { },
                ),
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

/** The panel's rows in their #14 entrance order (`DialogFlipMachine.itemProgress`). */
private const val ROW_HEADER = 0
private const val ROW_MODE = 1
private const val ROW_HINT = 2
private const val ROW_TIME = 3
private const val ROW_PLAY = 4

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
