# Video flow — notes (proposal, branch `redesign/video-flow`)

Single source of truth for the "video flow" redesign: Home → "Çarkını seç" → mode/duration dialog → letter morph
into the game. Owner-approved prototype in `drafts/video-flow/` (`flow.html`, `lexi-flow-prototype-v2.mp4`,
`home_v5.png` = current approved screens, `cats_v3.png` = categories + exam packs + dialog states, `art_v3.png` = the
19 category illustrations, `home_v4.png` = previous iteration, reference only). ADR 0027 records the decisions.

**Reversible.** Everything lives on `redesign/video-flow`, built on `main` and independent of `redesign/home-v2`. If
the owner prefers Home v2, this branch is dropped as a whole. Nothing on `main` refers to it.

**Phases.** 1 (done, tag `video-flow-phase1`): spec, motion tokens/tiers/frame monitor, morph overlay skeleton,
shared-element spike, benchmark placeholders, classic 3/5/7 min. 2 (done, tag `video-flow-phase2`): the screens
without the new transitions (§8). 3: card flip (#13–#15), letter morph (#16–#17), benchmarks filled in.

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
- **Engine:** `GameConfig.classicTimeLimitOptionsSeconds = [180, 300, 420]`; a stored 120 / 240 s falls back to
  180 s (`GAME_RULES.md` §5). Hardcore uses the chosen duration too (owner decision; phase 2): `GameRoute.time`
  carries it, else the remembered choice; a game keeps its limits for restore (`game.classicLimitSeconds`,
  `game.hardcoreLimitSeconds`; a Hardcore game saved before this has no Hardcore key and keeps 180 s).
- **Settings "Klasik süre" row: removed** (owner decision). The dialog is the only place to pick a duration and
  remembers the last choice in the same DataStore key (`classic_time_limit_seconds`, so no migration) as its
  preselection for Klasik and Hardcore.
- **Remembered selection** (`Settings`): level `selected_level` (the onboarding's key, so the onboarding / level test
  choice seeds it), mode `play_mode`, category `play_category` ("" = Karışık), time `classic_time_limit_seconds`.
  Unknown or removed values read as the defaults (A1, Klasik, Karışık, 3:00); a level change resets the category to
  Karışık; a stored category the level does not have reads as Karışık.

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
- **Grid entrance (#18, owner decision):** the cards of "Çarkını seç" rise 16 dp and fade in, 52 ms apart, on the
  first open per app session and only in tier Full (`FlowMotion.gridCardIn`); the run is registered with the
  session's `FrameMonitor` (`LocalMotionPolicy`).
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

## 7. Owner decisions (phase 1 open items) and what is still open

Decided: (1) Settings "Klasik süre" row removed; (2) tokens #1 / #6 / #12 stay 80 / 140 / 260 ms; (3) invite icon
built from the `LexiIcons` stroke rules (`LexiIcons.Invite`), **needs design**; (4) `art_v3.png` implemented as the
first draft (`CategoryArt`), **needs design**; (5) grid entrance stagger included: motion #18, first open per app
session, tier Full only; (6) Hardcore uses the same 3 / 5 / 7 choice as Klasik; (7) no Android battery-saver signal;
(8) NAT shows only the Karışık card; (9) goldens are written as tests and recorded on CI only (list in §8).

Still open:
1. **Design sign-off:** invite icon; the 19 category illustrations and the exam art (`ExamArt`; the IELTS art is a
   placeholder: speech bubble + headphones, not in the drafts).
2. **Home header logo** uses a new `LexiLogoSize.Header` (38dp blocks, from `flow.html`); `home_v5.png` has no
   pixel spec for the total-points type size (built 24sp / 12sp).
3. **Play card caption** "3 dk" shows the remembered time ("5 dk" after choosing 5:00); the draft shows a fixed
   "3 dk". Confirm.
4. **Touch targets:** the dialog's segments and the back buttons are 48dp (draft 46 / 44) to meet the 48dp rule.
5. **Old components** only the deleted screens used (`StartButton`, `LevelCard`, `CategoryCard`, `MixedWheelCard`,
   `StreakChip`, `PlayButton`, the old `DailyCard`) stay in `core:designsystem` (gallery and its goldens) until the
   flow is merged; remove them then.

## 8. Built in phase 2 (no new transitions)

- **Home** (`feature:home`): §1 as specified; content scrolls above the ruled bar, the play card's text is capped at
  1.3× so it stays on the first screen of a 320 × 568 phone at 200 % font size; "Davet et" shares the invite text
  with `AppLinks.landing(app language)` through `ShareService.shareText` and tracks `share` with `type=invite` once
  the sheet was shown (`ANALYTICS.md`; an enum code, no new data type).
- **"Çarkını seç"** (`feature:levels`, `picker` package; `CategoryPickerRoute(level)`): replaces `LevelSelectRoute`
  and `CategoriesRoute` (deleted). `LazyVerticalGrid` with keys and content types; LGS starts its wheel as before;
  locked YDS / YÖKDİL, YKS-DİL, TOEFL, IELTS are information only.
- **Dialog** (`PlayDialog`): phase 2 placeholder presentation (fade + scale 0.92 → 1 over `dialogGrow`, cross-fade
  with reduced motion); OYNA carries `morphAnchor(FlowMorphKeys.PLAY_BUTTON)`; the wheel's current letter carries
  `FlowMorphKeys.WHEEL_MARKER`. System back closes the dialog. The header (back arrow, name) and OYNA are pinned;
  the mode and time choices between them scroll (OYNA always reachable at 320 × 568, 200 %).
- **Large fonts:** no label breaks a word or is cut off (layout tests at 320 × 568 … 412 × 915, 100 / 130 / 200 %,
  TR + EN, with `WordSplitRecorder`). Home's points and "toplam puan" and the dialog's segments follow the font size
  up to 1.3× and shrink to one line; the dialog's title (wraps between words) and OYNA are capped at 1.3×; card names
  and the play card's mode captions shrink only when a word would not fit (`WordSafeText`).
- **Navigation:** Home → `CategoryPickerRoute(level)` → `GameRoute(level, mode, category, time)`; the perf tour goes
  Home → CategoryPicker → Game.
- **Tests:** view models (selection persistence, level change, time fallback, Practice, NAT, locked packs, invite),
  layout matrices for Home and "Çarkını seç" + dialog at 320 × 568, 360 × 640, 390 × 844, 412 × 915, font scale
  1.0 / 1.3 / 2.0, TR and EN (primary actions on screen, every button ≥ 48dp), behaviour / semantics tests.
- **Goldens for CI to record** (`screenshots-record`; not recorded on macOS):
  - `core/designsystem`: `CategoryArt.png`, `FlowComponents.png`, `FlowComponents_w320_fontScale200.png`
    (+ `LetterMorphLayer.png` from phase 1, recorded on macOS: re-record).
  - `feature/home`: `HomeScreen.png`, `HomeScreenEmpty.png`, `HomeScreenDailyPlayed.png`,
    `HomeScreenDailyUnavailable.png`, `HomeScreenDailyGiftNotice.png`, `HomeScreenEn.png`,
    `HomeScreen_w320h568_fontScale200.png`, `HomeScreen_w360h640_fontScale130.png` (the old Home goldens were
    deleted).
  - `feature/levels`: `CategoryPicker.png`, `CategoryPickerNat.png`, `CategoryPickerError.png`, `CategoryPickerEn.png`,
    `CategoryPicker_w320h568_fontScale200.png`, `PlayDialogClassic.png`, `PlayDialogPractice.png`,
    `PlayDialogHardcoreEn.png`, `PlayDialog_w320h568_fontScale200.png` (the LevelSelect / Categories goldens were
    deleted).
  - `feature/settings`: every `Settings*.png` (the row is gone; deleted, re-record).
  - `tools/store-graphics` now reads `feature/levels/CategoryPicker.png` / `CategoryPickerEn.png`.

## 9. Built in phase 3 (transitions and performance)

- **Card → dialog (#13–#15):** `PickerDialogFlip` drives `DialogFlipMachine`; Full: lift, Y flips with a 900 dp
  perspective, the dialog's frame grows from the card rect, rows rise one after another; Lite: scale 0.92 → 1 + fade;
  Minimal: 120 ms cross-fade. Interruptible (a close mid-open and a reopen mid-close play the same timeline
  backwards); back, the arrow and the scrim always close; the run is registered with the session `FrameMonitor`.
- **Letter morph (#16):** `LexiNavHost` hosts the NavHost in `MorphOverlayHost`; OYNA starts the morph
  (`LetterMorphLauncher`), the game is prepared during the expand and navigated to under the ink without a
  transition; the shrink waits for the loaded game (`markReady`) and the marker; the letter pulses meanwhile; the game
  clock and input wait for the board entrance (`GameIntent.HoldStart` / `ReleaseStart`). Back or the app going to the
  background cancels it (120 ms fade, no navigation once cancelled; back after the game was opened leaves it again);
  a navigation that does not happen after all also cancels it. Under the ink the content takes no touch and is hidden
  from screen readers. Minimal: no morph, the cross-fade.
- **Board entrance and #17:** every board piece enters from the shrink start (`BoardEntrance`); the score counts up
  with a pop, the clue slides, slots and keys are re-dealt, the centre letter leaves down and the next comes from
  above. Hidden keys take no tap during a re-deal.
- **#18 grid entrance** (phase 2) unchanged; Home has no entrance; other screen changes keep #12.
- **Performance:** draw-phase only; `FlowWarmUp` compiles the flip and morph shaders on Home; low-RAM / Low Power Mode
  start in Lite, a slow streak (3 frames > 24 ms during a registered transition) switches the session to Lite; debug
  builds have a tier switch and a frame-time graph (`MotionDebugOverlay`, shown from `androidApp/src/debug`). Numbers: `docs/PERF.md` § Video flow.
- **Goldens:** no new screenshot test and no golden change at rest (the motions start from and end on the resting
  look; `verifyRoborazzi` of `core/designsystem`, `feature/levels` and `feature/game` matched on macOS except the two
  known macOS failures `GameMotionComboBurst` and `GameMotionPassTurn`, which also fail on the phase 2 commit).
- **Recording:** `drafts/video-flow/actual/flow-android.mp4` (emulator).

Open after phase 3:
1. **Real devices:** Instruments (Animation Hitches) on iPhone 8 / SE 2 / 11 / current and the macrobenchmark on an
   Android 8 low-RAM phone, a Samsung A-series and a Pixel (`docs/PERF.md`); the emulator's P95 is 17.5–17.9 ms
   (RenderThread buffer wait), the bar is P95 < 16.6 ms.
2. **Owner check of the feel** against the prototype video, especially the pulse (new), the #17 motions on pass and
   after a wrong answer (the prototype only showed a correct answer), and the dialog header rising with the rows.
3. **#6 token:** the marker still jumps in one step; `wheelAdvanceDurationMillis` (140 ms) is no longer used by the
   centre letter (#17 steps now). Keep or remove when the flow is merged.
4. **A key tap dropped during a re-deal** still plays the key's press, haptic and sound (≤ 0.5 s after a turn
   change); acceptable or silence it.
5. **Navigation test gap:** `LexiNavigator.openGame`'s `onDropped` and the picker's back-handler gate are covered by
   reading and the launcher's unit tests, not by a NavHost integration test (GamePreparer is internal to
   `feature:game`).

