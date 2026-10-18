# Lexi — copy deck (TR primary, EN secondary)

> DRAFT for owner review. Nothing here is wired into the app; strings.xml is untouched. Keys follow the existing
> resource naming (`feature_screen_element`); rename freely when wiring. Budget = max characters, **both languages**
> (verified by script; Turkish runs ~15 % longer, so write it first). `{n}` / `{name}` are placeholders.
> Exam names appear only where marked **(EXAM)** — see `DECISIONS.md` → Open decisions before shipping them in store text.

**Tone:** warm, short, plain. We invite, we never scold: no "you'll lose", no "we miss you", no guilt, no shouting, no
emoji spam (max one, only in notifications, optional). Address the player as *sen* (informal, matches the app).
Numbers and facts over adjectives. Bauhaus-clean = few words, one idea per line.

## 1. Reminder notifications

Rules: **max 1 per day**, only when the user enabled the reminder (off until the pre-prompt), never inside **quiet
hours** (default 22:00–08:00 local, user-configurable; a reminder due inside quiet hours moves to the end of them),
never twice for the same reason in a row, stop the comeback series after the 14-day message (no more after that
until the user returns). Title = app name by default on iOS; the title below is the Android/iOS title.

| Key | Trigger | TR title | TR body | EN title | EN body | Budget (title/body) |
|---|---|---|---|---|---|---|
| `notif_daily_1_title/_body` | Daily wheel not played, usual hour | Günün Çarkı hazır | Bugünün 26 harfi seni bekliyor. 3 dakikan var mı? | Today's wheel is ready | 26 letters for today. Got 3 minutes? | 30 / 70 |
| `notif_daily_2_title/_body` | Daily wheel not played (alternate) | Bugünkü çark | Herkesle aynı çark, senin hızın. Hadi bir bak. | Today's wheel | Same wheel for everyone, at your pace. | 30 / 70 |
| `notif_daily_3_title/_body` | Daily wheel not played (alternate) | A'dan Z'ye, bir tur | Günlük çarkın hazır; tek deneme hakkın var. | A to Z, one lap | Your daily wheel is ready — one try today. | 30 / 70 |
| `notif_streak_1_title/_body` | Streak ≥ 2, today not played, evening | {n} günlük serin sürüyor | Bugünkü çarkı oynarsan seri devam eder. | Your {n}-day streak is on | Play today's wheel to keep it going. | 40 / 70 |
| `notif_streak_2_title/_body` | Streak ≥ 2, evening, last reminder window | Seriyi korumak için bir tur | Gece yarısına kadar bir çark yeter. | One lap keeps your streak | Any wheel before midnight counts. | 40 / 70 |
| `notif_streak_3_title/_body` | Streak is a multiple of 7 minus 1 | Yarın {n}. gün | Bir çark daha, bonus jokerin yolda. | Day {n} is tomorrow | One more wheel and a bonus joker is yours. | 40 / 70 |
| `notif_review_1_title/_body` | ≥ 5 notebook cards due | {n} kelime tekrar bekliyor | Defterindeki kelimeler tazelenmeye hazır. | {n} words ready to review | Your notebook words are due a refresher. | 40 / 70 |
| `notif_review_2_title/_body` | ≥ 10 cards due | Tekrar Çarkı hazır | Unutmadan pekiştir: {n} kelime sırada. | Review wheel is ready | Lock them in before they fade: {n} words. | 40 / 70 |
| `notif_comeback_3d_title/_body` | 3 days without play | Küçük bir mola mı? | Hazır olduğunda 5 dakikalık bir çark seni bekliyor. | Taking a short break? | A 5-minute wheel is here when you are. | 40 / 70 |
| `notif_comeback_7d_title/_body` | 7 days without play | Çark yerinde duruyor | Yeni bir Günün Çarkı var. Kaldığın yerden devam et. | The wheel is still here | There's a fresh daily wheel. Pick up where you left off. | 40 / 70 |
| `notif_comeback_14d_title/_body` | 14 days without play (last one) | Tekrar görüşmek güzel olur | Kelimelerin defterinde saklı; istediğinde devam. | Come back whenever | Your words are safe in your notebook. | 40 / 70 |
| `notif_goal_title/_body` | Daily goal 1 step left (optional) | Hedefe az kaldı | Bugünkü hedefin için {n} doğru cevap yeter. | Almost at your goal | {n} more correct answers hit today's goal. | 40 / 70 |

Pre-prompt (before the OS permission dialog, onboarding layout — design debt):

