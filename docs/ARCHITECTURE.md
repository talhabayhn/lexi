# Lexi — architecture

## 1. Principles
1. **One UI, one engine.** Compose Multiplatform renders every screen on both platforms; the game
   rules live in a pure-Kotlin engine that knows nothing about UI, storage or network.
2. **Deterministic, event-driven game.** A wheel is generated from a seed; a match is a list of
   events. This one decision powers solo play, daily wheel, same-device duel, online rooms,
   async challenges and replays with the same code.
3. **Offline first.** Everything in v1.0 works with no network. The network adds things (ads, IAP,
   crash reports, online rooms) but is never required to play.
4. **Unidirectional data flow (MVI).** `Intent → ViewModel → UiState (StateFlow) + Effects (Flow)`.
5. **Boundaries are enforced by modules**, not by discipline.

## 2. Modules

```
lexi/
├── build-logic/                 Gradle convention plugins (kmp-library, kmp-compose, android-app, feature)
├── androidApp/                  Thin Android shell: MainActivity, manifest, launcher icons (com.android.application)
├── shared/                      KMP umbrella: App(), navigation graph, Koin start, iOS framework export (`Shared`)
├── iosApp/                      Xcode project (Swift entry + native SDK bridges)
├── core/
│   ├── domain/                  PURE KOTLIN: models, repository interfaces, use cases, GameEngine, SRS, RNG
│   ├── data/                    SQLDelight DBs, repositories, DataStore, content loader, remote packs (later)
│   ├── designsystem/            LexiTheme, tokens, Bauhaus components, motion, haptics/sound locals
│   ├── platform/                implementations: TTS, haptics, sound player, ads, purchases, analytics, share
│   └── testing/                 fakes, test rules, fixtures (test-only dependency)
├── feature/
│   ├── onboarding/              onboarding + level choice
│   ├── home/                    main menu, daily wheel card, streak
│   ├── levels/                  level & mode select, categories
│   ├── game/                    game screen, joker sheet, pause, result, duel (hot-seat), level test
│   ├── notebook/                Kelime Defterim, review wheel entry, word detail
│   ├── stats/
│   ├── exams/                   exam packs
│   ├── settings/                settings + Pro/paywall
│   └── multiplayer/             (v1.1) rooms, lobby, online match
└── tools/
    ├── content-compiler/        JVM CLI: content/*.csv → validated content.db
    └── design-export/           script that renders design sources to PNG/HTML
```

