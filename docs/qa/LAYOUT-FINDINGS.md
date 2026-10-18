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
