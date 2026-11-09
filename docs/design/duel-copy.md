# Duel (hot-seat) copy — screens 17–20 and 02b

Status: **owner-approved copy, wired (M8.1)**; the name dialog still needs a design (see screen-map.md). Keys follow `feature_screen_element`; `{name}`, `{n}` are placeholders. Names are free
text, so TR never inflects them (no `'a/'ya` suffixes). Existing keys to reuse are marked **reuse**.
Length class: EN ≤ TR + 10 %. **DECISION** = needs the owner.

| Key | Turkish | English | Where | Max | Note |
|---|---|---|---|---|---|
| `home_tile_duel` | İki kişi, tek telefon | Two players, one phone | 02b title | 24 | |
| `home_tile_duel_subtitle` | Arkadaşınla sırayla yarış | Take turns against a friend | 02b line 2 | 28 | |
| `duel_setup_eyebrow` | İKİ KİŞİ · TEK TELEFON | TWO PLAYERS · ONE PHONE | 17 header | 26 | uppercase in resource |
| `duel_setup_title` | Sırayla yarışın | Take turns | 17 headline (2 lines) | 16 | |
| `duel_setup_subtitle` | Herkesin kendi çarkı ve kendi süresi var. | Everyone gets their own wheel and clock. | 17 | 44 | |
| `duel_player_label` | {n}. OYUNCU | PLAYER {n} | 17 card label | 12 | |
| `duel_player_default` | {n}. Oyuncu | Player {n} | 17 default name, 18–20 | 10 | used when the name field is left empty; saved names replace it |
| `duel_player_change` | Değiştir | Change | 17 card action | 8 | |
| `duel_level_label` | SEVİYE | LEVEL | 17 | 8 | |
| `duel_time_label` | HER OYUNCUNUN SÜRESİ | TIME PER PLAYER | 17 | 22 | |
| `duel_clock_option_minutes` | {n} dk | {n} min | 17 clock segments (3 · 5 · 7) | 6 | 1.0.11; segment design is **needs design** (built from `16-components` SegmentedControl) |
| `duel_rules_note` | Doğru bildikçe sıra sende kalır. Yanlış ya da pas sırayı rakibe geçirir. Bu modda joker yok. | Keep answering right and the turn stays yours. A wrong answer or a pass hands it over. No jokers in this mode. | 17 | 100 | 3 lines at 14 sp |
| `duel_start` | BAŞLA | START | 17 CTA | 8 | reuse the level-select BAŞLA/START if same |
| `duel_handover_eyebrow` | DÜELLO | DUEL | 18 top | 10 | reuse `result_mode_duel` |
| `duel_handover_turn` | SIRA | UP NEXT | 18 label over name | 10 | |
| `duel_handover_body` | Telefonu sıradaki oyuncuya ver. Hazır olunca dokun, süre o zaman başlar. | Pass the phone to the next player. Tap when ready: the clock starts then. | 18 | 80 | |
| `duel_handover_time` | KALAN SÜRE | TIME LEFT | 18 stat box | 12 | |
| `duel_handover_letters` | KALAN HARF | LETTERS LEFT | 18 stat box | 12 | |
| `duel_handover_previous` | Önceki oyuncu: {name} · {c} doğru · {w} yanlış | Last player: {name} · {c} correct · {w} wrong | 18 | 44 | plurals not needed (unit follows number) |
| `duel_handover_ready` | HAZIRIM | I'M READY | 18 CTA | 10 | |
| `game_pause_quit` | PES ET | GIVE UP | 18 link | 8 | **reuse**; asks "Pes etmek istiyor musun?" first |
| `duel_game_turn` | SIRA | TURN | 19 active card chip | 6 | |
| `result_mode_duel` | DÜELLO | DUEL | 20 header | 8 | **reuse**; header = `result_header` with `result_reason_*` |
| `duel_result_winner` | {name} kazandı | {name} wins | 20 headline | 18+name | name capped at 12 chars, ellipsis |
| `duel_result_draw` | Berabere | It's a draw | 20 headline | 12 | |
| `duel_result_score` | {a} – {b} doğru | {a} – {b} correct | 20 line 2, always shown | 16 | |
| `duel_result_tiebreak_wrong` | Doğrular eşit: yanlışı az olan kazandı | Tied on correct: fewer wrong answers wins | 20 line 3, under the score (14 sp) | 40 | rules §8 |
| `duel_result_tiebreak_time` | Doğru ve yanlış eşit: kalan süresi çok olan kazandı | Tied on correct and wrong: more time left wins | 20 line 3, under the score | 52 | rules §8 |
| `duel_result_forfeit` | {name} pes etti | {name} gave up | 20 line 3, under the score | 22+name | winner = the other player; header reason `PES ETTİ` / `GAVE UP` |
| `result_reason_forfeit` | PES ETTİ | GAVE UP | 20 header | 10 | **reuse, TR changes** from BIRAKILDI (EN already GAVE UP); also affects the other modes' result headers |
| `duel_name_title` | Oyuncu adı | Player name | name dialog title | 12 | **needs design** |
| `duel_name_hint` | Adın | Name | name field hint | 8 | max 12 characters; empty → default name |
| `duel_name_save` | Kaydet | Save | name dialog | 8 | |
| `duel_name_cancel` | Vazgeç | Cancel | name dialog | 8 | |
| `duel_result_time_up` | SÜRE BİTTİ | TIME UP | 20 header reason / player card | 12 | **reuse** `result_reason_time_up` |
| `duel_result_winner_badge` | KAZANAN | WINNER | 20 card badge | 10 | |
| `result_correct` / `result_wrong` / `result_passed` / `result_time_left` | doğru / yanlış / pas / kalan süre | correct / wrong / passed / time left | 20 stats | — | **reuse** (EN "pass" vs "passed" — check `values/strings.xml`) |
| `result_play_again` | TEKRAR OYNA | PLAY AGAIN | 20 CTA | 12 | **reuse** |
| `result_home` | Ana menüye dön | Back to menu | 20 link | 16 | **reuse** |

