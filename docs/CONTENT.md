# Lexi — content model & pipeline

## 1. Pipeline
```
content/cards/<level>/*.csv           ──┐
content/categories.csv                  ├─▶ tools:content-compiler (JVM) ──▶ validate ──▶ content.db
content/exam_packs/*.csv                ┘                                            (composeApp/src/commonMain/composeResources/files/)
```
- Source of truth = CSV in git (reviewable diffs). `content.db` is a build output (committed only for releases or generated in CI).
- `content/meta.json` holds `contentVersion` (integer, bump on every content change).
- Production flow: open word lists → AI drafts Turkish clues/examples → **human review** (product owner) →
  CSV → compiler validation → playtest.

## 2. Card CSV schema (`content/cards/<level>/*.csv`, UTF-8, comma, header row; any file name, e.g. one file per category)
| column | type | rule |
|---|---|---|
| `id` | slug | unique forever, `<level>-<answer>[-n]`, e.g. `a1-kitchen`. Never reuse or rename. |
| `answer` | text | English, uppercase A–Z and single spaces only (`GIVE UP`). Length 2–14 letters. |
| `letter` | A–Z | wheel letter this card serves |
| `rule` | `STARTS`/`CONTAINS` | `STARTS` ⇒ answer starts with `letter`; `CONTAINS` ⇒ answer contains it (not as first letter) |
| `pos` | `noun`/`verb`/`adj`/`adv`/`phrase`/`idiom`/`phrasal` | shown as "İPUCU · İSİM" |
| `clue_tr` | text | Turkish clue. A1–B1: direct translation ("mutfak"). B2+: short Turkish definition or translation + context. ≤ 60 chars. Must not contain the answer. |
| `example_en` | text | natural sentence containing the answer (used blanked for the Example joker) |
| `example_tr` | text | translation of the example |
| `difficulty` | 1–5 | within the level |
| `tags` | `a|b` | optional (e.g. `false-friend`, `exam:lgs`) |
| `category` | category id | must exist in `content/categories.csv` (§4) |

Level comes from the folder name (`A1`, `A2`, `B1`, `B2`, `C1`, `NAT`). See `content/cards/A1/sample.csv`. Clues for other languages (v2) go in `content/clues/<lang>/…` keyed by `id`.

## 3. Validation (compiler fails the build on any error)
- unique `id`; answer charset & length; rule consistency with `letter`; clue doesn't contain the answer (case/
  diacritic-insensitive); example contains the answer; no duplicate answer within a level;
- coverage: each shipped level has ≥ ⚙3 cards for every letter it plays (warn < 5); mixed wheel always fills 26;
- profanity/blocklist check; trailing spaces; Turkish characters valid UTF-8.

## 4. Categories (secondary axis)
| id | TR name | Levels |
|---|---|---|
| `mixed` | Karışık | all (virtual: all categories of the level) |
| `daily-life` | Günlük hayat | A1–B2 |
| `food-drink` | Yiyecek ve içecek | A1–B2 |
| `home` | Ev ve eşyalar | A1–A2 |
| `family-people` | Aile ve insanlar | A1–B1 |
| `colors-numbers` | Renkler ve sayılar | A1 |
| `body-health` | Vücut ve sağlık | A1–B2 |
| `school-work` | Okul ve iş | A1–C1 |
| `animals` | Hayvanlar | A1–B1 |
| `city-transport` | Şehir ve ulaşım | A1–B1 |
| `time-weather` | Zaman ve hava | A1–A2 |
| `travel` | Seyahat | A2–C1 |
| `sports` | Spor | A2–C1 |
| `music` | Müzik | A2–C1 |
| `film-tv` | Film ve dizi | B1–C1 |
| `business` | İş İngilizcesi | B1–C1 |
| `technology` | Teknoloji | B1–C1 |
| `false-friends` | Yalancı eşdeğerler | B1–C1 |
| `idioms` | Deyimler | B2, C1, NAT |
| `phrasal-verbs` | Phrasal verbs | B2, C1, NAT |

## 5. Volume targets (v1.0)
| Level | Cards | Notes |
|---|---|---|
| A1 | 850 | design shows 850 |
| A2 | 900 | |
| B1 | 1200 | |
| B2 | 1400 | |
| C1 | 1500 | |
| NAT | 800 | idioms + phrasal verbs only |
| Exam packs | LGS 600, YKS-Dil 1500, YDS 2000, IELTS 1800, TOEFL 1800 | mostly references to existing cards + extra cards |

Start with A1 complete + 26-card smoke sets for the other levels; grow per milestone.

## 6. Source lists & licensing (verify before release — not legal advice)
- Candidate open lists: **NGSL / NAWL / TSL** (CC BY-SA 4.0 — attribution + share-alike may apply to the list
  data; keep attribution in the About screen), **CEFR-J Wordlist** (free with citation; check commercial terms).
- Do **not** copy Oxford 3000/5000 or Cambridge English Vocabulary Profile data (proprietary).
- Clues and example sentences we write ourselves (AI-assisted, human-reviewed) are our own content.
- Exam pack names (LGS, YDS, IELTS, TOEFL) are used descriptively; no logos.

## 7. Database schema (content.db)
```sql
CREATE TABLE meta(key TEXT PRIMARY KEY, value TEXT NOT NULL);           -- contentVersion, builtAt
CREATE TABLE category(id TEXT PRIMARY KEY, name_tr TEXT, name_en TEXT, sort INTEGER, is_pro INTEGER DEFAULT 0);
CREATE TABLE card(
  id TEXT PRIMARY KEY, level TEXT NOT NULL, category_id TEXT NOT NULL REFERENCES category(id),
  answer TEXT NOT NULL, letter TEXT NOT NULL, rule TEXT NOT NULL, pos TEXT NOT NULL,
  example_en TEXT NOT NULL, difficulty INTEGER NOT NULL, tags TEXT NOT NULL DEFAULT ''
);
CREATE TABLE clue(card_id TEXT NOT NULL REFERENCES card(id), lang TEXT NOT NULL, clue TEXT NOT NULL,
  example_translation TEXT, PRIMARY KEY(card_id, lang));
CREATE TABLE exam_pack(id TEXT PRIMARY KEY, name TEXT, description TEXT, is_pro INTEGER, sort INTEGER);
CREATE TABLE exam_pack_card(pack_id TEXT, card_id TEXT, unit TEXT, PRIMARY KEY(pack_id, card_id));
CREATE INDEX card_level_letter ON card(level, letter);
CREATE INDEX card_level_category ON card(level, category_id);
```

## 8. user.db (sketch — owned by `data-engineer`)
`progress(card_id PK, box, due_epoch_day, seen, correct, wrong, last_seen_ms, mastered)`,
`session(id, mode, level, category, seed, started_ms, ended_ms, score, correct, wrong, passed, completed)`,
`daily_result(date PK, level, score, time_ms, share_code)`, `streak(day PK)`, `joker(type PK, count)`,
`achievement(id PK, unlocked_ms)`.
