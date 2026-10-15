# B1 summary (849 cards)

Batches 1-9 (`content/cards/B1/batch1..9.csv`) + `smoke.csv` (21 cards kept; `smoke` tag removed; JOURNEY, QUALITY, NERVOUS removed as A2 words, BORROW and TEMPERATURE removed as A2 duplicates; B1 removed from `smokeLevels`).
Per letter: A 51, B 46, C 60, D 47, E 31, F 35, G 28, H 28, I 37, J 20, K 21, L 31, M 32, N 29, O 23, P 44, Q 19, R 46, S 59, T 29, U 24, V 24, W 24, X 21, Y 19, Z 21.
Gaps (below 20): Q 19 and Y 19; true B1 Q and Y words are exhausted. Q, X, Y, Z, J, K and part of N are filled with CONTAINS cards (several B2 words, e.g. EXPORT, TECHNIQUE, CONQUER, SUFFIX; listed in batches 2, 4, 5, 8). Total 849 vs target ~900: B2/A2 and overlapping-clue candidates were dropped rather than padded.
No B1 answer repeats an A1/A2 answer, or a B2/C1/NAT smoke answer. Validation: Python mirror of the compiler (clue/example leaks, charset, rule, duplicates, blocklist-free); the real Gradle compiler still needs a local run.
Owner actions: bump `contentVersion` (not changed here); run `./gradlew :tools:content-compiler:run`; B1 level is not yet in `shippedLevels`.
Doubtful levels per batch are in `B1-batch-1..9.md`; the main ones: maybe-B2 words (ACCENT, AFFORD, BIOGRAPHY, DEBT, DIVORCE, NERVE, POVERTY, PRIVACY, RESEARCH-type A2/B2 borderline) and maybe-A2 (FALSE, ERROR, HAPPILY, PYJAMAS, TEENAGE).
