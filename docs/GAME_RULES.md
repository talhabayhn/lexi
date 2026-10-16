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
  ⚙ `recentlySeenWindowDays = 7` (`WheelGenerator.recentlySeen(lastSeenMs, nowMs, config)`). Owner decision:
  the window stays at 7 days.
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
   - While a wrong answer is revealed, input is ignored and the game clock stands still (owner decision; so time
     can't run out during a reveal); the reveal ends after `revealMs` of ticks, then the engine advances
     (Hardcore: game over).
   - Owner decision: a pass clears the typed letters (they go back to their keys), so the slot comes back empty on
     the next lap. Joker effects survive the pass, because the joker was paid for and the slot can't get another
     one (§7): letters placed by Reveal letter stay locked in their cells, disabled distractor keys stay disabled,
     and the slot's joker stays used. If time runs out while it is CURRENT again, it stays PASSED (counted as
     passed and missed, §10).
   - Owner decision: `Forfeit` (give up) ends the game with reason `FORFEIT`; every remaining TODO, PASSED or
     CURRENT slot becomes WRONG (counted as wrong, listed in `missedCards`, sent to SRS as wrong, §10). SKIPPED
     slots stay skipped. Only `GameOver` is emitted (no per-letter `Wrong` effects). A forfeit before `Start`
     (duel: during the first handover) also ends the game, with the full time left.
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
| Daily | ⚙ 180 s | yes | seed per date+level, **one scored attempt per day** (owner decision: per day, not per level), shareable |
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
| Harf aç (Reveal letter) | puts the correct letter into the next empty slot | ⚙ −10 s (untimed modes: −5 pts) | 1 joker per slot |
| Çeldiricileri temizle (Remove distractors) | disables all distractor keys | ⚙ −15 s (untimed modes: ⚙ −5 pts) | 1 joker per slot |
| Örnek cümle (Example) | shows example sentence with the word blanked | ⚙ −5 pts | 1 joker per slot |
| Telaffuzu dinle (Listen) | speaks the answer (TTS) | ⚙ −10 pts | 1 joker per slot; disabled at C1 and NAT |
- Inventory per joker type, persisted. Sources: start pack (2/1/3/1), daily gift +1 random, 7-day streak bonus
  +1 random, rewarded ad +1, Pro +3/day refill.
- Owner decision (free gifts, ⚙ `GameConfig.jokerGifts = JokerGiftConfig(dailyGift = 1, cap = 5,
  streakBonusEvery = 7, streakBonus = 1)`): 1 joker on the first app open of each local calendar day; inventory cap
  5 (applied per joker type, see below — to confirm with the owner); +1 bonus joker each time the streak (§11) completes 7 days.
