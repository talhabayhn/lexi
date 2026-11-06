# Fill the blank ("Boşluk doldurma") — mode proposal

Status: **PROPOSAL, not approved, not merged into `GAME_RULES.md`.** Screens: `screens/21…24-fillblank-*-proposed.{html,png}`
(logic sources in `source/`), copy: `fill-blank-copy.md`. ⚙ = tunable in `GameConfig`. **DECISION n** = owner decides (list in §9).

## 1. Idea
The player sees an English example sentence with the target word blanked ("A tiny ___ is singing nearby.") and the
Turkish translation of the sentence as the clue. The word is typed with the existing letter keys. Same Bauhaus look,
same keys / answer cells / PAS / jokers; the A–Z wheel is replaced by a 10-chip progress strip.

## 2. Round structure (wheel or not)
Proposal: **a round of ⚙ `fillBlankRoundSize = 10` sentences, no visible wheel.**
- A–Z letters mean nothing for a sentence; a 26-sentence round is ~10 minutes of reading, too long for the clock.
- Internally the round is **a sparse wheel**: 10 slots with distinct letters in A–Z order, exactly like the review wheel
  (`Wheel` already allows < 26 slots). The player never sees the letter; the strip shows 1…10 with the wheel chip states
  (todo cream circle, current ink square, correct blue circle, wrong red square, passed yellow leaf).
  This keeps `GameEngine`, `KeyboardGenerator`, scoring, jokers, SRS and stats unchanged (§8).
- Order of play = A–Z order of the 10 letters (looks random to the player). A pass comes back after sentence 10 (lap 2),
  as on the wheel.

