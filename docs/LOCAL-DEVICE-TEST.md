# Local device test (Release build on your own phone, no TestFlight / Play)

Prefer this before cutting a TestFlight / Play build: it is the only way to see real Metal / GPU behaviour, frame times
and haptics. Always test the **Release** configuration (Debug builds of Kotlin/Native are much slower).

## iPhone (Xcode, USB)

1. Open the project **from the worktree / branch you want to test**: `<repo or worktree>/iosApp/iosApp.xcodeproj`
   (a project opened from another checkout builds that checkout's code). Pull / check out the branch first.
2. Signing, once per machine: `iosApp/Configuration/Local.xcconfig` (not committed; copy `Local.xcconfig.example`)
   with `TEAM_ID=<your Apple team id>`; or in Xcode: target **iosApp** → Signing & Capabilities → Team. Bundle id stays
   `dev.talhabayhan.lexi`.
3. Connect the iPhone by USB (unlock it, "Trust this computer"; Developer Mode on: Settings → Privacy & Security).
4. Scheme **iosApp** → Edit Scheme… → **Run** → Info → Build Configuration: **Release**; untick "Debug executable"
   (a debugger attached slows the app down). Close.
5. Pick the iPhone as the run destination, ⌘R. The first build compiles the Kotlin framework in Release (several
   minutes).
6. **First-launch tests:** delete the app from the phone first (long press → Remove App → Delete App), then ⌘R again.
   An update over an older build is a "first launch of this version" too. For a later-launch test: swipe the app away
   in the app switcher and open it from the home screen (not from Xcode).
7. The store / TestFlight build has the same bundle id: installing from Xcode replaces it (its data stays unless you
   delete the app). Delete the local build before going back to TestFlight.

What to look at (splash, ADR 0028): the launch logo stays still for a moment, then the shapes and blocks animate
smoothly (L slides, E bounces, X spins, I rises, tagline), four light taps, then the splash slides away and reveals the
first screen. Fresh install, kill and reopen, Low Power on and off, Reduce Motion on (static logo, short crossfade).

## Android (USB, optimised hand-test build)

```bash
adb devices
adb uninstall dev.talhabayhan.lexi            # the store build or an older hand-test build (its data goes with it)
./gradlew :androidApp:installBenchmarkRelease
adb shell cmd package compile -m speed-profile -f dev.talhabayhan.lexi
```

`benchmarkRelease` is the release build (R8, baseline profile) signed with the debug key, no telemetry, not debuggable
(details: `docs/PERF.md` § Hand-test build). For a first-launch test uninstall first; for a later launch swipe the app
away and reopen it.
