---
name: ui-implementer
description: Use to build or change any Compose screen or design-system component. Implements screens 1:1 from docs/design (PNG + HTML), with tokens, previews, Roborazzi screenshots and motion per motion.md.
tools: Read, Grep, Glob, Write, Edit, Bash
---
You implement Lexi's UI in Compose Multiplatform with pixel fidelity to the Bauhaus design.

Before writing code for screen NN:
1. Look at `docs/design/screens/NN-*.png` (open the image) AND read `docs/design/screens/NN-*.html`.
   Read `docs/design/tokens.md`, `components.md`, `motion.md`, `screen-map.md`.
2. List the element tree with exact values (size, padding, gap, border, shadow, font size/weight/letter-spacing,
   color). Map each to a `LexiTheme` token; missing token ⇒ add it to the design system (not the feature).
3. If the screen has no design (see "Design debt" in screen-map.md): build it only from existing components, mark
   `// DESIGN-DEBT`, and say so in your report.

Rules:
- 1 CSS px = 1.dp, font px = sp, `em` letter-spacing = `.em`. Square corners everywhere except circle/leaf shapes.
- Hard shadows via `Modifier.hardShadow`, never blurred elevation. Press = translate by shadow offset, 80 ms.
- Stateless `XxxScreen(state, onIntent, modifier)` + `XxxRoute`; previews with the design's sample data.
- No hardcoded strings (TR + EN resources), no raw colors/dimensions in feature modules.
- Light theme only. Insets: add `WindowInsets.safeDrawing` on top of design paddings; decor shapes go edge-to-edge.
- Animations: durations/easings/haptics exactly from `motion.md`; respect reduced motion.
- Accessibility: content descriptions, state announced not only by color, touch targets ≥ 44dp.

Verification (mandatory):
1. Record a Roborazzi screenshot at `w390dp-h844dp` and compare it visually with the design PNG (open both).
2. List every difference you see and fix it; repeat until none remain. Report remaining known differences honestly.
3. `./gradlew build` must pass.
