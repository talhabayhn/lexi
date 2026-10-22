# Lexi — Privacy Policy

**Last updated:** 21 October 2026 · **Applies to:** the Lexi app for Android and iOS (app ID `dev.talhabayhan.lexi`)

## 1. Who we are
The data controller is **Hüseyin Talha Bayhan** ("we"). Contact: **talhabayhan.aps@gmail.com** · **https://talhabayhn.github.io/lexi**. VERIFY: add postal address / VERBİS or EU representative if required (see §12).

## 2. Summary
- Lexi works offline. There is **no account** and no sign-up in version 1.0.
- Your game progress, settings and word notebook are stored **only on your device**. We do not receive them.
- Third-party services we use: **Google AdMob** (rewarded ads), **Google User Messaging Platform** (consent), **Firebase Analytics and Crashlytics**, and **RevenueCat** (purchases) together with Apple App Store / Google Play billing.
- The app is **not directed to children**.

## 3. Data stored only on your device
Progress, scores, streaks, joker inventory, word notebook, statistics, settings (language, sound, haptics, reminder, daily goal), and your Pro status cache. We do not access this data. Deleting the app (or clearing app data) removes it. VERIFY: OS-level device backups (Google/Apple) may copy app data under your own backup settings.

## 4. Data processed by third-party services
| Service | Data (typically) | Purpose | Legal basis (VERIFY) |
|---|---|---|---|
| Google AdMob | Advertising ID (GAID / IDFA where permitted), IP address, device and app info, ad interaction data | Show and measure rewarded ads, fraud prevention | Consent (GDPR Art. 6(1)(a); KVKK explicit consent) |
| Google UMP | Consent choice, device identifiers needed to store your choice | Collect and record your consent | Legal obligation / legitimate interest |
| Firebase Analytics (**only if you opt in**) | App instance ID, device/OS/app version, country-level location, in-app events (app opened, onboarding finished, level test result, game started/finished with level, mode, correct answers and duration, joker used, share, reminder turned on — no free text). No advertising ID; not used for ads | Understand usage, improve the app | Explicit consent (KVKK; GDPR Art. 6(1)(a)); you can withdraw it at any time |
| Firebase Crashlytics | Crash traces, device model/OS, app version, installation ID | Fix crashes, keep the app stable | Legitimate interest; VERIFY whether consent is needed |
| RevenueCat | Anonymous app user ID, purchase/entitlement status, store receipt data, country | Validate and restore Lexi Pro purchases | Contract performance |
| Apple / Google payments | Handled by the store; we never see your card details | Process purchases and refunds | Contract performance |

Firebase and AdMob may be processed by Google LLC / Google Ireland Limited; RevenueCat by RevenueCat, Inc. (USA). They act as independent controllers or processors under their own terms. VERIFY: final SDK versions and data-processing terms.

## 5. Advertising ID and consent
- Ads are **rewarded** (you choose to watch one to earn a joker). VERIFY: if interstitial ads ship in v1.0, update this section.
- **Firebase Analytics is off by default.** It collects data only if you tick "Share anonymous usage statistics" in the last onboarding step or turn on **Settings → Usage statistics**; you can turn it off again at any time, which stops collection. Firebase Crashlytics (crash reports) always runs so we can fix crashes. Neither uses the advertising ID or is used for advertising. Analytics data is kept for 2 months.
- Once ads are added: in the EEA, UK and Türkiye (VERIFY scope), a consent form (Google UMP) is shown at first launch; personalised ads run only according to your choices. You can change your choice at any time in **Settings → Privacy**. VERIFY: the settings entry must exist in the build.
- On iOS, the app asks for tracking permission (App Tracking Transparency) only if personalised ads are enabled. VERIFY: see `app-privacy-apple.md`.
- You can reset or delete your advertising ID in your device settings. If you decline personalised ads, you may still see non-personalised ads.
- Lexi Pro removes ads.

## 6. Notifications
If you enable the daily reminder, notifications are scheduled **locally on your device**. We do not use push servers in v1.0. Permission can be revoked in system settings.

## 7. Retention
- On-device data: until you delete the app or its data.
- Analytics/crash data: retained by Google per our Firebase settings (VERIFY: configure e.g. 2 or 14 months for event data; Crashlytics about 90 days).
- RevenueCat: for the life of the customer record unless deletion is requested. VERIFY.
- Ad data: per Google's policies.

## 8. International transfers
Google and RevenueCat may process data outside your country (e.g. USA). We rely on the providers' safeguards such as standard contractual clauses and/or adequacy mechanisms. VERIFY: KVKK Art. 9 cross-border transfer rules (as amended 2024) and GDPR Chapter V.

## 9. Your rights
Under **GDPR** (access, rectification, erasure, restriction, portability, objection, withdraw consent at any time, complaint to your supervisory authority) and **KVKK Art. 11** (learn whether data is processed, request information, learn the purpose, know third parties to which data is transferred, request correction/deletion, object to automated outcomes, claim compensation). Contact **talhabayhan.aps@gmail.com**; we reply within 30 days. Because we do not hold your game data and have no accounts, we may need your advertising ID / RevenueCat user ID (shown in Settings → About; VERIFY it will be exposed) to find data about you. Requests about Google/RevenueCat-held data may be forwarded to those providers.

## 10. Children
Lexi is **not directed to children** and we do not knowingly collect personal data from children under 13 (or the age of digital consent in your country). VERIFY: the applicable age threshold in Türkiye and how consent capacity of minors is handled under KVKK. If you believe a child gave us data, contact us and we will help delete it. VERIFY: see target-audience decision in `app-privacy-apple.md`.

## 11. Security
Data in transit between the app and third-party services uses HTTPS/TLS. On-device data sits in the app's private storage. No method is perfectly secure.

## 12. Controller details (KVKK / GDPR)
VERIFY: KVKK Art. 10 disclosure (identity of controller, purpose, recipients, method and legal basis, rights) is satisfied by this notice; consider VERBİS registration thresholds; if you have no EU establishment, consider an EU representative (GDPR Art. 27).

## 13. Changes
We may update this policy; the date above changes and, for material changes, we will notify you in the app. 
