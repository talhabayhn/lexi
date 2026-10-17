# Second pass — open questions for the owner

Not changed in the second-pass fix commit (level moves cannot keep the id prefix; the rest are optional or vocabulary choices).

## Level placement (ids encode the level, so moving needs an owner decision)
- A1 -> A2: a1-whale, a1-sausage, a1-armchair, a1-eighth, a1-ninth.
- A2 -> B1: a2-unless, a2-astronaut, a2-yacht, a2-jellyfish (optional: a2-flu, a2-author).
- C1 -> B2 (optional): c1-brutal, c1-benign. NAT -> C1/B2 (optional): nat-sulk, nat-rowdy, nat-lucid.
- b1-skilled vs b2-skillful remain near-synonyms with the same spelling family; clues now differ, but one could be dropped.

## British vocabulary (not spelling) kept as is
Left because the task covered spelling only: a1-lorry, a1-biscuit, a1-fridge, a1-flat, a1-football (soccer), a1-trousers, a2-rubbish, a2-tap, and "petrol", "lift", "queue" inside examples. Done as spelling forms: a1-maths -> MATH, "mum" -> "mom". Decide whether to Americanize vocabulary too (lorry -> truck, petrol -> gas, biscuit -> cookie ...).

## Exam packs
`content/exam-packs/cards/**` was out of scope; see the end of `SPELLING-changes.md` for British spellings still present there.

## Kept intentionally
- Antonym / sibling pairs that share a clue pattern (countries, languages, top/bottom, agree/disagree, online/offline, niece/nephew): each clue names a different fact, no change.
- Scan false positives: answer_in_clue (a2-sandals, b2-statistics), tr_punct_spacing ("..."), tr_casing_dotless_I ("Işık").
- US keeps dialogue/catalogue/disc, so those were not altered.
