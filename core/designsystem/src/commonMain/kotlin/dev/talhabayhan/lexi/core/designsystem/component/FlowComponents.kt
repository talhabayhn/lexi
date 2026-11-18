package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.text.WordSafeText
import dev.talhabayhan.lexi.core.designsystem.text.rememberTextLayoutReport
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect

/**
 * Parts of the video flow's screens (`docs/design/video-flow-notes.md` §1–§3, drafts `home_v5.png` / `cats_v3.png`,
 * sizes from `drafts/video-flow/flow.html`): Home's play card, daily wheel card and ruled bottom bar, the level chips,
 * the illustrated category card and the "Başa dön" pill. Single-line control text shrinks to fit (never breaks a word)
 * instead of growing past its box at large font sizes.
 */
public object FlowDefaults {
    /** Control text (badges, chips, mode boxes, bar labels) follows the system font size up to this scale. */
    public const val CONTROL_FONT_SCALE_CAP: Float = 1.3f

    /** Home's header (logo blocks 38 + frame): the daily-gift toast floats below it. */
    public val HomeHeaderHeight: Dp = 40.dp

    /** Play card: padding 20, label → mode boxes 14, mode boxes → OYNA row 16, boxes 8 apart, play circle 68. */
    public val PlayCardPadding: Dp = 20.dp
    internal val PlayCardLabelGap: Dp = 14.dp
    internal val PlayCardRowGap: Dp = 16.dp
    internal val ModeBoxGap: Dp = 8.dp
    internal val ModeBoxPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
    public val PlayCircleSize: Dp = 68.dp
    internal val PlayIconSize: Dp = 26.dp

    /** Daily card: text column → badge 12; the text keeps at least 140dp, else the badge drops its word. */
    internal val DailyPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    internal val DailyGap: Dp = 12.dp
    internal val DailyTextMinWidth: Dp = 140.dp
    internal val BadgeMinHeight: Dp = 32.dp
    internal val BadgePadding: Dp = 8.dp
    internal val BadgeGap: Dp = 6.dp
    internal val BadgeIcon: Dp = 14.dp
    internal val BadgeCheck: Dp = 10.dp
    internal val BadgeChevron: Dp = 14.dp

    /** Study tile (`home_v5.png`, `flow.html` .mini): width 171 (fills grid column), height 84, padding 0 12, gap 10. */
    public val StudyTileMinHeight: Dp = 84.dp
    internal val StudyTilePaddingHorizontal: Dp = 12.dp
    internal val StudyTileGap: Dp = 10.dp

    /** Bottom bar: 4dp ink rule on top, items at least 56 high, icon 24, icon → label 4. */
    public val BottomBarMinHeight: Dp = 64.dp
    internal val BottomBarItemMinHeight: Dp = 56.dp
    internal val BottomBarIcon: Dp = 24.dp
    internal val BottomBarItemGap: Dp = 4.dp
    internal val BottomBarPaddingVertical: Dp = 8.dp

    /** Level chips: six equal, 40 high (48 touch band), gap 6. */
    public val LevelChipHeight: Dp = 40.dp
    public val LevelChipGap: Dp = 6.dp

    /** Category card: art field 95 + 3dp rule, info padding 8 / 10, name → progress row 6, row gap 8. */
    public val CategoryArtHeight: Dp = 95.dp
    internal val CategoryInfoPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    internal val CategoryInfoGap: Dp = 6.dp
    internal val CategoryMetaGap: Dp = 8.dp
    internal val LockBadgeSize: Dp = 28.dp
    internal val LockIconSize: Dp = 16.dp
    internal val LockBadgeInset: Dp = 8.dp
    internal val SoonTagPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 2.dp)

    /** Grid: 2 columns, 12 apart, rows 14 apart. */
    public val GridColumnGap: Dp = 12.dp
    public val GridRowGap: Dp = 14.dp

    /** Room under the last grid row: the "Başa dön" pill (48 + 20 margin) floats over it. */
    public val GridBottomRoom: Dp = 76.dp

    /** "Başa dön" pill: at least 48 high, padding 0 14, icon 16. */
    internal val PillMinHeight: Dp = 48.dp
    internal val PillPaddingHorizontal: Dp = 14.dp
    internal val PillIcon: Dp = 16.dp

    /** Smallest size single-line control text shrinks to. */
    internal val MinControlTextSize = 9.sp
}

