# Apple — App Privacy, ATT, age rating and store-content answers (v1.0 draft)

> DRAFT. Not legal advice. Re-verify against the final binary (SDK privacy manifests `PrivacyInfo.xcprivacy`) and Apple's current guidelines. Marked VERIFY = uncertain.

## 1. App Privacy ("nutrition labels") — App Store Connect → App Privacy
**Do you or your third-party partners collect data from this app?** Yes.

| Data type | Collected | Linked to user? | Used for tracking? | Purposes | Source |
|---|---|---|---|---|---|
| Identifiers → Device ID | Yes | **Yes** (VERIFY: AdMob/Firebase/RevenueCat tie IDs to the device/install) | **Yes** if personalised ads + IDFA used; No otherwise | Third-party advertising, Analytics, App functionality | IDFA/IDFV, RevenueCat ID, Firebase instance ID |
| Identifiers → User ID | Yes (RevenueCat anonymous ID) | Yes | No | App functionality | RevenueCat |
| Usage Data → Product Interaction | Yes | Yes (VERIFY; Firebase tied to instance ID) | No | Analytics | Firebase |
| Usage Data → Advertising Data | Yes | Yes | Yes (with ATT consent) | Third-party advertising | AdMob |
| Usage Data → Other usage data | Yes | Yes | No | Analytics | Firebase |
| Diagnostics → Crash Data | Yes | Yes (VERIFY; Crashlytics installation ID) | No | App functionality / Analytics | Crashlytics |
| Diagnostics → Performance / Other diagnostic data | Yes | Yes | No | Analytics | Firebase/AdMob |
| Purchases → Purchase History | Yes | Yes | No | App functionality | RevenueCat |
| Location → Coarse Location | Yes | Yes | Yes if used for ads (VERIFY) | Third-party advertising, Analytics | IP-derived |
| Contact info, Health, Financial info, Sensitive info, Contacts, User content, Browsing/Search history, Body, Surroundings | **No** | – | – | – | – |

Answer notes:
- If you serve **only non-personalised ads** and do not read the IDFA, "tracking" may be No (see §2) — VERIFY with Apple's definition of tracking.
- Keep Firebase Analytics free of IDFA: do not link `FirebaseAnalytics/IdentitySupport` (VERIFY) and set `GOOGLE_ANALYTICS_IDFV_COLLECTION_ENABLED` as appropriate.
- SDKs must ship privacy manifests and required-reason API declarations; use SDK versions that include them (VERIFY at build time).

## 2. ATT (App Tracking Transparency) — decision
**Recommendation: YES, ask for ATT permission** when AdMob serves personalised ads on iOS.

