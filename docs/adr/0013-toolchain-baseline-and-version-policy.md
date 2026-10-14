# ADR 0013: Toolchain baseline and version policy

- Status: Accepted
- Date: 2026-10-14

## Context
The Android Studio wizard generated the project with Kotlin 2.4.20, Compose Multiplatform 1.12.1, AGP 9.1.1 (with the
new `com.android.kotlin.multiplatform.library` plugin), Gradle 9.5.1, minSdk 24, JVM target 11, iOS deployment
target 18.2 and an alpha material3. Our docs require minSdk 26 and iOS 16+, and we want no pre-release dependencies
unless there is no stable alternative. Before building convention plugins on top of AGP 9's KMP library plugin we ran
a compatibility spike (ROADMAP M0).

## Decision

### Baseline
| Item | Version | Note |
|---|---|---|
| Kotlin | 2.4.21 | latest stable bug-fix release |
| Compose Multiplatform | 1.12.1 | latest stable (1.13 is alpha) |
| AGP | 9.1.1 | pinned to what Android Studio 2026.1.1 syncs; bump together with Studio |
| Gradle | 9.5.1 | inside the range tested by KGP 2.4.2x |
| Build JDK | 21 (Azul, Gradle daemon toolchain; CI: Zulu 21) | |
| Bytecode target | JVM 17 | AGP 9 baseline |
| Android | minSdk 26, compile/targetSdk 37 | |
| iOS | deployment target 16.0; targets `iosArm64`, `iosSimulatorArm64` | **no `iosX64`**: only Intel-Mac simulators need it, CI and current Macs are arm64 |
| material3 (JetBrains) | 1.9.0 (stable) | see below |

### Version policy
- Latest **stable** versions, recorded in `gradle/libs.versions.toml`. Bumps are their own commit.
- Pre-release dependencies only when no stable release works with the baseline. Each one is listed here with an
  upgrade condition. Current exceptions:
  - JetBrains `navigation-compose` **2.10.0-rc01** (pinned; added with feature:home). The only stable release, 2.9.2,
    pulls lifecycle 2.9 / savedstate 1.3 / compose 1.9 and conflicts with the CMP 1.12 set. Upgrade to 2.10.0 as soon as
    it is stable.
- **material3:** use the stable 1.9.0, never the alpha. If an API we need only exists in an alpha, it is raised with
  the product owner first. If 1.9.0 ever causes a KLIB/iOS link problem with the CMP version in use, material3 is
  dropped entirely (our components are custom, the bottom sheet will be custom too) instead of chasing workarounds.
- detekt is postponed (ADR 0012).

### Spike results (2026-10-14)
| Check | Result |
|---|---|
| SQLDelight 2.4.1 on `com.android.kotlin.multiplatform.library` (`core:data`; jvm + android + iOS) | ✅ works out of the box: code generation, jvmTest with the in-memory JDBC driver, iOS klib compile |
| SQLDelight native driver: link + `iosTest` on macOS CI | ✅ `iosSimulatorArm64Test` (incl. `core:data` native-driver round trip) green on macos-26 / Xcode 26.6 |
| Compose Multiplatform + compose resources on the KMP library plugin (`core:designsystem`, `androidResources.enable = true`) | ✅ works; the `Res` class is generated per module (`packageOfResClass`) |
| Roborazzi 1.76.0 + Robolectric 4.17 host test (`recordRoborazziAndroidHostTest`), 390×844 dp @xxhdpi | ✅ works with one workaround: Robolectric's SDK 36 runtime needs `--add-exports=java.base/jdk.internal.access=ALL-UNNAMED` on the test JVM (JDK 17+). Tests use `@Config(sdk = [36])`; Robolectric 4.17 has no SDK 37 runtime yet |
| material3 1.9.0 with CMP 1.12.1: Android render (Roborazzi) | ✅ |
| material3 1.9.0 with CMP 1.12.1: iOS framework link on macOS CI | ✅ `linkDebugFrameworkIosSimulatorArm64` and `xcodebuild` of `iosApp` (deployment target 16.0) green; material3 is kept |
| Configuration cache with SQLDelight, Roborazzi, Compose resources | ✅ entries stored and reused |

Known baseline noise: Kotlin/Native prints `KLIB loader: The same 'unique_name=…' found in more than one library` for
`annotation`, `collection`, `savedstate`, `savedstate-compose` and `navigationevent-compose`. They come from the
official CMP 1.12.1 + lifecycle 2.11.0 set itself (e.g. `runtime-saveable:1.12.1` depends on JetBrains `savedstate`
1.3.6 while `androidx.savedstate` 1.4.0 is also on the graph); they were present in the untouched wizard project too.
We accept them, do not force `-Xklib-duplicated-unique-name-strategy`, and re-check on every CMP bump.

## Consequences
- Convention plugins (next M0 step) can rely on AGP 9's KMP library plugin for every Android-facing module.
- Screenshot goldens are rendered by Robolectric; goldens recorded on different OSes can differ slightly in font
  rasterisation. Decide in M1 whether CI verifies screenshots (then record goldens on CI/Linux) or only records.
- Updating AGP requires updating Android Studio first.