## 3. Card selection and exclusion
A card is **usable** (`FillBlank.isUsable(card)`, pure function in `:core:domain`) when ALL hold:
1. `rule == STARTS` (so the answer's first letter = the slot letter and the existing "given first letter" rule applies).
2. The example contains the answer exactly once as whole words, as written. (Already guaranteed by the compiler for every
   card today: checked on all 4 877 cards, 0 exceptions. Multi-word answers match contiguously: `GIVE UP`.)
3. Example has ≥ ⚙ 4 words and the answer has ≥ ⚙ 3 letters (shorter sentences / words are too ambiguous: "Wipe the ___.").
4. The card has no `no-blank` tag in `tags` (curator escape hatch, no CSV schema change).

Current usable counts (rules 1–3): A1 698 / 847, A2 703 / 962, B1 880 / 1037, B2 648 / 745, C1 623 / 675, NAT 521 / 611.
Letter X has no `STARTS` card at any level, so the round picks its 10 letters from the letters that have usable cards.

Quality gate (content, not code): the compiler prints a **fill-blank report** (warning only): usable count per level,
sentences ≤ 4 words, answers ≤ 3 letters, and sentences whose translation does not contain a Turkish form of `clue_tr`
(heuristic) — the curator reviews that list once and tags bad ones `no-blank`. Needs a content pass before release
(see §7 risks); the other session owning `content/` is not touched by this proposal.

## 4. How the blank is generated
No inflection logic is needed: content validation already requires the answer to appear **exactly as written** (whole
word, any case), so the blank is always the base form that the sentence uses. `GameUiMapper.blankExample` already blanks
whole words in any case and keeps the spaces of multi-word answers; the round screen reuses its regex and splits the sentence
into `before | blank | after`.
- **Capitalisation:** the answer cells are always uppercase; the sentence keeps its own case (a sentence-initial answer
  just leaves the blank at the start; 167 cards). After the judgement the sentence shows the answer in the sentence's case
  ("Window" at the start, "window" elsewhere).
- **Blank look:** one inline cream box with a red bottom edge (the "next cell" border colour), width ≈ 20 dp × letters
  (min 56 dp), so length is visible; multi-word answers get one box per word (the answer cells below also show the gap).
  Screen-reader text: "boşluk, 4 harf".
- **Ambiguity handling (several words could fit "The ___ is closed."):** (a) the first letter is given (yellow cell), (b) the
  number of cells shows the length, (c) the Turkish translation, (d) the keys are the answer letters + distractors, so a
  synonym with other letters can't be typed. No alternative-answer list in v1 (**DECISION 5**).
- Spelling-only judgement as everywhere (case-insensitive exact match on the full word).

## 5. Scoring, timer, passes, jokers
- **Scoring:** unchanged §6: 10 × level multiplier, streak bonus +2 per streak step (cap +10), time bonus 1 pt per remaining
  second when all 10 are done, no Hardcore multiplier. Max (A1): 10×10 + streak 90 + time ≈ 190–250, so a round's points are
  not comparable to a wheel's → points are shown but never mixed in a leaderboard/best-score with wheel modes.
- **Timer:** one clock for the round, ⚙ `fillBlankTimeLimitSeconds = 150` (15 s per sentence; reading + typing a 5–7 letter
  word). The Classic time setting does not apply. Reveal pause and warning rules (§3, motion #8) unchanged.
  Untimed variant: none in v1 (**DECISION 3**: Practice-like "Pratik" for this mode?).
- **Pass:** same as the wheel (clears typed letters, returns on the next lap, no penalty in the score).
- **Jokers** (inventory shared with the wheel, §7; one per sentence):
  | Joker | Fill-blank | Why |
  |---|---|---|
  | Harf aç (Reveal letter) | **yes**, −10 s | works as is |
  | Çeldiricileri temizle | **yes**, −15 s | works as is |
  | Telaffuzu dinle (Listen) | **yes**, −10 pts, not at C1/NAT | speaks the answer word; still useful |
  | Örnek cümle (Example) | **hidden / disabled** | the sentence is already on screen; reason "bu modda anlamsız" |
  Not proposed for v1: a new "Kelime ipucu" joker showing `clue_tr` (needs a new joker type, inventory, store sync).
  Rewarded-ad joker row behaves as today.
- **Level rules:** A1–C1 and NAT, same level select. No Hardcore / Daily / Duel variant, no categories and no exam packs in v1
  (the category row is hidden while the mode is selected; later `category` can filter the pool unchanged).

## 6. Notebook, SRS, statistics, limits, determinism
- **SRS (§10): unchanged and shared.** A WRONG or PASSED sentence puts its card into box 0 of the notebook; a correct one for a
  notebook card moves it up. The notebook shows the card as always (word, clue, example); the review wheel stays a letter wheel.
  The result list shows the full sentence with the word underlined for every missed sentence, so the context is learned too.
- **History:** `usesCardHistory = true`; the least-recently-seen rotation of §1 applies per letter and cards answered in
  any mode count as seen (one `lastSeenMs` per card).
- **Statistics (§11):** words learned, accuracy, hardest letters and words per day read `CardProgress`, so fill-blank answers
  count automatically. A finished round counts as a played session for the streak and the daily goal; "completed wheels" on the
  stats screen then includes rounds (**DECISION 4**: count them separately as "Cümle turu" or together).
- **Daily limits:** none. No daily shared round, no best-score board in v1 (a "Günün cümleleri" daily is a possible v1.1,
  which the sub-seed design below already allows).
- **Determinism (§13):** same `seed` + same pool + same events ⇒ same round. New sub-seed purpose `fill:letters`:
  `SeededRandom(Seeds.derive(seed, "fill:letters"))` Fisher–Yates-shuffles the letters that have ≥ ⚙ 3 usable cards
  (history-filtered pool), takes the first `fillBlankRoundSize`, sorts them A–Z. Card per letter via the existing `wheel`
  sub-seed (one `nextInt` per letter, `STARTS` only), keys via `keys:<letter>`. No `Random()`, no clock in the engine.
  New golden test: fixed seed → fixed 10 letters / cards / keys.

## 7. Screens (see `screens/*-proposed`)
| File | Screen | Notes |
|---|---|---|
| `21-fillblank-home-proposed` | Home entry | the full-width duel tile becomes a row of two 96 dp tiles: "Düello" and "Boşluk doldurma". Opens `LevelSelectRoute(mode = FILL_BLANK)` like the practice tile passes PRACTICE. Alternative: no Home change, mode only in the level select (**DECISION 1**) |
| `22-fillblank-level-select-proposed` | Level + mode | 4th segment "Boşluk · 10 cümle" in OYUN MODU (each segment 86 dp, "Hardcore" still fits at 16 sp); BAŞLA chip "A1 · BOŞLUK · 10 CÜMLE" |
| `23-fillblank-game-proposed` | Round | `04-game` HUD, 10-chip strip (replaces the 320 dp wheel), sentence card (label + sentence 30 sp with the clue shrink rule down to 18 sp + level/length panel), Turkish translation box, answer cells, keys, joker / backspace / PAS |
| `24-fillblank-result-proposed` | Result | `06-result` layout: points, correct / wrong / pass, "Tekrar edilecekler" rows with the full sentence (answer underlined) + Turkish + listen button, TEKRAR OYNA; no Paylaş in v1 |
No design for: pause dialog text (reuse), word-detail sheet (reuse), error / empty states (`EmptyState`), reveal state after a
wrong answer (same as `04-game`: cells show the answer in red, the blank fills with the answer for 1.2 s).

## 8. Feasibility
**Reused as is:** `GameEngine` (turn flow, given first letter, reveal, pass lap), `KeyboardGenerator`, scoring, jokers, `Srs`,
`Stats`, `WheelGenerator` card pick, `GameUiMapper.blankExample`, answer cells, keys, actions, HUD, timer, motion, level select,
result screen skeleton, saved-game format (cards are stored), `ContentRepository` pool queries.

**New / changed (≈ in this order):**
1. `:core:domain`: `GameMode.FILL_BLANK(isScored = true, endsOnFirstWrong = false, usesCardHistory = true)`; `GameConfig`
   (`fillBlankRoundSize`, `fillBlankTimeLimitSeconds`, usable-card thresholds, per-mode disabled joker set so EXAMPLE is refused
   in this mode); `FillBlank.isUsable`; `WheelGenerator.fillBlank(pool, seed, history, config)` building the sparse wheel;
   `docs/GAME_RULES.md` §5/§7/§13 update. Tests first (rules above, golden seed, "usable" boundaries, sparse wheel order).
2. `:core:data`: store the new mode name in sessions (mode is a string; no table change expected, verify), pool query with
   the usable filter (or filter in `:core:domain`), compiler fill-blank report + `no-blank` tag validation (no new column).
3. `feature:game`: `GameUiMapper` turn = sentence + translation (clue card variant), `RoundStrip` composable (reuses wheel chip
   look), result summary carrying sentence/translation for missed rows, ViewModel branch by mode.
4. `feature:levels`: 4th mode segment, hidden category row; `feature:home`: tile row (or none).
5. Resources TR/EN, Roborazzi screenshots (23, 24, small phone 360 × 640 with the 13-word worst case).
Roughly 70 % reuse. **Effort: L** in total (domain M, UI M, content QA S–M). **Tier: Risky** (new game-mode rules in the
engine config, persistence of a new mode, more than two modules); plan first, `code-reviewer` until APPROVE, one Gradle
build at a time.

**Risks**
| Risk | Mitigation |
|---|---|
| Ambiguous blanks ("The ___ is closed." fits bridge, shop, door…); 16 % of cards have ≤ 4 words | usable filter, given first letter + length + keys, `no-blank` curation, optional alternates later (DECISION 5) |
| Bad / unnatural example sentences or mismatching translation | report in the compiler + one curator pass before enabling the mode; the mode ships behind `FeatureFlags.fillBlankMode` until it is done |
| The Turkish translation makes the answer trivial (translation game) | intended (the owner's brief); first-letter + length keep spelling relevant; B2+ sentences are longer and idiomatic. Option: tap-to-reveal the Turkish (DECISION 2) |
| Sentence too long for the card on small phones | same shrink rule as the clue (30 → 18 sp, then ellipsis); worst case 13 words tested at 320 × 568 |
| Few usable cards for rare letters (Q, J, Z) repeat often | letters need ≥ 3 usable cards to be drawn; rotation rule of §1 |
| Scores of two different modes compared | no shared best-score / leaderboard; points labelled per mode |
| Store declarations | no new SDK, no account, no Pro: none needed |

## 9. Decisions for the owner
1. **Entry:** Home tile row (duel + fill-blank half-width, +12 dp tile height, "Düello" short title) or level-select segment only?
2. **Turkish translation:** always visible (as drawn) or hidden behind a tap ("Türkçesini göster", −points)?
3. **Untimed variant:** only the 150 s round, or also a Practice-style untimed round?
4. **Stats:** do rounds count into "completed wheels" / streak / daily goal together with wheels (proposed), or separately?
5. **Ambiguity:** accept only the exact answer (proposed) or add a curated `alt` answers column later?
6. **Round size and clock:** 10 sentences / 150 s OK? (alternatives: 8 / 120 s, 12 / 180 s)
7. **Jokers:** Example joker disabled; no new "word clue" joker in v1 — OK?
8. **Gating:** ship free for everyone, or Pro later? Ship after the content pass behind a flag?
