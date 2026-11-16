# ADR 0026: Home v2 picks level, mode and category; `feature:levels` is removed

- Status: Accepted
- Date: 2026-11-16

## Context
The owner approved Home v2 (`docs/design/screens/02c-home-v2`, `docs/design/home-v2-notes.md`): the level, mode and
category of a wheel are chosen on Home and OYNA starts the game directly. The level select (`03-level-select`) and the
categories screen (`07-categories`) had no other entry point, so `feature:levels` would only keep the level progress,
the level names and the category loading — all needed by Home alone. Rule 1 (`ARCHITECTURE.md` §2) forbids
`feature:home` from importing another feature's internals.

## Decision
- Delete the `feature:levels` module (both screens, view models, routes, tests, goldens, strings).
- Category loading becomes a pure use case in `:core:domain` (`stats/LevelCategories.kt`: `GetLevelCategories`,
  `LevelCategoryContent.words()`); level progress keeps using `Stats.levelProgress`.
- The new visuals are `:core:designsystem` components (`HomeTierSize`, `DailyCard` v2 with one status badge, `LevelHeroCard`,
  `LevelChipRow`, `PickerRow`, `KeyTile`, `CountBadge`, `HomePlayButton`, four `LexiIcons`, and the `07-categories`
  cards `CategoryCard` / `MixedWheelCard` with a selected state for the category sheet) used by
  `feature:home`, which owns the state, the level names (`home_level_*`) and the category sheet.
- The selection is stored in the settings DataStore (`Settings.selectedLevel` on the existing `selected_level` key
  that the onboarding writes, plus `home_mode` and `home_category`); the splash waits for the loaded settings so
  Home's first frame shows the stored selection.
- Layout: OYNA follows the category row and the footer key tiles are pinned at the bottom; only when no tier fits is
  OYNA pinned above the footer and the part above it scrolls (`home-v2-notes.md` §1).
- Components only the removed screens or the old Home used (`StartButton`, `LevelSelectDefaults`, `DuelHomeBox`, `StreakChip`, `LexiIcons.Categories`) are deleted with them. Home no longer shows
  the streak or the daily goal (owner feedback); their logic is unchanged.

## Consequences
- One module fewer; dependencies stay one-way (`feature:home` → `core:*`). No new library: `feature:home` declares
  `navigationevent-compose` (already in the catalog, used by `feature:game`) for the sheet's system back.
- `LexiNavigator.openLevelSelect` / `openCategories` and both routes are gone; `GameRoute(level, mode, category?)` is
  unchanged, so saved games keep working. The perf tour measures `Home→Game` instead of two segments.
- A fourth mode (fill-the-blank) needs a decision on Home's mode control (`home-v2-notes.md` §7).
