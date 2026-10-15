# Component library (`core:designsystem`)

Visual reference: `screens/16-components.png` (+ `.html` for exact values). Build these first (milestone M1),
each with a `@Preview` and a Roborazzi screenshot. Feature screens compose ONLY these + layout primitives.

| Component | Variants / states | Notes |
|---|---|---|
| `LexiButton` | `Primary` (red, white text), `Secondary` (white), `Pass` (yellow), `Ink` (ink bg, yellow/cream text), `Blue` (notebook review CTA); states: normal, pressed, disabled (dashed `borderMuted`, `disabledFill`, `inkMuted` text) | 3dp border, square, hard shadow s5 (s6 for hero CTAs); pressed = translate + shadow 0 |
| `LexiTextLink` | — | underlined, 700 |
| `NavSquareButton` | back, close, settings, pause | 44dp square, white fill, 3dp border, icon 18–20 — **navigation = square** |
| `ActionCircleButton` | joker (red), others; disabled (50 %) | 56dp circle — **game actions = circle** |
| `IconSquareButton` | backspace | 56dp square |
| `WheelLetter` | Todo, Current, Correct, Wrong, Passed | 28dp chip, 13sp/800; shapes per tokens; Current scale 1.3; optional `WheelChipMotion` (#3 pop + 4dp ring, #5 turn + morph) |
| `LetterWheel` | 26 (or N) letters on a circle | 320dp box, radius 144, angle = −90° + i·360°/N, 288dp ring 3dp, centre 180dp yellow circle with big letter + rule label ("İLE BAŞLAR" / "İÇİNDE GEÇER"); centre letter slides down on change (#6), #5 arrow (24 × 16) |
| `AnswerSlot` | Filled (ink bg, cream letter), Active (white, red border), Empty, Wrong (red bg, white letter), JokerFilled (blue bg) | 40×48dp, letter 26sp/800 |
| `LetterKey` | Normal, Pressed (yellow), Used (dashed, muted), RemovedByJoker (dashed, empty), disabled (input blocked: 50 %, no shadow — design debt) | height 50, 3dp border, shadow s3, 22sp/800, 5-col grid gap 8 |
| `ClueCard` | —, with example (Örnek cümle joker, design debt: italic 14/500 line) | white, 3dp, shadow s6, label + clue 30sp, right blue column 64dp with level + letter count |
| `ComboBadge` / `ComboBurst` | "×3 SERİ" badge; four burst shapes (#7, `15-motion`) | badge: yellow, 3dp border, s4, padding 8 × 14, 24sp/800; shapes 26dp (+2dp border), offsets from `aB1`–`aB4` |
| `TimerBar` | normal (yellow fill, ink counter), warning = last 10 s (red fill, red counter pulsing 1 → 1.12 → 1 in 600 ms each time it changes, i.e. each second; colours only under reduced motion) | 48dp high, 3dp border, fill + right border |
| `ScoreBadge` | — | 48dp blue circle, white 15sp/800 |
| `SegmentedControl` | 2–6 segments | selected = ink bg + yellow text; dividers 3dp |
| `LexiSwitch` | on (blue track, yellow knob, right), off (cream track, ink knob, left) | 54×30 |
| `ProgressBar` | thin (8dp), thick (12dp), level card style | 2dp border, fill blue/ink |
| `LevelBadge` | A1 red, A2 white, B1 yellow, B2 blue, C1 white, NAT ink | small square chip |
| `LevelCard` | normal, selected (red bg + s6) | 2-col grid, 150dp high, shape icon + progress |
| `MenuTile` | white, ink (with red shadow) | 110dp, shape icon + title + subtitle, shadow s4 |
| `ListRow` / `ListCard` | with chip, with speaker button, with toggle, with value+chevron | rows separated by 2dp ink dividers inside a 3dp bordered white card |
| `WordRow` | notebook: word · tr, level · due, SRS bars (4), speaker | bars colored by box (red/yellow/blue) |
| `StatTile` / `StatGrid` | 2×2 grid (2dp dividers), 1×3 on the result screen (3dp) | shape + big number 30sp + label |
| `BarChart` | 7 bars, today outlined | see `09-stats` |
| `Toast` | success (blue), info (white with undo link) | top, shadow s4 (16-components.html) |
| `ConfirmDialog` | — | white, 3dp, shadow s8, red square icon, title 24/800, body, two buttons |
| `BottomSheet` / `BottomSheetPanel` / `ModalScrim` | joker sheet | cream, 4dp top border, scrim 55%; overlays at the screen root |
| `JokerRow` / `StatusPill` | enabled, disabled (50 %, no shadow) | joker sheet row: 40dp count chip, title 17/800, text 13/500, cost chip; white pill "Süre işlemeye devam ediyor · 1:31" (`05-joker-sheet`) |
| `ReviewRow` / `ReviewMoreRow` | wrong (red square), passed (yellow leaf) | result list row: 34dp letter chip, word 19/800, "clue · *example*", 44dp speaker; "+N kelime daha · Tümünü gör →" (`06-result`) |
| `EmptyState` | — | dashed 3dp border, overlapping circle+square illustration |
| `LexiLogo` / `LexiWordmark` / `LexiLogoMark` / `LexiAppIcon` | full (4 blocks: Hero, Splash, Menu), compact wordmark, shape mark, icon option 1 | `13-logo-and-icon` |
| `DecorShape` | circle, square, halfPill, leaf, diamond | markers and background decoration; size includes the border (HTML markers are content-box) |
| `HardShadow` modifier | s3–s8, color | core primitive |
| `PressableSurface` | — | shared press animation + haptic + sound |

Implemented in `core/designsystem/.../component`; sizes from the screens live in `XxxDefaults` objects next to each
component. Screenshots: `ComponentGallery.png` reproduces 16-components at its own size and 2x, and
`*Components.png` lay the components out like 04-game, 03-level-select, 02-home, 08-notebook, 09-stats, 11-settings,
05-joker-sheet and 13-logo-and-icon at the PNGs' 2x scale for crop comparison.

Naming in code: the `Lexi` prefix is used only where a name would collide with Material/foundation (`LexiButton`,
`LexiSwitch`, `LexiTextLink`, `LexiLogo`…); the other components keep the names in this table.
Every component takes `modifier: Modifier = Modifier` as the first optional parameter and has no hardcoded strings.
