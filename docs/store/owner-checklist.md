# Owner checklist — store & legal (v1.0)

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
| 5 | Analytics/Crashlytics behind consent | Decided 2026-10-21: on in release without a consent form until UMP ships with AdMob (ADR 0023); revisit then | UMP config, Firebase settings |
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
3. Main store listing: paste `listing-*.md` (default + other language), upload graphics.
4. App content: privacy policy URL, ads = yes, app access, content rating (IARC, `app-privacy-apple.md` §3), target audience (§4), data safety (`data-safety-google.md`), advertising ID = yes.
5. Monetize: subscriptions (monthly, yearly), in-app product (lifetime); link to RevenueCat via service credentials.
6. Testing: internal → closed (personal accounts need the 12-testers/14-day rule — VERIFY current requirement) → production.
### App Store Connect
1. Agreements, Tax, Banking (Paid Apps) complete.
2. App Information: name, subtitle, category (Games → Word), privacy policy URL, license.
3. Version page: promo text, description, keywords, support/marketing URL, what's new, screenshots, review notes, sign-in not required.
4. App Privacy (`app-privacy-apple.md` §1), Age Rating (§3), Export compliance (§6), IDFA/ATT answers.
5. In-App Purchases/Subscriptions: group, localisations (TR, EN), prices, review screenshot of paywall.
6. TestFlight external testing, then submit.
### Other
- AdMob: add app, ad unit, app-ads.txt on [WEBSITE] (VERIFY), UMP consent message (GDPR + US states if wished) in AdMob → Privacy & messaging.
- Firebase: link Play/ASC? (leave ad-personalisation linking off, VERIFY), set retention.
- RevenueCat: project, API keys in secrets (never committed).
- Legal: lawyer review of privacy-policy-*.md and terms-*.md; publish at [WEBSITE]/privacy and /terms; add links in app Settings (privacy policy URL is required in-app per `ARCHITECTURE.md` §13) and paywall.

## D. Pending owner actions (2026-10-22)

### 1.0.5 verification
- [ ] Publish 1.0.5 to Play closed testing and add it to the TestFlight group.
- [ ] Eyeball the 7 OnboardingStep3 goldens (screenshots workflow, golden-review artifact).
- [ ] Test the analytics consent checkbox + Settings switch; check Firebase Realtime (on = events, off = none).
- [ ] Confirm Firebase "Granular location" is off; check iOS Crashlytics stack traces are symbolicated (app dSYM).

### Before the public release
- [ ] App Store Connect App Privacy per `app-privacy-apple.md` §0; Play Data safety already submitted.
- [ ] Store listing TR/EN, screenshots.
- [ ] Designs for the consent block, reminder sheet, level-test intro/result.

### Payments
- [ ] W-8BEN status.
- [ ] Bank account (GVK 20/B).
- [ ] Paid Apps agreement Active.
- [ ] RevenueCat project + iOS products.
- [ ] Android products after the first SDK build.

### When a Mac is available
- [ ] iOS perf measurement (`docs/PERF.md`, compare mode).
