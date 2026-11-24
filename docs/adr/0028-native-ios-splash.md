# ADR 0028: The iOS splash is animated natively with Core Animation

- Status: Accepted
- Date: 2026-11-24

## Context
Months of Compose-level fixes (layer outsets, hidden warm-ups, the S4 first-launch hold) did not make the iOS first
launch smooth: on a fresh install the splash animation is skipped or janks (`docs/PERF.md` § iOS first launch, § 1.1.2
freeze, § Launch logo). Compose Multiplatform on iOS renders through Skia / Metal and builds GPU pipelines on first use
on the main thread, and Kotlin/Native and Compose start up on the same thread; an animation Compose draws stalls with
them. A Core Animation animation is composited by the system's render server, outside the app process: once committed
it keeps playing at the display's rate while the main thread is blocked.

## Decision
- **On iOS the splash (`motion.md` #9) is a native overlay** (`shared/src/iosMain/.../splash`): `NativeSplashOverlay`
  (plain `UIView`s: the shapes as coloured views, the four blocks as image views cut from the launch screen's own
  `LaunchLogo` image, the tagline as a `UILabel` in Jost Bold) animated only with `CAKeyframeAnimation` /
  `CABasicAnimation` (no per-frame main-thread drawing). It sits above the Compose view controller in
  `NativeSplashHostViewController`, the controller `MainViewController()` now returns (Swift unchanged).
- **One source for the timing:** the keyframes, easings and geometry live in `SplashTimeline` / `LexiCurves`
  (`core:designsystem`), used by both `LexiSplash` (Android, previews, tests) and the native overlay; durations come from
  `LexiMotion` (`StandardLexiMotion`). Only Compose's content-size → font-scale table is copied (internal in Compose).
- **Hand-off:** the overlay's first frames are the launch screen's pose (blocks at rest, no shapes, no tagline; same
  image, same centre). The animation starts after `LAUNCH_HAND_OFF_MILLIS` (350 ms): iOS cross-fades from the launch
  screen to the app's first frame over ≈ 330 ms (measured on the simulator), and blocks moving under that fade show the
  launch logo as a ghost. The hold is inside the same Core Animation timeline, so it does not depend on the main thread.
- **Lifecycle** (`IosLaunchSplash` + `NativeSplashGate`): the overlay leaves when BOTH its animation has ended (Core
  Animation's completion) AND Compose signals that the first real screen is drawn (`LaunchSplash.contentReady()`,
  called by `LexiNavHost` once the splash entry has left and 2 frames were drawn), or after a hard **3000 ms** cap
  (fail-safe, logged as `motion_failsafe` `kind=splash_timeout`). The exit is the old screen's half of #12 (−24 dp,
  fade 90 ms, `mechanical`; reduced motion: 150 ms crossfade), then the view is removed from the hierarchy.
- **Compose behind it:** with a native splash `SplashRoute` animates nothing; it draws the splash's resting pose
  statically and silently (seen only if the cap fires first), runs the S4 first-launch warm-up at once (same rules,
  same 2000 ms cap) and moves on as soon as content and warm-up are done; the switch to the first screen has no
  transition (hidden). Landing haptics and "tık" come from `NativeSplashFeedback` (app root) on the overlay's clock,
  through the gated `Haptics` / `SoundPlayer` (in-app settings respected).
- **Every launch** uses the native splash (identical animation); no extra hold besides the hand-off. Reduced motion:
  the static logo for `splashReducedHoldMillis`, then the crossfade.
- **Accessibility:** the overlay is one modal image element ("Lexi, <tagline>"), so VoiceOver neither announces the
  logo twice nor reaches the screen prepared under it; a screen-changed notification is posted when it goes. Light
  only (`overrideUserInterfaceStyle`).
- Android keeps the Compose splash (`NoLaunchSplash`).

Alternatives considered: more Compose warm-ups / longer holds (tried for months, `docs/PERF.md`); the overlay in Swift
(as first proposed): rejected because the timing constants would be duplicated in Swift and the project has no XCTest
target (a new target is a project change); in Kotlin the overlay uses the shared constants and is tested with the
existing iOS simulator tests. A Lottie / video splash: a new dependency and a second copy of the design.

## Consequences
- The splash on iOS is smooth whatever the main thread does; Compose's start-up and the first-launch warm-up are
  hidden behind it.
- A change to the splash design must keep `SplashTimeline` as the single source (both platforms follow it). The block
  images come from `LaunchLogo`: re-export it (`LaunchLogoExportIosTest`) after any logo change.
- Later iOS launches show the splash ≈ 350 ms longer (the hand-off); the first launch of a version is shorter (its
  warm-up runs under the animation instead of before it, the S4 hold of up to 2 s is gone).
- `transition_frames` no longer reports `splash` on iOS (Compose does not animate it).
- Not provable on the simulator: real Metal stalls and the render server keeping the animation smooth while the main
  thread is blocked; and the launch cross-fade's length on a device. Checked by the owner on an iPhone with a local
  Release build (`docs/LOCAL-DEVICE-TEST.md`).
