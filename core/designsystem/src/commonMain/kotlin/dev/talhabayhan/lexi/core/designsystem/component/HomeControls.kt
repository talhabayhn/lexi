package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.WithCappedFontScale
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect

/**
 * The selected level on Home (`02c-home-v2`, needs design): red, 3dp frame, s5 shadow, at least [minHeight]. Left the
 * level [code] (34/800) and its [name] (13/600; long names wrap); right, always beside it, the [count] ("412 / 850
 * kelime") over a 12dp bar (blue fill on white, 2dp frame). Not clickable: one node read as [description].
 */
@Composable
public fun LevelHeroCard(
    code: String,
    name: String,
    count: String,
    progress: Float,
    description: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = HomeTierSize.Regular.heroMinHeight,
    paddingVertical: Dp = HomeTierSize.Regular.heroPaddingVertical,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    // The count column keeps its share of the width as the count text grows, up to the controls' cap.
    val countScale = LocalDensity.current.fontScale.coerceIn(1f, HomeDefaults.CONTROL_FONT_SCALE_CAP)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clearAndSetSemantics { contentDescription = description }
            .lexiSurface(colors.red, BorderStroke(LexiTheme.borders.regular, colors.ink), LexiTheme.shadows.s5)
            .padding(horizontal = HomeDefaults.HeroPaddingHorizontal, vertical = paddingVertical),
        horizontalArrangement = Arrangement.spacedBy(HomeDefaults.HeroGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2)) {
            Text(code, style = typography.levelTitle, color = colors.white)
            Text(name, style = typography.caption13SemiBold, color = colors.white)
        }
        Column(
            modifier = Modifier.widthIn(max = HomeDefaults.HeroCountWidth * countScale).fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(HomeDefaults.HeroCountGap),
        ) {
            WithCappedFontScale(HomeDefaults.CONTROL_FONT_SCALE_CAP) {
                Text(
                    count,
                    style = typography.caption13SemiBold,
                    color = colors.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * Six equal level chips (`02c-home-v2`, needs design): [chipHeight] (≥ 48) high, 3dp frame, gap 6; the selected one ink
 * with yellow text. A radio group: each chip is read as its [descriptions] entry ("A1, Başlangıç") with its selected
 * state. Light haptic + tap on select.
 */
@Composable
public fun LevelChipRow(
    labels: List<String>,
    descriptions: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    chipHeight: Dp = HomeTierSize.Regular.chipHeight,
) {
    val colors = LexiTheme.colors
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(HomeDefaults.ChipGap),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = chipHeight)
                    .semantics { contentDescription = descriptions[index] }
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        haptics.perform(HapticKind.Light)
                        sounds.play(SoundEffect.Tap)
                        onSelect(index)
                    }
                    .lexiSurface(
                        if (selected) colors.ink else colors.white,
                        BorderStroke(LexiTheme.borders.regular, colors.ink),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val style = LexiTheme.typography.bodyStrong16
                BasicText(
                    text = label,
                    style = style.merge(TextStyle(textAlign = TextAlign.Center)),
                    color = { if (selected) colors.yellow else colors.ink },
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = style.fontSize * HomeDefaults.CHIP_MIN_SCALE,
                        maxFontSize = style.fontSize,
                        stepSize = HomeDefaults.CAPTION_STEP,
                    ),
                    modifier = Modifier.padding(horizontal = LexiTheme.dimens.gap2).clearAndSetSemantics {},
                )
            }
        }
    }
}

/**
 * "Kategori  Karışık ▾" (`02c-home-v2`, needs design): white, 3dp frame, [height] (48) high; muted [label], the
 * [value] (cut with an ellipsis) and a chevron down. A button that opens a picker sheet, read as [description].
 */
