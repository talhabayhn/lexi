package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
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
}

/**
 * Segmented control: 3dp ink frame on white, equal segments separated by [dividerWidth] ink lines (part of the
 * segment, as the CSS `border-left`); selected = ink with yellow title (cream subtitle). Light haptic + tap on select.
 */
@Composable
public fun SegmentedControl(
    segments: List<Segment>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = ControlDefaults.SegmentHeight,
    dividerWidth: Dp = LexiTheme.borders.thin,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    Row(
        modifier = modifier
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
            .selectableGroup(),
    ) {
        segments.forEachIndexed { index, segment ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .then(if (index > 0) Modifier.leftBorder(dividerWidth, colors.ink) else Modifier)
                    .background(if (selected) colors.ink else colors.white)
                    .selectable(selected = selected, role = Role.Tab, interactionSource = null, indication = null) {
                        haptics.perform(HapticKind.Light)
                        sounds.play(SoundEffect.Tap)
                        onSelect(index)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val titleColor = if (selected) colors.yellow else colors.ink
                if (segment.subtitle == null) {
                    Text(segment.title, style = typography.bodyStrong14, color = titleColor, maxLines = 1)
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
 * Give it a label through `Modifier.semantics { contentDescription = … }`, or use the toggle [ListRow], which makes the
 * whole row the switch (pass `onCheckedChange = null` for such display-only use). Compose extends the touch bounds
 * of small pointer targets to the platform minimum (48dp ≥ `minTouchTarget`), so the 30dp-high switch is tappable
 * on a 48dp band without growing.
 */
@Composable
public fun LexiSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
                    Modifier.toggleable(
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
) {
    val fraction = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(height + borderWidth * 2)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) }
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
