package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect

/** One option of a [SegmentedControl]; [subtitle] adds the small second line ("3:00") of the mode picker. */
@Immutable
public data class Segment(val title: String, val subtitle: String? = null)

/** Control sizes from the screens' HTML. */
public object ControlDefaults {
    /** 46: segmented control with one line (`08-notebook`, `16-components`). */
    public val SegmentHeight: Dp = 46.dp

    /** 56: segmented control with subtitles (`03-level-select` modes; dividers 3dp there). */
    public val SegmentHeightWithSubtitle: Dp = 56.dp

    /** Switch: 54 × 30, knob 18, knob inset 3. */
    public val SwitchWidth: Dp = 54.dp
    public val SwitchHeight: Dp = 30.dp
    internal val KnobSize: Dp = 18.dp
    internal val KnobInset: Dp = 3.dp

    /** Progress bar content heights (borders come on top, CSS content-box). */
    public val ProgressHeightThin: Dp = 8.dp
    public val ProgressHeightThick: Dp = 12.dp

    /** 6: the thin bars of the category cards (`07-categories`). */
    public val ProgressHeightCompact: Dp = 6.dp

    /** Line height of a segment with a subtitle (`line-height: 1.1`). */
    internal const val SEGMENT_TWO_LINE_HEIGHT: Double = 1.1

    /** System font scale above which a control that opted in ([SegmentedControl] `scrollable`) scrolls sideways. */
    public const val SCROLL_FONT_SCALE: Float = 1.75f

    /** System font scale from which compact labels (the stats chart's weekday names) shrink to one letter. */
    public const val SHORT_LABEL_FONT_SCALE: Float = 1.5f
}

/**
 * Segmented control: 3dp ink frame on white, equal segments separated by [dividerWidth] ink lines (part of the
 * segment, as the CSS `border-left`); selected = ink with yellow title (cream subtitle). Light haptic + tap on select.
 * With [scrollable], font scales above [ControlDefaults.SCROLL_FONT_SCALE] let the segments keep their natural width
 * and the row scroll sideways (the selected one is brought into view) instead of squeezing them against each other.
 */
@Composable
public fun SegmentedControl(
    segments: List<Segment>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = ControlDefaults.SegmentHeight,
    dividerWidth: Dp = LexiTheme.borders.thin,
    scrollable: Boolean = false,
) {
    val colors = LexiTheme.colors
    val scrolling = scrollable && LocalDensity.current.fontScale > ControlDefaults.SCROLL_FONT_SCALE
    val scrollState = rememberScrollState()
    val requesters = remember(segments.size) { List(segments.size) { BringIntoViewRequester() } }
    if (scrolling) {
        LaunchedEffect(selectedIndex) {
            // Wait for the first layout pass, otherwise the segment has no position to scroll to yet.
            withFrameNanos { }
            requesters.getOrNull(selectedIndex)?.bringIntoView()
        }
    }
    val typography = LexiTheme.typography
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    // At least [height]; a title that does not fit its segment (large font sizes) wraps and every segment grows with it.
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
            .selectableGroup()
            .then(if (scrolling) Modifier.horizontalScroll(scrollState) else Modifier),
    ) {
        segments.forEachIndexed { index, segment ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .then(if (scrolling) Modifier else Modifier.weight(1f))
                    .bringIntoViewRequester(requesters[index])
                    .fillMaxHeight()
                    .heightIn(min = height)
                    .then(if (index > 0) Modifier.leftBorder(dividerWidth, colors.ink) else Modifier)
                    .background(if (selected) colors.ink else colors.white)
                    .selectable(selected = selected, role = Role.Tab, interactionSource = null, indication = null) {
                        haptics.perform(HapticKind.Light)
                        sounds.play(SoundEffect.Tap)
                        onSelect(index)
                    }
                    .then(if (scrolling) Modifier.padding(horizontal = LexiTheme.dimens.gap12) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val titleColor = if (selected) colors.yellow else colors.ink
                if (segment.subtitle == null) {
                    Text(
                        segment.title,
                        style = typography.bodyStrong14,
                        color = titleColor,
                    )
                } else {
                    val lineHeight = ControlDefaults.SEGMENT_TWO_LINE_HEIGHT
                    Text(
                        segment.title,
                        style = typography.bodyStrong16.withCssLineHeight(lineHeight),
                        color = titleColor,
                    )
                    Text(
                        segment.subtitle,
                        style = typography.caption12.withCssLineHeight(lineHeight),
                        color = if (selected) colors.cream else colors.ink,
                    )
                }
            }
        }
    }
}

/** A CSS `border-left` inside the element: drawn at the left edge, content inset by its width. */
private fun Modifier.leftBorder(width: Dp, color: Color): Modifier = this
    .drawBehind { drawRect(color, size = Size(width.toPx(), size.height)) }
    .padding(start = width)

