# v1.0 submission audit (release build, `proEnabled = false`) — 2026-11-04

## 1. Feature table (release build vs listings)

| Feature | In release build | In listings |
|---|---|---|
| A–Z wheel, Turkish clue, EN answer, pass/laps | Yes | Yes |
| Levels A1–C1 + Native, categories, mixed wheel | Yes | Yes |
| Level test (onboarding) | Yes | Yes |
| Classic / Practice / Hardcore | Yes | Yes |
| Daily Wheel | Yes | Yes |
| Review Wheel (from notebook) | Yes | Yes |
| **Same-device duel** | Yes since 1.0.9 (Home entry) | Yes (listings); website line added 2026-11-07 |
| Notebook with spaced repetition | Yes | Yes |
| TTS pronunciation, accent setting | Yes | Yes |
| Stats (learned, accuracy, streak, weekly, hardest letters) | Yes | Yes |
| Jokers (reveal, clear, example, listen), daily/streak gifts | Yes | Yes |
| **LGS exam pack (free)** | Yes (Home tile → pack list) | Was missing → **added** |
| **Daily goal, daily + review reminders (optional)** | Yes | Was missing → **added** |
| **Share card** | Yes | Was missing → **added** |
| YDS/YÖKDİL Pro pack, Lexi Pro card, rewarded ads | No (hidden by `proEnabled`) | Not mentioned (correct) |
| Offline, no account, TR + EN UI | Yes | Yes |

## 2. Fixed in the build
- Home exam tile subtitle was hard-coded "LGS · YKS-Dil · YDS" (YKS-Dil does not exist, YDS is Pro). It now lists the pack names playable in this build (`HomeViewModel` filters by `FeatureFlags.proEnabled`): release = "LGS". Fallback string "Sınav kelime listeleri" / "Exam word lists" if packs cannot be read.
- Settings footer showed "Restore purchases" in release (no purchases exist). Now hidden unless `proEnabled`.
- Not reachable in release, left as is: "Pro paket, yakında"/"SOON" strings (exam list and Settings Pro card, only with `proEnabled`).
- Store graphics: the generator only uses Roborazzi sample states (no Pro, no "yakında"); the Home shot (tr-03/en-03) comes from `HomeSampleState`, now `examPackNames = ["LGS"]`.

## 3. Metadata hygiene (Apple 2.3)
- iOS text: no Android/Google Play/price mention (the "Google Play" heading is the Play section only). No third-party brand names (LGS/CEFR are exam/standard names).
- Keywords no longer repeat name/subtitle words (EN dropped learn english, words, quiz, alphabet; TR dropped kelime, ingilizce, öğren). Both ≤100 (`check-limits.py` OK).
- No beta/test/placeholder/"coming soon" in visible metadata or release UI.
- URLs `/`, `/privacy`, `/terms`, `/support` return 200 and match `AppLinks` (trailing slash). Copyright and contact filled.

## 4. Privacy consistency (Apple 5.1)
`app-privacy-apple.md` §0, `privacy-policy-*.md` and `Info.plist` agree: Analytics only after opt-in, Crashlytics always, no ATT/IDFA, notifications local and optional. Info.plist has no permission purpose strings and the app uses no permission that needs one (notifications and share sheet need none). No change needed.

## 5. Owner must do by hand
- App Store Connect: paste the new description, promo text, keywords, what's new (EN + TR) from `listing-*.md`; upload new screenshots from `docs/store/graphics/appstore/*` after the store-graphics run is committed.
- Play Console: same text from the Play sections; replace phone screenshots and the promo videos if re-generated.
- Re-publish the website (pages workflow) so the privacy policy (duel and rating events, word reminders) and the game-modes line match 1.0.9/1.0.10.
- App Privacy answers: no change.
