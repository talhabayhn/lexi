# ADR 0023: Firebase SDK integration (Android Gradle, iOS SPM) and telemetry privacy defaults

- Status: Accepted (amends 0006)
- Date: 2026-10-21

## Context
ADR 0006 chose Firebase Analytics + Crashlytics behind interfaces. M7 ships them, which adds the first native SDKs to
both apps. Open points: where the interfaces live (0006 said `core:platform`; `ARCHITECTURE.md` §2/§7 already put
interfaces in `core:domain`), how the iOS SDK gets into the Xcode project without breaking the `ios` / `release-ios`
workflows (the owner works on Windows; iOS is verified only by macOS CI), R8 mapping and dSYM upload, and privacy:
audience 13+, no ad ID, no ATT, no telemetry from debug builds. The owner created the Firebase project "Lexi"
(Android + iOS app `dev.talhabayhan.lexi`) and decided to commit both config files.

## Decision
- **Interfaces in `core:domain`** (`telemetry`): `AnalyticsTracker.track(AnalyticsEvent)` with a sealed, typed
  `AnalyticsEvent` (the only events and parameters allowed, `docs/ANALYTICS.md`; values are enum codes and counts,
  never free text or IDs) and `CrashReporter.recordException(throwable, message)` for caught, unexpected failures.
  Recording fakes in `core:testing`. Features and `shared` only see the interfaces (Koin).
- **Android**: Firebase BoM **35.0.2** (`firebase-analytics`, `firebase-crashlytics`) in `core:platform` androidMain
  (`FirebaseAnalyticsTracker`, `FirebaseCrashReporter`), bound in `shared`'s Android platform module. Gradle plugins in
  `androidApp`: `com.google.gms.google-services` **4.5.0** (reads `androidApp/google-services.json`) and
  `com.google.firebase.crashlytics` **3.0.8**. The R8 mapping upload runs only for release builds with
  `-Plexi.crashlyticsUpload=true` (set by `release-android` for signed tag/manual builds), so `./gradlew build`, the push
  CI and unsigned R8 checks never need the network.
- **iOS**: `firebase-ios-sdk` **13.0.1** (exact version) through **Swift Package Manager** in `iosApp.xcodeproj`, products
  `FirebaseAnalyticsCore` (Analytics without IDFA and ads conversion) and `FirebaseCrashlytics`, `-ObjC` in
  `Config.xcconfig`. Kotlin never links Firebase: `FirebaseTelemetry.swift` implements the Kotlin `IosTelemetry` bridge
  (`shared` iosMain) and is passed in a `PlatformServices` holder to `MainViewController(services)`; Kotlin adapters
  implement the domain interfaces. An unhandled Kotlin exception is recorded with its Kotlin stack (non-fatal model)
  before the runtime terminates; the crash itself is caught by Crashlytics. `GoogleService-Info.plist` lives in
  `iosApp/iosApp/`, a file-system-synchronized group, so it is a bundle resource without a pbxproj entry. A
  "Upload Crashlytics dSYMs" run-script phase calls the SDK's `Crashlytics/run` for **Release archives** only
  (`ACTION=install`; unsigned CI archives upload too, which is harmless) and only warns if the script or upload fails.
- **SPM, not CocoaPods**: Firebase recommends SPM; CocoaPods trunk is going read-only; the Kotlin CocoaPods plugin would
  replace `embedAndSignAppleFrameworkForXcode` with a Podfile + `.xcworkspace` and add `pod install` to every macOS job.
  With SPM the project, the scheme/target names and the workflows stay as they are: `xcodebuild -project` resolves
  packages itself (the `ios` job builds Debug, where the dSYM phase is a no-op; `release-ios` archives Release, which
  uploads dSYMs). Cost: the first resolve per runner clones the package graph. The `ios` job uploads the resolved
  `Package.resolved` as an artifact; it is committed so transitive versions are pinned too.
- **Privacy defaults** (audience 13+, no ATT): Android removes `com.google.android.gms.permission.AD_ID` from the merged
  manifest and sets `google_analytics_adid_collection_enabled=false`; both platforms set Consent Mode defaults
  ad_storage / ad_user_data / ad_personalization = denied, analytics_storage = granted, ad-personalisation signals off;
  iOS also turns off ad-network registration. IDFV / app-instance IDs stay at Firebase defaults.
- **Debug off, release on**: Android manifest placeholders (`firebase_analytics_collection_enabled`,
  `firebase_crashlytics_collection_enabled`) are true for release and false for debug unless `-Plexi.telemetry=true`.
  iOS Info.plist has both collection flags off; `FirebaseTelemetry.start()` turns them on every launch for Release
  builds, and for Debug only with the `-LexiTelemetry` launch argument (the setters persist, so they are always set).

## Consequences
- Both config files are committed (they identify the project, they are not secrets); the API keys must be restricted
  in Google Cloud Console to the Android package + signing SHA-1s and the iOS bundle id (owner).
- Play Data safety and Apple App Privacy change before the next public release (`docs/store/*`, ROADMAP re-check).
- When AdMob ships, the `AD_ID` removal and the ad Consent Mode defaults are revisited with UMP (its own ADR step).
- The iOS CI depends on GitHub/Google downloads for the SPM resolve; a release-ios archive also needs the Crashlytics
  upload endpoint (failures only warn).
- Swapping vendors (e.g. Sentry) touches `core:platform`, `shared` iosMain and the Swift bridge only.
