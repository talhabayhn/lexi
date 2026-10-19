# Layout Findings (Task M8.3)

Layout defects identified across small-screen (320x568, 360x640), large-font (fontScale 1.3), and tablet (600x960 portrait) visual passes.

| Screen | Size | Defect | Fixed |
|---|---|---|---|
| JokerSheet | 320x568 @ 1.3, 360x640 @ 1.3 | Non-scrollable sheet content does not fit viewport height; 4th joker card is pushed off-screen and 3rd card is clipped | yes |
| JokerSheet | 600x960 | Bottom sheet panel stretches awkwardly to full screen width instead of being constrained to content width | yes |
| PauseDialog | 600x960 | Dialog panel stretches awkwardly to full screen width instead of being constrained to content width | yes |
| LevelSelectScreen | 320x568 @ 1.3, 360x640 @ 1.3 | Fixed 150dp height in LevelCard causes subtitle and progress text to overflow, clipping text and overlapping progress bar | yes |
| CategoriesScreen | 320x568 @ 1.3, 360x640 @ 1.3 | Fixed 92dp height in CategoryCard causes 2-line wrapped title to push progress caption out of card, clipping text at bottom border | yes |
| ResultScreen | 320x568 @ 1.3 | Review section header title "Tekrar edilecekler" and subtitle "2 kelime defterine eklendi" collide and overlap without spacing | yes |
| NotebookScreen | 320x568 @ 1.3 | "Tekrar çarkını başlat" review button has fixed height, pushing wrapped text ("başlat") through the bottom border | yes |
| StatsScreen | 320x568 @ 1.3, 360x640 @ 1.3 | Week card header title "Bu hafta öğrenilen kelime" and subtitle "toplam 177" collide, forcing total to wrap into a 1-character vertical column | yes |

## fontScale 2.0 pass (2026-10-19)

Reproduced with Roborazzi at fontScale 2.0 on 390x844 and 320x568 (Home, Game, Result, Daily card, Notebook, Settings,
Onboarding, About, joker sheet). The game board and pause dialog, the About screen and onboarding steps 1–3 and the
level picker had no defect. A row whose trailing chip would leave the text column narrower than its longest word now
puts the chip under the text (`AdaptiveTrailingRow`): words wrap, they are never broken.

| Screen | Size | Defect | Fixed |
|---|---|---|---|
| JokerSheet | 390x844 @ 2.0, 320x568 @ 1.0–2.0 | The tall sheet ran under the "Süre işlemeye devam ediyor" pill and hid the title and first joker | yes (sheet stops below the pill and scrolls) |
| JokerRow | 320x568 @ 2.0 | The cost chip squeezed the title and description until words broke ("Çeldi-riciler") | yes (chip under the text) |
| ResultScreen | 390x844 @ 2.0, 320x568 & 360x640 @ 1.3 | "Tekrar edilecekler" broke into letter pairs next to the "eklendi" caption | yes (caption under the title) |
| HomeScreen menu tiles | 320x568 @ 2.0 | "İstatistikler" broke across lines in a half-width tile | yes (one tile per row when a title word does not fit) |
| DailyCard | 320x568 @ 2.0 | The title was cut after its second line without a sign | yes (ellipsis) |
| Settings rows, ProBanner | 320x568 @ 2.0 | The "YAKINDA" chip / value broke labels ("hatırlat-ma", "Le-xi Pro") | yes (chip under the label) |
| Notebook SegmentedControl | 390x844 @ 2.0 | Tab counts were clipped ("Tekrar ·") | yes (segments wrap and grow) |
| SearchField, LexiButton | 390x844 @ 2.0 | A long placeholder or label was cut mid-glyph | yes (ellipsis) |
| Onboarding level choice | 390x844 @ 2.0 | "Kendim seçeceğim" was clipped at the button edge | yes (ellipsis) |
