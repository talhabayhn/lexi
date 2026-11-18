# Category rebalancing — proposal

Status: **proposal; stage 1 and stage 2 applied** (stage 2: 507 topic moves per `docs/content-review/CATEGORY-STAGE2.csv`, the owner kept 21 B2/C1 nature words where they were; new ids `feelings`, `society-law`, `economy`, `clothes-shopping`; pairs below the floor stay hidden: `economy` B2 21, `society-law` B2 16 cards / 11 letters, `food-drink` B2 20; `ideas` arrives with stage 3) (owner decisions: listing minimum is **30 cards and 13 letters** instead of the 40 used below, the compiler errors under 25 cards / 13 letters; Hayvanlar ve doğa ends at B1; A1 stays as is; `food-drink` B2 and `animals` B1 were hidden after stage 1 (animals B1 is listed again since stage 2); the "too broad" warnings for daily-life, school-work and business are expected until stage 3). The data below is the pre-stage-1 snapshot (contentVersion 36),
**4,991 base cards** (A1 865 · A2 940 · B1 1,050 · B2 842 · C1 683 · NAT 611; exam-pack-only cards are not counted, see §4.6).
Counts below are produced by script from the CSVs (reproducible: category column + level folder).
Move list for the recommended option: `docs/content-review/CATEGORY-MOVES-C.csv` (1,937 rows, nothing applied).

Rules used to judge a category × level pair (from `CONTENT.md` §4 and `GAME_RULES.md` §1):
**floor 40 cards**, **≥ 13 letters** (`minCategoryLetters`, below that the engine fills the wheel from the level), and a soft
**ceiling of about 130 cards** so no tile dwarfs the others. Marks in the tables: `▼` below 40 cards, `✖` below 13 letters,
`▲` above 130 cards, `+n` = new cards needed to reach 40.

## 1. Cards per level × category today

| id | TR | EN | A1 | A2 | B1 | B2 | C1 |
|---|---|---|--:|--:|--:|--:|--:|
| `daily-life` | Günlük hayat | Daily life | 288 ▲ | 274 ▲ | 349 ▲ | 240 ▲ | · |
| `food-drink` | Yiyecek ve içecek | Food and drink | 86 | 84 | 42 | 20 ▼ | · |
| `home` | Ev ve eşyalar | Home | 62 | 74 | · | · | · |
| `family-people` | Aile ve insanlar | Family and people | 46 | 47 | 82 | · | · |
| `colors-numbers` | Renkler ve sayılar | Colours and numbers | 54 | · | · | · | · |
| `body-health` | Vücut ve sağlık | Body and health | 38 ▼ | 49 | 72 | 47 | · |
| `school-work` | Okul ve iş | School and work | 72 | 119 | 151 ▲ | 117 | 389 ▲ |
| `animals` | Hayvanlar | Animals | 50 | 47 | 26 ▼ | · | · |
| `city-transport` | Şehir ve ulaşım | City and transport | 80 | 66 | 35 ▼ | · | · |
| `time-weather` | Zaman ve hava | Time and weather | 89 | 43 | · | · | · |
| `travel` | Seyahat | Travel | · | 57 | 54 | 53 | 44 |
| `business` | İş İngilizcesi | Business English | · | · | 125 | 125 | 163 ▲ |
| `idioms` | Deyimler | Idioms | · | · | · | 45 | · |
| `phrasal-verbs` | Phrasal verbs | Phrasal verbs | · | · | · | 121 | · |
| `sports` | Spor | Sports | · | 53 | 29 ▼ | 17 ▼ ✖ | 20 ▼ |
| `music` | Müzik | Music | · | 27 ▼ | 9 ▼ ✖ | 12 ▼ ✖ | 10 ▼ ✖ |
| `film-tv` | Film ve dizi | Film and TV | · | · | 30 ▼ | 22 ▼ | 32 ▼ |
| `technology` | Teknoloji | Technology | · | · | 45 | 22 ▼ | 25 ▼ ✖ |
| `false-friends` | Yalancı eşdeğerler | False friends | · | · | 1 ▼ ✖ | 1 ▼ ✖ | · |

NAT: `general` 611 (hidden, Karışık only). Problems:

