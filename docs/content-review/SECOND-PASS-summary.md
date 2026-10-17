# Second-pass scan summary

Cards: A1 787, A2 878, B1 849, B2 729, C1 675, NAT 611 (total 4529). Flagged cards: 102.

Level-vs-frequency check skipped (no offline frequency list); `rare_words_low_level` is a corpus-frequency proxy.

| check | A1 | A2 | B1 | B2 | C1 | NAT | total |
|---|---|---|---|---|---|---|---|
| answer_in_clue | 0 | 1 | 0 | 1 | 0 | 0 | 2 |
| example_too_short | 5 | 0 | 0 | 0 | 0 | 0 | 5 |
| example_tr_missing_or_short | 7 | 1 | 0 | 0 | 0 | 0 | 8 |
| same_clue_diff_answer | 5 | 7 | 12 | 9 | 2 | 0 | 35 |
| similar_clue | 19 | 16 | 23 | 14 | 8 | 0 | 80 |
| tr_casing_dotless_I | 0 | 1 | 0 | 0 | 0 | 1 | 2 |
| tr_punct_spacing | 0 | 1 | 2 | 1 | 2 | 0 | 6 |

No check fired for: answer_charset, letter_rule_mismatch, answer_not_in_own_example, dup_answer_* (0 duplicate answers within or across levels), clue_too_short/long, example_too_long, tr_whitespace, tr_missing_diacritics, rare_words_low_level.
False positives noted on review: `answer_in_clue` (Turkish gloss after the comma, e.g. "sandalet"), `example_tr_missing_or_short` (valid short translations), `tr_punct_spacing` ("..." in clues), `tr_casing_dotless_I` ("Işık", "Işıklar" are correct).
Real signal: `same_clue_diff_answer` (35) and `similar_clue` (80) -- synonym pairs sharing one clue.
Model review: all 102 flagged cards + 40 seeded (seed 42) per level; findings in SECOND-PASS-<level>.md. No card was edited.
