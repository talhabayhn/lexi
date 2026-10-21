# ADR 0006: Firebase Analytics + Crashlytics behind interfaces

- Status: Accepted (amended by 0023: interfaces live in `core:domain`; SDK integration and privacy defaults)
- Date: 2026-10-14

## Context
We need crash reports and product analytics from day one; Firebase is the mobile standard and free.

## Decision
`Analytics` and `CrashReporter` interfaces in core:platform; Android impl with Firebase SDK, iOS impl in Swift passed in at startup. Analytics only after consent where required.

## Consequences
Two vendors (Firebase + Supabase) — acceptable because they serve different purposes. Swappable (e.g. Sentry KMP) without touching features.
