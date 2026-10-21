# Analytics events and crash reporting (v1)

Firebase Analytics + Crashlytics behind `AnalyticsTracker` / `CrashReporter` (`core:domain` `telemetry`; ADR 0006, 0023).
The event list is closed: a new event or parameter is added here first, then to the sealed `AnalyticsEvent`.

## Rules
- No personal data, no free text, no IDs of our own. Parameter values are enum codes or counts only.
- No advertising ID (Android `AD_ID` removed, iOS without IDFA support), ad-personalisation signals off, no ATT.
  Firebase's own defaults (app-instance ID, session and `first_open` events, device/OS/app version) stay.
- Audience 13+. Release builds collect; debug builds don't (see "Debug" below).
- Firebase names: snake_case, ≤ 40 chars; we stay clear of reserved names (`first_open`, `session_start`, `error`, …).

## Events
| Event | Parameters | Fired when (source) |
|---|---|---|
| `app_open` | – | Cold start, when the splash finishes (`shared` `SplashRoute`) |
| `onboarding_complete` | – | Onboarding ends: level picked, skipped, or the level-test result accepted (`OnboardingViewModel`, `LevelTestResultViewModel`) |
| `level_test_complete` | `level` | The placement test ends with a placement (`LevelTestViewModel`) |
| `game_start` | `mode`, `level` | A new wheel starts (not a restored one) (`GameViewModel`) |
| `game_finish` | `mode`, `level`, `correct`, `duration_s` | A wheel ends for any reason (all answered, time up, Hardcore wrong, forfeit) (`GameViewModel`) |
| `share` | – | The share sheet was shown for the result card (`ResultViewModel`) |
| `reminder_enabled` | – | The daily reminder was turned on: Settings switch with permission, or pre-prompt accepted and granted |
| `joker_used` | `type` | A joker was applied (`GameViewModel`) |
| `paywall_view` | `source` | Reserved (RevenueCat item) |
| `purchase` | `product`, `result` | Reserved (RevenueCat item) |

| Parameter | Type | Values |
|---|---|---|
| `mode` | string | `classic`, `practice`, `hardcore`, `daily`, `review` |
| `level` | string | `A1`, `A2`, `B1`, `B2`, `C1`, `NAT` (review: the level of the wheel's first card) |
| `correct` | int | correct answers of the wheel |
| `duration_s` | int | wall-clock seconds from the start to the end of the wheel |
| `type` | string | `reveal_letter`, `remove_distractors`, `example`, `listen` |

## Crash reporting
- Uncaught crashes are reported by Crashlytics itself. On iOS an uncaught Kotlin exception is also recorded with its
  Kotlin stack (non-fatal model) right before the runtime terminates, so one Kotlin crash shows up twice in the
  console (that non-fatal + the SIGABRT crash); triage them as one.
- `CrashReporter.recordException(throwable, message)` for caught, unexpected failures the app recovers from (content or
  user database reads/writes that fail, a game that can't be recorded). `message` is a fixed English description of what
  failed (it becomes a Crashlytics log line); never user input.

## Debug
- Android debug: off. `./gradlew :androidApp:installDebug -Plexi.telemetry=true` turns collection on; DebugView:
  `adb shell setprop debug.firebase.analytics.app dev.talhabayhan.lexi` (`.none.` to stop).
- iOS Debug: off. Scheme → Run → Arguments: `-LexiTelemetry` turns collection on, `-FIRDebugEnabled` sends to DebugView.
- Tests use `FakeAnalyticsTracker` / `FakeCrashReporter` (`core:testing`).

## Firebase console settings (owner)
Data retention 2 months; Google signals off; no Google Ads / AdMob linking until ads ship; ads personalisation off;
"Granular location and device data collection" off if Play/Apple should not list approximate location (`docs/store`).
