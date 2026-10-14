# Screen map — design ↔ code

| Design file | Canvas name | Route / composable | Module | Milestone |
|---|---|---|---|---|
| `14-splash` | Splash animasyonu | `SplashRoute` / `SplashScreen` | composeApp | M4 |
| `01-onboarding` | Onboarding / seviye testi (step 2 of 3) | `OnboardingRoute` | feature:onboarding | M6 |
| `02-home` | Ana menü | `HomeRoute` / `HomeScreen` | feature:home | M4 |
| `03-level-select` | Seviye ve mod seçimi | `LevelSelectRoute` | feature:levels | M4 |
| `07-categories` | Seviye kategorileri | `CategoriesRoute(level)` | feature:levels | M5 |
| `04-game` | Oyun ekranı | `GameRoute(setup)` / `GameScreen` | feature:game | M4 |
| `05-joker-sheet` | Joker seçimi | `JokerSheet` (bottom sheet inside GameScreen) | feature:game | M4 |
| `06-result` | Oyun sonu | `ResultRoute(sessionId)` | feature:game | M4 |
| `08-notebook` | Kelime Defterim | `NotebookRoute` | feature:notebook | M5 |
| `09-stats` | İstatistikler | `StatsRoute` | feature:stats | M5 |
| `10-exam-packs` | Sınav paketleri | `ExamPacksRoute` | feature:exams | M7 |
| `11-settings` | Ayarlar | `SettingsRoute` | feature:settings | M7 |
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
| Pause menu (dialog) | M4 | `ConfirmDialog` style: Devam et / Yeniden başla / Ayarlar / Bırak |
| Onboarding steps 1 & 3 | M6 | same layout as step 2, different shapes & copy |
| Level test result ("Seviyen: B1") | M6 | result screen header style + level badge |
| Daily wheel intro / already-played state | M6 | home daily card enlarged |
| Word detail sheet | M5 | bottom sheet: word, tr, example, speaker, SRS box, remove |
| Review wheel result | M5 | reuse result screen |
| Paywall (Lexi Pro) | M7 | ink card + yellow diamond from settings; 3 plans as level-card style |
| Notification permission pre-prompt | M7 | onboarding layout |
| Rating prompt | M8 | `ConfirmDialog` |
| Offline / error / empty states | M5–M7 | `EmptyState` |
| Hot-seat duel: player setup, handover card, duel result | M8 | big ink handover card with player name |
| Online rooms (v1.1) | — | see `MULTIPLAYER.md` §4 — **needs design** |
| Store screenshots | M8 | frames of real screens + headline band |

## Responsive rules
- Phones narrower than 390dp: wheel size = `min(320, screenWidth − 40)`; scale wheel radius/chip proportionally;
  everything else reflows (keys stay 5 columns).
- Short phones (< 760dp height): reduce wheel to 280 and vertical gaps by 25%; never scroll the game screen.
- Tablets / landscape: center a 430dp-wide column; landscape-specific layouts later.
- Font scale: honor system font size up to 1.3× on lists; game keys, slots and wheel are capped at 1.15×.
