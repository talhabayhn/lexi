# Sound design

**Concept: a Bauhaus letter board.** Wooden letter tiles, paper cards and one warm acoustic instrument — the
materials of a word game, nothing electronic. Frequent UI sounds are tactile (a tile on a wooden board, a paper
card turning); answers, rewards and the timer are short, plain melodic gestures (few notes, clean intervals, no
ornament). Never a synthetic beep, buzzer, square/saw wave or 8-bit blip. Timing and haptics stay in `motion.md`.

## Rules
- **One key for everything: C major pentatonic** (C D E G A), so overlapping sounds stay consonant.
- **Warm register:** melodies sit between C4 and A5 (most between G4 and E5; only final reward chords touch C6); body EQ around 200 Hz, low-pass
  at 5–5.5 kHz, a small wooden room (wet 8–13 %).
- **Frequent sounds** (tap, place, backspace, ticks) are 70–140 ms and quiet (peak −18 to −30 dBFS), with 3 subtle
  round-robin variants each (`name_1..3.wav`: other tile grain, a few cents, ±10 % velocity).
- **Rewards** (correct, combo, win, best, daily) are 2–5 note rising motifs ending on C or a hand-rolled C chord.
- **Mistakes are gentle**: `wrong` is a soft tile thud with a muted falling third (E4 → C4); `lose` and `time_up`
  settle down onto C — consoling, never punishing.
- Format: mono 16-bit 22.05 kHz WAV (unchanged), in `shared/src/commonMain/composeResources/files/sounds/`.

## Generator (`tools/sound-gen/generate.py`)
One command, deterministic (seeded per set/sound/variant), only `numpy` (`tools/sound-gen/requirements.txt`):

```bash
python3 tools/sound-gen/generate.py              # render sets A/B/C, previews, install set A into the app
python3 tools/sound-gen/generate.py --install C  # switch the shipped set (A | B | C | none)
```
Physical models, not oscillators: wooden tile = inharmonic block modes + contact noise + board thump (softer touch
is darker); paper = noise swept between two bands with crinkle grains; instruments below. Every note gets a
3–7 ms attack, light human timing (0–3 ms) and rolled chords. The script asserts silent edges and < 1 % energy
above 8 kHz.

| Set | Folder | Instrument (tiles and paper are the same in every set) |
|---|---|---|
| **A (shipped)** | `a-felt-piano` | felt upright piano: stiff-string partials, hammer position, felt low-pass, 3 detuned unison strings (slow beating, two-stage decay) |
| B | `b-kalimba` | kalimba: tine modes 1 : 5.93 : 13.4, thumb pluck, release glide, box resonance |
| C | `c-marimba` | rosewood marimba, yarn mallets, resonator tube |

Candidate sets render to `tools/sound-gen/out/<set>/` (git-ignored). Listening copies: `sound-previews/<set>/`
(OGG; `_reel.ogg` plays every main sound in list order). `tools/sounds/generate_sounds.py` (old placeholders) is
superseded — don't run it, it overwrites the shipped files.

## Sound list
Wired = played today through `SoundEffect` (`core:domain`); file name = `SoundEffect.name.lowercase()`.

| File | Purpose | Sound | Status |
|---|---|---|---|
| `tap` | Letter key tap and every button press (`PressableSurface`) — the most frequent sound | soft finger on a wooden tile, 70 ms, −19 dB | wired |
| `place` | Letter lands in its slot | tile set down + tiny rock, 90 ms | wired (enum, not played yet) |
| `correct` | Right answer; TTS starts after 280 ms | G4 → C5, 280 ms (matches `CORRECT_SOUND_DURATION`) | wired |
| `wrong` | Wrong answer — soft, never punishing | soft tile thud + muted E4 → C4, 300 ms | wired |
| `pass` | Pass / skip — neutral, light | paper card turns, lands on the board, 260 ms | wired |
| `combo` | Streak milestone (×3/×5/×10) | G4 C5 E5 G5, 400 ms | wired |
| `tick` | Last-10-seconds pulse, once per second | muted G4 + low tile knock (heartbeat), 120 ms | wired |
| `win` | Round complete (win) | C5 E5 G5 + rolled C4/G4/E5, 900 ms | wired (enum, not played yet) |
| `lose` | Round complete (lose) — consoling | A4 G4 E4 → C4/G4, 800 ms | wired (enum, not played yet) |
| `button` | Generic button press, a little fuller than `tap` (if keys and buttons get split) | larger, deeper tile, 90 ms | ready to wire |
| `backspace` | Delete a letter | short slide + light tile, 90 ms | ready to wire |
| `timer_tick` | Quiet relaxing per-second timer pulse (not a clock tick) | muted A4, 90 ms, −30 dB | ready to wire |
| `warning_1..3` | Rising urgency for the last 10 s: 10–7 s, 6–4 s, 3–1 s | G4 → A4 → C5 double pulse with tile knock, louder per stage | ready to wire |
| `time_up` | Timer ran out | G4 then rolled C4/G4, 700 ms | ready to wire |
| `joker` | Joker used | card flick + E5 G5 A5, 380 ms | ready to wire |
| `gift` | Joker gift / daily gift | paper unwrap + C5 E5 G5, 650 ms | ready to wire |
| `countdown` | Level start 3-2-1 beat | C5, 220 ms | ready to wire |
| `countdown_go` | "Go" after the countdown | rolled G4/C5/E5, 380 ms | ready to wire |
| `best` | New best score / streak record | C5 E5 G5 A5 + rolled C chord, 850 ms | ready to wire |
| `dialog_open` / `dialog_close` | Sheet or dialog opens / closes | paper card in / out, 180 ms, quiet | ready to wire |
| `toast` | Toast appears | single soft E5, 250 ms | ready to wire |
| `notebook_add` / `notebook_remove` | Word saved to / removed from the notebook | card slides in + E5 → A5 / card out + muted A4, ~230 ms | ready to wire |
| `daily_complete` | Daily wheel complete | G4 C5 E5 G5 + rolled C chord, 1 s | ready to wire |

Variants (`_1.._3`) exist for `tap`, `place`, `backspace`, `timer_tick`, `tick` — also ready to wire (pick one at
random per play).
