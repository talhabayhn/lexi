# Exam pack review — YÖKDİL

> Merged (owner decision 2026-10): YÖKDİL is now part of the single pack `yds-yokdil` (990 cards after
> de-duplication, ≥ 15 per letter). Counts below are the pre-merge source counts; the uncertain items still apply.

500 cards: 209 own pack-only cards (`content/exam-packs/cards/B1/yokdil.csv` 12, `B2/yokdil.csv` 187,
`C1/yokdil.csv` 10) + 114 references to YDS/LGS pack cards + 177 references to base cards. Referenced/own levels:
B2 369 · C1 85 · B1 45 · A2 1.

| unit | cards |
|---|---|
| health (sağlık bilimleri) | 159 |
| sciences (fen bilimleri) | 123 |
| research-methods (akademik metin ve yöntem) | 117 |
| social-sciences (sosyal bilimler) | 101 |

Cards per letter: A24 B21 C38 D28 E19 F13 G10 H13 I27 J11 K13 L13 M21 N12 O10 P34 Q12 R17 S35 T13 U12 V11 W12 X12
Y57 Z12 (minimum 10). QA: `python3 tools/content-qa/qa.py` → 0 errors, 0 warnings.

## Uncertain items for the product owner (14)
1. YÖKDİL shares ~114 cards with YDS (same card id in both `units/` files). A player who owns both packs sees the
   same clue twice; fine, or should YÖKDİL get its own clues (needs different answers per level)?
2. Basic organ/body words missing from base content were added as B1 YÖKDİL cards: CANCER, CLINIC, ORGAN, KIDNEY,
   LIVER, LUNG, SPINE, PILL, PULSE, HEAL, RECOVER, HYDROGEN. They belong in base A2/B1 eventually (then this pack
   should reference those ids instead).
3. C1 only because base/YDS B2 examples contain them: DIGEST, ASTHMA, INSULIN, BLOOD PRESSURE, SOLAR, BACTERIA,
   CONTROL GROUP, CLINICAL TRIAL, PEER REVIEW, SAMPLE SIZE. HYDROGEN is B1 because a base C1 example uses it.
4. Too long for the 14-letter limit and skipped: ARTIFICIAL INTELLIGENCE, CIVIL ENGINEERING, LITERATURE REVIEW,
   CARDIOVASCULAR is 14 and was not used; MICROORGANISM was not used.
5. Y via CONTAINS is very frequent (57 cards: SYMPTOM, THERAPY, PHYSIOLOGY, …). The Y slot of a wheel will rarely
   start with Y; acceptable?
6. X RAY is written without the hyphen (answer charset), clue says "röntgen".
7. Medical topics that some players may find heavy: CHEMOTHERAPY, ANOREXIA, DEMENTIA, PARALYSIS, OVERDOSE,
   SEIZURE. Written neutrally; keep?
8. Sensitive social topics: XENOPHOBIA, FEMINISM, NATIONALISM, PATRIARCHAL, SECULAR, PROPAGANDA, SUFFRAGE,
   DIASPORA. Examples are factual (e.g. "Türkiye is a secular republic", "Turkish women gained suffrage in 1934").
9. Category mapping: sciences → `technology`, health → `body-health` (C1 health cards → `school-work`, because
   `body-health` is not listed for C1), social/research → `school-work`.
10. US spelling: STERILIZE, IMMUNIZATION, WESTERNIZATION, PEDIATRIC; UK spelling: SULPHUR, TUMOUR (YDS).
11. ETHICS takes a plural verb in its example ("Medical ethics do not allow"); both are used.
12. FINDINGS and HUMANITIES are plural-only answers.
13. Person/place names: Farabi, Thomas More, Pamukkale, Manchester, Iceland, Rome, Parkinson.
14. `free_preview_letters` (ABCDE) is a placeholder; the design shows YDS and YÖKDİL as one row.
