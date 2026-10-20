# Perf tour

A launch-flag harness (`shared/.../perf`) that scripts the screen transitions and prints frame statistics. It is off
unless launched with the flag, works in release builds, and uses the same navigation functions as the real buttons
(`LexiNavigator`).

## Run

Finish onboarding once first (the tour waits 60 s for Home, then aborts).

- Android: `adb shell am start -n dev.talhabayhan.lexi/.MainActivity --ez lexi.perfTour true [--ei lexi.perfTourLoops 5]`,
  then `adb logcat -s LexiPerf:I System.out:I | grep LexiPerf`.
- iOS: add the process arguments `-LexiPerfTour [-LexiPerfTourLoops 5]` to the Xcode scheme (Run → Arguments) or launch
  with `xcrun devicectl device process launch ... -LexiPerfTour`. Output goes to the console; the full JSON is also
  written to the app's `Documents/lexi-perf.json`.

The flag is ignored when the activity is recreated (rotation, language change). Default is 5 loops. A release build on a real device is the baseline; simulators and debug builds are not comparable.

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
