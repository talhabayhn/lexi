# Lexi — product definition

## One-liner
A modern, Bauhaus-styled passaparola (A–Z wheel) game that teaches English vocabulary to Turkish
speakers, from A1 to native-level idioms — playable solo, on one phone with a friend, and online.

## Audience
- Primary: Turkish learners of English — students (LGS, YKS-Dil), university (YDS/YÖKDİL), adults (IELTS/TOEFL, work).
- Secondary (later): other clue languages (Spanish first). The data model already has `clue_lang`.

## Store positioning
- Title pattern: **"Lexi: İngilizce Passaparola"** (TR) / **"Lexi: English Alphabet Quiz"** (EN).
- Never use "Rosco"/"Pasapalabra" (licensed TV formats). Run a TÜRKPATENT + store search before release.
- References / competitors: Palabras, Cadev Games "Passaparola". Our edge: real CEFR levels, learning
  loop (notebook + SRS), smart letter keys, exam packs, daily wheel, Bauhaus UI, friend play.

## Feature set

### v1.0 (launch)
| Area | Feature |
|---|---|
| Core | A–Z wheel, Turkish clue → English answer, letter keys with distractors, pass & return, timer, jokers |
| Levels | A1, A2, B1, B2, C1, NAT (idioms, phrasal verbs) |
| Categories | Secondary axis inside each level (see `CONTENT.md` §4) + "Karışık" mixed wheel |
| Modes | Classic (timed), Practice (untimed), Hardcore (one mistake ends), Daily Wheel, Review Wheel |
| Learning | End-of-game review list, Kelime Defterim (Leitner SRS), pronunciation (TTS) |
| Daily | Günün Çarkı (deterministic seed per day + level), streak, share card |
| Onboarding | 3 steps + 2-minute adaptive level test (or pick a level) |
| Stats | Learned words, accuracy, best streak, weekly chart, level progress, hardest letters |
| Exam packs (Pro) | LGS, MEB, YKS-Dil, YDS/YÖKDİL, IELTS, TOEFL |
| Friends (offline) | **Same-device duel** (hot-seat, TV-show rules) |
| Monetisation | Free + rewarded ads (jokers) + Lexi Pro (monthly, yearly, lifetime) |
| Settings | Sound, haptics, TTS on correct, classic duration, reminder, daily goal, UI/clue language, accent. Owner decisions: daily reminder off until the M7 notification pre-prompt; UI language defaults to the system language |

### v1.1
Online private rooms with a 5-character code: **1v1 Duel** and **Race**. Joining a room is always free.
Level handicap in Race (each player on own level, normalized score). Anonymous auth + nickname.

### v1.2
**Teams** mode, **async challenge links** ("I did this wheel in 1:42 — beat me"), achievements, cloud backup.

### Later
Teacher/classroom mode, more clue/answer languages (ADR 0022), tablet-optimised layouts, widgets.

## Monetisation rules
Model: Free + rewarded ads + Lexi Pro (RevenueCat, ADR 0007; decisions in `DECISIONS.md`, 2026-10-21).
- Free: all levels playable; ads only as **rewarded** (opt-in, for jokers); 1 review wheel per day.
- **No interstitials in v1.** Later experiment only: from day 3, after the user leaves the result screen, max 1 per
  3 games, never in Daily, Level test or during a wheel.
- Pro: exam packs, no ads, +3 jokers daily, unlimited review wheels, create online rooms (v1.1).
- Prices: TR ₺89 monthly / ₺449 yearly / ₺1.199 lifetime. Store base country USA $4.99 / $29.99 / $59.99; the Turkey
  price is set manually in both stores. iOS Family Sharing on. Set in the stores + RevenueCat; never hardcode.
- Trial: 7-day free trial on yearly only, local reminder notification 2 days before it ends. Later: A/B test 3 vs 7
  days in RevenueCat.
- Privacy/ads: audience 13+, ad content rating G/PG, UMP consent; iOS: no ATT prompt in v1 (non-personalised ads),
  SKAdNetwork IDs in `Info.plist`.

## Success metrics
D1 ≥ 40%, D7 ≥ 15%, wheels/DAU ≥ 2, Pro conversion ≥ 2%, crash-free sessions ≥ 99.5%.
