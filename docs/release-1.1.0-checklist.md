# Release 1.1.0 checklist (video flow)

Owner steps to ship `redesign/video-flow` (ADR 0027) as **1.1.0**. 1.0.13 is live in both stores. Everything that
writes to `main` happens **after** the merge; nothing on the branch bumps the version or records goldens for `main`.
Runbook details: `docs/RELEASE.md`. Open questions of the flow: `docs/design/video-flow-notes.md` §7 and §10.

## 0. Decide before merging

- [ ] **Design sign-off** on `docs/design/drafts/video-flow/actual/art-sheet.png` (20 category cards, 5 exam cards open
      and locked, art at 0.75x, the bottom-bar icons). Phase 4 redrew Seyahat, Deyimler, IELTS, Yalancı eşdeğerler and
      the invite icon.
- [x] **Thin categories**: the fill rule is in the engine and main's rebalance removed the thin pairs (merged in phase 7,
      `video-flow-notes.md` §13).
- [ ] **Art for the 8 new categories** (`video-flow-notes.md` §13): they show the Mixed fallback until drawn.
- [ ] **Real-device pass** (§5 below). The emulator misses the 16.6 ms P95 bar (`docs/PERF.md`); only a phone decides.
- [ ] **Flow vs Home v2** (`docs/design/` home v2 reference on `main`): this branch already carries the new Home.

## 1. Merge strategy

Merge commit, not squash: `git switch main && git pull && git merge --no-ff redesign/video-flow`, then push. Reasons:
the branch is about 40 small commits that are individually revertable (CLAUDE.md: atomic commits, no history rewrite), and the
tags `video-flow-phase1…4` stay reachable from `main`. A squash would orphan them and lose the per-phase revert points.
Write the merge message in the owner identity without any attribution line (`author-check.yml` fails the push
otherwise). `git merge origin/main` into the branch first if `main` moved (merge, never rebase); the branch is current
with `main` as of phase 7 (tag `video-flow-phase7-merged`).

## 2. After the merge, in this order (wait for green CI between steps)

1. **Version:** `scripts/bump-version.sh 1.1.0`, commit, push (`lexi.versionName`, versionCode 10100,
   `Version.xcconfig`; `checkIosVersionSync` guards it). No tag yet.
2. **Remove the old components** the redesign orphaned (`video-flow-notes.md` §7 item 5: `StartButton`, `LevelCard`,
   `CategoryCard`, `MixedWheelCard`, `StreakChip`, `PlayButton`, the old `DailyCard`, with the gallery and their goldens)
   and the now unused `wheelAdvanceDurationMillis` token. Own commit.
3. **Goldens (Linux CI only):** Actions → `screenshots-record` → run once with `commit` **off**, read the Summary, then
   again with `commit` **on** (it writes to `main`). Phase 4 changed these on purpose, expect them in the report:
   `CategoryArt`, `LexiIcons`, `FlowComponents` (+`_w320_fontScale200`), all 8 `HomeScreen*` (the Invite icon is in the
   bottom bar), `CategoryPicker`, `CategoryPickerEn`, `CategoryPicker_w320h568_fontScale200` and the `PlayDialog*`
   (Seyahat, Okul ve iş, Deyimler are on screen). New art classifies as SUSPECT (colour / moved element): look at the
   `golden-review` images, then pass the reviewed paths in `accept`. Never record on macOS.
4. **Baseline profile** (needs an emulator, so CI): Actions → `perf` → `commit` **on** (writes
   `androidApp/src/release/generated/baselineProfiles/` to `main`). The profile tour is the app's own `PerfTour`
   through the real navigation, so it picks up the new Home, picker and game. Locally on a machine with an emulator:
   `./gradlew :androidApp:generateBaselineProfile -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
   -Pandroid.experimental.testOptions.managedDevices.setupTimeoutMinutes=20` (managed device `pixel6Api34`), then
   commit the changed files under `generated/baselineProfiles/`. Compare cold start with
   `:benchmark:pixel6Api34BenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark
   -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR` (`StartupBenchmark`).