| Key | TR | EN | Budget |
|---|---|---|---|
| `reminder_preprompt_title` | Hatırlatıcı ister misin? | Want a reminder? | 30 |
| `reminder_preprompt_body` | Günde en fazla bir bildirim göndeririz. İstediğin saati sen seçersin. | At most one a day, at the time you choose. | 80 |
| `reminder_preprompt_yes` | Evet, hatırlat | Yes, remind me | 18 |
| `reminder_preprompt_no` | Şimdi değil | Not now | 18 |
| `reminder_denied_hint` | Bildirimler kapalı. Ayarlar'dan açabilirsin. | Notifications are off. You can turn them on in Settings. | 70 |
| `reminder_quiet_hours_label` | Sessiz saatler | Quiet hours | 24 |
| `reminder_quiet_hours_hint` | Bu saatlerde bildirim göndermeyiz. | We won't send notifications during these hours. | 60 |

## 2. Paywall (Lexi Pro) — prices are placeholders, never hardcoded

| Key | TR | EN | Budget |
|---|---|---|---|
| `paywall_title` | Lexi Pro | Lexi Pro | 12 |
| `paywall_headline` | Reklamsız, sınırsız öğren | Learn without limits or ads | 36 |
| `paywall_benefit_1` | Reklam yok, kesintisiz oyun | No ads, uninterrupted play | 40 |
| `paywall_benefit_2` | Sınav paketleri (YDS/YÖKDİL ve fazlası) **(EXAM)** | Exam packs (YDS/YÖKDİL and more) **(EXAM)** | 48 |
| `paywall_benefit_3` | Her gün +3 joker, sınırsız Tekrar Çarkı | +3 jokers daily, unlimited Review Wheels | 48 |
| `paywall_plan_monthly` | Aylık · {price} | Monthly · {price} | 24 |
| `paywall_plan_yearly` | Yıllık · {price} | Yearly · {price} | 24 |
| `paywall_plan_yearly_note` | Aylık {price} · %{n} tasarruf | {price}/month · save {n}% | 32 |
| `paywall_plan_lifetime` | Ömür boyu · {price} | Lifetime · {price} | 24 |
| `paywall_plan_lifetime_note` | Tek seferlik ödeme | One-time payment | 24 |
| `paywall_best_value` | EN AVANTAJLI | BEST VALUE | 14 |
| `paywall_cta` | Pro'ya geç | Go Pro | 16 |
| `paywall_cta_trial` | {n} gün ücretsiz dene | Try free for {n} days | 28 |
| `paywall_trial_note` | {n} gün ücretsiz, sonra {price}. İstediğin zaman iptal et. | Free for {n} days, then {price}. Cancel anytime. | 64 |
| `paywall_restore` | Satın alımları geri yükle | Restore purchases | 28 |
| `paywall_restore_ok` | Satın alımların geri yüklendi. | Your purchases were restored. | 40 |
| `paywall_restore_none` | Geri yüklenecek satın alım bulunamadı. | No purchases found to restore. | 48 |
| `paywall_legal` | Abonelikler, dönem bitiminden en az 24 saat önce mağaza hesabından iptal edilmedikçe otomatik yenilenir. Ödeme, satın alma onayında hesabından tahsil edilir. Ömür boyu seçeneği tek seferliktir. | Subscriptions renew automatically unless cancelled in your store account at least 24 hours before the period ends. Payment is charged to your account at purchase confirmation. Lifetime is a one-time purchase. | 240 |
| `paywall_terms_link` | Kullanım Koşulları | Terms of Use | 20 |
| `paywall_privacy_link` | Gizlilik Politikası | Privacy Policy | 20 |
| `paywall_close` | Kapat | Close | 10 |
| `paywall_error` | Satın alma tamamlanamadı. Tekrar dene. | The purchase didn't go through. Please try again. | 56 |
| `paywall_offline` | Fiyatlar yüklenemedi. Bağlantını kontrol et. | Couldn't load prices. Check your connection. | 56 |
| `paywall_success` | Pro aktif. Hoş geldin! | Pro is active. Welcome! | 30 |
| `pro_locked_pack` | Bu paket Lexi Pro ile açılır. | This pack comes with Lexi Pro. | 40 |
| `pro_preview_note` | A–E harflerini ücretsiz deneyebilirsin. | Try letters A–E for free. | 44 |

## 3. Onboarding (3 steps)

