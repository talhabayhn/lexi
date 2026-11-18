# Category rebalance — stage 2 review sheet (topic moves)

Status: **review only, nothing applied.** Stage 1 is on main (`8ef91e2`). Source: `CATEGORY-MOVES-C.csv` topic rows (622 rows) re-judged against each card's `clue_tr` and `example_en` as it is now. Same rows in `CATEGORY-STAGE2.csv`.

## How to answer

Default is **approve all**. Write:

`hepsi onay, hariç: <id> → <category>, <id> → keep, ...`

`<category>` is the category the card should go to; `keep` means no stage-2 move (the card stays where it is now and the stage 3 word-class rule decides later). The *proposed* column is what happens if you say nothing.

Owner decisions already applied: `animals` ends at B1 (nature words of B2/C1 go to `ideas`); B2 food words are not folded into body-health; A1 not reopened; listing floor 30 cards and 13 letters.

## Summary

- 622 rows: **388 high**, **234 low** confidence.
- **97 rows where I disagree with the CSV** (proposed ≠ CSV target; the sheet shows my proposal). Of these 84 are `keep` (stay put, stage 3 decides), the rest go to another category.
- 137 more low rows keep the CSV target but are borderline (general word used in a topic example, multi-sense word).
- Cards that really change category in stage 2 if you approve all: **528**; the other 94 rows are `keep`.

### Low confidence, by theme

- `feelings` target, low: 61 rows (36 proposed `keep`).
- `society-law` target, low: 31; `economy` target, low: 69.
- `hobbies` target, low: 33; `clothes-shopping`, low: 13; nature (animals/ideas), low: 25.
- Homographs checked against their example sentence: `suit` (clothing, ok), `play` (a game, ok), `breed` (animals, ok), `deal` (money sense, ok), `paint` (fence = chore, keep), `top` (ladder, keep), `gain` (skills/weight, keep), `soar` (prices, economy but low), `charge`/`account` (money sense, ok), `migrate` (birds, keep), `sanction`, `convention`, `lease` (general, low).

### Rows where I changed the CSV target

| # | id | answer | CSV said | I propose | why |
|--:|---|---|---|---|---|
| 537 | `b1-gain` | GAIN | economy | **keep (business)** | example is gaining skills or weight, not money |
| 540 | `b1-increase` | INCREASE | economy | **keep (business)** | general verb (prices increase) |
| 547 | `b1-quantity` | QUANTITY | economy | **keep (business)** | general measure noun |
| 548 | `b1-rate` | RATE | economy | **keep (business)** | general measure noun (unemployment rate) |
| 556 | `b2-asset` | ASSET | economy | **keep (business)** | example is language skills, not finance |
| 563 | `b2-distribute` | DISTRIBUTE | economy | **keep (business)** | general verb |
| 565 | `b2-estimate` | ESTIMATE | economy | **keep (business)** | general verb |
| 593 | `c1-benchmark` | BENCHMARK | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 596 | `c1-bulk` | BULK | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 597 | `c1-capitalize` | CAPITALIZE | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 598 | `c1-consolidate` | CONSOLIDATE | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 601 | `c1-equity` | EQUITY | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 605 | `c1-leverage` | LEVERAGE | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 606 | `c1-liability` | LIABILITY | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 607 | `c1-marginal` | MARGINAL | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 609 | `c1-nominal` | NOMINAL | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 610 | `c1-nonprofit` | NONPROFIT | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 611 | `c1-offset` | OFFSET | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 616 | `c1-tangible` | TANGIBLE | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 618 | `c1-textile` | TEXTILE | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 619 | `c1-timescale` | TIMESCALE | economy | **keep (business)** | general or multi-sense word; stage 3 |
| 529 | `a1-paint` | PAINT | hobbies | **keep (daily-life)** | example is painting a fence (a chore), not art |
| 530 | `a1-top` | TOP | clothes-shopping | **keep (daily-life)** | example is the top of a ladder, not clothing |
| 531 | `a2-lend` | LEND | clothes-shopping | **keep (daily-life)** | lending is not shopping; stage 3 (verbs) |
| 532 | `a2-style` | STYLE | clothes-shopping | **keep (daily-life)** | "style" is general (example: your style) |
| 533 | `b1-aware` | AWARE | feelings | **keep (daily-life)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 535 | `b1-bulb` | BULB | animals | **keep (daily-life)** | electric light bulb / colour, not nature |
| 536 | `b1-emergency` | EMERGENCY | society-law | **keep (daily-life)** | not clearly law or society |
| 539 | `b1-honestly` | HONESTLY | feelings | **keep (daily-life)** | adverb of manner; stage 3 (adjectives and adverbs) |
| 542 | `b1-joker` | JOKER | hobbies | **keep (daily-life)** | not a hobby word (joker = funny person; juggle = manage tasks; novelty = newness) |
| 543 | `b1-military` | MILITARY | society-law | **keep (daily-life)** | not clearly law or society |
| 544 | `b1-negative` | NEGATIVE | feelings | **keep (daily-life)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 545 | `b1-nightmare` | NIGHTMARE | feelings | **keep (daily-life)** | a dream, not a feeling word |
| 546 | `b1-positive` | POSITIVE | feelings | **keep (daily-life)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 549 | `b1-sensible` | SENSIBLE | feelings | **keep (daily-life)** | Yalancı eşdeğerler card: "sensible" = reasonable/judgement, an adjective rather than a feeling; stage 3 word-class rule |
| 550 | `b1-threat` | THREAT | society-law | **keep (daily-life)** | not clearly law or society |
| 551 | `b1-uncomfortable` | UNCOMFORTABLE | feelings | **keep (daily-life)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 552 | `b1-unpleasant` | UNPLEASANT | feelings | **keep (daily-life)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 553 | `b1-violet` | VIOLET | animals | **keep (daily-life)** | electric light bulb / colour, not nature |
| 554 | `b1-weapon` | WEAPON | society-law | **keep (daily-life)** | not clearly law or society |
| 555 | `b1-weird` | WEIRD | feelings | **keep (daily-life)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 558 | `b2-compliment` | COMPLIMENT | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 559 | `b2-confess` | CONFESS | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 560 | `b2-confront` | CONFRONT | society-law | **keep (daily-life)** | example is not about law or society (fear, handshake, job offer, house) |
| 561 | `b2-conscious` | CONSCIOUS | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 562 | `b2-convention` | CONVENTION | society-law | **keep (daily-life)** | example is not about law or society (fear, handshake, job offer, house) |
| 564 | `b2-eel` | EEL | ideas | **keep (daily-life)** | an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 567 | `b2-graceful` | GRACEFUL | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 568 | `b2-grumble` | GRUMBLE | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 570 | `b2-hesitate` | HESITATE | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 571 | `b2-ignorant` | IGNORANT | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 572 | `b2-jackal` | JACKAL | ideas | **keep (daily-life)** | an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 573 | `b2-juggle` | JUGGLE | hobbies | **keep (daily-life)** | not a hobby word (joker = funny person; juggle = manage tasks; novelty = newness) |
| 575 | `b2-mansion` | MANSION | society-law | **keep (daily-life)** | example is not about law or society (fear, handshake, job offer, house) |
| 576 | `b2-migrate` | MIGRATE | society-law | **keep (daily-life)** | example is birds migrating (nature, not society) |
| 578 | `b2-mumble` | MUMBLE | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 580 | `b2-novelty` | NOVELTY | hobbies | **keep (daily-life)** | not a hobby word (joker = funny person; juggle = manage tasks; novelty = newness) |
| 583 | `b2-otter` | OTTER | ideas | **keep (daily-life)** | an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 585 | `b2-rejection` | REJECTION | society-law | **keep (daily-life)** | example is not about law or society (fear, handshake, job offer, house) |
| 586 | `b2-tortoise` | TORTOISE | ideas | **keep (daily-life)** | an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 587 | `b2-unaware` | UNAWARE | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 588 | `b2-unbearable` | UNBEARABLE | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 589 | `b2-warmth` | WARMTH | feelings | **keep (daily-life)** | speech act / movement / general adjective, not a feeling; stage 3 word class |
| 300 | `a2-tax` | TAX | society-law | **economy** | money / trade word; tax is paid, not a law |
| 327 | `b2-tenant` | TENANT | society-law | **economy** | renting a home: rent is a money word |
| 337 | `c1-capitalism` | CAPITALISM | society-law | **economy** | money / trade word; tax is paid, not a law |
| 348 | `c1-monopoly` | MONOPOLY | society-law | **economy** | money / trade word; tax is paid, not a law |
| 356 | `c1-subsidy` | SUBSIDY | society-law | **economy** | money / trade word; tax is paid, not a law |
| 359 | `c1-taxation` | TAXATION | society-law | **economy** | money / trade word; tax is paid, not a law |
| 360 | `c1-taxpayer` | TAXPAYER | society-law | **economy** | money / trade word; tax is paid, not a law |
| 538 | `b1-happily` | HAPPILY | feelings | **keep (family-people)** | adverb of manner; stage 3 (adjectives and adverbs) |
| 541 | `b1-independent` | INDEPENDENT | feelings | **keep (family-people)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 111 | `b2-thrill` | THRILL | hobbies | **feelings** | a feeling (example: roller-coaster thrill) |
| 534 | `b1-brilliant` | BRILLIANT | feelings | **keep (school-work)** | general adjective, not an emotion or trait; stage 3 (adjectives) |
| 591 | `c1-absurd` | ABSURD | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 592 | `c1-advocate` | ADVOCATE | society-law | **keep (school-work)** | example is doctors and diet |
| 594 | `c1-benign` | BENIGN | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 595 | `c1-brutal` | BRUTAL | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 599 | `c1-daunting` | DAUNTING | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 600 | `c1-discreet` | DISCREET | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 602 | `c1-eradicate` | ERADICATE | ideas | **keep (school-work)** | example is eradicating a disease; stage 3 |
| 603 | `c1-famine` | FAMINE | society-law | **keep (school-work)** | hunger / disaster; nature or society, no clear home |
| 604 | `c1-grim` | GRIM | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 608 | `c1-newcomer` | NEWCOMER | society-law | **keep (school-work)** | "newcomer" is a general noun (example: feeling lost) |
| 612 | `c1-outspoken` | OUTSPOKEN | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 613 | `c1-pledge` | PLEDGE | society-law | **keep (school-work)** | general verb; example is a mayor and money |
| 614 | `c1-relentless` | RELENTLESS | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 615 | `c1-sentiment` | SENTIMENT | feelings | **keep (school-work)** | means public opinion, an abstract noun |
| 617 | `c1-tedious` | TEDIOUS | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 620 | `c1-unconditional` | UNCONDITIONAL | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 621 | `c1-vigilant` | VIGILANT | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 622 | `c1-youthful` | YOUTHFUL | feelings | **keep (school-work)** | general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 174 | `b1-innocent` | INNOCENT | feelings | **society-law** | example is a court verdict, not a trait |
| 200 | `b2-fraud` | FRAUD | economy | **society-law** | fraud is a crime (example: prison); not an economy word |
| 231 | `c1-harass` | HARASS | feelings | **society-law** | harassment is a legal / social issue |
| 237 | `c1-infrastructure` | INFRASTRUCTURE | economy | **society-law** | roads / energy / communication: public works |
| 256 | `c1-solidarity` | SOLIDARITY | feelings | **society-law** | example is workers and strikers |

## Result after stage 2 (if everything is approved as proposed)

Cards / letters per category and level. `<30` or `<13` = below the listing floor, the level is dropped from `levels` until it is filled. Only levels that change are shown; stage 3 (word-class split) later moves many cards out of `hobbies` B2/C1, `daily-life` and `school-work`, so those numbers are not final.

| category | level | now | after | letters | listed? |
|---|---|--:|--:|--:|---|
| `feelings` | B1 | 0 | 86 | 23 | yes |
| `feelings` | B2 | 0 | 31 | 18 | yes |
| `feelings` | C1 | 0 | 41 | 18 | yes |
| `society-law` | B1 | 0 | 34 | 19 | yes |
| `society-law` | B2 | 0 | 16 | 11 | **no: hidden** |
| `society-law` | C1 | 0 | 55 | 22 | yes |
| `economy` | B1 | 0 | 46 | 20 | yes |
| `economy` | B2 | 0 | 21 | 18 | **no: hidden** |
| `economy` | C1 | 0 | 31 | 17 | yes |
| `clothes-shopping` | A1 | 0 | 34 | 14 | yes |
| `clothes-shopping` | A2 | 0 | 41 | 18 | yes |
| `hobbies` | A1 | 0 | 33 | 14 | yes |
| `hobbies` | A2 | 80 | 99 | 25 | yes |
| `hobbies` | B1 | 68 | 71 | 22 | yes |
| `hobbies` | B2 | 51 | 53 | 22 | yes |
| `animals` | B1 | 26 | 40 | 20 | yes |
| `ideas` | B2 | 0 | 12 | 9 | **no: hidden** |
| `ideas` | C1 | 0 | 9 | 7 | **no: hidden** |
| `business` | B1 | 170 | 125 | 26 | yes |
| `business` | B2 | 147 | 126 | 26 | yes |
| `business` | C1 | 188 | 153 | 26 | yes |
| `daily-life` | A1 | 288 | 221 | 26 | yes |
| `daily-life` | A2 | 274 | 214 | 26 | yes |
| `daily-life` | B1 | 350 | 276 | 26 | yes |
| `daily-life` | B2 | 241 | 180 | 26 | yes |
| `family-people` | B1 | 82 | 35 | 17 | yes |
| `school-work` | B1 | 151 | 134 | 26 | yes |
| `school-work` | C1 | 389 | 288 | 26 | yes |

### Pairs still under the 30 cards / 13 letters floor

- `society-law` B2: 16 cards, 11 letters. letters 11 < 13 as well; needs stage 4 cards.
- `economy` B2: 21 cards, 18 letters. CSV alone gave 24; 4 cards of mine are `keep`/society-law. Needs stage 4 cards (about +9 to reach 30).
- `ideas` B2: 12 cards, 9 letters. only the nature words; becomes real in stage 3 (word-class nouns).
- `ideas` C1: 9 cards, 7 letters. same, stage 3.
- `food-drink` B2: stays at 20 cards and hidden. The B2 data has only about 11 real food/drink/cooking words (mozzarella, oyster, lobster, nibble, organic, herbal, blend, nutrient and with the examples exotic, varied, authentic); the rest of the 20 are generic words (adequate, mediocre, reputation, preference...). A search of the B2 dump found no more than 2 more candidates (`consume`, `yield`, both multi-sense), so it cannot reach 30 without new cards: **stays hidden at B2**.

### Levels to list after stage 2

`feelings` B1·B2·C1 (B2 = 31 cards, thin) · `society-law` B1·C1 · `economy` B1·C1 (C1 = 31, thin) · `clothes-shopping` A1·A2 · `hobbies` A1·A2·B1·B2·C1 · `animals` A1·A2·B1 · not yet listed: `society-law` B2, `economy` B2, `ideas`.

## The rows

