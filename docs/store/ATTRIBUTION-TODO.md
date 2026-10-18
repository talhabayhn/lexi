# Word-List Attributions — Owner Action Required

> Tracked in Task M8.5 (About / licences screen).

## Background
- `docs/CONTENT.md` §6 and `docs/adr/0008-content-open-word-lists-ai-drafted-clues-human-review.md` note:
  - Candidate open lists: **NGSL / NAWL / TSL** (CC BY-SA 4.0 — attribution + share-alike may apply to the list data; keep attribution in the About screen), **CEFR-J Wordlist** (free with citation; check commercial terms).
  - Explicit rule: Do not copy Oxford 3000/5000 or Cambridge English Vocabulary Profile data (proprietary).
  - Clues and example sentences written in-house (AI-assisted, human-reviewed).
- However, the repo does not yet document which specific candidate open lists were actually adopted for headword selection across the shipped levels (A1, A2, B1, B2, C1, NAT) and exam packs (LGS, YDS/YÖKDİL).

## What the Product Owner Must Confirm
1. **Source Lists Used**: Specify which third-party word lists were actually used for each level's headword vocabulary.
2. **Attribution Statement**: For each confirmed third-party list, provide the exact legal citation/attribution text to display in the About screen's "Kaynaklar" / Sources section.
3. **In-House / Public Domain**: If headwords were selected independently or from public domain frequency data without incorporating protected dataset content, confirm that no external word-list attribution is required.

Until the product owner confirms provenance, the About screen displays only that clues and examples are original content, without claiming unverified third-party sources.
