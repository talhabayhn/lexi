# Design handoff — how to use these files

The canonical design lives on a Claude Design canvas ("Lexi UI Yönleri"). This folder is the frozen export the
app is built from. **The goal is a 1:1 implementation.**

```
docs/design/
├── screens/   NN-name.png   ← visual truth (2× PNG of the 390×844 frame)
│              NN-name.html  ← rendered static HTML: every color, size, border, gap as concrete CSS
├── source/    NN-name.dc.html ← original canvas source incl. data/logic (e.g. wheel geometry, list data)
├── fonts/     Jost-{Medium,SemiBold,Bold,ExtraBold}.ttf + OFL.txt
├── tokens.md      colors, type, spacing, borders, shadows, shapes
├── components.md  component library spec
├── motion.md      animations, easings, haptics, sounds
└── screen-map.md  design ↔ route ↔ module, design debt list, responsive rules
```

## Procedure for implementing a screen (the `ui-implementer` agent follows this)
1. Open `screens/NN-name.png` (look at it) **and** read `screens/NN-name.html`.
2. Write down the element tree: containers, sizes, paddings, gaps, borders, shadows, fonts. Map every value to a
   token in `tokens.md`. A value with no token → add it to the theme (with a comment pointing at the screen), never
   inline it in the feature.
3. Check `source/NN-name.dc.html` for logic (loops, computed styles, geometry formulas).
4. Build the stateless `XxxScreen(state, onIntent)` with preview data equal to the design's sample data.
5. Record a Roborazzi screenshot on a 390×844dp device (`@Config(qualifiers = "w390dp-h844dp-xxhdpi")`) and put
   it side-by-side with the PNG. Fix every visible difference (position ±1dp, color exact, font weight exact).
6. Wire the ViewModel, real data, animations per `motion.md`.

## Rules
- 1 CSS px = 1 dp; font px = sp; `letter-spacing: .14em` = `0.14.em`.
- Device frame corner radius (36) and canvas titles are NOT part of the UI.
- Annotation boxes are NOT UI: the white "Uygulamada: …" box at the bottom of `14-splash`, and the caption lines
  under each tile in `15-motion` (their content is captured in `motion.md`).
- Animated boards (`14-splash`, `15-motion`) are captured mid-animation; the keyframes in `source/` are the truth.
- System status/navigation bars are not drawn in the design: apply `WindowInsets.safeDrawing` padding on top
  of the design paddings; backgrounds and decorative shapes extend edge-to-edge.
- Sample texts in the design are real copy candidates — move them to string resources (TR), add EN.
- `[FİYAT]` etc. are placeholders → come from RevenueCat.

## Updating the design
When the canvas changes, ask Claude (claude.ai) to re-export. The renderer used for the export is
`tools/design-export/render.js` (Playwright): `node render.js <srcDir> <outDir> <Jost.ttf> "Name=NN-name,..."`.
Commit design updates in a separate commit: `docs(design): update 04-game`.
