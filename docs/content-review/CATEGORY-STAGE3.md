# Category rebalance — stage 3 proposal (word-class split)

Status: **proposal only, nothing applied.** Based on `main` at contentVersion 38 (stages 1 and 2 applied). Full list: `CATEGORY-STAGE3.csv` (id, answer, level, pos, current_category, proposed_category, reason).

## How to answer

`kural onay` (approve the rule as written), or `kural onay, hariç: <id> → <category|keep>, ...`, plus any rule change in words (for example "R4 kapalı", "zarflar ayrı kalsın"). Without an answer nothing happens.

## 0. Facts first

- **Categories listed per level today** (`content/categories.csv`, Karışık not counted): **A1 12, A2 12, B1 13, B2 9, C1 7**. The stage 2 report said "A1 10"; that was a counting mistake of mine in the report text, the file lists 12 at A1 (daily-life, food-drink, home, family-people, colors-numbers, body-health, school-work, animals, city-transport, time-weather, clothes-shopping, hobbies).
- `pos` values in the data: `noun`, `verb`, `adj`, `adv`, `phrase`, `idiom`, `phrasal` only. **There is no `prep`, `conj`, `det`, `pron` or `interj`**: function words and greetings are coded `phrase` (A1: 24, A2: 23, B1: 9, B2: 2; none at C1) and sit mostly in `daily-life`; `idiom` (45 B2, 3 NAT) and `phrasal` (121 B2, 1 NAT) are the contents of `idioms` / `phrasal-verbs`.
- 1429 cards are in scope of the rule; **1137 change category** (adjectives 502, verbs 407, ideas 228); 292 stay (A1–B1 nouns and phrases that are already in daily-life).

## 1. The rule

| pos | A1–B1 | B2–C1 |
|---|---|---|
| `verb` | `verbs` | `verbs` |
| `adj` | `adjectives` | `adjectives` |
| `adv` | `adjectives` | `adjectives` |
| `noun` | stays in `daily-life` | `ideas` |
| `phrase` (greetings, prepositions, conjunctions, question words, pronouns) | stays in `daily-life` (prepositions that sit in a topic category such as `after`, `above`, `across` stay there) | `adjectives` (only 2 cards: `whereas`, and `up-to-date` which is not in scope) |
| `idiom`, `phrasal` | not touched, they are the content of `idioms` / `phrasal-verbs` (B2) | same |

Why these homes for the odd pos values: the data has no separate part-of-speech for function words. At A1–B1 they are the first things a learner meets, so `daily-life` ("Günlük hayat") stays their home; the 23 + 12 + 9 cards also give `daily-life` its letters. At B2 there is only `whereas`; `Sıfatlar ve zarflar` is the closest word-class bucket (open question 1). Idioms and phrasal verbs are whole categories already; their cards must never be pooled into the word-class lists.

### Which source categories the rule applies to

| rule | source | cards | note |
|---|---|--:|---|
| R1 | `daily-life` A1–B2 (all) | 903 | nouns at A1–B1 stay, B2 nouns go to `ideas` |
| R2 | `school-work` B2 and C1 (all) | 414 | `school-work` then lists A1–B1 only; A1–B1 cards are not touched |
| R3 | `hobbies` B2/C1 cards the move sheet called word-class (adjective, verb, abstract noun: no topic) | 66 | the sport/music/film nouns stay in `hobbies` |
| R4 *(optional)* | `business` C1 adjectives and adverbs | 36 | makes `business` C1 117 instead of 153; see counts below |
| R5 | `food-drink` B2 generic words (adequate, mediocre, reputation, preference ...) | 10 | the 10 real food words stay in `food-drink` (hidden) |
The 21 nature words the owner kept in stage 2 are inside R1/R2 (they sit in `daily-life` B2 and `school-work` C1): nouns such as `meadow`, `orchid`, `ecosystem` go to `ideas`, verbs (`breed`, `contaminate`, `deplete`, `erode`) to `verbs`, adjectives (`fertile`, `nocturnal`, `extinct`, `hazy`) to `adjectives`.

**Never touched:** `food-drink` (A1–B1), `home`, `body-health`, `family-people`, `travel`, `city-transport`, `colors-numbers`, `time-weather`, `animals`, `clothes-shopping`, `hobbies` A1–B1, `feelings`, `society-law`, `economy`, `idioms`, `phrasal-verbs`, `business` B1–B2, NAT `general`, and all `school-work` A1–B1.