Numbered continuously. Grouped by **proposed** category, then level. `keep` at the end = no stage-2 move. `now` = the category the card has today.

### → Duygu ve karakter (`feelings`)  (158)

#### B1 (86)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 1 | `b1-admire` | ADMIRE | family-people | birine veya bir şeye hayran olmak, takdir etmek | I admire your bravery. | high | feelings / character word |
| 2 | `b1-amaze` | AMAZE | daily-life | çok şaşırtmak | Her skill will amaze you. | high | feelings / character word |
| 3 | `b1-amazement` | AMAZEMENT | daily-life | büyük şaşkınlık | She looked at it in amazement. | high | feelings / character word |
| 4 | `b1-ambition` | AMBITION | school-work | başarılı olmak için duyulan güçlü istek | Her ambition is to become a doctor. | high | feelings / character word |
| 5 | `b1-annoy` | ANNOY | daily-life | birini sinirlendirmek veya rahatsız etmek | His loud music began to annoy me. | high | feelings / character word |
| 6 | `b1-annoyed` | ANNOYED | daily-life | bir şeye canı sıkılmış, kızgın | She was annoyed because he was late. | high | feelings / character word |
| 7 | `b1-annoying` | ANNOYING | daily-life | insanı sinirlendiren, can sıkıcı | The noise from the street is annoying. | high | feelings / character word |
| 8 | `b1-awkward` | AWKWARD | daily-life | rahatsız edici ve utandırıcı; beceriksiz | There was an awkward moment after his joke. | high | feelings / character word |
| 9 | `b1-calm` | CALM | daily-life | sakin, heyecanlanmayan | Stay calm and listen to me. | high | feelings / character word |
| 10 | `b1-careless` | CARELESS | daily-life | dikkatsiz, özensiz | A careless driver can have an accident. | high | feelings / character word |
| 11 | `b1-comfort` | COMFORT | daily-life | rahatlık ve huzur hissi | They live in comfort in a big house. | high | feelings / character word |
| 12 | `b1-confidence` | CONFIDENCE | school-work | kendine duyulan güven | She speaks English with confidence. | high | feelings / character word |
| 13 | `b1-confident` | CONFIDENT | school-work | kendine güvenen | I feel confident about the test. | high | feelings / character word |
| 14 | `b1-confused` | CONFUSED | daily-life | ne yapacağını bilemeyen, kafası karışmış | I am confused by this map. | high | feelings / character word |
| 15 | `b1-courage` | COURAGE | family-people | korkuya rağmen harekete geçebilme gücü | It takes courage to speak in public. | high | feelings / character word |
| 16 | `b1-cuddle` | CUDDLE | family-people | sevgiyle kucaklamak | The child wanted to cuddle her toy. | high | feelings / character word |
| 17 | `b1-curious` | CURIOUS | family-people | öğrenmeye meraklı | Children are curious about everything. | high | feelings / character word |
| 18 | `b1-daydream` | DAYDREAM | daily-life | dalıp gidip hoş hayaller kurmak | I often daydream during long lessons. | high | feelings / character word |
| 19 | `b1-delight` | DELIGHT | family-people | büyük sevinç ve zevk | The children shouted with delight. | high | feelings / character word |
| 20 | `b1-desperate` | DESPERATE | daily-life | çaresiz, umutsuz; bir şeye çok ihtiyaç duyan | He is desperate for a job. | high | feelings / character word |
| 21 | `b1-embarrassed` | EMBARRASSED | daily-life | utanmış, mahcup olmuş | I felt embarrassed when I forgot his name. | high | feelings / character word |
| 22 | `b1-embarrassing` | EMBARRASSING | daily-life | insanı utandıran | It was an embarrassing mistake. | high | feelings / character word |
| 23 | `b1-emotion` | EMOTION | family-people | sevinç, korku gibi duygu | She could not hide her emotion. | high | feelings / character word |
| 24 | `b1-emotional` | EMOTIONAL | daily-life | çabuk duygulanan ya da duygu yüklü (sıfat) | It was an emotional goodbye at the airport. | high | feelings / character word |
| 25 | `b1-encourage` | ENCOURAGE | school-work | birine cesaret ve destek vermek | Teachers should encourage students to read. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 26 | `b1-enjoyable` | ENJOYABLE | daily-life | keyif veren, zevkli | It was an enjoyable evening. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 27 | `b1-enjoyment` | ENJOYMENT | daily-life | bir şeyden alınan zevk | Reading gives me great enjoyment. | high | feelings / character word |
| 28 | `b1-enthusiasm` | ENTHUSIASM | school-work | bir işe duyulan büyük istek, coşku | She worked with great enthusiasm. | high | feelings / character word |
| 29 | `b1-enthusiastic` | ENTHUSIASTIC | school-work | bir işe büyük heyecan duyan | The students were enthusiastic about the visit. | high | feelings / character word |
| 30 | `b1-excitement` | EXCITEMENT | daily-life | heyecan, coşku | The children jumped with excitement. | high | feelings / character word |
| 31 | `b1-feeling` | FEELING | family-people | içinde duyulan his | I have a good feeling about this plan. | high | feelings / character word |
| 32 | `b1-forgive` | FORGIVE | family-people | bir hatayı bağışlamak | Please forgive me for being late. | high | feelings / character word |
| 33 | `b1-frightened` | FRIGHTENED | family-people | korkmuş, ürkmüş | The frightened child hid under the bed. | high | feelings / character word |
| 34 | `b1-generous` | GENEROUS | family-people | paylaşmaktan çekinmeyen, eli açık | He is very generous with his friends. | high | feelings / character word |
| 35 | `b1-genius` | GENIUS | school-work | olağanüstü zeki kişi, deha | Einstein was a genius. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 36 | `b1-gentle` | GENTLE | family-people | yumuşak huylu, nazik | She has a gentle voice. | high | feelings / character word |
| 37 | `b1-giggle` | GIGGLE | daily-life | hafif sesle ve kıkır kıkır gülmek | The children began to giggle during the film. | high | feelings / character word |
| 38 | `b1-grateful` | GRATEFUL | family-people | yardım için minnettar olan | I am grateful for your help. | high | feelings / character word |
| 39 | `b1-greedy` | GREEDY | family-people | hep daha fazlasını isteyen, açgözlü | Don't be greedy; leave some cake for me. | high | feelings / character word |
| 40 | `b1-grin` | GRIN | daily-life | yüzde beliren geniş, neşeli gülümseme | He had a big grin on his face after the game. | high | feelings / character word |
| 41 | `b1-guilt` | GUILT | daily-life | yanlış yapınca duyulan kötü his | He felt guilt after the lie. | high | feelings / character word |
| 42 | `b1-guilty` | GUILTY | daily-life | suç işlemiş veya suçluluk duyan | He felt guilty about the lie. | low | legal "guilty" vs feeling; example is the feeling |
| 43 | `b1-happiness` | HAPPINESS | family-people | mutlu olmanın verdiği güzel his | Money cannot buy happiness. | high | feelings / character word |
| 44 | `b1-honest` | HONEST | family-people | doğru sözlü, yalan söylemeyen | Please be honest with me. | high | feelings / character word |
| 45 | `b1-hopeful` | HOPEFUL | daily-life | iyi bir sonuç bekleyen, umutlu | We are hopeful about the result. | high | feelings / character word |
| 46 | `b1-hopeless` | HOPELESS | daily-life | umudu kalmamış | The case looks hopeless. | high | feelings / character word |
| 47 | `b1-humour` | HUMOR | family-people | insanları güldürme yeteneği | I love his dry humor. | high | feelings / character word |
| 48 | `b1-impatient` | IMPATIENT | family-people | beklemekten hoşlanmayan, sabırsız | Don't be so impatient; the food is nearly ready. | high | feelings / character word |
| 49 | `b1-impressed` | IMPRESSED | family-people | etkilenmiş, hayran kalmış | I was impressed by her singing. | high | feelings / character word |
| 50 | `b1-inspire` | INSPIRE | school-work | birine yeni fikir ve heves vermek | Her story can inspire young people. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 51 | `b1-intelligent` | INTELLIGENT | school-work | zeki, öğrenme yeteneği yüksek | She is an intelligent girl. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 52 | `b1-jealousy` | JEALOUSY | family-people | başkasında olanı istemekten doğan kıskançlık | Jealousy can make you very unhappy. | high | feelings / character word |
| 53 | `b1-joyful` | JOYFUL | daily-life | çok mutlu ve sevinçli | It was a joyful day for the whole family. | high | feelings / character word |
| 54 | `b1-kindness` | KINDNESS | family-people | başkalarına nazik ve iyi davranma | Thank you for your kindness. | high | feelings / character word |
| 55 | `b1-laughter` | LAUGHTER | daily-life | gülen insanların çıkardığı ses | The room was full of laughter. | high | feelings / character word |
| 56 | `b1-loyal` | LOYAL | family-people | bağlı, ihanet etmeyen | A dog is a loyal friend. | high | feelings / character word |
| 57 | `b1-miserable` | MISERABLE | family-people | çok mutsuz | She felt miserable in the rain. | high | feelings / character word |
| 58 | `b1-mood` | MOOD | family-people | o anki ruh hali | She is in a good mood today. | high | feelings / character word |
| 59 | `b1-motivate` | MOTIVATE | school-work | birini bir şeyi yapmaya isteklendirmek, güdülemek | A good teacher can motivate students. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 60 | `b1-obedient` | OBEDIENT | family-people | söz dinleyen | The dog is very obedient. | high | feelings / character word |
| 61 | `b1-optimist` | OPTIMIST | family-people | olayların iyi biteceğine inanan kişi (isim) | My brother is an optimist and never worries. | high | feelings / character word |
| 62 | `b1-optimistic` | OPTIMISTIC | family-people | her şeyin iyi olacağına inanan, iyimser | I am optimistic about the future. | high | feelings / character word |
| 63 | `b1-outgoing` | OUTGOING | family-people | girişken, herkesle kolay arkadaş olan | She is an outgoing girl. | high | feelings / character word |
| 64 | `b1-personality` | PERSONALITY | family-people | bir kişinin kendine özgü karakteri | She has a warm personality. | high | feelings / character word |
| 65 | `b1-playful` | PLAYFUL | daily-life | oyun oynamayı seven, neşeli ve haylaz | Our puppy is very playful. | high | feelings / character word |
| 66 | `b1-praise` | PRAISE | school-work | birini överek takdir etmek | Teachers should praise good work. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 67 | `b1-pride` | PRIDE | family-people | başarıdan duyulan gurur | He looked at his son with pride. | high | feelings / character word |
| 68 | `b1-regret` | REGRET | family-people | yaptığına veya yapmadığına üzülmek | I regret saying that. | high | feelings / character word |
| 69 | `b1-relief` | RELIEF | daily-life | kaygı geçince duyulan rahatlama | It was a relief to hear that she was safe. | high | feelings / character word |
| 70 | `b1-respect` | RESPECT | family-people | birine duyulan saygı | Children should show respect to older people. | high | feelings / character word |
| 71 | `b1-romantic` | ROMANTIC | family-people | aşk ve duygularla ilgili | We had a romantic dinner by the sea. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 72 | `b1-satisfied` | SATISFIED | daily-life | durumdan memnun olan | Are you satisfied with your new phone? | high | feelings / character word |
| 73 | `b1-scream` | SCREAM | daily-life | korku ya da acıyla çığlık atmak | The children scream on the roller coaster. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 74 | `b1-selfish` | SELFISH | family-people | yalnızca kendini düşünen, bencil | Don't be so selfish; share your toys. | high | feelings / character word |
| 75 | `b1-sensitive` | SENSITIVE | family-people | çabuk duygulanan veya kolay etkilenen | She is very sensitive to criticism. | high | feelings / character word |
| 76 | `b1-shame` | SHAME | daily-life | utanç; yazık | It is a shame that you cannot come. | high | feelings / character word |
| 77 | `b1-shock` | SHOCK | daily-life | kötü bir haberin yarattığı ani sarsıntı | The news came as a shock. | high | feelings / character word |
| 78 | `b1-stubborn` | STUBBORN | family-people | fikrinden dönmeyen, inatçı | My brother is very stubborn. | high | feelings / character word |
| 79 | `b1-talkative` | TALKATIVE | family-people | çok konuşan | My little brother is very talkative. | high | feelings / character word |
| 80 | `b1-terrified` | TERRIFIED | family-people | dehşete düşmüş, çok korkmuş | She was terrified of the dark. | high | feelings / character word |
| 81 | `b1-thrilled` | THRILLED | family-people | çok heyecanlı ve sevinçli | She was thrilled to get the job. | high | feelings / character word |
| 82 | `b1-understanding` | UNDERSTANDING | family-people | başkasının durumunu anlayan, anlayışlı | My teacher was very understanding. | high | feelings / character word |
| 83 | `b1-upset` | UPSET | family-people | kötü bir olay yüzünden morali bozulmuş, kırılmış | She was upset about the news. | high | feelings / character word |
| 84 | `b1-weakness` | WEAKNESS | family-people | zayıf olan yön | Shyness is her only weakness. | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 85 | `b1-willing` | WILLING | daily-life | bir şeyi yapmaya hazır, gönüllü | Are you willing to help us? | low | borderline: trait / social-emotional verb; fits "character" loosely |
| 86 | `b1-worrying` | WORRYING | daily-life | insanı endişelendiren | The news is worrying. | high | feelings / character word |