/** System font size, capped at [cap], for the [content] (control text that must stay inside fixed boxes). */
@Composable
public fun WithCappedFontScale(cap: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    if (density.fontScale <= cap) {
        content()
    } else {
        CompositionLocalProvider(LocalDensity provides Density(density.density, cap), content = content)
    }
}

/**
 * One line of control text: shrinks (down to [FlowDefaults.MinControlTextSize]) instead of wrapping or overflowing.
 * Reports its layout to `LocalTextLayoutProbe` (layout tests).
 */
@Composable
public fun FitLine(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color),
        onTextLayout = rememberTextLayoutReport(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = FlowDefaults.MinControlTextSize,
            maxFontSize = style.fontSize,
            stepSize = 0.5.sp,
        ),
    )
}

/** One informational mode box of the play card: "Klasik" over "3 dk". */
@Immutable
public data class PlayCardMode(val title: String, val caption: String)

/**
 * Home's play card (`home_v5.png`): red, 3dp ink frame, s6. Top [label] ("SEVİYENİ, KATEGORİNİ VE MODUNU SEÇ"), the
 * three informational [modes] (not tappable), then [playLabel] ("OYNA") left and a round play icon right. The whole
 * card is one button, read as [contentDescription].
 */
@Composable
public fun PlayCard(
    label: String,
    modes: List<PlayCardMode>,
    playLabel: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().semantics { this.contentDescription = contentDescription },
        color = LexiTheme.colors.red,
        shadow = LexiTheme.shadows.s6,
        contentAlignment = Alignment.TopStart,
    ) {
        // All of the card's text is control text (also spoken through [contentDescription]): capped, so OYNA stays on
        // the first screen of a 320 × 568 phone at 200 % font size.
        WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) { PlayCardContent(label, modes, playLabel) }
    }
}

@Composable
private fun PlayCardContent(label: String, modes: List<PlayCardMode>, playLabel: String) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Column(Modifier.fillMaxWidth().clearAndSetSemantics {}.padding(FlowDefaults.PlayCardPadding)) {
        WordSafeText(label, typography.label12, colors.white)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = FlowDefaults.PlayCardLabelGap)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(FlowDefaults.ModeBoxGap),
        ) {
            modes.forEach { mode ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
                        .padding(FlowDefaults.ModeBoxPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    FitLine(mode.title, typography.bodyStrong16, colors.ink)
                    // A caption too wide for its box ("one mistake" on a 320dp phone) wraps between its words.
                    WordSafeText(
                        mode.caption,
                        typography.caption12SemiBold,
                        colors.ink,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = FlowDefaults.PlayCardRowGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FitLine(playLabel, typography.display46, colors.white, Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(FlowDefaults.PlayCircleSize)
                    .lexiSurface(
                        color = colors.white,
                        border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                        shape = LexiTheme.shapes.circle,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    LexiIcons.Play,
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(FlowDefaults.PlayIconSize),
                )
            }
        }
    }
}

/** The daily wheel's state as the card's status badge shows it (from the Home v2 daily card). */
public enum class DailyCardStatus {
    /** Not played today: yellow card, a badge with an empty ring, "Çöz" and a chevron; the card is a button. */
    Solve,

    /** Played today: lighter yellow card, a badge with a check and "Tamamlandı"; not clickable. */
    Completed,

    /** No wheel today: muted card without a badge; not clickable. */
    Unavailable,
}

/**
 * "Günün çarkı" card (Home v2's daily card, `home-v2-notes.md` §1, adapted on this branch): left the [title] over the
 * [description]; right one status badge for [status] with [badgeLabel] ("Çöz" / "Tamamlandı"). When the text column
 * would get narrower than 140dp the badge keeps only its icon. Yellow and a button while [DailyCardStatus.Solve];
 * lighter yellow when completed; muted without badge when unavailable. One node, read as [contentDescription].
 */
@Composable
public fun DailyWheelCard(
    title: String,
    description: String,
    status: DailyCardStatus,
    badgeLabel: String?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val unavailable = status == DailyCardStatus.Unavailable
    val textColor = if (unavailable) colors.inkMuted else colors.ink
    val color = when (status) {
        DailyCardStatus.Solve -> colors.yellow
        DailyCardStatus.Completed -> colors.yellowMuted
        DailyCardStatus.Unavailable -> colors.disabledFill
    }
    val described = modifier.fillMaxWidth().semantics { this.contentDescription = contentDescription }
    val row: @Composable () -> Unit = {
        Layout(
            contents = listOf(
                {
                    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2)) {
                        WordSafeText(title, typography.heading18.withCssLineHeight(TITLE_LINE_HEIGHT), textColor)
                        WordSafeText(
                            description,
                            typography.caption12SemiBold.withCssLineHeight(TEXT_LINE_HEIGHT),
                            textColor,
                        )
                    }
                },
                { if (!unavailable) DailyBadge(status, badgeLabel) },
                { if (!unavailable) DailyBadge(status, label = null) },
            ),
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}.padding(FlowDefaults.DailyPadding),
            measurePolicy = DailyRowPolicy,
        )
    }
    if (status == DailyCardStatus.Solve) {
        PressableSurface(onClick = onClick, modifier = described, color = color, shadow = LexiTheme.shadows.s6) {
            row()
        }
    } else {
        // Completed / unavailable: information only, read as text (no button, no "disabled").
        Box(
            described.lexiSurface(
                color = color,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = if (unavailable) null else LexiTheme.shadows.s6,
            ),
        ) { row() }
    }
}

