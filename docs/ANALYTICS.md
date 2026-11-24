# Analytics events and crash reporting (v1)

Firebase Analytics + Crashlytics behind `AnalyticsTracker` / `CrashReporter` (`core:domain` `telemetry`; ADR 0006, 0023).
The event list is closed: a new event or parameter is added here first, then to the sealed `AnalyticsEvent`.

## Rules
- No personal data, no free text, no IDs of our own. Parameter values are enum codes or counts only.
- No advertising ID (Android `AD_ID` removed, iOS without IDFA support), ad-personalisation signals off, no ATT.
  Firebase's own defaults (app-instance ID, session and `first_open` events, device/OS/app version) stay.
- Audience 13+. Analytics is **opt-in** (KVKK): collection is off until the player ticks the unchecked checkbox on the last onboarding step or turns on Settings → Gizlilik → Kullanım istatistikleri (`Settings.analyticsConsent`, DataStore). Existing users start off. `ConsentGatedAnalyticsTracker` drops every event and keeps the SDK collection disabled without consent (Crashlytics always runs). Release builds only; debug builds never collect (see "Debug" below).
- Firebase names: snake_case, ≤ 40 chars; we stay clear of reserved names (`first_open`, `session_start`, `error`, …).

## Events
| Event | Parameters | Fired when (source) |
|---|---|---|
| `app_open` | – | Cold start, when the splash finishes (`shared` `SplashRoute`) |
| `onboarding_complete` | – | Onboarding ends: level picked, skipped, or the level-test result accepted (`OnboardingViewModel`, `LevelTestResultViewModel`) |
| `level_test_complete` | `level` | The placement test ends with a placement (`LevelTestViewModel`) |
| `game_start` | `mode`, `level` | A new wheel starts (not a restored one) (`GameViewModel`) |
| `game_finish` | `mode`, `level`, `correct`, `duration_s` | A wheel ends for any reason (all answered, time up, Hardcore wrong, forfeit) (`GameViewModel`) |
| `share` | `type` | The share sheet was shown: the result card (`ResultViewModel`, `type=result`) or Home's "Davet et" invite (`HomeViewModel`, `type=invite`) |
| `reminder_enabled` | – | The daily reminder was turned on: Settings switch with permission, or pre-prompt accepted and granted |
| `rating_prompt_requested` | – | The store rating API was invoked (`RequestInAppReview`, `GAME_RULES.md` §14); the OS decides whether the player sees it, so it says nothing about a rating |
| `joker_used` | `type` | A joker was applied (`GameViewModel`) |
| `duel_start` | `clock_s` | A hot-seat duel is loaded and its first handover card shows (`DuelViewModel`) |
| `duel_finish` | `result_type` | A hot-seat duel ends (`DuelViewModel`); never the players' names |
| `motion_failsafe` | `kind` | A motion fail-safe ended an animation early (`kind=morph_timeout`: the letter morph waited `LetterMorphMachine.WAIT_LIMIT_MILLIS` for the game and was cancelled, the game starts without it; `kind=splash_timeout`: the iOS native splash reached its 3 s limit before the first screen was ready, ADR 0028; `LexiNavHost`). No personal data |
| `transition_frames` | `transition`, `worst_frame`, `motion_tier`, `first_launch_of_version` | A measured transition ended: its worst frame, at most once per transition per app process (`TransitionTelemetry` on the session's `FrameMonitor`, `LexiNavHost`; `docs/PERF.md` § Field frame times). No personal data |
| `paywall_view` | `source` | Reserved (RevenueCat item) |
| `purchase` | `product`, `result` | Reserved (RevenueCat item) |

| Parameter | Type | Values |
|---|---|---|
| `mode` | string | `classic`, `practice`, `hardcore`, `daily`, `review` |
| `level` | string | `A1`, `A2`, `B1`, `B2`, `C1`, `NAT` (review: the level of the wheel's first card) |
| `correct` | int | correct answers of the wheel |
| `duration_s` | int | wall-clock seconds from the start to the end of the wheel |
| `type` | string | `joker_used`: `reveal_letter`, `remove_distractors`, `example`, `listen`; `share`: `result`, `invite` (an enum code, no new data type: still app interactions) |
| `clock_s` | int | the duel's clock per player in seconds: `90`, `150`, `240` |
| `result_type` | string | `win` (decided by the §8 rules), `draw`, `forfeit` |
| `transition` | string | `splash`, `onboarding_to_home`, `picker_open` (grid entrance), `dialog_open`, `dialog_close` (card flip), `morph` (letter morph expand), `game_start` (morph shrink onto the board) |
| `worst_frame` | string | longest frame of the transition: `le_16` (≤ 16.7 ms, one 60 Hz vsync), `le_24`, `le_33` (≤ 33.4 ms), `le_50`, `le_100`, `gt_100` |
| `motion_tier` | string | `full`, `lite`, `minimal` (the video flow's tier when the transition ended) |
| `first_launch_of_version` | int | `1` on the first launch of the running app version (the launch with the hidden warm-up), else `0` |

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
"Granular location and device data collection" is turned off by the owner, so approximate location is not declared (`docs/store`).
