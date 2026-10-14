# ADR 0012: Static analysis (ktlint now, detekt later) and CI on GitHub Actions

- Status: Accepted
- Date: 2026-10-14

## Context
We develop trunk-based on `main` from a Windows-only machine. Windows can compile the iOS klibs but cannot link
the iOS framework, run simulator tests or run Xcode, so macOS verification must happen in CI. The repository may be
private, where macOS runner minutes cost 10x Linux minutes. For static analysis, the only stable detekt release
(1.23.x) is built against Kotlin 2.0 / AGP 8 and does not reliably support Kotlin 2.4 + AGP 9; detekt 2.0 is
still pre-release.

## Decision
- **Static analysis:** ktlint via `org.jlleitschuh.gradle.ktlint`, wired into `check` so `./gradlew build` runs it.
  detekt is postponed until a stable release supports our Kotlin version; then it gets added with `config/detekt.yml`.
- **`build.yml` (ubuntu-latest):** on every push to `main` (plus `pull_request`, manual). Runs
  `./gradlew build :androidApp:assembleDebug --continue`: unit tests, Android lint, ktlint (once added), Android
  assemble and the iOS klib compile. JDK Zulu 21 (matches the Gradle daemon toolchain), `gradle/actions/setup-gradle`
  cache (writes only from `main`), `~/.konan` cache keyed on the version catalog.
- **`ios.yml` (macos-26, Xcode 26.6 pinned via `DEVELOPER_DIR`):**
  - push to `main`, path-filtered to Kotlin/Gradle/resources/`iosApp` changes: link the debug simulator framework
    and run `iosSimulatorArm64Test`; `xcodebuild` of `iosApp` only when `iosApp/` or the workflow changed.
  - nightly (02:00 UTC): full run including `xcodebuild`, skipped by a cheap Linux pre-job when `main` had no
    commits in the last 24 h.
  - manual dispatch: full run.
- Both workflows use `concurrency` with `cancel-in-progress`, minimal `permissions`, and upload reports on failure.
- A red `build` run after a push is fixed before any new work. No branch protection that requires PRs.

## Consequences
- Every push gets Android + JVM + klib verification in ~10 min on Linux; iOS link problems surface on the same push
  when Kotlin code changes, Swift/Xcode problems at the latest the next night.
- Cancelled intermediate runs mean not every single commit is verified when pushing in quick succession; the latest
  commit always is.
- Adding detekt later requires a `config/detekt.yml`, a `lexi.quality` change and a docs update.
