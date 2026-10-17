# Exam packs — content format and open compiler work

Owner decision (2026-10): exam pack content lives in this repo under `content/exam-packs/` (the repo will become
private before release). It uses the compiler's existing external-pack layout (`CONTENT.md` §1), so no compiler
change is needed to compile it.

## Layout
```
content/exam-packs/
  packs.csv            id,name,description,is_pro,sort          read by the compiler today
  packs-meta.csv       id,title_tr,title_en,description_tr,target_level_range,card_target,free_preview_letters
                       (ignored by the compiler today; see "Compiler changes")
  units/<pack-id>.csv  card_id,unit                              pack membership; unit = curriculum/topic slug
  cards/<LEVEL>/<pack-id>.csv                                    pack-only cards, same 11 columns as content/cards
```
- A pack lists every card it plays in `units/<pack-id>.csv`: its own pack-only cards and **references** to base
  cards (`content/cards`, any level) or to another pack's cards. A word that already exists anywhere is always
  referenced, never re-created (no copied clues). LGS and YDS / YÖKDİL share a few cards this way.
- Pack-only cards carry `tags = exam:<pack>` (plus `exam:<other>` when another pack references them) and a normal
  category; they are `pack_only = 1` in `content.db`, so they never appear in free level/category wheels.
- `unit` values are English slugs (`friendship`, `science`, `health`, …). The app must map them to string
  resources (TR + EN), never show the slug.
- One wheel = one card per letter, as in levels; every pack has ≥ 5 cards per letter (QA script, below).
- Answers: A–Z and single spaces, 2–14 letters (same `AnswerRules`).

## Constraints that shape pack cards (enforced by the compiler today)
- Answers are unique per level across base **and** all packs → a pack word that exists at the same level is a
  reference, not a new card.
- Example leak check runs per level over base + pack cards: a pack card's `example_en` must not contain any other
  answer of its level, and adding a pack answer must not appear in any base example of that level. Multi-word
  answers therefore sit at a level where none of their component words is an answer (e.g. `POCKET MONEY` is B1
  because `POCKET` is an A2 answer). Some words (e.g. `TEAM`) appear in base examples on every level and cannot be
  added without rewriting base examples; they are listed in the review notes.

## QA without Gradle
`python3 tools/content-qa/qa.py [--quiet] [--min 5]` (repo root) re-implements the compiler's card rules that
matter for packs (charset, letter rule, clue leak, example contains answer, example leak per level, duplicates,
blocklist, categories, unit references) and adds the per-pack per-letter minimum. The compiler stays the source of
truth; run it after the script.

## Compiler changes (for the local session; not done yet)
1. **Pick up the in-repo packs by default**: `generateContentDb` / `run` should use `content/exam-packs` when
   neither `--packs` nor `LEXI_PACKS_DIR` is given (or the build sets it), and `.gitignore` keeps ignoring only the
   old `content/exam_packs/`.
2. **Pack metadata**: either widen `packs.csv` to the `packs-meta.csv` columns and add the matching
   `exam_pack` columns (`title_tr`, `title_en`, `description_tr`, `level_range`, `card_target`,
   `free_preview_letters`), or read `packs-meta.csv` and join it by id. Validation: ids match `packs.csv`;
   `target_level_range` = `<LEVEL>-<LEVEL>`; `card_target` positive int; `free_preview_letters` = distinct A–Z.
3. **Per-pack coverage**: error if a pack has < 3 cards for a letter, warning < 5 (same thresholds as levels).
4. **Pack card level window** (warning): a pack-only card whose level is outside the pack's
   `target_level_range`.
5. Report: cards per pack × letter and pack-only vs referenced counts.

## Owner decisions (2026-10)
- `lgs` is free (`is_pro = 0`, no preview letters), matching the design (no PRO badge).
- YDS and YÖKDİL are ONE Pro pack, `yds-yokdil` (one row in the design), `free_preview_letters = ABCDE`: what a
  free user can try from it. Its units keep both source themes (YDS: connectors … collocations; YÖKDİL: sciences,
  health, social-sciences, research-methods); a card shared by both appears once, under its first unit.
- Row subtitles in the design ("600 kelime", "2000") are placeholders; show the real card counts.
- TEAM is not added (no edits to base cards).
