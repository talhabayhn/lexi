package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Lexi's Bauhaus Classic theme. Light only: system dark mode and dynamic color are ignored (ADR 0010).
 *
 * Provides the tokens read through [LexiTheme] and configures Material3 underneath with the same palette, Jost
 * and square corners, so stock components cannot leak foreign colors or rounded shapes (ARCHITECTURE.md §9).
 *
 * @param reducedMotion overrides the OS reduced-motion setting (tests, previews, an in-app option later).
 */
@Composable
public fun LexiTheme(reducedMotion: Boolean = systemReducedMotion(), content: @Composable () -> Unit) {
    val typography = rememberLexiTypography()
    val motion = remember(reducedMotion) { LexiMotion(reducedMotion) }
    val materialTypography = remember(typography) { typography.toMaterialTypography() }
    CompositionLocalProvider(
        LocalLexiColors provides LightLexiColors,
        LocalLexiTypography provides typography,
        LocalLexiDimens provides DefaultLexiDimens,
        LocalLexiBorders provides DefaultLexiBorders,
        LocalLexiShapes provides DefaultLexiShapes,
        LocalLexiShadows provides DefaultLexiShadows,
        LocalLexiMotion provides motion,
    ) {
        MaterialTheme(colorScheme = LexiMaterialColors, typography = materialTypography, shapes = LexiMaterialShapes) {
            CompositionLocalProvider(
                LocalContentColor provides LightLexiColors.ink,
                LocalTextStyle provides typography.body16,
                content = content,
            )
        }
    }
}

/** Token access: `LexiTheme.colors.red`, `LexiTheme.typography.title`, `LexiTheme.dimens.gap12`, … */
public object LexiTheme {
    public val colors: LexiColors
        @Composable @ReadOnlyComposable
        get() = LocalLexiColors.current

    public val typography: LexiTypography
        @Composable @ReadOnlyComposable
        get() = LocalLexiTypography.current

    public val dimens: LexiDimens
        @Composable @ReadOnlyComposable
        get() = LocalLexiDimens.current

    public val borders: LexiBorders
        @Composable @ReadOnlyComposable
        get() = LocalLexiBorders.current

    public val shapes: LexiShapes
        @Composable @ReadOnlyComposable
        get() = LocalLexiShapes.current

    public val shadows: LexiShadows
        @Composable @ReadOnlyComposable
        get() = LocalLexiShadows.current

    public val motion: LexiMotion
        @Composable @ReadOnlyComposable
        get() = LocalLexiMotion.current
}

internal val LocalLexiColors = staticCompositionLocalOf { LightLexiColors }
internal val LocalLexiTypography = staticCompositionLocalOf<LexiTypography> {
    error("LexiTypography is missing: wrap the content in LexiTheme { … }")
}
internal val LocalLexiDimens = staticCompositionLocalOf { DefaultLexiDimens }
internal val LocalLexiBorders = staticCompositionLocalOf { DefaultLexiBorders }
internal val LocalLexiShapes = staticCompositionLocalOf { DefaultLexiShapes }
internal val LocalLexiShadows = staticCompositionLocalOf { DefaultLexiShadows }
internal val LocalLexiMotion = staticCompositionLocalOf { LexiMotion(reducedMotion = false) }

@Composable
private fun rememberLexiTypography(): LexiTypography {
    val jost = rememberJostFontFamily()
    return remember(jost) { lexiTypography(jost) }
}

/** Material3 roles mapped onto the Lexi palette; containers are white cards on the cream background. */
private val LexiMaterialColors: ColorScheme = with(LightLexiColors) {
    lightColorScheme(
        primary = red,
        onPrimary = white,
        primaryContainer = red,
        onPrimaryContainer = white,
        inversePrimary = yellow,
        secondary = blue,
        onSecondary = white,
        secondaryContainer = blue,
        onSecondaryContainer = white,
        tertiary = yellow,
        onTertiary = ink,
        tertiaryContainer = yellow,
        onTertiaryContainer = ink,
        background = cream,
        onBackground = ink,
        surface = cream,
        onSurface = ink,
        surfaceVariant = disabledFill,
        onSurfaceVariant = inkMuted,
        surfaceTint = cream,
        inverseSurface = ink,
        inverseOnSurface = cream,
        error = red,
        onError = white,
        errorContainer = red,
        onErrorContainer = white,
        outline = ink,
        outlineVariant = borderMuted,
        scrim = ink,
        surfaceBright = white,
        surfaceDim = disabledFill,
        surfaceContainer = white,
        surfaceContainerHigh = white,
        surfaceContainerHighest = white,
        surfaceContainerLow = white,
        surfaceContainerLowest = white,
    )
}

private val SquareCorners = RoundedCornerShape(0.dp)

private val LexiMaterialShapes = Shapes(
    extraSmall = SquareCorners,
    small = SquareCorners,
    medium = SquareCorners,
    large = SquareCorners,
    extraLarge = SquareCorners,
)

private fun LexiTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = wheelLetter,
    displayMedium = scoreHero,
    displaySmall = display,
    headlineLarge = display,
    headlineMedium = clue,
    headlineSmall = title,
    titleLarge = heading20,
    titleMedium = heading18,
    titleSmall = bodyStrong16,
    bodyLarge = body16,
    bodyMedium = body15,
    bodySmall = caption13,
    labelLarge = bodyStrong14,
    labelMedium = label13,
    labelSmall = label12,
)
