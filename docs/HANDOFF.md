# Lexi hand-off (state of 2026-11, owner: talhabayhn)

## Live and branches
- Stores: 1.0.14 (iOS + Android). main already has the category rebalance stages 1-4 (contentVersion 40); release 1.0.15 is being prepared by an agent run that stops at a "READY FOR GO RELEASE" gate (also includes a small fix for the old category screen: single column at font scale >= 1.3, shorter EN names).
- redesign/video-flow: new flow (Home -> "Çarkını seç" -> flip dialog -> letter morph -> game), phases 1-5 done (tags video-flow-phase1..5), NOT merged. redesign/home-v2: the alternative Home design, NOT merged. main is never touched by those branches.
- Decision pending: video-flow vs home-v2, owner tests on a real phone first (installBenchmarkRelease build, see docs/PERF.md "Hand-test build").

## Pending work (in order)
0. 1.1.0 candidate (2026-11-18): `release/1.1.0-rc` = video-flow + main 1.0.15, Linux goldens and the baseline profile committed on the branch from CI artifacts, release notes in `docs/store/release-notes-1.1.0.md` (since 1.0.13, the last published version). Waits for GO RELEASE: then `release-1.1.0-checklist.md` §2 steps 1, 2, 5, 6.
1. Finish the 1.0.15 run, owner types GO RELEASE, the build goes to TestFlight / Play closed test; the owner submits in the consoles.
2. Prompt N on redesign/video-flow: make the automatic quality-tier downgrade less trigger-happy and reversible (default must stay Full; the debug tier chip never ships). Ignore cold-start transitions, require two slow transitions or 5 slow frames, re-upgrade after 3 good transitions, never override Minimal/forced/lowRam/lowPower, show the reason in the debug chip, tests + docs, tag video-flow-phase6.
3. Merge main into video-flow (merge, never rebase), fix PickerCategoryUi.firstLetter so the morph letter comes from the domain wheel (thin categories are now filled), draw illustrations for the new categories (hobbies, feelings, society-law, economy, clothes-shopping, verbs, adjectives, ideas), regenerate the baseline profile, record goldens with screenshots-record (Linux CI) AFTER the merge, regenerate store graphics (tools/store-graphics), then release as 1.1.0.
4. Owner decisions still open: invite icon and category art sign-off, whether in-game motion #17 stays.

## Hard rules learned (keep)
- One Gradle at a time (`./gradlew --stop`); separate git worktrees on named branches for parallel work; never a detached HEAD.
- main only gets code commits after CI is green: push a temporary branch, run build.yml and screenshots.yml by workflow_dispatch, fast-forward main, delete the branch. ios.yml is path-filtered and has a nightly run; screenshots-record with commit enabled writes goldens to main: only use it on main.
- Never force-push, never move/delete a published tag. Release = scripts/bump-version.sh X.Y.Z (changes gradle.properties AND iosApp/Configuration/Version.xcconfig; run ./gradlew checkIosVersionSync), push to main, wait for green, tag vX.Y.Z; release-ios and release-android run on the tag. A duplicate-versionCode failure of the main-push release-android run is expected. "Maximum number of certificates" on release-ios: the owner revokes old Apple certificates, then `gh run rerun <id> --failed`.
- Releases are batched: never cut a build per task; agents never bump/tag on their own, they report "release candidate ready" and wait for the owner's explicit "GO RELEASE".
- Store consoles (App Store Connect, Play Console) are manual; production publishing is always the owner's step.
- Content: AI drafts, owner approves in review sheets (docs/content-review/*), ids never change, only the intended column changes (verify with a script), qa.py must report 0 errors.
- Commits: Conventional Commits, English, identity talhabayhn <htbayhan@gmail.com>, no AI attribution (a hook enforces it).
- Debug-only developer tools (tier chip, frame graph) must never be compiled into release builds.
