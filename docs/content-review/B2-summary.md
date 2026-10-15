# B2 summary (729 cards)

Batches 1-10 (`content/cards/B2/batch1..10.csv`) + `smoke.csv` (26 cards kept; `smoke` tag removed; B2 removed from `smokeLevels`) + `idioms.csv` (NO BRAINER). Mix: regular vocabulary, phrasal verbs (`phrasal-verbs`) and idioms (`idioms`).
Per letter: A 43, B 31, C 62, D 34, E 27, F 34, G 27, H 23, I 38, J 18, K 23, L 19, M 33, N 21, O 29, P 35, Q 16, R 30, S 40, T 26, U 20, V 28, W 20, X 14, Y 16, Z 22.
Gaps (below 20): J 18, L 19, Q 16, X 14, Y 16. True B2 words for these letters are scarce and many cards on J, K, Q, X, Y, Z are CONTAINS cards (hard-letter exception). Total 729 vs the 1400 plan in `docs/CONTENT.md` §5 (and ~900 session target): A2/B1/C1 words and overlapping clues were dropped rather than padded.
Rules applied: no answer repeats an A1/A2/B1 answer or a C1/NAT smoke answer; no -ISE/-IZE twin of an existing answer; phrasal verbs only if clearly above B1 (GET UP, FIND OUT, GROW UP... are A2); examples use the exact answer form; categories limited to those B2 supports.
Doubtful level per batch is listed in `B2-batch-1..10.md` (main ones: maybe-C1 CITE, CORRUPT, REVENUE, RIGID, SCARCE, SCOPE, INFER, LEGACY, EXPERTISE, STABILIZE; maybe-B1 TAKE UP, USE UP).
Validation: Python mirror of the compiler (the Gradle compiler can't run in the cloud); the real compiler runs in GitHub CI.
