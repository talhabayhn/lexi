# Roadmap & milestones

Mark tasks `[x]` when done (acceptance criteria met + `./gradlew build` green). One milestone at a time;
each milestone ends with a demo build on both platforms and a short Turkish summary for the product owner.

## M0 — Project bootstrap
Module mapping (replaces the earlier `composeApp`): `androidApp` = thin Android shell, `shared` = KMP umbrella
(App(), navigation, Koin start, iOS framework export), `iosApp` = Swift entry + SDK bridges.
- [x] Generate CMP project (Android Studio wizard: `androidApp`, `shared`, `iosApp`) with package `dev.talhabayhan.lexi`
- [x] Line endings: repo-wide `.gitattributes` (LF), renormalized; `gradlew` executable
- [x] GitHub Actions: `build.yml` (build + tests + lint on ubuntu, on push to `main`), `ios.yml` (macOS framework link + simulator tests, path-filtered; `xcodebuild` nightly / on `iosApp` changes)
- [x] Toolchain spike: AGP 9 KMP library plugin + SQLDelight schema in `core:data`, Roborazzi screenshot in `core:designsystem`; material3 stable or dropped; outcome in toolchain ADR
- [x] `build-logic` convention plugins: `lexi.kmp.library`, `lexi.kmp.compose`, `lexi.android.application`, `lexi.feature` (+ `lexi.kmp.pure`, `lexi.jvm.application`, `lexi.module-rules`); `shared` and `androidApp` migrated
- [x] Create modules from `ARCHITECTURE.md` §2 (empty, compiling): core:domain/data/designsystem/platform/testing, feature:* (v1.0 ones), tools:content-compiler; dependency rules enforced by a build check
- [x] Version catalog with: coroutines, datetime, serialization, Koin (+compose-viewmodel), lifecycle-viewmodel-compose (KMP), navigation-compose (KMP), SQLDelight, DataStore, Kermit, Turbine, Roborazzi, BuildKonfig, ktlint (detekt later)
- [x] ktlint configured (detekt later, ADR 0012); `./gradlew build` runs it
- [x] `.gitignore`, `local.properties.example` (keys placeholders), README run instructions
- [x] Koin started in `App()` (`shared`); empty `HomeRoute` (`feature:home`) renders "Lexi" on both platforms
  (Android ✅ verified on emulator; iOS ✅ simulator launch screenshot from the `ios.yml` `screenshot` job)
**Done when:** both apps launch; CI green; module graph matches the dependency rules.

## M1 — Design system
- [ ] Jost fonts in `composeResources/font`; `LexiTheme` (colors, typography, dimens, shapes, motion, shadows); Material3 light scheme mapped; dark mode ignored
- [ ] `hardShadow` modifier + `PressableSurface` (press animation 80 ms + haptic + sound hooks)
- [ ] All components from `docs/design/components.md` with previews
- [ ] Icons as ImageVectors
- [ ] `LexiLogo` + launcher icons (option 1) for Android (adaptive) and iOS (AppIcon set)
- [ ] Roborazzi screenshot of a "component gallery" composable ≈ `16-components.png`
**Done when:** gallery screenshot matches the design PNG side-by-side (reviewed by product owner).

## M2 — Game engine (TDD, core:domain)
- [ ] `SeededRandom` (SplitMix64) + tests for cross-platform stability (fixed golden sequence)
- [ ] Domain models: Level, Category, Card, LetterRule, Wheel, Slot, SlotState, GameMode, GameConfig, Joker…
- [ ] `WheelGenerator` (incl. review wheel, skipped letters)
- [ ] `KeyboardGenerator` (key count, distractors, determinism)
- [ ] `GameEngine.reduce` for all events in `GAME_RULES.md` §12; scoring §6; jokers §7; modes §5
- [ ] `MatchState` + duel turn rules §8 (hot-seat)
- [ ] `Srs` (Leitner) §10; `LevelTest` staircase §9; stats calculators §11
- [ ] Golden test: seed X + event log Y ⇒ exact final state snapshot (serialized)
**Done when:** ≥ 90% line coverage on engine package; every rule has a named test.

