# ADR 0019: Game screen runtime — timer, lifecycle, state/effects, animation, restore

- Status: Accepted
- Date: 2026-10-15

## Context
M4 wires `04-game` to the pure `GameEngine` (ADR 0002). Open points: who owns time, how the screen reacts to
background/rotation/process death, how engine state and `EngineEffect`s reach Compose without recomposition storms,
how motion (`motion.md`) is driven, where the event log lives and how a finished game is stored. `feature:game` is an
empty module; `LetterWheel`/`WheelLetter`/`GameHud` already exist in `core:designsystem`.

## Decision
**1. Time.** The engine never reads a clock; `GameViewModel` owns it.
- Monotonic time: inject `kotlin.time.TimeSource.WithComparableMarks` (prod `TimeSource.Monotonic`, tests
  `testScheduler.timeSource`). Wall time: new domain interface `AppClock { fun nowMs(): Long; fun epochDay(atMs: Long):
  Long }` (`core/domain/.../time`), impl `SystemAppClock` in `core:platform` (kotlin.time `Clock.System` +
  kotlinx-datetime zone), `FakeAppClock` in `core:testing` (reused by SRS/daily tests in M5/M6). Used only for
  `Start.timestampMs` and the session record.
- Ticker: one `viewModelScope` coroutine, running only while `status == PLAYING && !isPaused && foreground`. Every
  `TICK_INTERVAL = 100.milliseconds` (VM constant, technical, not a rule) it dispatches `Tick(elapsedMs)` measured
  from the last mark; the mark advances by exactly the dispatched whole ms, so there is no drift. 100 ms keeps the
  1200 ms reveal and the timer bar precise; the engine is interval-agnostic, so golden tests are unaffected.
- `Start` is dispatched once the wheel is loaded and the screen is first RESUMED.
- Lifecycle: `GameScreen` forwards `LifecycleEventEffect(ON_PAUSE/ON_RESUME)` (lifecycle-runtime-compose) as
  `GameIntent.Background` / `GameIntent.Foreground`. Background ⇒ flush a final `Tick` for the time since the last
  tick, stop the ticker, dispatch `Pause`. ON_PAUSE covers screen lock, incoming call, app switcher, iOS
  `willResignActive`/Control Centre. Foreground ⇒ nothing is charged for the time away; the game **stays paused**
  and the pause dialog is shown until the player taps resume (`Resume`, new mark). The joker sheet does not pause
  (`GAME_RULES.md` §5).
- Configuration change: the ViewModel survives. Android `MainActivity` declares `android:configChanges=
  "orientation|screenSize|screenLayout|smallestScreenSize|keyboard|keyboardHidden|navigation|uiMode|density"` so
  rotation/dark-mode toggles don't recreate the activity (and don't auto-pause). Any recreation that still happens
  (locale, font scale) just shows the pause dialog.
- Process death (Android only; iOS restarts at Splash): `SavedStateHandle` (Koin-injected; route args via
  `toRoute<GameRoute>()`) holds one JSON string `SavedGame(seed, slots = (letter, rule, cardId?, initial state),
  jokers, events)` encoded with kotlinx-serialization-json (same codec as golden tests), consecutive `Tick`s
  coalesced into one. Restore = `CardRepository.cards(ids)` → rebuild the wheel → replay events (effects discarded)
  → append `Pause`. If a card is missing (content changed) the game is abandoned and the player returns to level
  select. Finished games are not saved. Coalescing requires a new engine test: `Tick(a)` then `Tick(b)` yields the
  same state as `Tick(a + b)`.

