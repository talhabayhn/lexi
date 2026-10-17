# Lexi — ASO research (TR + EN)

Researched 2026-10-17. **Limits:** no keyword-volume tool was available and store pages were blocked in the sandbox, so *no search volumes or difficulty scores are sourced*. Intent and competitiveness below are **my estimates** (E) from competitor listing wording and general ASO practice; validate with AppTweak/Sensor Tower/Appfigures or Apple Search Ads keyword popularity before locking metadata. Character limits verified: Apple name/subtitle 30, promo text 170, keywords 100 ([Apple](https://developer.apple.com/app-store/product-page/), P); Play title 30 / short 80 / full 4000 (per `docs/store/check-limits.py`; Play help page not readable here). All variants below were length-checked with a script.

Constraints respected: no game/brand names (no Rosco/Pasapalabra/Passaparola-brand, no competitor names); exam names only in descriptive form and **only after the owner confirms** (see `docs/store/listing-*.md` notes and `owner-checklist.md` #8). Keywords sourced from competitor wording: [Kelibu](https://apps.apple.com/us/app/id6484268631), [İngilizce Kelimeler Öğren](https://apps.apple.com/us/app/-/id6447319999), [Yeni ingilizce kelimeler öğren](https://apps.apple.com/app/id6444373417), [İngilizce Mobil](https://apps.apple.com/us/app/id828435987) (they use YDS/YÖKDİL/YDT/TOEFL/IELTS, "kelime oyunu", "kelime ezberleme", "collocations"). Also [AppDrift TR guide](https://appdrift.co/app-store-optimization/turkey) (S): Google Play dominates in TR (claim unverified, treat as E).

## 1. Keyword candidates (estimates)

Intent: **I** = informational/learning, **G** = game/entertainment, **X** = exam, **B** = brand-free generic. Competitiveness: L/M/H (E).

### Turkish (App Store TR + Play TR)

| # | Keyword | Intent | Comp. | Note |
|---|---|---|---|---|
| 1 | ingilizce kelime öğren | I | H | Core generic; already in title |
| 2 | ingilizce kelime | I | H | Head term |
| 3 | kelime ezberle | I | H | High intent for memorisation |
| 4 | ingilizce kelime ezberleme | I | H | Longer variant |
| 5 | kelime oyunu | G | H | Competes with Turkish word games, not only English |
| 6 | ingilizce kelime oyunu | G+I | M | Sweet spot for Lexi |
| 7 | kelime çarkı | G | M | Format descriptor |
| 8 | harf oyunu | G | M | Wheel-of-letters feel |
| 9 | alfabe oyunu | G | M | |
| 10 | bilgi yarışması | G | H | Broad trivia; low relevance risk |
| 11 | ingilizce quiz | I+G | M | |
| 12 | ingilizce test | I | H | Test seekers, loose fit |
| 13 | seviye tespit | I | M | Matches placement test |
| 14 | ingilizce seviye testi | I | M | |
| 15 | a1 a2 b1 b2 | I | M | CEFR levels; used by competitors |
| 16 | cefr | I | L | Already in keyword draft |
| 17 | ingilizce a1 | I | M | |
| 18 | ingilizce b2 | I | M | |
| 19 | ingilizce c1 | I | L | |
| 20 | yds kelime | X | H | Trademark-adjacent (see §4) |
| 21 | yökdil kelime | X | M | |
| 22 | yds yökdil | X | H | |
| 23 | yks dil | X | H | |
| 24 | ydt kelime | X | M | |
| 25 | ielts kelime | X | M | |
| 26 | toefl kelime | X | M | |
| 27 | kelime defteri | I | M | Matches notebook feature |
| 28 | kelime tekrar | I | L | SRS/review |
| 29 | aralıklı tekrar | I | L | Spaced repetition, niche |
| 30 | flashcard ingilizce | I | M | |
| 31 | deyimler ingilizce | I | M | NAT level |
| 32 | phrasal verb | I | M | Used as English term in TR |
| 33 | günlük kelime | I | M | Daily Wheel |
| 34 | internetsiz oyun / çevrimdışı | G | M | Offline appeal, can be strong in TR (assumption) |
| 35 | arkadaşla oyun / düello | G | L | Same-device duel; v1.1 rooms later |
| 36 | pratik ingilizce | I | H | |
| 37 | ingilizce öğreten oyun | G+I | M | |
| 38 | kelime bulmaca | G | H | Competes with crosswords |

### English (EN storefronts: DE/NL/US diaspora and TR users with English UI)

| # | Keyword | Intent | Comp. | Note |
|---|---|---|---|---|
| 1 | learn english words | I | H | |
| 2 | english vocabulary | I | H | |
| 3 | vocabulary game | G+I | H | |
| 4 | word game | G | H | Very broad |
| 5 | alphabet quiz | G | L | Title phrase; low volume likely |
| 6 | a-z word game | G | L | |
| 7 | word wheel | G | L | |
| 8 | english vocabulary builder | I | H | |
| 9 | flashcards | I | H | |
| 10 | spaced repetition | I | M | |
| 11 | cefr vocabulary | I | L | |
| 12 | a1 english | I | M | |
| 13 | b2 english vocabulary | I | M | |
| 14 | c1 vocabulary | I | L | |
| 15 | english placement test | I | M | |
| 16 | english level test | I | M | |
| 17 | idioms | I | M | |
| 18 | phrasal verbs | I | M | |
| 19 | daily word | I | H | |
| 20 | daily puzzle | G | H | |
| 21 | turkish english | I | M | Diaspora signal |
| 22 | learn english turkish | I | L | |
| 23 | english for turkish speakers | I | L | |
| 24 | offline word game | G | M | |
| 25 | word quiz | G | H | |
| 26 | trivia | G | H | Low relevance |
| 27 | brain game | G | H | Low relevance |
| 28 | two player word game | G | M | Same-device duel |
| 29 | pass and play | G | L | |
| 30 | clue word game | G | L | |
| 31 | vocab quiz | G+I | M | |
| 32 | english exam vocabulary | X | M | Descriptive, no exam names |
| 33 | ielts vocabulary | X | H | Exam name — see §4 |
| 34 | toefl vocabulary | X | H | Exam name — see §4 |
| 35 | yds yokdil | X | L | Exact-match for diaspora/students; see §4 |
| 36 | english notebook | I | L | |
| 37 | wordle alternative | G | H | **Do not use**: third-party brand |

(#37 is shown only as a caution: competitor/brand terms are excluded.)

## 2. Category

| Store | Primary | Why | Alternative |
|---|---|---|---|
| Google Play | **Games → Word** (matches `owner-checklist.md` C.1) | The mechanic is a letter game; Word has far fewer apps than Education and the genre expectation fits | **Education** if the owner wants to compete with learning apps; Play lets you change category, test after day 30. Play now says Word is a game category (S: [Sensor Tower, 2014](https://sensortower.com/blog/2014/03/18/google-changes-play-app-categories) >12 months — re-check the current category list in Play Console) |
| App Store | Primary **Games → Word**, secondary **Education** | Apple allows two categories + game subgenres; secondary Education broadens browse reach. Apple category-name list was not retrievable here — confirm in ASC (E) | Primary Education (hurts if the game feel leads installs) |

Trade-off: Games→Word gets "game" browse traffic but monetises with ads (fits the model); Education gets learners but faces Duolingo-class competition. Recommend Word first, measure organic keyword mix at day 30, then test Education on Play.

## 3. Metadata variants (A/B candidates)

Lengths are character counts (script-verified).

### Google Play (store listing experiments: title, short, full, icon, screenshots can be tested; Play lets you A/B listings natively)

| | TR | len | EN | len |
|---|---|---|---|---|
| Title A (current draft) | Lexi: İngilizce Kelime Çarkı | 28 | Lexi: English Alphabet Quiz | 27 |
| Title B | Lexi: İngilizce Kelime Oyunu | 28 | Lexi: English Word Wheel | 24 |
| Title C | Lexi: A-Z İngilizce Kelime | 26 | Lexi: A-Z English Vocabulary | 28 |
| Short A (current) | A–Z çarkıyla İngilizce kelime öğren. Türkçe ipuçları, A1'den ana dil seviyesine. | 80 | Learn English words with a fun A–Z wheel. Turkish clues, A1 to native level. | 76 |
| Short B | Türkçe ipucu, İngilizce cevap: A–Z çarkıyla kelime ezberle. Çevrimdışı çalışır. | 79 | Turkish clue, English answer: build vocabulary with an A–Z word wheel. Offline. | 79 |
| Short C | Kelime ezberlemenin oyunlu yolu: A1–C1 seviyeler, günlük çark, tekrar defteri. | 78 | Word game for Turkish speakers: A1–C1 levels, daily wheel, review notebook. | 75 |

Note: "Native" in the short description is as in the current draft; "günlük çark, tekrar defteri" and "daily wheel" depend on those features shipping in v1.0 (listing notes already say VERIFY).

### App Store (name + subtitle indexed; promo text not indexed)

| | TR | len | EN | len |
|---|---|---|---|---|
| Subtitle A (current) | A'dan Z'ye kelime öğren | 23 | Learn words, A to Z | 19 |
| Subtitle B | Kelime ezberle, seviyeni geç | 28 | Vocabulary game, A1 to C1 | 25 |
| Subtitle C | A1-C1 kelime oyunu, çevrimdışı | 30 | Turkish clues, English words | 28 |
| Subtitle D (**exam-descriptive, only if owner approves §4**) | YDS YÖKDİL kelime hazırlığı | 27 | Vocabulary for YDS & YÖKDİL | 27 |

Because Apple indexes name + subtitle + keyword field without repeating words across them, avoid duplicating "ingilizce"/"kelime" in the keyword field when they are already in name/subtitle (this matches the VERIFY note in `listing-tr.md`; Apple's page also says to omit plurals, category names and duplicates).

### Keyword field drafts (≤100)

- TR (current, 96): `kelime,ingilizce,öğren,oyun,sözcük,bilgi yarışması,alfabe,harf,çevrimdışı,sınav,quiz,eğitim,cefr`
- TR v2 (97, drops words already in name/subtitle A): `ezberle,tekrar,defter,flashcard,bulmaca,çark,günlük,deyim,phrasal verb,seviye,hazırlık,yds,yökdil`
- EN v2 (97): `flashcards,cefr,wheel,puzzle,daily,notebook,idioms,phrasal verbs,placement test,spaced repetition`
- Exam names ("yds","yökdil") in the hidden keyword field are still trademark-adjacent; remove them if the owner picks the strict no-exam-names rule.

Suggested first test pairs: (1) Title A vs B (TR) — "Çarkı" vs "Oyunu"; (2) Short A vs B; (3) App Store subtitle A vs B in successive versions (Apple's product-page optimization tests creatives, **not** name/subtitle, so subtitle changes are sequential, not simultaneous).

## 4. Exam names

`docs/store/` deliberately omits LGS/YKS/YDS/YÖKDİL/IELTS/TOEFL pending the owner's decision. Options (E, not legal advice):
1. **None in metadata** (safest): describe as "sınav hazırlık paketleri" / "exam preparation packs" as the drafts do.
2. **Descriptive form only**: "YDS ve YÖKDİL için kelime hazırlığı" + disclaimer line in description "Lexi is not affiliated with ÖSYM, ETS, or the IELTS owners" (names and ownership: ÖSYM runs YDS/YÖKDİL; check who owns the others and use only what is factual). Nominative-fair-use is plausible for YDS/YÖKDİL as exam names, riskier for IELTS/TOEFL (owned by registered marks, Apple/Google can reject).
3. Keyword field only (not visible): lowest visibility to rivals, but still subject to Apple's rule against trademarks of others.
Recommend option 1 for v1.0 and option 2 for YDS/YÖKDİL after a lawyer's yes; competitors already use these names in TR listings (see sources above) which is evidence of market practice, not of permission.

## 5. Screenshot order

Apple shows the first 1–3 in search results; up to 10 allowed, 3 previews of ≤30s ([Apple](https://developer.apple.com/app-store/product-page/)). Recommended (E):
1. The wheel in play with a Turkish clue (game hook, big caption "Türkçe ipucu, İngilizce cevap")
2. Level picker A1→NAT ("6 seviye")
3. Daily Wheel + streak (retention)
4. Pass-and-return / jokers
5. End-of-game review + notebook (learning value)
6. Stats
7. Same-device duel
8. Pro/exam packs (only if shipped in v1.0; use neutral wording unless §4 approved)
9. Offline badge
Caption locale: separate TR and EN sets; TR is the default. App preview video (≤30s): 3 s wheel hook, 10 s gameplay, 5 s review.

## 6. Review-prompt timing

- iOS `SKStoreReviewController`: up to 3 prompts per 365 days (S: Apple forums/dev blogs, [thread](https://developer.apple.com/forums/thread/684074)); does not show in dev/TestFlight; the system may silently skip it. Android In-App Review: quota exists, exact value unpublished ([Play docs not fetched], S).
- Ask only after a **success moment**: a finished wheel with ≥ 70% correct (suggestion), on the 3rd completed game or day ≥ 2, never during a wheel, never right after a failed Hardcore run, never inside the first session (consistent with ad rule), never within a session that showed an interstitial or paywall.
- Pre-screen with an in-app "Enjoying Lexi?" thumb question? Not recommended on Apple (guideline against custom prompts that gate ratings is unclear to me, E) — simpler: call the system prompt directly, and route unhappy-feedback via Settings → contact.
- Cap: max once per app version and once per 90 days locally.

## 7. 30-day post-launch ASO checklist

Day 0–3
- [ ] Confirm indexing of name/subtitle/keywords in TR (search own app by 5 target terms).
- [ ] Set up Apple Search Ads keyword popularity (free) and Play Console search-term report.
- [ ] Verify screenshots/captions in TR and EN; check iPad/Play form factors.
- [ ] Confirm pricing/trial display in TR on both stores.

Day 4–10
- [ ] Record ranks for the top 15 TR and 10 EN keywords (daily, tool of choice).
- [ ] Reply to every review (TR) in <48 h; log themes.
- [ ] Check crash-free and rating stability before pushing review prompts harder.

Day 11–20
- [ ] Decide title test (Play): launch Title A vs B experiment; at least 7-day duration, wait for adequate sample.
- [ ] Update Apple keyword field using Search Ads popularity data; swap out zero-impression terms.
- [ ] Check "Promotional text" for seasonal hooks (YDS/YÖKDİL dates, exam season) — editable without a new version.

Day 21–30
- [ ] Review conversion rate (impression → install) by country and source; compare TR vs diaspora.
- [ ] Reconsider category (Word vs Education) on Play using organic keyword mix.
- [ ] Evaluate if diaspora needs localized DE/NL stores: add German/Dutch descriptions only if installs justify.
- [ ] Revisit price per `PRICING.md` §3 review cadence.
- [ ] Record outcomes in this folder (new `ASO-LOG.md`).