### Dependency rules
| Module | May depend on |
|---|---|
| `core:domain` | kotlinx-coroutines-core, kotlinx-datetime, kotlinx-serialization — nothing else |
| `core:data` | `core:domain`, SQLDelight, DataStore, Ktor/Supabase (v1.1) |
| `core:designsystem` | Compose, `core:domain` (e.g. the `Haptics`/`SoundPlayer` interfaces) |
| `core:platform` | `core:domain` (implementations only; the interfaces are declared in domain) |
| `feature:*` | `core:domain`, `core:designsystem`, `core:platform` (interfaces) — **never** `core:data` |
| `shared` | everything (wires DI) |
| `androidApp` | `shared` only |
| `tools:content-compiler` | `core:domain` (JVM target), SQLDelight + JVM sqlite driver, kotlinx-serialization-json — **not** `core:data` (it generates its own writer from core:data's content schema files) |

The table is enforced by `verifyModuleDependencies` (part of `check`, rules in
`build-logic/.../ModuleRules.kt`); `core:testing` is allowed only in test configurations.
`core:domain` and `core:testing` are pure KMP (`jvm` + iOS, no Android plugin); Android targets consume their `jvm`
variant.

Features talk to data only through domain interfaces injected by Koin. A feature never depends on another feature;
cross-feature navigation goes through route objects defined in `shared` (or a tiny `core:navigation` if it grows).

## 3. Layers inside a feature

```
feature/game/
  GameRoute.kt          @Serializable route + NavGraphBuilder extension
  GameScreen.kt         stateless composable: (state, onIntent) -> Unit   ← previews & screenshots use this
  GameViewModel.kt      holds GameSession + ticker; publishes GameUiState, GameClockUi, GameEffect
  GameSession.kt        engine + current state + applied-event log (SavedGame)
  GameUiMapper.kt       pure engine state → GameUiState / GameClockUi
  GameContract.kt       GameUiState, GameIntent, GameEffect
  components/           screen-specific composables (GameColumn, AnswerSlots, KeyGrid, GameMotion, ...)
  di/GameModule.kt      Koin module
```

```kotlin
// Contract shape used by every screen
data class GameUiState(...)                        // immutable, @Immutable
sealed interface GameIntent { data class KeyTapped(val index: Int) : GameIntent; data object Pass : GameIntent; ... }
sealed interface GameEffect { data class Haptic(val kind: HapticKind) : GameEffect; data class Speak(val word: String) : GameEffect; data object NavigateToResult : GameEffect }

class GameViewModel(...) : ViewModel() {
    val state: StateFlow<GameUiState>
    val effects: Flow<GameEffect>                  // Channel(BUFFERED).receiveAsFlow()
    fun onIntent(intent: GameIntent)
}
```

## 4. GameEngine (core:domain)

```
WheelGenerator.generate(pool, level, category, mode, random, recentlySeen) ─▶ Wheel (26 slots: letter, rule, card)
WheelGenerator.review(dueCards, random)                                    ─▶ Wheel (letters with due cards)
                                                          │
GameEvent stream ─▶ GameEngine(config).reduce(state, event) ─▶ Transition(GameState, List<EngineEffect>)
   Start, KeyTapped(i), Backspace, Pass, UseJoker(type), Tick(elapsedMs), Pause, Resume, Forfeit
   (submit is automatic when all cells are filled; time up comes from Tick)
MatchEvent stream ─▶ MatchEngine(config).reduce(match, event) ─▶ Transition(MatchState, List<EngineEffect>)
   BeginTurn(timestampMs), Play(gameEvent)
```
- `reduce` is a **pure function**. The ViewModel owns the coroutine ticker and an injected `TimeSource`;
  it converts real time into `Tick` deltas. Events that don't apply are ignored (same state, no effects).
  Game screen runtime (100 ms ticker, auto-pause on background, process-death restore, effects → motion): ADR 0019.
  Wall time only through `AppClock` (`core/domain/.../time`; `SystemAppClock` in `core:platform`, bound in
  `shared`). Process death: `SavedGame` + `SavedGameCodec` (initial slots as card ids + jokers + coalesced events,
  `restore` replays them). A finished game is stored by the `FinishGame` use case (session + SRS, once per instance;
  Koin `factory`).
- Randomness: `SeededRandom` (SplitMix64, own implementation — do not rely on `kotlin.random` internals) derived
  from `seed`. Sub-seeds per purpose: `Seeds.derive(seed, purpose)` = `mix64(seed xor fnv1a64(purpose))`
  (`GAME_RULES.md` §13: `wheel`, `keys:<letter>`, `level-test:<index>`, `player:<n>`).
- Seeds: solo = random 64-bit; daily = `Seeds.mix64(Seeds.stableHash("daily:" + epochDay + ":" + contentVersion))`;
  challenge/online = carried in link/room.
- `MatchState` wraps one `GameState` per player + turn info → hot-seat and online duel reuse it.
- `EngineEffect` (LetterPlaced, Correct, Wrong, Passed, Advanced, ComboReached, TimeWarning, JokerApplied,
  TurnChanged, GameOver, MatchOver) are mapped by the ViewModel to sound/haptic/animation effects. The engine never
  plays sounds.
- Learning on top of the engine, also pure: `Srs` (Leitner notebook), `LevelTest` (onboarding staircase) + `LevelTestRunner` (one one-slot game per card, shared clock), `Stats`.
  Days come in as epoch days in the player's time zone.
- `GameConfig` holds every ⚙ number; events, effects and states are `@Serializable` (golden tests:
  `GoldenTest` + `GoldenSnapshots.kt`, seed + event log ⇒ exact JSON).
- Full rules: `GAME_RULES.md`.

## 5. Match transport (multiplayer-ready from day one)
```kotlin
interface MatchTransport {
    val incoming: Flow<MatchEvent>                 // events from other participants
    suspend fun send(event: MatchEvent)
}
// v1.0: LocalTransport (solo, hot-seat)  v1.1: SupabaseRealtimeTransport  v1.2: ChallengeLinkTransport
```
All participants apply the same ordered event log → same state. See `MULTIPLAYER.md`.

## 6. Data

### Two SQLite databases (SQLDelight)
| DB | Content | Writable | Source |
|---|---|---|---|
| `content.db` | cards, clues, categories, exam packs, meta(version) | no | generated by every build (`generateContentDb`), bundled as shared's Compose resource `files/content.db`, copied to app storage on first launch / version change |
| `user.db` | SRS progress, sessions, daily results, streak, joker inventory + gift bookkeeping | yes | created on device; v1 in `.sq`, later changes as `.sqm` migrations in `core/data/src/commonMain/sqldelight/migrations` |

The content schema lives once, in `core/data/src/commonMain/sqldelight-content` (`ContentDatabase`).
`tools:content-compiler` generates its writer from those same `.sq` files (plus its own INSERTs), so the compiler
and the app always agree on the schema without a module dependency; the app's read queries live in
`sqldelight-content-reads`. Schema version = `PRAGMA user_version`.
Exam-pack extra cards have `card.pack_only = 1`: `CardRepository` level/category wheel queries must filter
`pack_only = 0`; those cards are served only through `ExamPackRepository` (`CONTENT.md` §1, §7).

Lifecycle (ADR 0018): `Databases` opens both lazily on the injected IO dispatcher. Content: if the bundled
`files/content-version.txt` differs from the installed copy's `meta.contentVersion` (or there is no readable copy),
the bundle is copied (temp file, stale `-wal`/`-shm`/`-journal` removed, atomic rename), then opened with
`PRAGMA query_only = 1`. Platform paths and drivers come from `DatabaseFiles` (Android: `getDatabasePath`; iOS:
`Application Support/databases`), the bundle from `BundledContentDb` (implemented in `shared` with `Res`).

Keeping content separate means content updates never touch user data; `card.id` is a stable slug
(`a1-kitchen`) so progress survives content updates.

Settings: DataStore Preferences (`SettingsRepository` exposes `Flow<Settings>`).

### Repositories (interfaces in domain)
`CardRepository`, `CategoryRepository`, `ExamPackRepository`, `ProgressRepository` (SRS),
`SessionRepository`, `DailyRepository`, `StreakRepository`, `JokerRepository`, `SettingsRepository`,
`EntitlementRepository` (Pro, M7 with RevenueCat), `ContentVersionRepository`.

Interfaces: `core/domain/.../repository`; implementations (internal) and `dataModule` (Koin): `core:data`.
Content reads are one-shot `suspend` functions (content never changes while the app runs); user data is observed as
`Flow` (`ProgressRepository.observeAll`, `SessionRepository.observeRecords`, `StreakRepository.observeDays`,
`DailyRepository.observe`, `JokerRepository.observeInventory`, `SettingsRepository.settings`). All DB and file work
runs on the dispatcher qualified `IoDispatcher`, including the mapping of rows to domain objects (`flowOn(io)`), so
screens never pay for it on the main thread while they slide in. Progress of card ids missing from the installed content is kept and
filtered out of every list. `SessionRepository.record` skips `GameConfig.srsExcludedModes` (hot-seat duel) and marks
the streak day of a completed wheel in the same transaction. `shared` provides `GameConfig`, `BundledContentDb` and
the platform `DatabaseFiles` (`rememberPlatformModule()`, Android needs the `Context`).

## 7. Platform services (expect/actual or interface + platform impl)
| Interface | Android | iOS |
|---|---|---|
| `Speaker` (TTS) | `TextToSpeech` | `AVSpeechSynthesizer` |
| `Haptics` | `View.performHapticFeedback` / `VibrationEffect` | `UIImpactFeedbackGenerator`, `UINotificationFeedbackGenerator` |
| `SoundPlayer` | `SoundPool` | `AVAudioPlayer` (preloaded) |
| `AdsService` | AdMob rewarded + UMP consent | AdMob iOS SDK via Swift bridge |
| `PurchaseService` | RevenueCat KMP | RevenueCat KMP |
| `AnalyticsTracker`, `CrashReporter` | Firebase Android (`core:platform`) | Firebase iOS (SPM) in Swift: `FirebaseTelemetry` implements `IosTelemetry` (`shared` iosMain); ADR 0023 |
| `ShareService` | share sheet with PNG | `UIActivityViewController` |
| `ReminderScheduler` | AlarmManager inexact + receivers (boot/time/tz/package replaced) | `UNUserNotificationCenter` calendar triggers; ADR 0021 |

Swift-implemented services are injected into Koin at startup through a `PlatformServices` holder
created in Swift and passed to `MainViewController(services)`.

## 8. Navigation
Navigation Compose (JetBrains multiplatform artifact, 2.10.0-rc01 pinned until 2.10.0 is stable; ADR 0016), type-safe
`@Serializable` routes. Each feature exposes its route + `NavGraphBuilder.xxxScreen()`; the graph is `LexiNavHost` in
`shared`. Route arguments are primitives/strings (enums as their `name`): navigation 2.10 has no enum nav type on iOS.
ViewModels that need arguments get the route from Koin (`get<SavedStateHandle>().toRoute<T>()` in the module), so
their tests construct it directly. The graph today is `Splash → Home → LevelSelect [→ Categories] → Game → Result`; the result pops
everything above Home (back returns Home, TEKRAR OYNA starts the next game from there); its route carries the
finished game's summary (ADR 0019 addendum). `core:domain` keeps its own small `civilDateOfEpochDay` (day/month of the home daily card) instead of adding kotlinx-datetime to the pure module. Splash (`shared/splash`) plays the logo animation while the content DB is
installed/version-checked and `GrantDailyJokers` gives today's free jokers (`GAME_RULES.md` §7), then pops itself. Flows:
`Splash → Onboarding? → Home`; `Onboarding → LevelTest → LevelTestResult → Home` (the result replaces the test;
"Pes et" and back from the result pop to the onboarding's level choice, nothing saved); `Home → LevelSelect [→ Categories] → Game → Result`; `Home → Notebook | Stats | Exams | Settings | Daily`;
`Game → JokerSheet (bottom sheet) | Pause (dialog)`; `Settings → Paywall`.
Screen ↔ design map: `docs/design/screen-map.md`.

## 9. Design system
`LexiTheme` provides `LexiColors`, `LexiTypography`, `LexiDimens`, `LexiBorders`, `LexiShapes`, `LexiShadows` and
`LexiMotion` via CompositionLocals (read as `LexiTheme.colors`, `LexiTheme.typography`, …); Material3 `MaterialTheme`
is configured underneath (light scheme, Jost, square corners) so stock components don't leak wrong colors or shapes.
`LexiMotion.reducedMotion` follows the OS setting (Android animator scale 0, iOS Reduce Motion) unless `LexiTheme`
gets an explicit value. `Haptics`/`SoundPlayer` (pure interfaces in `core:domain`, package `feedback`) reach components through
`LocalHaptics`/`LocalSoundPlayer`: no-op by default, the platform implementations are provided at the root in M4.
Tokens and components: `docs/design/tokens.md`, `docs/design/components.md`.

## 10. Errors & logging
- Domain returns `Result`-like sealed types for expected failures (no exceptions for control flow).
- `Logger` interface (Kermit) — no `println`.
- Unexpected exceptions → `CrashReporter`. Never crash on content issues: skip a broken card and report.

## 11. Testing strategy
| Layer | Tool | Target |
|---|---|---|
| Engine, SRS, generators | kotlin.test (+ property-style loops) on `jvmTest` | ≥ 90% line coverage |
| ViewModels | kotlin.test + Turbine + fakes from `core:testing` | every intent |
| Repositories | SQLDelight in-memory JVM driver | queries & migrations |
| UI | Roborazzi (Android/JVM) screenshots vs design PNGs | every designed screen |
| UI behaviour | Compose `runComposeUiTest` on Robolectric (ADR 0017) | design-system interactions |
| Content | content-compiler validation | 100% of cards |

## 12. Build & CI
- Version catalog `gradle/libs.versions.toml`; latest stable versions at bootstrap, recorded there.
- Convention plugins in `build-logic`.
- GitHub Actions (ADR 0012): `build.yml` on ubuntu for every push to `main` (`./gradlew build :androidApp:assembleDebug`:
  lint, tests, Android assemble, iOS klib compile); `ios.yml` on macOS, path-filtered (framework link +
  `iosSimulatorArm64Test` per push, `xcodebuild` of `iosApp` + simulator launch screenshot artifact nightly or when
  `iosApp/` changes).
- Secrets (RevenueCat, AdMob, Supabase) via `local.properties` / CI secrets → BuildConfig (BuildKonfig). Never committed.
  Firebase client config (`androidApp/google-services.json`, `iosApp/iosApp/GoogleService-Info.plist`) is committed: it
  is not secret; its API keys are restricted to the app ids in Google Cloud (ADR 0023).

## 13. Security & privacy
- KVKK/GDPR: UMP consent before ads; Firebase Analytics/Crashlytics run in release without a consent form until UMP ships
  with AdMob (no ad ID, ADR 0023); privacy policy URL in settings.
- No account in v1.0. v1.1 anonymous auth; nickname only.
- Online answers: clients validate locally; room events signed by Supabase auth; cheating in private
  friend rooms is accepted risk (documented in `MULTIPLAYER.md`).
