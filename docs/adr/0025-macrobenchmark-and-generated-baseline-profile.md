# ADR 0025: Macrobenchmark module and a generated baseline profile, measured on a CI emulator

- Status: Accepted
- Date: 2026-11-11

## Context
M8 asks for a cold start < 1.5 s on a mid-range Android phone and a 60 fps game screen. The perf tour (`docs/PERF.md`)
measures frames but needs a device, and no device or Mac is available to the team today. The shipped baseline profile
is a 20-line hand-written wildcard file (`androidApp/src/main/baseline-prof.txt`), so the startup path and the first
screen transitions are compiled ahead of time only where a person guessed them.

## Decision
- **New module `:benchmark`** (`com.android.test`, target `:androidApp`, no project dependencies, built-in Kotlin) with
  `androidx.benchmark:benchmark-macro-junit4` **1.5.0** and `androidx.test.uiautomator:uiautomator` **2.4.1** (test
  APK only, never shipped). It holds:
  - `BaselineProfileGenerator`: `startup` (cold launch to Home, with the onboarding Skip, which picks A1 on a fresh install) is
    also the startup profile; `screens` adds the perf tour (`lexi.perfTour`, 2 loops) so Home, Settings, LevelSelect,
    Game, Stats and Notebook are in the baseline profile only.
  - `StartupBenchmark`: cold startup (`startActivityAndWait`, Home drawn) with `CompilationMode.None` and
    `CompilationMode.Partial(BaselineProfile)`, 10 iterations each, `StartupTimingMetric` + `FrameTimingMetric`; the
    app reports fully drawn when the first screen after the splash has drawn (`timeToFullDisplay`).
  - `ScreensBenchmark`: `FrameTimingMetric` over one perf tour, 3 iterations.
- **`androidx.baselineprofile` Gradle plugin 1.5.0** on `:androidApp` and `:benchmark`; generation only on request
  (`automaticGenerationDuringBuild = false`), the result committed under `androidApp/src/release/generated/`
  (merged with the hand-written file). `dexLayoutOptimization` (R8 startup dex layout) stays off until the first
  generated profile is committed and measured. `androidx.profileinstaller` 1.4.1 is declared explicitly (it was transitive).
- The plugin's `benchmarkRelease` / `nonMinifiedRelease` build types are signed with the debug key and have telemetry
  off, so CI emulators never send Analytics or Crashlytics data.
- **Gradle managed device** `pixel6Api34` (AOSP image, x86_64) in a manual workflow `perf.yml` on `ubuntu-latest`
  with KVM: generates the profile, runs the startup benchmark, uploads the JSON results, and with `commit=true`
  commits the regenerated profile to `main` (owner identity, like `screenshots-record`).

## Consequences
- Emulator numbers are not phone numbers: they show the trend and the gain of the profile, not the M8 target. The
  target is still checked on a real mid-range phone when one is available.
- `./gradlew build` builds two more app variants (R8 once more) and the test APK; no emulator is needed for it.
- No store declaration change: nothing in the shipped APK collects or sends data; profileinstaller was already in it.
