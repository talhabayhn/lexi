package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Geometry of the exam packs screen (`10-exam-packs`). */
public object ExamPackDefaults {
    /** Intro banner: `padding: 14px 16px`, gap 4; its blue circle is CSS content-box 90 + 2 × 3 border at -24/-24. */
    internal val IntroPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    internal val IntroCircleSize: Dp = 96.dp
    internal val IntroCircleOffset: Dp = 24.dp

    /** `max-width: 250px` of the intro text. */
    internal val IntroTextMaxWidth: Dp = 250.dp

    /** Pack row: min-height 76 (border-box), `padding: 10px 12px`, gap 12. */
    public val RowMinHeight: Dp = 76.dp
    internal val RowPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp)

    /** Pack badge: 48 border-box with a 2dp frame. */
    internal val BadgeSize: Dp = 48.dp

    /**
     * 13px badge text, kept at this size whatever the font scale: the badge is a fixed tile and decoration (the
     * title next to it carries the name), so large text would only be cut off.
     */
    internal val BadgeTextSize: Dp = 13.dp

    /** Trailing chip: `padding: 3px 7px`, 2dp frame. */
    internal val ChipPadding: PaddingValues = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
}

/** The badge colors of the design's pack rows, in the design's order (LGS, MEB, YKS, YDS, IELTS, TOEFL). */
public enum class ExamPackBadgeStyle(public val shape: DecorShapeKind) {
    RedSquare(DecorShapeKind.Square),
    BlueCircle(DecorShapeKind.Circle),
    YellowLeaf(DecorShapeKind.Leaf),
    InkSquare(DecorShapeKind.Square),
    RedCircle(DecorShapeKind.Circle),
    BlueLeaf(DecorShapeKind.Leaf),
}

/**
 * The yellow intro banner of `10-exam-packs`: [title] (20/800) over [text] (14/500, at most 250dp wide), a blue circle
 * cut off at the bottom-right corner, s5 shadow. Display only.
 */
@Composable
public fun ExamIntroCard(title: String, text: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Box(
        modifier = modifier
            .fillMaxWidth()
            .lexiSurface(
                color = colors.yellow,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = LexiTheme.shadows.s5,
            )
            .clipToBounds(),
    ) {
        DecorShape(
            kind = DecorShapeKind.Circle,
            color = colors.blue,
            border = BorderStroke(LexiTheme.borders.regular, colors.ink),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = ExamPackDefaults.IntroCircleOffset, y = ExamPackDefaults.IntroCircleOffset)
                .size(ExamPackDefaults.IntroCircleSize),
        )
        Column(
            modifier = Modifier.padding(ExamPackDefaults.IntroPadding),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap4),
        ) {
            Text(title, style = typography.heading20)
            Text(
                text,
                style = typography.caption14,
                modifier = Modifier.widthIn(max = ExamPackDefaults.IntroTextMaxWidth),
            )
        }
    }
}

/**
 * A pack row of `10-exam-packs`: the [badge] text (13/800, not scaled with the font size) on its [badgeStyle] shape, [title] (17/800) over
 * [subtitle] (13/500) and a [trailing] chip ([ExamPackProgressChip], [ExamPackProChip]). White, 3dp frame, s3 shadow,
 * at least 76dp high (it grows with large fonts). [onClickLabel] names the tap's action for screen readers.
 */
@Composable
public fun ExamPackRow(
    badge: String,
    badgeStyle: ExamPackBadgeStyle,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val (fill, content) = badgeStyle.colors()
    PressableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = ExamPackDefaults.RowMinHeight),
        shadow = LexiTheme.shadows.s3,
        onClickLabel = onClickLabel,
        propagateMinConstraints = true,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(ExamPackDefaults.RowPadding),
            horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(ExamPackDefaults.BadgeSize), contentAlignment = Alignment.Center) {
                DecorShape(
                    kind = badgeStyle.shape,
                    color = fill,
                    border = BorderStroke(LexiTheme.borders.thin, colors.ink),
                    modifier = Modifier.fillMaxSize(),
                )
                val badgeTextSize = with(LocalDensity.current) { ExamPackDefaults.BadgeTextSize.toSp() }
                Text(
                    badge,
                    style = typography.caption13.copy(fontSize = badgeTextSize, fontWeight = FontWeight.ExtraBold),
                    color = content,
                    maxLines = 1,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = typography.bodyStrong17)
                Text(subtitle, style = typography.caption13)
            }
            trailing()
        }
    }
}

/** The cream progress chip of a free pack ("%34"): 12/800, 2dp frame. */
@Composable
public fun ExamPackProgressChip(text: String, modifier: Modifier = Modifier) {
    ExamPackChip(text, LexiTheme.colors.cream, LexiTheme.colors.ink, modifier)
}

/** The ink "PRO" chip of a Pro pack: yellow 12/800 text, 2dp frame. */
@Composable
public fun ExamPackProChip(text: String, modifier: Modifier = Modifier) {
    ExamPackChip(text, LexiTheme.colors.ink, LexiTheme.colors.yellow, modifier)
}

@Composable
private fun ExamPackChip(text: String, fill: Color, content: Color, modifier: Modifier) {
    Text(
        text,
        style = LexiTheme.typography.caption12ExtraBold,
        color = content,
        maxLines = 1,
        modifier = modifier
            .lexiSurface(fill, BorderStroke(LexiTheme.borders.thin, LexiTheme.colors.ink))
            .padding(ExamPackDefaults.ChipPadding),
    )
}

@Composable
private fun ExamPackBadgeStyle.colors(): Pair<Color, Color> {
    val colors = LexiTheme.colors
    return when (this) {
        ExamPackBadgeStyle.RedSquare, ExamPackBadgeStyle.RedCircle -> colors.red to colors.white
        ExamPackBadgeStyle.BlueCircle, ExamPackBadgeStyle.BlueLeaf -> colors.blue to colors.white
        ExamPackBadgeStyle.YellowLeaf -> colors.yellow to colors.ink
        ExamPackBadgeStyle.InkSquare -> colors.ink to colors.white
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun ExamPackCardsPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14),
        ) {
            ExamIntroCard(
                "Sınava çark çevirerek hazırlan",
                "Her paket, sınavın resmi kelime listesine göre seviyelendirilmiş çarklardan oluşur.",
            )
            ExamPackRow("LGS", ExamPackBadgeStyle.RedSquare, "LGS İngilizce", "8. sınıf · ünite bazlı · 600 kelime", {
            }) {
                ExamPackProgressChip("%34")
            }
            ExamPackRow("YDS", ExamPackBadgeStyle.InkSquare, "YDS / YÖKDİL", "Akademik metin kelimeleri · 2000", {}) {
                ExamPackProChip("PRO")
            }
        }
    }
}
