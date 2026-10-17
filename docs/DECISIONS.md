# Decisions log (ADR-lite)

Short, dated record of product, process and content decisions. Architecture decisions with real trade-offs live in
`docs/adr/` (full ADRs); this log indexes them and holds everything lighter. Newest first inside each section. Add a
row when a decision is made; change `Status` instead of deleting rows (`Accepted`, `Superseded`, `Open`).

## Product & game

| Date | Decision | Why | Alternatives | Status |
|---|---|---|---|---|
| 2026-10 | **Shared mixed-level daily wheel**: 26 slots with fixed counts A1×6, A2×5, B1×5, B2×4, C1×3, NAT×3; stored with `level = "SHARED"` sentinel | One identical wheel for everybody on a date (shareable, comparable); every player meets their level and a stretch | Wheel per level (4 wheels/day to play and verify); player-level only wheel | Accepted |
| 2026-10 | **Per-level points for the daily wheel**: A1 5, A2 7, B1 10, B2 13, C1 16, NAT 20 per correct answer | A mixed wheel must reward harder words fairly; flat 10 would favour easy letters | Level multiplier ×1.0–1.7 of `GAME_RULES.md` §6; flat points | Accepted |
| 2026-10 | **Pass has no penalty** (0 points, returns next lap); wrong = 0 too | Matches the TV-show feel and keeps learners from guessing; time is the real cost | Negative points for pass/wrong | Accepted |
| 2026-10 | **Daily resets at local midnight**; one scored attempt per local day (not per level) | Simple for players; offline-safe; matches streak/gift day logic (`AppClock.epochDay`) | UTC reset; rolling 24 h | Accepted |
| 2026-10 | **Home badge "N gün seri"** (day streak) is hidden at 0 and is a different thing from the in-game **"xN SERİ"** combo (consecutive correct answers) | Two "streaks" confuse; names and places differ (home vs HUD) and the engine already separates `Stats.currentStreak` and combo | One shared word/badge; show "0 gün seri" | Accepted |
| 2026-10 | **LGS pack is free; YDS + YÖKDİL are one Pro pack** `yds-yokdil` with `free_preview_letters = ABCDE` | Matches `10-exam-packs` (one row, LGS has no PRO badge); preview lets users try before paying | Both Pro; separate YDS and YÖKDİL packs | Accepted (`docs/content/EXAM_PACKS.md`) |
| 2026-10 | **No jokers in Daily, Duel and Level test** | Fair comparison; placement stays accurate | Allow with penalty | Accepted (`GAME_RULES.md` §7) |
| 2026-10 | **Daily joker gift, cap 5 per type, +1 at each 7-day streak** | Gentle retention loop without pay-to-win | Cap on total; no gifts | Accepted (`GAME_RULES.md` §7; cap to reconfirm) |
| 2026-10 | **Level test ≤ 14 questions, starts A2**, A1 if too few correct | Short onboarding, enough signal | 26-slot test; start at A1 | Accepted (`GAME_RULES.md` §9) |
| 2026-10 | **Duel answers don't touch notebook/stats** (`srsExcludedModes = {DUEL}`) | Hot-seat words are not personal progress | Count them | Accepted |
| 2026-10 | **Light theme only**, no dynamic colour | Bauhaus palette must be exact | Dark mode | Accepted (ADR 0010) |
| 2026-10 | Reminders **off until the M7 pre-prompt**; UI language follows the system language | Respect permission and defaults | On by default | Accepted (`PRODUCT.md`) |

## Content

| Date | Decision | Why | Alternatives | Status |
|---|---|---|---|---|
| 2026-10-17 | **American spelling is the standard** for all answers/examples (color, center, realize); accent setting only affects TTS | One canonical answer per card; the key row can't offer two spellings | British; accept both | Accepted (commits 4204be7, da166c2) |
| 2026-10 | **Stable card ids**: an id is never reused or renumbered once shipped | Notebook/SRS progress, daily seeds and shared wheels reference ids | Renumber on cleanup | Accepted (`CONTENT.md`) |
| 2026-10 | Exam packs live in-repo (`content/exam-packs/`), not a private repo | Single pipeline and review; repo goes private before release | Separate private repo | Accepted, see Open decisions |
| 2026-10 | Word lists open-licensed, **AI-drafted clues, human review** | Scale with quality control | Fully hand-written; fully automated | Accepted (ADR 0008) |
| 2026-10 | A1 stays below its 850 target (787) unless extended; unfinished levels must not ship as smoke sets | Strict A1 lists exhausted | Pad with B-level words | Open → release decision in `ROADMAP.md` |

## Store & marketing

| Date | Decision | Why | Alternatives | Status |
|---|---|---|---|---|
| 2026-10 | **Store images show real Turkish clues with English captions** | Real UI sells the mechanic; the English caption serves EN/TR viewers | Lorem/fake clues; separate TR/EN renders | Accepted |
| 2026-10 | Store names `Lexi: English Alphabet Quiz` / `Lexi: İngilizce Kelime Çarkı`; no third-party format names (Rosco, Pasapalabra) | Trademark safety | Earlier "Passaparola" title | Accepted; TÜRKPATENT check pending (`owner-checklist.md`) |

## Process & tooling