**2. State flow (MVI/UDF, ADR 0003).** `onIntent` → `GameSession.dispatch(event)` (internal class in `feature:game`:
holds `GameEngine`, current `GameState` and the append-only event log of *applied* events; duel later wraps
`MatchEngine` the same way) → `Transition` → (a) `MutableStateFlow<GameState>`, (b) effects mapped and sent.
- `state: StateFlow<GameUiState>` = engine state mapped by a pure `GameUiMapper` (domain → `@Immutable` UI model,
  `ImmutableList`s, string-resource keys not strings), `distinctUntilChanged` by equality, so pure ticks don't
  re-emit it. `clock: StateFlow<GameClockUi>` (remaining ms, fraction, whole seconds, warning flag) is separate.
- `effects: Flow<GameEffect>` = `Channel(BUFFERED).receiveAsFlow()`, collected while STARTED. Mapping per
  `motion.md`: `LetterPlaced`→fly #2 (its light haptic is the key's #1 press); `Correct`→pop #3 + Success + sound,
  then the word (after the correct sound) if TTS is on;
  `Wrong`→shake #4 + Error; `Passed`→#5 + Soft; `ComboReached`→#7 + Heavy; `TimeWarning`→Tick haptic + "tik";
  `JokerApplied`→inventory write; `GameOver`→record, then `NavigateToResult`. Haptics/sound play in the screen
  through `LocalHaptics`/`LocalSoundPlayer` so they stay in sync with the animation; TTS through a `Speaker`
  interface in `core:domain` (`feedback`), implemented in `core:platform` (M4 platform task).
- Persistence: joker use is saved at once (`JokerRepository.save`, no refund by killing the app). On `GameOver` a
  domain use case `FinishGame` (`core/domain/.../session`, pure orchestration over repository interfaces) builds the
  `SessionResult` (`AppClock` for `endedAtMs`/`epochDay`), calls `SessionRepository.record` and the SRS update; it
  runs once (guard flag) under `NonCancellable`. The event log is not stored in `user.db` in v1.0; it serves
  restore and replay tests. The result route payload is decided in the Result task.

