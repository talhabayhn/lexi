---
description: Implement a designed screen 1:1 (e.g. /implement-screen 04-game)
argument-hint: <NN-screen-name>
---
Implement the screen `$ARGUMENTS` using the `ui-implementer` agent.

1. Look up `$ARGUMENTS` in `docs/design/screen-map.md` (route, module, milestone).
2. Open `docs/design/screens/$ARGUMENTS.png` and read `docs/design/screens/$ARGUMENTS.html` and `docs/design/source/$ARGUMENTS.dc.html`.
3. Build stateless screen + route + ViewModel contract, previews with the design's sample data.
4. Record the Roborazzi screenshot (390×844dp), compare with the PNG, fix differences until none remain.
5. Run `./gradlew build`, then run the `code-reviewer` agent and address its findings.
6. Tick the task in `docs/ROADMAP.md` and summarise in Turkish what was done and any remaining differences.
