# ADR 0002: Pure, deterministic GameEngine with event sourcing

- Status: Accepted
- Date: 2026-10-14

## Context
Daily wheel, same-device duel, online rooms, async challenges and replays all need the same game on every device.

## Decision
GameEngine is a pure reducer in core:domain: `reduce(state, event) -> (state, effects)`. Wheel and keys are generated from a seed with our own SplitMix64 RNG. Time enters only as Tick events.

## Consequences
Trivially testable, multiplayer = exchanging events, golden tests possible. Requires discipline: no hidden randomness or clock in the engine.
