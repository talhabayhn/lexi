# Lexi

Passaparola-style English vocabulary game (Turkish clue → English answer, A1 → Native) for Android & iOS,
built with Kotlin + Compose Multiplatform. Bauhaus Classic design.

- Start here: [`CLAUDE.md`](CLAUDE.md) · Product: [`docs/PRODUCT.md`](docs/PRODUCT.md) ·
  Architecture: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) · Rules: [`docs/GAME_RULES.md`](docs/GAME_RULES.md) ·
  Plan: [`docs/ROADMAP.md`](docs/ROADMAP.md) · Design: [`docs/design/README.md`](docs/design/README.md)

## Run
- Requirements: JDK 21 (Android Studio's bundled JBR works: set `JAVA_HOME` to `<Android Studio>/jbr`), Android SDK
  (copy `local.properties.example` to `local.properties` and set `sdk.dir`), Xcode 26 on a Mac for iOS.
- Android: `./gradlew :androidApp:installDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode → Run (builds the `Shared` framework via Gradle).
- Full check: `./gradlew build` (on Windows/Linux the iOS framework link and simulator tests are skipped; CI runs
  them on macOS).

## CI
GitHub Actions: `build` (ubuntu, every push to `main`) and `ios` (macOS, path-filtered + nightly). See ADR 0012.

## One-time setup after cloning
```bash
git config core.hooksPath .githooks   # enables the committed commit-msg hook
```

## Licences
Jost font © indestructible type*, SIL Open Font License 1.1 (`docs/design/fonts/OFL.txt`).
Word-list attributions: see `docs/CONTENT.md` §6.
