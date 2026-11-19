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
  subtitle "LGS İngilizce"), the same shape icons the current Home uses. **Anchored (phase 5):** the section (label +
  both cards) sits 24 dp above the bottom bar, at the bottom of the content area, while the cards above stay at the
  top; when the content is taller than the space (short screens, large fonts) the spacer collapses and ÇALIŞ flows
  under the other cards with the usual scroll (the old 40 dp gap to the "İki kişi" box; 24 dp below the tiles).
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
  `content/categories.csv` in `sort` order, **except a category with no card at the selected level (phase 5: hidden;
  C1 Yalancı eşdeğerler / Deyimler / Phrasal verbs today)**. NAT shows only Karışık
  (its single `general` category is what Karışık plays).
- Card = its own illustration (top, coloured field) + name + "learned / total" + progress bar. Illustrations: 19
  categories (`art_v3.png`), drawn from Bauhaus primitives (rect, circle, leaf, triangle, lines) as cached Canvas
  `Path`s — **no bitmap assets**. Final art **needs design sign-off** (art_v3 is a first draft).
- Bottom: section "SINAV PAKETLERİ" / "Sınava çark çevirerek hazırlan": LGS İngilizce open (with its progress);
  YDS / YÖKDİL, YKS-DİL, TOEFL, IELTS locked: art dimmed, lock badge, "YAKINDA" tag, not tappable (a11y: "…, yakında").
