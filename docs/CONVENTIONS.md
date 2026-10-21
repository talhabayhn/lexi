# Conventions

## Kotlin
- Official Kotlin style, enforced by ktlint (`.editorconfig`, `intellij_idea` style, 120 columns; part of
  `./gradlew build`, fix with `./gradlew ktlintFormat`). detekt later, once a stable release supports our Kotlin
  version (ADR 0012).
- Explicit API mode in `core:*` modules (`explicitApi()`); features may use internal by default.
- Immutable models (`data class` with `val`, `kotlinx.collections.immutable` for UI state lists); `@Immutable`/`@Stable`
  on UI state classes.
- No `!!`. No `GlobalScope`. No `runBlocking` outside tests/tools.
- Dispatchers injected (`AppDispatchers`), never hardcoded in data/domain.
- Time injected (`Clock`, `TimeSource`); dates via kotlinx-datetime, "today" via the user's time zone.
- Expected failures as sealed results; exceptions only for programmer errors.
- Logging via Kermit `Logger.withTag("…")`.

## Compose
- Screen = `XxxRoute` (gets ViewModel via `koinViewModel()`, collects state with `collectAsStateWithLifecycle`,
  handles effects) + stateless `XxxScreen(state, onIntent, modifier)`.
- Previews for every screen and component, using design sample data.
- No raw colors/dp/sp in features — `LexiTheme.colors.*`, `LexiTheme.dimens.*`, `LexiTheme.typography.*`
  (also `borders`, `shapes`, `shadows`, `motion`).
- `Modifier` is the first optional parameter. Hoist state. `remember` keys explicit.
- Strings: `stringResource(Res.string.xxx)`; keys `screen_element_purpose` (e.g. `game_button_pass`).
- Accessibility: every icon-only button has `contentDescription`; wheel letters announce state ("K, doğru").

## Accessibility checklist (every new or changed screen)
- Icon-only buttons have `contentDescription`; keys read their letter; wheel letters announce state.
- Titles and section labels use `Modifier.lexiHeading()`.
- Decorative shapes are hidden (`clearAndSetSemantics` or null description).
- Expose roles (Button, Switch, RadioButton, Tab) and state (selected, checked, disabled).
- Semantic touch bounds are at least `LexiTheme.dimens.minTouchTarget`.
- Traversal order via `isTraversalGroup`/`traversalIndex`; game: HUD → clue → wheel → answer → keys → actions.
- Sheets and dialogs are modal (hide background), with the title as heading.
- Announcements go through `LiveAnnouncement`/`GameAnnouncer`: polite, once per event, never per tick.
- Font scale: scrollable layouts, `minHeight` not fixed height, buttons wrap via `maxLines`; game board and Home are capped at 1.15 by design.
- Reduced motion via `LexiMotion.reducedMotion` (fades allowed).
- Contrast: WCAG AA; never change the palette without owner approval. Known failing pairs: white/cream on
  red `E0534C` small text (3.81 / 3.47); TimerBar red counter on red fill; `borderMuted` Used key letter (2.95).
- Tests: `*A11yTest` semantics tests, plus fontScale 2.0 goldens for new screens.

## Packages
`dev.talhabayhan.lexi.<module>.<area>` e.g. `dev.talhabayhan.lexi.feature.game.components`.

## Testing
- Test names: backticks or `snake_case` describing behavior: `` `pass ends turn in duel` ``.
- Arrange/Act/Assert; one behavior per test; fakes over mocks (fakes live in `core:testing`).
- Engine tests run on `jvmTest` for speed; a small `commonTest` subset runs on iOS simulator in CI.
- Coverage is measured ad hoc (a one-off JaCoCo run on `jvmTest`), not in the build: no Kover (owner decision).
- Engine golden tests (`core/domain/.../game/GoldenTest.kt`): seed + literal event log ⇒ exact JSON in
  `GoldenSnapshots.kt`. A diff there changes behaviour for every device and stored replay: update it only on purpose
  (paste the "actual" JSON from the failure) and explain the rule change in the commit.
- Screenshot tests: Roborazzi, Robolectric host tests in `src/androidHostTest`, goldens under `src/androidHostTest/screenshots`, reviewed in the commit diff. Use `@Config(sdk = [36])` until Robolectric ships an SDK 37 runtime (ADR 0013).
- Component behaviour (clicks, haptics/sounds, timing): `runComposeUiTest` + `mainClock` in the same Robolectric
  host tests, fakes via `LocalHaptics`/`LocalSoundPlayer` (ADR 0017).

## Git
- Trunk-based: all work is committed directly on `main`. No feature branches, no PRs.
- Commit after each completed ROADMAP task. Keep commits atomic so any single one can be reverted.
- Conventional Commits, English, imperative mood: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`;
  scope = module/area.
- Before every push: `./gradlew build` and `./gradlew :androidApp:assembleDebug` green (Tiny changes: only the
  checks the change can affect) and the review the effort tier requires is done (`code-reviewer` reviews
  `git diff origin/main`); then `git pull --rebase`. Push to `origin/main` after each task or small group of tasks.
- Effort tiers: **Tiny** (docs, CI/config, copy/strings, constants, ≤ ~30 lines, no logic) = no subagents, no
  `code-reviewer`, only the affected checks. **Normal** (a screen, component, bug fix with tests, one module) = main
  session, one `code-reviewer` pass at the end, full build once before push. **Risky** (GameEngine/domain rules,
  concurrency/navigation lifecycle, persistence/migrations, payments, ads, auth, permissions, release/signing, > 2
  modules) = plan first, subagents allowed, `code-reviewer` until APPROVE. Pick the lowest tier that fits (or the one
  the prompt names) and state it in the final report; CI green after push in every tier.
- If a push turns CI red, fix it immediately before starting new work.
- Never force-push and never rewrite pushed history.
- A change that makes a doc outdated updates that doc in the same commit (or the same task).
- No AI attribution: no `Co-Authored-By: … Claude`, "Generated with Claude Code", `Claude-Session` or other
  Claude/Anthropic lines in commits, PRs or code comments. Enforced by `.claude/settings.json` (`attribution`) and
  the committed `.githooks/commit-msg` hook — run `git config core.hooksPath .githooks` once per clone.
- Tags `v1.0.0`; versionCode = `major*10000 + minor*100 + patch`.

## Definition of done
Acceptance criteria met · tests added & green · `./gradlew build` green · UI matches design screenshot ·
strings in TR+EN · no new warnings · docs/ROADMAP updated · `code-reviewer` passed when the effort tier requires it.
