# C1 summary (664 cards)

Batches 1-10 (`content/cards/C1/batch1..10.csv`) + `smoke.csv` (24 cards kept; `smoke` tag removed; COMPLEXITY removed from smoke and refilled in batch 10; C1 removed from `smokeLevels`).
Per letter: A 47, B 28, C 55, D 49, E 41, F 24, G 16, H 24, I 47, J 12, K 15, L 22, M 24, N 16, O 28, P 40, Q 16, R 32, S 38, T 18, U 14, V 15, W 8, X 11, Y 14, Z 10.
Gaps (below 20): B 28 ok; G 16, J 12, K 15, N 16, Q 16, T 18, U 14, V 15, W 8, X 11, Y 14, Z 10. Total 664 vs the 800-900 target: every batch was reviewed with a strict C1/C2 line and dozens of C2, B2 and derived-form candidates were dropped rather than padded. Q, X, Y, Z, J, K are largely CONTAINS cards (hard-letter exception).
Rules applied: no answer repeats an A1/A2/B1/B2/NAT answer (script-checked before each push; no -ISE/-IZE twin of a lower-level answer except Z hard-letter cards); British spelling for new cards (smoke keeps SCRUTINIZE/RESILIENT); categories limited to those C1 supports.
Doubtful levels per batch are in `C1-batch-1..10.md`. Validation: Python mirror of the compiler (clue/example leaks, charset, rule, duplicates, blocklist); the real Gradle compiler runs in GitHub CI.
Owner actions: bump `contentVersion` (not changed here); C1 is not yet in `shippedLevels`; consider a second reviewer pass on the doubtful lists.