#### B2 (31)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 87 | `b2-alarming` | ALARMING | daily-life | korku veya endişe yaratan | The news was alarming. | high | feelings / character word |
| 88 | `b2-cautious` | CAUTIOUS | daily-life | riskten kaçınan, tedbirli | Be cautious when you cross the road. | high | feelings / character word |
| 89 | `b2-eager` | EAGER | daily-life | bir şeyi yapmayı çok isteyen, hevesli | The children were eager to open their presents. | high | feelings / character word |
| 90 | `b2-easygoing` | EASYGOING | daily-life | rahat tavırlı, kolay kızmayan | My boss is easygoing and rarely gets upset. | high | feelings / character word |
| 91 | `b2-embarrass` | EMBARRASS | daily-life | birini başkalarının yanında mahcup etmek | Do not embarrass me in front of my friends. | high | feelings / character word |
| 92 | `b2-frustration` | FRUSTRATION | daily-life | isteneni yapamamaktan doğan hayal kırıklığı | I could hear the frustration in his voice. | high | feelings / character word |
| 93 | `b2-furious` | FURIOUS | daily-life | çok öfkeli | He was furious about the delay. | high | feelings / character word |
| 94 | `b2-generosity` | GENEROSITY | daily-life | karşılık beklemeden bol bol verme isteği | Thank you for your generosity. | high | feelings / character word |
| 95 | `b2-gloomy` | GLOOMY | daily-life | karamsar veya kasvetli | It was a gloomy, rainy day. | high | feelings / character word |
| 96 | `b2-hatred` | HATRED | daily-life | birine duyulan derin nefret | Years of war left a deep hatred between the two peoples. | high | feelings / character word |
| 97 | `b2-humble` | HUMBLE | daily-life | alçakgönüllü, gösterişsiz | He is a humble man despite his success. | high | feelings / character word |
| 98 | `b2-humorous` | HUMOROUS | daily-life | komik, espriye yatkın | He wrote a humorous story. | high | feelings / character word |
| 99 | `b2-irritate` | IRRITATE | daily-life | rahatsız edip sinirlendirmek | Loud music can irritate the neighbors. | high | feelings / character word |
| 100 | `b2-naive` | NAIVE | daily-life | deneyimsiz ve kolay kandırılan, saf | It was naive to believe everything he said. | high | feelings / character word |
| 101 | `b2-obsession` | OBSESSION | daily-life | zihni sürekli meşgul eden saplantı | Football is an obsession for him. | low | borderline fit; example is about feeling or trait |
| 102 | `b2-offend` | OFFEND | daily-life | birini kırmak veya gücendirmek | I hope I did not offend you. | high | feelings / character word |
| 103 | `b2-optimism` | OPTIMISM | daily-life | iyimserlik; olumlu bakış | His optimism helped the team. | high | feelings / character word |
| 104 | `b2-panic` | PANIC | daily-life | ani ve kontrolsüz korku | There was panic when the fire alarm rang. | high | feelings / character word |
| 105 | `b2-reluctant` | RELUCTANT | daily-life | bir şeyi yapmaya isteksiz | He was reluctant to speak in public. | high | feelings / character word |
| 106 | `b2-skeptical` | SKEPTICAL | daily-life | bir şeye inanmakta tereddüt eden, şüpheci | I am skeptical about the story. | high | feelings / character word |
| 107 | `b2-sympathetic` | SYMPATHETIC | daily-life | başkalarının derdini anlayan, ilgili | The nurse was kind and sympathetic. | high | Yalancı eşdeğerler card: kind/understanding person, fits character |
| 108 | `b2-sympathize` | SYMPATHIZE | daily-life | başkasının duygularını anlayıp paylaşmak | I sympathize with your situation. | high | feelings / character word |
| 109 | `b2-sympathy` | SYMPATHY | daily-life | başkasının acısını paylaşma | I have a lot of sympathy for him. | high | feelings / character word |
| 110 | `b2-temper` | TEMPER | daily-life | kolay öfkelenmeye yatkın huy | He has a terrible temper and shouts when he loses. | high | feelings / character word |
| 111 | `b2-thrill` | THRILL | daily-life | ani ve güçlü bir heyecan, haz | Riding the roller coaster gave him a thrill he would never forget. | low | CSV said hobbies. a feeling (example: roller-coaster thrill) |
| 112 | `b2-tolerate` | TOLERATE | daily-life | rahatsız edici bir şeye katlanmak | I cannot tolerate this noise. | low | borderline fit; example is about feeling or trait |
| 113 | `b2-uneasy` | UNEASY | daily-life | huzursuz, tedirgin | I felt uneasy in the dark street. | high | feelings / character word |
| 114 | `b2-unwilling` | UNWILLING | daily-life | bir şeyi yapmak istemeyen | He was unwilling to help. | high | feelings / character word |
| 115 | `b2-vicious` | VICIOUS | daily-life | çok acımasız ve saldırgan | The dog looked vicious. | low | borderline fit; example is about feeling or trait |
| 116 | `b2-vulnerable` | VULNERABLE | daily-life | zarar görmeye açık, savunmasız | Small children are vulnerable. | low | borderline fit; example is about feeling or trait |
| 117 | `b2-wisdom` | WISDOM | daily-life | deneyimle kazanılan bilgelik | Old people are full of wisdom. | low | borderline fit; example is about feeling or trait |

#### C1 (41)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 118 | `c1-alienate` | ALIENATE | school-work | birini kendinden soğutup uzaklaştırmak | Harsh words may alienate your friends. | low | borderline fit with feelings |
| 119 | `c1-apathy` | APATHY | school-work | her şeye karşı ilgisizlik, umursamazlık | Voter apathy is a real problem. | high | feelings / character word |
| 120 | `c1-avid` | AVID | school-work | (okur, hayran) çok hevesli, tutkulu | She is an avid reader of crime novels. | high | feelings / character word |
| 121 | `c1-benevolent` | BENEVOLENT | school-work | iyiliksever, yardım etmeyi seven | A benevolent donor paid for the new library. | high | feelings / character word |
| 122 | `c1-blunt` | BLUNT | school-work | açık sözlü, nezaketle yumuşatmayan | She gave a blunt answer to the question. | high | feelings / character word |
| 123 | `c1-boast` | BOAST | school-work | kendi başarılarıyla övünmek | He likes to boast about his new car. | high | feelings / character word |
| 124 | `c1-charitable` | CHARITABLE | school-work | yardımsever; hayır işleriyle ilgili | She gave money to a charitable organization. | low | borderline fit with feelings |
| 125 | `c1-cherish` | CHERISH | school-work | çok değer verip sevgiyle korumak | I will always cherish these memories. | high | feelings / character word |
| 126 | `c1-conscience` | CONSCIENCE | school-work | doğru ile yanlışı ayıran iç ses, vicdan | He had a guilty conscience after the lie. | high | feelings / character word |
| 127 | `c1-contempt` | CONTEMPT | school-work | birini değersiz görme, küçümseme duygusu | He looked at the offer with contempt. | high | feelings / character word |
| 128 | `c1-crave` | CRAVE | school-work | bir şeyi aşırı derecede canı çekmek | I always crave something sweet after dinner. | low | borderline fit with feelings |
| 129 | `c1-cynical` | CYNICAL | school-work | insanlara kuşkuyla bakan, her şeye inanmayan | He is cynical about politicians. | high | feelings / character word |
| 130 | `c1-despair` | DESPAIR | school-work | tamamen umudunu yitirme duygusu | She wept in despair at the news. | high | feelings / character word |
| 131 | `c1-dread` | DREAD | school-work | kötü bir şeyden çok korkmak | I dread meeting his family. | high | feelings / character word |
| 132 | `c1-eccentric` | ECCENTRIC | school-work | davranışları sıra dışı ve tuhaf | The eccentric professor wore two different socks. | high | feelings / character word |
| 133 | `c1-empathy` | EMPATHY | school-work | başkasının duygularını anlayabilme | A good nurse shows empathy for patients. | high | feelings / character word |
| 134 | `c1-frantic` | FRANTIC | school-work | korku veya telaştan çılgına dönmüş | She made a frantic search for the keys. | high | feelings / character word |
| 135 | `c1-fury` | FURY | school-work | kontrolsüz büyük öfke | She shouted in fury. | high | feelings / character word |
| 136 | `c1-gratitude` | GRATITUDE | school-work | minnet, teşekkür duygusu | She wrote a letter to show her gratitude. | high | feelings / character word |
| 137 | `c1-grief` | GRIEF | school-work | birinin ölümünden duyulan derin acı | She was overcome by grief. | high | feelings / character word |
| 138 | `c1-gullible` | GULLIBLE | school-work | her söylenene hemen inanıp kanan, saf | Gullible tourists bought fake tickets. | high | feelings / character word |
| 139 | `c1-humiliate` | HUMILIATE | school-work | birini herkesin önünde küçük düşürmek | He did not mean to humiliate her. | high | feelings / character word |
| 140 | `c1-hypocrisy` | HYPOCRISY | school-work | söylediği gibi yaşamayan ikiyüzlülük | People criticised the hypocrisy of the leaders. | high | feelings / character word |
| 141 | `c1-impulse` | IMPULSE | school-work | ani ve düşünmeden gelen istek | She bought the hat on impulse. | high | feelings / character word |
| 142 | `c1-indulge` | INDULGE | school-work | bir zevke kendini kaptırıp şımartmak | Let yourself indulge in a bit of chocolate. | low | borderline fit with feelings |
| 143 | `c1-intimidate` | INTIMIDATE | school-work | korkutarak sindirmek | The big dog may intimidate small children. | low | borderline fit with feelings |
| 144 | `c1-mischief` | MISCHIEF | school-work | çocukça yaramazlık | The kids were up to mischief again. | high | feelings / character word |
| 145 | `c1-outburst` | OUTBURST | school-work | ani öfke veya duygu patlaması | His outburst surprised the whole room. | high | feelings / character word |
| 146 | `c1-outrage` | OUTRAGE | school-work | haksızlığa duyulan öfke | She could not hide her outrage at the decision. | high | feelings / character word |
| 147 | `c1-qualm` | QUALM | school-work | vicdani rahatsızlık, tereddüt | My only qualm is the cost. | high | feelings / character word |
| 148 | `c1-quirk` | QUIRK | school-work | kişiye özgü tuhaf alışkanlık | Her quirk is that she never uses a pen. | high | feelings / character word |
| 149 | `c1-rejoice` | REJOICE | school-work | büyük sevinç duymak | The nation will rejoice at the news. | high | feelings / character word |
| 150 | `c1-resent` | RESENT | school-work | haksızlık sandığı bir şeye içerlemek | She began to resent his comments. | high | feelings / character word |
| 151 | `c1-resilient` | RESILIENT | school-work | zorluklardan çabuk toparlanan | Children are often more resilient than adults. | high | feelings / character word |
| 152 | `c1-squeamish` | SQUEAMISH | school-work | kan veya kötü kokudan hemen tiksinen | I am too squeamish to watch the surgery. | high | feelings / character word |
| 153 | `c1-virtue` | VIRTUE | school-work | ahlaki iyilik; bir şeyin artı yanı | Patience is a virtue. | high | feelings / character word |
| 154 | `c1-whimsical` | WHIMSICAL | school-work | tuhaf, oyunbaz ve ciddiyetten uzak | She has a whimsical sense of humor. | high | feelings / character word |
| 155 | `c1-witty` | WITTY | school-work | zekice ve eğlenceli konuşan | He is a witty speaker. | high | feelings / character word |
| 156 | `c1-yearn` | YEARN | school-work | özlem duymak, çok istemek | He began to yearn for his childhood home. | high | feelings / character word |
| 157 | `c1-zealous` | ZEALOUS | school-work | aşırı hevesli ve tutkulu | A zealous fan waited all night outside. | high | feelings / character word |
| 158 | `c1-zest` | ZEST | school-work | (hayata karşı) heves ve yaşama sevinci | She has a real zest for life. | high | feelings / character word |

### → Toplum ve hukuk (`society-law`)  (105)

#### B1 (34)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 159 | `b1-arrest` | ARREST | daily-life | (polis) birini tutuklamak, gözaltına almak | The police will arrest the thief. | high | law / politics / society word |
| 160 | `b1-burglar` | BURGLAR | daily-life | eve girip eşya çalan hırsız | A burglar broke into the shop. | high | law / politics / society word |
| 161 | `b1-charity` | CHARITY | daily-life | yoksullara yardım eden kuruluş | They gave the money to charity. | low | society / history: fits loosely |
| 162 | `a2-citizen` | CITIZEN | family-people | bir ülkenin vatandaşı | She is a citizen of Spain. | high | law / politics / society word |
| 163 | `b1-civilization` | CIVILIZATION | school-work | gelişmiş bir toplumun kültürü, uygarlık | Ancient Egypt had a great civilization. | low | society / history: fits loosely |
| 164 | `b1-community` | COMMUNITY | family-people | aynı yerde yaşayan insan topluluğu | The whole community helped after the storm. | high | law / politics / society word |
| 165 | `b1-conquer` | CONQUER | school-work | bir ülkeyi savaşla ele geçirmek, fethetmek | The army wanted to conquer the city. | low | society / history: fits loosely |
| 166 | `b1-crime` | CRIME | daily-life | yasaya aykırı suç | Crime is a problem in big cities. | high | law / politics / society word |
| 167 | `b1-criminal` | CRIMINAL | daily-life | suç işleyen kişi | The criminal was caught at the airport. | high | law / politics / society word |
| 168 | `b1-election` | ELECTION | daily-life | oy verilerek yapılan seçim | The election will be held in May. | high | law / politics / society word |
| 169 | `b1-equality` | EQUALITY | school-work | herkese aynı haklar verilmesi, eşitlik | Equality is important in a school. | high | law / politics / society word |
| 170 | `b1-government` | GOVERNMENT | daily-life | bir ülkeyi yöneten siyasi yönetim | The government plans to build new roads. | high | law / politics / society word |
| 171 | `b1-homeless` | HOMELESS | family-people | barınacak yeri olmayan | The homeless man sleeps in the park. | high | law / politics / society word |
| 172 | `b1-illegal` | ILLEGAL | daily-life | yasalara aykırı | It is illegal to park here. | high | law / politics / society word |
| 173 | `b1-immigrant` | IMMIGRANT | family-people | başka ülkeden gelip yerleşen kişi | My grandfather was an immigrant from Greece. | high | law / politics / society word |
| 174 | `b1-innocent` | INNOCENT | daily-life | suçsuz; saf | The court said he was innocent. | low | CSV said feelings. example is a court verdict, not a trait |
| 175 | `a2-judge` | JUDGE | daily-life | mahkemede karar veren kişi | The judge asked the man to stand up. | high | law / politics / society word |
| 176 | `b1-justice` | JUSTICE | daily-life | herkese hak ettiğini veren adil sistem | Everyone wants justice for the victims. | high | law / politics / society word |
| 177 | `b1-kingdom` | KINGDOM | school-work | kral veya kraliçenin yönettiği ülke | Spain is a kingdom in Europe. | low | society / history: fits loosely |
| 178 | `a2-law` | LAW | daily-life | bir ülkede uyulması zorunlu kurallar | Everyone must follow the law. | high | law / politics / society word |
| 179 | `b1-legal` | LEGAL | daily-life | yasalara uygun | It is legal to drive at eighteen. | high | law / politics / society word |
| 180 | `b1-minister` | MINISTER | daily-life | hükümette bir bakanlığı yöneten kişi, bakan | The minister spoke on television. | high | law / politics / society word |
| 181 | `b1-politics` | POLITICS | school-work | ülke yönetimiyle ilgili işler, siyaset | He is not interested in politics. | high | law / politics / society word |
| 182 | `b1-poverty` | POVERTY | daily-life | geçinmek için yeterli paranın olmaması | Many families live in poverty. | high | law / politics / society word |
| 183 | `b1-prejudice` | PREJUDICE | family-people | tanımadan beslenen önyargı | Prejudice against foreigners is wrong. | high | law / politics / society word |
| 184 | `a2-prison` | PRISON | daily-life | suçluların ceza çektiği büyük bina | He was in prison for a year. | high | law / politics / society word |
| 185 | `b1-protest` | PROTEST | daily-life | bir şeye karşı yapılan gösteri | There was a large protest in the city center. | high | law / politics / society word |
| 186 | `b1-religion` | RELIGION | daily-life | inanç sistemi, din | Religion is important to many people. | high | law / politics / society word |
| 187 | `b1-society` | SOCIETY | school-work | birlikte yaşayan insanların oluşturduğu toplum | Society changes over time. | high | law / politics / society word |
| 188 | `b1-theft` | THEFT | daily-life | başkasının malını çalma suçu | The theft happened at night. | high | law / politics / society word |
| 189 | `b1-tradition` | TRADITION | family-people | nesilden nesile geçen âdet | It is a tradition to eat pasta on that day. | low | society / history: fits loosely |
| 190 | `b1-victim` | VICTIM | daily-life | kaza veya suçtan zarar gören kişi | The victim was taken to hospital. | high | law / politics / society word |
| 191 | `a2-vote` | VOTE | daily-life | seçimde tercihini belirtmek | Everyone can vote at eighteen. | high | law / politics / society word |
| 192 | `b1-witness` | WITNESS | daily-life | bir olayı gözüyle görmüş kişi | The witness saw the accident. | high | law / politics / society word |

