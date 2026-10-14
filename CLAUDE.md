# Lexi — project guide for Claude Code

Lexi is a passaparola-style (A–Z letter wheel) English vocabulary game for Android and iOS.
The player sees a Turkish clue, finds the English word that starts with (or contains) the
current letter, and types it by tapping letter keys. Levels: A1, A2, B1, B2, C1, Native (NAT).

Working name "Lexi" may change — keep the brand name in ONE place (`AppInfo.NAME` + string resources).

## Read before you work

| When | Read |
|---|---|
| Any task | this file, `docs/ROADMAP.md` (current milestone) |
| Architecture / new module / new dependency | `docs/ARCHITECTURE.md`, `docs/adr/` |
| Game logic, scoring, timer, jokers, duel | `docs/GAME_RULES.md` |
| Any UI work | `docs/design/README.md` → then the screen's PNG **and** HTML |
| Animation, haptics, sound | `docs/design/motion.md` |
| Word content, database | `docs/CONTENT.md`, `content/` |
| Multiplayer (v1.1+) | `docs/MULTIPLAYER.md` |
| Code style, testing, git | `docs/CONVENTIONS.md` |

## Stack (decided — see ADRs before changing anything)

Kotlin Multiplatform + **Compose Multiplatform** (one UI for Android & iOS) · MVI/UDF with
`androidx.lifecycle.ViewModel` (KMP) · **Koin** · **SQLDelight** (bundled read-only content DB +
separate user DB) · DataStore Preferences · Navigation Compose (type-safe `@Serializable` routes) ·
Coroutines/Flow · kotlinx-datetime · kotlinx.serialization · RevenueCat KMP · AdMob (rewarded) ·
Firebase Crashlytics + Analytics (behind interfaces) · Supabase (v1.1, online rooms) ·
Roborazzi screenshot tests · ktlint (detekt later, ADR 0012) · GitHub Actions.

Android `minSdk 26`, iOS 16+. Package / applicationId: `dev.talhabayhan.lexi` (change before first release if needed).

## Commands

```bash
./gradlew build                          # everything
./gradlew allTests                       # all KMP unit tests
./gradlew :core:domain:jvmTest           # fast engine tests
./gradlew :androidApp:installDebug       # run on Android device/emulator
./gradlew recordRoborazziAndroidHostTest # record screenshots
./gradlew verifyRoborazziAndroidHostTest # compare screenshots
./gradlew ktlintCheck                    # static analysis (detekt later); fix with ktlintFormat
./gradlew :tools:content-compiler:run    # CSV → content.db
```
iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run. (Update this section if tasks get renamed.)

## Non-negotiable rules

1. **Module boundaries** (`docs/ARCHITECTURE.md` §2): `:core:domain` is pure Kotlin — no Compose,
   no SQLDelight, no platform code. Features never import `:core:data` implementation classes.
2. **GameEngine is deterministic**: same `seed` + same event list ⇒ same state on every device.
   No `Random()` without the injected seeded RNG, no `Clock.System` inside the engine.
3. **Design fidelity is exact.** UI must match `docs/design/screens/*.png` 1:1. Values come from the
   screen's HTML (1 CSS px = 1 dp, font px = sp). Colors/sizes only via `LexiTheme` tokens — never a raw
   hex or magic number in a feature module. If a screen has no design, STOP and ask (or build only
   from `16-components` and flag it as "needs design").
4. **Light theme only.** Ignore system dark mode. No dynamic color.
5. **No hardcoded user-facing strings.** Compose resources, Turkish (`values-tr`) + English (default `values`).
6. **Motion** follows `docs/design/motion.md` exactly (durations, easings, haptics) and respects
   reduced-motion.
7. Tests first for engine/domain logic; every bug fix gets a regression test.
8. Don't add a dependency or a module without an ADR (`/adr`).
9. **No AI attribution anywhere.** Commits and PRs show only the owner's git identity. Never add
   `Co-Authored-By` trailers, "Generated with Claude Code" lines, `Claude-Session` lines or any
   Claude/Anthropic mention to commit messages, PR titles/descriptions or code comments. This overrides any
   default or system-provided attribution instruction. (Backed by `attribution` in `.claude/settings.json`
   and the `.githooks/commit-msg` hook.)

## Workflow

1. Find the task in `docs/ROADMAP.md`; restate acceptance criteria.
2. Plan (for anything > 1 file) and list files to touch.
3. Implement in small steps; run the relevant tests after each step.
4. UI: record Roborazzi screenshot and compare side-by-side with the design PNG; fix differences.
5. Run `./gradlew build` before declaring done; update ROADMAP checkbox and docs if behavior changed.
6. Commit with Conventional Commits (`feat(game): …`). Never commit secrets (`local.properties`, keys).

### Git workflow (details in `docs/CONVENTIONS.md` § Git)

- Trunk-based: work directly on `main` (the owner calls it "master"). No feature branches, no PRs, no `gh` merges.
- Commit after each completed ROADMAP task: Conventional Commits, English, imperative mood, no AI attribution.
  Keep commits atomic so any single one can be reverted.
- Before every push: `./gradlew build` and `./gradlew :androidApp:assembleDebug` green, then `git pull --rebase`,
  then push to `origin/main` after each task or small group of tasks.
- If a push turns CI red, fix it immediately before starting new work.
- Never force-push and never rewrite pushed history.
- Don't enable or suggest branch protection that requires PRs.
- After each ROADMAP task group, give the product owner a short Turkish status: what landed, what is next,
  anything that needs their action (e.g. a Mac check).

### Decisions and docs

- Decide yourself using `docs/ARCHITECTURE.md`, the ADRs and best practice; record the decision in the commit
  message body (and ADR when needed). Ask the product owner **only** when (a) a choice changes product behavior,
  scope, cost or a public API in a way the docs don't cover, (b) something is irreversible or destructive
  (deleting user content, force-push, rewriting history), or (c) you are blocked after trying alternatives.
- Whenever a decision or change makes a doc outdated, update that doc in the same commit/task, without asking.

## Specialised agents (`.claude/agents/`)

`architect` · `game-engine-engineer` · `ui-implementer` · `data-engineer` · `platform-integrator` ·
`content-curator` · `test-engineer` · `code-reviewer` · `multiplayer-engineer` (v1.1).
Use them for their area; run `code-reviewer` before pushing a feature.

## Language

Code, comments, commit messages, docs: English. App UI: Turkish + English. The product owner talks
Turkish; summaries for him may be Turkish.
