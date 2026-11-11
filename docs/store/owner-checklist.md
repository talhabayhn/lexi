# Owner checklist — store & legal (v1.0)

> **v1.0 is free: no ads, no Pro, no subscriptions or in-app purchases, no Pro exam packs / "coming soon" UI (the free LGS pack stays)** (release builds have `FeatureFlags.proEnabled` off). Consequences: Play data safety / App Store privacy answers and listings declare no ads and no purchases; do not create subscription/IAP products or an AdMob app for this release; skip B4 and B7 and the AdMob/RevenueCat lines below until Pro ships.

> Everything here needs the owner. Drafts are NOT legal advice; get a lawyer to review privacy/terms (KVKK/GDPR). `VERIFY` items are in the draft files; grep: `grep -rn VERIFY docs/store`.

## A. Placeholders to fill
| Placeholder | Used in | Where to enter |
|---|---|---|
| `[DEVELOPER NAME]` (legal name / company) | listings (copyright), privacy, terms | Play Console → Settings → Developer account; App Store Connect → Agreements (seller name) & App Information → Copyright; website footer |
| `[CONTACT EMAIL]` | listings, privacy, terms | Play: Store settings → Contact details; ASC: App Information → Support, plus App Review contact; set up a monitored mailbox |
| `[WEBSITE]` | listings, privacy, terms | Play: Store settings → Website; ASC: Support URL + Marketing URL; host `/privacy`, `/terms`, `/support` (TR+EN, no login) |
| `[DATE]` / `[TARİH]` | privacy, terms | Fill at publication; update on every change |
| Postal address, trade registry / VERBİS info | privacy §12, terms §13 | Website legal pages; VERBİS registration if required — lawyer decides |
| Governing law `[Türkiye]` | terms §12 | Confirm with lawyer |

## B. Decisions
| # | Decision | Recommendation | Where |
|---|---|---|---|
| 1 | Store names (EN/TR) | `Lexi: English Alphabet Quiz` / `Lexi: İngilizce Kelime Çarkı`. `PRODUCT.md` still lists a title with a word you told me not to use — update it | Play listing, ASC name; do trademark (TÜRKPATENT) + store search first |
| 2 | ATT on iOS | Yes, with UMP first (`app-privacy-apple.md` §2) | ASC → App Privacy, `Info.plist` |
| 3 | Target audience | 13+, not Made for Kids | Play → App content → Target audience; ASC Age Rating |
| 4 | Interstitial ads | `PRODUCT.md` allows one every 3 games; privacy/data-safety text says rewarded only — decide and align | Docs + AdMob units |
| 5 | Analytics/Crashlytics consent | Changed in 1.0.5: Analytics is opt-in (onboarding + Settings); Crashlytics stays always on in release (ADR 0023). Decide whether Crashlytics needs consent too (`privacy-verify.md`) | Policy §4, Firebase settings |
| 6 | Data retention in Firebase | Event data 2 months (min) unless you need 14 | Firebase Console → Analytics → Data settings |
| 7 | Pro price points & trial | Set per country; lifetime as non-consumable | Play → Monetize → Subscriptions / In-app products; ASC → Subscriptions + In-App Purchases; RevenueCat offerings |
| 8 | Exam pack names in listings | Left out until trademark/affiliation checked | Listing files |
| 9 | Standard Apple EULA vs custom terms | Custom terms OK; state it | ASC → App Information → License Agreement |
| 10 | Account deletion/data requests | Email process only (no accounts) | Privacy §9 |
| 11 | Promo "what's new" and screenshots | Texts here; graphics in `docs/store/graphics/` (not touched) | Consoles |