- Floating "Başa dön ↑" pill (ink, bottom-right) once scrolled past the first screen; scrolls the grid to the top
  smoothly (motion #19: 400 ms ease-out whatever the distance, 120 ms with reduced motion, the user's drag takes over).

## 3. Mode and duration dialog

Opens by flipping the tapped card (§5 #13–#14). Header: back arrow + category name, "B2 · 20 kelime · 6 öğrenildi".
- **MOD** segmented control: Klasik / Pratik / Hardcore; one sentence below:
  Klasik "Süre dolana kadar olabildiğince çok kelime bil." · Pratik "Süre yok, istediğin kadar rahat çalış." ·
  Hardcore "Tek hata hakkın var. İlk yanlışta oyun biter."
- **SÜRE** segmented control: 3:00 / 5:00 / 7:00 (180 / 300 / 420 s), default 3:00. Applies to Klasik **and**
  Hardcore. In Pratik the row is disabled (dimmed in place) and the only visible no-timer text is the mode's description; "Pratik
  modda süre yok." is just the row's content description (phase 5). Daily stays 180 s.
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
  enter together. Full is the default and stays Full unless the device is really struggling; the automatic downgrade
  (`FrameMonitor`, phase 6, §12) is reversible: the first transition of each key per process is a warm-up and
  never counts; Lite after 2 separate transitions each with a slow streak (3 frames > 24 ms in a row) or one run
  of 5 slow frames; back to Full after 3 good transitions in a row. Only the moving parts are registered: for the
  morph the expand + letter pop and the shrink, **not the hold** (the game screen is composed under the ink then, and its first frames must not
  downgrade the session). Android `isLowRamDevice` starts in Lite. iOS Low Power Mode does **not** (after the 1.1.1 report:
  it kept every iPhone in Low Power on the scale + fade, so the card flip never ran although it plays smoothly
  there; only measured jank lowers Full). Reduced motion ⇒ Minimal. The frame rules only move between Full and
  Lite: they never override reduced motion, the debug `forcedTier` or low-RAM.
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
   placeholder: headphones + speech bubble, not in the drafts). Phase 4 redrew the weakest ones; review them on
   `drafts/video-flow/actual/art-sheet.png` (§10).
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
- **Performance:** draw-phase only; `FlowWarmUp` compiles the flip and morph shaders on Home (since 1.1.x also the
  real card mid-flip and the dialog mid-open, `CategoryFlipWarmUp`; iOS first launch: `docs/PERF.md`); low-RAM
  starts in Lite (Low Power Mode no longer does, §12), slow transitions switch the session to Lite (rules since
  phase 6: §12); a layer that fades content with hard shadows keeps room for them (`hardShadowOutsets`,
  `motion.md` § Layers); debug
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


## 10. Phase 4 (no device needed): art, robustness, engine investigation

**Art and icon (drafts, still need design sign-off).** Contact sheet of every card:
`drafts/video-flow/actual/art-sheet.png` (written by `ArtSheetRenderTest` to `core/designsystem/build/art-sheet/`; it
compares nothing and records no golden). Redrawn with the same primitives and tokens: Seyahat is an airliner seen from
above (the draft's plane was a faint stroke), Deyimler has proper closing quotation marks (ball with a tail), IELTS
is headphones around a speech bubble with yellow ear cups (still a placeholder), Yalancı eşdeğerler is rebalanced. A card
shows only the middle of the art on a narrow phone (cover scaling: at 320 dp 15.5 units of the 163 are cropped per
side), so Colours, School and False friends were pulled inside x 16–147; the Sports ball's seams now end on the circle
(they poked out). Stroke widths: 3 for outlines, 2.5 / 4 / 5 for glyph strokes as before. The invite icon's plus no
longer touches the head (gap 0.6 → 1.8 viewBox units). Checked at 163×95 dp, at 0.75x and cropped to 132 dp.
The LGS locked card's lock badge covers its top-right dot (unchanged, cosmetic).

**Robustness (tests added, one bug fixed).**
- *Bug:* the letter morph always showed "A", but a category without an A card opens its wheel on its first letter
  (13 of the 56 level × category pairs that have cards (59 are declared; false-friends, idioms and phrasal-verbs at C1 have none) have no A: Müzik B1/B2/C1, Spor B1/B2, Yalancı eşdeğerler B1/B2, Renkler ve sayılar
  A1, Film ve dizi C1, Deyimler B2, Şehir ve ulaşım B1, Vücut ve sağlık B2, Yiyecek ve içecek B1), so the big letter landed on a different marker. `PickerCategoryUi.firstLetter` and
  `StartGame.firstLetter` now carry the wheel's first letter into the morph. Tests: first letter per category, effect,
  launcher.
- *Orientation:* portrait only (Android `screenOrientation="portrait"` with `orientation|screenSize|…` in
  `configChanges`; iOS target is iPhone). Font scale and per-app language changes do recreate the activity.
- *Recreation / process death:* the morph state is `remember`ed, so a recreated activity has no overlay and the
  game, which `LetterMorphEntrance` finds with an idle morph, releases its clock at once (never a hold without a running
  morph, never ink left over). A dialog the view model still holds is shown at rest (no replay). Tests:
  `LetterMorphEntranceTest` (no morph, recreated, hold until the morph is over, cancel, load error, leaving the game
  mid-morph), `CategoryPickerFlipTest` (restored open).
- *Double OYNA:* the navigator drops taps while a game is preparing (`openGame`); the launcher test pins that the
  second tap starts no second morph and does not cancel the first.
- *Backgrounded mid-transition:* the morph cancels on ON_STOP (phase 3); a dialog flip resumes with the next frame's
  delta and ends at rest.

**Engine investigation (read-only, nothing changed).** What a category with few cards does today:
- `WheelGenerator.generate` builds 26 slots; a letter without a category card is `SKIPPED` and the wheel UI draws only
  playable slots (`GameUiMapper.map`). The category fallback (`alphabetWheel`, `fallback`) only helps a letter that *has*
  category cards, all seen recently; it never fills a letter with none ("a letter without category cards stays
  skipped", `GAME_RULES.md` §1). Every level has all 26 letters in `content/`, so a level-wide fill is always possible.
- Shipped content (letters with a card / cards): Yalancı eşdeğerler B1 **1 / 1** and B2 **1 / 1**, Müzik B1 8 / 9, B2 10 / 12,
  C1 10 / 10, Spor B2 11 / 17, Teknoloji C1 12 / 25; 41 more pairs have 13–25 letters and 8 have all 26.
- A one-card category is a **one-letter game**: no loader guard (only a wheel with zero playable slots is refused,
  `Opened.NoCards`), the engine starts, the first answer (right or wrong) leaves no open slot and ends the game
  `ALL_ANSWERED` = *completed*, so Classic pays the **time bonus** (1 point per remaining second, up to 180 / 300 / 420)
  and records a finished wheel for the streak, daily goal and "completed in N seconds" share card. Pass cycles on the
  same letter until the clock runs out. A 1-letter game is therefore also a points farm.
- **Smallest rule:** a category wheel with fewer than `minCategoryLetters` playable letters (⚙ 13, in `GameConfig`)
  (and has at least one category card, so a category without any card stays "no words": false-friends, idioms and
  phrasal-verbs at C1 today) takes every letter without a category card from the level's cards for that letter (the same candidates and
  `pickFresh` steps the recently-seen fallback uses). Categories with 13+ letters, mixed, packs, daily and review are
  unchanged, so no seed, golden or saved game moves; only the seven pairs under 13 letters change (Yalancı eşdeğerler B1 / B2,
  Müzik B1 / B2 / C1, Spor B2, Teknoloji C1). Per letter a category card still wins.
  The first letter becomes A for a filled wheel (the level always has an A), so the picker's `firstLetter` must use
  the same rule: expose it from the domain (`WheelGenerator.firstLetter(categoryLetters, config)`) instead of
  duplicating it in `CategoryPickerViewModel`.
- **Files:** `core/domain/.../game/WheelGenerator.kt` (the `generate` category branch and `alphabetWheel`),
  `GameConfig.kt` (`minCategoryLetters`), `feature/levels/.../CategoryPickerViewModel.kt` (`firstLetter`),
  `docs/GAME_RULES.md` §1, `docs/CONTENT.md` (thin categories). **Tests:** `WheelGeneratorTest`: a 1-card category
  gives 26 playable slots, own card on its letter, level cards elsewhere, one `nextInt` per non-skipped letter in A–Z
  order, deterministic per seed; a category at exactly the floor is unchanged; with history the recently-seen
  fallback still applies; mixed / pack unchanged. A content test (compiler or `core:data`) that every shipped
  (category, level) pair that has cards yields ≥ `minCategoryLetters` playable letters; one for a 0-card category staying empty. `GameViewModel`/loader: the thin category
  loads as a normal game. `CategoryPickerViewModelTest`: `firstLetter` for a filled wheel. Session recording and the
  category progress bar keep working: the filled cards belong to other categories ("counts toward its own category").
- **Alternatives:** (a) a floor-fill that adds only enough letters (A–Z order) to reach 13: smaller change of feel,
  an uneven wheel; (b) hide or disable categories below the floor in the picker (product call: the card exists in the
  draft with its art); (c) no time bonus when fewer than 26 letters were playable (does not make the game fun).

Open after phase 4: the art / icon sign-off (sheet above), the thin-category rule, the real-device pass and
the owner steps of `docs/release-1.1.0-checklist.md`.


## 11. Phase 5 (owner's device test): polish batch

1. **Home ÇALIŞ anchored** (see §1): the content column is at least as tall as the viewport (`BoxWithConstraints` +
   `heightIn(min)`), a `Spacer(weight(1f))` before the ÇALIŞ group takes the free height; bottom padding is 24 dp (was
   20, so the flowing layout also ends 24 dp above the bar). `HomeLayoutTest` (4 sizes × 3 font scales × TR/EN) now
   asserts per size that ÇALIŞ is either anchored (tiles end 24 design-dp above the bar's real top, `HOME_BOTTOM_BAR_TAG`)
   or flowing (40 dp below the duel box, tiles at or below the anchored position), and that 390 × 844 / 412 × 915 at 100 %
   are anchored. Gaps are measured in design dp: `LexiViewport` scales them by min(w / 390, h / 844) on bigger phones.
2. **"Başa dön" scroll** (#19, `animateScrollToTop` in `core:designsystem` `motion/`): see motion.md. Distance = rows
   above × row pitch (+ the first row's offset); beyond 1.5 viewports it is skipped in one `scrollBy` first, then a
   `tween(400, easeOut)` drives `scrollBy`, and a last `scrollToItem(0)` lands exactly (rows can differ in height). It
   runs at the default scroll priority, so a drag cancels it. Test: capped time from a short and a very long distance,
   120 ms, no-op at the top, a touch cancels it. Not registered with the frame monitor (a scroll is not a transition).
3. **Dialog never shifts:** the description cell lays out all three sentences (the other two transparent and without
   semantics), so it is as tall as the longest in the language and font scale; Pratik shows no second line (the row is
   dimmed in place, content description "Pratik modda süre yok."). `CategoryPickerLayoutTest` records the tops of the
   SÜRE label, the 3:00 box and OYNA in all three modes (4 sizes × 3 scales × TR/EN) and asserts they are identical (it
   fails on the old dialog: Pratik sat 44 px higher, OYNA 4 px lower at 100 %); `WordSplitRecorder` still guards it.
4. **First-use jank on the first flip:** `PickerDialogWarmUp` composes and draws the real dialog panel once per app
   process, posed mid-way through the open flip (rotation with perspective, grown frame, hard shadow, rows, segmented
   controls, OYNA), **behind the picker's opaque background** for 3 frames, ≈ 1.1 s after the grid entrance (0.4 s when
   there is none) and only when the grid is not scrolling, no dialog is up and the tier is not Minimal; no semantics, no
   input, no morph anchors. The baseline profile has a new `videoFlow` journey (`BaselineProfileGenerator`, steps shared
   with `VideoFlowBenchmark` in `VideoFlowJourney.kt`: picker → flip → dialog (mode switch, close by arrow and back,
   reopen) → morph → board), and `VideoFlowBenchmark.firstDialogFlip` measures only the first flip of a fresh process.
   Numbers and what they can and cannot show: `docs/PERF.md` § Phase 5.
5. **Empty categories hidden** (`CategoryPickerViewModel`): a category with no card at the selected level is not listed
   (Karışık stays). `content/categories.csv` is **not** edited: the validator only warns for a card at a level a category
   does not declare, so removing C1 from the three categories would pass, but any `categories.csv` change needs a
   `contentVersion` bump and collides with the parallel work on thin categories; the picker rule also holds for any
   future empty pair. The thin and unbalanced categories themselves are untouched.

**Goldens for CI to record after phase 5** (`screenshots-record`, not recorded here): `feature/home` `HomeScreen*.png` (ÇALIŞ
sits on the bottom on tall phones), `feature/levels` `PlayDialogPractice.png` (no second no-timer line) and every
`CategoryPicker*.png` / `PlayDialog*.png` whose content moved by the hidden empty categories or the reserved description
cell; `verifyRoborazzi` was not run on macOS for this batch.

## 12. Phase 6: reversible frame downgrade

Owner's emulator test: the old rule (3 frames > 24 ms in any registered transition ⇒ Lite for the whole session)
tripped on the first cold-start transition, so the flip disappeared for the session. Owner decision: Full is the
default and stays Full unless the device is really struggling; a downgrade must be reversible.

- **Warm-up:** `beginTransition(key)` takes a stable key per motion (`picker.flip.open`, `picker.flip.close`,
  `picker.gridEntrance`, `morph.expand`, `morph.shrink`). The first transition of each key in the process is not
  judged (JIT, class loading, first shader use), nor is anything overlapping it from the moment the cold key joins;
  a key stays cold until one of its transitions had at least 4 frames (a flip cancelled at once warms nothing up).
  The grid entrance plays once per session, so in practice it is never judged.
- **Verdict per transition** (first begin → last end): *slow* = a run of 3 frames > 24 ms; *good* = at least 4
  measured frames and no such run; shorter (e.g. cancelled at once) = no verdict.
- **Downgrade to Lite:** 2 slow transitions (not necessarily in a row), or 5 slow frames in a row in one transition (at
  once, mid-transition). 3 good transitions in a row while still in Full clear the slow count, so old evidence fades.
- **Recovery to Full:** 3 good transitions in a row (they are measured in Lite, which is cheaper; if Full is still too
  heavy the device downgrades again on fresh evidence, so a borderline device can alternate Full / Lite every ≈ 5
  transitions: owner to hand-test; if it shows, require more good transitions after each recovery). A slow
  transition restarts the count.
- **Priority** (`MotionPolicy.tierReason`): forced tier > reduced motion (Minimal) > low-RAM > frames > Full; tests
  cover that the frame rules never override the first three. iOS Low Power Mode was a fourth signal (Lite) up to
  1.1.1 and was removed: Full is the default and only evidence of real jank lowers it.
- **Debug chip** (`MotionDebugOverlay`, debug builds only): `auto · Lite: slow transitions x2 · good 1/3`,
  `auto · Lite: 5 slow frames in a row · good 0/3`, `auto · Full: default · recovered x1 · slow 1/2`,
  `Lite · Lite: forced`, `auto · Minimal: reduced motion`. Release-like builds compile the no-op `DebugTools`;
  the overlay is unreferenced there and R8 removes it (dex check on the `benchmarkRelease` APK: `docs/PERF.md`).
- **Tests:** `FrameMonitorTest` (fake frame clock: cold start ignored, 2-transition rule, 5-frame rule, recovery after
  3 good, evidence fading, short transitions) and `MotionPolicyTest` (cold start keeps Full; Minimal, forced and
  low-RAM never overridden; no power-related tier reason).

## 13. Phase 7: main merged (category rebalance)

- `git merge origin/main` (merge commit, tag `video-flow-phase7-merged`): content is main's (rebalance stages 1-4,
  contentVersion 40). Conflicts: main changed the old `CategoriesScreen`, its strings and its 4 `Categories_*` goldens,
  which this branch had deleted: deletion kept. None of the 33 old `feature/levels` string keys is used by the picker;
  the shortened English names live in `content/categories.csv`. main's `CategoriesLayoutTest` (old screen) is removed
  and ported as `CategoryPickerContentLayoutTest`.
- **Picker list per level** (declared in `categories.csv` and with cards): A1 14, A2 14, B1 15, B2 12, C1 9, plus
  Karışık and the exam cards (`ShippedPickerCountsTest` pins the numbers). B2 `daily-life` (4 cards) and `food-drink`
  (10) are not declared at B2, so hidden.
- **Layout** (`CategoryPickerContentLayoutTest`, every shipped name, 320/360/390 dp × 1.0/1.3/2.0 × TR/EN): nothing
  cut or split; names keep 14 sp at 1.0 and 18.2 sp at 1.3; at 2.0 `WordSafeText` shrinks the longest to 19 sp
  ("Clothes and shopping", 320 dp; TR smallest 21 sp), never below the 1.0 size, so the two columns stay.
- **First letter:** `WheelGenerator.firstLetter(categoryLetters, levelLetters, config)` is the engine's opening-letter
  rule (a thin category is filled, so it opens on the level's first letter); the picker uses it, and
  `ShippedCategoryWheelTest` checks it against every shipped wheel. With the rebalance no shipped category is thin.
- **Categories without art** (Mixed fallback via `CategoryArtKind.forId(...) ?: Mixed`, needs design):
  `clothes-shopping`, `hobbies`, `verbs`, `adjectives`, `economy`, `feelings`, `society-law`, `ideas`. Art for
  `sports`, `music`, `film-tv`, `technology`, `false-friends` is now unused (those categories were merged away).

