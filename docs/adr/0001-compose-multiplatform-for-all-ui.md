# ADR 0001: Compose Multiplatform for all UI

- Status: Accepted
- Date: 2026-10-14

## Context
The Bauhaus design must look identical on Android and iOS, and the team is one Android/KMP developer.

## Decision
Use Compose Multiplatform (shared UI) on top of Kotlin Multiplatform. Native Swift only for SDK bridges (Firebase, AdMob) and the app entry.

## Consequences
One UI codebase, pixel-identical screens, faster delivery. iOS-native feel is traded for brand consistency (acceptable for a game). Some iOS SDKs need Swift bridges.