#### B2 (16)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 193 | `b2-abuse` | ABUSE | daily-life | birine kötü davranma veya bir şeyi kötüye kullanma | Animal abuse is a crime. | high | law / politics / society word |
| 194 | `b2-accuse` | ACCUSE | daily-life | birini suç işlemekle suçlamak | They accuse him of stealing the bike. | high | law / politics / society word |
| 195 | `b2-citizenship` | CITIZENSHIP | daily-life | bir ülkenin vatandaşı olma hakkı | He applied for Turkish citizenship. | high | law / politics / society word |
| 196 | `b2-conflict` | CONFLICT | daily-life | uzun süren anlaşmazlık veya savaş | The conflict lasted for years. | high | law / politics / society word |
| 197 | `b2-dispute` | DISPUTE | daily-life | tarafların uzun süren tartışması | The neighbors had a dispute about the fence. | high | law / politics / society word |
| 198 | `b2-diversity` | DIVERSITY | daily-life | çok farklı türden öğelerin bir arada olması | The city is proud of its cultural diversity. | high | law / politics / society word |
| 199 | `b2-forbid` | FORBID | daily-life | bir şeyi yasaklamak | The law will forbid smoking in schools. | high | law / politics / society word |
| 200 | `b2-fraud` | FRAUD | business | para kazanmak için yapılan dolandırıcılık | He went to prison for fraud. | low | CSV said economy. fraud is a crime (example: prison); not an economy word |
| 201 | `b2-injustice` | INJUSTICE | daily-life | haksızlık, adaletsizlik | We must fight injustice. | high | law / politics / society word |
| 202 | `b2-jury` | JURY | daily-life | mahkemede karar veren halk heyeti | The jury found him not guilty. | high | law / politics / society word |
| 203 | `b2-kidnap` | KIDNAP | daily-life | bir kişiyi zorla kaçırıp alıkoymak | Two men tried to kidnap the businessman outside his hotel. | high | law / politics / society word |
| 204 | `b2-oppose` | OPPOSE | daily-life | bir fikre karşı çıkmak | Many people oppose the new law. | high | law / politics / society word |
| 205 | `b2-orphan` | ORPHAN | daily-life | anne babasını yitirmiş çocuk | The charity helps every orphan find a safe home. | high | law / politics / society word |
| 206 | `b2-rebel` | REBEL | daily-life | düzene karşı gelen kişi, asi | He was a rebel in his teenage years. | high | law / politics / society word |
| 207 | `b2-tribe` | TRIBE | daily-life | ortak kültürü olan geleneksel topluluk | The tribe lives deep in the rainforest and hunts with bows. | high | law / politics / society word |
| 208 | `b2-verdict` | VERDICT | daily-life | mahkemenin kararı | The court reached a verdict. | high | law / politics / society word |

#### C1 (55)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 209 | `c1-abolish` | ABOLISH | school-work | bir yasayı ya da uygulamayı tamamen kaldırmak | Many countries chose to abolish the death penalty. | high | law / politics / society word |
| 210 | `c1-acquit` | ACQUIT | school-work | mahkemede beraat ettirmek | The jury decided to acquit him. | high | law / politics / society word |
| 211 | `c1-allegation` | ALLEGATION | school-work | kanıtsız ileri sürülen suçlama | The mayor denied the allegation. | high | law / politics / society word |
| 212 | `c1-allege` | ALLEGE | school-work | kanıt göstermeden bir şeyi iddia etmek | Neighbors allege that the factory pollutes the river. | high | law / politics / society word |
| 213 | `c1-amend` | AMEND | school-work | metni ya da yasayı düzeltip değiştirmek | Parliament voted to amend the law. | high | law / politics / society word |
| 214 | `c1-asylum` | ASYLUM | school-work | tehlikeden kaçan kişiye verilen sığınma | The family applied for asylum last year. | high | law / politics / society word |
| 215 | `c1-atrocity` | ATROCITY | school-work | korkunç ve vahşi bir şiddet olayı | The army was accused of an atrocity against civilians. | high | law / politics / society word |
| 216 | `c1-backlash` | BACKLASH | school-work | bir olaya karşı sert toplumsal tepki | The new rule caused a public backlash. | high | law / politics / society word |
| 217 | `c1-ballot` | BALLOT | school-work | oy pusulası ya da gizli oylama | The result of the ballot will be known tonight. | high | law / politics / society word |
| 218 | `c1-bribe` | BRIBE | school-work | çıkar sağlamak için verilen rüşvet | The officer refused to take a bribe. | high | law / politics / society word |
| 219 | `c1-bureaucracy` | BUREAUCRACY | business | kâğıt işleri yüzünden yavaşlayan resmî düzen | Getting a permit means dealing with a lot of bureaucracy. | high | law / politics / society word |
| 220 | `c1-condemn` | CONDEMN | school-work | bir şeyi sert biçimde kınamak | Leaders will condemn the attack on the school. | high | law / politics / society word |
| 221 | `c1-confiscate` | CONFISCATE | school-work | yetkili olarak birinin eşyasına el koymak | Teachers can confiscate phones in class. | high | law / politics / society word |
| 222 | `c1-convict` | CONVICT | school-work | mahkemede birini suçlu bulup mahkûm etmek | The jury could not convict him without proof. | high | law / politics / society word |
| 223 | `c1-crackdown` | CRACKDOWN | school-work | suça karşı sert önlem | The police started a crackdown on speeding. | high | law / politics / society word |
| 224 | `c1-decree` | DECREE | school-work | yetkili birinin çıkardığı resmî emir | The king issued a decree against hunting. | high | law / politics / society word |
| 225 | `c1-diplomacy` | DIPLOMACY | school-work | devletler arası ilişkileri yürütme sanatı | The crisis ended through quiet diplomacy. | high | law / politics / society word |
| 226 | `c1-dissent` | DISSENT | school-work | çoğunluktan farklı düşünme, karşı çıkma | The leader did not accept any dissent. | high | law / politics / society word |
| 227 | `c1-extremist` | EXTREMIST | school-work | aşırı uçta görüşleri olan kimse | The attack was blamed on a known extremist. | high | law / politics / society word |
| 228 | `c1-eyewitness` | EYEWITNESS | school-work | olayı gözüyle gören tanık | An eyewitness described the robber. | high | law / politics / society word |
| 229 | `c1-govern` | GOVERN | school-work | bir ülkeyi veya kurumu yönetmek | The party will govern for four years. | high | law / politics / society word |
| 230 | `c1-grassroots` | GRASSROOTS | school-work | halkın tabanı, sıradan insanlar | Support for the plan came from the grassroots. | low | borderline: politics vs general word |
| 231 | `c1-harass` | HARASS | school-work | sürekli rahatsız edip taciz etmek | Reporters continued to harass the family. | low | CSV said feelings. harassment is a legal / social issue |
| 232 | `c1-homeland` | HOMELAND | school-work | insanın doğduğu yurt | She dreams of returning to her homeland. | low | borderline: politics vs general word |
| 233 | `c1-hostage` | HOSTAGE | school-work | rehin tutulan kişi | The hostage was released after two days. | high | law / politics / society word |
| 234 | `c1-humanitarian` | HUMANITARIAN | school-work | insanların acısını azaltmaya yönelik | The charity gives humanitarian aid. | high | law / politics / society word |
| 235 | `c1-indigenous` | INDIGENOUS | school-work | bir bölgenin yerli halkı veya yerli türü | Indigenous people lived here for centuries. | low | borderline: politics vs general word |
| 236 | `c1-inequality` | INEQUALITY | school-work | gelir veya haklar arasındaki eşitsizlik | Income inequality is growing. | high | law / politics / society word |
| 237 | `c1-infrastructure` | INFRASTRUCTURE | business | yol, enerji ve iletişim gibi temel yapılar | The country needs better infrastructure. | low | CSV said economy. roads / energy / communication: public works |
| 238 | `c1-judicial` | JUDICIAL | school-work | mahkemeler ve yargıyla ilgili | The country has an independent judicial system. | high | law / politics / society word |
| 239 | `c1-jurisdiction` | JURISDICTION | school-work | bir mahkemenin yetki alanı | The case falls under federal jurisdiction. | high | law / politics / society word |
| 240 | `c1-lawsuit` | LAWSUIT | business | mahkemede açılan hukuk davası | The firm faces a lawsuit. | high | law / politics / society word |
| 241 | `c1-legislation` | LEGISLATION | school-work | bir konudaki yasaların bütünü, mevzuat | New legislation protects renters. | high | law / politics / society word |
| 242 | `c1-legitimate` | LEGITIMATE | business | yasal ve haklı olan | Is this a legitimate business? | low | borderline: politics vs general word |
| 243 | `c1-liberate` | LIBERATE | school-work | esaretten kurtarmak | Troops came to liberate the town. | high | law / politics / society word |
| 244 | `c1-migrant` | MIGRANT | school-work | iş veya yaşam için yer değiştiren kişi | He came to the city as a migrant looking for work. | high | law / politics / society word |
| 245 | `c1-overthrow` | OVERTHROW | school-work | yönetimi zorla devirmek | Rebels tried to overthrow the king. | high | law / politics / society word |
| 246 | `c1-partisan` | PARTISAN | school-work | tek bir tarafı körü körüne tutan | The newspaper has become more partisan. | high | law / politics / society word |
| 247 | `c1-perpetrator` | PERPETRATOR | school-work | kötü bir eylemi yapan fail | The police are still hunting for the perpetrator. | high | law / politics / society word |
| 248 | `c1-polarise` | POLARIZE | school-work | insanları zıt iki uca bölmek | The issue could polarize the nation. | high | law / politics / society word |
| 249 | `c1-precedent` | PRECEDENT | school-work | sonraki kararlara örnek olan önceki olay | The ruling sets a precedent for future cases. | high | law / politics / society word |
| 250 | `c1-proclaim` | PROCLAIM | school-work | resmen ve açıkça duyurmak | The king will proclaim a holiday. | low | borderline: politics vs general word |
| 251 | `c1-ratify` | RATIFY | school-work | anlaşmayı resmen onaylamak | Parliament must ratify the treaty. | high | law / politics / society word |
| 252 | `c1-regime` | REGIME | school-work | bir ülkeyi yöneten sistem | The old regime fell in 1990. | high | law / politics / society word |
| 253 | `c1-revolt` | REVOLT | school-work | yönetime karşı ayaklanma | A revolt broke out in the north. | high | law / politics / society word |
| 254 | `c1-sanction` | SANCTION | school-work | bir ülkeye uygulanan yaptırım | The country faced a trade sanction last year. | low | borderline: politics vs general word |
| 255 | `c1-sexism` | SEXISM | school-work | cinsiyete dayalı ayrımcılık | The campaign fights sexism at work. | high | law / politics / society word |
| 256 | `c1-solidarity` | SOLIDARITY | school-work | ortak çıkar için dayanışma | Workers marched in solidarity with the strikers. | low | CSV said feelings. example is workers and strikers |
| 257 | `c1-sovereign` | SOVEREIGN | school-work | bağımsız ve kendi kendini yöneten | The island became a sovereign state. | high | law / politics / society word |
| 258 | `c1-testimony` | TESTIMONY | school-work | mahkemede verilen ifade | The witness gave her testimony. | high | law / politics / society word |
| 259 | `c1-unrest` | UNREST | school-work | toplumsal huzursuzluk ve kargaşa | The city suffers from political unrest. | high | law / politics / society word |
| 260 | `c1-uprising` | UPRISING | school-work | yönetime karşı halk ayaklanması | The army crushed the uprising. | high | law / politics / society word |
| 261 | `c1-vandalism` | VANDALISM | school-work | kamu malına kasten zarar verme | The park suffered from vandalism. | high | law / politics / society word |
| 262 | `c1-veto` | VETO | school-work | bir kararı engelleme hakkı | The president used his veto against the bill. | high | law / politics / society word |
| 263 | `c1-warrant` | WARRANT | school-work | polisin arama veya tutuklama izni | Police need a warrant to search the house. | high | law / politics / society word |

### → Para ve ekonomi (`economy`)  (98)

