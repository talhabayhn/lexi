# A2 summary (878 cards)

Batches 1-10 (`content/cards/A2/batch1..10.csv`) + `moved-from-a1.csv` (38 words) + `smoke.csv` (26 cards, kept; `smoke` tag removed, A2 removed from `smokeLevels`).
Per letter: A 43, B 46, C 66, D 35, E 33, F 36, G 29, H 40, I 23, J 21, K 24, L 36, M 37, N 27, O 28, P 54, Q 16, R 30, S 65, T 49, U 25, V 26, W 25, X 20, Y 23, Z 21.
Gap: Q has 16 (target 20); A2-level Q words are exhausted (QUIZ, QUALITY, QUICKLY, QUIETLY, SQUARE, EQUAL, REQUEST, EQUIPMENT, MOSQUE, CHEQUE, REQUIRE, LIQUID, SQUEEZE, ANTIQUE + QUITE, QUEUE; several are B1 exceptions).
Total 878 vs target 900: dropped many B1+ and leak-prone candidates rather than pad.
Owner actions: bump `contentVersion` (not changed here); B1 smoke still contains JOURNEY, QUALITY, NERVOUS (A2 words) and BORROW/TEMPERATURE exist in both A2 and B1 smoke; run `./gradlew :tools:content-compiler:run` locally (validation here used an equivalent script because Gradle could not download its toolchain).
Example-leak fixes touched older A2 examples (moved-from-a1: UNIFORM, VAN, LOUD, QUITE, HOLD, INVITE, VOLLEYBALL, VIEW, IMPORTANT, LARGE, ACTOR, YARD, AMAZING, POOR; smoke: ANCIENT, FOX).
