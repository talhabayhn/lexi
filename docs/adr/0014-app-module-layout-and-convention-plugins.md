# ADR 0014: App module layout (androidApp + shared) and build-logic convention plugins

- Status: Accepted
- Date: 2026-10-14

## Context
ADR 0009 decided on a multi-module build with convention plugins, and the docs originally assumed a single
`composeApp` module. The Android Studio wizard (AGP 9) instead generates `androidApp` (an Android application
module) and `shared` (a KMP library on `com.android.kotlin.multiplatform.library`), because AGP 9 no longer allows
the KMP and Android application plugins in one module.

## Decision
- **`androidApp`**: thin Android shell (MainActivity, manifest, launcher icons, applicationId). Depends only on
  `shared`.
- **`shared`**: KMP umbrella: `App()`, navigation graph, Koin start, iOS framework export (`Shared`, static). It is
  the only module allowed to depend on everything.
- **`iosApp`**: Xcode project (Swift entry + native SDK bridges), builds the framework via
  `:shared:embedAndSignAppleFrameworkForXcode`.
- **`build-logic`** (included build, `pluginManagement { includeBuild("build-logic") }`) with convention plugins
  that read versions from the shared `gradle/libs.versions.toml`:
  - `lexi.kmp.library`: KMP + AGP 9 KMP library plugin; Android (namespace from path, compile/minSdk from the
    catalog, JVM 17, host tests with resources), `iosArm64`, `iosSimulatorArm64`; explicit API for `core:*`;
    `kotlin-test` in `commonTest`.
  - `lexi.kmp.compose`: `lexi.kmp.library` + Compose Multiplatform + compose compiler; Android resources on;
    compose runtime/foundation/ui/resources/preview; `Res` class in `<namespace>.generated.resources`.
  - `lexi.android.application`: AGP application + compose compiler; SDK levels from the catalog, Java/JVM 17.
  - Added with the module skeleton: `lexi.feature`, a pure-Kotlin domain convention, a JVM application convention
    for `tools:content-compiler`, and the dependency-rule check.
- AGP and Kotlin plugin jars are `compileOnly` in build-logic; their versions come from the root build's
  `alias(...) apply false` block, so every module shares one plugin classloader.
- Feature-specific plugins (SQLDelight, Roborazzi) stay applied in the module that needs them until a second module
  needs the same setup.

## Consequences
- New modules need only a plugin id and their own dependencies; SDK levels, JVM target, targets and namespaces are
  changed in one place.
- `withHostTest` can only be enabled once per module, so `lexi.kmp.library` always enables it (with resources);
  convention plugins must not call it again.
- `shared`'s compose resources moved from `lexi.shared.generated.resources` to
  `dev.talhabayhan.lexi.shared.generated.resources`.
