# ADR 0015: Logging (Kermit), settings storage (DataStore) and test stack

- Status: Accepted
- Date: 2026-10-14

## Context
ARCHITECTURE.md and CONVENTIONS.md already name Kermit for logging, DataStore Preferences for settings and
Turbine/fakes for tests, and require immutable collections in UI state, but no ADR recorded these libraries.
The module skeleton adds them to the version catalog and creates `core:testing`.

## Decision
- **Logging:** Kermit 2.2.0 (`co.touchlab:kermit`) behind `Logger.withTag("…")`; no `println`.
- **Settings:** DataStore Preferences 1.2.1 (`androidx.datastore:datastore-preferences-core`, KMP incl. iOS) in
  `core:data`, exposed through `SettingsRepository` (`Flow<Settings>`).
- **Tests:** `kotlin.test` everywhere; kotlinx-coroutines-test 1.11.0 and Turbine 1.2.1 for flows and ViewModels;
  hand-written fakes in `core:testing` (pure KMP: `jvm` + iOS), which re-exports `core:domain`, kotlin-test,
  coroutines-test and Turbine as `api` and may only be used from test configurations (module rules).
- **UI state collections:** kotlinx-collections-immutable 0.5.2.
- Other kotlinx libraries in the catalog: coroutines 1.11.0, datetime 0.8.0 (time types from `kotlin.time`),
  serialization 1.11.0.

## Consequences
- Libraries are only declared in the catalog until a module uses them; adding them to a module needs no new ADR.
- Alternatives not taken: Napier (logging), multiplatform-settings (settings), MockK/Mokkery (mocks; fakes keep
  tests deterministic and work on all targets).
