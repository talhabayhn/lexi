# Release runbook

For the owner: Windows PC, no Mac, Google Play and Apple developer accounts already exist. Everything that
needs a Mac runs on GitHub Actions. Store texts and legal drafts: `docs/store/` (start with
`owner-checklist.md`).

| Workflow | Trigger | Needs secrets | Output |
|---|---|---|---|
| `release-android.yml` | every push to `main` | no | unsigned R8 release AAB/APK (shrinker check) |
| `release-android.yml` | tag `v*` or manual run | `ANDROID_*` (+ optional `PLAY_SERVICE_ACCOUNT_JSON`) | signed AAB artifact, Play internal track (draft) |
| `release-ios.yml` (experimental) | tag `v*` or manual run | `APPSTORE_*`, `TEAM_ID` (else unsigned archive) | TestFlight build |
| `pages.yml` | changes to `docs/site`, legal drafts | no (Pages must be enabled) | website with privacy/terms/support |

## 1. Android signing key (once)

Play App Signing: Google keeps the real app signing key; you sign uploads with your own **upload key**.
If the upload key is lost, Play support can reset it — the app is never lost.

1. Install a JDK (e.g. Android Studio ships one: `C:\Program Files\Android\Android Studio\jbr\bin`).
2. In PowerShell, outside the repo folder:
   ```powershell
   keytool -genkeypair -v -keystore lexi-upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
   ```
   Choose a strong store password; use the same for the key password when asked. Back the `.jks` file and
   passwords up in a password manager. **Never commit it.**
3. Base64 for the GitHub secret:
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("lexi-upload.jks")) | Set-Clipboard
   ```
4. Play Console → create app (if not yet) → **Test and release → App integrity → Play App Signing**: accept
   "Use Google-generated key" (default). The first AAB you upload registers your upload key.

## 2. GitHub secrets

GitHub → repo → **Settings → Secrets and variables → Actions → New repository secret**. Exact names:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | clipboard from step 1.3 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `upload` |
| `ANDROID_KEY_PASSWORD` | key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | optional, full JSON text from §3 |
| `APPSTORE_KEY_ID` | App Store Connect API key ID (§4) |
| `APPSTORE_ISSUER_ID` | issuer ID (§4) |
| `APPSTORE_KEY_P8_BASE64` | base64 of the `AuthKey_XXXX.p8` file (same PowerShell command as 1.3) |
| `TEAM_ID` | Apple Developer Team ID (developer.apple.com → Membership details) |

Secrets are never printed in logs and are not available to forks.

## 3. Play service account (optional, for automatic upload)

1. Google Cloud Console → create (or pick) a project → **IAM & Admin → Service accounts → Create**;
   no roles needed. Open it → **Keys → Add key → JSON** → download.
2. Enable the **Google Play Android Developer API** for that project.
3. Play Console → **Users and permissions → Invite new users** → the service account e-mail → app
   permissions for Lexi: *Release to testing tracks* (and *View app information*).
4. Paste the whole JSON file content into `PLAY_SERVICE_ACCOUNT_JSON`.

**A brand-new app must get its very first AAB uploaded by hand** (Play Console → Internal testing →
Create release → upload the AAB from the workflow artifact). Only after that does the API accept uploads.
CI uploads as **draft** to the internal track; open the release in Play Console and roll it out.

## 4. App Store Connect API key + TestFlight (experimental)

1. App Store Connect → **Users and Access → Integrations → App Store Connect API → Team Keys → +**,
   role **Admin** (cloud-managed signing certificates need it; App Manager is not enough). Download the `.p8` (only possible once). Note the Key ID and the Issuer ID.
2. Create the app record once: **Apps → + → New App**, bundle ID `dev.talhabayhan.lexi` (register the
   identifier first under developer.apple.com → Identifiers if it is not listed).
3. Add the four Apple secrets (§2). Run **Actions → release-ios → Run workflow** (version e.g. `1.0.0`).
   CI archives with automatic signing (`-allowProvisioningUpdates` creates certificates/profiles through
   the API key) and uploads to TestFlight. Processing takes 5–30 min; then add testers in TestFlight.
4. Export compliance: the app uses only standard HTTPS. Answer the encryption question in App Store
   Connect once per build, or add `ITSAppUsesNonExemptEncryption = NO` to Info.plist later.

Without the secrets the workflow still produces an unsigned archive (proof that Release builds on CI).
This path is experimental: if signing fails, the job log names the missing certificate/profile; a Mac is
not required to fix it, but the API key must have the Admin role.

## 5. Google Play testing rule for new personal accounts

Personal developer accounts created after 13 Nov 2023 must run a **closed test with at least 12 testers
who stay opted in for 14 consecutive days** before *Production* unlocks (organisation accounts are exempt).
Plan it right after the first internal build:

1. Internal testing (instant, up to 100 testers) → check the build.
2. Closed testing → create a track, add ≥ 12 testers by e-mail list or Google Group, share the opt-in link.
3. Keep ≥ 12 opted in for 14 days and ask them to really use the app; then apply for production access in
   Play Console (Dashboard) and answer the questions about the test.

VERIFY the current wording in Play Console Help before starting; the rule has changed before (was 20).

## 6. Versioning and releasing

- Tags `vMAJOR.MINOR.PATCH`; `versionCode = major*10000 + minor*100 + patch` (`docs/CONVENTIONS.md`).
  CI sets Android `versionName`/`versionCode` and iOS `MARKETING_VERSION` from the tag; the iOS build
  number is `versionCode.runNumber`, so re-running a tag still uploads.
- Every upload needs a higher versionCode than any previous one on Play: bump PATCH for a re-release.
- Release:
  ```bash
  git pull
  git tag v1.0.0
  git push origin v1.0.0
  ```
  Both release workflows start. Artifacts (AAB, R8 `mapping.txt`, iOS archive) are under the run's
  **Summary → Artifacts**. Keep `mapping.txt` (Crashlytics/Play de-obfuscation; CI also uploads it to Play).
- Manual run without a tag: **Actions → release-android → Run workflow**, enter a version.

## 7. Rollback

Stores cannot install a lower versionCode over a higher one, so rollback = **fix forward**:

- Play: halt the staged rollout (Release overview → *Halt rollout*), then ship `vX.Y.(Z+1)` built from the
  last good commit (`git revert` the bad commits on `main`, tag, push). A tester track can be pointed back
  to an older release only if nobody installed the newer one.
- App Store: **remove from sale** is drastic; prefer *Pause phased release*, then submit the fixed build
  (request expedited review if critical). TestFlight builds can be expired individually.
- Never move or delete a pushed tag; create a new one.

## 8. Website (GitHub Pages)

One-time: repo **Settings → Pages → Build and deployment → Source: GitHub Actions**. Then
**Actions → pages → Run workflow** (later it runs on every change to the legal drafts or `docs/site`).
URL: `https://talhabayhn.github.io/lexi/` — EN at the root, TR under `/tr/`; privacy at `privacy/`,
terms at `terms/`, support at `support/`. Use these as the store Privacy Policy / Support URLs.
Before the store submission: fill the `[PLACEHOLDERS]` and remove the DRAFT banners in
`docs/store/*.md` (lawyer review, `owner-checklist.md`).
Local preview: `python docs/site/build.py --out _site` and open `_site/index.html`.