- Engine decisions (`JokerGifts`, `GrantDailyJokers`): the cap is per joker type (the start pack already totals 7)
  and only limits gifts; each gifted joker goes to a type picked uniformly among the types below the cap (enum
  order, `SeededRandom` seeded per §13: daily gift from today's epoch day, bonus from the milestone day), re-checked
  after each pick; with every type at the cap there is no gift but the day/milestone is used up. "Local day" =
  `AppClock.epochDay` in the device time zone; the last gift day is stored and a gift is given only when today is
  after it (a clock or time zone moved back gives nothing and never lowers the stored day; moving it forward is not
  detectable offline). The streak bonus is checked at app open, not when the session is recorded: when the current
  streak (the run ending today or yesterday) has reached a multiple of 7 on a day after the last rewarded milestone
  day, one bonus is given (only the latest milestone of a run; a new run after a break counts again). So the bonus
  for day 7 arrives with the first open after that wheel (usually the next day); daily gift first, then bonus. The
  start pack is granted before the first gift. Gifts run once per cold start (splash) and are idempotent per day;
  a process kept alive across midnight gets the next gift at its next cold start. Gift inventory and bookkeeping are
  written in one `user.db` transaction (`JokerRepository.updateWallet`, table `joker_grant`).
- Jokers are not allowed in Daily (fair comparison), Duel and Level test (would distort the placement). ⚙
  `jokerFreeModes`. Owner decision: no jokers in the level test.
- Owner decision: Remove distractors costs points in untimed modes like Reveal letter (⚙
  `removeDistractorsUntimedCostPoints = 5`; 0 = free). Untimed modes are unscored today, so point costs only
  matter if one becomes scored.
- A slot answered with a joker still counts as correct but its streak bonus is 0 (the streak itself goes on).
- Owner decision: one joker per letter in total, of any type. After any joker on a slot, every further joker is
  refused for that slot, also on later laps.
- Engine decisions: Reveal letter fills the first
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
  second player's game starts on their first turn. Owner decision: `Forfeit` (also during the handover, even
  before the forfeiting player's first turn) ends the whole match, the other player wins, and the forfeiting
  player's remaining letters count as wrong.
- Race (online): same wheel, simultaneous, no turns; first to finish all slots wins, else most correct at time-up.
  Level handicap (optional room setting): each player plays own level; score = normalized points.

## 9. Level test (onboarding)
- 10–14 slots (owner decision: at most 14 questions, with early stop), adaptive staircase: start at A2; 2 correct
  in a row at a level → go up; 1 wrong/pass → go down.
- Result = highest level with ≥ 60% correct among its asked cards (min 3 asked), else the level below. Shown on
  a result card ("Seviyen: B1") with the option to change.
- Engine (`LevelTest`): the test ends after 10 answers once the current level has ≥ 3 asked cards (so it can be
  judged), and always after 14. The early-stop threshold moved from 12 to 10 with the cap: it keeps the old
  4-answer window between early stop and cap, so a test that has just changed level still has room to ask the 3
  cards needed to judge the new level; with 12 the window would be 2 answers and most tests would just hit the cap.
  An all-correct run ends after 11 answers at NAT. "Else the level below" = the level below the start level (A1); owner decision:
  a test that starts at A2 with too few correct answers places the player at A1. Level stays within
  A1…NAT. Each card is a seeded pick (sub-seed `level-test:<index>`) of an unasked card of the current level,
  preferring unused letters. If the pool has no unasked card of the current level, the test ends early and the
  result judges what was answered (`LevelTest.isFinished(state, pool)`).
- Integration (M6): the level test can't use a pre-built wheel, because each card depends on the previous answer.
  The onboarding ViewModel owns `LevelTestState` and runs one `GameEngine` game per card (mode `LEVEL_TEST`, a
  one-slot `Wheel` with the card from `nextCard`, seed = the test seed), feeds its CORRECT/WRONG/PASSED result to
  `LevelTest.record`, and carries the remaining time of the ⚙ 120 s clock into the next one-slot game (time up
  ⇒ `LevelTest.result` on the answers so far). The wheel UI shows the asked letters as they come.

## 10. SRS — Kelime Defterim (Leitner)
- Boxes 0–4 (the 4 bars in the notebook UI = box 1–4; box 0 = red/new). Intervals: box0 = today, 1 = 1 d,
  2 = 3 d, 3 = 7 d, 4 = 21 d; correct in box 4 ⇒ "mastered" (leaves due queue).
- In any game: WRONG or PASSED (never answered later) ⇒ card enters/returns to box 0 and appears in
  "Tekrar edilecekler" on the result screen. Correct in a game for a card already in the notebook ⇒ box +1.
- Review wheel: correct ⇒ box +1; wrong ⇒ box 0.
- "Zor" tab: notebook cards with ≥ 2 wrongs. "Tümü": every notebook card (box ≠ null; mastered ones last). "Tekrar": the
  due cards, most overdue first. Due count drives home & notebook badges. Rows show 1–4 bars = box + 1 (box 4 and
  mastered both 4; red at 1, yellow at 2, blue from 3). The word sheet's "Defterden çıkar" (`Srs.removeFromNotebook`)
  clears box and due day, keeps the counters; a later wrong or passed answer brings the card back into box 0.
- Review wheel (`GameMode.REVIEW`, started from the notebook as `GameRoute.review()`): the game reads the due cards at
  start, `WheelGenerator.review` builds the wheel, the game's level is the first card's level; nothing due ⇒ the game
  is abandoned (the notebook only offers it while something is due).
- Engine (`Srs`, `CardProgress`): every answered card has a progress row; only missed cards are in the notebook
  (box ≠ null). A first-time correct card stays outside the notebook. PASSED counts as missed but not as wrong.
  Wrong/passed mastered cards return to box 0. Correct for a notebook card moves it up even if it wasn't due yet.
  Results come from the final slot states (`Srs.results`); unanswered cards are left out. Owner decision: hot-seat
  duel answers don't change the notebook (⚙ `srsExcludedModes = {DUEL}`): `Srs.update` returns no changes, so `seen`,
  `correct`, `wrong` and `lastSeenMs` stay untouched and duel answers count neither in Stats (§11) nor in the
  recently-seen window (§1). Days are epoch days in the player's time zone.

## 11. Statistics
Learned words (= cards ever answered correctly), accuracy (correct / answered), longest streak (days with ≥1
completed wheel), completed wheels, words per day (last 7 days), per-level progress (learned / total), hardest
letters (lowest accuracy, min 5 attempts).
Engine (`Stats`): "words per day" = correct answers per day (all sessions); percentages are rounded half up;
hardest letters are sorted by exact accuracy, ties by letter; per-level progress needs the card totals per level.
Card-based stats read `CardProgress`, which duel answers don't touch (⚙ `srsExcludedModes`); the data layer
records no `SessionRecord` for those modes either, so streaks and words per day exclude them too.
Current streak (`Stats.currentStreak`, home and the joker streak bonus §7) = the run of consecutive streak days
ending today, or yesterday while today's wheel isn't done yet.

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
  Ticks are additive: `Tick(a)` then `Tick(b)` gives the same state as `Tick(a + b)` (also across the reveal end,
  the warning and time up; only `TimeWarning` effects may differ, see below), so the tick interval never matters
  and saved event logs coalesce consecutive ticks (`GameEngineTickSplitTest`, ADR 0019). Ticks ≤ 0 are ignored.
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
  | `level-test:<index>` | the level test's n-th card pick (`LevelTest.nextCard`) |
  | `player:<n>` | duel player n's (1-based) game seed; their `wheel` and `keys:<letter>` derive from it |
  | `joker-gift:daily` | daily joker gift type picks; base seed = today's epoch day, not a game seed (`JokerGifts`) |
  | `joker-gift:streak` | streak bonus type picks; base seed = the milestone's epoch day (`JokerGifts`) |