| # | Problem | Evidence |
|---|---|---|
| 1 | **Dump categories.** Daily life 288 / 274 / 349 / 240 (A1–B2) and School and work C1 389 (B2 117) are not topics. From B1 up they hold abstract verbs, adjectives, adverbs and nouns (`abandon`, `barely`, `nevertheless`, `hypothesis`, `zenith`). | A1 daily life = 82 verbs, 103 nouns, 62 adj, 18 adv, 23 phrases; C1 school-work = 120 verbs, 115 adj, 148 nouns |
| 2 | **Unplayable thin pairs.** 18 pairs under 40 cards, 7 under 13 letters. Yalancı eşdeğerler B1/B2 = `SENSIBLE` / `SYMPATHETIC` (and the category is listed for C1 with no card). Müzik 9–12 at B1–C1, Spor 17 at B2, Film ve dizi 22 at B2, Yiyecek B2 20, Hayvanlar B1 26. | engine already needs the thin-fill patch for 7 of them |
| 3 | **Category names do not match the content at B2/C1.** Spor B2/C1 = `ELIMINATE`, `SUSTAIN`, `TREMENDOUS`, `COMPLACENT`, `TRAJECTORY`; Müzik B2/C1 = `CONVEY`, `DEVOTE`, `ENHANCE`, `LEGACY`; Yiyecek B2 = `ADEQUATE`, `AUTHENTIC`, `MEDIOCRE`, `REPUTATION`. Only 24 of 51 B2 and 23 of 62 C1 sports/music/film cards are about sports, music or film. | merging them as they are (Option A) copies that problem into the new category |
| 4 | **Family and people B1 is mostly personality words** (`generous`, `jealousy`, `stubborn`, `courage`, 43 of 82). | |
| 5 | **Technology and Business overlap in level, not in topic**; Business itself is 125 / 125 / 163 and mostly abstract management and finance words. | |
| 6 | **A1 is saturated** (`EXPANSION_PLAN.md` §3: no more A1 batches unless the owner reopens it), so A1 deficits cannot be filled casually. | |

## 2. Three options

Ids marked *new* need an illustration (`CategoryArt.forId` returns null for an unknown id and the picker falls back to the
Karışık art, so a new id is safe to ship before its art exists, but it is "needs design" under rule 3).
A card keeps its `id` in every option (progress is keyed by card id).

### Option A — merge only, zero new cards

Spor + Müzik + Film ve dizi → **`hobbies`** (new id, "Hobiler ve eğlence"); Teknoloji → **`business`** (id kept, renamed
"İş ve teknoloji"); `false-friends` dissolved (`SENSIBLE` B1 and `SYMPATHETIC` B2 → daily-life); Yiyecek B2 dropped (10 real
food words → body-health, 10 others → daily-life); Hayvanlar B1 dropped (26 → daily-life); Şehir ve ulaşım B1 dropped (35 → travel).

| id | TR | EN | A1 | A2 | B1 | B2 | C1 |
|---|---|---|--:|--:|--:|--:|--:|
| `daily-life` | Günlük hayat | Daily life | 288 ▲ | 274 ▲ | 376 ▲ | 251 ▲ | · |
| `food-drink` | Yiyecek ve içecek | Food and drink | 86 | 84 | 42 | · | · |
| `home` | Ev ve eşyalar | Home | 62 | 74 | · | · | · |
| `family-people` | Aile ve insanlar | Family and people | 46 | 47 | 82 | · | · |
| `colors-numbers` | Renkler ve sayılar | Colours and numbers | 54 | · | · | · | · |
| `body-health` | Vücut ve sağlık | Body and health | 38 ▼ +2 | 49 | 72 | 57 | · |
| `school-work` | Okul ve iş | School and work | 72 | 119 | 151 ▲ | 117 | 389 ▲ |
| `animals` | Hayvanlar | Animals | 50 | 47 | · | · | · |
| `city-transport` | Şehir ve ulaşım | City and transport | 80 | 66 | · | · | · |
| `time-weather` | Zaman ve hava | Time and weather | 89 | 43 | · | · | · |
| `hobbies` | Hobiler ve eğlence | Hobbies and fun | · | 80 | 68 | 51 | 62 |
| `travel` | Seyahat | Travel | · | 57 | 89 | 53 | 44 |
| `business` | İş ve teknoloji | Work and technology | · | · | 170 ▲ | 147 ▲ | 188 ▲ |
| `idioms` | Deyimler | Idioms | · | · | · | 45 | · |
| `phrasal-verbs` | Phrasal verbs | Phrasal verbs | · | · | · | 121 | · |

