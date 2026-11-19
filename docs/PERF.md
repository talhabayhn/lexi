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

The tour starts when Home is RESUMED, plus 1 s idle (the game warm-up now runs in the splash on a version's first launch). The refresh interval is the median frame gap of that idle second.

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
  NavHost during the two warm-up frames on Home; worst frames are now 19.7–23.7 ms (since the 1.1.1 report it is drawn
  in the splash's hidden warm-up instead, § iOS 1.1.1 report). On a phone the shader cache
  persists across launches, so this mainly helps the first launch after install or an update.
- **Earlier bug found by the journeys:** a fresh install opened "Pick your wheel" scrolled to the exam packs (the grid
  kept its first visible item when the cards were inserted above it); fixed with a regression test.
- **What an emulator cannot tell:** GPU and display timing (the host GPU, a 60 Hz virtual display without real
  vsync pressure), thermal throttling, a low-RAM device's memory pressure, and how a real panel shows the motion. It
  does tell: main-thread work per frame, first-use costs (shader compile, class init) and regressions between runs.
  Real-device runs (`:benchmark:connectedBenchmarkReleaseAndroidTest`) on an Android 8 low-RAM phone, a Samsung
  A-series and a Pixel are still open.
- **Debug builds:** two chips under the status bar (developer only) force the tier (auto / Full / Lite / Minimal) and
  show the current tier with its reason (e.g. `auto · Lite: slow transitions x2 · good 1/3`), and a frame-time graph
  (16.7 and 24 ms lines, P95, worst) to check the tiers by eye on a phone. Not in release-like builds: the strings
  `slow transitions x` / `frames on` must not appear in the minified APK's dex (§ Hand-test build).
- **Automatic tier (phase 6):** a cold start can no longer downgrade the session: the first transition of each key per
  process is a warm-up; Lite needs 2 slow transitions or one run of 5 slow frames, and 3 good transitions return to
  Full (`video-flow-notes.md` §12). When reading `VideoFlowBenchmark` numbers, note that each iteration is a fresh
  process, so its first flip / morph is never judged.

### iOS: Instruments (no macrobenchmark on iOS)

1. Xcode → Product → Profile (Release configuration) on a real iPhone (iPhone 8 / SE 2nd gen / 11 / current), not the
   simulator (it renders on the Mac's GPU).
2. Template **Animation Hitches**; record the same journeys by hand: Home → play card → "Çarkını seç", fling the grid,
   open a card's dialog, close, reopen, OYNA → game, a few letters.
3. Read the **Hitches** track: every hitch with its duration and type (commit / render); the bar is no hitch above
   one frame interval in the flip and the morph, and the hitch-time ratio below 5 ms/s ("good" in Apple's scale).
   **Time Profiler** in the same run shows what the main thread did in a hitch (Compose recomposition vs Skia draw).
4. Repeat with Low Power Mode on (still tier Full since the 1.1.1 report; Lite only after measured slow transitions)
   and with Reduce Motion on (tier Minimal: cross-fades).


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

### Hand-test build (optimised, non-debuggable, no secrets)

`benchmarkRelease` = the release build type (R8 + resource shrinking + the committed baseline profile) signed with the
debug keystore, no telemetry, no debug overlay (`src/nodebug`), not debuggable. Same applicationId as the store build, so
uninstall the store build first (`adb uninstall dev.talhabayhan.lexi`; its data goes with it).

```bash
adb devices
# only when the video flow changed since the profile was last recorded (≈ 6 min on an emulator, phone or emulator connected):
./gradlew :androidApp:generateBaselineProfile -Plexi.profileOnConnectedDevice \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR   # drop suppressErrors on a phone
adb uninstall dev.talhabayhan.lexi            # the store build or an older hand-test build
./gradlew :androidApp:installBenchmarkRelease
adb shell cmd package compile -m speed-profile -f dev.talhabayhan.lexi   # make sure the profile is compiled now
```

**Debug overlay not in release (phase 6, 2026-11-18):** the tier chip and frame graph (`MotionDebugOverlay`) are
referenced only from `androidApp/src/debug`; R8 removes them from minified builds. Check after `assembleRelease` /
`assembleBenchmarkRelease` (expect 0; the debug APK gives > 0):

```bash
for a in androidApp/build/outputs/apk/{release,benchmarkRelease}/*.apk; do
  echo "$a: $(unzip -p "$a" 'classes*.dex' | LC_ALL=C grep -a -c -E 'slow transitions x|frames on|MotionDebugOverlay')"
done
```
Result: release 0, benchmarkRelease 0, debug 14. `nonMinifiedRelease` (benchmark-only build type, no R8) still holds
the unreferenced classes (11); it is never shipped.

## iOS first launch after install (2026-11-18, TestFlight 1.1.0, iPhone 13)

Owner report: on the first launch after a fresh install the splash and the first category-card flip stutter a little;
after closing and reopening the app both are smooth; the letter morph → game is fine. So the cost is one-time per
install, not the animations. Checked in code (no device trace yet):

| Hypothesis | Verdict |
|---|---|
| content.db copy / user DB / DataStore on the main thread or in the splash | **No.** Installed and opened on `Dispatchers.IO` while the splash plays (`SplashRoute` → `ContentVersionRepository`, `Databases`); 1.7 MB, off-main. The user DB opens on the first progress read (IO), DataStore runs on an IO scope. |
| First queries (categories, progress) on the main thread | **No.** All repository reads are `withContext(io)`; the picker only maps the rows on main (a few ms, the same on every launch). |
| GPU shaders compiled on first draw | **Yes.** After an install Metal builds every pipeline the first time Skia draws it and the OS caches the result for the next launches, which is exactly "first launch only". The splash had no warm-up at all (its shaders were built inside its own animation); the Home `FlowWarmUp` drew only the card's art and text turned, not the card's own fill / border / shadows / lift under perspective, and the dialog warm-up (`PickerDialogWarmUp`) waits ≈ 1.1 s on the picker, so a quick first tap beat it. |
| Category art / fonts decoded on first draw | **No.** The art is vector `Path`s (no bitmaps); the Jost fonts are read in the first composition, before the splash clock starts. |
| Koin / lazy init during the splash | **Partly (iOS).** Koin resolves in the first composition (before the clock). But `IosSoundPlayer` read the sound files and created + prepared every `AVAudioPlayer` on the **main thread** right after the first frame, i.e. inside the splash (slowest on the first launch: audio set-up, cold files), and `IosHaptics` created a new feedback generator for each of the splash's four landing haptics. |

What changed (no design, duration, easing, tier or reduced-motion change; nothing debug-only):

- **Splash pre-roll** (`SplashRoute`): the logo is drawn at 4 of its own poses behind the splash's opaque cream for 2
  frames, while the visible splash still shows its plain-cream first frame; then the 1.2 s clock starts. Not with
  reduced motion. Test: `SplashWarmUpTest` (no landing during the pre-roll, landings timed from the clock's start).
- **Flip warm-up on Home**: `FlowWarmUp` also draws `CategoryFlipWarmUp` (feature:levels): the real `PickerCard`
  posed lifted and half-turned with the flip's own layer, plus the real dialog panel mid-open, in the existing two
  warm-up frames after Home is first resumed (common code: Android and iOS). Test: `CategoryFlipWarmUpTest`.
- **iOS sounds off the main thread** (`IosSoundPlayer`): the audio session and the players are set up on
  `Dispatchers.Default`; each ready player is published to the main thread (Tap first). Test: `IosSoundPlayerTest`
  (Mac only, `:core:platform:iosSimulatorArm64Test`; Linux CI does not run iosTest).
- **iOS haptics**: one generator per kind, kept; the light one is created and prepared when `IosHaptics` is built
  (first composition, before the splash clock starts).

**Re-test (owner, iPhone):** the effect only shows once per install, so: delete the app (long press → Remove App →
Delete App; this also drops Metal's cached pipelines), install the build from TestFlight, open it once and watch the
splash, finish the onboarding, Home → play card → tap a category card right away and watch the flip. Then close the app
from the app switcher, reopen and compare. For numbers, profile the same first launch with Instruments → Animation
Hitches on a Release build installed fresh from Xcode (delete the app before each run).

## iOS 1.1.1 report (2026-11-19, TestFlight 1.1.1, iPhone 13)

Owner report: (1) no card flip with Low Power Mode on; (2) hard shadows appear ≈ 250–300 ms after their element looks
opaque (PLAY button every time, category cards on the first picker open of a launch, the dialog frame in Lite);
(3) on the first launch after a fresh install the splash still stutters, in both power modes.

| Finding | Evidence | Fix |
|---|---|---|
| `MotionPolicy` mapped iOS Low Power Mode to Lite: no flip, only scale + fade | code; owner: the flip works once Low Power is off | Low Power Mode is no longer a tier signal (Full by default, measured jank only); `MotionPolicyTest` |
| A `graphicsLayer` with alpha < 1 is drawn offscreen **clipped to its own bounds** (Compose `GraphicsLayer.setOutsets` docs; skiko `SkiaGraphicsLayer.requiresLayer`). Hard shadows draw right of / below the bounds, so they were cut off for the whole fade and popped in at alpha 1: the dialog rows' fade (PLAY), the grid entrance (first open per launch), the dialog panel's Lite fade (Full only shows / hides, alpha 0 or 1) | reproduced on the Android host renderer (`FadingHardShadowTest`: fails before, passes after) and on the iOS simulator's Skia (`FadingHardShadowIosTest`: clipped without outsets, drawn with them) | every fading layer with hard shadows sets `outsets` (`hardShadowOutsets`, `LexiShadows.fadeOutsets`): dialog rows and Lite panel, grid entrance, the game board's entrance / re-deal / clue swap. Same pixels at rest, no timing change |
| (a) The splash pre-roll never reached the GPU: it was drawn **behind** the splash, whose first draw is a full-screen opaque cream rectangle. Skia's GPU backends (Metal on iOS, HWUI on Android) turn such a draw into a clear and drop every draw recorded before it in the frame (and each of the four poses began with its own full-screen cream). So no pipeline was built in advance | code (draw order + Skia's full-screen-clear optimisation); not measurable here (the host and simulator tests render on the CPU) | `HiddenPreDraw` (designsystem): the poses are drawn **above** the splash, clipped to the top half and covered by an opaque cream rectangle that does not span the screen (an ordinary draw; Skia has no occlusion culling). The frame stays pixel-identical (`SplashWarmUpTest`) |
| (b) Heavy main-thread work during the splash | checked: content DB and DataStore on IO, sounds prepared on `Dispatchers.Default`, haptics prepared before the clock, speech / reminders / string preload after the splash, Firebase configured before the first frame | none needed |
| (c) First-launch-only extended warm-up | not done in this step: (a) explains the stutter | later decided by the owner: § First launch of a version |

**Same defect in the Home / picker warm-ups:** `GameWarmUp`, `FlowWarmUp` + `CategoryFlipWarmUp` and the picker's
`PickerDialogWarmUp` were also drawn behind a full-screen cream background (NavHost, picker), so they very likely warmed
nothing either (which fits the "no measurable difference" of § Phase 5). Owner decision: move them into the splash's
hidden area and run every warm-up on the first launch of each version only (§ First launch of a version).

**Re-test (owner, iPhone 13):** Low Power Mode ON and OFF: Home → play card → tap a category: the card lifts and
flips in both; the PLAY button's and the dialog frame's right / bottom shadows fade in with them (no late pop); kill
the app, reopen, open the picker: the cards' shadows come in with the cards. Fresh install (delete the app first):
watch the first splash, then kill and reopen and compare.

## First launch of a version: one warm-up (owner decision after the 1.1.1 report)

- **What:** `SplashRoute` draws `AppWarmUp` in its covered top-half area (`HiddenPreDraw`, frame pixel-identical):
  the splash's 4 poses, the game board (`GameWarmUp`), the card art turned and the morph's ink (`FlowWarmUp`), the real
  category card mid-flip, at rest and mid-entrance (a fading layer with its shadow room), and the dialog mid-open
  (`CategoryFlipWarmUp` → `PickerDialogWarmUp`). 3 frames, before the 1.2 s clock starts.
- **When:** only when `Settings.warmedUpVersionCode` (DataStore key `warmed_up_version_code`, absent = 0, no migration)
  differs from the running `AppInfo.BUILD_NUMBER` (versionCode); the version is stored after it ran. Every other
  launch skips it. Never with reduced motion (nothing stored then). The decision waits at most 2 frames for the
  store's first read (the 2-frame pre-roll every launch paid in 1.1.1); a store not read by then counts as warmed (no
  wait, the next launch tries again). So later launches start the clock no later than 1.1.1 did.
- **Removed:** the NavHost's two warm-up frames after Home's first resume and the picker's idle dialog warm-up
  (`rememberDialogWarmUp`): both sat behind opaque backgrounds.
- **Why per version:** iOS keeps compiled Metal pipelines across launches until the app is updated or reinstalled; an
  update can also change the drawing. Android's shader cache behaves the same way.
- **Trade-off accepted (owner: all warm-ups on a version's first launch only):** the old Home warm-ups also composed
  and laid out the game board and dialog in every process (class init, text shaping, strings), which do not outlive the
  process. Later launches and reduced motion now get none of that, so the first game start or first card flip of a
  later launch may cost a little more main-thread work than in 1.1.1 (launch time itself is not slower). To check on a
  device: `VideoFlowBenchmark.firstDialogFlip` and a first game start on a second cold start, before / after. Debug
  builds log `warm-up ran` or `warm-up skipped: settings not read in time` in the startup trace.
- **Tests:** `SplashWarmUpTest` (first launch of a version warms up before the clock and stores the version; a later
  launch never shows it and its first landing is no later than before; an update warms up again; a store not read
  within 2 frames skips it, stores nothing and is no later than before; reduced motion skips it and stores nothing; the
  warm-up frame is pixel-identical and its cover inset), `SettingsRepositoryTest` (the key round-trips).
- **Re-test (owner):** after installing the new build, the first launch shows the static cream splash a few frames
  longer, then the splash, Home → picker, first card flip and OYNA → game should be smooth; kill and reopen: same, with
  no extra wait.