## Changed in this pass (old → new)
- `OYUNCU 1` (label) → `1. OYUNCU` (Turkish ordinal; avoids "OYUNCU 1 / Oyuncu 1" echo)
- "…Jokerler bu modda yok. Adlar bir sonraki maç için saklanır." → "…Bu modda joker yok." (saving names is behaviour, not copy)
- `ÇARK 2 · SIRA DEĞİŞTİ` → `DÜELLO` (no "wheel 2" concept; "SIRA" is already the label below)
- `TELEFONU VER` → `SIRA` (label over the name)
- "Hazır olunca dokun. Süre o zaman başlar." → "Telefonu sıradaki oyuncuya ver. Hazır olunca dokun, süre o zaman başlar." (instruction moved here; no name suffix)
- stat `SÜRE` → `KALAN SÜRE`
- `A1 · İKİ KİŞİ` → `A1 · DÜELLO · ÇARK TAMAM` (matches `result_header` pattern)
- `19 – 17 · eşitlik yok` → `19 – 17 doğru`
- `ANA MENÜ` button → `Ana menüye dön` link (matches `06-result`)
- Unchanged and correct: Değiştir, SEVİYE, HER OYUNCUNUN SÜRESİ, BAŞLA, HAZIRIM, PES ET, KAZANAN, TEKRAR OYNA, home tile texts, İ/ı casing.

## Decisions (settled)
1. Name dialog copy approved; no design yet (`needs design` in screen-map.md). Empty name → "1. Oyuncu" / "2. Oyuncu".
2. Tie-break explanation is a separate line under the winner headline; the score line is always visible.
3. Forfeit header reason: `PES ETTİ` / `GAVE UP` (TR resource `result_reason_forfeit` changes from BIRAKILDI when wired).
4. EN title "Take turns"; Home box "Two players, one phone".
5. Clock per player (1.0.11, owner): 3 / 5 (default) / 7 minutes, labels "3 dk · 5 dk · 7 dk" / "3 min · 5 min · 7 min"
   (was 1:30 / 2:30 / 4:00). The result and handover screens show the remaining time as m:ss and need no change.
6. EN apostrophes are plain `'` in the resource; never `\'` (Compose resources show the backslash;
   `checkComposeStringEscapes` fails the build).
