# Dead Ends Reachable by Testers

> Audit of tappable elements that do nothing, elements showing a "YAKINDA" chip, and unfinished screens across all feature modules.
> Generated for milestone QA readiness. Do not fix these items in this branch; follow the recommendation per row.

| # | File | Element | Current Behaviour | Recommendation |
|---|------|---------|-------------------|----------------|
| 1 | `feature/home/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/home/HomeScreen.kt` | "Sınav paketleri" MenuTile (`home_tile_exams`) | Opens `ExamPacksScreen`, which is an unfinished placeholder screen showing only an empty state. | keep as "yakinda" |
| 2 | `feature/home/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/home/ExamPacksScreen.kt` | ExamPacksScreen placeholder | Displays `EmptyState` ("YAKINDA: LGS, YKS-Dil ve YDS çarkları hazırlanıyor.") without any playable exam packs. | keep as "yakinda" |
| 3 | `feature/home/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/home/HomeScreen.kt` | Daily card (AlreadyPlayed / Unavailable states) | Tappable card layout with `onClick = {}` and `enabled = false`; tapping does nothing. | keep as "yakinda" |
| 4 | `feature/settings/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/settings/SettingsScreen.kt` | Lexi Pro banner (`ProBanner`) | Displays a "YAKINDA" chip; non-tappable card that does not open a paywall or purchase flow. | keep as "yakinda" |
| 5 | `feature/settings/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/settings/SettingsScreen.kt` | "Günlük hatırlatıcı" row (`settings_daily_reminder`) | Displays "YAKINDA" chip with disabled alpha; non-interactive row. | keep as "yakinda" |
| 6 | `feature/settings/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/settings/SettingsScreen.kt` | "Tekrar hatırlatıcıları" row (`settings_review_reminders`) | Displays "YAKINDA" chip with disabled alpha; non-interactive row. | keep as "yakinda" |
| 7 | `feature/settings/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/settings/SettingsScreen.kt` | "Uygulama dili" row (`settings_language`) | Displays "YAKINDA" chip with disabled alpha; non-interactive row. | keep as "yakinda" |
| 8 | `feature/settings/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/settings/SettingsScreen.kt` | "İpucu dili" row (`settings_clue_language`) | Displays "YAKINDA" chip with disabled alpha; non-interactive row. | keep as "yakinda" |
| 9 | `feature/settings/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/settings/SettingsScreen.kt` | "Satın alımları geri yükle" footer text (`settings_footer_restore`) | Styled inline with clickable footer links, but rendered as static text with no `onClick` handler. | hide |
| 10 | `feature/game/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/game/components/JokerSheet.kt` | "Kısa reklam izle · +1 joker" button (`game_jokers_ad`) | Button has `onClick = {}`; gated behind `FeatureFlags.rewardedAdJokers` (default false), but does nothing when shown. | hide |
| 11 | `feature/game/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/game/result/ResultScreen.kt` | Result screen "Paylaş" button (`ResultIntent.Share`) | Button does nothing until B3/M6; currently hidden behind `SHARE_AVAILABLE = false // TODO(B3)`. | hide |
| 12 | `feature/onboarding/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/onboarding/OnboardingScreen.kt` | "SEVİYE TESTİNE BAŞLA" button on LevelChoice step | Resolved (B5/B6): opens the level test intro, then the test and its result. | — |
| 13 | `feature/levels/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/levels/CategoriesScreen.kt` | Empty category state (`categories_empty`) | When a level has no categories, shows `EmptyState` without any action other than navigating back. | keep as "yakinda" |
| 14 | `feature/notebook/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/notebook/NotebookScreen.kt` | Empty notebook state (`notebook_empty_*`) | When notebook has no words, shows `EmptyState` without any call-to-action button to start playing. | fix |
| 15 | `feature/stats/src/commonMain/kotlin/dev/talhabayhan/lexi/feature/stats/StatsScreen.kt` | Empty stats state (`stats_empty`) | When user has not played any game, shows `EmptyState` without any call-to-action button to start playing. | fix |
