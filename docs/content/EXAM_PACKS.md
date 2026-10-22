# Exam packs — content format and open compiler work

Owner decision (2026-10): exam pack content lives in this repo under `content/exam-packs/` (the repo will become
private before release). The compiler reads it by default (`CONTENT.md` §1) and every app build compiles it into
`content.db`.

## Layout
```
content/exam-packs/
  packs.csv            id,name,description,is_pro,sort          read by the compiler today
  packs-meta.csv       id,title_tr,title_en,description_tr,target_level_range,card_target,free_preview_letters
                       joined to packs.csv by id (both files required, same ids)
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

## Compiler validation (done, `ContentCompiler`)
Errors fail the compiler and therefore the app build (`generateContentDb`, nothing written); warnings are printed.
1. **Default packs**: `content/exam-packs` unless `--packs <dir>` / `LEXI_PACKS_DIR` (`--packs none` = no packs);
   `.gitignore` keeps ignoring only the old `content/exam_packs/`.
2. **Metadata**: `packs-meta.csv` joined by id into `exam_pack` (`title_tr`, `title_en`, `description_tr`,
   `level_range`, `card_target`, `free_preview_letters`). Errors: ids differ from `packs.csv`; `target_level_range`
   not `<LEVEL>-<LEVEL>` (ascending); `card_target` not a positive int; `free_preview_letters` not distinct A–Z.
3. **Per-pack coverage** (packs in `meta.json` `shippedPacks`): error < 3 cards for a letter, warning < 5.
   Unshipped packs only warn about letters below 3.
4. **Level window** (warning): a pack-only card outside its owner pack's `target_level_range`. The owner is the pack
   its file `cards/<LEVEL>/<pack-id>.csv` is named after; a pack-only card in another file is checked against every
   pack that uses it; base cards and another pack's cards are references and never checked.
5. **Report**: cards per pack × letter, pack-only vs referenced counts, units, Pro flag.
6. Unique answers per pack, unknown/duplicate unit rows, unused pack cards (warning), card rules as in `CONTENT.md` §2.

## Runtime (M7)
- `ExamPackRepository` lists packs and reads a pack's cards / card ids (pack-only cards included); level and
  category wheels keep `pack_only = 0`.
- Pack wheel: `WheelGenerator.pack`, Classic rules (`GAME_RULES.md` §1, §5). Units are not chosen yet (whole pack;
  a unit picker needs design).
- Pro gate: `EntitlementRepository.isPro`; production is never Pro until RevenueCat replaces only that binding.
  Debug builds are unlocked (Android: `-Plexi.debugUnlockPro=false` shows the locked state; iOS: Debug
  configuration). Locked packs show PRO + "Yakında" and a toast on tap; `free_preview_letters` is not used yet
  (paywall/preview needs design and RevenueCat).

## Owner decisions (2026-10)
- `lgs` is free (`is_pro = 0`, no preview letters), matching the design (no PRO badge).
- YDS and YÖKDİL are ONE Pro pack, `yds-yokdil` (one row in the design), `free_preview_letters = ABCDE`: what a
  free user can try from it. Its units keep both source themes (YDS: connectors … collocations; YÖKDİL: sciences,
  health, social-sciences, research-methods); a card shared by both appears once, under its first unit.
- Row subtitles in the design ("600 kelime", "2000") are placeholders; show the real card counts.
- TEAM is not added (no edits to base cards).
- `yds-yokdil` level window is B1–C1 (was B2–C1, contentVersion 22): the exam's health and science vocabulary is
  taught from B1, so its 12 B1 cards stay in the pack and the level-window warnings are gone.