| Date | Decision | Why | Alternatives | Status |
|---|---|---|---|---|
| 2026-10 | **Trunk-based on `main`**, no PRs/branch protection requiring PRs; Conventional Commits | Solo owner, small atomic commits | GitHub Flow | Accepted (`CONVENTIONS.md`) |
| 2026-10 | **No AI attribution** in commits/PRs/code comments; **author-check CI guard** (`.github/workflows/author-check.yml`) fails on author/committer matching claude/anthropic and on `Co-Authored-By` / "Generated with" lines (checks commits after `.github/author-check-base`) | Owner identity only | Honour-system | Accepted |
| 2026-10 | **Gemini-model (Antigravity) work is reviewed and committed manually by the owner**, not pushed autonomously | Quality gate on the cheaper model | Autonomous commits | Accepted |
| 2026-10 | Cloud sessions write docs/content only; code and Gradle stay local | No Android SDK/Mac in the cloud | Cloud builds | Accepted |

## Architecture (index of `docs/adr/`)

| Date | ADR | Decision | Status |
|---|---|---|---|
| 2026-10-14 | 0001 | Compose Multiplatform for all UI | Accepted |
| 2026-10-14 | 0002 | Pure, deterministic GameEngine with event sourcing (seeded RNG, no clock inside) | Accepted |
| 2026-10-14 | 0003 | MVI/UDF with androidx ViewModel (KMP) and Koin | Accepted |
| 2026-10-14 | 0004 | SQLDelight with separate read-only content DB and user DB | Accepted |
| 2026-10-14 | 0005 | Offline-first v1.0; Supabase for online features from v1.1 | Accepted |
| 2026-10-14 | 0006 | Firebase Analytics/Crashlytics behind interfaces | Accepted |
| 2026-10-14 | 0007 | Freemium: rewarded ads + Lexi Pro via RevenueCat | Accepted |
| 2026-10-14 | 0008 | Open word lists, AI-drafted clues, human review | Accepted |
| 2026-10-14 | 0009 | Multi-module Gradle with convention plugins | Accepted |
| 2026-10-14 | 0010 | Light theme only | Accepted |
| 2026-10-14 | 0011 | Roborazzi screenshot tests as design-fidelity gate | Accepted |
| 2026-10-14 | 0012 | Static analysis (ktlint now, detekt later) and CI | Accepted |
| 2026-10-14 | 0013 | Toolchain baseline and version policy | Accepted |
| 2026-10-14 | 0014 | App module layout and convention plugins | Accepted |
| 2026-10-14 | 0015 | Logging, settings and test stack | Accepted |
| 2026-10-14 | 0016 | Navigation Compose with type-safe routes | Accepted |
| 2026-10-14 | 0017 | Compose UI tests for design-system behaviour | Accepted |
| 2026-10-15 | 0018 | Content DB build wiring and database lifecycle | Accepted |
| 2026-10-15 | 0019 | Game screen runtime: timer, lifecycle, effects | Accepted |

## Open decisions

| # | Question | Options | Recommendation | Needed by |
|---|---|---|---|---|
| O1 | **Rewarded only vs interstitial in v1.0** (`PRODUCT.md` allows one interstitial per 3 games; privacy/data-safety drafts say rewarded only) | Rewarded only / + interstitial every 3rd game after session 1 | **Rewarded only.** Keeps the game calm, simplifies consent, store forms and reviews; revisit after retention data | Before M7 ads work and store forms |
| O2 | **ATT (iOS) vs non-personalized ads** | Show ATT after UMP / serve non-personalized only, no ATT prompt | **Non-personalized ads, no ATT prompt.** Fewer drop-offs, simpler privacy label; accept lower eCPM. Revisit if revenue matters | Before M7 / App Privacy answers |
| O3 | **Exam names (LGS, YKS, YDS, YÖKDİL, IELTS, TOEFL) in store text** | Use names / generic wording ("sınav hazırlığı") | **Generic wording in store listings** until the trademark/affiliation check is done; names may appear in-app as descriptive labels (decide with lawyer). Copy deck marks every **(EXAM)** string | Before store submission |
| O4 | **Repo visibility and exam-pack private repo** (repo planned to go private before release; packs now in-repo) | Keep public with packs / go private / split packs into a private repo | **Make the repo private before release** and keep packs in-repo (one pipeline). Confirm sources' licences allow redistribution in a shipped DB regardless | Before release; before any wider sharing |
| O5 | **Rewrite history for the 2 commits authored as Claude** (4204be7, da166c2) | Leave as is (author-check only guards new commits) / rewrite and force-push | **Leave pushed history alone** (rules: never rewrite pushed history; the guard starts after them) unless the owner explicitly wants a rewrite — destructive, owner decision only | Owner |
| O6 | **Coop mode** — no spec exists (closest: v1.2 Teams, `MULTIPLAYER.md`) | Fold into Teams / separate "co-op wheel" (players share one wheel and clock) / drop | Treat as part of v1.2 Teams; write a one-page spec before any design | After v1.0 |
| O7 | A1 content gap (787/850) and levels still on smoke sets: ship, complete, or mark "coming soon" | — | Complete or hide anything on a smoke set; accept 787 for A1 | Release checklist |
| O8 | Prices and trial length for monthly/yearly/lifetime | — | Set in stores + RevenueCat; copy uses placeholders | Before M7 sandbox tests |
