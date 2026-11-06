# A2 batch 12 review (81 cards)

Dosya: `content/cards/A2/batch12.csv`. Odak: ince harfler (I, U, Y, V, Q, X, Z, J, K) + az sayıda N/G. `python3 tools/content-qa/qa.py` ile doğrulandı; Gradle çalıştırılmadı, `content/meta.json` bump edilmedi.

Harf sayıları (STARTS / CONTAINS): G 2 (2/0), I 15 (3/12), J 7 (6/1), K 9 (3/6), N 3 (3/0), O 2 (2/0), Q 3 (1/2), U 18 (3/15), V 5 (3/2), X 5 (0/5), Y 8 (0/8), Z 4 (2/2)

| id | answer | letter | rule | clue_tr | example_en / example_tr | ambiguity note |
|---|---|---|---|---|---|---|
| a2-pyjamas | PYJAMAS | J | CONTAINS | gece yatarken giyilen giysi takımı | Ali wears blue pyjamas at night. / Ali geceleri mavi pijama giyiyor. | - |
| a2-bicycle | BICYCLE | Y | CONTAINS | pedalla giden iki tekerlekli taşıt | Selin goes to school by bicycle. / Selin okula bisikletle gidiyor. | BIKE A1 aynı anlam; clue "pedallı" |
| a2-quack | QUACK | Q | STARTS | ördeğin çıkardığı ses | The duck gives a quack. / Ördek vak diye ses çıkarıyor. | - |
| a2-squid | SQUID | Q | CONTAINS | kalamar, denizde yaşayan yumuşakça hayvan | We eat fried squid by the sea. / Denizin kenarında kızarmış kalamar yiyoruz. | OCTOPUS ile yakın; sekiz kollu clue ortak; sahibi kontrol etsin |
| a2-aquarium | AQUARIUM | Q | CONTAINS | balıkların yaşadığı cam fanus | The fish swim in the aquarium. / Balıklar akvaryumda yüzüyor. | - |
| a2-extremely | EXTREMELY | X | CONTAINS | aşırı derecede, çok çok | The soup is extremely hot. / Çorba aşırı sıcak. | VERY A1 ayrı |
| a2-explode | EXPLODE | X | CONTAINS | büyük bir sesle patlamak | The balloon can explode. / Balon patlayabilir. | - |
| a2-zombie | ZOMBIE | Z | STARTS | filmlerde ölüden dirilen korkunç yaratık | The zombie in the film is ugly. / Filmdeki zombi çirkin. | - |
| a2-bronze | BRONZE | Z | CONTAINS | madalyada üçüncülük rengi, kırmızımsı kahverengi metal | Ali gets a bronze prize. / Ali bronz bir ödül alıyor. | - |
| a2-sneakers | SNEAKERS | K | CONTAINS | spor yapmak için giyilen rahat ayakkabılar | Mert wears white sneakers. / Mert beyaz spor ayakkabı giyiyor. | - |
| a2-milkshake | MILKSHAKE | K | CONTAINS | dondurma ve sütle yapılan soğuk içecek | Mert orders a banana milkshake. / Mert muzlu bir milkshake sipariş ediyor. | - |
| a2-icy | ICY | I | STARTS | çok soğuk ve buz gibi | The road is icy this morning. / Yol bu sabah buz tutmuş. | - |
| a2-ink | INK | I | STARTS | kalemle yazmak için kullanılan renkli sıvı | My pen has no ink. / Kalemimde mürekkep yok. | - |
| a2-driving | DRIVING | I | CONTAINS | araba kullanma işi | Driving at night is hard for Dad. / Gece araba kullanmak babam için zor. | DRIVE A1 ayrı |
| a2-lipstick | LIPSTICK | I | CONTAINS | dudaklara sürülen renkli makyaj ürünü | Her lipstick is red. / Rujunun rengi kırmızı. | - |
| a2-shrimp | SHRIMP | I | CONTAINS | kabuklu küçük pembe deniz ürünü | Mert does not like shrimp. / Mert karidesi sevmez. | - |
| a2-bikini | BIKINI | I | CONTAINS | plajda giyilen iki parçalı kadın mayosu | She has a new bikini for the beach. / Plaj için yeni bir bikinisi var. | - |
| a2-pin | PIN | I | CONTAINS | kumaşları tutturan ince, sivri uçlu metal | Mum puts a pin in the cloth. / Annem kumaşa bir toplu iğne takıyor. | clue "toplu iğne" anlamı; HAIRPIN (toka) değil |
| a2-lid | LID | I | CONTAINS | tencerenin üstünü kapatan kapak | Put the lid on the pan. / Kapağı tavanın üstüne koy. | - |
| a2-skinny | SKINNY | I | CONTAINS | çok zayıf ve ince | The cat is small and skinny. / Kedi küçük ve sıska. | THIN A1 ayrı; "sıska" |
| a2-shiny | SHINY | I | CONTAINS | ışıkta parlayan | She wears shiny shoes. / Parlak ayakkabılar giyiyor. | - |
| a2-spice | SPICE | I | CONTAINS | yemeğe tat veren baharat | Dad puts spice in the soup. / Babam çorbaya baharat koyuyor. | SPICY A1 sıfat |
| a2-stripe | STRIPE | I | CONTAINS | zebranın sırtındaki gibi uzun çizgi | The shirt has a white stripe. / Gömlekte beyaz bir çizgi var. | - |
| a2-swing | SWING | I | CONTAINS | çocuk parkında iki ipten sarkan salıncak | The child plays on the swing. / Çocuk salıncakta oynuyor. | - |
| a2-turtle | TURTLE | U | CONTAINS | sırtında sert kabuk taşıyan yavaş hayvan | A turtle walks on the beach. / Bir kaplumbağa plajda yürüyor. | - |
| a2-plum | PLUM | U | CONTAINS | mor veya kırmızı kabuklu yuvarlak yaz meyvesi | Ali eats a plum. / Ali bir erik yiyor. | - |
| a2-gum | GUM | U | CONTAINS | ağızda çiğnenen tatlı şey | Don't chew gum in class. / Derste sakız çiğneme. | - |
| a2-mug | MUG | U | CONTAINS | kulplu büyük çay veya kahve bardağı | Mert drinks tea from a big mug. / Mert büyük bir kupadan çay içiyor. | CUP A1 ayrı: "kulplu büyük" |
| a2-rug | RUG | U | CONTAINS | odanın zeminine serilen küçük halı | The cat sleeps on the rug. / Kedi küçük halının üstünde uyuyor. | CARPET A1 ayrı: "küçük" |
| a2-plug | PLUG | U | CONTAINS | elektrik kablosunun prize takılan ucu | Put the plug into the wall. / Fişi duvara tak. | - |
| a2-mustard | MUSTARD | U | CONTAINS | sosislerle yenen sarı acı sos | Dad puts mustard on his sausage. / Babam sosisine hardal sürüyor. | KETCHUP ayrı: "sarı" |
| a2-sunshine | SUNSHINE | U | CONTAINS | güneşin verdiği ışık ve sıcaklık | We sit in the sunshine. / Güneş ışığında oturuyoruz. | SUN A1 ayrı; "ışık ve sıcaklık" |
| a2-sunburn | SUNBURN | U | CONTAINS | güneşte kalınca derinin kızarması | Selin has sunburn on her nose. / Selin'in burnunda güneş yanığı var. | - |
| a2-trunk | TRUNK | U | CONTAINS | filin uzun burnu | The elephant lifts its trunk. / Fil hortumunu kaldırıyor. | araba bagajı anlamı da var; clue fil |
| a2-puddle | PUDDLE | U | CONTAINS | yağmurdan sonra yerde biriken küçük su | The boy jumps in a puddle. / Çocuk su birikintisine atlıyor. | - |
| a2-pump | PUMP | U | CONTAINS | su veya havayı basan alet | Ali uses a pump for the bike tyre. / Ali bisiklet lastiği için pompa kullanıyor. | - |
| a2-weekday | WEEKDAY | Y | CONTAINS | cumartesi ve pazar dışındaki herhangi bir gün | I work every weekday. / Her hafta içi günü çalışıyorum. | WEEKEND A1 zıt |
| a2-subway | SUBWAY | Y | CONTAINS | yerin altında giden şehir treni | Mert takes the subway to work. / Mert işe metroyla gidiyor. | - |
| a2-doorway | DOORWAY | Y | CONTAINS | kapının bulunduğu açıklık | A cat sits in the doorway. / Bir kedi kapı aralığında oturuyor. | DOOR A1 ayrı |
| a2-anyway | ANYWAY | Y | CONTAINS | her neyse, ne olursa olsun | It rains, but we go anyway. / Yağmur yağıyor ama yine de gidiyoruz. | - |
| a2-cherry | CHERRY | Y | CONTAINS | küçük, kırmızı, çekirdekli yaz meyvesi | Selin puts a cherry on the cake. / Selin kekin üstüne bir kiraz koyuyor. | - |
| a2-curry | CURRY | Y | CONTAINS | Hint mutfağında baharatlı soslu yemek | Mert loves hot curry. / Mert acı körüyü çok seviyor. | - |
| a2-dairy | DAIRY | Y | CONTAINS | süt ve peynir gibi süt ürünleri | Cheese is a dairy product. / Peynir bir süt ürünüdür. | clue "süt ürünleri" |
| a2-vanilla | VANILLA | V | STARTS | dondurmaya tat veren beyaz aromalı bitki | Ali likes vanilla cake. / Ali vanilyalı keki sever. | - |
| a2-vine | VINE | V | STARTS | üzüm yetiştiren sarmaşık bitki | Grapes hang from a vine. / Üzümler asmadan sarkıyor. | bitki kategorisi yok, daily-life seçildi |
| a2-shovel | SHOVEL | V | CONTAINS | toprağı kazmak için kullanılan geniş uçlu alet | Dad digs with a shovel. / Babam kürekle kazıyor. | - |
| a2-curve | CURVE | V | CONTAINS | dümdüz olmayan yay biçimli yol | The car slows down at the curve. / Araba virajda yavaşlıyor. | - |
| a2-relaxed | RELAXED | X | CONTAINS | stresi olmayan, rahatlamış | Dad feels relaxed on holiday. / Babam tatilde kendini rahat hissediyor. | RELAX A1 fiil |
| a2-zucchini | ZUCCHINI | Z | STARTS | yeşil uzun yazlık sebze | Mum cooks zucchini for dinner. / Annem akşam yemeği için kabak pişiriyor. | - |
| a2-buzz | BUZZ | Z | CONTAINS | arıların çıkardığı uğultu | I hear the buzz of a bee. / Bir arının vızıltısını duyuyorum. | - |
| a2-jaguar | JAGUAR | J | STARTS | Güney Amerika'da yaşayan benekli büyük kedi | The jaguar hides in the forest. / Jaguar ormanda saklanıyor. | marka riski: araba markası yok, hayvan clue |
| a2-jersey | JERSEY | J | STARTS | sporcuların giydiği forma | Ali wears a red jersey. / Ali kırmızı bir forma giyiyor. | - |
| a2-jolly | JOLLY | J | STARTS | şen, keyfi yerinde | Grandpa is a jolly man. / Dedem şen biri. | - |
| a2-jumpsuit | JUMPSUIT | J | STARTS | tek parça pantolonlu giysi | Selin wears a blue jumpsuit. / Selin mavi bir tulum giyiyor. | - |
| a2-chalk | CHALK | K | CONTAINS | tahtaya yazmak için kullanılan beyaz çubuk | The teacher writes with chalk. / Öğretmen tebeşirle yazıyor. | - |
| a2-bark | BARK | K | CONTAINS | köpeğin yüksek sesle ses çıkarması | The dog starts to bark. / Köpek havlamaya başlıyor. | - |
| a2-pickle | PICKLE | K | CONTAINS | sirkede bekletilmiş salatalık | Ali puts a pickle on his plate. / Ali tabağına bir turşu koyuyor. | - |
| a2-chick | CHICK | K | CONTAINS | yumurtadan yeni çıkmış tavuk yavrusu | The chick is yellow. / Civciv sarı. | - |
| a2-kindly | KINDLY | K | STARTS | nazik bir şekilde | He kindly helps the old man. / Yaşlı adama nazikçe yardım ediyor. | KIND A1 sıfat |
| a2-nation | NATION | N | STARTS | aynı topraklarda yaşayan büyük halk | The nation loves its team. / Ulus takımını seviyor. | COUNTRY A1; clue "halk" |
| a2-nod | NOD | N | STARTS | kafayı aşağı yukarı sallamak | Ali gives a nod and smiles. / Ali başını sallayıp gülümsüyor. | - |
| a2-nowhere | NOWHERE | N | STARTS | hiçbir yerde, hiçbir yere | There is nowhere to sit. / Oturacak hiçbir yer yok. | - |
| a2-oak | OAK | O | STARTS | palamut veren büyük orman ağacı | An old oak stands in the park. / Parkta yaşlı bir meşe duruyor. | - |
| a2-outdoor | OUTDOOR | O | STARTS | binanın dışında, açık havada yapılan | Mert loves outdoor sports. / Mert açık hava sporlarını seviyor. | INDOOR zıt |
| a2-goose | GOOSE | G | STARTS | çiftlikte yaşayan büyük beyaz su kuşu | A goose swims on the lake. / Bir kaz gölde yüzüyor. | DUCK A1 ayrı |
| a2-indoor | INDOOR | I | STARTS | binanın içinde yapılan veya bulunan | Selin likes indoor games on rainy days. / Selin yağmurlu günlerde kapalı alan oyunlarını sever. | INSIDE (A1) ile yakın; clue "yapılan/bulunan" sıfat |
| a2-bride | BRIDE | I | CONTAINS | düğünde evlenen kadın | The bride wears a white dress. / Gelin beyaz bir elbise giyiyor. | - |
| a2-unkind | UNKIND | U | STARTS | başkalarına karşı nazik olmayan | That was an unkind thing to say. / Bu söylenecek nazik olmayan bir şeydi. | - |
| a2-unwell | UNWELL | U | STARTS | biraz hasta, kendini iyi hissetmeyen | Mum feels unwell today. / Annem bugün kendini iyi hissetmiyor. | - |
| a2-unpack | UNPACK | U | STARTS | bavuldaki eşyaları çıkarıp yerleştirmek | We unpack our bags at the hotel. / Otelde çantalarımızı açıp eşyaları yerleştiriyoruz. | - |
| a2-pudding | PUDDING | U | CONTAINS | sütle yapılan yumuşak tatlı | Mum makes chocolate pudding. / Annem çikolatalı puding yapıyor. | CAKE A1 ayrı |
| a2-burger | BURGER | U | CONTAINS | iki ekmek arasında köfteli yemek | Mert eats a burger for lunch. / Mert öğle yemeğinde bir burger yiyor. | HAMBURGER A1; clue "köfteli yemek"; yakın eşanlamlı |
| a2-cushion | CUSHION | U | CONTAINS | koltuğa konan yumuşak yastık | The cat sleeps on the cushion. / Kedi minderin üstünde uyuyor. | PILLOW ile yakın; clue "koltuğa konan" vs "yatakta" |
| a2-vitamin | VITAMIN | V | STARTS | meyve ve sebzelerdeki sağlıklı besin maddesi | Oranges have a lot of vitamin C. / Portakallarda çok C vitamini var. | - |
| a2-axe | AXE | X | CONTAINS | odun kesmek için kullanılan aletin adı | He cuts a tree with an axe. / Bir ağacı baltayla kesiyor. | - |
| a2-wax | WAX | X | CONTAINS | mumun yapıldığı yumuşak madde | Wax is soft when it is warm. / Balmumu ısınınca yumuşar. | - |
| a2-jeep | JEEP | J | STARTS | bozuk yollarda giden dört çeker araç | They drive a jeep in the mountains. / Dağlarda bir cip kullanıyorlar. | - |
| a2-jog | JOG | J | STARTS | yavaş tempoda koşmak | Mum likes to jog in the park. / Annem parkta hafif tempoyla koşmayı seviyor. | RUN A1 ayrı; "yavaş tempoda" |
| a2-koala | KOALA | K | STARTS | okaliptüs yaprağı yiyen Avustralya hayvanı | The koala sleeps all day. / Koala bütün gün uyuyor. | - |
| a2-kneel | KNEEL | K | STARTS | dizlerinin üstüne çökmek | Ali has to kneel to find his pen. / Ali kalemini bulmak için diz çökmek zorunda. | - |
| a2-goalkeeper | GOALKEEPER | G | STARTS | futbolda kaleyi koruyan oyuncu | The goalkeeper catches the ball. / Kaleci topu yakalıyor. | - |

A2 harf toplamları (önce→sonra, tüm base A2): I 23→38, U 26→46, V 28→33, Y 23→31, Q 16→23, X 20→29, Z 21→27, J 21→30, K 24→36, N 27→30, O 28→32, G 29→31

## Sahip kararı sonrası değişiklikler

- 16 kart (TURQUOISE, ZODIAC, WALTZ, JAVELIN, FRECKLE, KENNEL, SQUINT, SQUEAK, QUICHE, CUSTARD, BRUISE, PIXEL, FAX, TUXEDO, OSTRICH, OVAL) B1'e taşındı: `B1-batch-10.md`.
- JOYFUL, BOXER ve KNIT bu batch'ten çıkarıldı. Not: sahip mesajında "BOXING, KNITTING" yazıyordu; bunlar A2'de zaten yayında olan eski kartlar (`a2-boxing` batch1, `a2-knitting` lgs paketi), silinmediler. Yakın anlam çiftinin bu batch'teki tarafları (BOXER, KNIT) çıkarıldı.
- SQUID ipucu "kalamar, denizde yaşayan yumuşakça hayvan" yapıldı ("sekiz kollu" kalktı).
- `python3 tools/content-qa/qa.py`: 0 hata / 0 uyarı. contentVersion 24.
