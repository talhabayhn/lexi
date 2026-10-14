# ADR 0011: Roborazzi screenshot tests as design-fidelity gate

- Status: Accepted
- Date: 2026-10-14

## Context
The product owner expects screens identical to the design.

## Decision
Each designed screen has a Roborazzi screenshot at 390×844dp compared with docs/design/screens/*.png during review; recorded goldens guard regressions.

## Consequences
Needs Robolectric on Android unit tests; screenshots reviewed by humans for design match (not pixel-diffed against the HTML export).
