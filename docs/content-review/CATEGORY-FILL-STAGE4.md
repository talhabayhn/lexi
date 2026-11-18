# Category rebalance, stage 4: new cards for the hidden pairs

**Onay formatı:** `hepsi onay, hariç: <id> → drop` ya da `<id> → <daha iyi cevap>` (örn. `b2-purchase → b2-invoice`).

Docs only: `content/` değişmedi. 42 yeni kart, 4 çift; hepsi `STARTS`, American spelling, `tags` boş. Satırlar `CATEGORY-FILL-STAGE4.csv` içinde, uygulanırken B2 satırları `content/cards/B2/`, C1 satırları `content/cards/C1/` altına yeni dosya olarak eklenir. ★ = çiftin henüz sahip olmadığı harf.

## 1. Sonuç (kart / harf)

| çift | önce | eklenen | sonra | harf sonra | yeni harfler | eşik 30/13 |
|---|---|---|---|---|---|---|
| hobbies B2 | 26 / 17 | +6 | 32 | 22 | DELRV (5) | +2 kart, +9 harf |
| hobbies C1 | 23 / 14 | +9 | 32 | 23 | EHIKLOQUV (9) | +2 kart, +10 harf |
| economy B2 | 21 / 18 | +11 | 32 | 22 | CEPS (4) | +2 kart, +9 harf |
| society-law B2 | 16 / 11 | +16 | 32 | 22 | BEGHILMPSUW (11) | +2 kart, +9 harf |

İnce harflere katkı (EXPANSION_PLAN §3): B2'de U (underdog, unlawful), V (villain), L (league, liberty); C1'de Q, K, V, U, L (quintet, knockout, virtuoso, upbeat, lineup).

## 2. Kartlar

### Hobiler ve eğlence (spor, müzik, film), B2 (6)

| # | cevap | harf | tür | ipucu (TR) | örnek (EN) | zor. |
|---|---|---|---|---|---|---|
| 1 | **REFEREE** | R★ | noun | maçı yöneten ve kuralları uygulatan kişi | The referee blew his whistle and stopped the match. | 3 |
| 2 | **ENCORE** | E★ | noun | konserin sonunda seyircinin istediği ek parça | The band played one more song as an encore. | 4 |
| 3 | **VILLAIN** | V★ | noun | filmde ya da hikâyede kötü karakter | The villain in the film is both clever and cruel. | 3 |
| 4 | **LEAGUE** | L★ | noun | takımların sezon boyunca yarıştığı organizasyon | Our team finished second in the league this year. | 3 |
| 5 | **UNDERDOG** | U | noun | kazanma şansı düşük görülen takım ya da kişi | Nobody expected the underdog to win the final. | 4 |
| 6 | **DRIBBLE** | D★ | verb | topu yerde sürerek rakibin yanından geçmek | He can dribble past three defenders with ease. | 4 |

### Hobiler ve eğlence (spor, müzik, film), C1 (9)

| # | cevap | harf | tür | ipucu (TR) | örnek (EN) | zor. |
|---|---|---|---|---|---|---|
| 7 | **QUINTET** | Q★ | noun | beş çalgıcı ya da şarkıcıdan oluşan topluluk | A jazz quintet played in the hotel lobby every night. | 4 |
| 8 | **KNOCKOUT** | K★ | noun | boksta rakibi yere serip kazanılan galibiyet | He won the fight by a knockout in the third round. | 4 |
| 9 | **VIRTUOSO** | V★ | noun | çalgısını olağanüstü ustalıkla çalan sanatçı | She became a famous violin virtuoso at the age of twenty. | 5 |
| 10 | **ENSEMBLE** | E★ | noun | birlikte çalan ya da oynayan küçük topluluk | The string ensemble performed at the opening ceremony. | 4 |
| 11 | **IMPROVISE** | I★ | verb | önceden hazırlanmadan anında çalmak ya da oynamak | The pianist began to improvise a new melody. | 4 |
| 12 | **LINEUP** | L★ | noun | sahneye çıkacak sanatçıların ya da oyuncuların listesi | This year's festival lineup includes several famous bands. | 4 |
| 13 | **UPBEAT** | U★ | adj | neşeli, enerjik ve olumlu havada olan | The band played an upbeat song to cheer up the crowd. | 4 |
| 14 | **HOOLIGAN** | H★ | noun | spor maçlarında şiddet çıkaran taraftar | The hooligan was banned from every stadium for five years. | 4 |
| 15 | **OFFSIDE** | O★ | adj | futbolda kural dışı ileri konumda olan | The goal was disallowed because he was offside. | 4 |

