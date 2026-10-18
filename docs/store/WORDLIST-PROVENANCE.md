# Word-list provenance inventory

Status: inventory of what the repo says, as of `main` @ `12b47fb`. Facts and open questions only; this is not legal advice.
Scope read: `docs/CONTENT.md` §6, `docs/adr/0008-*`, `docs/store/ATTRIBUTION-TODO.md`, `content/` (cards, `meta.json`,
`exam-packs/`), `docs/content-review/*` (74 files), `docs/content/EXAM_PACKS.md`, `.claude/agents/content-curator.md`,
`tools/content-qa/`, and `git log` of `content/`.

Limits of this inventory:
- The local clone is shallow (50 commits, oldest `0d5ee5d` 2026-10-17). Content commits visible: `4204be7`, `f6f69b4`, `da166c2`,
  `385f1c1` (+ `5e6913d` as the grafted base). Earlier content history is not available; the full history on GitHub may say more.
- No headword list file, script input or download of any third-party list exists in the repo (`grep` for NGSL, NAWL, TSL, CEFR-J
  finds only the four doc/test lines quoted below; zero hits in `content/`, `tools/`, `docs/content-review/`).
- Online licence pages could only be checked through search-result summaries (the sandbox proxy blocks direct fetches of
  `newgeneralservicelist.com`, `cefr-j.org`, `oxfordlearnersdictionaries.com`). Everything resting on that is marked **VERIFY**.

## 1. What the repo documents as the headword source

Totals in `content/` (cards counted from the CSVs): A1 787, A2 878, B1 849, B2 729, C1 675, NAT 611 = 4529 base cards;
851 pack-only cards (LGS 297, YDS/YÖKDİL 554). `meta.json`: `shippedLevels` A1–NAT, `shippedPacks` `lgs`, `yds-yokdil`.

### Policy statements (all levels)
- `docs/CONTENT.md` §1: "Production flow: open word lists → AI drafts Turkish clues/examples → **human review** (product owner)".
- `docs/CONTENT.md` §6: "Candidate open lists: **NGSL / NAWL / TSL** (CC BY-SA 4.0 — attribution + share-alike may apply to the list
  data …), **CEFR-J Wordlist** (free with citation; check commercial terms). Do **not** copy Oxford 3000/5000 or Cambridge English
  Vocabulary Profile data (proprietary)."
- `docs/adr/0008-*`: "Word selection from licence-compatible open lists (NGSL family, CEFR-J — licence check before release)".
- `docs/store/ATTRIBUTION-TODO.md`: "the repo does not yet document which specific candidate open lists were actually adopted for
  headword selection across the shipped levels … and exam packs".
- `.claude/agents/content-curator.md:12`: "Pick words appropriate to the CEFR level from licence-compatible open lists … Never copy
  Oxford/Cambridge proprietary lists."
- `docs/CONTENT.md:95`: "CEFR level follows the official A1–C1 level of the word (Cambridge/Oxford level labels, used only as a
  reference, see §6)."
- `docs/content-review/SECOND-PASS-summary.md:5` and `tools/content-qa/scan_existing.py:110`: "Level-vs-frequency check skipped (no
  offline frequency list)" — no frequency list is used by any tool.

### Per level (what review sheets actually say)
| Level | Cards | Documented headword source | Quote / file |
|---|---|---|---|
| A1 | 787 | Cambridge Young Learners (Starters/Movers) and "Oxford 3000 A1" **level labels, from the reviewer's own knowledge**; no open list named | `M3-owner-decisions.md:3` "Level reference: Cambridge Starters/Movers and Oxford 3000 A1 (own knowledge, no list data copied)"; `A1-batch-8.md:3` "A1 words ran out under strict Starters/Movers/Oxford A1 filtering (only 44)"; `A1-batch-9.md:101` "Cambridge Starters/Movers/Oxford A1 candidates are exhausted" |
| A2 | 878 | Cambridge "Flyers-style" vocabulary; "A1 in Oxford" used as a level check | `A2-batch-9.md:4` "Cambridge Flyers-style nouns (ALIEN ... COMPASS)"; `A2-batch-10.md:3` "Flyers-level nouns and verbs", `:6` "ICE CREAM (A1 in Oxford)"; `A2-batch-2.md:8` "Flyers-accepted" |
| B1 | 849 | Oxford level labels used to drop/keep words; no source list named | `B1-batch-1.md:4` "ASSISTANT, BUSINESSMAN, AVAILABLE, ADVERTISEMENT (A2 in Oxford)"; `B1-batch-5.md:4` "several are B2 by Oxford" |
| B2 | 729 | Not documented. `B2-summary.md`: "A2/B1/C1 words … were dropped"; level calls from reviewer judgement | `B2-summary.md` |
| C1 | 675 | Not documented. "strict C1/C2 line", "Doubtful levels per batch" | `C1-summary.md` |
| NAT | 611 | Not documented. "educated native (C2+) single words" chosen by judgement | `NAT-summary.md:6` |

