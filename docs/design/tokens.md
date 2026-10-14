# Design tokens — Bauhaus Classic (light only)

Unit mapping: **1 CSS px in the design HTML = 1.dp in Compose; font-size px = sp.** The design frame is
390 × 844 (iPhone 14 logical size). Do not scale by density yourself.

## Colors (`LexiColors`)
| Token | Hex | Use |
|---|---|---|
| `cream` | `#F2EBDD` | app background, "todo" wheel letters, secondary chips |
| `white` | `#FFFFFF` | cards, keys, inputs, list rows |
| `ink` | `#111111` | text, all borders, hard shadows, "current" letter, selected segment |
| `red` | `#D9281C` | primary CTA (OYNA, BAŞLA), **wrong** state, active answer-slot border |
| `blue` | `#1F4FBF` | **correct** state, score badge, progress fill, toggle on |
| `yellow` | `#F2C200` | **pass** state, timer bar, highlights, selected-segment text on ink |
| `inkMuted` | `#5A5A5A` | placeholder, disabled text |
| `borderMuted` | `#7A7468` | dashed border of disabled button / used key |
| `disabledFill` | `#E2DACB` | disabled button fill |
| `gridLine` | `#B8B0A0` | chart guide lines (dashed 1dp) |
| `scrim` | `#111111` @ 55% | behind bottom sheets / dialogs |

Text on colors: on `red`/`blue`/`ink` → `white` or `cream`; on `yellow`/`white`/`cream` → `ink`.
State is never color-only: correct = circle, wrong = square, pass = leaf, current = black square.

## Typography (`LexiTypography`) — font **Jost** (OFL), files in `docs/design/fonts/`
Weights used: 500 Medium, 600 SemiBold, 700 Bold, 800 ExtraBold. Copy the TTFs to
`core/designsystem/src/commonMain/composeResources/font/` (static instances; do not use the variable font).

| Style | Size / weight / extras | Seen in |
|---|---|---|
| `wheelLetter` | 96 / 800, lineHeight 0.9 | game centre letter |
| `scoreHero` | 76 / 800, lineHeight 0.95 | result score |
| `logo` | 40–48 / 800 | menu & splash logo blocks |
| `display` | 34 / 800, lineHeight 1.05 | onboarding headline |
| `clue` | 30 / 800, lineHeight 1.05 | game clue |
| `title` | 28 / 800 | screen titles |
| `button` | 20–30 / 800, letterSpacing 0.06–0.12em | big CTAs (OYNA 30, BAŞLA 24, PAS 22) |
| `key` | 22 / 800 | letter keys; answer slot letters 26 / 800 |
| `heading` | 18–20 / 800 | card titles, section titles |
| `bodyStrong` | 16–17 / 600–800 | list row labels |
| `body` | 15–16 / 500, lineHeight 1.5 | paragraphs |
| `caption` | 13–14 / 500–600 | secondary lines |
| `label` | 12–13 / 700, letterSpacing 0.14em, UPPERCASE | "İPUCU · İSİM", "OYUN MODU", section headers |
| `wheelChip` | 13 / 800 | wheel letters |

letterSpacing in `em` → Compose `.em` units directly (0.14em → `0.14.em`).
In code (`LexiTypography`), roles given as a range carry the font size as suffix so one CSS declaration maps to one
token: `logo48`/`logo40`, `button30`…`button16`, `heading20`/`heading18`, `bodyStrong17`…`bodyStrong14`,
`body16`/`body15`, `caption14`…`caption12`, `label13`/`label12`. Unitless CSS `line-height` is applied CSS-style
(half-leading split evenly, box shrinks below Jost's natural 1.445); unset = CSS `normal`. Labels are uppercase in
the string resources, never via `uppercase()` (Turkish İ).

## Spacing (`LexiDimens`)
Screen padding: horizontal 20, top 24 (game 20), bottom 28 (list screens 20).
Gap scale: 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 24. Use the exact gap from the screen HTML.
Min touch target 44 × 44.

## Borders
| Token | Width | Use |
|---|---|---|
| `hairline` | 1 | chart grid (dashed) |
| `thin` | 2 | chips, wheel letters, list dividers, inner cells |
| `regular` | 3 | default for buttons, cards, inputs, keys, slots |
| `thick` | 4 | splash logo blocks, bottom-sheet top edge |
| `heavy` | 5 | large logo |
All borders are `ink` unless stated (active slot border = `red`).
Box model as in CSS `border-box`: the border is inside a fixed size and content starts inside the border
(`PressableSurface` insets its content by the border width), so HTML paddings are used unchanged.

## Hard shadows (`LexiShadow`) — offset, no blur, ink unless stated
| Token | Offset | Use |
|---|---|---|
| `s3` | 3,3 | letter keys, list cards, small buttons |
| `s4` | 4,4 | menu tiles |
| `s5` | 5,5 | PAS, primary/secondary buttons, result CTAs |
| `s6` | 6,6 | hero cards (clue card, daily card, OYNA, level card selected) |
| `s8` | 8,8 | dialogs |
Colored variants: `s5Red` (Pro card in settings), `s4Red` (exam-packs tile on home).
**Pressed state:** the element translates by its shadow offset and the shadow collapses to 0 (80 ms) — see motion.md.

Implementation: `Modifier.hardShadow(offset: Dp, color: Color = ink, shape: Shape)` drawing the shape
offset behind the content (drawBehind), not `Modifier.shadow` (that blurs).

## Shapes (`LexiShapes`)
| Token | CSS | Compose |
|---|---|---|
| `square` | radius 0 | `RectangleShape` (default for everything) |
| `circle` | 50% | `CircleShape` |
| `leaf` | `border-radius: 50% 0 50% 0` | `RoundedCornerShape(topStartPercent = 50, topEndPercent = 0, bottomEndPercent = 50, bottomStartPercent = 0)` |
| `diamond` | square rotated 45° | `Modifier.rotate(45f)` on a square |
| `halfPill` | `border-radius: 0 80px 80px 0` | decorative side shapes |
App icon corner radius is platform-owned; never round UI corners otherwise.

## Iconography
Inline stroke icons, 2.4–3 stroke width, square caps, 16–22 size. Implement as `ImageVector`s in
`core:designsystem/icons` (back, close, pause, settings, bulb (joker), backspace, speaker, search, chevron, play).
No emoji, no Material icon fonts.

## Decorative shapes
Screens carry 1–2 large background shapes (circle / half-pill / square) partly off-screen; positions are in each
screen's HTML (`aria-hidden` divs). They are part of the design — reproduce them exactly, behind content.