### Para ve ekonomi, B2 (11)

| # | cevap | harf | tür | ipucu (TR) | örnek (EN) | zor. |
|---|---|---|---|---|---|---|
| 16 | **SHAREHOLDER** | S★ | noun | bir şirketin hisselerine sahip olan kişi | Each shareholder received a letter about the merger. | 4 |
| 17 | **MERCHANT** | M | noun | mal alıp satarak ticaret yapan kişi | The merchant sold silk and spices at the market. | 3 |
| 18 | **BROKER** | B | noun | alıcı ile satıcı arasında aracılık eden kişi | A broker helped them buy shares at a good price. | 4 |
| 19 | **CREDITOR** | C★ | noun | borç veren ve parasını geri isteyen kişi ya da kurum | The creditor demanded payment within thirty days. | 4 |
| 20 | **EARNINGS** | E★ | noun | çalışarak kazanılan toplam gelir | His earnings rose sharply after he changed jobs. | 4 |
| 21 | **DIVIDEND** | D | noun | şirketin ortaklara dağıttığı kâr payı | The company paid a higher dividend this year. | 4 |
| 22 | **AUCTION** | A | noun | malların en yüksek teklife satıldığı açık satış | The painting was sold at auction for a record price. | 3 |
| 23 | **OVERDRAFT** | O | noun | hesapta olmayan parayı bankadan çekme durumu | He went into overdraft after paying the rent. | 4 |
| 24 | **PURCHASE** | P★ | noun | para karşılığı bir şey alma işlemi | Keep the receipt as proof of purchase. | 3 |
| 25 | **DOWNTURN** | D | noun | ekonomik faaliyetlerin gerilediği dönem | The economic downturn forced many shops to close. | 4 |
| 26 | **PAYROLL** | P★ | noun | şirketin çalışanlara ödediği maaşların listesi | The company has more than fifty people on its payroll. | 4 |

### Toplum ve hukuk, B2 (16)

| # | cevap | harf | tür | ipucu (TR) | örnek (EN) | zor. |
|---|---|---|---|---|---|---|
| 27 | **BURGLARY** | B★ | noun | eve ya da işyerine girip hırsızlık yapma suçu | The burglary happened while the family was on vacation. | 4 |
| 28 | **EVICTION** | E★ | noun | kiracıyı evden yasal olarak çıkarma işlemi | Many families face eviction when they cannot pay the rent. | 4 |
| 29 | **GANG** | G★ | noun | birlikte suç işleyen kişi grubu | The police arrested a gang that stole cars across the city. | 3 |
| 30 | **HIJACK** | H★ | verb | uçak ya da aracı zorla ele geçirmek | Two men tried to hijack the plane but failed. | 4 |
| 31 | **IMPRISON** | I★ | verb | birini hapse atıp özgürlüğünden yoksun bırakmak | The government can imprison anyone who breaks this law. | 4 |
| 32 | **LIBERTY** | L★ | noun | insanın özgürce yaşama ve davranma hakkı | Many people fought for liberty and equal rights. | 4 |
| 33 | **MUGGING** | M★ | noun | sokakta birine saldırıp parasını çalma | The mugging took place near the bus station. | 4 |
| 34 | **PARLIAMENT** | P★ | noun | yasaları çıkaran seçilmiş ulusal meclis | Parliament will vote on the new law tomorrow. | 3 |
| 35 | **SMUGGLE** | S★ | verb | bir şeyi yasadışı yoldan sınırdan geçirmek | They tried to smuggle gold across the border. | 4 |
| 36 | **UNLAWFUL** | U★ | adj | yasalara ya da kurallara aykırı olan | The judge decided that the arrest was unlawful. | 4 |
| 37 | **WRONGDOING** | W★ | noun | yasaya veya ahlaka aykırı davranış | The company denied any wrongdoing in the case. | 4 |
| 38 | **TRIAL** | T | noun | suçlamanın mahkemede incelenip karara bağlanması | His trial will begin next month in the capital. | 3 |
| 39 | **DEFENDANT** | D | noun | mahkemede hakkında dava açılmış kişi | The defendant said that he was not guilty. | 4 |
| 40 | **ROBBERY** | R | noun | silah ya da şiddet kullanarak para çalma | Two men were arrested for the bank robbery. | 3 |
| 41 | **ACTIVIST** | A | noun | toplumsal ya da siyasi değişim için çalışan kişi | The activist spoke about climate change at the meeting. | 4 |
| 42 | **CABINET** | C | noun | bakanlardan oluşan hükümet kurulu | The cabinet met to discuss the new budget plan. | 4 |