Why:
1. AdMob's personalised ads use the IDFA and cross-app data to target ads; Apple defines this as "tracking", which requires the ATT prompt and `NSUserTrackingUsageDescription` in `Info.plist` (e.g. TR: "Size daha uygun reklamlar göstermek için kullanılır." / EN: "Your data will be used to show you more relevant ads.").
2. Without consent, the IDFA is zeroed and AdMob falls back to non-personalised ads — the app keeps working.
3. Flow: Google UMP form first (GDPR/TR consent), then the ATT prompt (UMP can present Apple's ATT message through its IDFA message), shown only after the first screen with value (not on cold start before onboarding; Apple review dislikes prompts with no context). Never gate gameplay on the answer.

Alternative (simpler, lower ad revenue): request **non-personalised ads only**, don't touch IDFA, answer "No tracking", and skip ATT. VERIFY with Apple's review guidelines 5.1.2 and Google's iOS ATT docs before choosing; owner decides (see `owner-checklist.md`).

If ATT is used: label "Used to Track You" for Device ID + Advertising Data; add `SKAdNetworkItems` for AdMob (Google's list) in `Info.plist`.

## 3. Age rating
### Apple (App Store Connect → Age Rating) — expected result **4+** (VERIFY with the 2025+ questionnaire which now includes 13+/16+/18+ tiers)
| Question | Answer |
|---|---|
| Cartoon/fantasy violence, realistic violence, prolonged graphic violence, guns | None |
| Sexual content or nudity / mature or suggestive themes | None |
| Profanity or crude humor | None (VERIFY: word list contains no vulgar entries; check `content/`) |
| Horror/fear themes | None |
| Alcohol, tobacco, drugs | None (VERIFY: vocabulary like "beer"/"wine" in food-drink category are educational references only; answer Infrequent/Mild only if reviewers flag) |
| Simulated gambling | None |
| Contests | None |
| Medical/treatment info | None |
| Unrestricted web access | No |
| User-generated content / chat / messaging | None in v1.0 (re-answer for v1.1 online rooms) |
| In-app purchases / loot boxes | IAP: Yes; loot boxes: No |
| Ads (age assurance, new question) | Contains ads; rewarded only |
| Made for Kids | **No** |

### Google Play (IARC questionnaire) — expected **IARC 3+ / PEGI 3 / ESRB Everyone** (VERIFY output)
Violence: No · Sexuality: No · Language: No · Controlled substance: No (VERIFY food-drink words) · Miscellaneous: no gambling, no user interaction/sharing of user-generated content, no sharing of location, **digital purchases: Yes**, **contains ads: Yes**.
Category: **Game → Word** (Google Play; VERIFY whether Education is more suitable for Play "Education" tab: Education requires Teacher Approved only if designed for children, not needed here).

## 4. Target audience recommendation
**Declare target age groups 13+ (Play: 13–15, 16–17, 18+), not "Made for Kids" / not Designed for Families.**
Why: Lexi is a vocabulary game that will appeal to teens and students (LGS/YKS audiences are 13–18), but it contains AdMob and analytics SDKs, which are restricted for child-directed apps (Play Families Policy, Apple Kids Category rules, COPPA/GDPR-K). Declaring under-13 would force certified ad networks, no analytics IDs, and no ads personalisation. The visuals must not "appeal primarily to children" (cartoon characters, etc. — VERIFY with design). If the owner wants under-13, a separate Families-compliant build is needed — decision in `owner-checklist.md`. Add an in-app age gate? Not required if not targeted to children (VERIFY); UMP "tag for under age of consent" can be set to false.

## 5. Google Play "App content" checklist
| Item | Answer / action |
|---|---|
| Privacy policy | URL https://talhabayhn.github.io/lexi/privacy (live, no login, TR+EN) |
| Ads | Yes, contains ads |
| App access | All functionality available without login/special access |
| Content rating | Complete IARC (§3) |
| Target audience & content | 13+ (§4); not appealing primarily to children |
| Data safety | `data-safety-google.md` |
| Government apps / Financial features / Health | No |
| News app | No |
| Advertising ID | Yes (AdMob); declare in the Advertising ID form; keep AD_ID permission |
| Account deletion | N/A (no accounts) |
| Photo & video permissions / Exact alarms | None: reminders are inexact (Android) and local calendar notifications (iOS, ADR 0021); no exact-alarm permission |
| Subscriptions | Configure products (monthly, yearly, lifetime as one-time in-app product) + free-trial offers; prices set in Play Console |
| Store listing | `listing-*.md`; graphics owned elsewhere |
| App category / tags / contact details | Game → Word; email talhabayhan.aps@gmail.com; website https://talhabayhn.github.io/lexi; phone optional |

## 6. Export compliance (Apple + US EAR)
- **App Store Connect → "Does your app use encryption?"** The app uses encryption only through the OS (HTTPS/TLS via URLSession/system libraries; SDKs use platform TLS). Answer **Yes → exempt**: set `ITSAppUsesNonExemptEncryption = NO` in `Info.plist` (VERIFY: no proprietary/non-standard cryptography; SQLite content DB is not encrypted; Firebase/AdMob/RevenueCat use standard TLS — Apple says encryption limited to HTTPS/OS-provided is exempt).
- France: encryption declaration not required for standard-exempt use (VERIFY with Apple's current wording).
- Google Play: no export declaration form; ensure `usesCleartextTraffic=false`.

## 7. App Store subscription disclosure text
Use in the App Store description (already in `listing-*.md`), on the in-app paywall (near the Subscribe button) and link both in App Store Connect:

**EN**
> Lexi Pro is available as a monthly or yearly auto-renewing subscription, or as a one-time lifetime purchase. Payment is charged to your Apple ID account at confirmation of purchase. The subscription renews automatically unless it is cancelled at least 24 hours before the end of the current period. Your account will be charged for renewal within 24 hours before the end of the current period. You can manage or cancel your subscription in your Apple ID account settings. Any unused portion of a free trial period, if offered, is forfeited when you purchase a subscription. Privacy Policy: https://talhabayhn.github.io/lexi/privacy · Terms of Use: https://talhabayhn.github.io/lexi/terms

**TR**
> Lexi Pro aylık veya yıllık otomatik yenilenen abonelik ya da tek seferlik ömür boyu satın alma olarak sunulur. Ödeme, satın alma onayında Apple ID hesabınızdan tahsil edilir. Abonelik, mevcut dönemin bitiminden en az 24 saat önce iptal edilmedikçe otomatik yenilenir. Yenileme ücreti, mevcut dönemin bitiminden önceki 24 saat içinde hesabınızdan tahsil edilir. Aboneliğinizi Apple ID hesap ayarlarınızdan yönetebilir veya iptal edebilirsiniz. Ücretsiz deneme sunulursa kullanılmayan kısmı, abonelik satın alındığında geçersiz olur. Gizlilik Politikası: https://talhabayhn.github.io/lexi/privacy · Kullanım Koşulları: https://talhabayhn.github.io/lexi/terms

Apple requirements (Guideline 3.1.2): paywall must show subscription title, length, price (localised, price per period), Restore Purchases button, links to Privacy Policy and Terms (or Apple EULA link in the metadata). VERIFY: paywall (currently design debt in `ROADMAP.md`) satisfies these.

## 8. Other App Store Connect items
| Item | Answer |
|---|---|
| Sign in required | No (no demo account needed) |
| Review notes | Mention: offline game, rewarded ads optional, Pro via sandbox; provide how to reach paywall and restore |
| Content rights | Own/licensed content only — VERIFY word list and clue sources licences (`docs/CONTENT.md`) |
| IDFA usage (legacy question) | Yes: serve ads; attribute? No; (VERIFY current form) |
| Advertising identifier in reviews | Consistent with ATT choice (§2) |
