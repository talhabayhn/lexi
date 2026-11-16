# ADR 0027: Video flow redesign — motion infrastructure and letter-morph approach

- Status: Proposed (branch `redesign/video-flow`; accepted only if the owner picks this flow over Home v2)
- Date: 2026-11-17

(0026 is taken by the Home v2 branch, `redesign/home-v2`; this ADR is numbered after it so both branches can merge.)

## Context

The owner approved a new app flow from a motion prototype (`docs/design/drafts/video-flow/`): Home with one play card
→ "Çarkını seç" (level chips + illustrated category grid + exam packs) → a category card flips into a mode/duration
dialog → OYNA expands into a full-screen ink square with the wheel's letter, which shrinks into the game's
current-letter marker while the board comes in. Spec: `docs/design/video-flow-notes.md`; motion: `motion.md`
#13–#17. It competes with Home v2 (`redesign/home-v2`, ADR 0026 there): only one of the two will ship, so this work
must be reversible and must not depend on Home v2.

Two technical questions: (1) can Compose Multiplatform's shared elements carry the OYNA → marker morph across
NavHost destinations on both Android and iOS; (2) how do we keep a ~1 s choreography smooth on old devices
(iPhone 8, Android 8 low-RAM).

## Decision

1. **Branch-only, reversible.** Everything lives on `redesign/video-flow` from `main`; nothing on `main` refers to it;
   phase 1 adds no screens. Home v2 stays the fallback branch.
2. **Spike result — shared elements work on both platforms.** `SharedTransitionLayout` +
   `Modifier.sharedElement` between two Navigation Compose destinations (Compose Multiplatform 1.12.1, navigation
   2.10.0-rc01) animates the element's bounds on the Android host (Robolectric) **and** on the iOS simulator, with
   identical interpolated bounds at mid-transition and an exact landing (`shared/src/commonTest/.../spike/`,
   `SharedElementSpikeAndroidTest`, `SharedElementSpikeIosTest`; kept as a regression guard).
3. **The letter morph uses the overlay approach anyway** (`MorphOverlayHost` in `core:designsystem`), because the
   morph is not a two-state bounds transform: it goes button → full screen → marker with a colour change, a hold,
   and a shrink that must **wait for the game to be ready** — a shared element is bound to the NavHost transition's
   fixed duration. Shared elements also re-measure the element every frame (approach layout: the spike's element
   changes width), which breaks the draw-phase-only rule below, and their per-platform cost is unmeasured on old
   devices. The overlay draws one full-screen layer scaled onto the current rectangle and one text layer scaled; per
   frame it only invalidates those two layers. Shared elements remain available for small, layout-cheap continuity
   later (e.g. a category illustration into a game header), each case to be benchmarked first.
4. **Card → dialog flip stays inside one screen**: `rotationY` + `cameraDistance` on the card and dialog layers, no
   navigation and no shared elements.
5. **Motion infrastructure in `core:designsystem` (`motion` package)**, no dependency on Home or any feature module:
   `FlowMotion` tokens on `LexiMotion.flow` (prototype values ÷ 0.86 through `MotionStep.fromPrototype`, so every
   number is traceable); `MotionTier` {Full, Lite, Minimal} + `MotionPolicy` (reduced motion ⇒ Minimal; Android
   `isLowRamDevice`, iOS Low Power Mode or a slow streak ⇒ Lite, sticky for the session); `FrameMonitor` (3
   consecutive frames > 24 ms during a registered transition), fed from Compose's frame clock (`withFrameNanos`:
   Choreographer on Android, CADisplayLink on iOS) only while a transition is registered, so no expect/actual and no
   idle frames; `MorphOverlayHost` + `MorphOverlayState` + `Modifier.morphAnchor(key)` (a modifier node writing
   window rectangles to `MorphAnchors`; newest owner wins so leaving/entering screens don't clobber each other).
   Pure logic (`MotionStep`, `LetterMorphTimeline`, `lerpRect`, sRGB `lerpSrgb`, policy, monitor) is unit-tested;
   the host has a frame-clock behaviour test and the layer a Roborazzi screenshot.
6. **Performance rules** (binding for phase 3): animate only in the draw phase (`graphicsLayer { }` / `drawBehind`
   lambdas), only translate/scale/rotate/alpha, no animated size/padding/blur; category illustrations as cached
   `Path`s; `LazyVerticalGrid` with keys; the game is prepared in the background during the morph. Acceptance:
   `FrameTimingMetric` P95 < 16.6 ms and no frame > 100 ms per journey (`VideoFlowBenchmark`, placeholders now),
   on iPhone 8 / SE2 / 11 / current and Android 8 low-RAM / Samsung A-series / Pixel.
7. **Classic duration options become 3 / 5 / 7 min** (`GameConfig.classicTimeLimitOptionsSeconds`), a stored
   120 / 240 s falls back to 180 s (`GAME_RULES.md` §5). Done in phase 1 because the dialog needs it and the
   fallback is the existing settings validation.

Alternatives considered: **Home v2** (one Home with level/mode/category; ADR 0026) — kept as the fallback branch;
**shared elements for the morph** — proven feasible, rejected for the reasons in 3; **an expect/actual
Choreographer / CADisplayLink frame monitor** — unnecessary, Compose's frame clock already wraps both;
**Compose `lerp(Color)`** for the red → ink change — mixes in Oklab and passes through a different middle colour
than the prototype's sRGB mix.

## Consequences

- No new library or module: `compose-ui-test` is added to `:shared`'s test source sets only (catalog entries).
- `LetterMorphLayer.png` was recorded locally; re-record on Linux (`screenshots-record`) before a merge to `main`.
- Phase 3 must call `MorphOverlayState.markReady()` once the game's data is loaded and its board composed, put
  `Modifier.morphAnchor` on the OYNA button and the wheel marker, and start the game clock only after the board
  entrance (`FlowMotion.boardSettledMillis` after the shrink start).
- If the owner picks Home v2, the branch is dropped; nothing on `main` needs reverting.
