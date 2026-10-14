# Component library (`core:designsystem`)

Visual reference: `screens/16-components.png` (+ `.html` for exact values). Build these first (milestone M1),
each with a `@Preview` and a Roborazzi screenshot. Feature screens compose ONLY these + layout primitives.

| Component | Variants / states | Notes |
|---|---|---|
| `LexiButton` | `Primary` (red, white text), `Secondary` (white), `Pass` (yellow), `Ink` (ink bg, yellow/cream text); states: normal, pressed, disabled (dashed `borderMuted`, `disabledFill`, `inkMuted` text) | 3dp border, square, hard shadow s5 (s6 for hero CTAs); pressed = translate + shadow 0 |
| `LexiTextLink` | — | underlined, 700 |
| `NavSquareButton` | back, close, settings, pause | 44dp square, white fill, 3dp border, icon 18–20 — **navigation = square** |
| `ActionCircleButton` | joker (red), others | 56dp circle — **game actions = circle** |
| `IconSquareButton` | backspace | 56dp square |
| `WheelLetter` | Todo, Current, Correct, Wrong, Passed | 28dp chip, 13sp/800; shapes per tokens; Current scale 1.3 |
| `LetterWheel` | 26 (or N) letters on a circle | 320dp box, radius 144, angle = −90° + i·360°/N, 288dp ring 3dp, centre 180dp yellow circle with big letter + rule label ("İLE BAŞLAR" / "İÇİNDE GEÇER") |
| `AnswerSlot` | Filled (ink bg, cream letter), Active (white, red border), Empty, Wrong (red bg, white letter), JokerFilled (blue bg) | 40×48dp, letter 26sp/800 |
| `LetterKey` | Normal, Pressed (yellow), Used (dashed, muted), RemovedByJoker (dashed, empty) | height 50, 3dp border, shadow s3, 22sp/800, 5-col grid gap 8 |
| `ClueCard` | — | white, 3dp, shadow s6, label + clue 30sp, right blue column 64dp with level + letter count |
| `TimerBar` | normal (yellow), warning (red, pulsing counter) | 48dp high, 3dp border, fill + right border |
| `ScoreBadge` | — | 48dp blue circle, white 15sp/800 |
| `SegmentedControl` | 2–6 segments | selected = ink bg + yellow text; dividers 3dp |
| `LexiSwitch` | on (blue track, yellow knob, right), off (cream track, ink knob, left) | 54×30 |
| `ProgressBar` | thin (8dp), thick (12dp), level card style | 2dp border, fill blue/ink |
| `LevelBadge` | A1 red, A2 white, B1 yellow, B2 blue, C1 white, NAT ink | small square chip |
| `LevelCard` | normal, selected (red bg + s6) | 2-col grid, 150dp high, shape icon + progress |
| `MenuTile` | white, ink (with red shadow) | 110dp, shape icon + title + subtitle, shadow s4 |
| `ListRow` / `ListCard` | with chip, with speaker button, with toggle, with value+chevron | rows separated by 2dp ink dividers inside a 3dp bordered white card |
| `WordRow` | notebook: word · tr, level · due, SRS bars (4), speaker | bars colored by box (red/yellow/blue) |
| `StatTile` | 2×2 grid | shape + big number 30sp + label |
| `BarChart` | 7 bars, today outlined | see `09-stats` |
| `Toast` | success (blue), info (white with undo link) | top, shadow s5 |
| `ConfirmDialog` | — | white, 3dp, shadow s8, red square icon, title 24/800, body, two buttons |
| `BottomSheet` | joker sheet | cream, 4dp top border, scrim 55% |
| `EmptyState` | — | dashed 3dp border, overlapping circle+square illustration |
| `LexiLogo` | full (4 blocks), compact wordmark, icon | `13-logo-and-icon` |
| `DecorShape` | circle, square, halfPill, leaf | background decoration helper |
| `HardShadow` modifier | s3–s8, color | core primitive |
| `PressableSurface` | — | shared press animation + haptic + sound |

Naming in code: `Lexi*` prefix for public components. Every component takes `modifier: Modifier = Modifier` as the
first optional parameter and has no hardcoded strings.
