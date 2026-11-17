# Turkish clue review, A2 to NAT (index)

The clue standard (`docs/CONTENT.md` "Clue standard (TR)") and the scanner (`tools/content-qa/clue_scan.py`) are applied to
A2, B1, B2, C1 and NAT, as was done for A1 (`CLUE-A1-1.md`). **No card was edited**: every proposal is in a review
sheet, waiting for the owner. Sheets (read on a phone, grouped by letter): [A2](CLUE-A2-1.md), [B1](CLUE-B1-1.md),
[B2](CLUE-B2-1.md), [C1](CLUE-C1-1.md), [NAT](CLUE-NAT-1.md); the same rows as CSV (`CLUE-<LEVEL>-1.csv`: id, answer,
current_clue, example_en, rule, proposed_clue, note).

| level | scanner hits (C1 / C2 / C3 / C4) | in the sheet | C1 | C2 | C3/C4 (worst 50) | new clue | keep |
|---|---|---|---|---|---|---|---|
| A2 | 4 / 11 / 61 / 33 | 65 | 4 | 11 | 50 | 34 | 31 |
| B1 | 8 / 7 / 36 / 39 | 65 | 8 | 7 | 50 | 32 | 33 |
| B2 | 12 / 5 / 20 / 45 | 67 | 12 | 5 | 50 | 27 | 40 |
| C1 | 4 / 0 / 16 / 27 | 47 | 4 | 0 | 43 | 13 | 34 |
| NAT | 0 / 5 / 13 / 1 | 18 | 0 | 5 | 13 | 14 | 4 |
| **total** | | **262** | **28** | **28** | **206** | **120** | **142** |

- **Scanner hits** are the whole level (from `clue_scan.py`); the sheet holds every C1 and every C2 hit and the 50 worst
  C3/C4 hits (all of them for C1 and NAT, which have fewer). Rows with C1 or C2 are never counted as C3/C4. "Worst" =
  both C3 and C4 first, then C3 (longest clue first), then C4 (shortest clue first), ties by id.
- **Proposal** = one clue of 3 to 7 words, defining the answer's own meaning in the sense of `example_en`, fitting the
  card's `pos`, without the answer. **keep** = the current clue is fine and the scanner was wrong; the reason is in the
  sheet (most C3 hits are a noun head ending in -an/-en/-ip/-up/-mak/-mek such as "hayvan", "orman", "tören", "yemek";
  most C4 hits are a correct short gloss whose words just share no stem with `example_tr`). 142 of 262 rows are keeps.
- **Check (no card edited):** the proposals were applied to a temp copy of `content/` and `tools/content-qa/qa.py` ran on it:
  **0 errors, 0 warnings** (clue leak, example leak, blocklist, duplicates, coverage), the same as on the real content.
  `clue_scan.py` on that copy no longer flags any of the 120 rewritten rows.
- **Scanner fix** (in this commit): `tr_words` did not know â, î, û, so "dükkân", "resmî" or "rüzgâr" counted as two words
  (false C2 on 5 rows, and a missed C1). With the fix A2 loses 3 C2 hits, B1 1, C1 1, B2 gains 1 C4. A1 (already reviewed) gains
  one C1: `a1-wind` "rüzgâr" is a bare translation; suggested clue "esen, yön değiştiren hava akımı"; it is not in any sheet.
- **Not covered:** C3/C4 hits beyond the 50 worst per level (they stay advisory), pack-only cards of the exam packs.

## How to approve

Reply with `hepsi onay, hariç: <id> → <clue>` (the exceptions are optional and may be several, one per line), or name a level:
`A2 onay`. "hepsi onay" applies every proposal of the five sheets and leaves the **keep** rows as they are; an exception
replaces that row's proposal with your clue, and `<id> → keep` leaves the current clue. A follow-up task then writes the
clues into `content/cards/`, keeps the ids (progress is keyed by them), runs the compiler and `qa.py`, and updates the
`CLUE-SCAN.csv`. Clue changes alter no id, answer or wheel, so saved games and progress are unaffected.
