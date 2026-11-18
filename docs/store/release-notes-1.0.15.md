# Release notes 1.0.15 (draft)

Status: draft, not released. Nothing is bumped or tagged until the owner says GO RELEASE. Limits: App Store "What's New" ≤ 4000 characters; Play release notes ≤ 500 characters per language. Lengths (counted): App Store TR 573 / EN 547; Play tr-TR 373 / en-US 360.

## What changed in the app
- Categories rebalanced (content version 41): clearer, more even categories. New: Duygu ve karakter, Toplum ve hukuk, Para ve ekonomi, Giysi ve alışveriş, Hobiler ve eğlence, Fiiller, Sıfatlar ve zarflar, Fikirler ve kavramlar. Merged or removed because they were too thin: Spor, Müzik, Film ve dizi (now Hobiler ve eğlence), Teknoloji (now İş ve teknoloji), Yalancı eşdeğerler.
- 42 new words (sport, music, film, money, law and society).
- With a large system font (1.3 and up) the category list is one column; a few English category names are shorter.
- No change to progress, notebook, stats or saved games: card ids are unchanged, only the category of a card moved.

## App Store: What's New

### TR
```
Kategoriler yeniden düzenlendi: daha net ve dengeli.

• Yeni kategoriler: Duygu ve karakter, Toplum ve hukuk, Para ve ekonomi, Giysi ve alışveriş, Hobiler ve eğlence, Fiiller, Sıfatlar ve zarflar, Fikirler ve kavramlar.
• Çok az kelimesi olan kategoriler kaldırıldı veya birleştirildi (Spor, Müzik, Film ve dizi artık Hobiler ve eğlence içinde).
• Spor, müzik, film, para ve hukuk konularında 42 yeni kelime.
• Büyük yazı boyutunda kategori listesi tek sütun olarak daha rahat okunuyor.

İlerlemen, defterin ve istatistiklerin aynen duruyor: öğrendiğin kelimeler kaybolmaz.
```

### EN
```
Categories were rebalanced: clearer and more even.

• New categories: Feelings & traits, Society and law, Money & economy, Clothes and shopping, Hobbies and fun, Verbs, Adjectives & adverbs, Ideas and concepts.
• Categories with too few words were removed or merged (Sports, Music and Film & TV now live in Hobbies and fun).
• 42 new words about sport, music, film, money and law.
• With a large system font the category list is a single, easier-to-read column.

Your progress, notebook and stats are unchanged: the words you learned stay learned.
```

## Play Console release notes (≤ 500 characters each)

### tr-TR
```
Kategoriler yeniden düzenlendi: daha net ve dengeli.
• Yeni: Duygu ve karakter, Toplum ve hukuk, Para ve ekonomi, Giysi ve alışveriş, Hobiler ve eğlence, Fiiller, Sıfatlar ve zarflar, Fikirler ve kavramlar.
• Çok az kelimeli kategoriler kaldırıldı veya birleştirildi.
• 42 yeni kelime.
• Büyük yazı boyutunda tek sütun.
İlerlemen, defterin ve istatistiklerin aynen duruyor.
```

### en-US
```
Categories were rebalanced: clearer and more even.
• New: Feelings & traits, Society and law, Money & economy, Clothes and shopping, Hobbies and fun, Verbs, Adjectives & adverbs, Ideas and concepts.
• Categories with too few words were removed or merged.
• 42 new words.
• Single column with large system fonts.
Your progress, notebook and stats are unchanged.
```

## Store declarations (privacy, data safety, permissions)
Checked against `git diff v1.0.14..HEAD`: no change to `gradle/libs.versions.toml`, any `build.gradle.kts`, `AndroidManifest.xml`, `Info.plist` or a privacy manifest. The release contains only card CSV and `categories.csv` content, two string resources and one layout change in `feature/levels`. So it adds no SDK, no permission and no data type; Play "Data safety" and App Store "App Privacy" stay as they are (CLAUDE.md rule 11: nothing to re-check). No ads, Pro, login or user-to-user feature is touched.

## Owner checklist (TestFlight and a real phone)
1. Fresh update over 1.0.14 (not a clean install): the app opens, no crash, progress and streak are the same.
2. Old category screen at A1, B1, B2 and C1 (and A2): the new categories are listed; no empty tile; each tile opens a game. Expected counts without Karışık: A1 14, A2 14, B1 15, B2 12, C1 9 (B2 and C1 changed with the rebalance).
3. Start a game in a new category (e.g. Hobiler ve eğlence at B2, Toplum ve hukuk at B2, Fiiller at B1): the wheel fills all 26 letters and the words match the topic.
4. Karışık çark at each level: it still fills 26 letters.
5. Notebook (Defter): words learned before the update are still shown as learned; spaced-repetition due counts look normal.
6. A saved game from before the update: continue it; it must not crash or show an empty wheel.
7. Stats screen: category progress shows plausible numbers.
8. Settings > larger text (1.3 and above): the category list is one column and no name is cut off or split; also check a small phone if you have one.
9. English UI: names read "Animals & nature", "Adjectives & adverbs", "Business & tech", "Money & economy", "Feelings & traits", "Colors and numbers".
10. Check the Turkish and English texts above, then paste them into App Store Connect (What's New) and the Play Console (release notes). Production publishing stays with the owner.
