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
- No raw colors/dp/sp in features — `LexiTheme.colors.*`, `LexiTheme.dimens.*`, `LexiTheme.type.*`.
- `Modifier` is the first optional parameter. Hoist state. `remember` keys explicit.
- Strings: `stringResource(Res.string.xxx)`; keys `screen_element_purpose` (e.g. `game_button_pass`).
- Accessibility: every icon-only button has `contentDescription`; wheel letters announce state ("K, doğru").

## Packages
`dev.talhabayhan.lexi.<module>.<area>` e.g. `dev.talhabayhan.lexi.feature.game.components`.

## Testing
- Test names: backticks or `snake_case` describing behavior: `` `pass ends turn in duel` ``.
- Arrange/Act/Assert; one behavior per test; fakes over mocks (fakes live in `core:testing`).
- Engine tests run on `jvmTest` for speed; a small `commonTest` subset runs on iOS simulator in CI.
- Screenshot tests: Roborazzi, Robolectric host tests in `src/androidHostTest`, goldens under `src/androidHostTest/screenshots`, reviewed in the commit diff. Use `@Config(sdk = [36])` until Robolectric ships an SDK 37 runtime (ADR 0013).

## Git
- Trunk-based: all work is committed directly on `main`. No feature branches, no PRs.
- Commit after each completed ROADMAP task. Keep commits atomic so any single one can be reverted.
- Conventional Commits, English, imperative mood: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`;
  scope = module/area.
- Before every push: `./gradlew build` and `./gradlew :androidApp:assembleDebug` green, then `git pull --rebase`.
  Push to `origin/main` after each task or small group of tasks.
- If a push turns CI red, fix it immediately before starting new work.
- Never force-push and never rewrite pushed history.
- A change that makes a doc outdated updates that doc in the same commit (or the same task).
- No AI attribution: no `Co-Authored-By: … Claude`, "Generated with Claude Code", `Claude-Session` or other
  Claude/Anthropic lines in commits, PRs or code comments. Enforced by `.claude/settings.json` (`attribution`) and
  the committed `.githooks/commit-msg` hook — run `git config core.hooksPath .githooks` once per clone.
- Tags `v1.0.0`; versionCode = `major*10000 + minor*100 + patch`.

## Definition of done
Acceptance criteria met · tests added & green · `./gradlew build` green · UI matches design screenshot ·
strings in TR+EN · no new warnings · docs/ROADMAP updated · `code-reviewer` agent passed.
