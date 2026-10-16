# Sound design

**Concept: a Bauhaus letter board, played on real instruments.** Frequent UI sounds are real wood (woodblock, slit
drum, claves) played softly, pitched down and darkened into small tactile knocks; answers, rewards and the timer are
short, plain melodic gestures (few notes, clean intervals, no ornament) on one real acoustic instrument. Nothing
electronic: never a synthetic beep, buzzer, square/saw wave or 8-bit blip. Timing and haptics stay in `motion.md`.

## Rules
- **One key for everything: C major pentatonic** (C D E G A), so overlapping sounds stay consonant (the generator
  asserts every note is in the scale).
- **Warm register:** melodies sit between C4 and A5 (most between G4 and E5; only final reward chords touch C6).
- **Frequent sounds** (tap, place, backspace, ticks) are 70–140 ms and quiet (peak −18 to −30 dBFS), with 3 subtle
  round-robin variants each (`name_1..3.wav`: a few cents, ±10 % velocity, timing; `tap` also rotates three
  real woodblock strokes).
- **Rewards** (correct, combo, win, best, daily) are 2–5 note rising motifs ending on C or a hand-rolled C chord.
- **Mistakes are gentle**: `wrong` is a soft low slit-drum knock with a damped falling third (E4 → C4); `lose` and
  `time_up` settle down onto C — consoling, never punishing.
- Format: mono 16-bit 22.05 kHz WAV (unchanged), in `shared/src/commonMain/composeResources/files/sounds/`.

## Sources and generator
Recordings: **VCSL** (Versilian Community Sample Library), **CC0 1.0** — public domain, commercial use, no royalties,
no attribution. Short tuned excerpts (1.8 MB) are vendored in `tools/sound-gen/samples/` (`SOURCE.md` there);
`tools/sound-gen/fetch_samples.py` re-cuts them from a pinned VCSL commit.

```bash
python3 tools/sound-gen/generate.py              # render sets A-D, previews, install set A into the app
python3 tools/sound-gen/generate.py --install C  # switch the shipped set (A | B | C | D | none)
```
Deterministic (seeded per set/sound/variant), offline, only `numpy`. Light processing: pitch by resampling (instrument
notes a few semitones at most), velocity as level + brightness, optional damping, rolled chords and 0–3 ms human timing, gentle
EQ and low-pass, a small wooden room (synthetic IR, wet 8–13 %), natural release. Asserts silent edges and < 1 %
energy above 8 kHz.

| Set | Folder | Instrument (the wood sounds are the same in every set) |
|---|---|---|
| **A (shipped)** | `a-piano` | Yamaha upright piano, softest velocity |
| B | `b-kalimba` | Tanzanian kalimba |
| C | `c-marimba` | marimba, soft mallets |
| D | `d-vibraphone` | vibraphone, soft mallets |

Candidate sets render to `tools/sound-gen/out/<set>/` (git-ignored). Listening copies: `sound-previews/<set>/`
(OGG; `_reel.ogg` plays every main sound in list order; the folder is fully generated, don't hand-edit it). `tools/sounds/generate_sounds.py` (old placeholders) is
superseded — don't run it, it overwrites the shipped files.

## Sound list
Wired = played today through `SoundEffect` (`core:domain`); file name = `SoundEffect.name.lowercase()`.

| File | Purpose | Sound | Status |
|---|---|---|---|
| `tap` | Letter key tap and every button press (`PressableSurface`) — the most frequent sound | soft woodblock, pitched down, 70 ms, −19 dB | wired |
| `place` | Letter lands in its slot | soft woodblock + faint slit drum, 90 ms | wired (enum, not played yet) |
| `correct` | Right answer; TTS starts after 280 ms | G4 → C5, 280 ms (matches `CORRECT_SOUND_DURATION`) | wired |
| `wrong` | Wrong answer — soft, never punishing | low slit-drum knock + damped E4 → C4, 300 ms | wired |
| `pass` | Pass / skip — neutral, light | slit-drum knock + damped D5 → G4, 260 ms | wired |
| `combo` | Streak milestone (×3/×5/×10) | G4 C5 E5 G5, 400 ms | wired |
| `tick` | Last-10-seconds pulse, once per second | damped G4 + low slit drum (heartbeat), 120 ms | wired |
| `win` | Round complete (win) | C5 E5 G5 + rolled C4/G4/E5, 900 ms | wired (enum, not played yet) |
| `lose` | Round complete (lose) — consoling | A4 G4 E4 → C4/G4, 800 ms | wired (enum, not played yet) |
| `button` | Generic button press, a little fuller than `tap` (if keys and buttons get split) | slit drum, 90 ms | ready to wire |
| `backspace` | Delete a letter | soft claves, far down, 90 ms | ready to wire |
| `timer_tick` | Quiet relaxing per-second timer pulse (not a clock tick) | damped A4, 90 ms, −30 dB | ready to wire |
| `warning_1..3` | Rising urgency for the last 10 s: 10–7 s, 6–4 s, 3–1 s | G4 → A4 → C5 double pulse with slit drum, louder per stage | ready to wire |
| `time_up` | Timer ran out | G4 then rolled C4/G4, 700 ms | ready to wire |
| `joker` | Joker used | E5 G5 A5, 380 ms | ready to wire |
| `gift` | Joker gift / daily gift | C5 E5 G5 C6, 650 ms | ready to wire |
| `countdown` | Level start 3-2-1 beat | C5, 220 ms | ready to wire |
| `countdown_go` | "Go" after the countdown | rolled G4/C5/E5, 380 ms | ready to wire |
| `best` | New best score / streak record | C5 E5 G5 A5 + rolled C chord, 850 ms | ready to wire |
| `dialog_open` / `dialog_close` | Sheet or dialog opens / closes | high / low slit drum, 180 ms, quiet | ready to wire |
| `toast` | Toast appears | single soft E5, 250 ms | ready to wire |
| `notebook_add` / `notebook_remove` | Word saved to / removed from the notebook | woodblock + E5 → A5 / woodblock + damped A4, ~230 ms | ready to wire |
| `daily_complete` | Daily wheel complete | G4 C5 E5 G5 + rolled C chord, 1 s | ready to wire |

Variants (`_1.._3`) exist for `tap`, `place`, `backspace`, `timer_tick`, `tick` — also ready to wire (pick one at
random per play).
