# Performance audit (2026-11-23, after the 1.1.2 freeze)

Scope: every screen and transition (Splash, Onboarding, Home, picker + dialog flip, letter morph, Game, Result,
Notebook, Stats, Exams, Settings, Duel, Daily, navigation). Method: code audit of every draw / layer / composition path,
Skia (iOS) behaviour checked in the Compose 1.12.1 sources, pixel tests on the iOS simulator's Skia renderer and on the
Android host renderer. Design values, durations, easings, haptics and reduced motion are unchanged by every fix below.

**Not measurable here:** real-iPhone frame times and Metal pipeline compiles (no device; the simulator and the host
tests render on the CPU). Android frame numbers come from the CI `perf` workflow (`docs/PERF.md` § Macrobenchmark);
it does not cover iOS. Items marked *measure* need an Instruments trace on an iPhone (fresh install).

## Findings, ranked by impact

| # | Impact | Where | Finding | Evidence | Status |
|---|---|---|---|---|---|
| 1 | **Critical** | grid entrance #18, board entrance #16, re-deal / clue swap #17, dialog rows, Lite dialog panel | `graphicsLayer { alpha; outsets }`: on iOS (Skia) the alpha is frozen at the parent's last recording, so fade-ins from 0 stayed invisible (1.1.2 freeze) | `OutsetsAlphaFreezeIosTest`, `CategoryPickerEntranceIosTest`, `BoardEntranceIosTest` (fail on 1.1.2, pass now); `docs/PERF.md` § 1.1.2 freeze | **fixed**: `fadeWithShadowRoom` |
| 2 | High | letter morph #16 | no upper bound on the wait for the game: a game that never got ready (or a marker never laid out) kept the ink up | `LetterMorphMachine` code; `LetterMorphMachineTest` | **fixed**: 3 s wait limit → cancel, game starts, `motion_failsafe` logged |
| 3 | High | game board (`dealtPiece`, `BoardEntrance.piece`) | every recomposition (key tap, turn) built new piece modifiers for all keys, slots and wheel chips while an entrance / re-deal object was alive (all session after OYNA): no skipping, every piece updated and re-recorded | code (new lambdas per call, element equality by identity) | **fixed**: one modifier per piece, cached (`rememberDealtPieces`, `BoardEntrance` cache) |
| 4 | High | morph ink + big letter (`LetterMorphLayer`) | alpha < 1 on two **screen-sized** layers: an offscreen buffer the size of the screen for one rectangle / one glyph during the letter's fade-in and the cancel fade | code | **fixed**: `CompositingStrategy.ModulateAlpha` (single primitive: same pixels, no buffer) |
| 5 | Med | first-use GPU work: morph letter at real size | the warm-up drew the big letter only small (≈ 100 px); near its real size (≈ 690 px on a 3x phone) Skia draws glyphs above ≈ 256 px as paths, another pipeline; the fading letter was not warmed either | Skia glyph atlas limit; `FlowWarmUp` | **fixed**: second pose at scale 1, alpha 0.5, in the hidden splash warm-up; *measure* |
| 6 | Med | `CategoryArt` / `ExamArt` | the drawing lambda was rebuilt on every recomposition, so `drawWithCache` re-parsed the SVG paths and re-measured text (grid reuse, flip start) | code (`remember` keyed on a new lambda) | **fixed**: `remember(kind, colors)` |
| 7 | Med | game motion warm-up gaps | #3 ring stroke, #5 pass shape + rotated arrow, #7 combo burst, the centre letter's cross-fade are not in `GameWarmUp` (drawn at rest only) | code | open, *measure* first (first correct / pass / combo of a fresh install in Instruments) |
| 8 | Med | dialog open / close | 1 content layer + 5 row layers fading inside the rotating panel: up to 6 offscreen buffers per frame | code | open, *measure* (merging the content alpha into the rows needs a visual check) |
| 9 | Med | picker / stats / notebook ViewModels | the UI list (`groupBy`, first letters) is built on the main thread while the 260 ms slide plays | code | open: move to `Dispatchers.Default` (`flowOn`), measure the gain |
| 10 | Low | grid entrance #18 | 8 card-sized offscreen buffers per frame for ≈ 0.6 s, once per session | inherent to the fade (ModulateAlpha would show the shadow through the card) | accepted (FrameMonitor drops to Lite on slow devices) |
| 11 | Low | locked exam cards | always alpha < 1 (offscreen while scrolling) | code | accepted, *measure* |
| 12 | Low | morph frame, pass motion, dialog rect, score count-up | small per-frame recomputation / allocation (3× frame per morph frame, a `Stroke` per frame, number re-measured per value) | code | accepted (cheap, short) |
| 13 | Low | nav fade-through #12 | two full-screen offscreen layers for 260 ms | inherent to the design | accepted |

