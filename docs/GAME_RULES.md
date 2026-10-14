# Lexi — game rules (engine specification)

Numbers marked ⚙ live in `GameConfig` (domain) and can be overridden by Remote Config later. Tests use these
exact defaults.

## 1. The wheel
- 26 slots, letters A–Z in order. Each slot = `(letter, rule, card)`.
- `rule`: `STARTS` ("K ile başlar") or `CONTAINS` ("içinde X geçer"). Content decides per card; Q, X, Z and
  rare letters at low levels are usually `CONTAINS`.
- Generation (`WheelGenerator`): for each letter pick one card from the pool (level [+ category]) using the
  seeded RNG, preferring cards the player hasn't seen recently (solo only — daily, duel, review and level test
  ignore history so all players get the same wheel; `GameMode.usesCardHistory`). If no card exists for a letter,
  the slot is `SKIPPED` (not shown as playable; counts as neither right nor wrong). Content validation must keep
  this at 0 for shipped levels.
- Pick: per letter A–Z, candidates = pool cards of that level (+ category unless `mixed`) and letter, sorted by
  card id (pool order never matters; card ids in a pool must be unique); drop recently seen cards unless that
  leaves none; `pick` = one `nextInt(size)` on the `wheel` sub-seed (§13). Recently seen = last seen within
  ⚙ `recentlySeenWindowDays = 7` (`WheelGenerator.recentlySeen(lastSeenMs, nowMs, config)`).
- Review wheel: built from SRS-due cards (any level/category); may have fewer than 26 slots (only letters that
  have due cards), in A–Z order; one card per letter, picked the same way. Other due cards wait for the next review.

## 2. Slot states
`TODO → CURRENT → (CORRECT | WRONG | PASSED)`; `PASSED → CURRENT` again on the next lap.
UI encoding (from design): todo = cream circle, current = black square (yellow letter, scale 1.3),
correct = blue circle, wrong = red square, passed = yellow leaf.

## 3. Turn flow
1. `Start` → first slot becomes CURRENT, timer starts (if mode has one).
2. Player fills answer slots with letter keys. When all slots are filled → automatic submit.
   - Match is case-insensitive; spaces in multi-word answers are fixed (pre-rendered gaps, no key needed).
   - Correct → CORRECT, effects `Correct`, advance.
   - Wrong → WRONG, correct answer revealed for ⚙`revealMs = 1200`, advance.
3. `Pass` → PASSED, advance.
4. Advance = next slot clockwise that is TODO or PASSED. After Z it wraps (lap 2, 3, …) over passed slots.
5. End when: no TODO/PASSED slots left, or time is up, or (Hardcore) first WRONG, or `Forfeit`.
6. Engine decisions:
   - While a wrong answer is revealed, input is ignored and the game clock stands still (⚙ `clockRunsDuringReveal
     = false`); the reveal ends after `revealMs` of ticks, then the engine advances (Hardcore: game over). With
     `clockRunsDuringReveal = true`, time running out during the reveal ends the game without advancing.
   - A passed slot keeps its input (typed letters, disabled keys, used jokers) for the next lap. If the game ends
     while it is CURRENT again (time up, forfeit), it stays PASSED (counted as passed and missed, §10).
   - Events that don't apply (input while paused/revealing, a used key, anything after game over) are ignored.

## 4. Letter keys
- Key count: `max(10, ceil(answerLetters / 5) * 5)`, capped at 15 → 2 or 3 rows of 5 (design: 5-column grid).
  Answers have 2–14 letters (`AnswerRules`).
- Keys = multiset of the answer's letters + distractors, shuffled with the slot's sub-seed.
- Distractors: letters not over-represented in the answer, weighted by English letter frequency, with ≥ 2
  "near" letters (visually/phonetically similar, e.g. C/K, I/E, V/W) when available. Deterministic.
  - Decision: distractors are distinct letters that do **not** occur in the answer, so every key is either part of
    the answer multiset or a distractor (Remove distractors disables exactly those). Always feasible: answers have
    ≤ 14 letters, so ≥ 12 letters remain.
  - Draw order (`KeyboardGenerator`, sub-seed `keys:<letter>`): first `min(2, needed, near candidates)` near
    letters, then the rest from all remaining candidates; both weighted by English letter frequency (hundredths of
    a percent, E = 1270 … Z = 7) without replacement; then answer keys + distractors are Fisher–Yates shuffled.
  - Near pairs (symmetric): CK CS CG KQ QG KX IE EY IY EA AO OU OQ UV VW UW WM MN NH BP BD DP DT TF FV FP GJ SZ
    XZ LI LR RP JY.
