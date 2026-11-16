# Home v2 (`02c-home-v2`) — interaction rules and implementation plan

Status: **implemented** (ADR 0026; the board is the golden reference, goldens recorded on CI). Visual truth: `screens/02c-home-v2.png` (+ `.html`, source
`source/02c-home-v2.dc.html`). Palette/sizes are `tokens.md` (the older `02-home` HTML still carries the pre-contrast
hexes; use the tokens). Level, mode and category are chosen **on Home**; `03-level-select` is superseded once this ships.
New pieces marked **needs design**: the four footer icons (book, bar chart, graduation cap, two people — add to
`LexiIcons`), the footer key tiles, the due-reviews badge, the level chips row, the category bottom sheet, the
disabled/hidden category state. Their values are from the board; everything else reuses `16-components`.

> The owner's draft `docs/design/drafts/dashboard-d2.png` (panel "D2") was **not in the repo or on this machine**, so
> the board was drawn from the written brief. Commit the draft next to it and compare once.

## 1. Layout and tiers (360 × 640 at 100 %, 130 %, 200 %)
Order: header (logo 36 + tagline 11 sp 0.2em, gear) → daily card → `ÇARKINI KUR` (hero card, 6 chips, mode control,
category row) → **OYNA right under the category row** → free space → **footer tiles pinned at the bottom**. Tiers
(`HomeTierSize` + `HomeLayout`, DESIGN-DEBT below regular, measured by `FitToHeight`, first that fits inside the
safe-drawing insets wins; sizes in dp):

| Tier | Padding T / X / B | Block gap / build gap | Logo | Daily padding V / H | Hero min | Chip | Mode | Category | OYNA | Key |
|---|---|---|---|---|---|---|---|---|---|---|
| Regular (390 × 844, as drawn) | 24 / 20 / 28 | 16 / 10 | 36 | 8 / 12 | 76 | 48 | 56 | 48 | 68 | 56 |
| Compact (360 × 640 drawn) | 8 / 20 / 12 | 8 / 6 | 28 | 6 / 12 | 56 | 48 | 48 | 48 | 52 | 44 |
| Tight | 8 / 16 / 12 | 8 / 6 | 28 | 6 / 10 | 56 | 48 | 48 | 48 | 52 | 44 |

- Order tried: Regular → Compact → Tight, each with OYNA under the category row and nothing scrolling. If even Tight
  does not fit (tiny screen / huge font, board frame 08), **OYNA is pinned just above the footer** and the part above it
  scrolls (`HomeLayout.TightPinned`), so OYNA is never off screen.
- The spare height sits between OYNA and the footer (nothing grows). The daily card v2 saved ~39 dp at 360 × 640
  (Compact: ~88 → ~49 dp); that space now shows under OYNA.
- Chips and key tiles never go below 48 / 44 dp touch height; chip row is a 6-column grid, gap 6.
- Font scale: scrolling text follows the system scale up to 2.0. **Pinned and control text (OYNA, badge, footer
  labels, chips, mode cells, hero count, daily badge, tagline) is capped at 1.3**, the daily card's text at 1.5.
- Hero card: long names wrap on the left; the count and the 12 dp bar stay in the right column (width 132 × min(scale,
  1.3)).
- Footer badge = due reviews (`dueReviews`): blue circle 22 dp, 2 dp ink border, 11 sp white; hidden at 0, `99+` from 100.
  Blue chosen over red because red means "wrong" in this app; flip if the owner prefers.
- OYNA is inset by its 6 dp shadow, the hero card is not (as on the board). Decoration: the blue circle (112 dp) is
  centred on the screen's right edge at the daily card's centre, the red half-pill on the left edge at the footer's
  centre with 16 dp showing, so neither sits behind a chip or the hero card. Level chip labels shrink to 70 % before
  they would touch the frame.

### Daily card (owner feedback, replaces the first version)
Left: title `home_daily_card_title` "Günün çarkı" / "Daily wheel" (16/800) over a description (12/600). Right: ONE
status badge (white, 3 dp ink border, min 32 high, padding 0 8, gap 6). About 56 dp high in the regular tier; the
completed state may grow to ~64 dp when its line wraps.

