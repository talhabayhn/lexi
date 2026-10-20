# Lexi — real-device test plan (Android + iOS)

> Scope: manual tests on physical devices. Unit/screenshot tests are covered by CI. Items marked **[after M7]** need
> RevenueCat / AdMob / UMP / Firebase / notifications (see `ROADMAP.md`). Rule sources: `GAME_RULES.md`, `PRODUCT.md`,
> `design/screen-map.md`. Mark each row ✅ / ❌ (bug id) / ➖ (n/a) per device and build.

## 0. Device matrix (minimum)
| Slot | Android | iOS |
|---|---|---|
| Low-end | 3–4 GB RAM, Android 8–10 (minSdk 26) | iPhone SE 2/3 (small screen), iOS 16 |
| Mid-range (perf reference) | Android 12–13, ~6 GB | iPhone 12/13, iOS 17 |
| Current | Android 14–15, Pixel-class | iPhone 15+, latest iOS |
| Big screen | tablet or "smallest display size" setting | iPad (any) |
Also: one device set to English UI, one to Turkish; one with font size at max; one on a slow/no network.

## 1. Smoke checklist (every build, ~10 min)
1. Fresh install launches: splash plays, no crash, home appears (< 1.5 s cold on mid-range Android).
2. Onboarding (3 steps) → level choice → home. Back/skip behave.
3. Home → OYNA → level select → start A1 Classic → answer some letters, pass one, finish or let time run out → result.
4. Result: score, counts, "Tekrar edilecekler" list, TEKRAR OYNA starts a new game, back to home.
5. Daily wheel opens, plays, result; opening it again shows the already-played state.
6. Notebook shows missed words; word detail sheet opens; review wheel starts when something is due.
7. Stats opens (empty state on a fresh install, numbers after a game).
8. Settings: sound / haptics / speak-on-correct switches work; language change applies.
9. Kill the app mid-game and relaunch: no crash, game resumes or is discarded cleanly (as designed).
10. Sound, haptics (Android + iPhone), TTS pronunciation audible.
11. Update install over the previous build keeps progress, notebook, streak, jokers.

## 2. Per-screen scenarios
Compare each screen with its PNG in `docs/design/screens/` side by side (spacing, colours, fonts, shadows).

| Screen | Scenarios |
|---|---|
| Splash `14` | Plays once, ≤ ~2 s, no white flash; reduced-motion shows a static logo |
| Onboarding `01` | 3 steps, progress indicator, skip, back; level test vs pick-myself; result card "Seviyen: X", change level; interrupted test (home button) restarts cleanly |
| Home `02` | Streak badge "N gün seri" hidden at 0, shown from 1; due-count badge; daily card state (new / played); tiles inert vs live as in screen-map; never scrolls at 320×568 |
| Level select `03` | Each level selectable; mode chips (Classic / Practice / Hardcore); "Kategoriler" link; level without full content marked/behaves per release decision |
| Categories `07` | Cards list, mixed wheel, empty level shows `EmptyState`, read failure retry |
| Game `04` | Wheel states (todo/current/correct/wrong/passed); keys place/remove letters; backspace; multi-word answers (fixed gaps); pass → returns next lap; wrong → reveal ~1.2 s, input blocked; timer, pause, background stops clock; combo badge at 3/5/10; Hardcore ends on first wrong; Practice shows "SÜRESİZ" |
| Joker sheet `05` | Each joker works once per slot; one joker per letter total; refused when cost ≥ time left; Listen disabled at C1/NAT; none in Daily and level test; inventory counts update |
| Pause dialog | Resume, restart (confirm, new seed), settings and back, give up (confirm → result) |
| Result `06` | Score maths (base × level, streak bonus, time bonus, Hardcore ×1.5); review list; share (after M6); Daily has no TEKRAR OYNA |
| Notebook `08` | Tabs Tekrar / Zor / Tümü, search, rows bars 1–4, word sheet (listen, remove), empty states |
| Stats `09` | Numbers, weekly chart, level bars, hardest letters (≥ 5 answers), empty and error states |
| Exam packs `10` | Free LGS opens; YDS/YÖKDİL shows Pro lock + A–E preview **[after M7]** |
| Settings `11` | All rows live after M7 (classic time, goal, reminder, languages, accent, Pro card); "YAKINDA" chips gone |
| Share card `12` | Renders 432×540 image, correct score/time/date, share sheet opens, image not blurry |
| Duel (M8) | Player setup, handover card hides the board, per-player clocks, TV-show turn rules, forfeit, result tie-breaks |