## 2. Result after stage 3

Cards / letters per level (Karışık excluded). R4 applied. `*` = below 30 cards or 13 letters (level must be dropped from `levels`), `!` = above 140 cards.

| category | A1 | A2 | B1 | B2 | C1 |
|---|--:|--:|--:|--:|--:|
| `daily-life` | 71/22 | 96/24 | 125/26 | · | · |
| `food-drink` | 86/24 | 84/25 | 42/22 | 10/8* | · |
| `home` | 62/22 | 74/25 | · | · | · |
| `family-people` | 46/19 | 47/22 | 35/17 | · | · |
| `colors-numbers` | 54/19 | · | · | · | · |
| `body-health` | 38/18 | 49/21 | 72/25 | 47/21 | · |
| `school-work` | 72/23 | 119/25 | 134/26 | · | · |
| `animals` | 50/23 | 47/23 | 40/20 | · | · |
| `city-transport` | 80/23 | 66/25 | 35/21 | · | · |
| `time-weather` | 89/19 | 43/20 | · | · | · |
| `clothes-shopping` | 34/14 | 41/18 | · | · | · |
| `hobbies` | 33/14 | 99/25 | 71/22 | 26/17* | 23/14* |
| `travel` | · | 57/18 | 54/22 | 53/23 | 44/24 |
| `business` | · | · | 125/26 | 126/26 | 117/26 |
| `economy` | · | · | 46/20 | 21/18* | 31/17 |
| `feelings` | · | · | 86/23 | 31/18 | 41/18 |
| `society-law` | · | · | 34/19 | 16/11* | 55/22 |
| `verbs` | 72/21 | 53/23 | 64/21 | 109/24 | 109/21 |
| `adjectives` | 78/23 | 65/25 | 87/24 | 122/26 | 150/25! |
| `ideas` | · | · | · | 115/26 | 113/26 |
| `idioms` | · | · | · | 45/19 | · |
| `phrasal-verbs` | · | · | · | 121/20 | · |

Categories listed per level after stage 3: **A1 14, A2 14, B1 15, B2 9, C1 8** (today 12 / 12 / 13 / 9 / 7). New: `verbs` and `adjectives` at A1–C1, `ideas` at B2–C1; `daily-life` and `school-work` end at B1; `hobbies` drops out at B2 and C1.

Still below the floor (30 cards / 13 letters), hidden until stage 4 cards: `hobbies` B2 26/17 (+4 cards), `hobbies` C1 23/14 (+7), `economy` B2 21/18 (+9), `society-law` B2 16/11 (+14 and 2 new letters), `food-drink` B2 10/8 (stays hidden for good, its 10 words are the only real food words at B2).
Above 140 ("too wide"): with R4 only `adjectives` C1 (150). Without R4: `business` C1 153 and `adjectives` C1 114. Either way one C1 tile is 10–13 cards over; I propose R4 on, because the business adjectives (`lucrative`, `tangible`, `marginal`, `nominal`) are general words. Split of `adjectives` into adjectives and adverbs would cost one more category for 7 C1 adverbs and is not proposed.
`daily-life` A1–B1 now 71 / 96 / 125 (was 221 / 214 / 276); `school-work` A1–B1 unchanged 72 / 119 / 134; the stage 1 "too broad" warnings for daily-life, school-work C1 and business C1 disappear (business C1 if R4).

## 3. Spot-check sample (60 rows, 12 per level)

Mixed pos values and rules, plus the odd cases (marked ⚠ with a proposed fix). Rows are all moves (stays are not sampled).

