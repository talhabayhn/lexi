# Motion, haptics & sound

Reference: `screens/15-motion.png` and the exact keyframes in `source/15-motion.dc.html` / `source/14-splash.dc.html`.

**Rule:** movements are short and *mechanical* — shapes slide, collapse, rotate. No soft fades for state changes (screen transitions, #12, are the exception),
no blur, no spring wobble beyond the specified overshoot. All 80–420 ms.
**Reduced motion** (Android "remove animations" / iOS "Reduce Motion"): only color/shape changes, no translation,
scale or rotation; splash shows the logo statically.

## Easings (`LexiMotion`)
| Token | Cubic bezier | Use |
|---|---|---|
| `mechanical` | (0.2, 0.8, 0.2, 1) | splash, general enter |
| `fly` | (0.3, 0.7, 0.2, 1) | key → slot |
| `pop` | (0.3, 1.6, 0.5, 1) (overshoot) | correct pop, combo badge |
| `turn` | (0.4, 0, 0.2, 1) | pass rotation |
| `linear` | — | timer bar |

## Catalogue
| # | Event | Animation | Duration | Haptic | Sound |
|---|---|---|---|---|---|
| 1 | Any button press | element translates (+shadow offset), shadow → 0; release returns | 80 ms out / 80 ms back | light | "tık" |
| 2 | Letter key tapped | key letter flies to first empty slot (≈ translate + scale 1.15 at midpoint → 1), slot turns ink with cream letter; key becomes "used" (dashed) | 220 ms, `fly` | light | — |
| 3 | Correct answer | wheel chip: cream circle → blue circle with scale 1 → 1.45 → 1; blue ring (4dp) expands from 0.6 → 1.8 and fades | 320 ms, `pop` | success | two rising notes; with TTS on, the word instead (spoken at once) |
| 4 | Wrong answer | answer slots shake horizontally (−12, 12, −9, 9, −4, 0 dp) and flash red, then correct answer shown; chip → red square | 360 ms | error | descending "bzzt" |
| 5 | Pass | chip rotates 90° while morphing circle → leaf, turns yellow; small arrow slides 70dp right | 260 ms, `turn` | soft | "vınn" |
| 6 | Wheel advance | black "current" square jumps to next slot (step, no easing between slots); centre letter slides down and is replaced | 140 ms | — | — |
| 7 | Combo (streak 3, 5, 10) | 4 shapes (square/circle/leaf/ink square) burst diagonally ~100dp and fade; badge "×N SERİ" pops (scale 0 → 1.3 → 1, rotate −20° → 6° → −4°) | 420 ms, `pop` | heavy | short fanfare |
| 8 | Time warning (last 10 s) | timer fill `yellow` → `red` and counter `ink` → `red`, switched at once (no fade); counter pulses scale 1 → 1.12 → 1 (linear) each second, with the tick; reduced motion: colours only | 600 ms per pulse | tick each second | "tik"; at 0 s once the descending two-tone buzzer (`time_up`, no tick with it) |
| 9 | Splash | bg yellow circle (top-left) & blue square (bottom-right) scale 0 → 1; L slides from left (overshoot 8dp), E drops from top & bounces 10dp, X rotates −270° → 10° → 0 with scale 0 → 1.08 → 1, I rises from bottom (overshoot −8dp); tagline fades up 12dp | total ≈ 1.2 s in app (demo is 3.6 s looped), `mechanical` | light on each landing | "tık" per landing |
| 10 | Bottom sheet (joker) | slides up from bottom, scrim fades to 55% | 220 ms | — | — |
| 11 | Toast | slides down from top 16dp, auto-dismiss 2.5 s | 180 ms | — | — |
| 12 | Screen transitions | slide + fade-through: incoming slides in from +24dp (260 ms) and fades 0 → 1 (200 ms, starts at 60 ms); outgoing slides to −24dp (260 ms) and fades 1 → 0 over its first 90 ms; reverse on back; reduced motion: 150 ms crossfade, no translation | 260 ms, `mechanical` | — | — |

#1 is implemented once in `PressableSurface` (`core:designsystem`): CSS `ease-out` per direction (`.lx-press` in
`source/15-motion.dc.html`); a quick tap still plays the full 80 ms press before returning; haptic and sound fire on
click (release inside the element), not on touch-down, so a cancelled touch stays silent. Reduced motion: no
translation, the shadow disappears instantly while pressed.

