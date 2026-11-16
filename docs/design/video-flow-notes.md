# Video flow — notes (proposal, branch `redesign/video-flow`)

Single source of truth for the "video flow" redesign: Home → "Çarkını seç" → mode/duration dialog → letter morph
into the game. Owner-approved prototype in `drafts/video-flow/` (`flow.html`, `lexi-flow-prototype-v2.mp4`,
`home_v5.png` = current approved screens, `cats_v3.png` = categories + exam packs + dialog states, `art_v3.png` = the
19 category illustrations, `home_v4.png` = previous iteration, reference only). ADR 0027 records the decisions.

**Reversible.** Everything lives on `redesign/video-flow`, built on `main` and independent of `redesign/home-v2`. If
the owner prefers Home v2, this branch is dropped as a whole. Nothing on `main` refers to it.

**Phases.** 1 (this branch, done): spec, motion tokens/tiers/frame monitor, morph overlay skeleton, shared-element
spike, benchmark placeholders, classic 3/5/7 min. 2: designs still missing (invite icon, illustrations final art) +
components. 3: screens, wiring, benchmarks filled in.

## 1. Home

- **Header:** LEXI logo (left) + total score right-aligned: "12.480" (big) over "toplam puan" (small). **No streak
  anywhere on Home.**
- **Play card** (red, 3 dp ink border + offset hard shadow; the whole card is one button): top label
  "SEVİYENİ, KATEGORİNİ VE MODUNU SEÇ"; three informational mode boxes, not tappable: Klasik "3 dk", Pratik "süresiz",
  Hardcore "tek hata"; then big "OYNA" (left) + round play icon (right). Tap → "Çarkını seç" (§2).
- **Günün çarkı:** the Home v2 card (`home-v2-notes.md`): title + description, right badge "Çöz ›" with an empty ring;
  completed: "Tamamlandı ✓" with a filled check and "N puan · yeni çark …"; unavailable: muted. No month, no day
  number, no streak. Built on this branch from `main`'s code, not from the Home v2 branch.
