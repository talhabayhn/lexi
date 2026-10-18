# Screen map — design ↔ code

| Design file | Canvas name | Route / composable | Module | Milestone |
|---|---|---|---|---|
| `14-splash` | Splash animasyonu | `SplashRoute` / `SplashScreen` (visuals: `LexiSplash` in designsystem) | shared | M4 (done) |
| `01-onboarding` | Onboarding / seviye testi (step 2 of 3) | `OnboardingRoute` | feature:onboarding | M6 |
| `02-home` | Ana menü | `HomeRoute` / `HomeScreen` | feature:home | M4 (done; tiles of M5-M7 are inert) |
| `03-level-select` | Seviye ve mod seçimi | `LevelSelectRoute(mode)` / `LevelSelectScreen` (`mode` = the preselected mode; Home's practice tile passes PRACTICE) | feature:levels | M4 (done) |
| `07-categories` | Seviye kategorileri | `CategoriesRoute(level, mode)` / `CategoriesScreen` (`mode` = the level select's mode, passed on to the game) | feature:levels | M5 (done) |
| `04-game` | Oyun ekranı | `GameRoute(level, mode, category?)` / `GameScreen` | feature:game | M4 (done) |
| `05-joker-sheet` | Joker seçimi | `JokerSheet` (bottom sheet inside GameScreen; ad button hidden behind `FeatureFlags.rewardedAdJokers` until M7) | feature:game | M4 (done) |
| `06-result` | Oyun sonu | `ResultRoute(summary)` / `ResultScreen` (summary payload: ADR 0019 addendum; Paylaş inert until M6) | feature:game (`result`) | M4 (done) |
| `08-notebook` | Kelime Defterim | `NotebookRoute` / `NotebookScreen` (review wheel: `GameRoute.review()`) | feature:notebook | M5 (done) |
| `09-stats` | İstatistikler | `StatsRoute` | feature:stats | M5 |
| `10-exam-packs` | Sınav paketleri | `ExamPacksRoute` / `ExamPacksScreen` (M5: header + "YAKINDA" `EmptyState` only, in feature:home until feature:exams is wired) | feature:home → feature:exams | M7 |
| `11-settings` | Ayarlar | `SettingsRoute` / `SettingsScreen` (Home gear icon; back pops) | feature:settings | M5 (switches), M7 (rest) |
| — | Hakkında | `AboutRoute` / `AboutScreen` (Settings footer link; back pops) | feature:settings | M8 (done) |
| `12-share-card` | Paylaşım kartı (432×540) | `ShareCardRenderer` (renders to bitmap) | feature:home / core:designsystem | M6 |
| `13-logo-and-icon` | Logo ve uygulama ikonu — use **option 1** for app icon | `LexiLogo`, launcher icons | designsystem, platform res | M1 |
| `15-motion` | Animasyonlar | `LexiMotion`, effects | designsystem | M1/M4 |
| `16-components` | Bileşen kütüphanesi | components.md | designsystem | M1 |

The `.dc.html` sources still link to each other by their canvas names (e.g. `BauhausMenu.dc.html` = `02-home`).

## Design debt — screens without a design yet
Build from `16-components` only, mark `// DESIGN-DEBT` and list in the PR. Product owner may request a design
before shipping.

| Screen | Milestone | Hint |
|---|---|---|
| Pause menu (dialog) — **needs design** (stays as built, DESIGN-DEBT) | M4 | built as `PauseDialog` (feature:game) from the `ConfirmDialog` panel (`DialogPanel`): "Oyun duraklatıldı" with a vertical stack of 48dp buttons, gap 10: DEVAM ET (red, s4) / YENİDEN BAŞLAT / AYARLAR / PES ET (white, flat). YENİDEN BAŞLAT → "Yeniden başlatmak istiyor musun?" Başlat / Vazgeç: the paused game is dropped unrecorded and a new wheel starts with the same level, mode, category and time limit (new seed); hidden in Daily, level test and duel (like TEKRAR OYNA). AYARLAR opens `SettingsRoute` over the paused game; back returns to the dialog. PES ET → "Pes etmek istiyor musun?" Pes et / Vazgeç (owner decision: "Pes et" because the button gives up and shows the result, unlike `16-components`'s "Bırak") |
| Example sentence of the "Örnek cümle" joker | M4 | italic line under the clue in `ClueCard` |
| Result without misses (stays as built, DESIGN-DEBT) | M4 | `EmptyState` instead of the review list |
| Result screen grouped review (DESIGN-DEBT) | M6 | `06-result` was extended with grouped review: lists all 26 words grouped as Doğru / Yanlış / Pas / Süre bitti with a per-row notebook button for "Süre bitti" words |
| Joker sheet clock pill over the live game | M4 | `05-joker-sheet` shows the pill on a dimmed screen; in the app it is drawn above the scrim at the top, over the dimmed but still running HUD timer, so the time is shown twice |
| Combo badge position in the game | M4 | `15-motion` #7 shows badge + burst alone; in the game they sit in the HUD row, vertically centred on it and right-aligned next to the score circle (over the empty end of the timer bar; the strip under the HUD is too thin), so the badge never covers the centre letter or wheel chips (product owner decision); badge at its resting −4°. The burst shapes (±100dp) may briefly pass over the top chips |
| Result after a Daily (no TEKRAR OYNA) | M4 | deviation: the Daily allows one scored attempt per day, so TEKRAR OYNA is hidden and Paylaş takes the full row |
| Result TEKRAR OYNA target | M4 | deviation: the design sends TEKRAR OYNA to level select; the app starts a new game at once with the same level, mode and category (product owner decision) |
| Joker sheet ad button | M4 | shipped look has no "Kısa reklam izle · +1 joker" button (`FeatureFlags.rewardedAdJokers`, off until M7); `05-joker-sheet.png` shows it |
| Daily joker gift notice | M4 | none yet: `GrantDailyJokers` returns the grants (type + reason) for a later toast; today only the joker counts change |
| Disabled game inputs (wrong-answer reveal) | M4 | keys / backspace / joker at 50 % without shadow (joker sheet's disabled rows), PAS = `LexiButton` disabled |
| Onboarding steps 1 & 3 | M6 | same layout as step 2, different shapes & copy |
| Level-choice screen & picker (DESIGN-DEBT) | M6 | built from 16-components (D4/D5 area): level choice with single CTA when test button is hidden behind LEVEL_TEST_AVAILABLE = false (TODO B5), simple A1..NAT picker built from existing level-select components (2×3 LevelCard grid) |
| Level test result ("Seviyen: B1") | M6 | result screen header style + level badge |
| Daily wheel intro / already-played state (stays as built, DESIGN-DEBT) | M6 | home daily card enlarged. The 3 home daily-card states (Available, AlreadyPlayed with score and countdown to next wheel, Unavailable) were built from `16-components` without dedicated Figma screens; flagged as DESIGN-DEBT (needs design). |
| Entry to the categories | M5 | the design has no way in (07's back arrow returns to level select): a small underlined "Kategoriler" link on the "OYUN MODU" label line of the level select (`LexiTypography.link12`; the touch target is widened by Compose). Starting a category starts `GameRoute(level, mode, category)`; the mixed card starts the level's regular wheel |
| Categories: Pro lock, empty level, read failure | M5 | none in the design: no shipped category is Pro (TODO M7: lock behind the paywall); a level without categories shows `EmptyState` instead of the cards; unreadable content shows the `EmptyState` with "Tekrar dene" instead (no card can be started) |
| Word detail sheet | M5 | built (stays as built, DESIGN-DEBT): `BottomSheet` over the notebook, opened by tapping a row: word as title, Turkish clue, level · category, example + translation, memory box and next review, seen / correct / wrong, ink "Dinle" button (speaker), white "Defterden çıkar" (no confirmation) |
| Notebook empty states, review button | M5 | no design: `EmptyState` per tab / search; a failed read (notebook or word content) shows the `EmptyState` with "Tekrar dene" in place of the list, never the empty notebook; the blue "Tekrar çarkını başlat" button is hidden while nothing is due (the design always shows it); it shows on every tab |
| Notebook: placeholder font weight | M5 | the design's search placeholder is a thinner (browser default 400) Jost; the app has no Regular, so it uses Medium and is ~5 % wider |
| Stats empty / error states | M5 | no design: `EmptyState` in place of the cards when no wheel was played yet (no stored session and no answered card) or the read failed (then with "Tekrar dene"); the hardest-letters section shows an `EmptyState` until a letter has 5 answers; a level without cards is hidden |
| Stats: hardest letters overflow | M5 | the design's four chips are wider than the screen (the 4th is cut at the edge); built as a sideways scrolling row, cut at the 20dp padding instead of the screen edge; no accuracy yet shows "–" |
| Settings: rows of M7 | M7 | built: sound, haptics, speak-on-correct, classic time (cycles 120/180/240/300s, wired to GameConfig in GameViewModel), daily goal (cycles 1/2/3/5 wheels, stored in settings, wired to Home daily goal chip), and accent (cycles US/UK, wired to Speaker/TTS) are live with design values and chevrons (D12); Pro card (waits for M7.4 / RevenueCat), daily reminder and notebook review reminders (wait for M7.2 scheduler & pre-prompt), UI language (CMP has no clean runtime locale switching; follows system language), and clue language (only TR in content) stay YAKINDA; page scrolls and footer links are plain text |
| Home daily goal progress chip (DESIGN-DEBT) | M7 | built from 16-components: progress chip under daily card showing "Bugün {done}/{goal}" (reached: yellow filled chip with checkmark), refreshes on return from game and at midnight |
| Paywall (Lexi Pro) | M7 | ink card + yellow diamond from settings; 3 plans as level-card style |
| Notification permission pre-prompt | M7 | onboarding layout |
| Rating prompt | M8 | `ConfirmDialog` |
| Untimed / unscored game HUD (Practice, Review) | M5 | built: the timer bar stays empty with "SÜRESİZ" in place of the clock; the score badge counts correct answers (unscored modes score 0, content description "Doğru: N"); point-cost jokers show "ücretsiz" |
| Result of unscored modes (Practice, Review) | M5 | built: the hero number is the correct answers with "doğru" instead of "puan"; no "kalan süre" |
| Game load errors | M5 | built: content or saved game unreadable ⇒ `EmptyState` with "Tekrar dene" (secondary button) and a "Geri dön" link at the top of the game screen (over the decor); a level / category without any card ⇒ the empty state with only "Geri dön" (nothing is recorded) |
| Result: review list read failure | M5 | built: `EmptyState` with "Tekrar dene" in place of the "Tekrar edilecekler" list; the counts stay |
| Level select: read failure | M5 | built: unreadable card totals ⇒ the `EmptyState` with "Tekrar dene" between the title and the level cards (they show 0 / 0, the page scrolls); BAŞLA still works, the game screen reports content it can't read |
| Offline / error / empty states | M5–M7 | `EmptyState`; with an action (retry / back) it is `EmptyState(message, actionText, onAction, secondaryText?)`: the empty state over a full-width secondary `LexiButton`, gap 16, optional `LexiTextLink` below |
| Hot-seat duel: player setup, handover card, duel result | M8 | big ink handover card with player name |
| Online rooms (v1.1) | — | see `MULTIPLAYER.md` §4 — **needs design** |
| Store screenshots | M8 | frames of real screens + headline band |
| About / licences screen (DESIGN-DEBT) | M8 | built from 16-components: app identity, version name + build number, neutral tagline, legal & support links, in-house content source statement, and open-source licenses list |

## Responsive rules
- **Expanded tier (all screens, `LexiViewport` around the nav host):** when the space inside the system bars and cutout
  is larger than the 390 × 844 design in **both** directions (e.g. Android "smallest display size": 480 × 1000dp at a
  low density), every screen is drawn as the design scaled by `min(width / 390, height / 844)` (at most 1.6×) through
  a scaled `LocalDensity`: wheel, keys, tiles, texts, gaps, borders, shadows and motion distances grow together, so no
  hole opens between OYNA and the tiles or between the keys and the actions. Phones at or below the design size keep
  scale 1 (390 × 844 stays pixel-identical); the IME never changes the scale. Every screen's content column is centred
  and at most 430dp (× scale) wide (`Modifier.lexiContentWidth()`); decor shapes stay edge-to-edge. Roborazzi + bounds
  tests at 480 × 1000, 600 × 1100, 600 × 960 (home, game) and 600 × 1100 (the other screens). On iOS the safe-area
  insets are dp-based and grow with the scale (only on viewports above 390 × 844, i.e. iPads).
- Taller than the design's proportions after scaling (scale limited by the width, e.g. 400 × 1000): the home's tile
  rows grow (up to +55dp each) once the OYNA–tiles space exceeds the design's (131dp, cap 136); the game's four row
  gaps grow up to 2× and the column is then centred, the actions staying 12dp under the reserved keys.
- Phones narrower than 390dp: wheel size = `min(320, screenWidth − 40)`; scale wheel radius/chip proportionally
  (chip, centre letter and rule label texts too); everything else reflows (keys stay 5 columns).
- Short phones (< 760dp height): reduce wheel to 280 and vertical gaps by 25%; never scroll the game screen.
- **Game layout is computed once** (`gameLayout` in feature:game) from the space inside the safe-drawing insets and
  the capped font scale, never from the turn, so a new clue, answer or key set moves nothing:
  - Clue card: fixed height = chrome (borders, padding, one-line label) + a clue area of one 30sp line (the design).
    The clue (max 3 lines) and the joker's example (max 2 lines, never larger than the clue) shrink in 1sp steps
    down to 12sp / 11sp until they fit, centred vertically; below that they are cut with an ellipsis. Height left
    over with the wheel at its maximum (tall phones, e.g. 412 × 915) widens the clue area by up to one more 30sp line.
  - Keys: three rows (15 keys) are always reserved under the answer boxes, with at least 12dp (scaled with the gaps)
    to the actions; a two-row turn leaves the third row empty. At 390 × 844 this is exactly the design's free space,
    so the design stays pixel-identical (wheel 320, card 420–498, boxes 518, keys 582, actions 760).
  - The wheel gets what is left, up to its maximum. Tiers: regular (≥ 760dp) → short → compact, each taken only when
    the previous one leaves the wheel under 240dp. Compact (very short phones, e.g. 320 × 568, 360 × 640;
    DESIGN-DEBT, derived from `04-game`): padding 12 top / 16 bottom, gaps 8, answer margin 2, HUD 44, answer boxes
    40 high, keys 44 high with 6 gaps, actions 48. At 320 × 568 the wheel is ~144dp.
- **Home never scrolls.** `FitToHeight` (feature:home) measures the home for each tier (`HomeTier`) inside the safe-drawing
  insets and keeps the first that fits: Regular (the design, 390 × 844 pixel-identical; the spacer absorbs spare height
  up to the design's, then the tiles grow), Compact (DESIGN-DEBT, derived from `02-home`: padding 12 / 16, gaps 12, logo blocks 46, daily card padding 12 with a
  48 circle, OYNA 60, tiles min 84 with gap 10), Tight (padding 8 / 12, gaps 8, OYNA 52, tiles content-sized with
  16 / 12sp text). Both tile rows have equal heights. Font scale is capped at 1.15 on the home; decor shapes stay
  edge-to-edge decoration. `HomeLayoutTest` asserts all texts stay on screen at 320 × 568, 360 × 640, 360 × 780,
  390 × 844, 412 × 915 and font scale 1.15 / 1.3.
- Tablets / landscape: the 430dp content column above (scaled in portrait by the Expanded tier); landscape-specific
  layouts later.
- Font scale: honor system font size up to 1.3× on lists; the whole game board (HUD, wheel, clue, slots, keys,
  actions) is capped at 1.15×; its fixed heights grow with the capped scale.
