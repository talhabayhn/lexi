# Proposal: leak-check exemption list for base A1 words

Status: implemented (uncommitted): `content/leak-exempt.txt`, `qa.py`, `ContentCompiler.checkExampleLeaks` (+2 tests), `fill_blank_scan.py` R9. Applies to the example-leak rule (`docs/CONTENT.md` §Example leak).

## Problem
A1 has 847 answers, so `example_en` of any other A1 card may not contain them. Many are glue words (`very`, `every`,
`old`, `big`), so natural A1 sentences are almost impossible; FB-A1-1 needed heavy workarounds (inflections, rare nouns).

## Proposed exempt words (25)
`about, after, all, also, always, around, because, before, big, every, first, good, last, little, long, many, more,
much, new, next, old, one, ten, very, when`
(Extend to `what, where, who, why, how` only if the cards stay in the exempt list below.)

## Why it is safe
- They are function words, quantifiers or very basic adjectives: seeing one in another card's Example joker reveals
  nothing useful about its own card, and the player already knows them.
- The leak rule exists to stop the Example joker spoiling the *answer of the card shown*. These words are answers
  of their own cards, but are seen in nearly every sentence anyway.
- Their own cards stay in the game, and their own examples must still contain them (blank rule unchanged).
- Not exempt: content nouns/verbs/adjectives that make a clue guessable (`dog`, `cold`, `hungry`, `school`).

## How to apply
1. Add `content/leak-exempt.txt` (one lowercase word per line, `#` comments). Single source for all tools.
2. `tools/content-qa/qa.py`: load the file; in the example-leak loop skip `o["answer"].lower()` found in the set.
3. `ContentCompiler.checkExampleLeaks`: same skip, reading the file (needs a data-engineer task and a test:
   exempt word passes, non-exempt word still fails).
4. `tools/content-qa/fill_blank_scan.py`: it has no leak rule today; add R9 (leak) using the same file so scan and
   compiler agree. Add a case to `test_fill_blank_scan.py`.
5. Update `docs/CONTENT.md` §Example leak with one sentence + link to the list; do not change card answers.

## Open points
- A2+ levels: the same list can apply (their answers are checked per level), start with A1 only.
- Review the list after the first re-run; add words only with a counted reason (sentences blocked).
