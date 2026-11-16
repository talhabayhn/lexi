package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * [content] honours the system font size up to [maxScale] (e.g. Home's controls stop at 1.3×, `home-v2-notes.md` §1);
 * dp sizes are unchanged. Below the cap the density is passed through as it is; above it the capped text scales
 * linearly (Android 14's non-linear scaling only applies below the cap). The composition structure is the same on
 * both sides, so crossing the cap keeps remembered state.
 */
@Composable
public fun WithCappedFontScale(maxScale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val capped = if (density.fontScale <= maxScale) density else Density(density.density, maxScale)
    CompositionLocalProvider(LocalDensity provides capped, content = content)
}
