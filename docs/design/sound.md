# Sound design

Calm, warm, minimal: Lexi is a relaxing word puzzle, so every sound is a soft struck or plucked tone — never a
synthetic beep, buzzer, square/saw wave or 8-bit blip. Animation timing and haptics stay in `motion.md`.

## Rules
- **One key for everything: C major pentatonic** (C D E G A). Any two sounds overlapping stay consonant; there is
  no minor second anywhere.
- **Frequent sounds** (key tap, ticks) are tiny (70–140 ms), quiet (peak −19 to −28 dBFS) with a short room tail.
  Each also has 3 subtle round-robin variants (`name_1..3.wav`: a few cents, ±10 % velocity, fresh mallet noise)
  so repeated taps don't sound machine-gunned.
- **Rewards** (correct, combo, win, best, daily) are 2–5 note rising pentatonic motifs ending on C or a soft rolled
  C chord; tails up to ~700 ms.
- **Mistakes are gentle**: `wrong` is a muted low wooden falling third (E4 → C4), `lose` and `time_up` resolve
  downward to C — consoling, never punishing.
- Format: mono 16-bit 22.05 kHz WAV (unchanged), in `shared/src/commonMain/composeResources/files/sounds/`.

## Synthesis (`tools/sound-gen/generate.py`)
One command, deterministic (seeded per set/sound/variant), only `numpy` (`tools/sound-gen/requirements.txt`):

```bash
python3 tools/sound-gen/generate.py              # render sets A/B/C, previews, install set A into the app
python3 tools/sound-gen/generate.py --install C  # switch the shipped set (A | B | C | none)
```
Modal/additive synthesis with inharmonic partials and individual exponential decays, random detune/phase per
partial, soft noise mallet/hammer transient, 4–6 ms raised-cosine attack (no clicks), 4th-order low-pass at
6–7 kHz (the script asserts < 1 % energy above 8 kHz and silent edges), gentle `tanh` saturation, and a small-room
reverb from a synthetic impulse response (early reflections + darkening noise tail, wet 10–13 %).

| Set | Folder | Instrument |
|---|---|---|
| **A (shipped)** | `a-kalimba` | wooden bar, modes 1 : 3.98 : 9.2 + tube resonance, felt mallet |
| B | `b-glass` | soft glass/bell, free-bar ratios 1 : 2.76 : 5.40 : 8.93, slow 0.7 Hz beating pairs |
| C | `c-felt` | felt piano / soft pluck, stiff-string partials, felt-damped highs, double decay, hammer thump |

Candidate sets render to `tools/sound-gen/out/<set>/` (git-ignored). Listening copies: `sound-previews/<set>/`
(OGG; `_reel.ogg` plays the whole set in list order). `tools/sounds/generate_sounds.py` (old placeholders) is
superseded — don't run it, it overwrites the shipped files.

## Sound list
Wired = played today through `SoundEffect` (`core:domain`); file name = `SoundEffect.name.lowercase()`.

| File | Purpose | Notes (set A timing) | Status |
|---|---|---|---|
| `tap` | Letter key tap and every button press (`PressableSurface`) — the most frequent sound | G5, damped, 70 ms, −19 dB | wired |
| `place` | Letter lands in its slot | D5, damped, 80 ms | wired (enum, not played yet) |
| `correct` | Right answer; TTS starts after 280 ms | G5 → C6, 280 ms (matches `CORRECT_SOUND_DURATION`) | wired |
| `wrong` | Wrong answer — soft, never punishing | muted E4 → C4 thud, 300 ms | wired |
| `pass` | Pass / skip — neutral, light | quick C5 D5 E5 roll, 260 ms | wired |
| `combo` | Streak milestone (×3/×5/×10) | C5 E5 G5 C6, 400 ms | wired |
| `tick` | Last-10-seconds pulse, once per second | soft A4, 120 ms | wired |
| `win` | Round complete (win) | C5 E5 G5 + rolled C chord, 900 ms | wired (enum, not played yet) |
| `lose` | Round complete (lose) — consoling | A4 G4 E4 → C4/G4, 800 ms | wired (enum, not played yet) |
| `button` | Generic button press, a little fuller than `tap` (if keys and buttons get split) | C5, 90 ms | ready to wire |
| `backspace` | Delete a letter | E5, very damped, 70 ms | ready to wire |
| `timer_tick` | Quiet relaxing per-second timer pulse (not a clock tick) | A5, 90 ms, −28 dB | ready to wire |
| `warning_1..3` | Rising urgency for the last 10 s: 10–7 s, 6–4 s, 3–1 s | G4 → A4 → C5 double pulse, louder per stage | ready to wire |
| `time_up` | Timer ran out | rolled C5/G4 then C4, 700 ms | ready to wire |
| `joker` | Joker used | G5 A5 D6 sparkle, 380 ms | ready to wire |
| `gift` | Joker gift / daily gift | E5 G5 C6 E6, 650 ms | ready to wire |
| `countdown` | Level start 3-2-1 beat | C5, 220 ms | ready to wire |
| `countdown_go` | "Go" after the countdown | rolled G5/C6, 380 ms | ready to wire |
| `best` | New best score / streak record | C5 E5 G5 A5 + C chord, 850 ms | ready to wire |
| `dialog_open` / `dialog_close` | Sheet or dialog opens / closes | D5 → G5 / G5 → D5, 180 ms, quiet | ready to wire |
| `toast` | Toast appears | single soft A5, 250 ms | ready to wire |
| `notebook_add` / `notebook_remove` | Word saved to / removed from the notebook | E5 → A5 / A5 → E5, ~200 ms | ready to wire |
| `daily_complete` | Daily wheel complete | G4 C5 E5 G5 + C chord, 1 s | ready to wire |

Variants (`_1.._3`) exist for `tap`, `place`, `backspace`, `timer_tick`, `tick` — also ready to wire (pick one at
random per play).
