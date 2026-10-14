# Conventions

## Kotlin
- Official Kotlin style, enforced by ktlint; detekt default rules + `config/detekt.yml`.
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
- Screenshot tests: Roborazzi, files under `src/androidUnitTest/screenshots`, reviewed in PRs.

## Git
- `main` is always green and never receives direct commits or pushes. Everything lands through a PR.
- One branch per ROADMAP task group / feature, not per tiny change: `feat/m0-bootstrap`, `feat/design-system`,
  `feat/game-engine`, `feat/content-compiler`, `feat/game-screen`, `fix/<topic>`, `chore/<topic>`, `docs/<topic>`.
  Several small commits per branch are fine.
- Before starting a task group: `git switch main && git pull`, then create the branch. Never stack unrelated work
  on one branch.
- Conventional Commits, English, imperative mood: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`;
  scope = module/area. Commit after each completed ROADMAP task.
- When a branch is done: push the branch → open a PR into `main` (`gh pr create`). PR body: summary, changes,
  decisions taken, how to test, screenshots for UI, checklist (tests, lint, docs updated, no secrets).
- **Squash-merge** with `gh pr merge --squash --delete-branch` once `./gradlew build` and
  `./gradlew :androidApp:assembleDebug` are green locally and CI is green, after checking `git log` shows only the
  owner's identity. Then `git switch main && git pull` and start the next branch. Agents may merge their own PRs
  under these conditions.
- Never commit or push directly to `main`, never force-push, never rewrite published history.
- A change that makes a doc outdated updates that doc in the same branch.
- No AI attribution: no `Co-Authored-By: … Claude`, "Generated with Claude Code", `Claude-Session` or other
  Claude/Anthropic lines in commits, PRs or code comments. Enforced by `.claude/settings.json` (`attribution`) and
  the committed `.githooks/commit-msg` hook — run `git config core.hooksPath .githooks` once per clone.
- Tags `v1.0.0`; versionCode = `major*10000 + minor*100 + patch`.

## Definition of done
Acceptance criteria met · tests added & green · `./gradlew build` green · UI matches design screenshot ·
strings in TR+EN · no new warnings · docs/ROADMAP updated · `code-reviewer` agent passed.
