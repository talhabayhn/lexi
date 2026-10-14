package dev.talhabayhan.lexi.core.designsystem.feedback

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import dev.talhabayhan.lexi.core.platform.haptics.Haptics
import dev.talhabayhan.lexi.core.platform.sound.SoundPlayer

/**
 * Haptics used by design-system components (`PressableSurface`). No-op until the app provides the platform
 * implementation (M4) at the root, so previews and screenshot tests stay silent.
 */
public val LocalHaptics: ProvidableCompositionLocal<Haptics> = staticCompositionLocalOf { Haptics {} }

/** Sound effects used by design-system components; no-op until the app provides the implementation (M4). */
public val LocalSoundPlayer: ProvidableCompositionLocal<SoundPlayer> = staticCompositionLocalOf { SoundPlayer {} }