## 3. Daily wheel & streak edge cases
Rules: seed per local date + level; one scored attempt per local day; resets at **local midnight**; shared mixed-level
wheel; streak = consecutive local days with ≥ 1 completed wheel (Practice/duel excluded as per `GAME_RULES.md` §11).

| # | Scenario | Expected |
|---|---|---|
| D1 | Two devices, same date, same app version | Identical daily wheel (letters, clues, order) |
| D2 | Play daily, reopen it | Already-played state, no second scored attempt |
| D3 | Start daily at 23:58, finish after midnight | Counts for the day it started (verify and record actual); new day offers a new wheel |
| D4 | Midnight passes while app is in foreground on home | Next cold start/resume shows new daily; no stale "played" state (known: gifts run per cold start) |
| D5 | Change time zone forward (e.g. +3 h) crossing midnight | Day boundary follows device local time; no crash; no duplicate gift |
| D6 | Change time zone backward after playing | Stored last-gift day never goes back; no extra daily joker; streak not lowered |
| D7 | Set device clock forward 1 day, play, set back | Document behaviour; app must not crash or corrupt streak below its real value |
| D8 | Set clock back 1 day | No joker gift, no streak loss, no negative durations |
| D9 | Airplane mode all day | Everything works offline (daily wheel is local) |
| D10 | Streak 6 → play on day 7 | Streak 7; bonus joker arrives on next open; cap 5 per type respected |
| D11 | Skip one full day | Streak resets to 0 (badge hidden); longest streak kept in stats |
| D12 | Play only yesterday, open today before playing | Streak still shown (run ends yesterday); lost only after today ends |
| D13 | Daylight-saving change night (TR has none; test a device set to a DST zone) | Day lengths 23/25 h do not break day rollover |
| D14 | Auto time zone off, manual clock wrong, then fixed | No crash; document |
| D15 | Reminder (after M7): daily not played at reminder time | Max 1 notification/day, none in quiet hours |
| D16 | Android 13+: turn reminder on (or pre-prompt "Evet, hatırlat") | System notification dialog once; none at app start |
| D17 | Deny the permission | Switch stays off; sheet shows `settings_reminder_denied_hint` and an "open settings" button |
| D18 | Reboot with reminder on | Reminder still fires at the chosen time (re-armed on boot) |
| D19 | Change time zone (manual, then back) | Reminder re-armed; fires at the chosen local time |
| D20 | iOS (needs Mac): permission prompt, delivery | Prompt once; notification arrives at the chosen time with the app closed |

## 4. Content checks
1. Spot-check 30 random cards per level (A1…NAT): Turkish clue unambiguous, answer correct, **American spelling**
   (color, center, realize), no offensive content, no clue that leaks the answer.
2. Rule label matches card: "X ile başlar" vs "içinde X geçer"; Q/X/Z slots make sense.
3. Multi-word answers, hyphens, apostrophes render and accept input correctly.
4. Every level yields a full 26-slot wheel (no `SKIPPED`); note thin letters per level (A1: Q, Z, V, Y, I, J).
5. Example sentences contain the answer (blanked for the joker) and read naturally; TTS pronounces answer well.
6. Turkish characters (ç ğ ı ö ş ü İ) display and sort correctly in notebook search and clues.
7. Exam packs (LGS free, YDS/YÖKDİL Pro with A–E preview): counts shown are real; no pack leaks into normal level wheels.
8. Note anything for `content-review/` rather than fixing on the spot.

