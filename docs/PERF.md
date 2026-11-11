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

Home→Settings, back; Home→LevelSelect; LevelSelect→Game (A1, Practice: no clock, left unfinished so never recorded); stay 1.5 s;
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

Run on CI: Actions → **perf** → Run workflow (`commit` = commit the regenerated profile). The Summary page shows
initial and full display (median, min-max) and frame CPU time P50/P90/P99 plus overrun P90 per test; traces and JSON are in the `perf-results` artifact. The runner is an
emulator with a software GPU: compare runs with each other, not with the M8 target (< 1.5 s on a mid-range phone).

Locally (emulator or phone): `./gradlew :androidApp:generateBaselineProfile` (managed device `pixel6Api34`) and
`./gradlew :benchmark:pixel6Api34BenchmarkReleaseAndroidTest
-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark` (add
`...suppressErrors=EMULATOR` on an emulator); for a connected phone use `:benchmark:connectedBenchmarkReleaseAndroidTest`.