#### B1 (46)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 264 | `b1-account` | ACCOUNT | business | bankada paranın tutulduğu kayıtlı hesap | I opened a new bank account yesterday. | high | money / trade / finance word |
| 265 | `b1-advertise` | ADVERTISE | business | bir ürünü duyurmak için reklam yapmak | They advertise their shop on the radio. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 266 | `b1-brand` | BRAND | business | bir ürünün ticari markası | What brand of shoes do you wear? | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 267 | `b1-budget` | BUDGET | business | harcamalar için ayrılan para | We have a small budget for the party. | high | money / trade / finance word |
| 268 | `b1-charge` | CHARGE | business | bir hizmet için belli bir ücret istemek | The hotel will charge you fifty euros a night. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 269 | `b1-deal` | DEAL | business | iki taraf arasında yapılan anlaşma, pazarlık | We made a good deal on the car. | high | money / trade / finance word |
| 270 | `b1-debt` | DEBT | business | ödenmesi gereken borç | He is in debt to the bank. | high | money / trade / finance word |
| 271 | `b1-demand` | DEMAND | business | bir şeye duyulan güçlü talep | There is a big demand for new houses. | high | money / trade / finance word |
| 272 | `b1-discount` | DISCOUNT | business | fiyattan yapılan indirim | Students get a ten percent discount. | high | money / trade / finance word |
| 273 | `b1-economy` | ECONOMY | business | bir ülkenin üretim ve ticaret düzeni | The economy is growing slowly. | high | money / trade / finance word |
| 274 | `b1-expand` | EXPAND | business | büyüyüp yayılmak, genişlemek | The company wants to expand abroad. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 275 | `b1-export` | EXPORT | business | malı yurt dışına satmak, ihraç etmek | Turkey will export more fruit this year. | high | money / trade / finance word |
| 276 | `b1-factory` | FACTORY | business | ürünlerin makinelerle üretildiği tesis | My father works in a car factory. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 277 | `b1-finance` | FINANCE | business | paranın yönetimi ve parayla ilgili işler | She works in finance. | high | money / trade / finance word |
| 278 | `b1-fortune` | FORTUNE | business | büyük miktarda para; şans | He made a fortune in the oil business. | high | money / trade / finance word |
| 279 | `b1-goods` | GOODS | business | satılan mallar, eşyalar | The shop sells sports goods. | high | money / trade / finance word |
| 280 | `b1-growth` | GROWTH | business | büyüme veya artış | The growth of the town is fast. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 281 | `b1-guarantee` | GUARANTEE | business | bir ürünün bozulmasına karşı verilen güvence | The watch has a two-year guarantee. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 282 | `b1-income` | INCOME | business | düzenli olarak kazanılan para | Her main income comes from teaching. | high | money / trade / finance word |
| 283 | `b1-industry` | INDUSTRY | business | bir ülkedeki üretim ve fabrika sektörü | The car industry employs many people. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 284 | `b1-insurance` | INSURANCE | business | olası zarara karşı yapılan sigorta | Do you have car insurance? | high | money / trade / finance word |
| 285 | `b1-investment` | INVESTMENT | business | kazanç için para yatırma | Buying a house can be a good investment. | high | money / trade / finance word |
| 286 | `b1-loan` | LOAN | business | bankadan alınan borç | He took out a loan to buy a house. | high | money / trade / finance word |
| 287 | `b1-loss` | LOSS | business | kaybetme; bir işte para kaybı, zarar | The company reported a loss this year. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 288 | `b1-marketing` | MARKETING | business | ürünlerin tanıtılıp satılması işi | She works in marketing. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 289 | `b1-owe` | OWE | business | birine borçlu olmak | I owe you ten pounds. | high | money / trade / finance word |
| 290 | `b1-payment` | PAYMENT | business | bir şey için yapılan ödeme | We accept payment by card. | high | money / trade / finance word |
| 291 | `b1-pension` | PENSION | business | emeklilikte alınan aylık maaş | My grandfather lives on his pension. | high | money / trade / finance word |
| 292 | `b1-produce` | PRODUCE | business | bir şeyi üretmek | The farm will produce more milk this year. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 293 | `b1-product` | PRODUCT | business | satılmak üzere üretilen mal | This product is made in Turkey. | high | money / trade / finance word |
| 294 | `b1-production` | PRODUCTION | business | bir şeyin üretilmesi | Car production has fallen this year. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 295 | `b1-profit` | PROFIT | business | satıştan elde edilen kazanç | The shop made a small profit. | high | money / trade / finance word |
| 296 | `b1-property` | PROPERTY | business | bir kişiye ait mal veya mülk | This land is my family's property. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 297 | `b1-receipt` | RECEIPT | business | alışverişte verilen ödeme belgesi, fiş | Keep the receipt in case you need to return it. | high | money / trade / finance word |
| 298 | `b1-refund` | REFUND | business | alınan ürün iade edilince geri verilen para | I asked for a refund. | high | money / trade / finance word |
| 299 | `b1-supply` | SUPPLY | business | ihtiyaç duyulan malzeme stoku | The shop has a large supply of fruit. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 300 | `a2-tax` | TAX | daily-life | devlete ödenen vergi | We pay tax every year. | low | CSV said society-law. money / trade word; tax is paid, not a law |
| 301 | `b1-trade` | TRADE | business | ülkeler veya şirketler arasında mal alım satımı | Trade between the two countries is growing. | high | money / trade / finance word |
| 302 | `b1-unemployed` | UNEMPLOYED | business | işi olmayan, iş arayan | He has been unemployed for a year. | high | money / trade / finance word |
| 303 | `b1-unemployment` | UNEMPLOYMENT | business | çalışmak isteyenin iş bulamaması | Unemployment is high in the north. | high | money / trade / finance word |
| 304 | `b1-value` | VALUE | business | bir şeyin değeri | The value of the house has doubled. | low | general / multi-sense word; example is about money or trade but the sense is broad |
| 305 | `b1-voucher` | VOUCHER | business | mağazada nakit yerine geçen kupon | I got a voucher for a free coffee. | high | money / trade / finance word |
| 306 | `b1-wage` | WAGE | business | saat veya gün başına ödenen ücret | The wage in this shop is low. | high | money / trade / finance word |
| 307 | `b1-warehouse` | WAREHOUSE | business | ürünlerin depolandığı büyük bina | The boxes are in the warehouse. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 308 | `b1-wealth` | WEALTH | business | çok miktarda para ve mal varlığı | Wealth does not always make people kind. | high | money / trade / finance word |
| 309 | `b1-worth` | WORTH | business | belli bir değerde olan, ... eden (fiyat) | This old car is worth about a thousand euros. | low | general / multi-sense word; example is about money or trade but the sense is broad |

#### B2 (21)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 310 | `b2-affordable` | AFFORDABLE | business | fiyatı herkesin ödeyebileceği kadar makul | They sell affordable clothes for students. | low | general word; example is about money or sales |
| 311 | `b2-bankrupt` | BANKRUPT | business | borçlarını ödeyemeyecek duruma gelmiş, iflas etmiş | The company went bankrupt last year. | high | money / trade / finance word |
| 312 | `b2-currency` | CURRENCY | business | bir ülkenin parası | The Turkish currency is the lira. | high | money / trade / finance word |
| 313 | `b2-decline` | DECLINE | business | azalma veya düşüş | There has been a decline in sales. | low | general word; example is about money or sales |
| 314 | `b2-delivery` | DELIVERY | business | bir şeyin adrese getirilmesi | Delivery usually takes two days. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 315 | `b2-expansion` | EXPANSION | business | bir şeyin büyümesi, genişleme | The company plans an expansion abroad. | low | general word; example is about money or sales |
| 316 | `b2-fluctuate` | FLUCTUATE | business | sürekli inip çıkmak, değişkenlik göstermek | Prices fluctuate during the summer. | low | general word; example is about money or sales |
| 317 | `b2-inflation` | INFLATION | business | fiyatların genel olarak artması, enflasyon | Inflation makes life more expensive. | high | money / trade / finance word |
| 318 | `b2-lease` | LEASE | business | uzun süreli yapılan kira sözleşmesi | Our lease ends in June, so we must find a new apartment. | low | general word; example is about money or sales |
| 319 | `b2-manufacture` | MANUFACTURE | business | fabrikada büyük miktarda üretmek | They manufacture cars in this city. | low | general word; example is about money or sales |
| 320 | `b2-mortgage` | MORTGAGE | business | ev almak için bankadan alınan uzun vadeli kredi | They took out a mortgage to buy their first home. | high | money / trade / finance word |
| 321 | `b2-output` | OUTPUT | business | üretilen miktar veya çıktı | The factory doubled its output. | low | general word; example is about money or sales |
| 322 | `b2-packaging` | PACKAGING | business | ürünün ambalajı | The packaging is made of paper. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 323 | `b2-privatize` | PRIVATIZE | business | devlet şirketini özel sektöre devretmek | They plan to privatize the railway. | high | money / trade / finance word |
| 324 | `b2-projection` | PROJECTION | business | gelecek hakkında yapılan hesap; yansıtma | The sales projection looks good for next year. | low | general word; example is about money or sales |
| 325 | `b2-quota` | QUOTA | business | izin verilen ya da hedeflenen pay | Each country has a fishing quota. | low | general word; example is about money or sales |
| 326 | `b2-revenue` | REVENUE | business | bir şirketin geliri | The company's revenue grew by ten percent. | high | money / trade / finance word |
| 327 | `b2-tenant` | TENANT | daily-life | ev sahibine kira ödeyerek oturan kişi | The tenant paid the rent on the first day of every month. | low | CSV said society-law. renting a home: rent is a money word |
| 328 | `b2-turnover` | TURNOVER | business | bir şirketin yıllık satış cirosu | The company's turnover doubled last year. | high | money / trade / finance word |
| 329 | `b2-underpaid` | UNDERPAID | business | emeğinin karşılığından az maaş alan | Nurses are often underpaid for the long hours they work. | high | money / trade / finance word |
| 330 | `b2-venture` | VENTURE | business | riskli yeni girişim | The shop is a new business venture. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |

#### C1 (31)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 331 | `c1-acquisition` | ACQUISITION | business | bir şeyi satın alma veya elde etme | The acquisition cost millions. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 332 | `c1-affiliate` | AFFILIATE | business | büyük bir kuruluşa bağlı şirket ya da kuruluş | The shop is an affiliate of a large chain. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 333 | `c1-allocate` | ALLOCATE | business | kaynağı ya da parayı bir amaca ayırmak | The council will allocate more money to schools. | low | fits economy; check sense |
| 334 | `c1-appraisal` | APPRAISAL | business | bir kişinin ya da şeyin değerlendirilmesi | Every employee has a yearly appraisal. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 335 | `c1-audit` | AUDIT | business | hesapların resmî olarak denetlenmesi | The company passed the annual audit. | high | money / trade / finance word |
| 336 | `c1-boom` | BOOM | business | ekonomide ani ve hızlı yükseliş | The city enjoyed an economic boom in the nineties. | high | money / trade / finance word |
| 337 | `c1-capitalism` | CAPITALISM | business | özel mülkiyete dayalı ekonomik düzen | Many debates focus on the pros and cons of capitalism. | low | CSV said society-law. money / trade word; tax is paid, not a law |
| 338 | `c1-commodity` | COMMODITY | business | alınıp satılan ürün, emtia | Oil is an important global commodity. | high | money / trade / finance word |
| 339 | `c1-deficit` | DEFICIT | business | gelirin giderden az olması, açık | The country has a large budget deficit. | high | money / trade / finance word |
| 340 | `c1-depreciate` | DEPRECIATE | business | değer kaybetmek | A new car will depreciate quickly. | high | money / trade / finance word |
| 341 | `c1-franchise` | FRANCHISE | business | bir markanın adını kullanma hakkı | He bought a franchise of a burger chain. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 342 | `c1-freelance` | FREELANCE | business | belirli bir işverene bağlı olmayan | She works as a freelance writer. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 343 | `c1-incentive` | INCENTIVE | business | bir şeyi yapmaya teşvik eden ödül | Lower taxes are an incentive to invest. | low | fits economy; check sense |
| 344 | `c1-incur` | INCUR | business | istemeden bir masrafa veya zarara uğramak | The company will incur heavy losses. | high | money / trade / finance word |
| 345 | `c1-liquidity` | LIQUIDITY | business | paraya çevrilebilirlik | The bank has strong liquidity. | high | money / trade / finance word |
| 346 | `c1-logistics` | LOGISTICS | business | malların taşınması ve düzenlenmesi | She works in logistics. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 347 | `c1-lucrative` | LUCRATIVE | business | çok kazanç getiren | He left for a more lucrative job abroad. | high | money / trade / finance word |
| 348 | `c1-monopoly` | MONOPOLY | business | bir pazarda tek satıcı olma durumu | The firm has a monopoly on rail travel. | low | CSV said society-law. money / trade word; tax is paid, not a law |
| 349 | `c1-outlet` | OUTLET | business | ürün satış noktası; boşalma yolu | The brand opened a new outlet in the mall. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 350 | `c1-plunge` | PLUNGE | business | aniden dalmak veya sert düşmek | Share prices began to plunge on Monday. | low | fits economy; check sense |
| 351 | `c1-quarterly` | QUARTERLY | business | üç ayda bir yapılan | The firm publishes a quarterly report. | low | fits economy; check sense |
| 352 | `c1-recession` | RECESSION | business | ekonomide geçici gerileme dönemi | The recession cost many jobs. | high | money / trade / finance word |
| 353 | `c1-retail` | RETAIL | business | ürünün tek tek satışı, perakende | He works in retail. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 354 | `c1-soar` | SOAR | business | hızla yükselmek veya havada süzülmek | Prices began to soar last winter. | low | fits economy; check sense |
| 355 | `c1-stakeholder` | STAKEHOLDER | business | bir işte çıkarı olan kişi veya grup | Every stakeholder was invited to the meeting. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 356 | `c1-subsidy` | SUBSIDY | business | devletin verdiği mali destek | Farmers receive a government subsidy. | low | CSV said society-law. money / trade word; tax is paid, not a law |
| 357 | `c1-surge` | SURGE | business | talep veya fiyatta ani sıçrama | There was a surge in demand for masks. | low | fits economy; check sense |
| 358 | `c1-syndicate` | SYNDICATE | business | ortak çıkar için kurulan iş birliği grubu | A syndicate of banks funded the project. | low | company / commerce operations: could equally be İş ve teknoloji, but that category is already over 140 cards; kept in economy |
| 359 | `c1-taxation` | TAXATION | business | vergi uygulama sistemi | Taxation affects every worker. | low | CSV said society-law. money / trade word; tax is paid, not a law |
| 360 | `c1-taxpayer` | TAXPAYER | business | vergi veren kişi | Every taxpayer helps pay for public roads. | low | CSV said society-law. money / trade word; tax is paid, not a law |
| 361 | `c1-volatile` | VOLATILE | business | ani ve öngörülemez biçimde değişen | Oil prices are highly volatile this year. | low | fits economy; check sense |

### → Giysi ve alışveriş (`clothes-shopping`)  (75)