## 5. Ads, consent, purchases — sandbox **[after M7]**
| Area | Scenarios |
|---|---|
| UMP / consent | First launch in EEA/TR test geography shows form; accept, decline, change later in Settings; ATT prompt order on iOS (UMP first); no ad request before consent |
| Rewarded ad | Joker sheet button "Kısa reklam izle"; ad plays, close early = no reward, complete = +1 joker; no fill = message; cap 5 respected; never in Daily/level test; never mid-wheel unless user-initiated |
| Interstitial | Per `DECISIONS.md` open decision: if rewarded only, confirm none ever appears |
| Test ads only | Debug/internal builds use AdMob **test** units; never tap real ads |
| Purchases (Play licence testers / App Store sandbox) | Monthly, yearly, lifetime: buy → Pro unlocks immediately (no ads, packs, +3 jokers/day); trial wording matches store; cancel in store → expiry handled; restore on reinstall and on second device; refund/revoke removes Pro; pending/declined payment message; offline paywall message |
| Price display | Prices come from the store (never hardcoded), correct currency and locale |
| Pro gating | Pack locked when free, preview A–E works, unlock reflected without restart |
| Telemetry | Analytics events fire only with consent; Crashlytics test crash visible in dashboard on a debug build |

## 6. Interruptions
| Scenario | Expected |
|---|---|
| Incoming call / alarm mid-game | Timer stops (background), game intact on return |
| Home button / app switch 10 s and 10 min | Same; no double-count of time |
| Notification shade, control centre pull | Game pauses or is unaffected, no stuck key |
| Rotation (Android auto-rotate on; iOS unlock) | Layout stays portrait-correct or adapts; no state loss, no crash |
| Split-screen / Stage Manager / multi-window | Usable or gracefully letterboxed |
| Low memory (many apps open; Android "Don't keep activities") | Process death → resume without crash; saved game handled |
| Low battery / battery saver | Animations still finish; reduced-motion respected |
| Lock screen mid-game | Pauses; unlock resumes |
| Audio: music playing, headphones plug/unplug, silent switch (iOS), Do Not Disturb | Sounds/TTS respect silent mode; no crash on route change |
| Storage nearly full | App starts; DB copy failure shows an error, not a crash |
| Language switch in system settings while app is open | App follows on next start (UI default = system) |

## 7. Small screen, tablet, large font
1. Android 320×568, 360×640, 360×780; iPhone SE: home never scrolls, game never scrolls, wheel ≥ ~144 dp, all texts on screen.
2. Tall phones 412×915: layout uses space without gaps.
3. "Smallest display size" / iPad / tablet portrait: content column ≤ 430 dp (scaled), centred, decor edge-to-edge.
4. Tablet landscape: usable (dedicated layout comes later).
5. Font scale 1.0, 1.15, 1.3, max: game board capped at 1.15, lists honour up to 1.3; nothing clipped or overlapping.
6. Display size large + font large combined.
7. Notch / cutout / Dynamic Island / gesture bar: nothing under system UI.
8. Keyboard (IME) never appears in game; in notebook search it does not break layout.

## 8. Accessibility — TalkBack (Android) and VoiceOver (iOS)
- [ ] Every interactive element has a meaningful label (keys read the letter; backspace, pass, pause, joker, back).
- [ ] Focus order follows visual order; no focus traps in sheets and dialogs; dialogs announce their title.
- [ ] Wheel: current letter and state announced; correct/wrong/passed announced after an answer.
- [ ] Clue text read before the answer boxes; answer boxes announce filled/empty count.
- [ ] Score, timer and combo have content descriptions (timer not spammed every second; warns at ≤ 10 s).
- [ ] Switches expose on/off state; chips and tabs expose selected state; disabled items announced as disabled.
- [ ] Decor shapes are hidden from accessibility.
- [ ] Touch targets ≥ 48 dp / 44 pt; colour is never the only signal (shapes differ per slot state).
- [ ] Contrast of text on cream / yellow / blue / red backgrounds readable (check with the OS contrast tools).
- [ ] Reduced motion / "Remove animations" disables wheel spins, confetti and the splash movement.
- [ ] Haptics off honoured; mute honoured.
- [ ] Share card has an alt description; share sheet reachable.
- [ ] Full path completable eyes-closed: onboarding → game → result.

