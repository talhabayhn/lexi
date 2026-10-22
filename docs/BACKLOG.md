# Backlog (single prioritized list)

Order = priority within each section; sections are in release order. **Owner:** `AG` = Antigravity (local, Gemini
routine / Opus hard work), `Cloud` = cloud session (docs/content only, no Gradle), `Owner` = product owner (accounts,
Mac, devices, decisions). **Size:** S ≤ half a day, M ≈ 1–2 days, L > 2 days. IDs are stable; tick in `ROADMAP.md`
when done. Sources: `ROADMAP.md`, `screen-map.md`, `DECISIONS.md`, `store/owner-checklist.md`.

## 1. M6 remainder
| ID | Item | Owner | Size | Depends on |
|---|---|---|---|---|
| B1 | ~~Countdown label on daily card/result ("Yeni çark: {time}") + already-played state~~ — done | AG | S | `COPY.md` §5 |
| B2 | Grouped result review (group "Tekrar edilecekler" by missed / passed) | AG | M | — |
| B3 | ~~Share card renderer (`12-share-card`, 432×540 bitmap) + system share sheet (Android + iOS)~~ — done | AG | L | `COPY.md` §7, platform share bridge |
| B4 | ~~Onboarding 3 steps (`01-onboarding` + steps 1 & 3 design debt)~~ — done | AG | M | `COPY.md` §3 |
| B5 | ~~Level test flow (one-slot games, carried clock) + intro (DESIGN-DEBT), level choice to `01-onboarding`~~ — done | AG (Opus·high for engine wiring) | L | B4, `GAME_RULES.md` §9 |
| B6 | ~~Level test result screen ("Seviyen: B1", change level, start; DESIGN-DEBT, `06-result` header style)~~ — done | AG | M | B5, `COPY.md` §4 |
| B7 | ~~Daily wheel intro / already-played state polish~~ — done | AG | S | B1 |
| B8 | Verify "two devices get the identical daily wheel" on real devices | Owner | S | B1–B7, `TEST_PLAN.md` §3 |

## 2. Content
| ID | Item | Owner | Size | Depends on |
|---|---|---|---|---|
| C1 | ~~Compile content, bump `contentVersion` so installed apps refresh `content.db`~~ — done | AG | S | — |
| C2 | ~~Compiler: pick up `content/exam-packs/` by default, widen pack metadata, per-pack coverage + level-window checks (`docs/content/EXAM_PACKS.md`)~~ — done | AG | M | C1 |
| C3 | ~~Exam-pack tables used at runtime (pack query path with `pack_only = 0` filter on normal wheels)~~ — done | AG | M | C2 |
| C4 | ~~Release decision: A1 at 787/850, levels with smoke sets~~ — decided: A1 stays at the current count (accepted); every level has a full set, so no "coming soon" | Owner | S | O7 in `DECISIONS.md` |
| C5 | ~~Spot-check content per `TEST_PLAN.md` §4 (30 cards/level) and log into `content-review/`~~ — done | Cloud | M | C1 |
| C6 | (not needed, A1 accepted as is) Finish thin letters (A1 Q, Z, V, Y, I, J; others per review notes) if the owner wants 850 | Cloud | L | C4 |

## 3. M7 — monetisation, settings, telemetry
| ID | Item | Owner | Size | Depends on |
|---|---|---|---|---|
| M7.1 | ~~Settings: classic time, goal, languages, accent, reminder rows live; remove "YAKINDA" chips~~ — done | AG | M | — |
| M7.2 | ~~Notification pre-prompt + reminder scheduler (1/day, quiet hours, 12 variants)~~ — done | AG | L | M7.1, `COPY.md` §1 |
| M7.3 | Decide O1 (ad policy) and O2 (ATT) | Owner | S | — |
| M7.4 | RevenueCat: products in stores, Pro entitlement, restore | Owner (stores) + AG | L | Dev accounts (R1), O8 prices |
| M7.5 | Paywall screen (design debt) | AG, design from Owner | M | M7.4, `COPY.md` §2 |
| M7.6 | AdMob rewarded (+1 joker) with UMP consent; enable `FeatureFlags.rewardedAdJokers` | AG | L | M7.3, AdMob account (R2) |
| M7.7 | ~~Exam packs screen `10-exam-packs` wired with Pro lock and A–E preview~~ — done | AG | M | C3, M7.4 |
| M7.8 | ~~Firebase Analytics + Crashlytics behind interfaces; events per `ANALYTICS.md`; no consent form until M7.6 (owner, ADR 0023)~~ — done | AG | M | Firebase project (R3) |
| M7.9 | Category Pro lock hook (no shipped category is Pro today) | AG | S | M7.4 |
| M7.10 | Sandbox test pass for purchases/ads/consent | Owner | M | M7.4–M7.8, `TEST_PLAN.md` §5 |

## 4. M8 — duel, polish
| ID | Item | Owner | Size | Depends on |
|---|---|---|---|---|
| M8.1 | Hot-seat duel: setup, handover card, per-player clocks, result (design needed first) | AG (Opus·high) | L | Design (D9) |
| M8.2 | ~~Accessibility pass (labels, font caps, contrast, reduced motion)~~ — done | AG | M | `TEST_PLAN.md` §8 |
| M8.3 | Small-screen/tablet checks and fixes | AG | M | `TEST_PLAN.md` §7 |
| M8.4 | ~~Rating prompt (`ConfirmDialog`) with trigger rules~~ — done | AG | S | `COPY.md` §8 |
| M8.5 | ~~About/licences screen (Jost OFL, word-list attributions)~~ — done | AG | S | — |
| M8.6 | Performance work: cold start < 1.5 s, 60 fps game | AG | M | `TEST_PLAN.md` §9 |
| M8.7 | Store listing text finalised (O3 exam names), screenshots, privacy/data-safety/nutrition labels | Cloud + Owner | M | O1–O3 |
| M8.8 | Real-device test pass on both platforms | Owner | M | `TEST_PLAN.md` |