**3. Animation.** Compose animation APIs only (`Animatable`, `updateTransition`, `animate*AsState`,
`rememberInfiniteTransition`); durations/easings only from `LexiTheme.motion`. Rule: the **resting look always comes
from state** (slot colours, current marker #6, centre letter, timer colour #8), so screenshots, restore and reduced
motion are correct; **effects only trigger transient motion** (fly, pop, shake, burst, pass rotation) that starts and
ends on that look. Reduced motion: transient transforms snap to their end values, colour/shape changes stay;
haptics and sound still fire.
- Wheel: keep the existing composable layout (26 `WheelLetter` chips placed on the circle in `LetterWheel`), not a
  single Canvas: per-chip animations (#3 scale + ring, #5 rotate + morph) map to `graphicsLayer`, each letter keeps
  accessibility semantics, and the component is already screenshot-verified. 26 small nodes are cheap.
- Timer: only the HUD timer reads `clock`; the bar fill reads the fraction in `drawBehind`/`graphicsLayer` (draw
  phase only); the counter text reads whole seconds (recomposes once per second); the #8 pulse is one 600 ms pulse
  keyed on the counter text while warning (once per second, with the tick). The rest of the screen never reads time.

**4. Tests.** `GameUiMapper` and `GameSession` (replay = live state) as plain unit tests; `GameViewModelTest` with
`runTest`, `Dispatchers.setMain(StandardTestDispatcher)`, `testScheduler.timeSource`, fakes from `core:testing`
(`FakeAppClock`, card/session/progress/joker repos) and Turbine: ticks, pause/background/foreground (no time charged),
reveal, game over records exactly once, effect mapping, restore from `SavedStateHandle`. Roborazzi
`GameScreenScreenshotTest` (stateless `GameScreen` with the design's sample state, `w390dp-h844dp-xhdpi` like
`GameSampleScreenshotTest`) plus time-warning and wrong-reveal states. No new library: `feature:game` adds existing
catalog entries only (Roborazzi plugin, robolectric/roborazzi for `androidHostTest`, kotlinx-serialization-json,
an explicit `lifecycle-viewmodel-savedstate` alias of the pinned lifecycle version); `core:platform` adds
kotlinx-datetime.

## Alternatives considered
- Engine-owned timer / `delay` in domain: breaks ADR 0002 determinism and golden replay.
- 1 s ticks: reveal could last up to 2 s and the bar would jump; event-driven next-wake scheduling: more code, same result.
- Auto-resume on foreground: the clock would run before the player is ready again.
- Abandon on process death: loses rounds on low-memory devices although event sourcing makes restore cheap.
- Saving the full `GameState` (cards, clues): larger Bundle, duplicates content.
- Canvas wheel: re-implements text, shapes and semantics for no measurable gain.
- Feedback played by the ViewModel: harder to keep in sync with animations; `GameEffect` stays testable either way.

## Consequences
- Background time is never charged; a paused screen is the single return point after interruptions.
- New engine property test (tick additivity) and new domain types (`AppClock`, `Speaker`, `FinishGame`).
- Orientation: the app is portrait-only on phones (all designs are portrait; Android `screenOrientation=portrait`,
  iOS `UISupportedInterfaceOrientations` portrait). `configChanges` above still covers fold/multi-window resizes.
- Open (M6): for the Daily wheel, whether an attempt counts at `Start` (otherwise killing the app could replay it).
- Open (M7): `FinishGame` is not idempotent. If `SessionRepository.record` succeeded but the SRS update failed in
  both attempts, the snapshot is kept, so a game restored after a process death records the session a second time.
  Fix with an idempotent record keyed by `seed + startedAtMs` (unique key in `user.db`), not by a schema change now.

## Addendum (M4 joker sheet, pause, result — 2026-10-15)
- **Result payload:** `ResultRoute` carries a small summary instead of a session id (level, mode, category, reason,
  score, correct/wrong/passed, longest streak, remaining ms, `missedCards` in wheel order, the `passedCards` subset,
  `recorded`). `user.db` keeps counts only, not per-card outcomes, and the event log is not stored; card ids are
  resolved from the content by the `ResultViewModel`. The list is exactly what `FinishGame` put into box 0
  (`GameSummary.missedCards` = WRONG/PASSED slots = `Srs.results` misses). Navigation: `Game → Result` pops up to Home,
  so back and "Ana menüye dön" return to Home; TEKRAR OYNA starts `GameRoute(level, mode, category)` the same way.
- **Snapshot:** a finished game stays in the `SavedStateHandle` until `FinishGame` succeeded (a process death in
  between records it after restore). A failed `FinishGame` is retried once (it repeats only failed steps), then
  logged with Kermit (TODO M7: crash reporter); the result is shown with `recorded = false` (no "added to notebook").
- **Jokers:** the sheet is ViewModel state (not engine state); the clock keeps running while it is open; it closes
  when a joker is applied, on pause/background, reveal and game over. Rows are enabled by `GameEngine.canUseJoker`,
  the same rule `reduce` applies. Inventory writes run undispatched under `NonCancellable`, serialized by a mutex, and
  each stores the latest inventory. The Listen joker speaks the answer; the Example joker's blanked sentence is shown
  in the clue card (design debt).
- **Back / pause:** system back (both platforms, `NavigationBackHandler` in common code; `navigationevent-compose`
  is declared through a catalog alias of the version navigation-compose already resolves, not a new library) closes
  the joker sheet, leaves the quit confirmation, or pauses a running game; on the pause dialog it does nothing, so
  only "Devam et" restarts the clock. While loading, back leaves the screen. The pause dialog (design debt) is two
  `ConfirmDialog` steps: pause (Devam et / Bırak) and, since giving up can't be undone, a confirmation (Bırak =
  `Forfeit` / Vazgeç).
- **Effects:** the ViewModel ignores a transition only when it is the same state *and* has no effects, the same rule
  as `GameSession` (`Transition.isIgnored`).