/** The daily card's row: text left, badge right; the badge keeps its word only while the text keeps 140dp. */
private object DailyRowPolicy : MultiContentMeasurePolicy {
    override fun MeasureScope.measure(measurables: List<List<Measurable>>, constraints: Constraints): MeasureResult {
        val (text, full, iconOnly) = measurables
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val gap = FlowDefaults.DailyGap.roundToPx()
        val loose = Constraints()
        val fullBadge = full.firstOrNull()?.measure(loose)
        val keepWord = fullBadge != null && width - fullBadge.width - gap >= FlowDefaults.DailyTextMinWidth.roundToPx()
        val badge = if (keepWord) fullBadge else iconOnly.firstOrNull()?.measure(loose)
        val badgeRoom = badge?.let { it.width + gap } ?: 0
        val column = text.first().measure(Constraints(maxWidth = (width - badgeRoom).coerceAtLeast(0)))
        val height = maxOf(column.height, badge?.height ?: 0)
        return layout(width, height) {
            column.place(0, (height - column.height) / 2)
            badge?.place(width - badge.width, (height - badge.height) / 2)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<List<IntrinsicMeasurable>>,
        width: Int,
    ): Int = intrinsicHeight(measurables, width)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<List<IntrinsicMeasurable>>,
        width: Int,
    ): Int = intrinsicHeight(measurables, width)

    private fun IntrinsicMeasureScope.intrinsicHeight(measurables: List<List<IntrinsicMeasurable>>, width: Int): Int {
        val (text, full, iconOnly) = measurables
        val gap = FlowDefaults.DailyGap.roundToPx()
        val fullWidth = full.firstOrNull()?.maxIntrinsicWidth(Constraints.Infinity)
        val keepWord = fullWidth != null && width - fullWidth - gap >= FlowDefaults.DailyTextMinWidth.roundToPx()
        val badge = if (keepWord) full.firstOrNull() else iconOnly.firstOrNull()
        val badgeWidth = badge?.maxIntrinsicWidth(Constraints.Infinity)
        val textWidth = (width - (badgeWidth?.let { it + gap } ?: 0)).coerceAtLeast(0)
        val textHeight = text.first().maxIntrinsicHeight(textWidth)
        return maxOf(textHeight, badge?.maxIntrinsicHeight(badgeWidth ?: 0) ?: 0)
    }
}