- Tapping a key places its letter in the first empty slot and marks the key USED; `Backspace` returns the last
  placed letter to its key.
- Keys are never reordered during a slot.

## 5. Modes
| Mode | Timer | Scoring | Notes |
|---|---|---|---|
| Classic | ⚙ 180 s total (settings: 120/180/240/300) | yes | default |
| Practice | none | no leaderboard, still SRS | "süresiz, baskısız" |
| Hardcore | ⚙ 180 s | yes ×1.5 | first wrong ends the game |
| Daily | ⚙ 180 s | yes | seed per date+level, **one scored attempt per day**, shareable |
| Review | none | no | SRS-due cards |
| Level test | ⚙ 120 s | no | adaptive, see §9 |
| Duel (hot-seat / online) | per-player clocks ⚙ 150 s each | yes | §8 |

Timer runs while the joker sheet is open (design states "Süre işlemeye devam ediyor"); stops on Pause and when
the app goes to background.

## 6. Scoring
- Correct: ⚙ `base = 10` × level multiplier (A1 1.0, A2 1.1, B1 1.2, B2 1.3, C1 1.5, NAT 1.7), rounded.
- Streak bonus on each correct: ⚙ `+2 × (streak − 1)`, capped at +10. Combo celebrations at streak 3, 5, 10.
- Wrong / pass: 0. Passed-then-correct on a later lap: full points, but streak was broken by the pass.
- Time bonus at the end if the wheel is completed: ⚙ 1 point per remaining second.
- Hardcore ×1.5 on final total.
- Points use the card's level (review wheels mix levels); unscored modes (§5) score 0 everywhere. All rounding is
  half up. "Completed" = ended with no TODO/PASSED left (wrong answers allowed); the time bonus counts whole
  remaining seconds. Joker point costs never take the running score below 0. A wheel without playable slots ends
  at `Start` with reason `EMPTY_WHEEL` (not completed, no time bonus).
- Multipliers are stored as integer percentages in `GameConfig` (110 = ×1.1) so scoring is exact on every platform.

## 7. Jokers
| Joker | Effect | Cost | Limit |
|---|---|---|---|
| Harf aç (Reveal letter) | puts the correct letter into the next empty slot | ⚙ −10 s (untimed modes: −5 pts) | 1 per slot |
| Çeldiricileri temizle (Remove distractors) | disables all distractor keys | ⚙ −15 s (untimed modes: ⚙ 0 pts = free) | 1 per slot |
| Örnek cümle (Example) | shows example sentence with the word blanked | ⚙ −5 pts | 1 per slot |
| Telaffuzu dinle (Listen) | speaks the answer (TTS) | ⚙ −10 pts | disabled at C1 and NAT |
- Inventory per joker type, persisted. Sources: start pack (2/1/3/1), daily login +1 random, rewarded ad +1,
  Pro +3/day refill. Jokers are not allowed in Daily (fair comparison), Duel and Level test (would distort the
  placement). ⚙
- A slot answered with a joker still counts as correct but its streak bonus is 0 (the streak itself goes on).
- Engine decisions: every joker type is limited to once per slot, also across laps. Reveal letter fills the first
  cell that is empty or wrong; typed letters from there on go back to their keys; the revealed letter is locked
  (Backspace skips it); filling the last cell submits. Remove distractors also returns distractor letters already
  typed, and is refused when the slot has no distractors (10-letter answers). Time jokers are refused when the
  cost is ≥ the remaining time. Inventory is part of the game state; the app persists it after `JokerApplied`.

## 8. Duel (same device; same rules online in v1.1)
- Each player has an own wheel (same level, different sub-seeds; online Race uses the same wheel) and an own
  clock that runs only on their turn.
- TV-show rule: your turn continues while you answer correctly. A **wrong** answer or a **pass** ends your turn.
- Same device: between turns show a full-screen "Sıra: <name>" handover card (prevents peeking).
- Game ends when both wheels are finished or both clocks are at 0. Winner: more correct; tie → fewer wrong;
  tie → more remaining time; else draw.
