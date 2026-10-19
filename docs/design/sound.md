# Sound design

**Shipped set: "Damla" (water drop).** Chosen by the product owner in two listening rounds (round 1: 26 very
different candidates, liked the bubble pop, the soft click and marimba; round 2: four families in that clean,
rounded language, picked Damla). Every sound is a soft, rounded sine "blup" that glides up into its note, a soft
click, or a low rounded "bonk". Never a harsh beep, buzzer, square/saw wave or 8-bit blip. Timing and haptics stay
in `motion.md`.

## Rules
- **One key for everything: C major pentatonic** (C D E G A), so overlapping sounds stay consonant (the generator
  asserts every note is in the scale).
- **Letters are drops, buttons are clicks:** letter keys (`tap`, `place`) are small drops; generic buttons, dialogs
  and the per-second timer use the soft click.
- **Frequent sounds** (tap, place, backspace, ticks) are 70–140 ms and quiet (peak −18 to −30 dBFS), with 3 subtle
  round-robin variants each (`name_1..3.wav`: a few cents, ±10 % velocity, timing).
- **Rewards** (correct, combo, win, best, daily) are 2–5 rising pentatonic drops; correct, win, best and daily end on C or a rolled C chord.
- **Mistakes are gentle**: `wrong` is two low rounded bonks (E4 → C4); `lose` settles down onto C; `time_up` is a soft
  descending two-tone buzzer (E4 → C4, rounded square wave, no edge above 2 kHz).
- Format: mono 16-bit 22.05 kHz WAV (unchanged), in `shared/src/commonMain/composeResources/files/sounds/`.

## Generator (`tools/sound-gen/generate.py`)
```bash
python3 tools/sound-gen/generate.py              # render all sets, previews, install set E (Damla) into the app
python3 tools/sound-gen/generate.py --install A  # switch the shipped set (E | A | B | C | D | none)
```
Deterministic (seeded per set/sound/variant), offline, only `numpy`. Damla voice: sine + faint 2nd harmonic whose
pitch starts about 30 % (~6 semitones) low and rises into the note within ~12 ms, 1.5 ms soft attack, short exponential
tail; click = band-passed noise burst with a 3 ms decay; bonk = sine falling slightly into a low note. Then low-pass at 7 kHz, a small
room (synthetic IR, wet 6–13 %), natural release. Asserts silent edges and < 1 % energy above 8 kHz.

| Set | Folder | Sound |
|---|---|---|
| **E (shipped)** | `e-damla` | Damla: rounded synthetic drops, soft clicks |
| A–D | `a-piano`, `b-kalimba`, `c-marimba`, `d-vibraphone` | earlier candidates from real CC0 recordings (VCSL, see `tools/sound-gen/samples/SOURCE.md`): soft wood for UI, one instrument for melodies |

Candidate sets render to `tools/sound-gen/out/<set>/` (git-ignored). Listening copies: `sound-previews/<set>/`
(OGG; `_reel.ogg` plays every main sound in list order; the folder is fully generated, don't hand-edit it).
`tools/sounds/generate_sounds.py` (old placeholders) is superseded — don't run it, it overwrites the shipped files.

## Sound list (Damla)
Wired = played today through `SoundEffect` (`core:domain`); file name = `SoundEffect.name.lowercase()`.

| File | Purpose | Sound | Status |
|---|---|---|---|
| `tap` | Letter key tap and every button press (`PressableSurface`) — the most frequent sound | small drop G5, 70 ms, −19 dB | wired |
| `place` | Letter lands in its slot | smaller drop E5, 90 ms | wired (enum, not played yet) |
| `correct` | Right answer (not played while the word is spoken: TTS replaces it) | drops G5 → C6, 280 ms | wired |
| `wrong` | Wrong answer — soft, never punishing | low bonks E4 → C4, 300 ms | wired |
| `pass` | Pass / skip — neutral, light | drops E6 → C6, 260 ms | wired |
| `combo` | Streak milestone (×3/×5/×10) | rising drops G5 C6 E6 G6, 400 ms | wired |
| `tick` | Last-10-seconds pulse, once per second | click + drop G5, 120 ms | wired |
| `win` | Round complete (win) | C6 E6 G6 + rolled C chord, 900 ms | wired (enum, not played yet) |
| `lose` | Round complete (lose) — consoling | A5 G5 E5 → C5/G5, 800 ms | wired (enum, not played yet) |
| `button` | Generic button press (if keys and buttons get split) | soft click, 90 ms | ready to wire |
| `backspace` | Delete a letter | falling "pit" E5, 90 ms | ready to wire |
| `timer_tick` | Quiet per-second timer pulse | very soft click, 90 ms, −30 dB | ready to wire |
| `warning_1..3` | Rising urgency for the last 10 s: 10–7 s, 6–4 s, 3–1 s | click + G5 → A5 → C6 double, louder per stage | ready to wire |
| `time_up` | Timer ran out, once (`SoundEffect.TimeUp`) | buzzer E4 260 ms → C4 400 ms, 750 ms, −12 dBFS | wired |
| `joker` | Joker used | quick E6 G6 A6, 380 ms | ready to wire |
| `gift` | Joker gift / daily gift | C6 E6 G6 C7, 650 ms | ready to wire |
| `countdown` | Level start 3-2-1 beat | drop C6, 220 ms | ready to wire |
| `countdown_go` | "Go" after the countdown | rolled G5/C6/E6, 380 ms | ready to wire |
| `best` | New best score / streak record | C6 E6 G6 A6 + rolled C chord, 850 ms | ready to wire |
| `dialog_open` / `dialog_close` | Sheet or dialog opens / closes | click + rising / falling D6, 180 ms, quiet | ready to wire |
| `toast` | Toast appears | single soft drop E6, 250 ms | ready to wire |
| `notebook_add` / `notebook_remove` | Word saved to / removed from the notebook | E6 → A6 / falling A5, ~230 ms | ready to wire |
| `daily_complete` | Daily wheel complete | G5 C6 E6 G6 + rolled C chord, 1 s | ready to wire |

Variants (`_1.._3`) exist for `tap`, `place`, `backspace`, `timer_tick`, `tick` — also ready to wire (pick one at
random per play).
