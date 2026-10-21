# ADR 0022: Multi-language roadmap

- Status: Accepted
- Date: 2026-10-21

## Context
v1.0 launches in Turkey: Turkish clues, Turkish/English UI, English answers on an A–Z wheel. The owner wants a
global path after the TR launch without rewriting the engine. The content DB already keys clues by language
(`clue(card_id, lang, …)`, `CONTENT.md` §7) and `ClueLanguage` exists in settings, so the first step is mostly content
and UI work. Changing the answer language is a much bigger change (alphabet, keyboard, letter rules, normalisation, TTS).

## Decision
Three steps, each its own milestone after the v1.0 TR launch (`ROADMAP.md` "Global languages"):
1. **X→English.** New clue languages and UI languages; answers stay English, `GameEngine` unchanged. Candidates in
   order of evaluation: ES, PT-BR, DE, ID. RTL languages (AR, FA, HE) later, after an RTL layout pass.
2. **English→English** for B1+: English definitions as clues (`lang = "en"`), for learners who don't want translation.
3. **X→Y.** Answers in a language other than English: per-language answer alphabet (wheel letters, keyboard
   generator, `STARTS`/`CONTAINS` rules, answer normalisation incl. diacritics and digraphs), TTS voice per language,
   per-language word lists and levels. Needs its own ADR before work starts.

Prerequisites before step 1:
- No hardcoded Turkish clue language in code paths. Known spots (follow-ups, not fixed now):
  - `tools/content-compiler` `CardRules.COLUMNS` / `clue_tr`, `example_tr`; `ContentDbWriter.CLUE_LANG_TR`; Turkish-only
    blocklist columns (`Blocklist.TR_COLUMNS`) and `TextMatch` Turkish locale.
  - `core/data` `ContentRepositories`: `clueLanguage` defaults to `ClueLanguage.TR`; must come from settings.
  - `core/domain` `ClueLanguage` has only `TR`; `Card` docs assume Turkish.
  - `category.name_tr` / `name_en` columns and the `Locale.current.language == "tr"` switch in `CategoriesScreen` and
    `NotebookScreen` (only two UI languages possible); `exam_pack.name` / `description` single-language.
  - `NotebookViewModel` search folding is Turkish-specific (dotted/dotless i); needs per-language folding.
  - `SettingsScreen` clue-language row is fixed to "Türkçe" (no chevron); needs the picker (design).
  - `AppLanguage` has only `TR`/`EN`; `AppLinks` TODO for localised policy paths.
- Content translation + native review pipeline (`CONTENT.md` §1): `content/clues/<lang>/…` keyed by card id, AI draft,
  native reviewer sign-off per batch, compiler coverage per language.
- Store listing localisation per language (title, short/long description, screenshots).
- Brand-name check per target market (trademark + meaning of "Lexi"/store titles).
- Per-language content packs (downloaded or split DBs) if `content.db` size grows too much; decide with a size budget.

## Consequences
- Step 1 adds no engine change and no new module; it is content, settings and UI work plus the follow-ups above.
- Step 3 touches `core:domain` rules and the deterministic engine; it gets its own ADR and test plan.
- New languages must respect design fidelity (longer strings, font coverage) and the store-declaration rule.