### Game screen (#2–#8, M4)
Implemented in `feature:game` (`GameMotion`, `GameMotionLayer`, `GameFeedback`) on top of `LetterWheel` / `WheelLetter`
(`WheelChipMotion`), `TimerBar`, `ComboBadge` / `ComboBurst`; keyframe positions are `LexiMotion` tokens taken from
`source/15-motion.dc.html`. The resting look always comes from the state (ADR 0019): effects only add transient motion.
- Feedback starts with the animation, from the screen (`LocalHaptics` / `LocalSoundPlayer`). Keys, PAS and joker rows
  are buttons: their press plays #1, which is also #2's light haptic, so a placed letter adds none; the effect a press
  causes (#3/#4 after the last letter, #5 after PAS) follows right after it. TTS (#3, setting "Doğru cevapta
  telaffuz") starts the moment the answer is judged (QUEUE_FLUSH, so it cuts off the previous word) and replaces the
  two notes, which would clash with it; the success haptic stays. With the setting off, or while the engine is
  still starting or has no voice for the accent, the two notes play and nothing is spoken. The engine and voice are
  warmed up right after the splash while the setting is on (its language setup runs off the main thread); debug builds
  log the speak-to-audio latency (tag `Speech`). The Listen joker speaks at once.
- #2: a copy of the key (white, s3) travels on `fly` for 5/7 of the time while growing to 1.15, then settles to 1 on
  the cell; it changes size from the key's to the cell's and lands exactly on it. Until then the cell keeps its look
  from before (red "next" border). Finishing an answer (#3/#4/#5/#6) ends letters still in the air.
- #3: pop and ring peak at 1/3; the pop is back to 1 at 2/3; the ring (CSS ease-out, drawn under the chip, above its
  neighbours) is gone at 320 ms.
- #4: shake of the answer row; the red is the state's wrong cells showing the correct answer during the reveal.
- #5: only the chip shape turns (the letter stays upright) and morphs into the leaf turned back by 90°, so it ends on the
  resting leaf. The arrow runs in the same 260 ms (opacity peak at 2/7) and slides clockwise along the wheel, towards
  the next letter (on the wheel "right" means clockwise).
- #6: the current square jumps with the state; the centre letter slides down out of its box and the next one follows
  from above (`mechanical`).
- #7: burst and badge sit in the HUD row next to the score circle (design debt, `screen-map.md`); the badge pops in 420 ms, holds 720 ms
  and shrinks to 0 in 240 ms (`aCombo`). Reduced motion: the badge rests at −4° for 420 + 720 ms, no burst.
- #8: `TimerBar` pulses whenever the counter changes (and when the warning starts), not on first composition, so a
  paused or restored warning does not pulse.
- #11: the result screen's share error toast slides down 16dp in 180 ms (`mechanical`) and goes after 2.5 s; reduced
  motion: shown in place, no slide. (The home daily-gift notice shows the toast without the slide.)
- `win` / `lose` have no catalogue row, so they are not played yet (to be decided with the result screen's motion).

#12 is set on `LexiNavHost` (all destinations): enter slides from +24dp over 260 ms and fades in over 200 ms from
60 ms, the old screen moves −24dp and fades out over its first 90 ms, back mirrors it, `mechanical`; the host is filled
with `cream` so the mid-fade never shows the window background. Reduced motion: 150 ms crossfade, no translation
(opacity is allowed). All values are `LexiMotion` tokens (`screenTransition*`).

Splash timing in app (scale the demo keyframe percentages from 3.6 s to 1.2 s; keep order and overshoots):
bg 0–216 ms, L 48–312 ms, E 120–384 ms, X 192–480 ms, I 264–552 ms, tagline 576–696 ms, hold to 1200 ms.

## Haptic map (`HapticKind`)
Android uses the device `Vibrator` (no View dependency); API 29+ uses predefined effects, API 26-28 a waveform
(`timing ms / amplitude`).
| Kind | Android | iOS |
|---|---|---|
| `Light` | `EFFECT_TICK` (29+) / one-shot 10 ms, 60 | `UIImpactFeedbackGenerator(.light)` |
| `Soft` | `EFFECT_CLICK` (29+) / one-shot 15 ms, 40 | `UIImpactFeedbackGenerator(.soft)` |
| `Heavy` | `EFFECT_HEAVY_CLICK` (29+) / one-shot 40 ms, 255 | `UIImpactFeedbackGenerator(.heavy)` |
| `Success` | `EFFECT_DOUBLE_CLICK` (29+) / double tick: 15 ms, 60 ms gap, 25 ms | `UINotificationFeedbackGenerator(.success)` |
| `Error` | long tick: one-shot 80 ms, 220 (all levels) | `UINotificationFeedbackGenerator(.error)` |
| `Tick` | very short one-shot 8 ms, 40 (all levels) | `UISelectionFeedbackGenerator` |
Respect the in-app "Titreşim" setting.
iOS TTS shares the app's ambient audio session, so the silent switch also mutes pronunciation (accepted behaviour).

## Sound assets (generated placeholders)
`tap`, `place`, `correct` (two rising notes), `wrong` (bzzt), `pass` (vınn), `combo`, `tick`, `win`, `lose` as mono
16-bit 22.05 kHz **WAV** (one format for `SoundPool` and `AVAudioPlayer`), ≤ 300 ms (win/lose ≤ 800 ms), in
`shared/src/commonMain/composeResources/files/sounds/`. They are synthesized by `tools/sounds/generate_sounds.py`
(stdlib only, no third-party audio, no licensing) and are placeholders: replace a file with a final asset of the same
name and format and nothing else changes. `SoundPlayer` implementations (`core:platform`) preload them, respect the
"Ses efektleri" setting (`GatedSoundPlayer`) and the iOS silent switch (ambient audio session); haptics respect
"Titreşim" (`GatedHaptics`).
