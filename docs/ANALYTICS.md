# Analytics events (draft — finalize in M7)

Only after consent where required. No personal data; no free text.

| Event | Params |
|---|---|
| `app_open` | `first_open` |
| `onboarding_complete` | `method` (test/manual), `level` |
| `game_start` | `mode`, `level`, `category` |
| `game_end` | `mode`, `level`, `score`, `correct`, `wrong`, `passed`, `completed`, `duration_s` |
| `joker_used` | `type`, `level` |
| `rewarded_ad` | `placement`, `result` |
| `paywall_view` / `purchase` | `source` / `product`, `result` |
| `daily_played` | `level`, `score` |
| `share` | `kind` |
| `review_wheel` | `cards`, `correct` |
| `duel_end` | `type` (hotseat/online), `result` |