**Clean:** Splash (layer-phase only), Home (drag in `graphicsLayer`), Onboarding, Result, Notebook (keyed `LazyColumn`),
Stats, Exams, Settings / About / Reminder, Duel setup / result / review, Daily (uses the game board: #3 applies), timer
and HUD (draw-phase fill, per-second `derivedStateOf`), feedback toasts, `PressableSurface`.

## Shader variety (S4, iOS Skia / Metal)

Searched every `clip`, `clipToBounds` / `clipRect`, offscreen layer (`alpha` < 1, `saveLayer`), `blur` /
`RenderEffect` (none) and gradient (one: onboarding's `ScrollCue`).

| Finding | Status |
|---|---|
| Every `lexiSurface` / `PressableSurface` clips its content to its shape (`clip(shape)` = a layer with a clip). Under a fractional transform (entrances, re-deal, flip, nav slide) Skia draws through an anti-aliased clip effect, a pipeline variant per kind of draw; the X block's leaf is a path clip (a coverage mask while it spins) | **not replaced**: dropping the clip changes edge anti-aliasing (round / leaf) or lets overflowing content show (rectangles), so the pixels are not provably identical. Warmed instead: the hidden warm-up now draws Home and onboarding mid screen-transition and the board mid-entrance (sub-pixel move, scale, `fadeWithShadowRoom`), the X turned (pose 0.28) |
| `ScrollCue` vertical gradient (onboarding) | warmed only when page 1 overflows (same condition as the real page); otherwise first drawn live (small, one gradient) |
| Locked category / exam art at alpha 0.5 (#11) | kept (overlapping shapes: `ModulateAlpha` would change pixels) |

Remaining *measure* items: #5 (now real size in the warm-up), #8 dialog open / close and the transitions above are
measured in the field by `transition_frames` (`dialog_open`, `dialog_close`, `morph`, `game_start`, `picker_open`;
`docs/PERF.md` § Field frame times); #7 (in-game motions) and #11 still need an Instruments trace on a fresh install.

## Recording the flip / dialog into a GraphicsLayer (S6 Part C): not done

Evaluated: record the card and the dialog content once (`rememberGraphicsLayer()` + `record`) and only transform the
recorded layer during #13–#15. Not implemented, because it would not be cheaper:
- On Skiko (iOS and the host renderer) a `GraphicsLayer` is a `RenderNode` that records a **display list** (Skia
  picture) and replays it on every draw (`SkiaGraphicsLayer.skiko.kt`); it is not a cached texture. `Modifier.graphicsLayer`
  already uses the same `GraphicsLayer`, and the flip writes only layer properties per frame (`rotationY`, `translationY`,
  `rotationZ`, `alpha` in the layer block): the content is already recorded once and replayed. A manually recorded layer
  would replay the same draw operations, so the GPU work per frame is the same.
- The per-frame re-recordings that remain are inherent: the dialog's clip rectangle and border follow `dialogRect`
  every frame (the panel's content is its own layer, so only a layer reference is re-recorded), and the card's lift
  shadow (`drawBehind`) is a single rectangle.
- A raster cache (draw the content into an offscreen texture once, then transform the texture) would resample it under
  the perspective turn: edges and text would not be the same pixels, which the design rules forbid.

## Rules kept from this audit

- Fades of content with hard shadows: `fadeWithShadowRoom`, never `graphicsLayer` outsets with a changing alpha
  (`motion.md` § Layers).
- Every motion ends at rest whatever happens to its frames (`motion.md` § Fail-safes).
- Per-frame values only in `graphicsLayer` / draw lambdas; modifiers built from them are created once and reused.

## Owner re-test (iPhone 13 and iPhone 17)

Fresh install (delete first), kill and reopen, Low Power on and off, and background → foreground during the picker
entrance and during the letter morph: the picker cards appear with their shadows, OYNA → letter morph → board lands
within the normal ≈ 1 s, the game starts; after background → foreground everything is at rest.