| State | Card | Description | Badge | Content description |
|---|---|---|---|---|
| Not played (`Available`) | yellow, s6, button | `home_daily_title` "Herkes aynı çarkı çözüyor" | empty 14 dp ring + `home_daily_badge_solve` "Çöz" / "Solve" + chevron | `home_daily_desc_solve` "Günün çarkı, herkes aynı çarkı çözüyor, çözülmedi" |
| Played today (`AlreadyPlayed`) | `yellowMuted` #ECC98E, s6, not clickable | `home_daily_done_hours_minutes` / `_minutes` "1840 puan · yeni çark 5 sa 12 dk" | filled ink circle with a check + `home_daily_badge_done` "Tamamlandı" / "Completed", no chevron | `home_daily_desc_done` "Günün çarkı, tamamlandı, 1840 puan" |
| Unavailable | `disabledFill`, muted text, no shadow | `home_daily_unavailable` "Şu an kullanılamıyor" | none | `home_daily_desc_unavailable` |

- When the text column would get narrower than 140 dp (320 dp phones, large fonts) the badge drops its word and keeps
  the ring / check (and the chevron); the content description always carries the full text.
- Removed from Home's UI: the month label, the day-number circle, the streak pill and the daily-goal line (their
  strings are deleted). The streak, daily goal, 7-day gift bonus and its notice still run unchanged in the view model;
  Statistics shows only the **longest** streak, so the current streak is no longer visible anywhere (owner to decide).

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
Row `Kategori  <seçim> ▾` (48 dp) opens a bottom sheet (`BottomSheet`: scrim 55 %, cream, 4 dp top border, a 40 × 4
grab handle, as tall as its content up to **85 %** of the screen; title, close button and hint stay, the cards scroll
under them with a 24 dp cream fade at the bottom while more is below). Board frame 04. Content = the old
`07-categories` cards (`CategoryCard`, `MixedWheelCard`):
- First the full-width **"Karışık çark"** card (yellow circle, "Tüm <LEVEL> kategorilerinden 26 kelime", the level's
  word total on the right; red, ink when selected).
- Then the level's categories in **two columns** (gap 10, cards ≥ 92 dp, 3 dp frame, s3): red square / yellow circle /
  blue leaf cycling down the list, name (2 lines), a 6 dp bar and `learned / total`; complete = blue bar + "· tamam".
  Same data and exclusions as `07-categories` (no virtual `mixed`, no `general`).
- **One column** from font scale 1.3 or when a card would get narrower than 140 dp.
- **Selected:** ink card, yellow name, yellow fill, white bar outline, a 20 dp yellow check badge on the top-right
  corner (Karışık selected ⇒ its card is ink instead of red).
- A tap only **selects** and closes the sheet; OYNA starts the game. The row shows the selection.
- Accessibility: a radio group; each card reads "<name>, <learned> / <total> kelime" with selected / not selected.
- Read failure ⇒ `EmptyState` with "Tekrar dene" and the row falls back to Karışık (play still works). System back,
  the scrim and × close the sheet.
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
- Tiers: `HomeTierSize` (Regular / Compact / Tight) and `HomeLayout` as in §1; `FitToHeight` scrolls only when OYNA had
  to be pinned above the footer. Roborazzi: 390 × 844, 360 × 640, 360 × 640 @ 2.0, 412 × 915 and `HomeLayoutTest` (all texts on screen).
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
- **Reading order:** gear → logo ("Lexi") → daily card → "Çarkını kur" heading → hero card → level chips →
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
- Daily card: one node read as its content description (see §1 "Daily card"); a button only while unsolved.
- Targets: chips ≥ 48, key tiles ≥ 44 × 44 (whole column), category row 48, mode cells ≥ 48, gear 44.
- Reduced motion: chip / mode selection is a color swap, the sheet slides per motion.md and just fades when reduced.