## 3. Doğrulama

- `content/` geçici kopyasına kartlar eklendi; `categories.csv` kopyasında `hobbies` B2|C1, `economy` B2, `society-law` B2 listeye alındı. `python3 tools/content-qa/qa.py`: **0 error**; yeni dosyalardan **0 warning**.
- Kalan 314 warning mevcut ve bu işle ilgisiz: yds-yokdil paketinin `school-work`/`idioms`/`phrasal-verbs` kartları ile `daily-life`/`food-drink` B2 gizli çiftleri (main'de 400 idi, 86'sı bu dört çift listelenince kalktı). Dolayısıyla "0 warning" hedefi bu işin kartları için sağlandı, tüm içerik için main'de zaten sağlanmıyor.
- Her cevap tüm seviyelerde ve paketlerde aranmış (büyük/küçük harf, çekim ve türev dahil): kopya yok. Blocklist'te yok; ipucunda/örnekte sızıntı yok; yeni cevaplar mevcut aynı seviye örneklerinde geçmiyor, yeni örnekler başka cevap içermiyor.
- Zaten var olduğu için **elenen** adaylar: lyrics, director, documentary, javelin, jockey, trailer, tournament (hobiler); export, import, profit, loan, budget, tariff, wholesale, deposit, surplus (ekonomi); crime, witness, bribe, ballot, sanction, strike, verdict, democracy (hukuk). Türev çifti oldukları için elenenler: retailer/retail, installment/install, harassment/harass, lender/lend.
- Eski bir örneği bozduğu için değiştirilenler (content/'e dokunmamak için kart değişti): savings, court, taxpayer.

## 4. Uygulama aşamasında yapılacaklar (bu aşamada yapılmadı)

- `content/categories.csv`: `hobbies` → A1|A2|B1|B2|C1, `economy` → B1|B2|C1, `society-law` → B1|B2|C1; docs/CONTENT.md §4 satırları (gizli notları) güncellenir; contentVersion +1.
- Eşik sonrası marj: her çift 32 kart (+2). Bir kart düşerse çift hâlâ eşikte (31).

## 5. Düşük güvenli seviye kararları (gözden geçirin)

- B2 mi daha alt mı: `purchase`, `gang`, `trial`, `league`, `merchant`, `parliament`; B2 mi C1 mi: `payroll`, `overdraft`, `creditor`, `imprison`, `hijack`, `wrongdoing`, `quintet`/`lineup` (C1'de uygun).
- Hassas konu: `hooligan`, `mugging`, `robbery`, `burglary`, `hijack`, `smuggle` (suç temalı, şiddet ayrıntısı yok); bildirim hariç tutma etiketi (`sensitive`) eklenmedi, gerekirse belirtin.