- **"İki kişi, tek telefon":** exactly as the current app (red square + blue circle icon, subtitle "Arkadaşınla
  sırayla yarış").
- **ÇALIŞ:** Defter (white card, blue circle shape, due count "24 tekrar") and Sınavlar (dark card, yellow diamond,
  subtitle "LGS İngilizce"), the same shape icons the current Home uses.
- **Bottom bar ("thin ruled"):** 4 dp ink top rule, icon + label, no boxes, three items: İstatistik
  (`LexiIcons.Stats`), Davet et, Ayarlar (`LexiIcons.Settings`). No Home tab.
- **Davet et:** new invite icon (person + plus) — **needs design** in `16-components` style. Opens the system share
  sheet with a ready message + the landing link in the app language (`AppLinks.landing(turkish)`); analytics `share`
  with param `type=invite` (today `AnalyticsEvent.Share` has no params: add one, keep the result-share call as
  `type=result`); a11y label "Arkadaşını davet et".
- **No entrance animation** on the dashboard (the splash already animates).

## 2. "Çarkını seç" (categories)

- Back button (icon vertically centred in its box), title "Çarkını seç".
- Level chips A1 · A2 · B1 · B2 · C1 · NAT, equal width, selected = ink fill with yellow text.
- 2-column grid (`LazyVerticalGrid`) of category cards: **Karışık first**, then **all** categories of that level from
  `content/categories.csv` in `sort` order — never hide a category (no "empty" filtering). NAT shows only Karışık
  (its single `general` category is what Karışık plays).
- Card = its own illustration (top, coloured field) + name + "learned / total" + progress bar. Illustrations: 19
  categories (`art_v3.png`), drawn from Bauhaus primitives (rect, circle, leaf, triangle, lines) as cached Canvas
  `Path`s — **no bitmap assets**. Final art **needs design sign-off** (art_v3 is a first draft).
- Bottom: section "SINAV PAKETLERİ" / "Sınava çark çevirerek hazırlan": LGS İngilizce open (with its progress);
  YDS / YÖKDİL, YKS-DİL, TOEFL, IELTS locked: art dimmed, lock badge, "YAKINDA" tag, not tappable (a11y: "…, yakında").
- Floating "Başa dön ↑" pill (ink, bottom-right) once scrolled past the first screen; scrolls the grid to the top.

## 3. Mode and duration dialog

Opens by flipping the tapped card (§5 #13–#14). Header: back arrow + category name, "B2 · 20 kelime · 6 öğrenildi".
- **MOD** segmented control: Klasik / Pratik / Hardcore; one sentence below:
  Klasik "Süre dolana kadar olabildiğince çok kelime bil." · Pratik "Süre yok, istediğin kadar rahat çalış." ·
  Hardcore "Tek hata hakkın var. İlk yanlışta oyun biter."
- **SÜRE** segmented control: 3:00 / 5:00 / 7:00 (180 / 300 / 420 s), default 3:00. Applies to Klasik **and**
  Hardcore. In Pratik the row is disabled (muted) with "Pratik modda süre yok." Daily stays 180 s.
- **OYNA ▸** (red, full width) → letter morph (§5 #16).
- **Engine (done in phase 1):** `GameConfig.classicTimeLimitOptionsSeconds = [180, 300, 420]`; a stored 120 / 240 s
  falls back to 180 s (same pattern as the duel clock in 1.0.11; `GAME_RULES.md` §5). **Phase 3:** Hardcore uses the
  chosen duration too (today `hardcoreTimeLimitSeconds` is fixed 180 s): pass the chosen limit per game.
- **Settings "Klasik süre" row — proposal: remove it.** The dialog becomes the only place to pick a duration and
  remembers the last choice in the same DataStore key (`classic_time_limit_seconds`, so no migration) as its
  preselection for Klasik and Hardcore. Keeping both would show one value in two places with two meanings. (Owner
  decision, see Open items.)

## 4. Copy (TR; EN in `values`)

New strings: play card label, mode box captions, "Çarkını seç", "Sınav paketleri" header + subtitle, "YAKINDA",
"Başa dön", dialog sentences above, "Pratik modda süre yok.", "Davet et" + invite share text + a11y label,
"toplam puan". EN wording to be written with the strings in phase 3.

## 5. Motion

Catalogue rows #13–#17 in `motion.md`; tokens in `LexiMotion.flow` (`FlowMotion`). **All durations are the prototype's
values ÷ 0.86** (the prototype ran at `SPEED = 0.86`), converted by `MotionStep.fromPrototype`, e.g. lift 120 → 140 ms,
flip 160 → 186 ms, morph expand/shrink 300 → 349 ms; letter morph total (expand start → shrink end) 930 ms.
- Press (#1, 80 + 80 ms), screen transitions (#12, 260 ms slide + fade) and the wheel marker jump (#6, 140 ms) keep
  their shipped tokens (see Open items).
- **Clock start:** the shrink starts only when the game is ready (data loaded and the board composed under the ink:
  `MorphOverlayState.markReady()` + the marker anchor laid out); the game clock and input start when the board
  entrance has settled (`FlowMotion.boardSettledMillis` after the shrink start), never before the overlay's
  `onFinished`.
- **Reduced motion** (tier Minimal): every flow transition is a 120 ms cross-fade; nothing moves, scales or rotates.

## 6. Performance

- Animate only in the draw phase: values read inside `graphicsLayer { }` / `drawBehind { }` lambdas; no
  recomposition or layout per frame; only translation / scale / rotation / alpha; never animate width, height,
  padding or blur.
- Letter morph: the ink is **one full-screen layer** scaled and translated onto the current rectangle; the big letter
  is measured once at 230 sp and scaled. Implemented in `MorphOverlayHost` (`core:designsystem`, see ADR 0027).
- Card flip: `rotationY` + `cameraDistance` on the card and dialog layers; the dialog's final layout is measured once
  and the grow from the card rect is a scale + translation of that layer (content counter-scaled or faded in after).
- Category illustrations: `Path`s built once per category (remembered / cached), drawn with `drawPath`.
- `LazyVerticalGrid` with stable keys (category id) and `contentType`.
- The game is prepared in the background during the morph (`GamePreparer` already prepares a game before navigation);
  the NavHost switches to the game under the ink after the expand, without the #12 slide.
- **Quality tiers** (`MotionTier`, `MotionPolicy`): Full / Lite / Minimal. Lite keeps timing and structure but drops
  tilt and shadow growth, replaces Y flips with scale 0.92 → 1 + fade, drops the letter pop, and lets staggered groups
  enter together. Automatic downgrade: 3 consecutive frames > 24 ms during a registered transition (`FrameMonitor`)
  ⇒ Lite for the rest of the session. Only the moving parts are registered: for the morph the expand + letter pop and
  the shrink, **not the hold** (the game screen is composed under the ink then, and its first frames must not
  downgrade the session). Android `isLowRamDevice` and iOS Low Power Mode start in Lite. Reduced motion
  ⇒ Minimal.
- **Acceptance:** macrobenchmark `FrameTimingMetric` P95 < 16.6 ms and no frame > 100 ms per journey
  (`benchmark/.../VideoFlowBenchmark.kt`, placeholders until phase 3). Device matrix: iPhone 8, SE (2nd gen), 11,
  current iPhone; Android 8 low-RAM, a Samsung A-series, a Pixel. iOS has no macrobenchmark: measure with Instruments
  (Animation Hitches) on the same journeys.

## 7. Open items

1. **Settings "Klasik süre" row:** remove (proposal, §3) or keep as the dialog's default? Owner decision.
2. **Shipped tokens vs ÷ 0.86:** press (#1) would be 93 ms, screen transition (#12) 302 ms and marker jump (#6)
   163 ms if scaled. Kept at 80 / 260 / 140 ms so the whole app stays consistent; confirm.
3. **Invite icon** (person + plus): needs design in `16-components` style; until then build it from
   `LexiIcons.Settings`' stroke rules and flag "needs design".
4. **Category illustrations:** `art_v3.png` is a first draft; final shapes/colours need design sign-off before the
   Path code is frozen. YDS / YKS-DİL / TOEFL / IELTS locked art (graduation cap, EN sheet, globe, …) is in
   `cats_v3.png` only partly (IELTS is cut off): needs design.
5. **Category grid entrance:** the prototype staggers the cards (45 ms prototype → 52 ms) on entering "Çarkını seç";
   not in the approved catalogue, so not tokenised. Include?
6. **Hardcore duration** (§3) changes Hardcore scoring comparability (longer games ⇒ more points): accept, or keep
   Hardcore at 3:00 only?
7. **Android battery saver** as a Lite signal (iOS Low Power Mode counterpart)? Not requested; not implemented.
8. **Mixed in NAT:** NAT has one content category (`general`); Karışık = that category. Confirm no separate card.
9. **Screenshot golden** `LetterMorphLayer.png` was recorded locally (macOS); re-record on Linux with the
   `screenshots-record` workflow before this branch is merged.