- Moved cards: **436**. New cards: **0** (A1 Vücut ve sağlık stays 2 short of 40; accepted).
- Illustrations: 19 → **15** (retire Spor, Müzik, Film ve dizi, Teknoloji, Yalancı eşdeğerler; new: Hobiler, may reuse the Müzik art).
- Fixes every thin pair and every ✖. Does **not** touch the owner's main complaint: Daily life is now 288 / 274 / **376** / **251**,
  School and work C1 389, İş ve teknoloji 170 / 147 / 188. Biggest : smallest tile per level is 7–9×.
- Hobiler B2/C1 would be 53 % / 63 % words that have nothing to do with hobbies (see problem 3).

### Option B — merge + targeted filling of what stays thin

Option A, except the two merges that lose a useful topic are kept and filled instead:
**Hayvanlar → "Hayvanlar ve doğa"** at B1 (the 16 nature words that sit in Daily life B1 move in: `blizzard`, `climate`,
`environment`, `pollution`, `tulip`, `greenhouse` … → 42 cards, no new cards) and **Şehir ve ulaşım B1** stays (35 + 5 new cards).

| id | TR | EN | A1 | A2 | B1 | B2 | C1 |
|---|---|---|--:|--:|--:|--:|--:|
| `daily-life` | Günlük hayat | Daily life | 288 ▲ | 274 ▲ | 334 ▲ | 251 ▲ | · |
| `food-drink` | Yiyecek ve içecek | Food and drink | 86 | 84 | 42 | · | · |
| `home` | Ev ve eşyalar | Home | 62 | 74 | · | · | · |
| `family-people` | Aile ve insanlar | Family and people | 46 | 47 | 82 | · | · |
| `colors-numbers` | Renkler ve sayılar | Colours and numbers | 54 | · | · | · | · |
| `body-health` | Vücut ve sağlık | Body and health | 38 ▼ +2 | 49 | 72 | 57 | · |
| `school-work` | Okul ve iş | School and work | 72 | 119 | 151 ▲ | 117 | 389 ▲ |
| `animals` | Hayvanlar ve doğa | Animals and nature | 50 | 47 | 42 | · | · |
| `city-transport` | Şehir ve ulaşım | City and transport | 80 | 66 | 35 ▼ +5 | · | · |
| `time-weather` | Zaman ve hava | Time and weather | 89 | 43 | · | · | · |
| `hobbies` | Hobiler ve eğlence | Hobbies and fun | · | 80 | 68 | 51 | 62 |
| `travel` | Seyahat | Travel | · | 57 | 54 | 53 | 44 |
| `business` | İş ve teknoloji | Work and technology | · | · | 170 ▲ | 147 ▲ | 188 ▲ |
| `idioms` | Deyimler | Idioms | · | · | · | 45 | · |
| `phrasal-verbs` | Phrasal verbs | Phrasal verbs | · | · | · | 121 | · |

- Moved cards: **391**. New cards: **7** (city B1 5, body A1 2; the A1 two only if the owner reopens A1).
- Illustrations: 19 → **15**, same as A.
- Same weakness as A: the four dump categories stay (334 / 251 / 389 / 188 cards). B is "A, but nothing shrinks that does not have to".

### Option C — B + split the dump categories into real topics

Everything in B (Hayvanlar ve doğa now A1–C1), plus the dump categories are split in two steps, always by **topic first, word class second**:

1. **Topic lexicons** (by answer word, current category and level): `clothes-shopping` (A1–A2), `hobbies` (A1–C1, also
   rescues hobby words from daily life), `feelings` (B1–C1, includes the 43 personality words of Family and people B1),
   `society-law` (B1–C1), `economy` (B1–C1, split out of Business), `animals` (nature words at B1–C1).