## M3 — Content & data
- [ ] `tools:content-compiler`: parse CSVs, validate (`CONTENT.md` §3), write `content.db`, print report
- [ ] A1 full set (850) — content-curator in batches of 100, product owner reviews; other levels 26-card smoke sets
- [ ] SQLDelight `content.db` (read-only, copied from resources on first run/version bump) + `user.db` with migrations
- [ ] Repositories (`ARCHITECTURE.md` §6) + DataStore settings
- [ ] Repository tests with in-memory driver
**Done when:** app reads a wheel from the bundled DB on both platforms.

## M4 — Core loop UI
- [ ] Splash (`14-splash`) with motion spec
- [ ] Home (`02-home`), Level select (`03-level-select`)
- [ ] Game screen (`04-game`) wired to engine: wheel, timer, clue, slots, keys, backspace, pass, score
- [ ] Joker sheet (`05-joker-sheet`) + jokers inventory
- [ ] Pause dialog (design debt)
- [ ] Result (`06-result`) incl. "Tekrar edilecekler" → SRS
- [ ] Motion catalogue #1–#8, haptics & sounds (`motion.md`)
- [ ] Platform: Speaker (TTS), Haptics, SoundPlayer
**Done when:** a full A1 classic game is playable start→result on both platforms, screenshots match designs.

## M5 — Learning & progress
- [ ] Categories (`07-categories`) + category wheels
- [ ] Notebook (`08-notebook`): tabs, search, rows, review wheel, word detail sheet (design debt)
- [ ] Stats (`09-stats`)
- [ ] Practice & Hardcore modes; empty/error states
**Done when:** SRS loop works across days (test with fake clock).

## M6 — Daily, sharing, onboarding
- [ ] Daily wheel (seed per date+level, one scored attempt, already-played state)
- [ ] Streak logic + home badge
- [ ] Share card (`12-share-card`) rendered to image + system share sheet
- [ ] Onboarding 3 steps (`01-onboarding` + design debt), level test, level-test result
**Done when:** two devices get the identical daily wheel for the same date and level.

## M7 — Monetisation, settings, exam packs, telemetry
- [ ] Settings (`11-settings`) all options functional; reminders (notification permission pre-prompt)
- [ ] RevenueCat: Pro entitlement, paywall (design debt), restore purchases
- [ ] AdMob rewarded (+1 joker) with UMP consent; interstitial policy from `PRODUCT.md`
- [ ] Exam packs (`10-exam-packs`) gated by Pro; LGS pack content
- [ ] Firebase Analytics + Crashlytics behind interfaces; event list in `docs/ANALYTICS.md`
**Done when:** sandbox purchase unlocks Pro on both platforms; ads only where allowed.

## M8 — Same-device duel, polish, release
- [ ] Hot-seat duel: setup, handover card, per-player clocks, duel result (design debt)
- [ ] Accessibility pass (TalkBack/VoiceOver labels, font scale caps, contrast, reduced motion)
- [ ] Small-screen & tablet layout rules (`screen-map.md`)
- [ ] Rating prompt, About/licenses (Jost OFL, word-list attributions)
- [ ] Performance: cold start < 1.5 s mid-range Android; 60 fps game screen
- [ ] Store listing TR/EN, screenshots, privacy policy, data safety / privacy nutrition labels
- [ ] Release: Play internal testing + TestFlight
**Done when:** v1.0 submitted.

## v1.1 — Online rooms (`MULTIPLAYER.md`)
Design online screens → Supabase project & RLS → anonymous auth + nickname → `SupabaseRealtimeTransport` →
rooms (create/join/lobby) → online Duel → Race (+ level handicap) → reconnect → load test with 8 clients.

## v1.2
Teams, async challenge links, achievements, cloud backup.