- Engine (`MatchEngine`, `MatchState`): a wrong answer ends the turn once its reveal is over. A player whose wheel
  is finished (completed or clock at 0) hands over; when the other player is already finished, the turn stays
  without handover. The handover card is dismissed with `MatchEvent.BeginTurn`; until then nothing runs and the
  second player's game starts on their first turn. `Forfeit` (also during the handover) ends the whole match and
  the other player wins.
- Race (online): same wheel, simultaneous, no turns; first to finish all slots wins, else most correct at time-up.
  Level handicap (optional room setting): each player plays own level; score = normalized points.

## 9. Level test (onboarding)
- 12–16 slots, adaptive staircase: start at A2; 2 correct in a row at a level → go up; 1 wrong/pass → go down.
- Result = highest level with ≥ 60% correct among its asked cards (min 3 asked), else the level below. Shown on
  a result card ("Seviyen: B1") with the option to change.

## 10. SRS — Kelime Defterim (Leitner)
- Boxes 0–4 (the 4 bars in the notebook UI = box 1–4; box 0 = red/new). Intervals: box0 = today, 1 = 1 d,
  2 = 3 d, 3 = 7 d, 4 = 21 d; correct in box 4 ⇒ "mastered" (leaves due queue).
- In any game: WRONG or PASSED (never answered later) ⇒ card enters/returns to box 0 and appears in
  "Tekrar edilecekler" on the result screen. Correct in a game for a card already in the notebook ⇒ box +1.
- Review wheel: correct ⇒ box +1; wrong ⇒ box 0.
- "Zor" tab: cards with ≥ 2 wrongs. "Tümü": all ever seen. Due count drives home & notebook badges.

## 11. Statistics
Learned words (= cards ever answered correctly), accuracy (correct / answered), longest streak (days with ≥1
completed wheel), completed wheels, words per day (last 7 days), per-level progress (learned / total), hardest
letters (lowest accuracy, min 5 attempts).

## 12. Engine events & effects (contract)
```
Events:  Start(timestampMs) | KeyTapped(keyIndex) | Backspace | Pass | UseJoker(type) |
         Tick(elapsedMs) | Pause | Resume | Forfeit
Effects: LetterPlaced(cellIndex, letter) | Correct(letter, card, points) | Wrong(letter, card) | Passed(letter) |
         Advanced(toLetter, lap) | ComboReached(streak) | TimeWarning(secondsLeft ≤ 10, lowest per step) |
         JokerApplied(type) | TurnChanged(player) | GameOver(summary) | MatchOver(result)
Duel:    BeginTurn(timestampMs) | Play(gameEvent)   (events of the active player)
```
- `Tick.elapsedMs` is the time since the previous tick (a delta); the ViewModel produces it from its `TimeSource`.
- `TimeWarning(secondsLeft)`: seconds left = remaining ms rounded up. Emitted when that value drops to ≤ 10; at
  most one per transition (a big tick or a time joker that skips seconds emits only the lowest one); none at 0.
- `LetterPlaced`: cells are answer letters only (spaces are not cells). `Advanced.lap` starts at 1.
- `Start.timestampMs` is only recorded (`startedAtMs`); it never influences rules.

Every rule above must have a unit test named after the rule, e.g. `pass_endsTurn_inDuel()`.

## 13. Randomness & seeds
- `SeededRandom` = SplitMix64 (reference `splitmix64.c`), own implementation; golden sequences are unit-tested.
- `nextInt(bound)` is unbiased (31-bit rejection sampling, as `java.util.Random`); shuffles are Fisher–Yates from
  the last index down (`j = nextInt(i + 1)`).
- String hashing for seeds = FNV-1a 64 over UTF-8 (`Seeds.stableHash`), never `String.hashCode()`.
- Sub-seed per purpose: `Seeds.derive(seed, purpose) = mix64(seed xor stableHash(purpose))`, always from the
  game seed (never from a stream's current position). Purposes:
  | purpose | used for |
  |---|---|
  | `wheel` | card pick per letter (`WheelGenerator`, also the review wheel) |
  | `keys:<letter>` | key set + shuffle of the slot with that letter (`KeyboardGenerator`) |
  | `player:<n>` | duel player n's (1-based) game seed; their `wheel` and `keys:<letter>` derive from it |
