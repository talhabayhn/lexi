# Home v2 (`02c-home-v2`) — interaction rules and implementation plan

Status: **owner-approved design, not implemented.** Visual truth: `screens/02c-home-v2.png` (+ `.html`, source
`source/02c-home-v2.dc.html`). Palette/sizes are `tokens.md` (the older `02-home` HTML still carries the pre-contrast
hexes; use the tokens). Level, mode and category are chosen **on Home**; `03-level-select` is superseded once this ships.
New pieces marked **needs design**: the four footer icons (book, bar chart, graduation cap, two people — add to
`LexiIcons`), the footer key tiles, the due-reviews badge, the level chips row, the category bottom sheet, the
disabled/hidden category state. Their values are from the board; everything else reuses `16-components`.

> The owner's draft `docs/design/drafts/dashboard-d2.png` (panel "D2") was **not in the repo or on this machine**, so
> the board was drawn from the written brief. Commit the draft next to it and compare once.

## 1. Layout and tiers (360 × 640 at 100 %, 130 %, 200 %)
Order: header (logo 36 + tagline 11 sp 0.2em, gear) → daily card → `ÇARKINI KUR` (hero card, 6 chips, mode control,
category row) → **OYNA** → footer tiles. OYNA and the footer are **pinned**; everything above them is one scroll
column (the onboarding rule). Tiers (`HomeTier`, DESIGN-DEBT, measured like `FitToHeight`, first that fits inside the
safe-drawing insets wins; sizes in dp):

| Tier | Padding T / X / B | Block gap / build gap | Logo | Daily circle | Hero min | Chip | Mode | Category | OYNA | Key |
|---|---|---|---|---|---|---|---|---|---|---|
| Regular (390 × 844, as drawn) | 24 / 20 / 28 | 16 / 10 | 36 | 42 | 76 | 48 | 56 | 48 | 68 | 56 |
| Compact (360 × 640 drawn) | 8 / 20 / 12 | 8 / 6 | 28 | 34 | 56 | 48 | 48 | 48 | 52 | 44 |
| Tight (200 % font drawn) | 8 / 16 / 12 | 8 / 6 | 28 | 32 | 56 | 48 | 48 | 48 | 52 | 44 |

- Regular uses the spare height as a spacer between the category row and OYNA (nothing grows); that is why the
  390 × 844 board has a gap. At 360 × 640 with 24 / 16 dp system bars Compact has ~20 dp left.
- Chips and key tiles never go below 48 / 44 dp touch height; chip row is a 6-column grid, gap 6.
- Font scale: scrolling text follows the system scale up to 2.0. **Pinned and control text (OYNA, badge, footer
  labels, chips, mode cells, hero count, streak pill, tagline) is capped at 1.3** so four footer columns and six chips
  keep fitting; their content descriptions carry the full text. The old Home cap of 1.15 goes away.
- Hero card: long names wrap on the left; the count and the 12 dp bar stay in the right column (width 132 × min(scale,
  1.3)). Daily card: the streak pill wraps under the label when the label gets long.
- Daily card states keep their copy: Available (title + goal line), Completed (`Tamamlandı · N puan`, next-wheel
  countdown, goal line `1/1` with a filled pip — 3 text lines, ~10 dp taller than Available, inside the tier budget),
  Unavailable (muted `Kullanılamıyor`, no chevron). Streak 0 hides the pill. The old floating "Bugün 0/1" chip and the old
  streak chip are removed; `DailyGoalProgress` feeds the pip (`done >= goal` ⇒ filled).
- Footer badge = due reviews (`dueReviews`): blue circle 22 dp, 2 dp ink border, 11 sp white; hidden at 0, `99+` from 100.
  Blue chosen over red because red means "wrong" in this app; flip if the owner prefers.
- EN strings to add: tagline `FROM A TO Z IN ENGLISH`, `SET UP YOUR WHEEL`, `Everyone solves the same wheel`,
  `today's goal %1$d/%2$d`, footer `Notebook / Statistics / Exams / Duel`; TR `ÇARKINI KUR`, `Herkes aynı çarkı
  çözüyor`, `bugünkü hedef %1$d/%2$d`, `Defter / İstatistik / Sınavlar / Düello`. Level names, mode labels/hints,
  `levels_progress` and the badge format are the existing `levels_*` / `home_play_badge` strings (move them to the
  shared place the Home owns; `feature:home` may not depend on `feature:levels` internals — see §6).

## 2. Remembered selection
Today: the only stored level is `selected_level` (`OnboardingRepositoryImpl`, written once by the onboarding level
choice / level-test result) and **nothing reads it** — Home's badge is hardcoded `A1 · KLASİK` (`HomeUiState.playLevel`
TODO) and the level select always opens on A1 / Classic. Mode and category are never stored. Proposal (DataStore
Preferences, `SettingsRepositoryImpl` style, one writer = the Home view model, written on every change):

