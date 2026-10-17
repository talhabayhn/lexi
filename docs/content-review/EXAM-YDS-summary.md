# Exam pack review — YDS

720 cards: 345 pack-only (`content/exam-packs/cards/B2/yds.csv` 324, `cards/C1/yds.csv` 21) + 375 references
(371 base cards, 4 LGS cards: DROUGHT, ENDANGERED, CARBON, VACCINE). Referenced levels: B2 530 · C1 145 · B1 40 ·
A2 4 · NAT 1.

| unit | cards | unit | cards |
|---|---|---|---|
| general-academic | 166 | science | 63 |
| phrasal-verbs | 92 | health | 62 |
| economy | 71 | environment | 47 |
| law-politics | 71 | connectors | 46 |
| social-science | 67 | collocations | 35 |

Cards per letter: A49 B27 C68 D54 E34 F19 G22 H16 I47 J12 K15 L21 M26 N15 O15 P62 Q13 R37 S46 T21 U15 V15 W17
X11 Y34 Z9 (minimum 9). QA: `python3 tools/content-qa/qa.py` → 0 errors, 0 warnings.

## Uncertain items for the product owner (18)
1. **Phrase answers constrain future examples.** New B2/C1 phrase answers (LEAD TO, DEPEND ON, GIVE UP, TURN INTO,
   IN SPITE OF, TAKE PLACE, PAY ATTENTION, …) may no longer appear in any other B2/C1 example (leak rule). Very
   common function phrases (SUCH AS, AS WELL AS, RATHER THAN, IN ORDER TO, EVEN THOUGH, AS LONG AS, THANKS TO,
   AS A RESULT, IN ADDITION) were deliberately left out for that reason. OK, or should packs get their own leak
   scope (compiler change)?
2. GIVE UP, SET UP, FIGURE OUT, PUT OFF, SPEED UP are B1-ish phrasal verbs placed at B2/C1 (they are absent from
   base content because base examples use them).
3. Placed at C1 only because a base B2 example contains them: PESTICIDE, COMPULSORY, COMPENSATION, SET UP,
   COPE WITH, ENGAGE IN, CONTRIBUTE TO, ADAPT TO, DERIVE FROM, WITH REGARD TO, IN CONTRAST, PRIOR TO,
   REGARDLESS OF, RAISE AWARENESS, GAIN ACCESS, ATTRIBUTE TO.
4. Dropped for the 14-letter limit: NOTWITHSTANDING; skipped: CARBON FOOTPRINT, SUPPLY AND DEMAND, TAKE INTO
   ACCOUNT, DRAW A CONCLUSION, CONDUCT RESEARCH, REACH AN AGREEMENT.
5. Collocations are filed under category `idioms` (no `collocations` category exists; pack-only cards never show
   in category wheels, so this only matters for stats).
6. DEPRESSION, DISEASE, CURE, DECADE, IMPACT, CONCEPT, PREVIOUS, THEREFORE, IMPORT, CONSUMER are B1-level words
   that were missing from base content; added at B2 as YDS cards. Consider moving them to base B1 later.
7. Y via CONTAINS is heavy (34 cards: SYMPTOM, THERAPY, OBESITY, …); fine for play, review variety.
8. ATTRIBUTE TO example ("hard to attribute to any single factor") — natural enough?
9. HYPOTHESIZE and URBANIZATION, GLOBALIZATION, SOCIALIZE use US -ize spelling (base uses both styles).
10. BEHAVIOURAL, LABOUR, TUMOUR use UK spelling.
11. LATTER has `pos = noun` (used as "the latter").
12. TREATY example uses "Treaty of Lausanne" (capitalised, fine for the joker).
13. DOMESTIC clue covers two senses ("ülke içi; evle ilgili"); example uses the first.
14. GROSS: only the finance sense ("brüt"); the informal "iğrenç" sense is not mentioned.
15. COUP: the clue says "darbe"; check that this political topic is acceptable for the app.
16. GENDER, SEGREGATION, DISCRIMINATION, ETHNIC, IDEOLOGY: sensitive social topics written neutrally; review tone.
17. Brand/place/person names in examples: Göbeklitepe, Çatalhöyük, Lausanne, Sherlock Holmes, Bursa, Antalya.
18. `free_preview_letters` (ABCDE) is a placeholder; the design shows one "YDS / YÖKDİL" row.