| Key | TR | EN | Budget |
|---|---|---|---|
| `onboarding_step_counter` | {n}/3 | {n}/3 | 5 |
| `onboarding_1_title` | Türkçe ipucu, İngilizce kelime | A Turkish clue, an English word | 34 |
| `onboarding_1_body` | İpucunu oku, harfle başlayan (ya da harfi içeren) kelimeyi bul. | Read the clue and find the word that starts with — or contains — the letter. | 80 |
| `onboarding_2_title` | A'dan Z'ye bir çark | A wheel from A to Z | 30 |
| `onboarding_2_body` | Emin değilsen pas geç; çark dönünce geri gelirsin. | Not sure? Pass — the wheel brings you back around. | 70 |
| `onboarding_3_title` | Her gün biraz | A little every day | 30 |
| `onboarding_3_body` | Günün Çarkı, seri ve kelime defterinle ilerlemeni görürsün. | Track progress with the daily wheel, your streak and your notebook. | 76 |
| `onboarding_next` | Devam | Next | 14 |
| `onboarding_skip` | Atla | Skip | 10 |
| `onboarding_start` | Başlayalım | Let's start | 16 |

## 4. Level test

| Key | TR | EN | Budget |
|---|---|---|---|
| `leveltest_choice_title` | Seviyeni bulalım | Let's find your level | 30 |
| `leveltest_choice_body` | 2 dakikalık kısa bir test ya da seviyeni kendin seç. | A quick 2-minute test, or pick your level yourself. | 64 |
| `onboarding_level_pick_body` | Seviyeni seç, istediğin zaman değiştirebilirsin. | Pick your level. You can change it any time. *(Used until the level test ships)* | 64 |
| `leveltest_choice_test` | Testi başlat | Start the test | 18 |
| `leveltest_choice_pick` | Kendim seçeceğim | I'll pick myself | 22 |
| `leveltest_intro_title` | Seviye testi | Level test | 20 |
| `leveltest_intro_body` | En fazla 14 soru. Joker yok, doğru ya da yanlış puanı etkilemez. Kolay başlar, cevapladıkça zorlaşır. | Up to 14 questions. No jokers, and nothing counts toward your score. It starts easy and adapts as you go. | 110 |
| `leveltest_intro_hint` | Bilmediğini pas geçebilirsin. | You can pass on anything you don't know. | 44 |
| `leveltest_intro_cta` | Başla | Start | 12 |
| `leveltest_result_label` | Seviyen | Your level | 16 |
| `leveltest_result_level` | Seviyen: {level} | Your level: {level} | 24 |
| `leveltest_result_body` | Bu seviyeden başlamanı öneriyoruz. İstediğin zaman değiştirebilirsin. | We suggest starting here. You can change it any time. | 76 |
| `leveltest_result_cta` | {level} ile başla | Start at {level} | 24 |
| `leveltest_result_change` | Seviyeyi değiştir | Change level | 22 |
| `leveltest_result_a1` | Temelden başlıyoruz; güzel bir yol seni bekliyor. | We start from the basics — a good road ahead. | 60 |
| `leveltest_result_high` | Güçlü bir başlangıç! Zorlu kelimeler seni bekliyor. | A strong start! Tougher words await. | 60 |
| `leveltest_error` | Test başlatılamadı. Seviyeni elle seçebilirsin. | The test couldn't start. You can pick a level manually. | 64 |

## 5. Empty and error states (use with `EmptyState`)

| Key | TR | EN | Budget |
|---|---|---|---|
| `error_generic` | Bir şeyler ters gitti. | Something went wrong. | 30 |
| `error_retry` | Tekrar dene | Try again | 16 |
| `error_back` | Geri dön | Go back | 14 |
| `error_content_unreadable` | Kelimeler okunamadı. Tekrar dene. | Couldn't read the words. Try again. | 44 |
| `error_offline_feature` | Bu özellik için internet gerekir. | This feature needs an internet connection. | 48 |
| `empty_notebook_title` | Defterin boş | Your notebook is empty | 24 |
| `empty_notebook_body` | Kaçırdığın kelimeler burada toplanır. | Words you miss are collected here. | 44 |
| `empty_notebook_search` | Eşleşen kelime yok. | No matching words. | 30 |
| `empty_review_none` | Şu an tekrar edilecek kelime yok. | Nothing to review right now. | 44 |
| `empty_stats` | Henüz istatistik yok. Bir çark oyna. | No stats yet. Play a wheel to begin. | 50 |
| `empty_stats_letters` | En zor harfler için 5 cevap gerekir. | Hardest letters need 5 answers first. | 50 |
| `empty_no_words` | Bu seçimde kelime yok. | No words for this choice. | 36 |
| `empty_result_no_misses` | Hiç kelime kaçırmadın. Harika! | You didn't miss a single word. Great! | 44 |
| `empty_category_none` | Bu seviyede kategori yok. | This level has no categories. | 40 |
| `daily_already_played` | Bugünkü çarkı oynadın. Yarın yenisi geliyor. | You've played today's wheel. A new one arrives tomorrow. | 64 |
| `daily_next_in` | Yeni çark: {time} | Next wheel in {time} | 28 |
| `coming_soon` | YAKINDA | SOON | 10 |