## 9. Performance targets (from `ROADMAP.md` M8 / `PRODUCT.md`)
| Metric | Target | How to check |
|---|---|---|
| Cold start (mid-range Android) | < 1.5 s to interactive home | `adb shell am start -W` ×5, median |
| Game screen frame rate | 60 fps, no jank during wheel advance, combo burst, reveal | GPU rendering profile / Android Studio profiler; Xcode Instruments on iOS |
| Memory | no growth over 10 consecutive games | profiler |
| Crash-free sessions | ≥ 99.5 % | Crashlytics once enabled |
| App size | note download size per store | Play / TestFlight |
| Battery | 10-min game session: no abnormal drain | OS battery stats |
| Content DB first-run copy | no visible freeze on low-end device | stopwatch |
| Retention goals (post-launch) | D1 ≥ 40 %, D7 ≥ 15 %, wheels/DAU ≥ 2, Pro ≥ 2 % | analytics |

## 10. Bug report quality (for the team)
Attach build number (Settings → About), device, OS, language, steps, expected vs actual, screenshot or screen recording.
Triage: **P0** crash / data loss / cannot play; **P1** wrong score, wrong answer accepted, streak/daily broken; **P2**
layout/visual; **P3** wording/typo.

---

# Kapalı test gönüllüleri için kısa rehber (1 sayfa, Türkçe)

Merhaba, Lexi'yi denediğin için teşekkürler! 12 kişilik kapalı testteyiz; amacımız oyunu yayınlamadan önce hataları
bulmak. Teknik bilgi gerekmiyor.

**Nasıl kuracaksın?** Sana gönderilen davet bağlantısını aç (Android: Google Play test bağlantısı, iPhone: TestFlight
daveti), uygulamayı yükle. Testin geçerli sayılması için uygulamayı **14 gün boyunca yüklü tut** ve mümkünse her gün aç.

**Ne yapacaksın?** Haftada en az 3 gün, günde 5–10 dakika oyna:
1. Günün Çarkı'nı oyna (günde bir kez).
2. Farklı seviyeleri dene (A1'den Native'e). Kendi seviyenden başla.
3. Klasik, Alıştırma ve Hardcore modlarını bir kez dene; jokerleri kullan.
4. Kelime Defterim'e bak, Tekrar Çarkı'nı oyna.
5. Uygulamayı oyun sırasında kapat, telefonu kilitle, arama gelsin; geri dönünce her şey yerinde mi?
6. İnternetsiz (uçak modu) dene.
7. Mümkünse yazı boyutunu büyüt ve telefonu bir kere yan çevir.

**Nelere dikkat et?**
- Uygulama çöktü ya da donduysa
- Puan, süre ya da seri yanlış hesaplandıysa
- Kelime/ipucu yanlış, belirsiz ya da yazım hatası varsa (kelime ve seviyeyi yaz)
- Yazılar taştı, kesildi ya da üst üste bindiyse
- Ses, titreşim ya da telaffuz çalışmıyorsa
- Anlaşılmayan bir yerde takıldıysan (bu da değerli geri bildirim!)

**Hata nasıl bildirilir?** Aşağıdaki şablonu kopyala, doldur ve bize e-postayla gönder (`[CONTACT EMAIL]`) ya da
sana verilen formu kullan. Mümkünse ekran görüntüsü veya ekran kaydı ekle. Bir hatayı bulamadığın kadar tekrar etmek
zorunda değilsin; "bir kere oldu" bile işimize yarar.

```
Başlık (tek cümle):
Telefon modeli:                Android / iOS sürümü:
Uygulama sürümü (Ayarlar → Hakkında):
Dil (Türkçe / English):
Ne yapıyordun? (adım adım):
1.
2.
3.
Ne bekliyordun?
Ne oldu?
Ne sıklıkla? (her seferinde / bazen / bir kere)
Ne kadar can sıkıcı? (oyun çöküyor / yanlış sonuç / görsel / küçük not)
Ekran görüntüsü veya video: (ekledim / eklemedim)
```

**Gizlilik:** Bize yalnızca sen gönderdiğin bilgiler ulaşır; şifre ya da kişisel bilgi yazma. Test bitince uygulamayı
silebilirsin. Emeğin için çok teşekkürler!
