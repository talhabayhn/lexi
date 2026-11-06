# ADR 0024: In-app rating request through the official store APIs

- Status: Accepted
- Date: 2026-11-07

## Context
The v1 groundwork (`RatingPromptPolicy`, `RatingPromptDialog`) asked "Enjoying Lexi?" first and only sent happy players
to the store. Both stores forbid gating a review request on sentiment, and Apple expects the system prompt itself.
Review state (rated, refused) is also unknowable: neither API tells whether a prompt was shown or answered.

## Decision
- **Interface in `core:domain`**: `ReviewRequester.requestReview(): Boolean` (true = the OS API was invoked) and
  `storePageUrl` (the listing, for the About row). Fake in `core:testing`; real classes in `core:platform`.
- **Android**: `com.google.android.play:review` **2.0.2** (`ReviewManagerFactory`, `requestReviewFlow` + `launchReviewFlow`
  on the current activity, results wrapped into coroutines; no `-ktx`, no `play-services-tasks` await). minSdk of the
  library is 23 (< our 26); it brings only Play Core common/tasks, no Compose or AndroidX conflict. Needs the
  Play-installed app to show anything.
- **iOS**: `SKStoreReviewController.requestReviewInScene` on the foreground-active window scene (StoreKit system
  framework, no dependency). Shown only in release/TestFlight builds; the OS rate-limits it (3 times / 365 days).
- **No custom pre-question dialog.** `RatingPromptDialog`, its strings and goldens are removed; `RatingPromptState`
  (rated/refusal counters) is removed because the state is unknowable.
- **Policy** (`RatingPromptPolicy`, pure): >= 5 completed real games, >= 3 days since install, the latest real game
  completed (not forfeit, time-up or Hardcore failure), not level test or duel, >= 120 days since our last request.
  Persisted as `Settings.firstLaunchMs` and `Settings.ratingLastRequestMs` (DataStore). The request is made from Home
  after the player returns from a real game (the reminder pre-prompt takes precedence; never both).
- Analytics `rating_prompt_requested` (no parameters) behind the consent gate.

## Consequences
One new dependency (Android only). No store declaration changes: no new data collected or shared, the Play review
library adds no permissions. An explicit "Rate Lexi" row (About) opens the store listing for players who want to rate.
