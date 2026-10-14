package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.dashedBorder
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Color roles of [LexiButton] (`16-components` § BUTONLAR). */
public enum class LexiButtonVariant {
    /** Red fill, white text: OYNA, BAŞLA, Bırak. */
    Primary,

    /** White fill, ink text. */
    Secondary,

    /** Yellow fill, ink text: PAS. */
    Pass,

    /** Ink fill, yellow text: "Kısa reklam izle", NAT. */
    Ink,
}

/** Sizes of [LexiButton] and [LexiTextLink] (`16-components`, CTAs in the screens). */
public object LexiButtonDefaults {
    /** 52: compact buttons. */
    public val Height: Dp = 52.dp

    /** 48: dialog buttons, level chips. */
    public val SmallHeight: Dp = 48.dp

    /** 56: PAS, ad button. */
    public val LargeHeight: Dp = 56.dp

    /** 60: result CTAs, notebook review CTA. */
    public val LargerHeight: Dp = 60.dp

    /** 64: onboarding CTA. */
    public val HeroHeight: Dp = 64.dp

    /** 68: BAŞLA. */
    public val StartHeight: Dp = 68.dp

    /** 76: OYNA. */
    public val PlayHeight: Dp = 76.dp

    /** `padding: 0 20px`. */
    public val ContentPadding: PaddingValues = PaddingValues(horizontal = 20.dp)

    /** Gap between a leading icon and the label. */
    public val IconGap: Dp = 10.dp

    /** Leading icon size. */
    public val IconSize: Dp = 18.dp

    /** Text link: `height: 44px; padding: 0 8px`. */
    public val LinkHeight: Dp = 44.dp
    public val LinkContentPadding: PaddingValues = PaddingValues(horizontal = 8.dp)
}

@Composable
private fun LexiButtonVariant.containerColor(): Color = when (this) {
    LexiButtonVariant.Primary -> LexiTheme.colors.red
    LexiButtonVariant.Secondary -> LexiTheme.colors.white
    LexiButtonVariant.Pass -> LexiTheme.colors.yellow
    LexiButtonVariant.Ink -> LexiTheme.colors.ink
}

@Composable
private fun LexiButtonVariant.defaultContentColor(): Color = when (this) {
    LexiButtonVariant.Primary -> LexiTheme.colors.white
    LexiButtonVariant.Secondary, LexiButtonVariant.Pass -> LexiTheme.colors.ink
    LexiButtonVariant.Ink -> LexiTheme.colors.yellow
}

/**
 * Bauhaus button: square, 3dp ink border, hard shadow (s5; s6 for hero CTAs, `null` for flat dialog buttons), press
 * per `PressableSurface`. Disabled: `disabledFill`, dashed `borderMuted` border, `inkMuted` content, no shadow.
 * [content] is laid out in a row (centered by default; `Arrangement.SpaceBetween` for OYNA-style CTAs) and inherits
 * the content color.
 */
@Composable
public fun LexiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LexiButtonVariant = LexiButtonVariant.Primary,
    enabled: Boolean = true,
    height: Dp = LexiButtonDefaults.Height,
    shadow: LexiShadow? = LexiTheme.shadows.s5,
    contentColor: Color = variant.defaultContentColor(),
    contentPadding: PaddingValues = LexiButtonDefaults.ContentPadding,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = LexiTheme.colors
    val border = LexiTheme.borders.regular
    val row: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxHeight().padding(contentPadding),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
    if (enabled) {
        PressableSurface(
            onClick = onClick,
            modifier = modifier.height(height),
            color = variant.containerColor(),
            border = BorderStroke(border, colors.ink),
            shadow = shadow,
            propagateMinConstraints = true,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor, content = row)
        }
    } else {
        Box(
            modifier = modifier
                .height(height)
                .semantics {
                    role = Role.Button
                    disabled()
                }
                .background(colors.disabledFill)
                .dashedBorder(border, colors.borderMuted)
                .padding(border),
            propagateMinConstraints = true,
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.inkMuted, content = row)
        }
    }
}

/** [LexiButton] with a text label (style e.g. `button16`…`button30`) and an optional leading icon. */
@Composable
public fun LexiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LexiButtonVariant = LexiButtonVariant.Primary,
    enabled: Boolean = true,
    textStyle: TextStyle = LexiTheme.typography.button16,
    height: Dp = LexiButtonDefaults.Height,
    shadow: LexiShadow? = LexiTheme.shadows.s5,
    contentColor: Color = variant.defaultContentColor(),
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = LexiButtonDefaults.ContentPadding,
) {
    LexiButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        enabled = enabled,
        height = height,
        shadow = shadow,
        contentColor = contentColor,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(LexiButtonDefaults.IconGap, Alignment.CenterHorizontally),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(LexiButtonDefaults.IconSize))
        }
        Text(text = text, style = textStyle, color = LocalContentColor.current, maxLines = 1)
    }
}

/** Underlined text action ("Metin bağlantı", "Atla", "Ana menüye dön"); 44dp high touch target, no shadow. */
@Composable
public fun LexiTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LexiTheme.typography.link15,
    contentPadding: PaddingValues = LexiButtonDefaults.LinkContentPadding,
) {
    PressableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = LexiButtonDefaults.LinkHeight),
        color = Color.Transparent,
        border = null,
        shadow = null,
    ) {
        Text(text = text, style = textStyle, color = LexiTheme.colors.ink, modifier = Modifier.padding(contentPadding))
    }
}

// design sample, not user-facing
@Preview
@Composable
private fun LexiButtonPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12)) {
                LexiButton("Birincil", onClick = {})
                LexiButton("İkincil", onClick = {}, variant = LexiButtonVariant.Secondary)
                LexiButton("PAS", onClick = {}, variant = LexiButtonVariant.Pass)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12)) {
                LexiButton("Pasif", onClick = {}, enabled = false)
                LexiTextLink("Metin bağlantı", onClick = {})
            }
            LexiButton(
                text = "Kısa reklam izle · +1 joker",
                onClick = {},
                variant = LexiButtonVariant.Ink,
                shadow = null,
                leadingIcon = LexiIcons.Play,
                height = LexiButtonDefaults.LargeHeight,
            )
        }
    }
}
