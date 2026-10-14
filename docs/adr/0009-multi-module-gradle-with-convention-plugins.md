# ADR 0009: Multi-module Gradle with convention plugins

- Status: Accepted
- Date: 2026-10-14

## Context
Boundaries (pure domain, no data access from features) must be enforced; build times matter.

## Decision
Modules per ARCHITECTURE.md §2 with build-logic convention plugins.

## Consequences
Some Gradle setup cost up front; clear ownership for agents and parallel work.