| Key | Type | Default | Notes |
|---|---|---|---|
| `selected_level` (existing) | string | `A1` | reuse: onboarding already seeds it, so a new player lands on their level; no migration |
| `home_mode` | string (`GameMode.name`) | `CLASSIC` | only CLASSIC / PRACTICE / HARDCORE are selectable; unknown or removed value ⇒ CLASSIC |
| `home_category` | string (`CategoryId`, empty = Karışık) | empty | stored **per level** is overkill: store one value and drop it when it is not in the selected level's list |

Rules: restored before the first frame (read with the settings flow; show defaults only until it emits, no
flash of A1). Changing level resets the category to Karışık (categories differ per level) but keeps the mode. A game
started from Home writes nothing extra (the selection is already saved). Categories are read lazily for the selected
level; a stored id that no longer exists silently becomes Karışık.

## 3. Category row and sheet
Row `Kategori  <seçim> ▾` (48 dp) opens a bottom sheet (`BottomSheet`, scrim 55 %, cream, 4 dp top border): title,
`Karışık` first (selected = ink row, yellow text, filled yellow radio), then the level's categories with
`learned / total` (same data and exclusions as `07-categories`: no virtual `mixed`, no `general`), a completed category
reads `· tamam`. Tapping a row selects it and closes the sheet (no start). Sheet scrolls past ~6 rows; read failure ⇒
`EmptyState` with "Tekrar dene" and the row falls back to Karışık (play still works, like the level select's error).
- **Level without categories (today: NAT):** the row is **hidden** (as `LevelSelectScreen` does). It is the last item
  of the scroll column, so nothing above it moves; the spacer absorbs the freed height. The OYNA badge still reads
  `NAT · … · 26 HARF`. A level whose category list is empty or unreadable behaves the same.
- NAT card hero shows `Deyim · phrasal verb` (wraps), progress as usual.

## 4. Old `LevelSelectRoute` callers → new destination
All current uses (grep of `LevelSelectRoute` / `openLevelSelect`):

| Caller | Today | After v2 |
|---|---|---|
| Home OYNA (`homeScreen(onPlay)` → `openLevelSelect()`) | opens level select (Classic) | **starts the game directly** with the saved level / mode / category (`startGame(level, mode, category)`); `HomeEffect.NavigateToLevelSelect` → `StartGame(level, mode, category)` |
| Home practice tile (`onOpenPractice` → `openLevelSelect(PRACTICE)`) | level select on Practice | **removed**; Practice is the mode control's 2nd cell (`HomeIntent.OpenPractice` deleted) |
| Level-test result "Seviyeyi değiştir" | **does not use the route** — `LevelPickerContent` inside `LevelTestResultScreen` | unchanged; it only saves `selected_level` (which Home now reads) and goes Home |
| Onboarding end (`finishOnboarding`, level chip, "{level} ile başla") | `Home` via `popUpTo(Onboarding)`; saves `selected_level` | unchanged; Home now opens on that level (see §2) |
| `PerfTour` (`Home→LevelSelect`, `openLevelSelect()`) | script segment | drop the segment; start the game from Home (`Home→Game`), update `docs/PERF.md`, `ScreensBenchmark` / `LexiJourney` KDoc |
| `AppModulesTest` (`LevelSelectRoute::class` in the route list) | Koin graph check | remove the entry with the route |
| `LevelSelectViewModelTest`, `LevelSelectLayoutTest` | cover level select | port the cases that still apply (progress per level, Classic time, category fallback, tier fit at 360 × 640 / 390 × 844 / 412 × 915) to the Home tests, delete the rest |
| `CategoriesRoute(level, mode)` | opened from the level select row | no entry point once the sheet exists; keep the screen until the sheet ships, then delete `CategoriesRoute` / screen (the sheet reuses `CategoriesViewModel`'s loading, moved to a shared use case) |

`LexiNavigator.openLevelSelect`, `openCategories`, `LevelSelectRoute` and `levelSelectScreen` are deleted in the
cleanup commit (**after** v2 is live; until then 03 stays). Result "TEKRAR OYNA" is unaffected (already `playAgain`).
`GameRoute(level, mode, category?)` is unchanged, so saved games and share links keep working.

## 5. Analytics (names and params must not change)
`app_open`, `onboarding_complete`, `level_test_complete(level)`, `game_start(mode, level)`, `game_finish(...)`,
`joker_used`, `share`, `duel_start(clock)`, `duel_finish`, `reminder_enabled`, `rating_prompt_requested`. None is fired
by the level select today, so removing it removes no event. `game_start` still fires once from `GameViewModel` after the
game is prepared, now straight from Home. New events are **not** added in this task (would need the privacy/consent
review, rule 11 doesn't apply: no SDK change); if wanted later: `home_selection_change(kind)` only with the owner's OK.

## 6. Implementation notes (for the later Kotlin task)
- Home owns the state: `HomeUiState` gets `levels: List<LevelProgressUi>`, `selectedLevel`, `selectedMode`,
  `category`, `classicTimeLimitSeconds`, sheet state; `HomeIntent` gets `SelectLevel`, `SelectMode`, `OpenCategorySheet`,
  `SelectCategory`, `Play`. Module rule 1: `feature:home` must not import `feature:levels` internals — move
  `LevelProgressUi`, level-name strings and the category loading into `:core:domain` use cases + shared UI in
  `:core:designsystem` (or merge the remaining level code into `feature:home`; architect decides, ADR if a module
  changes). The level progress already comes from `Stats.levelProgress`.
- The mode control and chips are the existing level-select components (`LevelSelectDefaults`); the key tile is the game
  keyboard key (56 / 44 dp, `s3`) with a 24 dp `LexiIcons` glyph and a 12 sp label below, whole column = one 44 dp+
  target; pressed state = translate by the shadow (motion.md).
- Tiers: extend `HomeTier` (Regular / Compact / Tight) as in §1; `FitToHeight` scrolls only the part above the pinned
  OYNA + footer. Roborazzi: 390 × 844, 360 × 640, 360 × 640 @ 2.0, 412 × 915 and `HomeLayoutTest` (all texts on screen).
- Home "never scrolls" in `screen-map.md` becomes "scrolls only above OYNA/footer when no tier fits".

## 7. Fill-the-blank (open item — owner decision needed)
`21/22-fillblank-*-proposed` assume a 4th mode segment ("Boşluk doldurma") and a half-width tile row. With v2 there is
no level select, so v1 of the mode cannot "enter via 22". Options: (a) **4-cell mode control** — 4 × ~83 dp cells, label
13 sp, the hint line shortened (`10 cümle`) or dropped; Hardcore / Boşluk doldurma labels need wrapping at ≥ 1.3×;
(b) keep the 3-cell control and put fill-blank in the footer (a 5th tile breaks the equal four) or in the mode
control's own row below as a text link; (c) a separate entry on the Düello-style "other games" sheet. Recommendation:
(a) with a two-row segmented control only if label length requires it, decided when the content scan (§10 of the
proposal) is done. Until then `SelectableModes` stays three entries and `HomeUiState` must not hard-code three cells.

## 8. Accessibility
- **Reading order:** gear → logo ("Lexi") → daily card → streak → "Çarkını kur" heading → hero card → level chips →
  mode control → category row → OYNA → footer (Defter, İstatistik, Sınavlar, Düello). The scroll column and the pinned
  group are separate focus regions; the pinned group is always reachable.
- **Hero card:** one node, not clickable: "B2, İyi orta seviye, 412 / 850 kelime" (EN "B2, Upper intermediate, 38 of
  1400 words"); the bar is decorative (merged into it).
- **Level chips:** a radio group; each "A1, Başlangıç, seçili" / "B2, İyi orta" (`selectable`, role RadioButton, selected
  state from semantics, not color only: ink fill + yellow text + the hero card changing).
- **Mode control:** radio group; cell reads "Klasik, 3 dakika, seçili" / "Pratik, süresiz" / "Hardcore, tek hata".
- **Category row:** button, "Kategori: Karışık, değiştirmek için dokun"; sheet is a dialog with focus trapped, title
  announced, rows are radios "Günlük hayat, 64 / 120 kelime, seçili değil"; back / scrim closes and returns focus to the row.
- **OYNA:** "Oyna, A1, Klasik, 26 harf" (the badge is merged, not read twice).
- **Footer tiles:** buttons named by their label; Defter "Defter, 24 tekrar bekliyor" (plural, none ⇒ "Defter, tekrar
  yok"), the badge is hidden from semantics; the icons are decorative. İstatistik, Sınavlar ("Sınav paketleri"),
  Düello ("Düello, iki oyuncu, tek telefon").
- Daily card: one node "Günün çarkı, 16 Kasım, 7 gün seri, bugünkü hedef 0 / 1" (existing `home_daily_goal_desc*` strings);
  the 9 sp label is below the usual minimum, so it must scale with font size (it does, uncapped) and carries nothing
  that the node description lacks.
- Targets: chips ≥ 48, key tiles ≥ 44 × 44 (whole column), category row 48, mode cells ≥ 48, gear 44.
- Reduced motion: chip / mode selection is a color swap, the sheet slides per motion.md and just fades when reduced.
