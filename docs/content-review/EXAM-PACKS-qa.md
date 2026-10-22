# Exam packs — compiler QA numbers (contentVersion 21, 2026-10-22)

Source: `./gradlew :tools:content-compiler:run` (packs from `content/exam-packs` by default) and the compiled
`build/content/content.db`. 0 errors; pack warnings: 12 level-window warnings (below), no coverage warning.

| pack | cards | pack-only | referenced | units | min cards / letter | STARTS / CONTAINS |
|---|---|---|---|---|---|---|
| `lgs` (free, A2-B1, target 600) | 573 | 297 | 276 | 10 | 6 (J) | 522 / 51 |
| `yds-yokdil` (Pro, B2-C1, target 1000, preview ABCDE) | 990 | 563 | 427 | 13 | 15 (Z) | 858 / 132 |

## Cards per level (all members / pack-only)
| pack | A1 | A2 | B1 | B2 | C1 | NAT |
|---|---|---|---|---|---|---|
| `lgs` | 15 / 0 | 354 / 224 | 186 / 73 | 18 / 0 | – | – |
| `yds-yokdil` | – | 5 / 0 | 76 / 21 | 733 / 511 | 175 / 31 | 1 / 0 |

Referenced base cards outside the range (e.g. A1 in LGS) are intended: a word that exists is referenced at its own
level. Of the 21 B1 pack-only cards in `yds-yokdil`, 9 are LGS cards it references (not checked); 12 are its own.

## Cards per letter
- `lgs`: A30 B25 C54 D31 E20 F22 G19 H23 I17 J6 K13 L20 M21 N8 O8 P36 Q7 R29 S70 T34 U9 V22 W18 X16 Y8 Z7
  (X has no STARTS card: the wheel uses a CONTAINS card there, `GAME_RULES.md` §1).
- `yds-yokdil`: A60 B42 C94 D65 E43 F28 G26 H21 I59 J16 K19 L30 M36 N20 O20 P81 Q17 R48 S67 T27 U21 V20 W22 X20
  Y73 Z15.

## Units (cards)
- `lgs`: in-the-kitchen 76, the-internet 70, adventures 63, teen-life 55, tourism 54, science 54, on-the-phone 52,
  natural-forces 51, chores 51, friendship 47.
- `yds-yokdil`: general-academic 166, health 152, phrasal-verbs 92, sciences 76, law-politics 71, economy 71,
  social-science 67, science 63, research-methods 59, environment 47, connectors 46, social-sciences 45,
  collocations 35.

## Level-window warnings (owner review)
`yds-yokdil` pack-only B1 cards below its B2-C1 range (`content/exam-packs/cards/B1/yds-yokdil.csv`): CANCER,
CLINIC, HEAL, HYDROGEN, KIDNEY, LIVER, LUNG, ORGAN, PILL, PULSE, RECOVER, SPINE. They sit at B1 because of the
per-level answer/leak rules; either keep them (warnings stay) or widen the range to B1-C1 in `packs-meta.csv`.