#### A1 (34)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 362 | `a1-belt` | BELT | daily-life | pantolonu belde tutan kemer | Hasan wears a leather belt. | high | clothes / shopping word |
| 363 | `a1-boots` | BOOTS | daily-life | bilekten yukarı çıkan sağlam ayakkabılar | Farmers trudge through the puddles in rubber boots. | high | clothes / shopping word |
| 364 | `a1-buy` | BUY | daily-life | parasını verip almak | I will buy some stamps. | high | clothes / shopping word |
| 365 | `a1-cap` | CAP | daily-life | siperlikli yumuşak başlık | This cap is too tight. | high | clothes / shopping word |
| 366 | `a1-cheap` | CHEAP | daily-life | fiyatı düşük | These pants are cheap, just a few dollars. | high | clothes / shopping word |
| 367 | `a1-clothes` | CLOTHES | daily-life | insanların üzerine giydiği şeyler | She hangs her wet clothes on the line. | high | clothes / shopping word |
| 368 | `a1-coat` | COAT | daily-life | kalın dış giysi | My coat is in the hall. | high | clothes / shopping word |
| 369 | `a1-dollar` | DOLLAR | daily-life | Amerika'nın para birimi | She paid a dollar for it. | high | clothes / shopping word |
| 370 | `a1-dress` | DRESS | daily-life | kadınların giydiği tek parça giysi | She wears a silk dress. | high | clothes / shopping word |
| 371 | `a1-expensive` | EXPENSIVE | daily-life | fiyatı çok yüksek | These shoes are too expensive. | high | clothes / shopping word |
| 372 | `a1-glasses` | GLASSES | daily-life | iyi görmek için takılan çerçeveli camlar | Ali cleaned his glasses. | high | clothes / shopping word |
| 373 | `a1-glove` | GLOVE | daily-life | parmakları saran ve eli koruyan giysi | The goalkeeper wore a padded glove to save penalties. | low | borderline: shopping / money verb or accessory; fits the category loosely |
| 374 | `a1-hat` | HAT | daily-life | başa takılan giysi | My hat is woolen. | high | clothes / shopping word |
| 375 | `a1-jacket` | JACKET | daily-life | ceket, mont | Mert's jacket is thick. | high | clothes / shopping word |
| 376 | `a1-jeans` | JEANS | daily-life | kot pantolon | These jeans are too tight. | high | clothes / shopping word |
| 377 | `a1-money` | MONEY | daily-life | alışverişte ödediğimiz şey | I don't have any money. | high | clothes / shopping word |
| 378 | `a1-pay` | PAY | daily-life | karşılığında para vermek | I pay in cash. | high | clothes / shopping word |
| 379 | `a1-pound` | POUND | daily-life | İngiltere'de kullanılan para birimi | The tickets cost a pound. | high | clothes / shopping word |
| 380 | `a1-price` | PRICE | daily-life | bir şey için ödenecek para miktarı | The price is low. | high | clothes / shopping word |
| 381 | `a1-scarf` | SCARF | daily-life | boyna sarılan uzun kumaş | Ayşe made this scarf. | high | clothes / shopping word |
| 382 | `a1-sell` | SELL | daily-life | malı para karşılığında başkasına vermek | They sell lemons at the stall. | high | clothes / shopping word |
| 383 | `a1-shirt` | SHIRT | daily-life | üst gövdeye giyilen düğmeli giysi | His shirt is wet. | high | clothes / shopping word |
| 384 | `a1-shoe` | SHOE | daily-life | tek ayakkabı | My shoe is wet. | high | clothes / shopping word |
| 385 | `a1-shopping` | SHOPPING | daily-life | mağazalardan bir şeyler satın alma işi | Ali hates shopping. | high | clothes / shopping word |
| 386 | `a1-shorts` | SHORTS | daily-life | yazın giyilen, dizin üstünde biten pantolon | Ali wears shorts for jogging. | high | clothes / shopping word |
| 387 | `a1-size` | SIZE | daily-life | bir şeyin büyüklüğü, beden | My size is 38. | high | clothes / shopping word |
| 388 | `a1-skirt` | SKIRT | daily-life | belden aşağı giyilen kumaş giysi | She wore a pleated skirt and a blouse. | high | clothes / shopping word |
| 389 | `a1-sock` | SOCK | daily-life | ayağa ayakkabıdan önce giyilen yumuşak kumaş | There is a hole in my sock. | high | clothes / shopping word |
| 390 | `a1-suit` | SUIT | daily-life | ceket ve pantolondan oluşan takım elbise | Ali wears a suit. | low | homograph (suit); both examples are the clothing sense |
| 391 | `a1-sweater` | SWEATER | daily-life | yünden örülmüş sıcak tutan üst giysi | Ayşe knitted a sweater. | high | clothes / shopping word |
| 392 | `a1-tie` | TIE | daily-life | gömleğin yakasına bağlanan uzun kumaş | Mr Demir wears a tie. | high | clothes / shopping word |
| 393 | `a1-trousers` | TROUSERS | daily-life | iki bacağı da örten uzun giysi | Ali bought trousers. | high | clothes / shopping word |
| 394 | `a1-wallet` | WALLET | daily-life | para ve kart taşınan küçük cüzdan | Ali lost his wallet. | high | clothes / shopping word |
| 395 | `a1-wear` | WEAR | daily-life | giysiyi üzerine geçirip taşımak | Do you wear contact lenses? | high | clothes / shopping word |

#### A2 (41)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 396 | `a2-bikini` | BIKINI | daily-life | plajda giyilen iki parçalı kadın mayosu | She has a new bikini for the beach. | high | clothes / shopping word |
| 397 | `a2-blouse` | BLOUSE | daily-life | kadınların giydiği hafif gömlek | She wears a white blouse. | high | clothes / shopping word |
| 398 | `a2-bracelet` | BRACELET | daily-life | bileğe takılan süs | She wears a thin bracelet on her arm. | high | clothes / shopping word |
| 399 | `a2-cash` | CASH | daily-life | banknot ve bozuk para olarak elde bulunan para | Do you pay by card or by cash? | high | clothes / shopping word |
| 400 | `a2-coin` | COIN | daily-life | madenden yapılmış bozuk para | I found a coin on the street. | high | clothes / shopping word |
| 401 | `a2-collar` | COLLAR | daily-life | gömleğin boyun çevresindeki yaka kısmı | The collar of this shirt is dirty. | high | clothes / shopping word |
| 402 | `a2-cost` | COST | daily-life | bir şeyin fiyatı kadar olmak | How much does this jacket cost? | high | clothes / shopping word |
| 403 | `a2-cotton` | COTTON | daily-life | bitkiden elde edilen yumuşak kumaş malzemesi | This shirt is made of cotton. | high | clothes / shopping word |
| 404 | `a2-customer` | CUSTOMER | daily-life | dükkândan alışveriş yapan kişi | The customer paid at the desk. | high | clothes / shopping word |
| 405 | `a2-diamond` | DIAMOND | daily-life | çok sert ve değerli, parlak taş | The ring has a small diamond. | low | borderline: shopping / money verb or accessory; fits the category loosely |
| 406 | `a2-earring` | EARRING | daily-life | kulağa takılan süs | Selin lost an earring at the beach. | high | clothes / shopping word |
| 407 | `a2-euro` | EURO | daily-life | bazı Avrupa ülkelerinde kullanılan para birimi | The ticket costs one euro. | high | clothes / shopping word |
| 408 | `a2-fashion` | FASHION | daily-life | giyim tarzındaki geçici moda | Her sister works in the fashion industry. | high | clothes / shopping word |
| 409 | `a2-handbag` | HANDBAG | daily-life | kadınların elde taşıdığı çanta | She lost her handbag on the bus. | high | clothes / shopping word |
| 410 | `a2-jewellery` | JEWELRY | daily-life | yüzük ve kolye gibi süs eşyaları | She keeps her jewelry in a box. | high | clothes / shopping word |
| 411 | `a2-jumper` | JUMPER | daily-life | yünden örülmüş, baştan geçirilen kazak | She wears a red jumper. | high | clothes / shopping word |
| 412 | `a2-jumpsuit` | JUMPSUIT | daily-life | tek parça pantolonlu giysi | Selin wears a blue jumpsuit. | high | clothes / shopping word |
| 413 | `a2-leather` | LEATHER | daily-life | hayvan derisinden yapılan malzeme | These boots are made of leather. | high | clothes / shopping word |
| 414 | `a2-lipstick` | LIPSTICK | daily-life | dudaklara sürülen renkli makyaj ürünü | Her lipstick is red. | low | borderline: shopping / money verb or accessory; fits the category loosely |
| 415 | `a2-necklace` | NECKLACE | daily-life | boyna takılan süs | She wears a long necklace. | high | clothes / shopping word |
| 416 | `a2-outfit` | OUTFIT | daily-life | birlikte giyilen kıyafetler | She chose a blue outfit for the party. | high | clothes / shopping word |
| 417 | `a2-perfume` | PERFUME | daily-life | güzel koku veren sıvı, parfüm | She wears a sweet perfume. | low | borderline: shopping / money verb or accessory; fits the category loosely |
| 418 | `a2-pocket` | POCKET | daily-life | giysilerde küçük eşya konan göz | He put his keys in his pocket. | high | clothes / shopping word |
| 419 | `a2-purse` | PURSE | daily-life | bozuk para koymak için küçük kese | She keeps coins in her purse. | high | clothes / shopping word |
| 420 | `a2-pyjamas` | PYJAMAS | daily-life | gece yatarken giyilen giysi takımı | Ali wears blue pyjamas at night. | high | clothes / shopping word |
| 421 | `a2-raincoat` | RAINCOAT | daily-life | yağmurdan korumak için giyilen su geçirmez mont | Take your raincoat today. | high | clothes / shopping word |
| 422 | `a2-ribbon` | RIBBON | daily-life | süs olarak kullanılan ince şerit kumaş, kurdele | She has a red ribbon in her hair. | low | borderline: shopping / money verb or accessory; fits the category loosely |
| 423 | `a2-sale` | SALE | daily-life | ucuz fiyatla yapılan satış, indirim | There is a sale at the shoe shop. | high | clothes / shopping word |
| 424 | `a2-sandals` | SANDALS | daily-life | yazın giyilen açık ayakkabı, sandalet | I wear sandals in the summer. | high | clothes / shopping word |
| 425 | `a2-size` | SIZE | daily-life | giysinin numarası | Do you have this in a bigger size? | high | clothes / shopping word |
| 426 | `a2-sleeve` | SLEEVE | daily-life | giysinin kolu | The sleeve of my shirt is short. | high | clothes / shopping word |
| 427 | `a2-sneakers` | SNEAKERS | daily-life | spor yapmak için giyilen rahat ayakkabılar | Mert wears white sneakers. | high | clothes / shopping word |
| 428 | `a2-spend` | SPEND | daily-life | para harcamak ya da zaman geçirmek | I spend too much money on books. | low | money or time; example is money |
| 429 | `a2-suit` | SUIT | daily-life | aynı kumaştan ceket ve pantolon takımı | He wears a dark suit at work. | low | homograph (suit); both examples are the clothing sense |
| 430 | `a2-sunglasses` | SUNGLASSES | daily-life | güneşten korunmak için takılan gözlük | Wear your sunglasses today. | high | clothes / shopping word |
| 431 | `a2-sweatshirt` | SWEATSHIRT | daily-life | pamuktan yapılmış kalın spor üst | He wears a gray sweatshirt. | high | clothes / shopping word |
| 432 | `a2-tight` | TIGHT | daily-life | vücuda çok yapışan ya da sıkı | These shoes are too tight. | low | borderline: shopping / money verb or accessory; fits the category loosely |
| 433 | `a2-underwear` | UNDERWEAR | daily-life | giysilerin altına giyilen iç çamaşırı | Take some underwear with you. | high | clothes / shopping word |
| 434 | `a2-vest` | VEST | daily-life | kolsuz iç ya da dış giysi, yelek | Mert wears a vest. | high | clothes / shopping word |
| 435 | `a2-wool` | WOOL | daily-life | koyundan alınan yumuşak lif | This scarf is made of wool. | high | clothes / shopping word |
| 436 | `a2-zip` | ZIP | daily-life | fermuarı çekip kapatmak | Can you zip it, Ali? | low | verb: closing a zip, clothing sense |

### → Hobiler ve eğlence (`hobbies`)  (57)

#### A1 (33)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 437 | `a1-ball` | BALL | daily-life | oyunlarda tepilen ya da atılan yuvarlak nesne | Selin throws the ball. | high | hobby / entertainment word |
| 438 | `a1-balloon` | BALLOON | daily-life | içine hava üflenerek şişirilen renkli lastik | Ayşe is holding a balloon. | low | general word used for play / leisure; fits hobbies loosely |
| 439 | `a1-band` | BAND | daily-life | birlikte müzik çalan grup | The band played for hours. | low | general word used for play / leisure; fits hobbies loosely |
| 440 | `a1-basketball` | BASKETBALL | daily-life | potaya top atılan takım sporu | Ayşe plays basketball with Ali. | high | hobby / entertainment word |
| 441 | `a1-camera` | CAMERA | daily-life | fotoğraf çeken alet | Tourists photographed the castle with a camera. | low | general word used for play / leisure; fits hobbies loosely |
| 442 | `a1-cartoon` | CARTOON | daily-life | çocukların televizyonda izlediği çizgi film | Ali is watching a cartoon. | high | hobby / entertainment word |
| 443 | `a1-climb` | CLIMB | daily-life | eller ve ayaklarla yukarı tırmanmak | Ali can climb the ladder. | low | general word used for play / leisure; fits hobbies loosely |
| 444 | `a1-dance` | DANCE | daily-life | dans etmek | They dance at the festival. | low | general word used for play / leisure; fits hobbies loosely |
| 445 | `a1-doll` | DOLL | daily-life | çocukların oynadığı küçük insan figürü | Selin sewed tiny pajamas for her doll. | high | hobby / entertainment word |
| 446 | `a1-film` | FILM | daily-life | sinemada ya da evde izlenen hikâyeli görüntüler | Ali and I watched a film. | high | hobby / entertainment word |
| 447 | `a1-football` | FOOTBALL | daily-life | ayakla oynanan takım sporu | Football is popular in my area. | high | hobby / entertainment word |
| 448 | `a1-game` | GAME | daily-life | kuralları olan eğlence etkinliği | Mert and Ali finished the chess game quickly. | high | hobby / entertainment word |
| 449 | `a1-guitar` | GUITAR | daily-life | altı telli çalgı | Mert strummed his guitar by the campfire. | high | hobby / entertainment word |
| 450 | `a1-hobby` | HOBBY | daily-life | boş zamanda yapılan sevilen iş | Collecting stamps is Mert's hobby. | high | hobby / entertainment word |
| 451 | `a1-kite` | KITE | daily-life | ipiyle rüzgârda uçurulan hafif oyuncak | Mert's kite is bright. | high | hobby / entertainment word |
| 452 | `a1-magazine` | MAGAZINE | daily-life | düzenli çıkan resimli yayın, dergi | This magazine has colorful pictures. | low | general word used for play / leisure; fits hobbies loosely |
| 453 | `a1-movie` | MOVIE | daily-life | sinema filmi | The movie is a comedy. | high | hobby / entertainment word |
| 454 | `a1-party` | PARTY | daily-life | arkadaşlarla yapılan eğlence | The party was lovely. | high | hobby / entertainment word |
| 455 | `a1-photo` | PHOTO | daily-life | kamerayla çekilen görüntü | Her photo is in my purse. | low | general word used for play / leisure; fits hobbies loosely |
| 456 | `a1-piano` | PIANO | daily-life | tuşlara basılarak çalınan büyük çalgı | She plays the piano. | high | hobby / entertainment word |
| 457 | `a1-play` | PLAY | daily-life | eğlenmek için bir oyuna katılmak | Do you play chess? | low | general word used for play / leisure; fits hobbies loosely |
| 458 | `a1-player` | PLAYER | daily-life | bir oyunu ya da sporu oynayan kişi | Ali is the best player in his team. | low | general word used for play / leisure; fits hobbies loosely |
| 459 | `a1-ride` | RIDE | daily-life | hayvanın ya da aracın üstünde gitmek | Can you ride a scooter? | low | general word used for play / leisure; fits hobbies loosely |
| 460 | `a1-robot` | ROBOT | daily-life | insan yerine iş yapan mekanik makine | The tiny robot is made of metal. | low | general word used for play / leisure; fits hobbies loosely |
| 461 | `a1-sing` | SING | daily-life | şarkı söylemek | She can sing in a choir. | low | general word used for play / leisure; fits hobbies loosely |
| 462 | `a1-soccer` | SOCCER | daily-life | Amerikan İngilizcesinde futbol | Mert plays soccer with Ali. | low | general word used for play / leisure; fits hobbies loosely |
| 463 | `a1-sport` | SPORT | daily-life | beden gücüyle yapılan yarışma oyunu | Rugby is a popular sport. | low | general word used for play / leisure; fits hobbies loosely |
| 464 | `a1-stamp` | STAMP | daily-life | mektubun üstüne yapıştırılan küçük kâğıt | This stamp is rare. | low | general word used for play / leisure; fits hobbies loosely |
| 465 | `a1-swim` | SWIM | daily-life | suda kollarla ilerlemek | Ducks can swim in the pond. | low | general word used for play / leisure; fits hobbies loosely |
| 466 | `a1-tennis` | TENNIS | daily-life | raketle file üzerinden oynanan spor | Ali plays tennis. | high | hobby / entertainment word |
| 467 | `a1-toy` | TOY | daily-life | çocukların oynadığı eşya | This toy is for children. | high | hobby / entertainment word |
| 468 | `a1-video` | VIDEO | daily-life | hareketli görüntü kaydı | Let's see the video. | low | general word used for play / leisure; fits hobbies loosely |
| 469 | `a1-violin` | VIOLIN | daily-life | yay ile çalınan dört telli müzik aleti | Ayşe plays the violin. | high | hobby / entertainment word |