/**
 * Bauhaus switch, 54 × 30 with a 3dp border: on = blue track, yellow knob right; off = cream track, ink knob left.
 * Standalone use needs a [label] (read as the switch's name; the on/off state is announced by its switch role, or
 * by [stateDescription] when given). The toggle [ListRow] makes the whole row the switch instead (pass
 * `onCheckedChange = null` for such display-only use). Compose extends the touch and accessibility bounds of small
 * pointer targets to the platform minimum (48dp ≥ `minTouchTarget`), so the 30dp-high switch is tappable and
 * focusable on a 48dp band without growing.
 */
@Composable
public fun LexiSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    stateDescription: String? = null,
) {
    val colors = LexiTheme.colors
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    val trackInner = ControlDefaults.SwitchWidth - LexiTheme.borders.regular * 2
    val knobOffset = if (checked) {
        trackInner - ControlDefaults.KnobSize - ControlDefaults.KnobInset
    } else {
        ControlDefaults.KnobInset
    }
    Box(
        modifier = modifier
            .size(ControlDefaults.SwitchWidth, ControlDefaults.SwitchHeight)
            .then(
                if (onCheckedChange != null) {
                    Modifier
                        .semantics {
                            if (label != null) contentDescription = label
                            if (stateDescription != null) this.stateDescription = stateDescription
                        }
                        .toggleable(
                            value = checked,
                            enabled = enabled,
                            role = Role.Switch,
                            interactionSource = null,
                            indication = null,
                        ) {
                            haptics.perform(HapticKind.Light)
                            sounds.play(SoundEffect.Tap)
                            onCheckedChange(it)
                        }
                } else {
                    Modifier
                },
            )
            .lexiSurface(
                if (checked) colors.blue else colors.cream,
                BorderStroke(LexiTheme.borders.regular, colors.ink),
            ),
    ) {
        Box(
            Modifier
                .offset(x = knobOffset, y = ControlDefaults.KnobInset)
                .size(ControlDefaults.KnobSize)
                .lexiSurface(
                    color = if (checked) colors.yellow else colors.ink,
                    border = BorderStroke(LexiTheme.borders.thin, colors.ink),
                    shape = LexiTheme.shapes.circle,
                ),
        )
    }
}

/**
 * Progress bar with a CSS content-box: [height] is the track, the [borderWidth] frame comes on top (8 + 2 + 2 on the
 * level cards, 12 + 2 + 2 in stats). [fillEdge] adds the timer-style ink right edge to the fill (`16-components`).
 * [contentDescription] names the bar and [stateDescription] replaces the spoken value (e.g. "48 percent, 12 of 25
 * words"); without it the screen reader derives the percentage from the range info.
 */
@Composable
public fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = ControlDefaults.ProgressHeightThin,
    borderWidth: Dp = LexiTheme.borders.thin,
    borderColor: Color = LexiTheme.colors.ink,
    trackColor: Color = LexiTheme.colors.white,
    fillColor: Color = LexiTheme.colors.blue,
    fillEdge: Boolean = false,
    contentDescription: String? = null,
    stateDescription: String? = null,
) {
    val fraction = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(height + borderWidth * 2)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                if (contentDescription != null) this.contentDescription = contentDescription
                if (stateDescription != null) this.stateDescription = stateDescription
            }
            .lexiSurface(trackColor, BorderStroke(borderWidth, borderColor)),
    ) {
        if (fraction > 0f) {
            Row(Modifier.fillMaxWidth(fraction).fillMaxHeight()) {
                Box(Modifier.weight(1f).fillMaxHeight().background(fillColor))
                if (fillEdge) Box(Modifier.width(borderWidth).fillMaxHeight().background(borderColor))
            }
        }
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun ControlsPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16),
        ) {
            SegmentedControl(
                segments = listOf(Segment("Tekrar · 24"), Segment("Tümü · 412"), Segment("Zor · 31")),
                selectedIndex = 0,
                onSelect = {},
            )
            SegmentedControl(
                segments = listOf(
                    Segment("Klasik", "3:00"),
                    Segment("Pratik", "süresiz"),
                    Segment("Hardcore", "tek hata"),
                ),
                selectedIndex = 0,
                onSelect = {},
                height = ControlDefaults.SegmentHeightWithSubtitle,
                dividerWidth = LexiTheme.borders.regular,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap24)) {
                LexiSwitch(checked = true, onCheckedChange = {})
                LexiSwitch(checked = false, onCheckedChange = {})
            }
            ProgressBar(
                progress = 0.48f,
                fillColor = LexiTheme.colors.ink,
                height = ControlDefaults.ProgressHeightThick,
            )
        }
    }
}
