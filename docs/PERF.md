# Perf tour

A launch-flag harness (`shared/.../perf`) that scripts the screen transitions and prints frame statistics. It is off
unless launched with the flag, works in release builds (Android: only via `adb shell`, see below), and uses the same
navigation functions as the real buttons (`LexiNavigator`).

## Run

Finish onboarding once first (the tour waits 60 s for Home, then aborts).

- Android: `adb shell am start -n dev.talhabayhan.lexi/.MainActivity --ez lexi.perfTour true [--ei lexi.perfTourLoops 5]`,
  then `adb logcat -s LexiPerf:I System.out:I | grep LexiPerf`.
- iOS: add the process arguments `-LexiPerfTour [-LexiPerfTourLoops 5]` to the Xcode scheme (Run → Arguments) or launch
  with `xcrun devicectl device process launch ... -LexiPerfTour`. Output goes to the console; the full JSON is also
  written to the app's `Documents/lexi-perf.json`.

The flag is ignored when the activity is recreated (rotation, language change). Default is 5 loops (Android clamps
`lexi.perfTourLoops` to 1–20). The Android launcher activity is exported, so a release build honours the extras only
when started from the shell (`adb shell am start`, referrer `com.android.shell`; a caller-set `Intent.EXTRA_REFERRER`
disables it); a debuggable build honours them from any caller. A release build on a real device is the baseline;
simulators and debug builds are not comparable.

## Script (per loop)

Home→Settings, back; Home→CategoryPicker ("Çarkını seç", A1); CategoryPicker→Game (A1, Practice: no clock, left unfinished so never recorded); stay 1.5 s;
Game→Settings, back to Game; Game→Home (abandoned, never recorded); Home→Stats, back; Home→Notebook, back.

The tour starts when Home is RESUMED, the one-time game warm-up (`GameWarmUp`, two frames) is over, plus 1 s idle. The refresh interval is the median frame gap of that idle second.

## Output

One line per segment (trigger until the destination is RESUMED, plus 400 ms):

`LexiPerf {"label":"Home→Settings","loop":0,"frames":…,"maxGapMs":…,"dropped":…,"long":…,"firstGapMs":…}`

- `dropped`: gaps > 1.5 × refresh interval; `long`: gaps > 50 ms.
- `firstGapMs`: the first gap after the trigger, i.e. the cost of the destination's first frame.
- Game segments include the preparation before the navigation (`GamePreparer`, at most 250 ms of idle frames).

At the end: `LexiPerf SUMMARY {…}` (per label: median and max of `maxGapMs`, sums of `dropped` and `long`; refresh
interval, platform, reduced-motion flag), then `LexiPerf DONE`. `LexiPerf ABORT …` means a trigger was dropped or a
destination never resumed. Lines go to `println` and the Kermit tag `LexiPerf`.

The gap math is `PerfStats.kt` (tested in `PerfStatsTest`).

## Macrobenchmark and generated baseline profile (ADR 0025)

`:benchmark` (test APK) drives the release-like `benchmarkRelease` build (debug-signed, telemetry off) from outside:

- `BaselineProfileGenerator`: `startup` (cold launch to Home; a fresh install first skips the onboarding, which picks
  A1) is also the startup profile; `screens` adds the perf tour (2 loops) to the baseline profile only. Output:
  `androidApp/src/release/generated/baselineProfiles/`, merged with the hand-written `androidApp/src/main/baseline-prof.txt`.
- `StartupBenchmark`: cold start, 10 runs with no AOT code (`None`) and with the profile. `timeToInitialDisplay` is the
  splash's first frame; `timeToFullDisplay` is the first screen after the splash (the app calls `reportFullyDrawn()` two
  frames after `SplashState.finished`, `MainActivityLanguage.LexiApp`), so it includes the designed ≈ 1.2 s splash.
- `ScreensBenchmark`: frame times over one perf tour (main screens + game), 3 runs with the profile. CI renders with a
  software GPU, so frame times there are far above a phone's; use them only to compare runs.
- `VideoFlowBenchmark` (branch `redesign/video-flow`, ADR 0027): `homeToCategories` (play card → "Pick your wheel",
  grid fling down / up, back), `categoryDialogFlip` (card lift + Y flip into the dialog, mode change, close by the
  arrow and by back), `playLetterMorph` (OYNA → letter morph → board entrance, three letters, two passes) and
  `fullVideoFlow` (all of it, with a close and reopen), 5 iterations each, a fresh process per iteration. Bar: P95
  < 16.6 ms and no frame > 100 ms on real devices (results: § Video flow).

