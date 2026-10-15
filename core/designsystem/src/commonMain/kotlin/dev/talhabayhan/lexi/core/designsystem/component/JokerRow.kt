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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Joker sheet geometry (`05-joker-sheet.html`). */
public object JokerRowDefaults {
    /** Row: `padding: 10px 12px; min-height: 68px; gap: 12px`. */
    public val Padding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    public val MinHeight: Dp = 68.dp
    public val Gap: Dp = 12.dp

    /** Count chip: 40dp (border-box) with a 2dp border. */
    public val ChipSize: Dp = 40.dp

    /** Cost chip: `padding: 3px 8px`, 2dp border on cream. */
    public val CostPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 3.dp)

    /** A joker that can't be used: `opacity: .5`, no shadow. Also the disabled look of game inputs. */
    public const val DISABLED_ALPHA: Float = 0.5f

    /** Status pill above the sheet: `padding: 6px 12px`, 3dp border, white. */
    public val PillPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp)

    /** Ad button `margin-top: 4px` on top of the sheet gap. */
    public val AdButtonTopMargin: Dp = 4.dp
}

/**
 * A joker of the joker sheet: count chip ([marker] in [markerColor], [count] in [markerContentColor]), title (17/800),
 * description (13/500) and cost chip (14/800 on cream, 2dp border). White, 3dp border, s3; when not [enabled] it is
 * drawn at 50 % opacity without shadow and is not clickable. [countDescription] is read instead of the bare count.
 */
@Composable
public fun JokerRow(
    count: String,
    countDescription: String,
    title: String,
    description: String,
    cost: String,
    marker: DecorShapeKind,
    markerColor: Color,
    markerContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    PressableSurface(
        onClick = onClick,
        enabled = enabled,
        shadow = if (enabled) LexiTheme.shadows.s3 else null,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = JokerRowDefaults.MinHeight)
            .alpha(if (enabled) 1f else JokerRowDefaults.DISABLED_ALPHA),
        propagateMinConstraints = true,
    ) {
        Row(
            modifier = Modifier.padding(JokerRowDefaults.Padding),
            horizontalArrangement = Arrangement.spacedBy(JokerRowDefaults.Gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(JokerRowDefaults.ChipSize)
                    .clearAndSetSemantics { contentDescription = countDescription },
                contentAlignment = Alignment.Center,
            ) {
                DecorShape(
                    marker,
                    markerColor,
                    Modifier.fillMaxSize(),
                    BorderStroke(LexiTheme.borders.thin, colors.ink),
                )
                Text(count, style = typography.bodyStrong17, color = markerContentColor)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = typography.bodyStrong17)
                Text(description, style = typography.caption13)
            }
            Text(
                text = cost,
                style = typography.bodyStrong14,
                modifier = Modifier
                    .lexiSurface(colors.cream, BorderStroke(LexiTheme.borders.thin, colors.ink))
                    .padding(JokerRowDefaults.CostPadding),
            )
        }
    }
}

/** White pill with a 3dp border and 14/800 text: "Süre işlemeye devam ediyor · 1:31" above the joker sheet. */
@Composable
public fun StatusPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = LexiTheme.typography.bodyStrong14,
        modifier = modifier
            .lexiSurface(LexiTheme.colors.white, BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink))
            .padding(JokerRowDefaults.PillPadding),
    )
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun JokerRowPreview() {
    LexiTheme(reducedMotion = false) {
        val colors = LexiTheme.colors
        Column(
            modifier = Modifier.background(colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        ) {
            StatusPill("Süre işlemeye devam ediyor · 1:31")
            JokerRow(
                count = "2",
                countDescription = "2 hakkın var",
                title = "Harf aç",
                description = "Sıradaki boş kutuya doğru harfi koyar",
                cost = "−10 sn",
                marker = DecorShapeKind.Circle,
                markerColor = colors.blue,
                markerContentColor = colors.white,
                onClick = {},
            )
            JokerRow(
                count = "0",
                countDescription = "hakkın yok",
                title = "Telaffuzu dinle",
                description = "Cevabı sesli okur (C1 ve üstünde kapalı)",
                cost = "−10 puan",
                marker = DecorShapeKind.Square,
                markerColor = colors.white,
                markerContentColor = colors.ink,
                onClick = {},
                enabled = false,
            )
        }
    }
}
