# A2 batch 1 review (103 cards)

Hard-letter batch: Q X Z J K V Y (plus words that serve them via CONTAINS). File: `content/cards/A2/batch1.csv`.
Validation: Gradle/content-compiler could not run in the cloud sandbox (JDK toolchain + Maven downloads blocked), so an equivalent
checker (schema, leaks, rule/letter, A1 duplicates) was used; run `./gradlew :tools:content-compiler:run` locally to confirm.
Existing A2 smoke and moved-from-A1 cards are kept unchanged.

Open points for the owner:
- `content/meta.json` `contentVersion` was not bumped (task scope); bump it when A2 ships.
- B1 smoke already has JOURNEY, QUALITY, NERVOUS (A2 words, kept here). Replace them in B1 later. KNOWLEDGE, YOUTH, ZONE, EXPERIENCE were dropped from A2 (B1).
- Q stays thin (12): QUIZ, QUALITY, QUICKLY, QUIETLY, SQUARE, EQUAL, REQUEST, EQUIPMENT, MOSQUE, CHEQUE + QUITE, QUEUE. QUARREL/UNIQUE dropped as too hard.

Doubtful level (hard-letter exceptions, B1/B2-ish): REQUEST, CHEQUE, EXCEPT, EXPERT, EXPLORE, EXCHANGE, JEALOUS, JUDGE, JAIL, JOY, OBJECT,
VOLCANO, VEHICLE, VOTE, VISA, VASE, REALIZE, KNOT, BREEZE, SNEEZE, DIZZY, KILL, STYLE, PLENTY, ROYAL.
Judgement calls: GUY clue "günlük dilde adam, tip"; ANYONE/ANYTHING/YOURSELF use pos `phrase`; ZIPPER dropped (US spelling, near ZIP).