Run on CI: Actions → **perf** → Run workflow (`commit` = commit the regenerated profile). The Summary page shows
initial and full display (median, min-max) and frame CPU time P50/P90/P99 plus overrun P90 per test; traces and JSON are in the `perf-results` artifact. The runner is an
emulator with a software GPU: compare runs with each other, not with the M8 target (< 1.5 s on a mid-range phone).

Locally (emulator or phone): `./gradlew :androidApp:generateBaselineProfile` (managed device `pixel6Api34`) and
`./gradlew :benchmark:pixel6Api34BenchmarkReleaseAndroidTest
-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark` (add
`...suppressErrors=EMULATOR` on an emulator); for a connected phone use `:benchmark:connectedBenchmarkReleaseAndroidTest`.

### First CI results (2026-11-12, perf runs 43088244259 and 43109920469)

- Cold start, splash first frame (TTID): median 0.94 s and 1.52 s on two runners, the same with and without the
  profile (the hand-written wildcards already cover the splash path). Time to Home after the splash (TTFD): 3.9 s with
  the profile, 4.2 s without.
- The traces show where the emulator time goes: one splash frame waits ≈ 1.6 s in `postAndWait` on the RenderThread
  (software GPU), frames of the tour are GPU-bound the same way, and runner noise is large (Crashlytics init in
  `bindApplication` 27–240 ms across iterations). So CI can show regressions and the profile's TTFD gain (≈ 0.3 s),
  but not the phone target or the 60 fps target.
- App-side costs seen on every run: `bindApplication` 180–500 ms (Firebase Crashlytics + sessions init), the first
  frame's composition and text layout ≈ 200 ms of CPU. Re-check both on a phone before optimising them.

## Video flow (phase 3, branch `redesign/video-flow`)

### Local emulator results (2026-11-17, `VideoFlowBenchmark`, 5 iterations, `CompilationMode.Partial`)

Android emulator API 35 (`sdk_gphone64_arm64`, Apple-silicon host, host GPU), `benchmarkRelease`,
`./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest -P…class=…VideoFlowBenchmark -P…suppressErrors=EMULATOR`.
Frame CPU time (`frameDurationCpuMs`, ms) and frame overrun (`frameOverrunMs`, ms; > 0 = missed its deadline):

| Journey | frames | P50 | P90 | P95 | P99 | max | overrun P95 / max | frames > 100 ms |
|---|---|---|---|---|---|---|---|---|
| homeToCategories | 699 | 2.6 | 12.1 | 17.6 | 18.6 | 23.7 | 1.7 / 9.4 | 0 |
| categoryDialogFlip | 1348 | 3.1 | 17.4 | 17.9 | 18.7 | 21.3 | 2.0 / 6.2 | 0 |
| playLetterMorph | 1540 | 2.9 | 17.1 | 17.8 | 18.6 | 19.7 | 1.9 / 3.9 | 0 |
| fullVideoFlow | 2020 | 2.9 | 8.8 | 17.5 | 18.5 | 19.9 | 1.6 / 5.4 | 0 |

CI (`perf` workflow on the branch, run 44080753974; Gradle managed device API 34 x86_64, **software GPU**
`swiftshader_indirect`): frame CPU P95 homeToCategories 188.7, categoryDialogFlip 154.6, playLetterMorph 164.8,
fullVideoFlow 158.0 ms, against 202.0 ms for the existing perf tour (`ScreensBenchmark`) in the same run. Every frame
there is GPU-bound in software rendering, so these only compare runs and screens with each other: the video flow is
not heavier than the existing screens; they say nothing about a phone.

