# Privacy — facts from code and open VERIFY items (1.0.5)

Not published on the site. Owner reads this before the App Store submission and the Android production rollout.

## Facts established from the code
| Fact | Evidence |
|---|---|
| SDKs: Firebase Analytics + Crashlytics only. No AdMob, UMP, RevenueCat, Play Billing, Supabase, own HTTP client. Entitlement is static (release never Pro). | `gradle/libs.versions.toml`, `core/platform/build.gradle.kts`, `StaticEntitlementRepository.kt` |
| Analytics is opt-in: manifest `firebase_analytics_collection_enabled=false`, Info.plist `FIREBASE_ANALYTICS_COLLECTION_ENABLED=false`, iOS `start()` re-disables each launch; `ConsentGatedAnalyticsTracker` drops events and keeps collection off until `Settings.analyticsConsent`. Existing users start off. | `AndroidManifest.xml`, `FirebaseTelemetry.swift`, `ConsentGatedAnalyticsTracker.kt`, ADR 0023 |
| Crashlytics is NOT consent-gated: on in release (`telemetryEnabled=true`; iOS `allowed=true` unless DEBUG). Debug builds off. | `androidApp/build.gradle.kts`, `FirebaseTelemetry.swift` |
| Events: closed list, enum codes and counts only, no free text. Crash log lines are fixed English strings. | `docs/ANALYTICS.md`, `FirebaseTelemetry.kt` |
| No advertising ID: `AD_ID`, `ACCESS_ADSERVICES_*` removed; `google_analytics_adid_collection_enabled=false`; iOS uses `FirebaseAnalyticsCore`, ad-network registration off, no ATT string. Ad Consent Mode defaults denied. | `AndroidManifest.xml`, `Info.plist`, ADR 0023 |
| No location permission on either platform. | `AndroidManifest.xml`, `Info.plist` |
| Identifiers: Firebase app-instance ID (Analytics, opt-in), Crashlytics installation ID, Firebase Installation ID (SDK default), IDFV on iOS (Firebase default; no IDFV opt-out key set). | SDK defaults |
| Only network use outside Firebase: opening privacy/terms/support pages in the system browser (GitHub Pages). | `AppLinks.kt`, `AndroidUrlOpener.kt`, `IosUrlOpener.kt` |
| Android `allowBackup=true`, no backup rules. | `AndroidManifest.xml` |
| No `PrivacyInfo.xcprivacy` in the app target. | `find` |

## Mismatches found and fixed
1. Policy TR/EN described AdMob, UMP, RevenueCat, ATT and purchases that the binary does not contain; Crashlytics legal-basis column and many VERIFY notes were published on the site. Rewritten for the real binary, VERIFY notes moved here. The old text is in git history for when ads or purchases ship.
2. `app-privacy-apple.md` §0 said Linked = No and listed IDFV as always collected; now ready-to-click, Linked = Yes (conservative), Tracking = No. §1–§2 marked FUTURE.
3. `data-safety-google.md` §0 now a click table; Approximate location now declared conservatively (was "not declared"); §1–§5 marked FUTURE.
4. Listing privacy paragraph (EN/TR) mentioned ads and consent forms that do not exist; replaced.
5. Site support page mentioned Restore purchases / subscriptions, which do not exist in 1.0.5; removed.
6. `owner-checklist.md` row 5 was stale (said analytics on without consent).

## VERIFY (owner)
1. **Listing Pro text** (`listing-en/tr.md` "LEXI PRO", subscription terms, "No ads" bullet): the binary has no Pro purchase yet. Do not submit those paragraphs with 1.0.5 unless Pro ships.
2. **Retention**: confirm Firebase Console → Analytics → Data retention shows 2 months, otherwise change policy §7. Crashlytics 90 days is Google's documented default.
3. **Console settings** claimed in the policy: Google signals off, no Ads/AdMob linking, "Granular location and device data collection" off, ad personalisation off.
4. **Approximate location**: declared conservatively in both Apple (Coarse Location) and Play (Approximate location) because Firebase derives country from the IP. With granular collection off it should be country only; if you are sure that does not count, untick it in both forms.
5. **Linked to user (Apple)**: set conservatively to Yes. If you want No, you must be able to argue the IDs are not associated with identity or device.
6. **Crashlytics without consent**: policy states it always runs. A lawyer should confirm KVKK/GDPR allow this (legitimate interest) or you should gate it like Analytics (code change, not part of this task).
7. **IP address wording** (policy §4) and Firebase Installations contacting Google at launch even with Analytics off: check once in a proxy or Firebase docs; the text only says Google receives the IP when the app connects.
8. **Deletion path**: IDs are random and not shown in the app, so per-user deletion is not possible; policy says so. Decide if that is acceptable or add a "reset analytics ID / show ID" feature later.
9. **Contact email**: policy uses `talhabayhan.aps@gmail.com`; make sure it matches the Play/ASC contact fields. Postal address, VERBİS registration threshold, EU representative (Art. 27): lawyer.
10. **Children / age**: audience 13+ is declared in the policy; confirm the Türkiye threshold and the Play/Apple age answers (`app-privacy-apple.md` §3–§4).
11. `PrivacyInfo.xcprivacy`: run Xcode → Organizer → Generate Privacy Report on the archive; check required-reason APIs for the app target.
12. Google TTS / system speech engines may use the network on the device; that is OS behaviour, not declared.
13. Android auto-backup copies app data to the user's Google Drive by default (mentioned in policy §3); decide whether to restrict with backup rules.
