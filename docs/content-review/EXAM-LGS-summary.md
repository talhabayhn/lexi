# Exam pack review — LGS

573 cards: 297 pack-only (`content/exam-packs/cards/A2/lgs.csv` 222, `cards/B1/lgs.csv` 75) + 276 references to
base cards. Units follow the MEB 8th-grade themes (slugs in `units/lgs.csv`):

| unit | cards | unit | cards |
|---|---|---|---|
| friendship | 47 | tourism | 54 |
| teen-life | 55 | chores | 51 |
| in-the-kitchen | 76 | science | 54 |
| on-the-phone | 52 | natural-forces | 50 |
| the-internet | 70 | adventures | 63 |

Cards per letter: A30 B25 C54 D31 E20 F22 G19 H23 I17 J6 K13 L20 M21 N8 O8 P36 Q7 R29 S70 T34 U9 V22 W18 X16 Y8 Z7
(minimum 5 everywhere). QA: `python3 tools/content-qa/qa.py` → 0 errors, 0 warnings.

## Uncertain items for the product owner (24)
**Level / placement**
1. B2 base cards referenced for LGS (their clues are B2-style): COUNT ON, GET ALONG, KEEN, NUTRIENT, POSTPONE,
   HOLD ON, GET THROUGH, CUT OFF, BOOKMARK, ADVENTUROUS, MEMORABLE, LANDMARK, CURRENCY, OBSERVE, GRAVITY, COLLAPSE,
   PANIC, EXTINCT. Keep (they are in the MEB unit word lists) or drop?
2. C1 references removed (ITINERARY, LANDSLIDE, MAGNITUDE, DEBRIS) — too hard for 8th grade.
3. `b1-password`: B1 only because base `a2-user`'s example contains "password".
4. Multi-word answers are B1 because their words are A2 answers (HANG UP, PICK UP, MOBILE PHONE, TIDY UP, …);
   for LGS players this is invisible, but these cards count as B1 in stats.

**Missing / dropped words**
5. TEAM is missing everywhere: base examples on every level contain "team", so it cannot be added without
   rewriting those examples. Suggest adding `a1-team` and rewriting the ~6 A1 examples in a base batch.
6. Dropped for the 14-letter limit: TAKE OUT THE RUBBISH, NATURAL DISASTER. EXTREME SPORTS dropped (EXTREME is
   a B1 answer).

**Wording / regional**
7. TRASH (American) next to RUBBISH (British, base) — keep both? Clue says "Amerikan İngilizcesinde".
8. FIRE BRIGADE (UK) vs fire department; PENFRIEND (UK) vs pen pal.
9. X letter via CONTAINS on pack cards: MIXER, EXTENSION, TEXTING.
10. Brand/place names in examples: Türksat, Ölüdeniz, Köprüçay, Saklıkent, Alaçatı, Uludağ, Avanos, Göreme,
    Lycian Way, Popeye ("Temel Reis"), Thomas Edison, Aziz Sancar, Darwin.
11. PARDON has `pos = noun` (interjection is not in the pos list).
12. CALORIE example uses "calorie count" (singular needed for the Example joker).
13. ZIP LINE clue/translation uses the loanword "zipline".
14. SITCOM, THRILLER, KARAOKE, KNITTING, HASHTAG, VIRAL: themed but slightly above A2.
15. DATA, HACKER, CYBERBULLYING: A2 placement for the internet unit; CEFR lists them higher.
16. ENGAGED (phone line) is British; younger learners may know "busy" only.
17. JUNK FOOD vs base FAST FOOD (if added later) need distinct clues.
18. SOCIABLE vs base OUTGOING: clues differ ("başkalarıyla vakit geçirmekten hoşlanan" vs base clue) — check.
19. MESSY vs base UNTIDY, NEAT vs base TIDY: near-synonym pairs on different letters; clues checked, review.
20. SCARED vs base AFRAID: different letters, review clue overlap.
21. FRIGHTENING / TERRIFYING / SCARY: three near-synonyms in one unit (different letters).
22. LEFTOVERS is plural-only; example uses the plural.
23. VEGAN noun vs adjective usage.
24. `free_preview_letters` (ABCDE) and `is_pro = 1` for LGS are placeholders (see `docs/content/EXAM_PACKS.md`).