## C. Console steps
### Google Play Console
1. Create app (name, default language TR or EN, App, Free) → declarations.
2. Store settings: category Game → Word; contact email/website.
3. Main store listing: paste `listing-*.md` (default + other language), upload graphics. **Play Console → Main store listing → Graphics:** phone screenshots `docs/store/graphics/play/phone/{tr,en}-01…08.png` are now **2160×3840** (rendered from 4× sources, RGB, no alpha, < 1 MB each; re-upload them so the store page is sharp), feature graphic `play/feature-graphic-{tr,en}.png` 1024×500 (drawn at 2× and downsampled), icon `play/icon-512.png`.
4. App content: privacy policy URL, ads = no, app access, content rating (IARC, `app-privacy-apple.md` §3), target audience (§4), data safety (`data-safety-google.md`), advertising ID per the merged manifest (Firebase Analytics may add AD_ID).
5. Monetize: SKIP for v1.0 (free). Later: subscriptions (monthly, yearly), in-app product (lifetime); link to RevenueCat via service credentials.
6. Testing: internal → closed (personal accounts need the 12-testers/14-day rule — VERIFY current requirement) → production.
### App Store Connect
1. Agreements, Tax, Banking (Paid Apps) complete.
2. App Information: name, subtitle, category (Games → Word), privacy policy URL, license.
3. Version page: promo text, description, keywords, support/marketing URL, what's new, screenshots, review notes, sign-in not required. **Screenshots: upload `docs/store/graphics/appstore/iphone-6.3/` (1206×2622); the 6.9" 1320×2868 files are rejected.**
4. App Privacy (`app-privacy-apple.md` §1), Age Rating (§3), Export compliance (§6), IDFA/ATT answers.
5. In-App Purchases/Subscriptions: SKIP for v1.0 (free). Later: group, localisations (TR, EN), prices, review screenshot of paywall.
6. TestFlight external testing, then submit.
### Other
- SKIP for v1.0 (no ads/Pro): AdMob: add app, ad unit, app-ads.txt on [WEBSITE] (VERIFY), UMP consent message (GDPR + US states if wished) in AdMob → Privacy & messaging.
- Firebase: link Play/ASC? (leave ad-personalisation linking off, VERIFY), set retention.
- SKIP for v1.0: RevenueCat: project, API keys in secrets (never committed).
- Legal: lawyer review of privacy-policy-*.md and terms-*.md; publish at [WEBSITE]/privacy and /terms; add links in app Settings (privacy policy URL is required in-app per `ARCHITECTURE.md` §13) and paywall.

## D. Pending owner actions (2026-10-22)

### 1.0.5 verification
- [ ] Publish 1.0.5 to Play closed testing and add it to the TestFlight group.
- [ ] Eyeball the 7 OnboardingStep3 goldens (screenshots workflow, golden-review artifact).
- [ ] Test the analytics consent checkbox + Settings switch; check Firebase Realtime (on = events, off = none).
- [ ] Confirm Firebase "Granular location" is off; check iOS Crashlytics stack traces are symbolicated (app dSYM).

### Before the public release
- [x] Play production live (https://play.google.com/store/apps/details?id=dev.talhabayhan.lexi); site links Google Play.
- [ ] iOS: pending App Store review; when approved, replace the site's "App Store: coming soon" note with the store link.
- [ ] App Store Connect App Privacy per `app-privacy-apple.md` §0; Play Data safety already submitted.
- [ ] Store listing TR/EN, screenshots.
- [ ] Designs for the consent block, reminder sheet, level-test intro/result.

### 1.0.8
- [x] Share card link: `StoreLinks.SHARE_LINK_LABEL` is the website URL https://talhabayhn.github.io/lexi (one link offering both stores).
- [x] The in-app rating prompt uses the official APIs: Play In-App Review (`PlayReviewRequester`, `ReviewManager`) and iOS `SKStoreReviewController.requestReviewInScene` (`IosReviewRequester`), ADR 0024.
- [ ] Play Data safety: add approximate location (optional, analytics) for consistency with App Privacy.

### Payments
- [ ] W-8BEN status.
- [ ] Bank account (GVK 20/B).
- [ ] Paid Apps agreement Active.
- [ ] RevenueCat project + iOS products.
- [ ] Android products after the first SDK build.

### When a Mac is available
- [ ] iOS perf measurement (`docs/PERF.md`, compare mode).

## Duel (M8.1)
- The same-device duel is in the build (1.0.9) and in the listings.