- **Acceptance (P95 < 16.6 ms, no frame > 100 ms):** "no frame > 100 ms" is met; P95 is 17.5–17.9 ms, so the bar is
  **not met on this emulator**. The traces show why: the ≈ 17 ms frames are the RenderThread blocked in
  `eglSwapBuffers` → `dequeueBuffer` (14–15 ms, waiting for a free buffer from the emulator's compositor), not app
  work. The main thread's `Choreographer#doFrame` in the same runs: P50 0.5–0.7, P95 1.5–2.2, worst 12.6 ms. The app's
  per-frame work fits a 60 Hz frame many times over; whether the swap wait exists on a phone must be measured there.
- **Fixed in this phase:** the first iteration had one 163–187 ms frame per journey: the main thread waited
  (`postAndWait`) while the RenderThread compiled GPU shaders the first time a perspective-turned card or the morph's
  scaled letter was drawn (`AtlasTextOp` 127 ms on the emulator's GL translation). `FlowWarmUp` draws both behind the
  NavHost during the two warm-up frames on Home; worst frames are now 19.7–23.7 ms. On a phone the shader cache
  persists across launches, so this mainly helps the first launch after install or an update.
- **Earlier bug found by the journeys:** a fresh install opened "Pick your wheel" scrolled to the exam packs (the grid
  kept its first visible item when the cards were inserted above it); fixed with a regression test.
- **What an emulator cannot tell:** GPU and display timing (the host GPU, a 60 Hz virtual display without real
  vsync pressure), thermal throttling, a low-RAM device's memory pressure, and how a real panel shows the motion. It
  does tell: main-thread work per frame, first-use costs (shader compile, class init) and regressions between runs.
  Real-device runs (`:benchmark:connectedBenchmarkReleaseAndroidTest`) on an Android 8 low-RAM phone, a Samsung
  A-series and a Pixel are still open.
- **Debug builds:** two chips under the status bar (developer only) force the tier (auto / Full / Lite / Minimal) and
  show a frame-time graph (16.7 and 24 ms lines, P95, worst) to check the tiers by eye on a phone.

### iOS: Instruments (no macrobenchmark on iOS)

1. Xcode → Product → Profile (Release configuration) on a real iPhone (iPhone 8 / SE 2nd gen / 11 / current), not the
   simulator (it renders on the Mac's GPU).
2. Template **Animation Hitches**; record the same journeys by hand: Home → play card → "Çarkını seç", fling the grid,
   open a card's dialog, close, reopen, OYNA → game, a few letters.
3. Read the **Hitches** track: every hitch with its duration and type (commit / render); the bar is no hitch above
   one frame interval in the flip and the morph, and the hitch-time ratio below 5 ms/s ("good" in Apple's scale).
   **Time Profiler** in the same run shows what the main thread did in a hitch (Compose recomposition vs Skia draw).
4. Repeat with Low Power Mode on (the flow starts in tier Lite) and with Reduce Motion on (tier Minimal: cross-fades).


### Phase 5: first card flip (2026-11-18, debug build, emulator)

Question: does the first card flip of a session stutter more than the later ones, and does `PickerDialogWarmUp` (+ the
`videoFlow` baseline profile journey) remove it? Method: `installDebug` before (tracked changes stashed) and after,
fresh process, Home → play card → "Pick your wheel", 4 s idle, then 3 × (tap the Mixed card → 1.6 s → `dumpsys gfxinfo
framestats`, reset before each tap; frame time = FrameCompleted − IntendedVsync); three sessions each.
Worst frame of a flip (ms), flips 1 / 2 / 3: before 50 / 181 / 38, 279 / 35 / 249, 200 / 35 / 175; after 144 / 267 / 34,
51 / 163 / 301, 217 / 34 / 210. P50 17.5–18 ms in all (the emulator's buffer wait, see above).

**Result: no measurable difference, and no "first flip is worse" pattern.** Outliers of 150–300 ms occur on any flip in both
builds; the worst frame's breakdown is a 156 ms wait *before* the frame started (the main thread busy with the tap's
recomposition of the dialog and the first measure of its text), then 18 ms animation + 17 ms draw: main-thread work per
open on a debug build (JIT-interpreted Compose, no baseline profile, a debug overlay), not a shader compile. What this
setup **can** show: that nothing got worse and where a stall sits (main thread vs RenderThread). What it **cannot**:
the effect of a baseline profile (debug builds ignore it), shader-cache behaviour of a phone's GPU, or the P95 bar.
Next step on a device (release-like build, fresh install so the profile applies; compare no profile with the profile):

```bash
./gradlew :androidApp:generateBaselineProfile        # records the new videoFlow journey (managed device or emulator)
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.talhabayhan.lexi.benchmark.VideoFlowBenchmark#firstDialogFlip
# add -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR on an emulator
```
Compare `frameDurationCpuMs` P99 / max of `firstDialogFlip` with and without the warm-up commit and with
`CompilationMode.None()` vs the default. The warm-up costs one extra composition of the dialog on an idle picker (about
the cost of one flip's first frame, invisible behind the background); if a device shows no gain, remove
`rememberDialogWarmUp` (one call in `CategoryPickerScreen`) and keep the profile.