/** White box, 3dp frame: an empty ring + label + chevron (to solve) or a filled check + label (solved). */
@Composable
private fun DailyBadge(status: DailyCardStatus, label: String?) {
    val colors = LexiTheme.colors
    WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
        Row(
            modifier = Modifier
                .heightIn(min = FlowDefaults.BadgeMinHeight)
                .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
                .padding(horizontal = FlowDefaults.BadgePadding),
            horizontalArrangement = Arrangement.spacedBy(FlowDefaults.BadgeGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val solved = status == DailyCardStatus.Completed
            Box(
                modifier = Modifier
                    .size(FlowDefaults.BadgeIcon)
                    .lexiSurface(
                        color = if (solved) colors.ink else Color.Transparent,
                        border = BorderStroke(LexiTheme.borders.thin, colors.ink),
                        shape = LexiTheme.shapes.circle,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (solved) {
                    Icon(
                        LexiIcons.Check,
                        contentDescription = null,
                        tint = colors.white,
                        modifier = Modifier.size(FlowDefaults.BadgeCheck),
                    )
                }
            }
            if (label != null) {
                Text(
                    label,
                    style = LexiTheme.typography.caption12ExtraBold,
                    maxLines = 1,
                    onTextLayout = rememberTextLayoutReport(),
                )
            }
            if (!solved) {
                Icon(
                    LexiIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(FlowDefaults.BadgeChevron),
                )
            }
        }
    }
}

private const val TITLE_LINE_HEIGHT = 1.1
private const val TEXT_LINE_HEIGHT = 1.2

/**
 * Study tile on Home ("Defter", "Sınavlar", `home_v5.png`, `flow.html` `.mini`):
 * marker shape (circle / diamond) on the LEFT, title (17/800) and subtitle on the RIGHT.
 * Height 84dp, padding horizontal 12dp, gap 10dp between marker and text column.
 */
@Composable
public fun StudyTile(
    title: String,
    subtitle: String,
    marker: DecorShapeKind,
    markerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
) {
    val colors = LexiTheme.colors
    val content = if (inverted) colors.cream else colors.ink
    PressableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = FlowDefaults.StudyTileMinHeight),
        color = if (inverted) colors.ink else colors.white,
        shadow = if (inverted) LexiTheme.shadows.s4Red else LexiTheme.shadows.s4,
        propagateMinConstraints = true,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FlowDefaults.StudyTilePaddingHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FlowDefaults.StudyTileGap),
        ) {
            DecorShape(
                kind = marker,
                color = markerColor,
                border = BorderStroke(LexiTheme.borders.thin, if (inverted) colors.cream else colors.ink),
                modifier = Modifier.size(CardDefaults.TileMarkerSize),
            )
            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2),
            ) {
                Text(
                    text = title,
                    style = LexiTheme.typography.bodyStrong17.withCssLineHeight(1.05),
                    color = content,
                )
                Text(
                    text = subtitle,
                    style = LexiTheme.typography.caption12SemiBold,
                    color = content,
                )
            }
        }
    }
}

/**
 * The narrowest [StudyTile] whose text column still holds the longest word of every one of [titles] on one line.
 * If the column width is narrower, the tiles stack into a Column.
 */
@Composable
public fun studyTileMinWidth(titles: List<String>): Dp {
    val measurer = rememberTextMeasurer()
    val titleStyle = LexiTheme.typography.bodyStrong17
    val subtitleStyle = LexiTheme.typography.caption12SemiBold
    val padding = FlowDefaults.StudyTilePaddingHorizontal
    val gap = FlowDefaults.StudyTileGap
    val markerSize = CardDefaults.TileMarkerSize
    val border = LexiTheme.borders.regular
    val density = LocalDensity.current
    return remember(titles, titleStyle, subtitleStyle, padding, gap, markerSize, border, density, measurer) {
        val widest = titles.flatMap { it.split(' ') }.maxOfOrNull { word ->
            maxOf(
                measurer.measure(word, titleStyle, softWrap = false, maxLines = 1).size.width,
                measurer.measure(word, subtitleStyle, softWrap = false, maxLines = 1).size.width,
            )
        } ?: 0
        with(density) { widest.toDp() } + padding * 2 + border * 2 + markerSize + gap
    }
}

/** One item of [RuledBottomBar]: an [icon] over its [label]; [contentDescription] replaces the label when spoken. */
@Immutable
public data class BottomBarItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val contentDescription: String? = null,
)

/**
 * Home's "thin ruled" bottom bar (`home_v5.png`): a 4dp ink rule on top, then equal items (icon + label), no boxes,
 * no selected tab. Each item is a button at least 56dp high. The caller adds the navigation-bar inset below it.
 */