#### A2 (19)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 470 | `a2-alien` | ALIEN | daily-life | başka gezegenden geldiği düşünülen yaratık | The film is about an alien. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 471 | `a2-chess` | CHESS | daily-life | iki kişinin satranç tahtasında oynadığı oyun | My grandfather teaches me chess. | high | hobby / entertainment word |
| 472 | `a2-circus` | CIRCUS | daily-life | palyaço ve akrobatların gösteri yaptığı çadır | The children love the circus. | high | hobby / entertainment word |
| 473 | `a2-clown` | CLOWN | daily-life | renkli giyip insanları güldüren sirk sanatçısı | The clown made the children laugh. | high | hobby / entertainment word |
| 474 | `a2-collect` | COLLECT | daily-life | bir şeyleri biriktirmek ya da toplamak | He likes to collect stamps. | high | hobby / entertainment word |
| 475 | `a2-collection` | COLLECTION | daily-life | bir araya getirilmiş eşyalar topluluğu | He has a big stamp collection. | high | hobby / entertainment word |
| 476 | `a2-dragon` | DRAGON | daily-life | ateş püskürten, masallardaki kanatlı canavar | The dragon lives on a mountain. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 477 | `a2-fairy` | FAIRY | daily-life | masallarda sihirli güçleri olan küçük kanatlı kız | The fairy waved her wand. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 478 | `a2-fireworks` | FIREWORKS | daily-life | gece gökyüzünde patlayan renkli gösteri | We watched the fireworks on New Year. | high | hobby / entertainment word |
| 479 | `a2-ghost` | GHOST | daily-life | ölmüş birinin ruhu olduğuna inanılan görüntü | The children ran away from the ghost. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 480 | `a2-giant` | GIANT | daily-life | masallardaki kocaman yaratık, dev | The giant in the story lived on a hill. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 481 | `a2-horror` | HORROR | daily-life | korku türü (film, kitap) | I do not like horror films. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 482 | `a2-magic` | MAGIC | daily-life | olağanüstü şeyler yapan gizemli güç, sihir | The story is full of magic. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 483 | `a2-mask` | MASK | daily-life | yüze takılan, kimliği gizleyen örtü | He wore a mask at the party. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 484 | `a2-monster` | MONSTER | daily-life | korkunç ve garip görünümlü masal yaratığı, canavar | There is a monster in the story. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |
| 485 | `a2-puppet` | PUPPET | daily-life | iplerle ya da elle oynatılan oyuncak bebek, kukla | The children watch a puppet show. | high | hobby / entertainment word |
| 486 | `a2-puzzle` | PUZZLE | daily-life | parçaları birleştirilerek çözülen oyun | This puzzle has a hundred pieces. | high | hobby / entertainment word |
| 487 | `a2-swing` | SWING | daily-life | çocuk parkında iki ipten sarkan salıncak | The child plays on the swing. | high | hobby / entertainment word |
| 488 | `a2-zombie` | ZOMBIE | daily-life | filmlerde ölüden dirilen korkunç yaratık | The zombie in the film is ugly. | low | story / fantasy creature: entertainment (films, tales), loosely a hobby |

#### B1 (3)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 489 | `b1-gardening` | GARDENING | daily-life | bahçede bitki yetiştirme işi | Gardening is my father's hobby. | high | hobby / entertainment word |
| 490 | `b1-jigsaw` | JIGSAW | daily-life | küçük parçaları birleştirerek resim yapılan oyun | We finished the jigsaw in two hours. | high | hobby / entertainment word |
| 491 | `b1-leisure` | LEISURE | daily-life | boş zaman | What do you do in your leisure time? | high | hobby / entertainment word |

#### B2 (2)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 492 | `b2-gamble` | GAMBLE | daily-life | şansa güvenip para oynamak, kumar oynamak | He lost his savings when he began to gamble at the casino. | low | gambling as a pastime; also a crime / money word |
| 493 | `b2-knit` | KNIT | daily-life | yün ipliğini şişle örmek | My grandmother used to knit scarves for the whole family. | high | hobby / entertainment word |

### → Hayvanlar ve doğa (`animals`, B1 only)  (14)

#### B1 (14)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 494 | `b1-blizzard` | BLIZZARD | daily-life | çok şiddetli kar fırtınası | The blizzard closed all the roads. | high | nature word |
| 495 | `b1-cactus` | CACTUS | daily-life | çölde yetişen dikenli bitki | The cactus needs very little water. | low | garden / plant words: fit "nature" loosely |
| 496 | `b1-climate` | CLIMATE | daily-life | bir bölgenin uzun süreli hava koşulları | Turkey has a warm climate. | high | nature word |
| 497 | `b1-environment` | ENVIRONMENT | daily-life | doğayı da kapsayan yaşam çevresi | We must save the environment. | high | nature word |
| 498 | `a2-flood` | FLOOD | daily-life | suyun kara üstüne taşması, sel | The flood covered the road. | high | nature word |
| 499 | `b1-greenhouse` | GREENHOUSE | daily-life | bitki yetiştirilen camdan yapılmış sıcak yapı | We grow tomatoes in the greenhouse. | low | garden / plant words: fit "nature" loosely |
| 500 | `a2-hurricane` | HURRICANE | daily-life | çok şiddetli tropik fırtına, kasırga | The hurricane came to the island. | high | nature word |
| 501 | `b1-jasmine` | JASMINE | daily-life | beyaz ve çok güzel kokulu bir çiçek | The garden smells of jasmine in summer. | low | garden / plant words: fit "nature" loosely |
| 502 | `b1-lawn` | LAWN | daily-life | evin önündeki kısa kesilmiş çimenlik | He cuts the lawn every Sunday. | low | garden / plant words: fit "nature" loosely |
| 503 | `b1-orchard` | ORCHARD | daily-life | meyve ağaçlarının dikildiği bahçe | They pick apples in the orchard. | low | garden / plant words: fit "nature" loosely |
| 504 | `b1-pollution` | POLLUTION | daily-life | hava, su ve toprağın kirlenmesi | Air pollution is a problem in big cities. | high | nature word |
| 505 | `b1-recycle` | RECYCLE | daily-life | kullanılmış maddeleri yeniden kullanıma kazandırmak | We recycle paper and glass at home. | low | garden / plant words: fit "nature" loosely |
| 506 | `b1-thunderstorm` | THUNDERSTORM | daily-life | şimşek, gök gürültüsü ve yağmurlu fırtına | We stayed inside during the thunderstorm. | high | nature word |
| 507 | `b1-tulip` | TULIP | daily-life | soğanından yetişen, Hollanda ile anılan bahar çiçeği | A red tulip grew in the garden. | high | nature word |

### → Fikirler ve kavramlar (`ideas`, nature words of B2/C1)  (21)

#### B2 (12)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 508 | `b2-blaze` | BLAZE | daily-life | büyük, şiddetli yangın | Firefighters fought the blaze all night. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 509 | `b2-crystal` | CRYSTAL | daily-life | şeffaf, sert mineral parçası; kristal | The glass looks like crystal. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 510 | `b2-extinct` | EXTINCT | daily-life | soyu tükenmiş | Dinosaurs are extinct. | high | nature word (to ideas, animals ends at B1) |
| 511 | `b2-hazy` | HAZY | daily-life | sisli veya belirsiz | The morning was hazy. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 512 | `b2-hedge` | HEDGE | daily-life | bahçeyi çeviren, düzenli kesilen sık çalı sırası | A tall hedge separates our garden from the road. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 513 | `b2-meadow` | MEADOW | daily-life | çiçekli otlarla kaplı açık arazi; çayır | Wildflowers covered the meadow behind the old farmhouse. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 514 | `b2-orchid` | ORCHID | daily-life | gösterişli çiçekleriyle bilinen, saksıda yetişen bitki | The orchid on the windowsill blooms once a year. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 515 | `b2-pollute` | POLLUTE | daily-life | çevreyi kirletmek | Factories pollute the river. | high | nature word (to ideas, animals ends at B1) |
| 516 | `b2-quake` | QUAKE | daily-life | isim: yer sarsıntısı, "earthquake"in kısa hali | The quake shook the whole region at dawn. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 517 | `b2-topaz` | TOPAZ | daily-life | sarı renkli değerli bir taş | The museum displayed a huge topaz found in the mountains. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 518 | `b2-twilight` | TWILIGHT | daily-life | güneş battıktan sonraki loş ışık; alacakaranlık | The mountains turned purple in the twilight. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |
| 519 | `b2-zinc` | ZINC | daily-life | paslanmayı önleyen gümüşi mavi metal | Workers coat the steel with zinc to protect it from rust. | low | nature word moved to ideas per owner decision (animals ends at B1); weak topical fit, check |

#### C1 (9)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 520 | `c1-biodiversity` | BIODIVERSITY | school-work | bir bölgedeki canlı türlerinin çeşitliliği | Forests protect the biodiversity of the region. | high | nature word (to ideas, animals ends at B1) |
| 521 | `c1-breed` | BREED | school-work | hayvan yetiştirmek veya üretmek | Farmers here breed horses for racing. | low | homograph (breed): example is horses = animals sense |
| 522 | `c1-contaminate` | CONTAMINATE | school-work | bir şeyi zararlı maddeyle kirletmek | Waste can contaminate the drinking water. | high | nature word (to ideas, animals ends at B1) |
| 523 | `c1-deplete` | DEPLETE | school-work | kaynakları azaltıp tüketmek | Overfishing may deplete the stock of fish. | low | resource use (fish stock): ideas, borderline |
| 524 | `c1-ecosystem` | ECOSYSTEM | school-work | canlıların ve çevrenin oluşturduğu doğal bütün | Pollution can harm the whole ecosystem of a lake. | high | nature word (to ideas, animals ends at B1) |
| 525 | `c1-erode` | ERODE | school-work | su veya rüzgârla yavaşça aşınmak | Rain will erode the soil on the hill. | high | nature word (to ideas, animals ends at B1) |
| 526 | `c1-fertile` | FERTILE | school-work | toprak verimli, bol ürün veren | The river valley has fertile soil. | high | nature word (to ideas, animals ends at B1) |
| 527 | `c1-nocturnal` | NOCTURNAL | school-work | (hayvan) geceleri etkin olan | Owls are nocturnal birds. | high | nature word (to ideas, animals ends at B1) |
| 528 | `c1-offspring` | OFFSPRING | school-work | bir canlının yavruları veya çocukları | The bird protects its offspring. | high | nature word (to ideas, animals ends at B1) |

### → keep: no stage-2 move (stage 3 decides)  (94)

#### A1 (2)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 529 | `a1-paint` | PAINT | daily-life | fırça ya da rulo ile boya sürmek | Mert will paint the fence. | low | CSV said hobbies; keep in daily-life. example is painting a fence (a chore), not art |
| 530 | `a1-top` | TOP | daily-life | bir şeyin en üst kısmı | Ali is at the top of the ladder. | low | CSV said clothes-shopping; keep in daily-life. example is the top of a ladder, not clothing |

#### A2 (2)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 531 | `a2-lend` | LEND | daily-life | geri almak üzere birine bir şey vermek | Can you lend me a pencil? | low | CSV said clothes-shopping; keep in daily-life. lending is not shopping; stage 3 (verbs) |
| 532 | `a2-style` | STYLE | daily-life | bir şeyin kendine özgü tarzı | I like your style. | low | CSV said clothes-shopping; keep in daily-life. "style" is general (example: your style) |

#### B1 (23)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 533 | `b1-aware` | AWARE | daily-life | bir durumun farkında olan | Are you aware of the problem? | low | CSV said feelings; keep in daily-life. general adjective, not an emotion or trait; stage 3 (adjectives) |
| 534 | `b1-brilliant` | BRILLIANT | school-work | çok zeki veya harika | She is a brilliant scientist. | low | CSV said feelings; keep in school-work. general adjective, not an emotion or trait; stage 3 (adjectives) |
| 535 | `b1-bulb` | BULB | daily-life | elektrikle ışık veren cam lamba | The bulb in the kitchen is broken. | low | CSV said animals; keep in daily-life. electric light bulb / colour, not nature |
| 536 | `b1-emergency` | EMERGENCY | daily-life | hemen yardım gerektiren ani tehlike | Call 112 in an emergency. | low | CSV said society-law; keep in daily-life. not clearly law or society |
| 537 | `b1-gain` | GAIN | business | zamanla deneyim, beceri ya da kilo kazanmak | You can gain skills by working here. | low | CSV said economy; keep in business. example is gaining skills or weight, not money |
| 538 | `b1-happily` | HAPPILY | family-people | mutlu bir şekilde | They lived happily in a small house. | low | CSV said feelings; keep in family-people. adverb of manner; stage 3 (adjectives and adverbs) |
| 539 | `b1-honestly` | HONESTLY | daily-life | dürüstçe, açıkçası | Honestly, I did not like the film. | low | CSV said feelings; keep in daily-life. adverb of manner; stage 3 (adjectives and adverbs) |
| 540 | `b1-increase` | INCREASE | business | artmak veya artırmak | Prices increase every year. | low | CSV said economy; keep in business. general verb (prices increase) |
| 541 | `b1-independent` | INDEPENDENT | family-people | başkasına bağlı olmayan, bağımsız | She wants to be independent from her parents. | low | CSV said feelings; keep in family-people. general adjective, not an emotion or trait; stage 3 (adjectives) |
| 542 | `b1-joker` | JOKER | daily-life | esprili şaka yapmayı seven kişi | Our uncle is a real joker at parties. | low | CSV said hobbies; keep in daily-life. not a hobby word (joker = funny person; juggle = manage tasks; novelty = newness) |
| 543 | `b1-military` | MILITARY | daily-life | orduyla ilgili | He did his military service last year. | low | CSV said society-law; keep in daily-life. not clearly law or society |
| 544 | `b1-negative` | NEGATIVE | daily-life | olumsuz, kötü yönde | Try not to be so negative. | low | CSV said feelings; keep in daily-life. general adjective, not an emotion or trait; stage 3 (adjectives) |
| 545 | `b1-nightmare` | NIGHTMARE | daily-life | korkutucu kötü rüya | I had a nightmare last night. | low | CSV said feelings; keep in daily-life. a dream, not a feeling word |
| 546 | `b1-positive` | POSITIVE | daily-life | olumlu, iyimser | She feels positive about life. | low | CSV said feelings; keep in daily-life. general adjective, not an emotion or trait; stage 3 (adjectives) |
| 547 | `b1-quantity` | QUANTITY | business | sayılan ya da ölçülen nicelik | A large quantity of rice was sold. | low | CSV said economy; keep in business. general measure noun |
| 548 | `b1-rate` | RATE | business | bir şeyin ölçüsü veya oranı | The rate of people without jobs is falling. | low | CSV said economy; keep in business. general measure noun (unemployment rate) |
| 549 | `b1-sensible` | SENSIBLE | daily-life | mantıklı ve akıllıca davranan | It is sensible to wear a helmet. | low | CSV said feelings; keep in daily-life. Yalancı eşdeğerler card: "sensible" = reasonable/judgement, an adjective rather than a feeling; stage 3 word-class rule |
| 550 | `b1-threat` | THREAT | daily-life | zarar verme ihtimali olan tehlike | The storm is a threat to the village. | low | CSV said society-law; keep in daily-life. not clearly law or society |
| 551 | `b1-uncomfortable` | UNCOMFORTABLE | daily-life | rahatsız hissettiren, rahat olmayan | This chair is very uncomfortable. | low | CSV said feelings; keep in daily-life. general adjective, not an emotion or trait; stage 3 (adjectives) |
| 552 | `b1-unpleasant` | UNPLEASANT | daily-life | hoşa gitmeyen, nahoş | There was an unpleasant smell in the room. | low | CSV said feelings; keep in daily-life. general adjective, not an emotion or trait; stage 3 (adjectives) |
| 553 | `b1-violet` | VIOLET | daily-life | mora yakın, açık mor renk | The wall was painted a soft violet colour. | low | CSV said animals; keep in daily-life. electric light bulb / colour, not nature |
| 554 | `b1-weapon` | WEAPON | daily-life | saldırı veya savunmada kullanılan silah | The police found a weapon in the car. | low | CSV said society-law; keep in daily-life. not clearly law or society |
| 555 | `b1-weird` | WEIRD | daily-life | tuhaf, garip | I had a weird dream last night. | low | CSV said feelings; keep in daily-life. general adjective, not an emotion or trait; stage 3 (adjectives) |

