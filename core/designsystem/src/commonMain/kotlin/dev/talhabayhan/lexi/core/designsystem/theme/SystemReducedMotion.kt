package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.runtime.Composable

/**
 * Whether the OS asks for reduced motion (Android: animator duration scale 0 / "remove animations";
 * iOS: Reduce Motion). Observed, so toggling the setting while the app runs recomposes.
 */
@Composable
internal expect fun systemReducedMotion(): Boolean
