---
description: Plan and implement a roadmap feature end-to-end with the right agents
argument-hint: <feature description or ROADMAP task>
---
Feature: $ARGUMENTS

1. `architect` agent: produce a plan (modules, interfaces, files, tests, risks). Show me the plan and wait for "OK" if it adds a module/dependency or changes GAME_RULES.
2. Domain logic → `game-engine-engineer` (tests first). Data → `data-engineer`. Platform → `platform-integrator`. UI → `ui-implementer`.
3. `test-engineer` fills gaps; `./gradlew build` green.
4. `code-reviewer` must APPROVE.
5. Update docs/ROADMAP.md and any spec that changed; commit with Conventional Commits.
6. Final summary in Turkish (what changed, how to try it, open questions).