@Composable
public fun PickerRow(
    label: String,
    value: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = HomeTierSize.Regular.pickerHeight,
) {
    val colors = LexiTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .semantics { contentDescription = description },
        shadow = null,
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = HomeDefaults.PickerPadding),
            horizontalArrangement = Arrangement.spacedBy(HomeDefaults.PickerGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = LexiTheme.typography.body15SemiBold,
                color = colors.inkMuted,
                maxLines = 1,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                value,
                style = LexiTheme.typography.bodyStrong15,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).clearAndSetSemantics {},
            )
            Icon(LexiIcons.ChevronDown, contentDescription = null, modifier = Modifier.size(HomeDefaults.PickerChevron))
        }
    }
}

/**
 * A footer key tile (`02c-home-v2`, needs design): a [keySize] square in the game keyboard's key style (white, 3dp
 * frame, s3 shadow, press per motion.md #1) with a 24dp [icon], the [label] (12/700) under it and an optional count
 * [badge] ("24", "99+") over the key's top-right corner. The whole column is one button read as [description] (the
 * badge and icon are not read).
 */
@Composable
public fun KeyTile(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    keySize: Dp = HomeTierSize.Regular.keySize,
    labelGap: Dp = HomeTierSize.Regular.keyLabelGap,
    badge: String? = null,
) {
    val source = remember { MutableInteractionSource() }
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    Column(
        modifier = modifier
            .clickable(interactionSource = source, indication = null, role = Role.Button) {
                haptics.perform(HapticKind.Light)
                sounds.play(SoundEffect.Tap)
                onClick()
            }
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(labelGap),
    ) {
        Box {
            // The key moves like a letter key (motion.md #1) while the whole column is pressed; the column handles the click.
            val motion = LexiTheme.motion
            val pressProgress = rememberPressProgress(source, enabled = true, motion = motion)
            val forcedPressed = LocalForcePressed.current
            val colors = LexiTheme.colors
            PressableSurfaceLayout(
                pressProgress = { if (forcedPressed) 1f else pressProgress.value },
                translateOnPress = !motion.reducedMotion,
                shape = LexiTheme.shapes.square,
                color = colors.white,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = LexiTheme.shadows.s3,
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(keySize),
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(HomeDefaults.KeyIcon))
            }
            if (badge != null) {
                CountBadge(
                    badge,
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = HomeDefaults.CountBadgeOffset, y = -HomeDefaults.CountBadgeOffset),
                )
            }
        }
        Text(label, style = LexiTheme.typography.caption12Bold, maxLines = 1, textAlign = TextAlign.Center)
    }
}

/** Blue count badge (22dp, 2dp ink frame, 11/800 white): the due reviews on the notebook key tile. Decorative. */
@Composable
public fun CountBadge(text: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    Box(
        modifier = modifier
            .heightIn(min = HomeDefaults.CountBadgeSize)
            .widthIn(min = HomeDefaults.CountBadgeSize)
            .clearAndSetSemantics {}
            .lexiSurface(colors.blue, BorderStroke(LexiTheme.borders.thin, colors.ink), shape = LexiTheme.shapes.circle)
            .padding(horizontal = HomeDefaults.CountBadgePadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = LexiTheme.typography.count11, color = colors.white, maxLines = 1)
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun HomeControlsPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
        ) {
            LevelHeroCard("A1", "Başlangıç", "412 / 850 kelime", 412f / 850, "A1, Başlangıç, 412 / 850 kelime")
            val levels = listOf("A1", "A2", "B1", "B2", "C1", "NAT")
            LevelChipRow(levels, levels, selectedIndex = 0, onSelect = {})
            PickerRow("Kategori", "Karışık", "Kategori: Karışık", onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(HomeDefaults.FooterGap)) {
                KeyTile(LexiIcons.Notebook, "Defter", "Defter", onClick = {}, Modifier.weight(1f), badge = "24")
                KeyTile(LexiIcons.Stats, "İstatistik", "İstatistik", onClick = {}, Modifier.weight(1f))
                KeyTile(LexiIcons.Exams, "Sınavlar", "Sınavlar", onClick = {}, Modifier.weight(1f))
                KeyTile(LexiIcons.Duel, "Düello", "Düello", onClick = {}, Modifier.weight(1f))
            }
        }
    }
}