2. **Word class for what is left** (no topic): verbs → `verbs`, adjectives and adverbs → `adjectives`; leftover nouns →
   `daily-life` at A1–B1 and **`ideas`** at B2–C1. School and work then only exists at A1–B1.
   Sports/music/film cards at B2/C1 that are not about sport, music or film follow the same rule instead of staying in Hobiler.

| id | TR | EN | A1 | A2 | B1 | B2 | C1 |
|---|---|---|--:|--:|--:|--:|--:|
| `daily-life` | Günlük hayat | Daily life | 70 | 95 | 119 | · | · |
| `food-drink` | Yiyecek ve içecek | Food and drink | 86 | 84 | 42 | · | · |
| `home` | Ev ve eşyalar | Home | 62 | 74 | · | · | · |
| `family-people` | Aile ve insanlar | Family and people | 46 | 47 | 33 ▼ +7 | · | · |
| `colors-numbers` | Renkler ve sayılar | Colours and numbers | 54 | · | · | · | · |
| `body-health` | Vücut ve sağlık | Body and health | 38 ▼ +2 | 49 | 72 | 57 | · |
| `school-work` | Okul ve iş | School and work | 72 | 119 | 133 ▲ | · | · |
| `animals` | Hayvanlar ve doğa | Animals and nature | 50 | 47 | 42 | 16 ▼ ✖ +24 | 10 ▼ ✖ +30 |
| `city-transport` | Şehir ve ulaşım | City and transport | 80 | 66 | 35 ▼ +5 | · | · |
| `time-weather` | Zaman ve hava | Time and weather | 89 | 43 | · | · | · |
| `clothes-shopping` | Giysi ve alışveriş | Clothes and shopping | 35 ▼ +5 | 43 | · | · | · |
| `hobbies` | Hobiler ve eğlence | Hobbies and fun | 34 ▼ +6 | 99 | 72 | 29 ▼ +11 | 23 ▼ +17 |
| `verbs` | Fiiller | Verbs | 71 | 52 | 64 | 101 | 102 |
| `adjectives` | Sıfatlar ve zarflar | Adjectives and adverbs | 78 | 65 | 77 | 114 | 100 |
| `travel` | Seyahat | Travel | · | 57 | 54 | 53 | 44 |
| `business` | İş ve teknoloji | Work and technology | · | · | 121 | 123 | 139 ▲ |
| `economy` | Para ve ekonomi | Money and economy | · | · | 49 | 24 ▼ +16 | 41 |
| `feelings` | Duygu ve karakter | Feelings and character | · | · | 99 | 41 | 56 |
| `society-law` | Toplum ve hukuk | Society and law | · | · | 38 ▼ +2 | 21 ▼ ✖ +19 | 61 |
| `ideas` | Fikirler ve kavramlar | Ideas and concepts | · | · | · | 97 | 107 |
| `idioms` | Deyimler | Idioms | · | · | · | 45 | · |
| `phrasal-verbs` | Phrasal verbs | Phrasal verbs | · | · | · | 121 | · |

- Moved cards: **1,937** (39 %): 297 merges, 612 topic moves, 1,028 word-class moves (434 adjective/adverb, 390 verb, 204 noun).
- Pairs: 66 (today 56). Categories per level: A1 14, A2 14, B1 15, B2 13, C1 10 (today 10 / 12 / 14 / 13 / 7). Largest : smallest tile
  per level: A1 89 : 40, A2 119 : 43, B1 133 : 40, B2 123 : 40, C1 139 : 40.
- **Illustrations: 19 → 22**: new `hobbies`, `clothes-shopping`, `feelings`, `society-law`, `economy`, `verbs`, `adjectives`, `ideas`
  (8; Hobiler may reuse Müzik/Spor shapes); retired Spor, Müzik, Film ve dizi, Teknoloji, Yalancı eşdeğerler (5);
  Hayvanlar ve doğa needs a leaf added to the animal art. Longest new name "Fikirler ve kavramlar" (21 chars): check the tile wraps.
- **New cards to reach 40: 144**, all in pairs that would otherwise stay thin:

| pair | now | new | why / letter need |
|---|--:|--:|---|
| Hayvanlar ve doğa B2 | 16 (10 letters) | 24 | environment, wildlife, climate vocabulary; at least 3 new letters |
| Hayvanlar ve doğa C1 | 10 (7 letters) | 30 | same; at least 6 new letters |
| Toplum ve hukuk B2 | 21 (12 letters) | 19 | law/politics B2; at least 1 new letter |
| Para ve ekonomi B2 | 24 | 16 | money/trade B2 |
| Hobiler ve eğlence B2 / C1 | 29 / 23 | 11 / 17 | real sport / music / film words at the level (B2 and C1 equivalents of `referee`, `lyrics`, `cinematography`) |
| Aile ve insanlar B1 | 33 | 7 | relatives, life stages |
| Şehir ve ulaşım B1 | 35 | 5 | |
| Toplum ve hukuk B1 | 38 | 2 | |
| **A1** Hobiler / Giysi ve alışveriş / Vücut ve sağlık | 34 / 35 / 38 | 6 / 5 / 2 | only if the owner reopens A1 (13 cards); otherwise accept 34–38 at A1 |

  Without the two Hayvanlar ve doğa B2/C1 rows (cards fall back to `ideas`) the total is **90**. Cards written for these gaps also count
  towards the per-letter targets in `EXPANSION_PLAN.md` §3 (choose thin letters), so they are not extra work on top of it.
- Dropped on purpose: Giysi ve alışveriş B1 (22 cards, B1 clothing vocabulary is too small to fill; its words go through the word-class rule) and Hayvanlar ve doğa as a separate "Doğa"
  category (nature has 16 / 10 cards at B2 / C1 today; it rides on Hayvanlar ve doğa instead).

### Comparison

| | today | A | B | C |
|---|--:|--:|--:|--:|
| Pairs (category × level A1–C1) | 56 | 41 | 43 | 66 |
| Moved cards | 0 | 436 | 391 | 1,937 |
| Pairs below 40 cards (before filling) | 18 | 1 | 2 | 12 |
| Pairs below 13 letters | 7 | 0 | 0 | 3 (all filled by new cards) |
| Pairs above 130 cards | 7 | 9 | 9 | 2 |
| New cards for floor 40 | 344 | 0 (2 short) | 7 | 144 (90 without Hayvanlar B2/C1) |
| Illustrations (new / retired / total) | 19 | 1 / 5 / 15 | 1 / 5 / 15 | 8 / 5 / 22 |
| Largest : smallest tile at A1 / B1 / C1 | 7.6 / 349 / 38.9 | 7.2 / 9.0 / 8.8 | 7.2 / 8.4 / 8.8 | 2.2 / 3.3 / 3.5 |

## 3. Recommendation: Option C, in three stages

**Why C.** A and B repair the thin categories but leave the owner's second complaint (Daily life 240–349) untouched, and A makes
Hobiler a category where up to 63 % of the words are not hobbies. C is the only option where every tile at every level is between
40 and about 140 cards and where the name matches what the player sees. Its cost is moves and art, not writing: 144 new cards
(90 if Hayvanlar ve doğa stays at A1–B1), because the cards already exist, they were just in the wrong drawer.
Moves are safe (see §4.4); new cards and art are the slow part.

