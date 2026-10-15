package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Sizes of the icon-only buttons (screens' HTML). Navigation = square, game actions = circle. */
public object IconButtonDefaults {
    /** 44: back, close, settings. */
    public val NavSize: Dp = 44.dp

    /** 48: game pause. */
    public val NavSizeLarge: Dp = 48.dp

    /** 56: joker circle, backspace square. */
    public val ActionSize: Dp = 56.dp

    /** 18: back, close, speaker. */
    public val IconSize: Dp = 18.dp

    /** 16: pause. */
    public val IconSizeSmall: Dp = 16.dp

    /** 20: settings. */
    public val IconSizeMedium: Dp = 20.dp

    /** 22: joker bulb, backspace. */
    public val IconSizeLarge: Dp = 22.dp
}

/**
 * Square navigation button (back, close, settings, pause): white fill, 3dp ink border, no shadow. The game's pause
 * button is [IconButtonDefaults.NavSizeLarge] on cream with a 16dp icon.
 */
@Composable
public fun NavSquareButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = IconButtonDefaults.NavSize,
    iconSize: Dp = IconButtonDefaults.IconSize,
    containerColor: Color = LexiTheme.colors.white,
) {
    IconButtonBase(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        size = size,
        iconSize = iconSize,
        shape = LexiTheme.shapes.square,
        containerColor = containerColor,
        contentColor = LexiTheme.colors.ink,
        border = BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
    )
}

/** Circular game action (joker): red fill, white icon, 3dp ink border, 56dp. */
@Composable
public fun ActionCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = IconButtonDefaults.ActionSize,
    iconSize: Dp = IconButtonDefaults.IconSizeLarge,
    containerColor: Color = LexiTheme.colors.red,
    contentColor: Color = LexiTheme.colors.white,
    enabled: Boolean = true,
) {
    IconButtonBase(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        size = size,
        iconSize = iconSize,
        shape = LexiTheme.shapes.circle,
        containerColor = containerColor,
        contentColor = contentColor,
        border = BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
        enabled = enabled,
    )
}

/**
 * Square game action (backspace): white fill, 3dp border, 56dp. Also the 44dp speaker button of word rows
 * (`borderWidth = thin`, cream fill, 18dp icon).
 */
@Composable
public fun IconSquareButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = IconButtonDefaults.ActionSize,
    iconSize: Dp = IconButtonDefaults.IconSizeLarge,
    containerColor: Color = LexiTheme.colors.white,
    borderWidth: Dp = LexiTheme.borders.regular,
    enabled: Boolean = true,
) {
    IconButtonBase(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        size = size,
        iconSize = iconSize,
        shape = LexiTheme.shapes.square,
        containerColor = containerColor,
        contentColor = LexiTheme.colors.ink,
        border = BorderStroke(borderWidth, LexiTheme.colors.ink),
        enabled = enabled,
    )
}

@Composable
private fun IconButtonBase(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier,
    size: Dp,
    iconSize: Dp,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke,
    shadow: LexiShadow? = null,
    enabled: Boolean = true,
) {
    PressableSurface(
        onClick = onClick,
        // Disabled (input blocked): 50 % opacity like the joker sheet's disabled rows (no design of its own).
        modifier = modifier.size(size).alpha(if (enabled) 1f else JokerRowDefaults.DISABLED_ALPHA),
        enabled = enabled,
        shape = shape,
        color = containerColor,
        border = border,
        shadow = shadow,
        onClickLabel = null,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = contentColor, modifier = Modifier.size(iconSize))
    }
}

// design sample, not user-facing
@Preview
@Composable
private fun IconButtonsPreview() {
    LexiTheme(reducedMotion = false) {
        Row(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10),
        ) {
            NavSquareButton(LexiIcons.Back, contentDescription = "Geri", onClick = {})
            NavSquareButton(
                LexiIcons.Pause,
                contentDescription = "Duraklat",
                onClick = {},
                size = IconButtonDefaults.NavSizeLarge,
                iconSize = IconButtonDefaults.IconSizeSmall,
                containerColor = LexiTheme.colors.cream,
            )
            ActionCircleButton(LexiIcons.Bulb, contentDescription = "Joker kullan", onClick = {})
            IconSquareButton(LexiIcons.Backspace, contentDescription = "Harf sil", onClick = {})
        }
    }
}