No file for any level names NGSL, NAWL, TSL or CEFR-J as an input.

### Per exam pack
| Pack | Cards | Documented source | Quote / file |
|---|---|---|---|
| `lgs` (free) | 573 (297 pack-only) | "10 MEB 8th-grade themes"; items kept because "they are in the MEB unit word lists". Unit word lists themselves are not in the repo. | `CONTENT.md:59`; `EXAM-LGS-summary.md:4`, `:21` |
| `yds-yokdil` (Pro) | 990 (554 pack-only) | Topic units (connectors, general-academic, science, health, …) chosen by the author; no syllabus, past-paper or list named as a source | `CONTENT.md` §1 table; `EXAM-YDS-summary.md`, `EXAM-YOKDIL-summary.md` |

`docs/store/terms-en.md:12` and `docs/research/ASO.md:148` already say the app is not affiliated with any exam body (descriptive use of names only).

## 2. Open lists that may have been used, and their published licences

None is confirmed as used. These are only the lists the repo names as candidates, plus the facts found online.

| List | Owner | Licence as published | Status |
|---|---|---|---|
| NGSL 1.2 (2,809 words) | Browne, Culligan & Phillips, New General Service List Project | Project pages carry "Creative Commons Attribution-ShareAlike 4.0 International"; sibling pages say permissions beyond the licence may be available from the author ([project site](https://www.newgeneralservicelist.com/new-general-service-list), via [search summary](https://www.newgeneralservicelist.com/our-wordlists-1)) | **VERIFY** on the list's own page (summaries only; core-list licence line not seen directly in the first search) |
| NAWL (~957–963 words) | same project | Same CC BY-SA 4.0 line reported on the project's sibling lists ([NAWL page](https://www.newgeneralservicelist.com/new-academic-word-list), [NGSL-Spoken](https://www.newgeneralservicelist.com/ngsl-spoken)); suggested APA-style citation, authors/year vary by source (2013/2014, 2–3 authors) | **VERIFY** licence line and citation wording |
| TSL (TOEIC Service List) | same project | CC BY-SA line reported on the project's [TOEIC Service List page](https://www.newgeneralservicelist.com/toeic-service-list) | **VERIFY** |
| CEFR-J Wordlist (v1.6) | Tono Laboratory, TUFS | Secondary source (Open Language Profiles repo, v1.5 data): free for research and commercial use if cited; copyright Tono Laboratory ([olp-en-cefrj](https://github.com/openlanguageprofiles/olp-en-cefrj)). Papers cite it as Tono Laboratory (2020) for v1.6. Official `cefr-j.org` terms not retrieved | **VERIFY** on `cefr-j.org` |
| Oxford 3000 / 5000 | Oxford University Press | Published on [Oxford Learner's Dictionaries](https://www.oxfordlearnersdictionaries.com/about/wordlists/oxford3000-5000); no open licence found; terms not retrieved | Proprietary (repo's own rule); **VERIFY** OUP terms |
| English Vocabulary Profile | Cambridge University Press & Assessment | No open licence found | Proprietary (repo's own rule); **VERIFY** |

## 3. Risks (facts from the repo)

1. **Proprietary level references were used during review.** Lines mentioning Oxford: 14 (9 files); Cambridge: 7; Starters/Movers/Flyers: 12
   (counted over `docs/content-review`, `CONTENT.md`, `ATTRIBUTION-TODO.md`, ADR 0008, `.claude`). Excluding the three "do not copy"
   rules (`CONTENT.md:178`, `ATTRIBUTION-TODO.md:8`, `content-curator.md:12`), the remaining lines are in **8 review files**
   (`A1-batch-8`, `A1-batch-9`, `A2-batch-2`, `A2-batch-9`, `A2-batch-10`, `B1-batch-1`, `B1-batch-5`, `M3-owner-decisions`) plus
   `CONTENT.md:95`. They use "Oxford A1/A2/B2" and "Starters/Movers/Flyers" as **level labels** to keep, move or drop words.
   `M3-owner-decisions.md:3` states "own knowledge, no list data copied"; no file or visible commit contains an Oxford/Cambridge list
   file or a pasted list. Whether headwords for A1/A2 came from recalling those lists is a question for the owner (§4, Q2).
   Note: the Cambridge Young Learners (Starters/Movers/Flyers) word lists are a third proprietary-ish source the repo's rule does not name.
2. **Share-alike:** if NGSL/NAWL/TSL headwords were taken, CC BY-SA 4.0 requires attribution and applies ShareAlike to adaptations of the
   licensed material. Whether a headword selection embedded in a closed app/database is an "adaptation" is a legal question; the repo
   plans to make `content/` private (`docs/content/EXAM_PACKS.md`). Not decided here.
3. **Attribution gap:** About screen shows only "Clues and example sentences are written for Lexi." (`about_sources_content` in
   `feature/settings/.../values/strings.xml:54`, TR at `values-tr/strings.xml:54`). If any list was used, this is incomplete; if none, it is accurate.
4. **No provenance record:** zero cards carry a source tag; CSV schema has no `source` column; shallow history hides early commits.
5. **Exam packs:** LGS cites the MEB unit word lists (not in repo) as a basis for keeping 18 B2 references; their licence/permission is unknown
   (VERIFY whether any MEB text/list was consulted or copied).
6. **Store text:** `docs/store/app-privacy-apple.md:103` already carries "VERIFY word list and clue sources licences".

## 4. Questions the product owner must answer (closes `ATTRIBUTION-TODO.md`)

1. For each level A1, A2, B1, B2, C1, NAT: were headwords picked from (a) a downloaded/pasted list (which, which version, from where),
   (b) the reviewer's/AI's own knowledge only, (c) something else? Please answer per level.
2. A1/A2 sheets cite "Oxford 3000 A1", "Starters/Movers/Flyers": were these lists open in front of the reviewer, pasted into a prompt, or recalled from memory?
   Was any list file (PDF/CSV/web page) given to Claude/another model in any session?
3. Were NGSL, NAWL, TSL or CEFR-J ever consulted or loaded, even only to check levels? If yes, which version, and was it used to select or only to verify?
4. Were any headwords filtered or ranked against a frequency list or corpus (none is in the repo)?
5. LGS: were MEB textbook unit word lists copied, paraphrased or only used as topic inspiration? Which book/year?
6. YDS/YÖKDİL: were past-paper words, ÖSYM material or commercial YDS lists used? Which?
7. Confirm the final stance for the About screen: (a) "no external list; all headwords chosen by us" or (b) list each confirmed list with attribution (§5).
8. If (a): is the owner comfortable with that statement given Q2 (level labels from Oxford/Cambridge knowledge)? If any doubt, should flagged cards be re-leveled or replaced?
9. Is `content/` going private before release, and should share-alike (if any list is confirmed) be handled by a legal check before launch?
10. Who owns the exam-pack names/disclaimer (ÖSYM, MEB) wording in the store listing? (Existing disclaimer: not affiliated.)

## 5. Proposed attribution text (use only for lists confirmed in §4; wording to be checked against each list's own page)

EN / TR, for the About → Sources section. Replace `<version>` after confirming.

- **NGSL** — "Word selection informed by the New General Service List (NGSL <version>) by Browne, Culligan & Phillips, licensed under CC BY-SA 4.0 (creativecommons.org/licenses/by-sa/4.0). newgeneralservicelist.com. Changes: selection, level assignment and all clues/examples are our own."
  TR: "Kelime seçimi, Browne, Culligan ve Phillips'in CC BY-SA 4.0 lisanslı New General Service List (NGSL <version>) listesinden yararlanmıştır. Değişiklikler: seçim, seviye ataması ve tüm ipuçları/örnekler bize aittir."
- **NAWL** — "Academic vocabulary informed by the New Academic Word List (NAWL <version>) by Browne, Culligan & Phillips, CC BY-SA 4.0. newgeneralservicelist.com."
  TR: "Akademik kelimeler, Browne, Culligan ve Phillips'in CC BY-SA 4.0 lisanslı New Academic Word List (NAWL) listesinden yararlanmıştır."
- **TSL** — "Business vocabulary informed by the TOEIC Service List (TSL) by Browne & Culligan, CC BY-SA 4.0." TR equivalent as above.
- **CEFR-J** — "Level information based on the CEFR-J Wordlist <version>, © Tono Laboratory, Tokyo University of Foreign Studies. cefr-j.org." TR: "Seviye bilgisi, Tokyo Yabancı Diller Üniversitesi Tono Laboratuvarı'nın CEFR-J Wordlist <version> listesine dayanır."
- **If none confirmed** (keep current text): "Clues and example sentences are written for Lexi." plus, optionally: "Level labels follow the CEFR scale; Lexi is not affiliated with any exam body."
- **Not to be used:** do not name or credit Oxford 3000/5000, Cambridge EVP or Cambridge Young Learners lists as a source unless a licence/permission exists; attribution does not make copying permissible.