## 5. Release tasks
| ID | Item | Owner | Size | Depends on |
|---|---|---|---|---|
| R1 | Developer accounts (Play, Apple), tax/banking | Owner | M | — |
| R2 | AdMob + RevenueCat + Firebase projects and keys (secrets, never committed) | Owner | M | R1 |
| R3 | Android keystore + GitHub secrets (`docs/RELEASE.md`) | Owner | S | R1 |
| R4 | iOS signing, bundle id, TestFlight (Mac check) | Owner | M | R1 |
| R5 | Fill placeholders (`[DEVELOPER NAME]`, `[CONTACT EMAIL]`, `[WEBSITE]`), host privacy/terms/support pages, lawyer review | Owner | M | `owner-checklist.md` |
| R6 | Trademark/store-name search (TÜRKPATENT) | Owner | S | — |
| R7 | Closed test: 12 volunteers × 14 days (Play requirement, verify current rule); give them the guide in `TEST_PLAN.md` | Owner | L | M8.8, build in internal track |
| R8 | Decide O4 repo visibility; make private before release | Owner | S | — |
| R9 | Production submission Play + App Store | Owner | M | R1–R7 |

## 6. v1.1 — online rooms (`MULTIPLAYER.md`)
| ID | Item | Owner | Size | Depends on |
|---|---|---|---|---|
| V1.1 | Design online screens (rooms, lobby, race, results) | Owner/design | L | v1.0 shipped |
| V1.2 | Supabase project, RLS, anonymous auth + nickname | AG (Opus·high) | L | V1.1 |
| V1.3 | `SupabaseRealtimeTransport`, create/join/lobby | AG (Opus·high) | L | V1.2 |
| V1.4 | Online Duel, Race (+ level handicap), reconnect | AG (Opus·high) | L | V1.3 |
| V1.5 | Load test with 8 clients; privacy docs updated | AG + Cloud | M | V1.4 |

## 7. v1.2
| ID | Item | Owner | Size | Depends on |
|---|---|---|---|---|
| V2.1 | Teams mode — also settles O6 coop (spec first) | Cloud (spec) → AG | L | V1.4 |
| V2.2 | Async challenge links ("I did this wheel in 1:42") | AG | L | V1.2 |
| V2.3 | Achievements | AG | M | design |
| V2.4 | Cloud backup | AG | L | V1.2 |

## 8. Design debt (screens without a design — `screen-map.md`)
Built from `16-components` today; an owner design request is optional unless marked **needs design**.
| ID | Screen | Milestone | Owner | Size | Notes |
|---|---|---|---|---|---|
| D1 | Paywall (Lexi Pro) | M7 | Owner (design) → AG | M | **needs design** before M7.5 |
| D2 | Notification permission pre-prompt | M7 | Owner → AG | S | onboarding layout |
| D3 | Onboarding steps 1 & 3 | M6 | Owner → AG | S | same layout as step 2 |
| D4 | Level test result ("Seviyen: B1") | M6 | Owner → AG | S | |
| D5 | Daily wheel intro / already-played | M6 | Owner → AG | S | |
| D6 | Rating prompt | M8 | AG | S | `ConfirmDialog` |
| D7 | Hot-seat duel setup, handover card, result | M8 | Owner (design) → AG | M | **needs design** |
| D8 | Online rooms | v1.1 | Owner (design) | L | **needs design** (`MULTIPLAYER.md` §4) |
| D9 | Store screenshots (frames + headline band) | M8 | Cloud/AG | M | real Turkish clues, English captions |
| D10 | Daily joker gift notice (toast) | M7 | AG | S | `GrantDailyJokers` already returns grants |
| D11 | Pause dialog, word detail sheet, stats/notebook/game empty and error states, result without misses, joker sheet clock pill, combo badge position, compact layouts | M4–M5 | Owner | S | shipped "as built, DESIGN-DEBT"; ask for a design only if you want pixel parity |
| D12 | Settings M7 rows (design values/chevrons replace "YAKINDA") | M7 | AG | S | with M7.1 |
| D13 | Notebook placeholder font weight (needs a Regular weight of Jost) | M5 | Owner | S | optional |

## 9. Polish leftovers (2026-10-19 pass)
| ID | Item | Owner | Size | Notes |
|---|---|---|---|---|
| P1 | ~~Wheel centre letter sits ~13dp higher than `04-game`~~ — accepted by the owner, documented in `screen-map.md` | Owner | S | done |
| P2 | ~~Words longer than the whole column still split at 320dp + fontScale 2.0 (e.g. a joker title in the stacked layout)~~ | AG | S | done: JokerRow title uses `Hyphens.Auto` |
| P3 | ~~SegmentedControl: wrapped segment titles are start-aligned (centring them moved the design-size text 1px)~~ | AG | S | done: wrapped titles centred per line, one-line case unchanged |
| P4 | `docs/design/sound-previews` still holds the old `time_up` preview | AG | S | rerun `tools/sound-gen/generate.py` with previews (needs ffmpeg) |
| P5 | 14 goldens differ beyond the palette (ComponentGallery, LevelSelectComponents, LexiIcons, RatingPromptDialog + En, 9 Notebook screens) | AG | S | review each change, then re-record |
| P6 | ~~`TokensColors` screenshot still labels the pre-9fcd32d hex values~~ | AG | S | done: labels derived from the tokens |