@Composable
public fun RuledBottomBar(items: List<BottomBarItem>, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val rule = LexiTheme.borders.thick
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = FlowDefaults.BottomBarMinHeight)
            .background(colors.cream)
            .drawBehind { drawRect(colors.ink, Offset.Zero, Size(size.width, rule.toPx())) }
            .padding(
                top = rule + FlowDefaults.BottomBarPaddingVertical,
                bottom = FlowDefaults.BottomBarPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val haptics = LocalHaptics.current
        val sounds = LocalSoundPlayer.current
        items.forEach { item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = FlowDefaults.BottomBarItemMinHeight)
                    .clickable(role = Role.Button) {
                        haptics.perform(HapticKind.Light)
                        sounds.play(SoundEffect.Tap)
                        item.onClick()
                    }
                    .then(
                        if (item.contentDescription != null) {
                            Modifier.clearAndSetSemantics {
                                contentDescription = item.contentDescription
                                role = Role.Button
                            }
                        } else {
                            Modifier
                        },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FlowDefaults.BottomBarItemGap, Alignment.CenterVertically),
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(FlowDefaults.BottomBarIcon),
                )
                WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
                    FitLine(item.label, LexiTheme.typography.caption12ExtraBold, colors.ink)
                }
            }
        }
    }
}

/**
 * The six level chips of "Çarkını seç" (`cats_v3.png`): equal width, 3dp ink frame, white; the selected one ink with
 * yellow text. A radio group: each chip is a radio button read as [labels] (or [contentDescriptions] when given).
 */
@Composable
public fun LevelChipRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentDescriptions: List<String>? = null,
) {
    val colors = LexiTheme.colors
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
        Row(
            modifier = modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(FlowDefaults.LevelChipGap),
        ) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                // A 48dp touch band around the 40dp chip.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = LexiTheme.dimens.minTouchTargetAccessible)
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            interactionSource = null,
                            indication = null,
                        ) {
                            if (!selected) {
                                haptics.perform(HapticKind.Light)
                                sounds.play(SoundEffect.Tap)
                            }
                            onSelect(index)
                        }
                        .then(
                            contentDescriptions?.getOrNull(index)?.let { description ->
                                Modifier.semantics { contentDescription = description }
                            } ?: Modifier,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = FlowDefaults.LevelChipHeight)
                            .lexiSurface(
                                if (selected) colors.ink else colors.white,
                                BorderStroke(LexiTheme.borders.regular, colors.ink),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        FitLine(
                            label,
                            LexiTheme.typography.bodyStrong15.copy(textAlign = TextAlign.Center),
                            if (selected) colors.yellow else colors.ink,
                            Modifier.padding(horizontal = LexiTheme.dimens.gap2),
                        )
                    }
                }
            }
        }
    }
}

/**
 * An illustrated card of "Çarkını seç" (`cats_v3.png`, `art_v3.png`): the [art] field on top (95dp, 3dp rule), the
 * [name] and a row of [progressText] ("412 / 850") + a progress bar of [progress]. With [onClick] it is a radio button
 * of its group ([selected] = the last chosen category) or, with [role] Button, an action; with [locked] it is dimmed information only: a lock badge on
 * the art and [lockedLabel] ("YAKINDA") instead of the progress. Read as [contentDescription].
 */
