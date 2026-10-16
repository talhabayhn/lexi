package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Geometry of the Lexi Pro card of `11-settings`. */
public object ProBannerDefaults {
    /** 34: the yellow diamond (a 34 square rotated by 45 degrees). */
    public val DiamondSize: Dp = 34.dp
}

/**
 * The ink "Lexi Pro" card at the top of the settings: red hard shadow (5), padding 12 / 14, gap 12, a yellow diamond,
 * [title] (18/800) over [subtitle] (13/500) in cream and a [trailing] slot (the price). Display only.
 */
@Composable
public fun ProBanner(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .lexiSurface(
                color = colors.ink,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = LexiTheme.shadows.s5Red,
            )
            .semantics(mergeDescendants = true) {}
            .padding(PaddingValues(horizontal = dimens.gap14, vertical = dimens.gap12)),
        horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DecorShape(DecorShapeKind.Diamond, colors.yellow, Modifier.size(ProBannerDefaults.DiamondSize))
        Column(Modifier.weight(1f)) {
            Text(title, style = LexiTheme.typography.heading18, color = colors.cream)
            Text(subtitle, style = LexiTheme.typography.caption13, color = colors.cream)
        }
        trailing()
    }
}