**Three owner decisions before starting** (they change the plan, so they are the owner's):
1. Are word-class categories (**Fiiller**, **Sıfatlar ve zarflar**) acceptable as real categories? They hold 1,028 of the moves and keep the
   remaining dump small. Without them Daily life stays at 120–250 cards per level (≈ Option B). They are the only non-topic categories.
2. Keep Hayvanlar ve doğa at B2/C1 (54 new cards) or end it at B1 (cards go to Fikirler ve kavramlar)?
3. Reopen A1 for 13 cards, or accept A1 pairs at 34–38?

**Order of work** (each stage is one commit, one `contentVersion` bump, CI green before the next):
1. **Stage 1: merges** (297 rows: reasons `merge …`, `technology joins …`, `B2 food word …`): new `categories.csv`, `hobbies`, `business`
   renamed, `false-friends` / `sports` / `music` / `film-tv` / `technology` removed, `ShippedCategoryWheelTest` thin list updated
   (it names false-friends and music), art for `hobbies`. This stage alone equals Option A's merge part.
2. **Stage 2: topic moves** (612 rows): feelings, society-law, economy, clothes-shopping, hobbies, nature; art for the 5 new ids.
3. **Stage 3: word-class split** (1,028 rows): verbs, adjectives, ideas; `school-work` ends at B1, `daily-life` at B1; art for 3 ids.
4. **Stage 4: new cards** (144, or 90) in batches ≤ 100 per level (B2 70, C1 47 / 17, B1 14, A1 13 if reopened), with the usual clue and
   leak rules; update the batch table in `EXPANSION_PLAN.md` (it names `technology`, `daily-life`, `business` as batch categories).
   Until stage 4 lands the thin pairs are hidden by dropping the level from `levels` (see §4.5), so nothing ships thin.

**Review process.**
- The classification is done by answer word, part of speech and current category, **not by reading each clue**, so homographs
  (`match`, `suit`, `light`, `play`) can be wrong. Every moved card is in the CSV with its reason, sorted by level and proposed category.
- Stage 1–2 sheets: the owner skims them on the phone and answers with a list of ids to veto (default: approved). Stage 3 is a rule
  ("remaining verbs → Fiiller, remaining adjectives and adverbs → Sıfatlar ve zarflar, remaining nouns → Günlük hayat / Fikirler ve kavramlar"),
  so the owner approves the rule plus a spot check of about 50 rows instead of 1,028.
- Stage 4: same flow as the clue batches, `docs/content-review/CATEGORY-fill-<level>.md` (like the existing `A1-category-fill.md`).
- Vetoes are applied by me; the compiler (`./gradlew :tools:content-compiler:run`) re-checks floors and letters after each stage.

## 4. Impact and implementation notes

### 4.1 `categories.csv` for Option C (levels as they will be after stage 4)
```csv
id,name_tr,name_en,levels,sort,is_pro
mixed,Karışık,Mixed,A1|A2|B1|B2|C1|NAT,0,0
daily-life,Günlük hayat,Daily life,A1|A2|B1,1,0
food-drink,Yiyecek ve içecek,Food and drink,A1|A2|B1,2,0
home,Ev ve eşyalar,Home,A1|A2,3,0
family-people,Aile ve insanlar,Family and people,A1|A2|B1,4,0
colors-numbers,Renkler ve sayılar,Colours and numbers,A1,5,0
body-health,Vücut ve sağlık,Body and health,A1|A2|B1|B2,6,0
school-work,Okul ve iş,School and work,A1|A2|B1,7,0
animals,Hayvanlar ve doğa,Animals and nature,A1|A2|B1|B2|C1,8,0
city-transport,Şehir ve ulaşım,City and transport,A1|A2|B1,9,0
time-weather,Zaman ve hava,Time and weather,A1|A2,10,0
clothes-shopping,Giysi ve alışveriş,Clothes and shopping,A1|A2,11,0
hobbies,Hobiler ve eğlence,Hobbies and fun,A1|A2|B1|B2|C1,12,0
verbs,Fiiller,Verbs,A1|A2|B1|B2|C1,13,0
adjectives,Sıfatlar ve zarflar,Adjectives and adverbs,A1|A2|B1|B2|C1,14,0
travel,Seyahat,Travel,A2|B1|B2|C1,15,0
business,İş ve teknoloji,Work and technology,B1|B2|C1,16,0
economy,Para ve ekonomi,Money and economy,B1|B2|C1,17,0
feelings,Duygu ve karakter,Feelings and character,B1|B2|C1,18,0
society-law,Toplum ve hukuk,Society and law,B1|B2|C1,19,0
ideas,Fikirler ve kavramlar,Ideas and concepts,B2|C1,20,0
idioms,Deyimler,Idioms,B2,21,0
phrasal-verbs,Phrasal verbs,Phrasal verbs,B2,22,0
general,Genel,General,NAT,99,0
```
`sports`, `music`, `film-tv`, `technology`, `false-friends` are removed; `business` keeps its id and gets new names. Ids are never reused
for a different meaning. `levels` is the only place that decides which tile the picker shows.

### 4.2 Card CSVs
Only the `category` column of 1,937 base rows changes (ids, answers, levels, clues and examples are untouched). New cards later get
`category` from the new set. Files stay where they are (file names do not matter to the compiler).

### 4.3 Picker (`docs/design/video-flow-notes.md` §2)
The grid already reads `categories.csv` per level in `sort` order and never hides a category, so no picker code changes.
Tiles per level (+ Karışık): A1 15, A2 15, B1 16, B2 14, C1 11, NAT 1. The golden screenshots of `feature/levels` change (re-record on CI),
`CategoryArt.forId` gets 8 new ids, the notes' "19 category illustrations" becomes 22, `CONTENT.md` §4 and its category table are rewritten,
and `ShippedCategoryWheelTest` gets a new thin list (or none).

### 4.4 Saved data and stats
- **Progress**: keyed by card id; `Stats.categoryProgress` is computed at read time from the card to category map of the level, so a moved
  card keeps its learned state and simply counts toward its new category. Visible side effect: a category's "learned / total" jumps
  (the total shrinks while learned cards are redistributed) and every new tile starts with whatever the player already knew.
- **Stored play category** (`play_category`): a removed or level-less id reads as Karışık (already specified, `video-flow-notes.md` §2).
- **Past sessions** keep `session.category` text with removed ids; there is no FK and nothing joins it to the category table
  (only the history export, `UserRepositories.kt`), so old rows stay valid and are just history.
- **SRS, notebook, daily goal, streaks, reminders**: keyed by card id, unaffected. **Karışık** plays all categories of a level, unaffected.
- Installed apps refresh the DB when `contentVersion` changes (bump it per stage).
- Seeds: a category wheel of a moved category is a different card pool, so saved *unfinished* category games of those pairs would
  not replay identically; a finished game is stored with its result, not its pool. Check how an in-progress saved game is restored before stage 2
  (I did not read that code).

### 4.5 Rule for the minimum category size shown in the picker
Keep the picker as is (no runtime hiding) and enforce the size **in the content**, because `levels` already controls what is shown:
- a category is listed for a level only if it has **≥ 40 cards and ≥ 13 letters** there;
- the compiler **errors** for a listed pair under 25 cards or under 13 letters (this would have caught Yalancı eşdeğerler C1 with no card), **warns**
  under 40 cards and over 140 cards (too broad), and **warns** for a level that has cards of a category but does not list it (already exists);
- the engine's thin fill (`minCategoryLetters = 13`) stays as a safety net, not as a design tool.

### 4.6 Exam packs
`units/<pack>.csv` reference card ids only, so LGS and YDS/YÖKDİL content, counts and units are unaffected. Pack-only cards
(`content/exam-packs/cards/**`, 851 rows) also carry a `category`, but they are `pack_only = 1` and never in a category wheel; the compiler only
warns when their category is not listed for the level. Under Option C **394** of them (School and work B2 272 / C1 16, Teknoloji 65,
Spor 24, Müzik 5, plus 12 already unlisted idioms / phrasal-verbs C1) should be re-pointed mechanically with the same rule as base cards
(no owner review needed); under A / B 108 / 106.

### 4.7 Not done here
No card, `categories.csv`, code, golden or doc other than this file and the moves CSV was changed. Store declarations: no SDK, paywall or account
change, so rule 11 does not apply.

## 5. Moves sheet

`docs/content-review/CATEGORY-MOVES-C.csv`: `id, answer, level, current_category, proposed_category, reason`, 1,937 rows (NAT untouched),
sorted by level, then proposed category, then answer. Reasons: `adjective / adverb: no topic` 434, `verb: no topic` 390,
`merge Spor+Müzik+Film ve dizi` 195, `emotion / character word` 196, `abstract noun: no topic` 204, `law / politics / society word` 120,
`money / trade / finance word` 114, `technology joins İş ve teknoloji` 92, `clothes / shopping word` 78, `hobby / entertainment word` 62,
`nature word -> Hayvanlar ve doğa` 42, `B2 food word folds into health / nutrition` 10.
Known soft spots: about 10 off-topic words stay in Hobiler B1 (`complex`, `suitable`, `violence`, `united` …); `daily-life` A1 keeps its 23 phrases (`about`, `because`, `of course`) by design; B2 `boutique`, `linen`, `nightgown` land in Fikirler ve kavramlar (clothing is only a category at A1–A2).
