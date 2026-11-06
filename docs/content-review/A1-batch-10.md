# A1 batch 10 review (60 cards)

Dosya: `content/cards/A1/batch10.csv`. Kural: A1 sözcükleri, bu batch'in odağı ince harfler (I, U, O, V, Y, Q, X, Z). `python3 tools/content-qa/qa.py` = 0 hata / 0 uyarı. Gradle derleyici çalıştırılmadı (başka oturum kullanıyor); `content/meta.json` contentVersion BUMP EDİLMEDİ (sahibi inceledikten sonra artırılacak).

Harf sayıları (STARTS / CONTAINS): I 11 (4/7), U 13 (2/11), O 12 (4/8), V 4 (4/0), Y 10 (4/6), Q 1 (0/1), X 5 (0/5), Z 4 (0/4)

Notlar:
- Örnek cümleler çok temel kelimelerle yazıldı; A1 cevaplarından hiçbiri (çekimli/çoğul biçimler hariç) örnekte tam sözcük olarak geçmiyor. Eski örneklerle çakışan 23 aday (SHY, TRUCK, PUZZLE, ROUND, WIDE, VASE, PIE, SHIP, COOKIE, PUPIL, CLOWN, POCKET, QUIZ, VET, ORDER, DAILY, ONCE, SILVER, TIDY, POEM, ROCK, GOLD, LOVELY/HOLE/ROPE) eski kartlara dokunmamak için çıkarıldı.
- Q/X/Z ve bazı V/Y/U kartları resmî A1 listesinin dışında olabilir (kural istisnası, aşağıda işaretli). Hedef: Q/X/Z >= 25 yine de karşılanmadı (bu batch'te Q 6, X 6, Z 10), sonraki batch gerekli.
- ID'ler tüm seviyelerde benzersiz; blocklist temiz.

| id | answer | letter | rule | clue_tr | example_en / example_tr | ambiguity note |
|---|---|---|---|---|---|---|
| a1-important | IMPORTANT | I | STARTS | çok önemli, değerli | Health is important for us. / Sağlık bizim için önemlidir. | - |
| a1-invite | INVITE | I | STARTS | bir kişiyi partiye çağırmak | Selin wants to invite her friends. / Selin arkadaşlarını davet etmek istiyor. | - |
| a1-insect | INSECT | I | STARTS | sineğin ve karıncanın da türü olan altı bacaklı küçük canlı | Ali sees an insect on his shoulder. / Ali omzunda bir böcek görüyor. | BUG ayrı kart; clue tür adı |
| a1-iron | IRON | I | STARTS | kıyafetlerin kırışıklığını gideren sıcak alet | Mum has an iron. / Annemin bir ütüsü var. | metal anlamı değil |
| a1-skin | SKIN | I | CONTAINS | vücudu dıştan saran doku | Her skin is soft. / Cildi yumuşak. | - |
| a1-chin | CHIN | I | CONTAINS | ağzın altındaki yüz bölümü | Ali has a beard on his chin. / Ali'nin çenesinde sakal var. | JAW düşük seviyede kart değil |
| a1-life | LIFE | I | CONTAINS | doğumdan ölüme kadar süren zaman | Selin loves her life. / Selin hayatını seviyor. | - |
| a1-piece | PIECE | I | CONTAINS | bir bütünden ayrılmış küçük bölüm | Ali wants a piece of watermelon. / Ali bir parça karpuz istiyor. | - |
| a1-spicy | SPICY | I | CONTAINS | içinde acı baharat olan | Mert likes spicy sauce. / Mert acılı sosu sever. | HOT sıcaklık anlamı da taşır; clue yemek |
| a1-rich | RICH | I | CONTAINS | çok parası olan | Mr Aksoy is rich. / Bay Aksoy zengin. | - |
| a1-sign | SIGN | I | CONTAINS | yolda uyarı veya bilgi veren levha | Ali reads the sign. / Ali levhayı okuyor. | - |
| a1-uniform | UNIFORM | U | STARTS | okulda ya da işte giyilen aynı tip kıyafet | Selin wears a uniform. / Selin üniforma giyiyor. | - |
| a1-unhappy | UNHAPPY | U | STARTS | mutlu olmayan, üzgün ve keyifsiz | Mert is unhappy. / Mert mutsuz. | SAD ile yakın; clue "keyifsiz" |
| a1-sound | SOUND | U | CONTAINS | kulakla duyduğumuz şey | Ali likes the sound of birds. / Ali kuşların sesini seviyor. | - |
| a1-button | BUTTON | U | CONTAINS | gömleği iliğe takarak kapatan küçük yuvarlak parça | Ali has a button on his sleeve. / Ali'nin kolunda bir düğme var. | - |
| a1-museum | MUSEUM | U | CONTAINS | eski ve değerli eserlerin sergilendiği yer | Selin goes to the museum with Mert. / Selin Mert'le müzeye gidiyor. | - |
| a1-bug | BUG | U | CONTAINS | yatakta ya da evde çıkan küçük haşere | Mert sees a bug on his pillow. / Mert yastığında bir böcek görüyor. | INSECT ile çakışır; clue "haşere" |
| a1-nut | NUT | U | CONTAINS | kabuğu sert yağlı yemiş | Selin eats a nut. / Selin bir fındık yiyor. | - |
| a1-plus | PLUS | U | CONTAINS | toplama işareti, artı | 3 plus 4 is 7. / 3 artı 4, 7 eder. | - |
| a1-hug | HUG | U | CONTAINS | sevgiyle sarılma | Ali wants a hug. / Ali bir sarılma istiyor. | - |
| a1-oil | OIL | O | STARTS | yemek pişirirken kullanılan sıvı yağ | Mum cooks with oil. / Annem yağ ile yemek yapıyor. | - |
| a1-own | OWN | O | STARTS | başkasına ait olmayan, kendine ait | Selin has her own diary. / Selin'in kendi günlüğü var. | - |
| a1-olive | OLIVE | O | STARTS | zeytinyağı da çıkan küçük yeşil ya da siyah meyve | Ali eats an olive. / Ali bir zeytin yiyor. | - |
| a1-soccer | SOCCER | O | CONTAINS | Amerikan İngilizcesinde futbol | Mert plays soccer with Ali. / Mert, Ali'yle futbol oynuyor. | FOOTBALL ile çakışma riski; clue ABD sözcüğü |
| a1-rose | ROSE | O | CONTAINS | dikenli saplı, güzel kokulu çiçek | Ali gives Selin a rose. / Ali Selin'e bir gül veriyor. | FLOWER genel; burada özel |
| a1-stone | STONE | O | CONTAINS | yerde bulunan sert küçük kaya parçası | Mert throws a stone. / Mert bir taş atıyor. | - |
| a1-wood | WOOD | O | CONTAINS | ağaçtan elde edilen, masa yapılan malzeme | The shelf is made of wood. / Raf ahşaptan yapılmış. | - |
| a1-broom | BROOM | O | CONTAINS | yeri süpürmeye yarayan uzun saplı alet | Mum has a broom. / Annemin bir süpürgesi var. | - |
| a1-van | VAN | V | STARTS | küçük kapalı yük ve yolcu aracı | A van stops. / Bir minibüs duruyor. | - |
| a1-violin | VIOLIN | V | STARTS | yay ile çalınan dört telli müzik aleti | Ayşe plays the violin. / Ayşe keman çalıyor. | - |
| a1-voice | VOICE | V | STARTS | konuşurken ya da şarkı söylerken çıkan ses | Selin has a soft voice. / Selin'in yumuşak bir sesi var. | SOUND ile karışmaz; clue insan sesi |
| a1-view | VIEW | V | STARTS | pencereden görülen manzara | Ali loves the view. / Ali manzarayı çok seviyor. | - |
| a1-yard | YARD | Y | STARTS | evin önündeki ya da arkasındaki açık alan | Ali plays in the yard. / Ali avluda oynuyor. | GARDEN ile karışmaz (avlu) |
| a1-yogurt | YOGURT | Y | STARTS | sütten yapılan, kaşıkla yenen ekşi gıda | Ali eats yogurt. / Ali yoğurt yiyor. | - |
| a1-yoga | YOGA | Y | STARTS | esneme ve nefes egzersizlerinden oluşan spor | Mum does yoga. / Annem yoga yapıyor. | - |
| a1-yummy | YUMMY | Y | STARTS | çok lezzetli (çocuk dili) | This dessert is yummy! / Bu tatlı çok lezzetli! | DELICIOUS ile çakışır; clue çocuk dili |
| a1-candy | CANDY | Y | CONTAINS | çocukların sevdiği şekerli küçük tatlı | Selin gets candy. / Selin şeker alıyor. | SUGAR ile ayrışır |
| a1-lucky | LUCKY | Y | CONTAINS | şansı yaver giden | Ali is lucky. / Ali şanslı. | - |
| a1-teddy | TEDDY | Y | CONTAINS | çocukların sarıldığı tüylü oyuncak ayı | Ayşe sleeps with her teddy. / Ayşe oyuncak ayısıyla uyuyor. | TOY ile karışmaz |
| a1-copy | COPY | Y | CONTAINS | aynısını yapmak ya da yazmak | Ali, copy the sentence. / Ali, cümleyi kopyala. | - |
| a1-squirrel | SQUIRREL | Q | CONTAINS | fındık toplayan kuyruğu gür küçük orman hayvanı | A squirrel eats an acorn. / Bir sincap meşe palamudu yiyor. | - |
| a1-fox | FOX | X | CONTAINS | kızıl tüylü, kurnaz bilinen ormandaki hayvan | A fox sleeps. / Bir tilki uyuyor. | - |
| a1-mix | MIX | X | CONTAINS | iki ya da daha fazla şeyi karıştırmak | Mix the flour and the eggs. / Unu ve yumurtaları karıştır. | - |
| a1-exit | EXIT | X | CONTAINS | binadan çıkış kapısı | The exit is there. / Çıkış orada. | - |
| a1-extra | EXTRA | X | CONTAINS | gerekenden fazla, ek | Selin has an extra coin. / Selin'in fazladan bir bozuk parası var. | - |
| a1-fix | FIX | X | CONTAINS | bozuk bir şeyi onarmak | Mert can fix it. / Mert onu tamir edebilir. | - |
| a1-size | SIZE | Z | CONTAINS | bir şeyin büyüklüğü, beden | My size is 38. / Bedenim 38. | - |
| a1-lazy | LAZY | Z | CONTAINS | çalışmaktan hoşlanmayan, tembel | The hamster is lazy. / Hamster tembel. | - |
| a1-crazy | CRAZY | Z | CONTAINS | çok garip ve akıldışı, deli gibi | That plan is crazy! / O plan çılgınca! | - |
| a1-dozen | DOZEN | Z | CONTAINS | on iki tane | Ali buys a dozen eggs. / Ali bir düzine yumurta alıyor. | - |
| a1-dust | DUST | U | CONTAINS | eşyaların üstünde biriken ince toz | Dust is on the shelf. / Rafta toz var. | - |
| a1-suit | SUIT | U | CONTAINS | ceket ve pantolondan oluşan takım elbise | Ali wears a suit. / Ali takım elbise giyiyor. | - |
| a1-jungle | JUNGLE | U | CONTAINS | sıcak ülkelerdeki sık yağmur ormanı | Ali sees tigers in the jungle. / Ali tropik ormanda kaplanlar görüyor. | FOREST ile ayrışır (tropik) |
| a1-mud | MUD | U | CONTAINS | yağmurdan sonra yerde oluşan ıslak çamur | Ali has mud on his shoes. / Ali'nin ayakkabılarında çamur var. | - |
| a1-popcorn | POPCORN | O | CONTAINS | sinemada yenen patlamış mısır | Ali eats popcorn at the movies. / Ali sinemada patlamış mısır yiyor. | - |
| a1-jelly | JELLY | Y | CONTAINS | çocukların sevdiği sallanan meyveli tatlı | Selin loves jelly. / Selin jöleyi çok seviyor. | - |
| a1-bunny | BUNNY | Y | CONTAINS | küçük tavşan, çocuk dili | The bunny jumps. / Tavşan zıplıyor. | RABBIT ile çakışır; clue çocuk dili |
| a1-snowman | SNOWMAN | O | CONTAINS | kardan yapılan havuç burunlu figür | Ali builds a snowman. / Ali bir kardan adam yapıyor. | - |
| a1-comb | COMB | O | CONTAINS | saçı düzeltmek için kullanılan dişli araç | Ayşe has a comb. / Ayşe'nin bir tarağı var. | - |
| a1-owner | OWNER | O | STARTS | dükkânın ya da evin sahibi | Mert is the owner. / Mert sahibi. | - |

## Sahip kararı sonrası değişiklikler

- Seviye şüphesi olan 26 kart A1'den çıkarıldı. 23'ü zaten A2'de (veya A2 `moved-from-a1.csv`/`smoke.csv` içinde) kart olduğu için tekrar eklenmedi: VOLLEYBALL, VACATION, VISITOR, VALLEY, VOTE, YAWN, JOY, SQUARE, MOSQUITO, EQUAL, QUITE, EXPLAIN, ZIP, AMAZING, PINEAPPLE, DINOSAUR, GUESS, VOLCANO, MAZE, SNEEZE, BREEZE, FROZEN, SQUEEZE. Kalan 3 kart `A2-batch-11.md` ile A2'ye taşındı: UNIT, VOWEL, VEST.
- BUG ipucu belirginleştirildi ("yatakta ya da evde çıkan küçük haşere"); OWNER ipucu "dükkânın ya da evin sahibi" yapıldı.
- SOCCER, YUMMY, UNHAPPY, CANDY, BUNNY baş harfleri farklı olduğundan kaldı; SOCCER/VACATION ABD yazımı kalıyor.
- `python3 tools/content-qa/qa.py`: 0 hata / 0 uyarı. Gradle derleyici çalıştırılmadı.