@Composable
public fun PickerCard(
    name: String,
    contentDescription: String,
    art: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    progressText: String? = null,
    progress: Float = 0f,
    selected: Boolean = false,
    locked: Boolean = false,
    lockedLabel: String? = null,
    /** [Role.RadioButton] for a category of the picker's group; [Role.Button] for an action (an exam pack). */
    role: Role = Role.RadioButton,
    onClick: (() -> Unit)? = null,
) {
    val colors = LexiTheme.colors
    val content: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            Box(Modifier.fillMaxWidth().height(FlowDefaults.CategoryArtHeight)) {
                art(Modifier.fillMaxWidth().height(FlowDefaults.CategoryArtHeight))
                if (locked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(FlowDefaults.LockBadgeInset)
                            .size(FlowDefaults.LockBadgeSize)
                            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.thin, colors.ink)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            LexiIcons.Lock,
                            contentDescription = null,
                            tint = colors.ink,
                            modifier = Modifier.size(FlowDefaults.LockIconSize),
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(LexiTheme.borders.regular).background(colors.ink))
            Column(
                modifier = Modifier.padding(FlowDefaults.CategoryInfoPadding),
                verticalArrangement = Arrangement.spacedBy(FlowDefaults.CategoryInfoGap),
            ) {
                // Two lines at most; a long word shrinks instead of breaking ("İngilizcesi" at 2× font size).
                WordSafeText(
                    name,
                    LexiTheme.typography.bodyStrong14.withCssLineHeight(TITLE_LINE_HEIGHT),
                    colors.ink,
                    maxLines = 2,
                )
                if (locked && lockedLabel != null) {
                    WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
                        Text(
                            lockedLabel,
                            style = LexiTheme.typography.label12,
                            color = colors.ink,
                            onTextLayout = rememberTextLayoutReport(),
                            modifier = Modifier
                                .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.thin, colors.ink))
                                .padding(FlowDefaults.SoonTagPadding),
                        )
                    }
                } else if (progressText != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FlowDefaults.CategoryMetaGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
                            FitLine(
                                progressText,
                                LexiTheme.typography.caption12ExtraBold,
                                colors.ink,
                                Modifier.widthIn(max = PROGRESS_TEXT_MAX_WIDTH),
                            )
                        }
                        ProgressBar(
                            progress = progress,
                            modifier = Modifier.weight(1f),
                            height = ControlDefaults.ProgressHeightCompact,
                        )
                    }
                }
            }
        }
    }
    if (onClick != null && !locked) {
        PressableSurface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().semantics {
                this.contentDescription = contentDescription
                if (role == Role.RadioButton) this.selected = selected
            },
            shadow = LexiTheme.shadows.s4,
            role = role,
            contentAlignment = Alignment.TopStart,
        ) { content() }
    } else {
        Box(
            modifier
                .fillMaxWidth()
                .semantics { this.contentDescription = contentDescription }
                .lexiSurface(
                    color = colors.white,
                    border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                    shadow = LexiTheme.shadows.s4,
                ),
        ) { content() }
    }
}

/** The progress text keeps at most this width, so the bar always has room. */
private val PROGRESS_TEXT_MAX_WIDTH = 96.dp

/** "Başa dön ↑" (`cats_v3.png`): an ink pill with cream text and an up arrow; scrolls the grid to the top. */
@Composable
public fun ScrollToTopPill(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    PressableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = FlowDefaults.PillMinHeight),
        color = colors.ink,
        shadow = null,
    ) {
        WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
            Row(
                modifier = Modifier.padding(horizontal = FlowDefaults.PillPaddingHorizontal),
                horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    style = LexiTheme.typography.bodyStrong15,
                    color = colors.cream,
                    maxLines = 1,
                    onTextLayout = rememberTextLayoutReport(),
                )
                Icon(
                    LexiIcons.ArrowUp,
                    contentDescription = null,
                    tint = colors.cream,
                    modifier = Modifier.size(FlowDefaults.PillIcon),
                )
            }
        }
    }
}

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun FlowComponentsPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            Modifier.background(LexiTheme.colors.cream).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlayCard(
                label = "SEVİYENİ, KATEGORİNİ VE MODUNU SEÇ",
                modes = listOf(
                    PlayCardMode("Klasik", "3 dk"),
                    PlayCardMode("Pratik", "süresiz"),
                    PlayCardMode("Hardcore", "tek hata"),
                ),
                playLabel = "OYNA",
                contentDescription = "Oyna",
                onClick = {},
            )
            DailyWheelCard("Günün çarkı", "Herkes aynı çarkı çözüyor", DailyCardStatus.Solve, "Çöz", "", {})
            LevelChipRow(listOf("A1", "A2", "B1", "B2", "C1", "NAT"), selectedIndex = 3, onSelect = {})
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PickerCard(
                    name = "Günlük hayat",
                    contentDescription = "",
                    art = { CategoryArt(CategoryArtKind.DailyLife, it) },
                    modifier = Modifier.weight(1f),
                    progressText = "120 / 240",
                    progress = 0.5f,
                    onClick = {},
                )
                PickerCard(
                    name = "TOEFL",
                    contentDescription = "",
                    art = { ExamArt(ExamArtKind.Toefl, it, locked = true) },
                    modifier = Modifier.weight(1f),
                    locked = true,
                    lockedLabel = "YAKINDA",
                )
            }
            ScrollToTopPill("Başa dön", onClick = {})
            RuledBottomBar(
                listOf(
                    BottomBarItem(LexiIcons.Stats, "İstatistik", {}),
                    BottomBarItem(LexiIcons.Invite, "Davet et", {}),
                    BottomBarItem(LexiIcons.Settings, "Ayarlar", {}),
                ),
            )
        }
    }
}