## 6. Rewarded-ad prompts (opt-in only; never interrupt a wheel)

| Key | TR | EN | Budget |
|---|---|---|---|
| `ad_joker_button` | Kısa reklam izle · +1 joker | Watch a short ad · +1 joker | 34 |
| `ad_joker_title` | Joker kazan | Earn a joker | 20 |
| `ad_joker_body` | Kısa bir reklam izle, 1 joker senin. | Watch a short ad and get 1 joker. | 48 |
| `ad_joker_confirm` | Reklamı izle | Watch ad | 16 |
| `ad_joker_cancel` | Vazgeç | Not now | 14 |
| `ad_reward_granted` | +1 joker eklendi. | +1 joker added. | 28 |
| `ad_not_ready` | Reklam şu an hazır değil. Biraz sonra dene. | No ad available right now. Try again soon. | 56 |
| `ad_failed` | Reklam yüklenemedi. Joker eklenmedi. | The ad failed to load. No joker was added. | 50 |
| `ad_cap_reached` | Bu joker için üst sınıra ulaştın (5). | You're at the cap for this joker (5). | 48 |
| `ad_pro_hint` | Reklam istemiyorsan Lexi Pro'ya göz at. | Prefer no ads? Take a look at Lexi Pro. | 52 |
| `consent_title` | Reklam tercihlerin | Your ad choices | 24 |
| `consent_body` | Ödüllü reklamlar için tercihini sorarız. Ayarlar'dan değiştirebilirsin. | We ask your preference for rewarded ads. You can change it in Settings. | 90 |

## 7. Share-card captions (432×540 card; caption sits under the score)

| Key | TR | EN | Budget |
|---|---|---|---|
| `share_caption_time` | A'dan Z'ye {time}'de bitirdim. Yenebilir misin? | A to Z in {time}. Can you beat it? | 52 |
| `share_caption_daily` | Günün Çarkı: {correct}/26 doğru. Sen kaç yaparsın? | Today's wheel: {correct}/26. What's your score? | 56 |
| `share_caption_streak` | {n} gündür her gün bir çark. Sen de dene. | A wheel a day for {n} days. Join in. | 52 |
| `share_message` | Lexi'de oynuyorum: {link} | Playing on Lexi: {link} | 60 |
| `share_cta` | Paylaş | Share | 14 |
| `share_error` | Paylaşım başlatılamadı. | Couldn't open sharing. | 34 |

## 8. Rating prompt (`ConfirmDialog`, design debt)

Ask only after a **good moment**: ≥ 3 completed wheels, a result with ≥ 18 correct or a 3+ day streak, never in the
first session, never right after a wrong answer, never twice within 120 days. "Not now" = ask again no sooner than
30 days; after 3 refusals stop. The native review dialog follows the "Yes" button; there is no star pre-filter that
hides low ratings from the store.

| Key | TR | EN | Budget |
|---|---|---|---|
| `rate_title` | Lexi hoşuna gidiyor mu? | Enjoying Lexi? | 30 |
| `rate_body` | Bir yorum bırakırsan başkalarının da bizi bulmasına yardım edersin. | A quick review helps other learners find us. | 76 |
| `rate_yes` | Puan ver | Rate Lexi | 16 |
| `rate_later` | Şimdi değil | Not now | 16 |
| `rate_feedback` | Önerim var | Send feedback | 20 |
| `rate_feedback_body` | Görüşlerini e-postayla iletebilirsin. | You can send us your thoughts by email. | 56 |

## 9. Streak and daily vocabulary (reference)

| Key | TR | EN | Budget |
|---|---|---|---|
| `home_streak_badge` | {n} gün seri | {n}-day streak | 14 |
| `game_combo_badge` | x{n} SERİ | x{n} COMBO | 10 |

Home badge hidden at 0; distinct from the in-game combo (see `DECISIONS.md`).