| # | id | answer | pos | now → proposed | clue | example | ⚠ |
|--:|---|---|---|---|---|---|---|
| 1 | `a1-bad` | BAD | adj | daily-life → **adjectives** | iyi olmayan | I had a bad dream. |  |
| 2 | `a1-fine` | FINE | adj | daily-life → **adjectives** | iyi, hoş; sağlıklı ya da memnun | I am fine, thank you. | ⚠ adjective, but also noun/verb in other uses; example is the adjective; ok |
| 3 | `a1-hard` | HARD | adj | daily-life → **adjectives** | yumuşak olmayan | The cushion is soft, but the bench is hard. |  |
| 4 | `a1-okay` | OKAY | adj | daily-life → **adjectives** | sorunu olmayan | Selin fell, but she is okay. |  |
| 5 | `a1-away` | AWAY | adv | daily-life → **adjectives** | uzağa, başka bir yere | The thief ran away. |  |
| 6 | `a1-only` | ONLY | adv | daily-life → **adjectives** | yalnızca, bundan başkası değil | I only have a few coins. | ⚠ adverb that acts like a function word; stays an adverb -> adjectives (open question 1) |
| 7 | `a1-well` | WELL | adv | daily-life → **adjectives** | iyi bir biçimde, güzelce | Selin sings well. |  |
| 8 | `a1-rubber` | RING | noun | daily-life → **daily-life** | parmağa takılan halka biçimli takı, yüzük | She wears a gold ring. | ⚠ example sentence is about a ring, not rubber (data error, not a pos issue); fix the example in a content batch; category unaffected |
| 9 | `a1-fly` | FLY | verb | daily-life → **verbs** | kanatla ya da uçakla havada gitmek | Birds can fly. |  |
| 10 | `a1-help` | HELP | verb | daily-life → **verbs** | yardım etmek | Firefighters came quickly to help the villagers. |  |
| 11 | `a1-like` | LIKE | verb | daily-life → **verbs** | hoşlanmak veya beğenmek | I like this song. | ⚠ verb here; also preposition; ok, example is the verb |
| 12 | `a1-look` | LOOK | verb | daily-life → **verbs** | gözünü bir yöne çevirmek | Look, it is snowing! |  |
| 13 | `a2-careful` | CAREFUL | adj | daily-life → **adjectives** | dikkatli davranan | Be careful with the hot water. |  |
| 14 | `a2-dead` | DEAD | adj | daily-life → **adjectives** | artık yaşamayan, ölmüş | The plant is dead because we forgot to water it. |  |
| 15 | `a2-own` | OWN | adj | daily-life → **adjectives** | başkasına değil, kişinin kendisine ait | She has her own room. |  |
| 16 | `a2-anywhere` | ANYWHERE | adv | daily-life → **adjectives** | herhangi bir yerde ya da yere | You can sit anywhere. |  |
| 17 | `a2-even` | EVEN | adv | daily-life → **adjectives** | beklenmeyeni vurgulayan sözcük: bile | Even my little brother knows this. | ⚠ adverb that is really a focus particle; adjectives (open question 1) |
| 18 | `a2-indoors` | INDOORS | adv | daily-life → **adjectives** | binanın içinde | It is raining, so the kids stay indoors. |  |
| 19 | `a2-kindly` | KINDLY | adv | daily-life → **adjectives** | nazik bir şekilde | He kindly helps the old man. | ⚠ adverb in -ly, ok; ok |
| 20 | `a2-quite` | QUITE | adv | daily-life → **adjectives** | fazla olmasa da epeyce | The story is quite good. |  |
| 21 | `a2-drop` | DROP | verb | daily-life → **verbs** | elden istemeden düşürmek | Do not drop the eggs on the kitchen floor. |  |
| 22 | `a2-explode` | EXPLODE | verb | daily-life → **verbs** | büyük bir sesle patlamak | The balloon can explode. |  |
| 23 | `a2-keep` | KEEP | verb | daily-life → **verbs** | tutmak, saklamak | You can keep the book for a week. |  |
| 24 | `a2-search` | SEARCH | verb | daily-life → **verbs** | bir şeyi bulmak için dikkatle aramak | I search for my keys everywhere. |  |
| 25 | `b1-current` | CURRENT | adj | daily-life → **adjectives** | şu anda geçerli olan, güncel | What is your current address? |  |
| 26 | `b1-private` | PRIVATE | adj | daily-life → **adjectives** | herkese açık olmayan, yalnızca belli kişilerin kullandığı | This is a private beach. |  |
| 27 | `b1-spare` | SPARE | adj | daily-life → **adjectives** | yedekte tutulan, fazla | Do you have a spare key? |  |
| 28 | `b1-directly` | DIRECTLY | adv | daily-life → **adjectives** | doğrudan, dolambaçsız | Go directly to the office. |  |
| 29 | `b1-neither` | NEITHER | adv | daily-life → **adjectives** | (olumsuz cümlede) ... da değil, ... de değil | I can't swim, and neither can my sister. |  |
| 30 | `b1-normally` | NORMALLY | adv | daily-life → **adjectives** | olağan koşullarda, istisna yoksa (zarf) | I normally wake up at seven. |  |
| 31 | `b1-clothing` | CLOTHING | noun | daily-life → **daily-life** | giyim (toplu ad; çocuk giyimi gibi) | The shop sells children's clothing. | ⚠ noun in -ing; clothing is a topic word; ok, stays daily-life (clothes-shopping ends at A2) |
| 32 | `b1-warning` | WARNING | noun | daily-life → **daily-life** | tehlikeyi bildiren uyarı | There was a warning on the box. | ⚠ noun in -ing, ok (lexicalised); ok, stays daily-life |
| 33 | `b1-whenever` | WHENEVER | phrase | daily-life → **daily-life** | her ne zaman, ne zaman olursa olsun | Call me whenever you need help. | ⚠ function word (conjunction) coded phrase; stays daily-life |
| 34 | `b1-appear` | APPEAR | verb | daily-life → **verbs** | görünür hale gelmek, ortaya çıkmak | A rainbow will appear after the rain. |  |
| 35 | `b1-decorate` | DECORATE | verb | daily-life → **verbs** | bir yeri süsleyip güzelleştirmek | We decorate the tree at Christmas. |  |
| 36 | `b1-exist` | EXIST | verb | daily-life → **verbs** | var olmak | Do ghosts really exist? |  |
| 37 | `b2-meaningful` | MEANINGFUL | adj | daily-life → **adjectives** | anlamlı ve önemli | It was a meaningful conversation. |  |
| 38 | `b2-systematic` | SYSTEMATIC | adj | school-work → **adjectives** | belli bir düzene göre yapılan | They made a systematic search of the house. |  |
| 39 | `b2-wicked` | WICKED | adj | hobbies → **adjectives** | çok kötü kalpli, zalim | The wicked queen locked the princess in a tower. |  |
| 40 | `b2-barely` | BARELY | adv | daily-life → **adjectives** | zar zor, çok az farkla | I could barely hear the teacher. |  |
| 41 | `b2-barrier` | BARRIER | noun | daily-life → **ideas** | geçişi engelleyen engel | Language can be a barrier. |  |
| 42 | `b2-contrast` | CONTRAST | noun | school-work → **ideas** | iki şey arasındaki belirgin zıtlık | There is a big contrast between summer and winter here. |  |
| 43 | `b2-eel` | EEL | noun | daily-life → **ideas** | yılana benzeyen, uzun ve kaygan nehir balığı | The eel slipped through his fingers and back into the river. | ⚠ animal noun kept in stage 2; -> ideas looks wrong; alternative: keep in a hidden animals B2? (open question 4) |
| 44 | `b2-meadow` | MEADOW | noun | daily-life → **ideas** | çiçekli otlarla kaplı açık arazi; çayır | Wildflowers covered the meadow behind the old farmhouse. | ⚠ nature noun kept in stage 2; -> ideas, as the owner asked |
| 45 | `b2-scale` | SCALE | noun | school-work → **ideas** | ölçek; terazi | The map has a scale of one to a thousand. | ⚠ noun, also verb; example is the map scale; ok |
| 46 | `b2-whereas` | WHEREAS | phrase | school-work → **adjectives** | oysa, -e karşılık | He likes tea, whereas she prefers coffee. | ⚠ conjunction coded phrase, only B2+ function word; -> adjectives (open question 1), or ideas |
| 47 | `b2-infer` | INFER | verb | school-work → **verbs** | ipuçlarından sonuç çıkarmak | What can you infer from this text? |  |
| 48 | `b2-worsen` | WORSEN | verb | daily-life → **verbs** | daha kötü hale gelmek ya da getirmek | Heavy rain could worsen the flooding in the valley. |  |
| 49 | `c1-empirical` | EMPIRICAL | adj | school-work → **adjectives** | gözlem ve deneye dayanan | The theory needs empirical evidence. |  |
| 50 | `c1-heartbreaking` | HEARTBREAKING | adj | hobbies → **adjectives** | yürek parçalayıcı | It was a heartbreaking story. |  |
| 51 | `c1-negligible` | NEGLIGIBLE | adj | business → **adjectives** | yok denecek kadar az | The difference in price is negligible. |  |
| 52 | `c1-nonetheless` | NONETHELESS | adv | school-work → **adjectives** | bununla birlikte, yine de (daha resmî) | The test was hard; nonetheless, she passed. |  |
| 53 | `c1-fundraising` | FUNDRAISING | noun | school-work → **ideas** | bir amaç için para toplama etkinliği | The school held a fundraising evening. | ⚠ noun in -ing, could be society; -> ideas, ok |
| 54 | `c1-hardship` | HARDSHIP | noun | school-work → **ideas** | yoksulluk veya acı çekilen zor dönem | Many families faced hardship during the war. |  |
| 55 | `c1-offspring` | OFFSPRING | noun | school-work → **ideas** | bir canlının yavruları veya çocukları | The bird protects its offspring. | ⚠ kept nature word, noun in -ing; -> ideas |
| 56 | `c1-setback` | SETBACK | noun | hobbies → **ideas** | ilerlemeyi geciktiren engel | The injury was a major setback for the team. |  |
| 57 | `c1-sibling` | SIBLING | noun | school-work → **ideas** | kız ya da erkek kardeş | She has one sibling, an older brother. | ⚠ family word, noun in -ing; -> ideas; family-people ends at B1; accept or exception |
| 58 | `c1-wellbeing` | WELLBEING | noun | school-work → **ideas** | sağlık ve mutluluk hâli | The school cares about pupils' wellbeing. | ⚠ noun in -ing; -> ideas, ok |
| 59 | `c1-inhibit` | INHIBIT | verb | school-work → **verbs** | bir süreci veya davranışı bastırıp kısıtlamak | Fear can inhibit learning. |  |
| 60 | `c1-intrigue` | INTRIGUE | verb | hobbies → **verbs** | merak uyandırıp ilgi çekmek | The plot will intrigue viewers. |  |

