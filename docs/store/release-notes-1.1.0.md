# Release notes 1.1.0 (draft)

Status: draft, not released. Nothing is bumped or tagged until the owner says GO RELEASE. Covers everything since the
last published store version, **1.0.13** (owner, 2026-11-18): 1.0.14, 1.0.15 and the video flow (ADR 0027).
Limits: App Store "What's New" ≤ 4000, Promotional Text ≤ 170, Play release notes ≤ 500 characters per language.
Lengths (counted with `len()`): see the line under each block.

## What changed in the app since 1.0.13
- New Home and a new way into a game: Home → "Çarkını seç" → tap a category card, it flips into the mode / time
  dialog → OYNA → the category's first letter morphs into the wheel and the board enters.
- Classic and Hardcore can be played with 3, 5 or 7 minutes, chosen in the dialog (the Settings "Klasik süre" row is
  gone). The last choice is remembered.
- Total score on Home; "Arkadaşını davet et" opens the system share sheet with the store link (no account, no data).
- Category art for all categories, including the 8 new ones; categories without cards at the selected level are hidden.
- Categories rebalanced (1.0.15): Duygu ve karakter, Toplum ve hukuk, Para ve ekonomi, Giysi ve alışveriş, Hobiler ve
  eğlence, Fiiller, Sıfatlar ve zarflar, Fikirler ve kavramlar; thin ones merged or removed; 42 new words.
- Thin category wheels are filled from the whole level so every wheel has 26 letters (1.0.14).
- Large system fonts: words never split, the dialog stays reachable; Reduce Motion / Low Power use lighter animations.
- Smoother start and animations on Android (baseline profile with the new flow).
- Progress, notebook, stats and saved games are unchanged: card ids did not change.

## App Store: Promotional Text (≤ 170)

### TR
```
Yeni Lexi: çarkını seç, kartı çevir, harf çarka dönüşsün! Klasik ve Hardcore'u 3, 5 veya 7 dakikayla oyna.
```
TR: 106

### EN
```
The new Lexi: pick your wheel, flip the card and watch the letter become the wheel. Play Classic and Hardcore for 3, 5 or 7 minutes.
```
EN: 132

## App Store: What's New (≤ 4000)

### TR
```
Lexi 1.1 ile oyuna giriş baştan tasarlandı.

• Yeni ana ekran: toplam puanın, çalışma bölümü ve alt menü tek bakışta.
• "Çarkını seç": kategoriyi seç, kart çevrilsin, modu ve süreyi seç, OYNA'ya bas; harf çarka dönüşür ve oyun başlar.
• Klasik ve Hardcore artık 3, 5 veya 7 dakikayla oynanabiliyor. Son seçimin hatırlanır.
• Her kategoriye yeni bir çizim, yeni kategoriler dahil.
• Kategoriler yeniden düzenlendi: Duygu ve karakter, Toplum ve hukuk, Para ve ekonomi, Giysi ve alışveriş, Hobiler ve eğlence, Fiiller, Sıfatlar ve zarflar, Fikirler ve kavramlar. Çok az kelimeli kategoriler birleştirildi.
• 42 yeni kelime; her çark 26 harfin hepsiyle dolu.
• Seçtiğin seviyede kelimesi olmayan kategoriler gizleniyor.
• Arkadaşını davet et: mağaza bağlantısını istediğin uygulamayla paylaş.
• Büyük yazı boyutu, Hareketi Azalt ve Düşük Güç Modu için iyileştirmeler.

İlerlemen, defterin, istatistiklerin ve kayıtlı oyunların aynen duruyor.
```
TR: 937

### EN
```
Lexi 1.1 redesigns the way into a game.

• A new Home: your total score, the study section and the bottom bar at a glance.
• "Pick your wheel": choose a category, the card flips, pick the mode and time, tap PLAY; the letter turns into the wheel and the game begins.
• Classic and Hardcore can now be played for 3, 5 or 7 minutes. Your last choice is remembered.
• New artwork for every category, the new ones included.
• Categories were rebalanced: Feelings & traits, Society and law, Money & economy, Clothes and shopping, Hobbies and fun, Verbs, Adjectives & adverbs, Ideas and concepts. Categories with too few words were merged.
• 42 new words; every wheel is filled with all 26 letters.
• Categories without words at your level are hidden.
• Invite a friend: share the store link with any app.
• Better support for large text, Reduce Motion and Low Power Mode.

Your progress, notebook, stats and saved games are unchanged.
```
EN: 928

## Play Console release notes (≤ 500)

### tr-TR
```
Lexi 1.1: oyuna giriş baştan tasarlandı.
• Yeni ana ekran ve "Çarkını seç": kart çevrilir, mod ve süreyi seç, harf çarka dönüşür.
• Klasik ve Hardcore 3, 5 veya 7 dakika.
• Her kategoriye yeni çizim; yeni kategoriler ve 42 yeni kelime.
• Her çark 26 harfle dolu.
• Arkadaşını davet et.
• Daha akıcı animasyonlar, büyük yazı desteği.
İlerlemen ve defterin aynen duruyor.
```
tr-TR: 369

### en-US
```
Lexi 1.1 redesigns the way into a game.
• New Home and "Pick your wheel": the card flips, pick mode and time, the letter becomes the wheel.
• Classic and Hardcore for 3, 5 or 7 minutes.
• New art for every category; new categories and 42 new words.
• Every wheel has all 26 letters.
• Invite a friend.
• Smoother animations, large text support.
Your progress and notebook are unchanged.
```
en-US: 386

## Store declarations (privacy, data safety, permissions)
`git diff v1.0.13..HEAD` shows no change to `gradle/libs.versions.toml`, any `AndroidManifest.xml`, `Info.plist` or
privacy manifest: no new SDK, permission or data type. The invite uses the system share sheet with the existing store
link. Data safety / App Privacy stay as they are; because 1.1.0 is a feature release, still walk the list in
`docs/release-1.1.0-checklist.md` §3 before submitting (Ads = No, no `AD_ID`, ratings and audience unchanged, no
Pro wording, local reminders only, listing texts and screenshots).

## TestFlight / Play internal test checklist
1. Update over 1.0.13 (not a clean install): opens, no crash; progress, streak, notebook and stats unchanged.
2. Home: total score, study section above the bottom bar, Invite opens the share sheet with the store link.
3. "Çarkını seç" at A1, B1 and C1: every visible category has art (none falls back to Mixed), none is empty.
4. Tap a card: it flips into the dialog; tap again fast while it flips (no stuck card); back closes it.
5. Classic 3 / 5 / 7 min and Hardcore: OYNA → letter morph → board; the clock starts with the chosen time; the
   morph letter equals the wheel's first letter. Next game remembers the choice.
6. Back / swipe-back during the flip and during the morph; home button mid-morph and return; rotate or change the
   language or font size in system settings while the dialog is open.
7. Duel still works (turn change with a pass); a saved game from 1.0.13 continues.
8. Largest text size on the smallest phone: Home, picker and dialog readable, OYNA reachable, no split words.
9. iOS: Reduce Motion on (cross-fades, no morph) and Low Power Mode on (lighter flip): a game still starts correctly.
10. Android: an older or low-RAM phone feels smooth; no debug chip or frame graph anywhere in the build.
11. English UI: category names and "Pick your wheel" texts are not cut off.
12. Paste the texts above into App Store Connect and the Play Console; upload the new screenshots
    (`store-graphics`). Production publishing stays with the owner.
