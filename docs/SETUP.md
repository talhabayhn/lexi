# Setup (manual console steps)

Keep this file updated by the `platform-integrator` agent. Secrets go to `local.properties` (see `local.properties.example`), never to git.

| Service | When | Steps | Keys |
|---|---|---|---|
| Firebase (Analytics, Crashlytics) | M7 | create project, add Android app `dev.talhabayhan.lexi` + iOS app, download config files (git-ignored) | `google-services.json`, `GoogleService-Info.plist` |
| RevenueCat | M7 | create project, products `lexi_pro_monthly`, `lexi_pro_yearly`, `lexi_pro_lifetime`, entitlement `pro` | `REVENUECAT_ANDROID_KEY`, `REVENUECAT_IOS_KEY` |
| AdMob | M7 | app + rewarded unit (+ interstitial), UMP consent message for EEA/UK/TR | `ADMOB_APP_ID_*`, `ADMOB_REWARDED_*` |
| Supabase | v1.1 | project, anonymous auth on, Realtime on, run `supabase/migrations` | `SUPABASE_URL`, `SUPABASE_ANON_KEY` |
| Play Console / App Store Connect | M8 | listings TR/EN, internal testing / TestFlight | — |
