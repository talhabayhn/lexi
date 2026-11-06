# Fill the blank ("Boşluk doldurma") copy — screens 21–24 (PROPOSED)

Status: **proposed, not approved.** Keys follow `feature_screen_element`; `{n}`, `{c}`, `{w}`, `{level}` are placeholders.
Existing keys to reuse are marked **reuse**. Length class: EN ≤ TR + 10 %. Max = characters that fit the design's width at the
given size (labels are uppercase in the resource, never via `uppercase()`).

| Key | Turkish | English | Where | Max | Note |
|---|---|---|---|---|---|
| `home_tile_duel_short` | Düello | Duel | 21 half tile title | 12 | new short title; the long `home_tile_duel` stays for a full-width tile |
| `home_tile_duel_short_subtitle` | İki kişi, tek telefon | Two players, one phone | 21 half tile line 2 | 22 | 13 sp, one line in 142 dp |
| `home_tile_fillblank` | Boşluk doldurma | Fill the blank | 21 half tile title | 16 | 16 sp bold, one line |
| `home_tile_fillblank_subtitle` | Cümleyi tamamla | Complete the sentence | 21 half tile line 2 | 22 | |
| `levels_mode_fillblank` | Boşluk | Blank | 22 4th segment | 8 | 16 sp bold in 86 dp |
| `levels_mode_fillblank_sub` | 10 cümle | 10 sentences | 22 segment line 2 | 10 | 12 sp; number from `fillBlankRoundSize` |
| `levels_start_chip_fillblank` | {level} · BOŞLUK · {n} CÜMLE | {level} · BLANK · {n} SENTENCES | 22 BAŞLA chip | 26 | 14 sp; EN is 2 chars longer — check at 360 dp, else "{n} SENT." |
| `fillblank_card_label` | BOŞLUĞU DOLDUR · {pos} | FILL THE BLANK · {pos} | 23 sentence card label | 28 | {pos} = the existing POS word (İSİM / NOUN …), as the clue label |
| `fillblank_blank_a11y` | boşluk, {n} harf | blank, {n} letters | 23 screen reader text of the blank | 20 | not visible |
| `fillblank_translation_label` | TÜRKÇESİ | IN TURKISH | 23 translation box | 12 | EN is longer (11 vs 8); still one line |
| `fillblank_strip_a11y` | Cümle {n} / {total} | Sentence {n} of {total} | 23 progress strip | 20 | not visible |
| `fillblank_length_badge` | {n} HARF | {n} LETTERS | 23 card panel | 9 | **reuse** the game's "7 HARF" key |
| `fillblank_joker_example_off` | bu modda anlamsız | pointless in this mode | joker sheet reason under the Example row | 22 | 13 sp reason line (`caption13`) |
| `result_mode_fillblank` | BOŞLUK | BLANK | 24 header ({level} · MODE · reason) | 8 | like `result_mode_duel` |
| `result_reason_complete_fillblank` | TAMAM | DONE | 24 header | 8 | **reuse** `result_reason_*` (ÇARK TAMAM is wheel-specific) |
| `result_review_title` | Tekrar edilecekler | To review | 24 section | 20 | **reuse** |
| `result_review_added` | {n} cümle defterine eklendi | {n} sentences added to your notebook | 24 section right | 32 | 13 sp, may wrap on 320 dp (same as wheel's "kelime") |
| `result_review_more_correct` | +{n} doğru cümle · Tümünü gör → | +{n} correct sentences · Show all → | 24 more row | 34 | 14 sp bold |
| `result_review_less` | Daha az göster ↑ | Show less ↑ | 24 more row (expanded) | 18 | **reuse** |
| `result_play_again` | TEKRAR OYNA | PLAY AGAIN | 24 CTA | 12 | **reuse** |
| `result_back_home` | Ana menüye dön | Back to home | 24 link | 18 | **reuse** |
| `result_word_listen_a11y` | {word} telaffuzunu dinle | Listen to {word} | 24 row speaker | — | **reuse** |
| `settings_fillblank_*` | — | — | — | — | none: no settings in v1 |
| `leveltest_*`/`pause_*` | — | — | — | — | pause dialog, "Pes et", errors: **reuse** unchanged |
| `fillblank_empty` | Bu seviyede henüz yeterli cümle yok. | Not enough sentences at this level yet. | `EmptyState` when < 10 usable cards / < 10 letters | 44 | secondary action "Geri dön" (**reuse**) |

Rules: TR never inflects the level or numbers ("10 cümle" = number + unit, no suffix). The strings in screens 21–24 above
(sample sentence "A tiny bird is singing nearby." / "Yakınlarda minik bir kuş ötüyor.", the result rows) are real A1 cards
(`a1-bird`, `a1-bridge`, `a1-cheese`, `a1-ticket`), not copy.
