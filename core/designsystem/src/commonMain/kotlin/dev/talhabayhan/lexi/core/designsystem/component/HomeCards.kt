package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.WithCappedFontScale
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

/**
 * One layout tier of Home (`02c-home-v2`, `home-v2-notes.md` §1): [Regular] is the design at 390 × 844; the others are
 * DESIGN-DEBT steps for short phones ([Compact], drawn at 360 × 640) and huge font sizes ([Tight]).
 */
@Immutable
public enum class HomeTierSize(
    public val paddingTop: Dp,
    public val paddingBottom: Dp,
    public val paddingHorizontal: Dp,
    /** Logo + tagline to the daily card. */
    public val headerGap: Dp,
    /** Between the daily card, the "ÇARKINI KUR" block, OYNA and the footer. */
    public val blockGap: Dp,
    /** Between the hero card, chips, mode control and category row. */
    public val buildGap: Dp,
    /** "ÇARKINI KUR" label to the hero card. */
    public val labelGap: Dp,
    public val logo: LexiLogoSize,
    public val daily: DailyCardSize,
    public val heroMinHeight: Dp,
    public val heroPaddingVertical: Dp,
    public val chipHeight: Dp,
    public val modeHeight: Dp,
    public val pickerHeight: Dp,
    public val playHeight: Dp,
    /** OYNA's label: 30sp in the regular tier, 24sp below. */
    public val playLabelLarge: Boolean,
    public val playPaddingHorizontal: Dp,
    public val keySize: Dp,
    public val keyLabelGap: Dp,
) {
    Regular(
        paddingTop = 24.dp,
        paddingBottom = 28.dp,
        paddingHorizontal = 20.dp,
        headerGap = 14.dp,
        blockGap = 16.dp,
        buildGap = 10.dp,
        labelGap = 8.dp,
        logo = LexiLogoSize.Header,
        daily = DailyCardSize.Regular,
        heroMinHeight = 76.dp,
        heroPaddingVertical = 12.dp,
        chipHeight = 48.dp,
        modeHeight = 56.dp,
        pickerHeight = 48.dp,
        playHeight = 68.dp,
        playLabelLarge = true,
        playPaddingHorizontal = 18.dp,
        keySize = 56.dp,
        keyLabelGap = 6.dp,
    ),
    Compact(
        paddingTop = 8.dp,
        paddingBottom = 12.dp,
        paddingHorizontal = 20.dp,
        headerGap = 6.dp,
        blockGap = 8.dp,
        buildGap = 6.dp,
        labelGap = 4.dp,
        logo = LexiLogoSize.HeaderCompact,
        daily = DailyCardSize.Compact,
        heroMinHeight = 56.dp,
        heroPaddingVertical = 8.dp,
        chipHeight = 48.dp,
        modeHeight = 48.dp,
        pickerHeight = 48.dp,
        playHeight = 52.dp,
        playLabelLarge = false,
        playPaddingHorizontal = 18.dp,
        keySize = 44.dp,
        keyLabelGap = 3.dp,
    ),
    Tight(
        paddingTop = 8.dp,
        paddingBottom = 12.dp,
        paddingHorizontal = 16.dp,
        headerGap = 6.dp,
        blockGap = 8.dp,
        buildGap = 6.dp,
        labelGap = 4.dp,
        logo = LexiLogoSize.HeaderCompact,
        daily = DailyCardSize.Tight,
        heroMinHeight = 56.dp,
        heroPaddingVertical = 8.dp,
        chipHeight = 48.dp,
        modeHeight = 48.dp,
        pickerHeight = 48.dp,
        playHeight = 52.dp,
        playLabelLarge = false,
        playPaddingHorizontal = 14.dp,
        keySize = 44.dp,
        keyLabelGap = 3.dp,
    ),
}

/** Daily card padding per Home tier (`02c-home-v2`: about 56dp high in the regular tier). */
@Immutable
public enum class DailyCardSize(internal val paddingVertical: Dp, internal val paddingHorizontal: Dp) {
    Regular(8.dp, 12.dp),
    Compact(6.dp, 12.dp),
    Tight(6.dp, 10.dp),
}

/** The daily wheel's state as the card's status badge shows it (`02c-home-v2`). */
public enum class DailyCardStatus {
    /** Not played today: yellow card, a badge with an empty ring, "Çöz" and a chevron; the card is a button. */
    Solve,

    /** Played today: lighter yellow card, a badge with a check and "Tamamlandı"; not clickable. */
    Completed,

    /** No wheel today: muted card without a badge; not clickable. */
    Unavailable,
}

/** Geometry of the home screen's own parts (`02c-home-v2`). */
public object HomeDefaults {
    /** Pinned and control text (OYNA, badges, footer labels, chips, mode cells, hero count, pills) stop at 1.3×. */
    public const val CONTROL_FONT_SCALE_CAP: Float = 1.3f

    /** The daily card's text stops at 1.5×. */
    public const val DAILY_FONT_SCALE_CAP: Float = 1.5f