#### B2 (35)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 556 | `b2-asset` | ASSET | business | bir kişi veya şirketin değerli varlığı | Her language skills are a big asset. | low | CSV said economy; keep in business. example is language skills, not finance |
| 557 | `b2-blend` | BLEND | food-drink | birkaç şeyi karıştırıp birleştirmek | Blend the fruit with milk. | high | CSV said food-drink; keep in food-drink. real food word: stays in food-drink (owner decision: not folded into body-health) |
| 558 | `b2-compliment` | COMPLIMENT | daily-life | birine söylenen hoş söz, iltifat | He gave me a nice compliment. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 559 | `b2-confess` | CONFESS | daily-life | yapılan hatayı itiraf etmek | He will confess that he broke the window. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 560 | `b2-confront` | CONFRONT | daily-life | bir sorunla veya kişiyle yüz yüze gelmek | You must confront your fear. | low | CSV said society-law; keep in daily-life. example is not about law or society (fear, handshake, job offer, house) |
| 561 | `b2-conscious` | CONSCIOUS | daily-life | bir şeyin farkında olan | She was conscious of the noise. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 562 | `b2-convention` | CONVENTION | daily-life | toplumun kabul ettiği gelenek; büyük toplantı | It is a social convention to shake hands. | low | CSV said society-law; keep in daily-life. example is not about law or society (fear, handshake, job offer, house) |
| 563 | `b2-distribute` | DISTRIBUTE | business | bir şeyi insanlara paylaştırmak | They distribute food to poor families. | low | CSV said economy; keep in business. general verb |
| 564 | `b2-eel` | EEL | daily-life | yılana benzeyen, uzun ve kaygan nehir balığı | The eel slipped through his fingers and back into the river. | low | CSV said ideas; keep in daily-life. an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 565 | `b2-estimate` | ESTIMATE | business | yaklaşık olarak hesaplamak | Can you estimate the cost of the trip? | low | CSV said economy; keep in business. general verb |
| 566 | `b2-gallon` | GALLON | food-drink | yaklaşık dört litre ölçü birimi | The old car uses a gallon of fuel every thirty miles. | low | CSV said food-drink; keep in food-drink. stays in food-drink for now; example is fuel / candy wrapper, not food (fate in stage 3) |
| 567 | `b2-graceful` | GRACEFUL | daily-life | hareketleri akıcı ve narin | The dancer's graceful movements amazed the audience. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 568 | `b2-grumble` | GRUMBLE | daily-life | homurdanarak şikâyet etmek | He likes to grumble about cold coffee. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 569 | `b2-herbal` | HERBAL | food-drink | bitkilerden elde edilen (çay, ilaç) | She drinks herbal tea before bed. | high | CSV said food-drink; keep in food-drink. real food word: stays in food-drink (owner decision: not folded into body-health) |
| 570 | `b2-hesitate` | HESITATE | daily-life | karar verirken duraksamak | Don't hesitate to call me. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 571 | `b2-ignorant` | IGNORANT | daily-life | bilgisiz, haberi olmayan | He is ignorant of the rules. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 572 | `b2-jackal` | JACKAL | daily-life | kurda benzeyen, leşle beslenen yabani hayvan | A jackal howled somewhere in the dark, far from the camp. | low | CSV said ideas; keep in daily-life. an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 573 | `b2-juggle` | JUGGLE | daily-life | birkaç işi aynı anda yetiştirmeye çalışmak | She has to juggle work and school. | low | CSV said hobbies; keep in daily-life. not a hobby word (joker = funny person; juggle = manage tasks; novelty = newness) |
| 574 | `b2-lobster` | LOBSTER | food-drink | büyük pençeli, kırmızı kabuklu deniz canlısı | We ordered grilled lobster at a restaurant by the sea. | high | CSV said food-drink; keep in food-drink. real food word: stays in food-drink (owner decision: not folded into body-health) |
| 575 | `b2-mansion` | MANSION | daily-life | bahçeli, çok büyük ve lüks ev; konak | A security guard patrols the mansion at night. | low | CSV said society-law; keep in daily-life. example is not about law or society (fear, handshake, job offer, house) |
| 576 | `b2-migrate` | MIGRATE | daily-life | mevsimsel ya da toplu olarak göç etmek | Birds migrate south in winter. | low | CSV said society-law; keep in daily-life. example is birds migrating (nature, not society) |
| 577 | `b2-mozzarella` | MOZZARELLA | food-drink | pizzada eriyen İtalyan beyaz peyniri | The chef recommended fresh mozzarella with tomatoes and basil. | high | CSV said food-drink; keep in food-drink. real food word: stays in food-drink (owner decision: not folded into body-health) |
| 578 | `b2-mumble` | MUMBLE | daily-life | anlaşılmayacak kadar alçak sesle konuşmak | Please do not mumble; I cannot hear a word you say. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 579 | `b2-nibble` | NIBBLE | food-drink | ufak ufak ısırarak yemek | Guests like to nibble on olives while they wait for dinner. | high | CSV said food-drink; keep in food-drink. real food word: stays in food-drink (owner decision: not folded into body-health) |
| 580 | `b2-novelty` | NOVELTY | daily-life | yeni olmanın verdiği ilginçlik | The novelty of the toy soon wore off. | low | CSV said hobbies; keep in daily-life. not a hobby word (joker = funny person; juggle = manage tasks; novelty = newness) |
| 581 | `b2-nutrient` | NUTRIENT | food-drink | vücudun beslenmek için ihtiyaç duyduğu besin öğesi | This food has an important nutrient. | high | CSV said food-drink; keep in food-drink. food / health word: stays in food-drink |
| 582 | `b2-organic` | ORGANIC | food-drink | kimyasal gübre kullanılmadan yetiştirilen | We buy organic vegetables from the farmers' market. | high | CSV said food-drink; keep in food-drink. real food word: stays in food-drink (owner decision: not folded into body-health) |
| 583 | `b2-otter` | OTTER | daily-life | suda yüzen kahverengi kürklü hayvan | An otter carried a fish up onto the river bank. | low | CSV said ideas; keep in daily-life. an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 584 | `b2-oyster` | OYSTER | food-drink | kabuğunun içinde inci yapabilen deniz hayvanı | The pearl was hidden inside a single oyster. | high | CSV said food-drink; keep in food-drink. real food word: stays in food-drink (owner decision: not folded into body-health) |
| 585 | `b2-rejection` | REJECTION | daily-life | bir teklifin reddedilmesi | He could not accept the rejection. | low | CSV said society-law; keep in daily-life. example is not about law or society (fear, handshake, job offer, house) |
| 586 | `b2-tortoise` | TORTOISE | daily-life | sırtında kabuğu olan yavaş hayvan | A tortoise can live for more than a hundred years. | low | CSV said ideas; keep in daily-life. an animal word: Hayvanlar ve doğa ends at B1 and "Fikirler ve kavramlar" would hide it; stays in its category until the word-class stage |
| 587 | `b2-unaware` | UNAWARE | daily-life | bir şeyden haberi olmayan | He was unaware of the danger. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 588 | `b2-unbearable` | UNBEARABLE | daily-life | katlanılamayacak kadar kötü | The heat in the tiny room was unbearable. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 589 | `b2-warmth` | WARMTH | daily-life | sıcaklık veya sıcakkanlılık | I enjoyed the warmth of the fire. | low | CSV said feelings; keep in daily-life. speech act / movement / general adjective, not a feeling; stage 3 word class |
| 590 | `b2-wrapper` | WRAPPER | food-drink | şeker ya da yiyecek paketinin dış kâğıdı | Please put the candy wrapper in the bin. | low | CSV said food-drink; keep in food-drink. stays in food-drink for now; example is fuel / candy wrapper, not food (fate in stage 3) |

#### C1 (32)

| # | id | answer | now | clue | example | conf | reason |
|--:|---|---|---|---|---|---|---|
| 591 | `c1-absurd` | ABSURD | school-work | saçma ve mantıksız | It is absurd to pay so much for a small cup of tea. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 592 | `c1-advocate` | ADVOCATE | school-work | bir fikri açıkça savunmak ve desteklemek | Doctors advocate a healthy diet for everyone. | low | CSV said society-law; keep in school-work. example is doctors and diet |
| 593 | `c1-benchmark` | BENCHMARK | business | başkalarını karşılaştırmak için ölçüt | The test sets a benchmark for the whole industry. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 594 | `c1-benign` | BENIGN | school-work | zararsız, kötü huylu olmayan | The doctor said the lump was benign. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 595 | `c1-brutal` | BRUTAL | school-work | acımasız ve çok sert | The winter was brutal for the animals. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 596 | `c1-bulk` | BULK | business | bir şeyin büyük kısmı veya yığın hacmi | The bulk of the work is already finished. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 597 | `c1-capitalize` | CAPITALIZE | business | fırsattan yararlanmak | They hope to capitalize on the success. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 598 | `c1-consolidate` | CONSOLIDATE | business | pekiştirmek; birkaç şeyi tek yapıda birleştirmek | The firm plans to consolidate its small offices. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 599 | `c1-daunting` | DAUNTING | school-work | korkutucu ve gözünü yıldıran | The exam seemed a daunting task. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 600 | `c1-discreet` | DISCREET | school-work | dikkatli ve gizliliğe saygılı | Please be discreet about this matter. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 601 | `c1-equity` | EQUITY | business | hakkaniyet, fırsat eşitliği; şirket hissesi | The group works for equity in education. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 602 | `c1-eradicate` | ERADICATE | school-work | bir şeyi kökünden yok etmek | The country hopes to eradicate the disease. | low | CSV said ideas; keep in school-work. example is eradicating a disease; stage 3 |
| 603 | `c1-famine` | FAMINE | school-work | yaygın kıtlık ve açlık | The famine forced thousands to leave the region. | low | CSV said society-law; keep in school-work. hunger / disaster; nature or society, no clear home |
| 604 | `c1-grim` | GRIM | school-work | korkunç, ümitsiz ve kasvetli | The doctor had grim news for the family. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 605 | `c1-leverage` | LEVERAGE | business | bir kişiye üstünlük sağlayan güç | His fame gave him leverage in the talks. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 606 | `c1-liability` | LIABILITY | business | yasal olarak üstlenilen sorumluluk | The company accepts no liability for lost items. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 607 | `c1-marginal` | MARGINAL | business | çok küçük, önemsiz düzeyde olan | There was only a marginal improvement in sales. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 608 | `c1-newcomer` | NEWCOMER | school-work | bir yere yeni gelen kişi | As a newcomer, she felt lost. | low | CSV said society-law; keep in school-work. "newcomer" is a general noun (example: feeling lost) |
| 609 | `c1-nominal` | NOMINAL | business | çok düşük, sembolik düzeyde | A nominal fee is charged for entry. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 610 | `c1-nonprofit` | NONPROFIT | business | kâr amacı gütmeyen | She works for a nonprofit group. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 611 | `c1-offset` | OFFSET | business | bir etkiyi dengeleyerek azaltmak | Savings will offset the higher rent. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 612 | `c1-outspoken` | OUTSPOKEN | school-work | düşüncesini açıkça söyleyen | She is an outspoken critic of the plan. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 613 | `c1-pledge` | PLEDGE | school-work | söz verip taahhüt etmek | The mayor will pledge more money for parks. | low | CSV said society-law; keep in school-work. general verb; example is a mayor and money |
| 614 | `c1-relentless` | RELENTLESS | school-work | durmayan, amansız | The relentless rain lasted three days. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 615 | `c1-sentiment` | SENTIMENT | school-work | bir konudaki yaygın duygu veya görüş | Public sentiment turned against the plan. | low | CSV said feelings; keep in school-work. means public opinion, an abstract noun |
| 616 | `c1-tangible` | TANGIBLE | business | elle tutulur, somut | We need tangible proof of the progress. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 617 | `c1-tedious` | TEDIOUS | school-work | bitmek bilmeyen, tekdüze ve sıkıcı (iş) | Filling in forms is tedious. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 618 | `c1-textile` | TEXTILE | business | dokuma kumaş | Cotton is the main textile made in the town. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 619 | `c1-timescale` | TIMESCALE | business | bir işin planlanan süresi | We need a clear timescale for the work. | low | CSV said economy; keep in business. general or multi-sense word; stage 3 |
| 620 | `c1-unconditional` | UNCONDITIONAL | school-work | hiçbir şart koşulmayan | The team gave unconditional support. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 621 | `c1-vigilant` | VIGILANT | school-work | tehlikeye karşı tetikte | Drivers must stay vigilant at night. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |
| 622 | `c1-youthful` | YOUTHFUL | school-work | genç görünüşlü, dinç | She has a youthful face. | low | CSV said feelings; keep in school-work. general adjective (medical, weather, appearance...) not a feeling or trait; stage 3 |

