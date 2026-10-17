# Google Play — Data safety form (v1.0 draft)

> DRAFT. Not legal advice. Answers assume the planned SDKs only (AdMob + UMP, Firebase Analytics + Crashlytics, RevenueCat, Play Billing). **VERIFY every row against the final build's merged manifest and each SDK's current "Play data disclosure" guidance** (Google AdMob, Firebase, RevenueCat each publish one) before submitting. Play Console → App content → Data safety.

## 1. Overview questions
| Question | Answer |
|---|---|
| Does the app collect or share any of the required user data types? | **Yes** |
| Is all of the user data collected by your app encrypted in transit? | **Yes** (HTTPS/TLS only; VERIFY no cleartext traffic — `usesCleartextTraffic=false`) |
| Do you provide a way for users to request that their data is deleted? | **Yes** — by email ([CONTACT EMAIL]) for data held by us/SDKs; on-device data is deleted by uninstalling or clearing app data. VERIFY: Play wants a deletion URL when there are accounts; there are none in v1.0, still provide the policy URL |
| Account creation | None (no account) — in the "account creation" section choose "My app does not allow users to create an account" |
| Independent security review (optional badge) | No |
| Privacy policy URL | [WEBSITE]/privacy |

## 2. Data types
"Collected" = transmitted off the device to us or a third party (SDK providers count). "Shared" = transferred to a third party; Google excludes service providers acting on your behalf and data sent for legal reasons — VERIFY which SDKs count as service providers (Google's guidance: Firebase/RevenueCat processing for you is *not* sharing; AdMob ad-serving *is* sharing).

| Category → type | Collected | Shared | Optional? | Purpose(s) | Source SDK / note |
|---|---|---|---|---|---|
| **Location → Approximate location** | Yes | No (VERIFY) | Required by SDK; user can't opt out except via consent | Analytics, Advertising | Derived from IP by Firebase/AdMob. |
| Location → Precise location | No | – | – | – | Not requested |
| **App activity → App interactions** | Yes | No | Consent-dependent (UMP) | Analytics | Firebase Analytics events (wheel completed, level, mode) |
| App activity → In-app search history / other user-generated content | No | – | – | – | No free text sent |
| App activity → Other actions | Yes | Yes (AdMob) | Consent-dependent | Analytics, Advertising | Ad interactions |
| **App info and performance → Crash logs** | Yes | No | No | Analytics (app functionality/stability) | Crashlytics |
| App info and performance → Diagnostics | Yes | No | No | Analytics | Crashlytics/Firebase performance-type data |
| App info and performance → Other app performance data | Yes | No | No | Analytics | |
| **Device or other IDs → Device or other IDs** | Yes | Yes | Consent-dependent for ad ID | Analytics, Advertising or marketing, Fraud prevention/security/compliance, App functionality (RevenueCat user ID) | GAID (AdMob), Firebase app instance ID, RevenueCat anonymous ID. Declare `com.google.android.gms.permission.AD_ID` |
| **Financial info → Purchase history** | Yes | No | No (only if buying) | App functionality (entitlement), Account management n/a | RevenueCat receives purchase/entitlement data; payment cards never seen. Google: purchases via Play Billing are not "collected" by you, but RevenueCat receiving purchase tokens counts — VERIFY |
| Financial info → Payment info / credit score | No | – | – | – | Handled by Play |
| Personal info (name, email, address, phone, race, politics, religion, sexual orientation, other) | No | – | – | – | Email only if the user writes to us (not collected via app) |
| Health and fitness | No | – | – | – | |
| Messages / emails / SMS | No | – | – | – | |
| Photos and videos / Audio files / Voice or sound recordings | No | – | – | – | TTS only outputs speech; no recording |
| Files and docs / Calendar / Contacts | No | – | – | – | |
| Web browsing | No | – | – | – | |

## 3. Per-type follow-ups (as asked in the form)
| Item | Answer |
|---|---|
| Is this data processed ephemerally? | No (retained by SDK providers per their policies) |
| Is data collection required or optional? | Device ID/approx. location/app interactions: **optional** where UMP consent applies (user can decline); crash logs: **required** (VERIFY — consider consent-gating Crashlytics to make it optional) |
| Why is this data collected? | See table (Analytics, Advertising or marketing, Fraud prevention/security/compliance, App functionality) |
| Why is this data shared? | Advertising or marketing (AdMob) |
| Encryption in transit | Yes |
| Deletion | Email request; on-device by uninstall; Firebase/AdMob/RevenueCat data per their processes |

## 4. SDK reference (verify at submission)
| SDK | Data accessed (typical) | Official disclosure |
|---|---|---|
| Google Mobile Ads (AdMob) + UMP | GAID, IP, app/device info, ad interactions | Google "AdMob data disclosure" help page |
| Firebase Analytics | App instance ID, events, device info, approx. location | Firebase "Understand data disclosure for Play" page |
| Firebase Crashlytics | Crash logs, installation ID, diagnostics | same |
| RevenueCat | Anonymous user ID, purchases, device/store info | RevenueCat docs → Google data safety |
| Play Billing | Handled by Google Play | – |
| Firebase Analytics ad features | If Google signals / ad-personalisation linking is on, advertising-related sharing increases — keep it OFF unless needed (VERIFY) | |

## 5. Related Play declarations
| Declaration | Answer |
|---|---|
| Advertising ID | **Yes**, app uses it (AdMob). Purposes: Advertising/marketing, Analytics, Fraud prevention |
| Ads | **Yes**, app contains ads |
| Government app / News / Financial features / Health apps | No |
| COVID / Account deletion | N/A (no accounts) |