    /** The category sheet is at most this share of the screen high; its cards scroll inside. */
    public const val SHEET_MAX_HEIGHT_FRACTION: Float = 0.85f

    /** Logo + tagline height (36 + 4 + the 11sp line): the daily-gift toast floats below it. */
    public val HeaderHeight: Dp = 56.dp

    /**
     * Background blue circle 112, centred on the screen's right edge at the daily card's centre: small enough to stay
     * clear of the hero card under the 56dp daily card.
     */
    public val CircleSize: Dp = 112.dp

    /** Background red half-pill 60 × 120 at the screen's left edge, centred on the footer row; 16dp of it show. */
    public val PillWidth: Dp = 60.dp
    public val PillHeight: Dp = 120.dp
    public val PillVisible: Dp = 16.dp

    /** Daily card: text column to the badge 12; the text keeps at least 140dp, else the badge drops its word. */
    internal val DailyGap: Dp = 12.dp
    internal val DailyTextMinWidth: Dp = 140.dp

    /** Daily status badge: at least 32 high, padding 0 8, gap 6; ring / check 14, check mark 10, chevron 14. */
    internal val BadgeMinHeight: Dp = 32.dp
    internal val BadgePadding: Dp = 8.dp
    internal val BadgeGap: Dp = 6.dp
    internal val BadgeIcon: Dp = 14.dp
    internal val BadgeCheck: Dp = 10.dp
    internal val BadgeChevron: Dp = 14.dp

    /** Hero card: padding 14 sideways, gap 12, the count column 132 wide (× font scale up to the cap), gap 6. */
    internal val HeroPaddingHorizontal: Dp = 14.dp
    internal val HeroGap: Dp = 12.dp
    public val HeroCountWidth: Dp = 132.dp
    internal val HeroCountGap: Dp = 6.dp

    /** Level chips: 6 equal, gap 6; a label that does not fit (large fonts, narrow phones) shrinks down to 70 %. */
    public val ChipGap: Dp = 6.dp
    internal const val CHIP_MIN_SCALE: Float = 0.7f

    /** Picker row ("Kategori  Karışık ▾"): padding 0 12, gap 10, chevron 18. */
    internal val PickerPadding: Dp = 12.dp
    internal val PickerGap: Dp = 10.dp
    internal val PickerChevron: Dp = 18.dp

    /** OYNA badge: padding 4 / 8, 2dp border. */
    internal val PlayBadgePadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp)

    /** Footer: four columns, gap 12; key icon 24; count badge 22 high, padding 0 5, 9dp out of the key's corner. */
    public val FooterGap: Dp = 12.dp
    internal val KeyIcon: Dp = 24.dp
    internal val CountBadgeSize: Dp = 22.dp
    internal val CountBadgePadding: Dp = 5.dp
    internal val CountBadgeOffset: Dp = 9.dp

    /** Smallest scale a CTA caption shrinks to before it is cut with an ellipsis. */
    internal const val CAPTION_MIN_SCALE: Float = 0.7f
    internal val CAPTION_STEP: TextUnit = 0.5.sp
}

/**
 * "Günün çarkı" card (`02c-home-v2`): left the [title] ("Günün çarkı") over the [description] ("Herkes aynı çarkı
 * çözüyor", or the score and the next-wheel countdown once solved); right one status badge for [status] with
 * [badgeLabel] ("Çöz" / "Tamamlandı"). When the text column would get narrower than 140dp (narrow phones, large fonts)
 * the badge keeps only its icon. Yellow and a button while [DailyCardStatus.Solve]; lighter yellow when completed;
 * muted without badge when unavailable. One node for accessibility, read as [contentDescription].
 */
@Composable
public fun DailyCard(
    title: String,
    description: String,
    status: DailyCardStatus,
    badgeLabel: String?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: DailyCardSize = DailyCardSize.Regular,
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
                        Text(
                            title,
                            style = typography.bodyStrong16.withCssLineHeight(DAILY_TITLE_LINE_HEIGHT),
                            color = textColor,
                        )
                        Text(
                            description,
                            style = typography.caption12SemiBold.withCssLineHeight(DAILY_TEXT_LINE_HEIGHT),
                            color = textColor,
                        )
                    }
                },
                { if (!unavailable) DailyBadge(status, badgeLabel) },
                { if (!unavailable) DailyBadge(status, label = null) },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {}
                .padding(horizontal = size.paddingHorizontal, vertical = size.paddingVertical),
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

/**
 * The daily card's row: the text column on the left, the badge on the right; the badge keeps its word only while the
 * text keeps [HomeDefaults.DailyTextMinWidth]. Intrinsics follow the same choice (Home measures its tiers with them).
 */