5. **Store graphics:** the sources are `feature/home/HomeScreen.png`, `feature/levels/CategoryPicker.png`,
   `CategoryPickerEn.png`, the English `store:home/Home.png` capture and the 4x `StoreHiResCapture` of the picker and
   Home; the Game, Result, Notebook, Stats and Joker shots are unchanged. The `store-graphics` workflow re-records
   them itself, so step 3 is only a prerequisite for a *local* `tools/store-graphics/build.py` run (it falls back to
   the committed 2x goldens). Actions → `store-graphics` (`commit` defaults to **on**, writes `docs/store/graphics` to
   `main`), then upload per `RELEASE.md` §9. Decide whether a store screenshot should show the new mode / time dialog
   (`PlayDialogClassic`) instead of the plain grid; the captions in `build.py` (`SCREENS`) stay valid for the grid.
6. **Release:** check that the push to `main` produced a green unsigned R8 AAB (`release-android.yml`), then
   `git tag v1.1.0 && git push origin v1.1.0` (both release workflows). Keep `mapping.txt`.

## 3. Store re-check (ROADMAP "Play Console re-check")

The video flow adds no SDK, permission, account, ad or data collection: the invite is the system share sheet with the
existing store link, and the animations are local. So no new declaration is expected, but go through the list before
submitting 1.1.0 anyway, because 1.1.0 is a feature release:

- [ ] Play **Data safety** and Apple **App Privacy** still match the build (`docs/store/data-safety-google.md`,
      `app-privacy-apple.md`): Firebase analytics + Crashlytics, no AdMob, no RevenueCat.
- [ ] Play **Ads** = No, **Advertising ID** = No (no `AD_ID` in the merged manifest), **Content rating** and **Target
      audience** unchanged, privacy policy and terms URLs live.
- [ ] No Pro / ad / subscription wording in the listing (`FeatureFlags.proEnabled` is off in release). The locked
      YDS / YKS-DİL / TOEFL / IELTS cards ("YAKINDA") shipped in 1.0.x as locked exam rows (`84a1ef7`), so they are not new.
- [ ] Notifications unchanged (local reminders only).
- [ ] Listing texts: What's New and the changed paragraphs in `docs/store/listing-*.md` (DRAFT blocks), then
      `python3 docs/store/check-limits.py`; replace screenshots and videos from step 5.

## 4. Rollback

Fix forward (`RELEASE.md` §7): halt the staged rollout, `git revert -m 1 <merge commit>` on `main` for the whole flow
(or revert single commits), bump to 1.1.1, tag. Never move a pushed tag.

## 5. Real-device test list

iPhone (Xcode build or TestFlight; at least an iPhone 8 / SE 2nd gen or 11 and a current one):
- [ ] Instruments → **Animation Hitches** on: Home → "Çarkını seç"; card flip open / close / interrupted by a second tap;
      OYNA → letter morph → board; a turn change with a pass. Target: no hitch above 100 ms, hitch time ratio in the
      green band; compare with the emulator numbers in `PERF.md`.
- [ ] **Low Power Mode** on (flow starts in tier Lite: card scale + fade, letter without pop) and **Reduce Motion** on
      (tier Minimal: cross-fades, no morph). Both must still start a game with the clock correct.
- [ ] Swipe-back while the morph runs cancels it and stays on the picker; app switcher during the morph.
- [ ] Largest Dynamic Type on the smallest iPhone: Home points, dialog (OYNA reachable), no split words.
- [ ] Invite opens the share sheet with the store link.

Android: an Android 8 / low-RAM phone (tier Lite from the start), a mid-range Samsung A-series, a Pixel:
- [ ] Macrobenchmark on a USB device, release-like build:
      `./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest
      -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark
      -Pandroid.testInstrumentationRunnerArguments.class=dev.talhabayhan.lexi.benchmark.VideoFlowBenchmark`
      (no `suppressErrors` on a real phone; results in `benchmark/build/outputs/`). Bar: P95 < 16.6 ms, no frame > 100 ms.
- [ ] Debug build: `MotionDebugOverlay` (frame times + tier switch) to watch the FrameMonitor downgrade to Lite.
- [ ] Back button during the card flip and during the morph; home button mid-morph and return; the font size or
      language changed in system settings while the picker dialog is open (the activity is recreated).
- [ ] Font scale 2.0 and the smallest screen you have.
- [ ] Compare the screen recording with `docs/design/drafts/video-flow/actual/flow-android.mp4` and the prototype.