### What the sample and a scan of the full list showed about the pos column

- I checked every in-scope answer that is also a common word of another class (`light`, `match`, `like`, `well`, `even`, `fast`, `fine`, `hard`, `sound`, `present`, `claim`, `yield` ...: 86 cards). In every case the example sentence uses the class written in `pos`, so the pos column is reliable as the primary signal. No exception rule is needed.
- Gerund-looking nouns (`clothing`, `warning`, `ceiling`, `upbringing`, `wellbeing`, `sibling`) are real nouns; they follow the noun rule.
- **Not a pos problem but found on the way:** `a1-rubber` has the example "She wears a gold ring." (wrong sentence for the answer RUBBER). To be fixed in a content batch, not in stage 3.
- The only verb-looking noun risk is the 4 animal nouns the owner kept (`eel`, `otter`, `jackal`, `tortoise`): they would land in `ideas`, which does not fit (open question 4).

## 4. Open questions for the owner

1. **Is `Sıfatlar ve zarflar` fine for adverbs and for the few function words** (`whereas` at B2; at A1–B1 function words stay in `daily-life`)? Alternative: a separate small category is not worth it (7 adverbs at C1).
2. **R4** (business C1 adjectives/adverbs to `adjectives`): yes (recommended) or no (business C1 stays 153, adjectives C1 114)?
3. **R5** (the 10 generic B2 food-drink words leave food-drink): yes or keep them, which keeps the hidden pair at 20 cards?
4. **Animals at B2:** `eel`, `otter`, `jackal`, `tortoise` go to `ideas` by the rule. Accept, or send them to `daily-life` and keep `daily-life` listed at B2 (it would only have 4 cards, so hidden)?
5. **`sibling` / `upbringing` / `fundraising` at C1** go to `ideas` although they are family or society words; accept (the topic categories end at B1 / are thin at C1) or move individually.
6. **Hobbies B2/C1** drop below the floor (26 / 23 cards) because only the sport/music/film nouns stay; they are hidden until stage 4 adds 4 / 7 cards, or the whole pair can be dropped for good. Which?
7. Phrasal verbs and idioms stay out of the word-class lists (my proposal); `verbs` therefore does not contain phrasal verbs. OK?

