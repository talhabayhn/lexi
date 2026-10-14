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
- Trunk-based: short branches `feat/game-wheel`, `fix/timer-pause`; PR into `main`; squash merge.
- Conventional Commits: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`; scope = module/area.
- Every PR: description, screenshots for UI, checklist (tests, lint, docs updated, no secrets).
- Tags `v1.0.0`; versionCode = `major*10000 + minor*100 + patch`.

## Definition of done
Acceptance criteria met · tests added & green · `./gradlew build` green · UI matches design screenshot ·
strings in TR+EN · no new warnings · docs/ROADMAP updated · `code-reviewer` agent passed.
