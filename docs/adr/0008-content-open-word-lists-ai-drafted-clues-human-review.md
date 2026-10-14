# ADR 0008: Content: open word lists + AI-drafted clues + human review

- Status: Accepted
- Date: 2026-10-14

## Context
Six levels × 26 letters × hundreds of cards; manual writing is too slow, pure AI is too error-prone.

## Decision
Word selection from licence-compatible open lists (NGSL family, CEFR-J — licence check before release); clues/examples drafted with AI, reviewed by the product owner; CSV in git; compiler validation.

## Consequences
Fast, reviewable, our own clue text. Licence attribution required in About screen.
