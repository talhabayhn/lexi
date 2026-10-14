# ADR 0003: MVI / UDF with androidx ViewModel (KMP) and Koin

- Status: Accepted
- Date: 2026-10-14

## Context
Game state is complex and changes many times per second; screens must be previewable and screenshot-testable.

## Decision
Each screen has UiState (StateFlow), Intent and Effect types; stateless Screen composable; androidx.lifecycle ViewModel (multiplatform); DI with Koin (koin-compose-viewmodel).

## Consequences
Predictable state, simple tests with Turbine. Koin is runtime DI (no compile-time graph check) — mitigated with a Koin `verify()` test.
