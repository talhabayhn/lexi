# ADR 0017: Compose UI tests for design-system behaviour

- Status: Accepted
- Date: 2026-10-14

## Context
Screenshots (ADR 0011) pin how components look, not how they behave: `PressableSurface` must fire exactly one
haptic and one sound per click, stay silent on a cancelled or disabled touch and follow the 80 ms press timing
(`motion.md` #1). That needs input injection and a controllable frame clock.

## Decision
- Behaviour tests use Compose Multiplatform `ui-test` (`org.jetbrains.compose.ui:ui-test`, version = CMP) with
  `runComposeUiTest` and `mainClock`, in `androidHostTest` under Robolectric (same runner as Roborazzi).
- `androidx.compose.ui:ui-test-manifest` (the AndroidX Compose release CMP resolves to, `androidx-compose` in the
  catalog) declares the host activity for Robolectric. Both are test-only dependencies.
- Fakes for `Haptics`/`SoundPlayer` are injected through `LocalHaptics`/`LocalSoundPlayer`.

## Consequences
- Bump `androidx-compose` together with Compose Multiplatform.
- The same API runs in `commonTest` on iOS if a component ever needs an iOS-only behaviour check.
- Alternative not taken: testing only extracted pure functions (misses the real pointer/clickable pipeline).