## 9. Store graphics and videos

**Regenerate (after any UI or colour change):** GitHub → **Actions → store-graphics → Run workflow** (~20 min).
It re-records every Roborazzi screenshot with the current `LexiTheme`, records real gameplay footage
(`StoreFootageCapture`), builds all images and videos, and uploads the **`store-graphics`** artifact
(run page → Summary → Artifacts → download zip). The same files are committed under `docs/store/graphics/`.
Locally (needs footage from CI for the videos): `pip install pillow numpy`, then
`python tools/store-graphics/build.py` and `python tools/store-graphics/video.py` (ffmpeg on PATH).
Required sizes are listed in the `build.py` header.

| File | Store | Where to upload |
|---|---|---|
| `play/icon-512.png` | Play | Play Console → Grow users → Store presence → Main store listing → App icon |
| `play/feature-graphic-{tr,en}.png` (1024×500) | Play | same page → Feature graphic (per language: Translations / Manage translations) |
| `play/phone/{tr,en}-01…08.png` (1080×1920) | Play | same page → Phone screenshots (2–8, in this order) |
| `play/promo-video-{tr,en}-1920x1080.mp4` | Play | upload to **YouTube** (Public or Unlisted, ads/monetisation off, embeddable), paste the video URL into Main store listing → Video |
| `appstore/iphone-6.9/{tr,en}-01…08.png` (1320×2868) | App Store | App Store Connect → app → version page → Previews and Screenshots → iPhone 6.9" Display (per localization: Turkish / English) |
| `appstore/iphone-6.5/…` (1284×2778) | App Store | only if App Store Connect asks for the 6.5" set |
| `appstore/preview/{tr,en}-886x1920.mp4` | App Store | same section, drag into the 6.9" slot as App Preview (15–30 s, H.264, ≤ 500 MB). Optional: choose the poster frame |
| `appstore/app-icon-1024.png` | App Store | not uploaded: the icon comes from the build (`iosApp/.../AppIcon.appiconset`); the file is for reference/marketing |

Upload from Windows: open the files from the downloaded artifact (or the repo folder) and drag them into the
browser pages above. Play accepts PNG/JPEG without alpha (all files here are RGB); App Store accepts PNG/JPEG
screenshots and .mp4 previews. Apple requires app previews to show the app itself; these videos use only
real app frames and the app's own sounds.
