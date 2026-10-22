# QA A2 — 30-card spot check

Seed 20261022 (Python `random.Random`, cards sorted by id then shuffled, first 30). Criteria: docs/CONTENT.md §2 rules. Error rate: 0/30 content errors, 2 notes

- `a2-cheque` (answer QUILT): id does not match the answer (compiler warning only). Ids are stable, so no change.
- `a2-either`: `pos` is `phrase`; `adj`/conjunction would be more exact. Cosmetic, left.