private object DailyRowPolicy : MultiContentMeasurePolicy {
    override fun MeasureScope.measure(measurables: List<List<Measurable>>, constraints: Constraints): MeasureResult {
        val (text, full, iconOnly) = measurables
        // Always inside a full-width card; an unbounded width (never used) falls back to the minimum.
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val gap = HomeDefaults.DailyGap.roundToPx()
        val loose = Constraints()
        val fullBadge = full.firstOrNull()?.measure(loose)
        val keepWord = fullBadge != null && width - fullBadge.width - gap >= HomeDefaults.DailyTextMinWidth.roundToPx()
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
        val gap = HomeDefaults.DailyGap.roundToPx()
        val fullWidth = full.firstOrNull()?.maxIntrinsicWidth(Constraints.Infinity)
        val keepWord = fullWidth != null && width - fullWidth - gap >= HomeDefaults.DailyTextMinWidth.roundToPx()
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
    WithCappedFontScale(HomeDefaults.CONTROL_FONT_SCALE_CAP) {
        Row(
            modifier = Modifier
                .heightIn(min = HomeDefaults.BadgeMinHeight)
                .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
                .padding(horizontal = HomeDefaults.BadgePadding),
            horizontalArrangement = Arrangement.spacedBy(HomeDefaults.BadgeGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val solved = status == DailyCardStatus.Completed
            Box(
                modifier = Modifier
                    .size(HomeDefaults.BadgeIcon)
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
                        modifier = Modifier.size(HomeDefaults.BadgeCheck),
                    )
                }
            }
            if (label != null) Text(label, style = LexiTheme.typography.caption12ExtraBold, maxLines = 1)
            if (!solved) {
                Icon(
                    LexiIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(HomeDefaults.BadgeChevron),
                )
            }
        }
    }
}

/** The daily title's `line-height: 1.1` and the description's 1.2. */
private const val DAILY_TITLE_LINE_HEIGHT = 1.1
private const val DAILY_TEXT_LINE_HEIGHT = 1.2

/**
 * OYNA: the red CTA with s6 shadow ([height] 76 on `02-home`, 68 on `02c-home-v2`); [badge] ("A1 · KLASİK · 26 HARF")
 * is a small white chip on the right with a 2dp frame. A long badge shrinks (then ends in an ellipsis) instead of
 * touching the label. [labelStyle] / [badgeStyle] default to `02-home`'s 30sp label and 14sp badge.
 */
@Composable
public fun PlayButton(
    label: String,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = LexiButtonDefaults.PlayHeight,
    labelStyle: TextStyle = LexiTheme.typography.button30,
    badgeStyle: TextStyle = LexiTheme.typography.badge14,
    badgePadding: PaddingValues = CardDefaults.BadgePadding,
    contentPadding: PaddingValues = LexiButtonDefaults.ContentPadding,
) {
    val colors = LexiTheme.colors
    LexiButton(
        onClick = onClick,
        height = height,
        shadow = LexiTheme.shadows.s6,
        horizontalArrangement = Arrangement.SpaceBetween,
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(label, style = labelStyle, color = colors.white, maxLines = 1)
        ShrinkingText(
            text = badge,
            style = badgeStyle,
            color = colors.ink,
            modifier = Modifier
                .padding(start = LexiTheme.dimens.gap12)
                .weight(1f, fill = false)
                .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.thin, colors.ink))
                .padding(badgePadding),
        )
    }
}

/** OYNA of Home v2 (`02c-home-v2`): 68 / 52dp, 30 / 24sp label, 11sp badge with padding 4 / 8. */
@Composable
public fun HomePlayButton(
    label: String,
    badge: String,
    onClick: () -> Unit,
    tier: HomeTierSize,
    modifier: Modifier = Modifier,
) {
    val typography = LexiTheme.typography
    PlayButton(
        label = label,
        badge = badge,
        onClick = onClick,
        modifier = modifier,
        height = tier.playHeight,
        labelStyle = if (tier.playLabelLarge) typography.button30 else typography.button24,
        badgeStyle = typography.badge11,
        badgePadding = HomeDefaults.PlayBadgePadding,
        contentPadding = PaddingValues(horizontal = tier.playPaddingHorizontal),
    )
}

/** One line, right aligned; when it does not fit, the font shrinks to a minimum, then it is ellipsized. */
@Composable
private fun ShrinkingText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(TextStyle(textAlign = TextAlign.End)),
        color = { color },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = style.fontSize * HomeDefaults.CAPTION_MIN_SCALE,
            maxFontSize = style.fontSize,
            stepSize = HomeDefaults.CAPTION_STEP,
        ),
    )
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun HomeCardsPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        ) {
            DailyCard(
                title = "Günün çarkı",
                description = "Herkes aynı çarkı çözüyor",
                status = DailyCardStatus.Solve,
                badgeLabel = "Çöz",
                contentDescription = "Günün çarkı, herkes aynı çarkı çözüyor, çözülmedi",
                onClick = {},
            )
            HomePlayButton("OYNA", "A1 · KLASİK · 26 HARF", onClick = {}, tier = HomeTierSize.Regular)
        }
    }
}
