# B1 batch 12 review (75 kart)

Dosya: `content/cards/B1/batch12.csv`. `python3 tools/content-qa/qa.py`: 0 hata / 0 uyarı. İlk taslak 90 karttı: 14 kart seviye şüphesiyle B2'ye taşındı (`B2-batch-12.md`), `b1-omelet` çıkarıldı (`a2-omelette` var). Seçilen kartların ipuçları sözcük türü ve ayırt edici ayrıntıyla netleştirildi; `fill_blank_scan.py` bu partide temiz.

Harf sayıları (STARTS / CONTAINS): E 8 (8/0), G 9 (9/0), H 8 (8/0), L 11 (11/0), M 7 (7/0), N 7 (7/0), O 7 (7/0), T 4 (4/0), U 8 (8/0), W 6 (6/0)

B1 toplamları önce->sonra (yalnız `content/cards/B1`): E 31->39, G 33->42, H 33->41, L 31->42, M 32->39, N 34->41, O 30->37, T 37->41, U 33->41, W 36->42  | toplam 947->1022

| id | answer | letter | rule | clue_tr | example | not |
|---|---|---|---|---|---|---|
| b1-giggle | GIGGLE | G | STARTS | hafif sesle ve kıkır kıkır gülmek | The children began to giggle during the film. / Çocuklar film sırasında kıkır kıkır gülmeye başladı. | GRIN ve LAUGH farklı; clue sesli kıkırdama |
| b1-grammar | GRAMMAR | G | STARTS | bir dilin cümle kurma kuralları | My teacher explains English grammar very clearly. / Öğretmenim İngilizce dil bilgisini çok net anlatıyor. | - |
| b1-grin | GRIN | G | STARTS | yüzde beliren geniş, neşeli gülümseme | He had a big grin on his face after the game. / Maçtan sonra yüzünde kocaman bir sırıtış vardı. | SMILE sinonim; clue geniş sırıtış |
| b1-goldfish | GOLDFISH | G | STARTS | akvaryumda beslenen turuncu küçük balık | Our goldfish lives in a small bowl. / Japon balığımız küçük bir fanusta yaşıyor. | - |
| b1-gown | GOWN | G | STARTS | uzun ve şık kadın elbisesi ya da mezuniyet cübbesi | She wore a long blue gown to the party. / Partiye uzun mavi bir elbise giydi. | DRESS sinonim; clue uzun, gece/mezuniyet |
| b1-greenhouse | GREENHOUSE | G | STARTS | bitki yetiştirilen camdan yapılmış sıcak yapı | We grow tomatoes in the greenhouse. / Domatesleri serada yetiştiriyoruz. | - |
| b1-grasshopper | GRASSHOPPER | G | STARTS | uzun bacaklarıyla zıplayan yeşil böcek | A grasshopper jumped onto my shoe. / Bir çekirge ayakkabımın üstüne zıpladı. | - |
| b1-guitarist | GUITARIST | G | STARTS | gitar çalan müzisyen | The guitarist played a slow song. / Gitarist yavaş bir şarkı çaldı. | - |
| b1-gasoline | GASOLINE | G | STARTS | arabanın deposuna doldurulan yakıt | We stopped to buy gasoline on the way. / Yolda benzin almak için durduk. | US sözcük (BrE petrol); DIESEL ayrı |
| b1-hedgehog | HEDGEHOG | H | STARTS | sırtı dikenlerle kaplı küçük hayvan | A hedgehog was sleeping under the bush. / Bir kirpi çalının altında uyuyordu. | - |
| b1-herb | HERB | H | STARTS | yemeğe lezzet katan yeşil yapraklı bitki (nane vb.) | She added fresh herb leaves to the soup. / Çorbaya taze ot yaprakları ekledi. | clue: yeşil yapraklı bitki (SPICE ayrı). Kayıt: ABD'de H okunmaz |
| b1-hike | HIKE | H | STARTS | doğada yapılan uzun yürüyüş | We went on a long hike in the mountains. / Dağlarda uzun bir doğa yürüyüşüne çıktık. | WALK genel; clue doğa, uzun |
| b1-hood | HOOD | H | STARTS | montun başı örten kısmı | It was raining, so she put up her hood. / Yağmur yağıyordu, o yüzden kapüşonunu taktı. | Mahalle anlamı da var (AmE) |
| b1-horn | HORN | H | STARTS | arabada uyarı sesi çıkaran düğme ya da cihaz | The driver pressed the horn twice. / Sürücü kornaya iki kez bastı. | Boynuz anlamı da var; clue araba |
| b1-hunter | HUNTER | H | STARTS | hayvan avlamak için ormanda dolaşan kişi | The hunter walked quietly through the forest. / Avcı ormanda sessizce yürüdü. | clue: kişi, 'hunt' (fiil) değil |
| b1-heartbeat | HEARTBEAT | H | STARTS | kalbin düzenli atışı | You can hear your heartbeat after running. / Koştuktan sonra kalp atışını duyabilirsin. | - |
| b1-headlight | HEADLIGHT | H | STARTS | arabanın önündeki, geceyi aydınlatan lamba | One headlight of the car is broken. / Arabanın bir farı bozuk. | - |
| b1-nearby | NEARBY | N | STARTS | yakın bir yerde (zarf; ardından isim almaz) | Is there an ATM or a pharmacy nearby, please? / Yakınlarda bir ATM ya da eczane var mı, lütfen? | clue: zarf, 'near' gibi isim almaz; örnek bağlamlandı (R5) |
| b1-nasty | NASTY | N | STARTS | tadı, kokusu ya da davranışı iğrenç (sıfat) | This medicine has a nasty taste. / Bu ilacın berbat bir tadı var. | clue netleştirildi (tat/koku/davranış) |
| b1-necktie | NECKTIE | N | STARTS | gömleğin yakasına bağlanan uzun şerit | He wore a red necktie to the interview. / Mülakata kırmızı bir kravat taktı. | TIE kısa biçim; AmE |
| b1-newborn | NEWBORN | N | STARTS | yeni doğmuş bebek | The newborn slept all afternoon. / Yeni doğan bebek öğleden sonra boyunca uyudu. | BABY genel; clue yeni doğmuş |
| b1-noun | NOUN | N | STARTS | isim; kişi, yer ya da şeyin adı olan sözcük türü | 'Book' is a noun, not a verb. / 'Book' bir fiil değil, isimdir. | - |
| b1-normally | NORMALLY | N | STARTS | olağan koşullarda, istisna yoksa (zarf) | I normally wake up at seven. / Genellikle yedide uyanırım. | clue: olağan koşullar; USUALLY/normal ile karışmasın |
| b1-nosebleed | NOSEBLEED | N | STARTS | burundan kan gelmesi | He had a nosebleed in the hot classroom. / Sıcak sınıfta burnu kanadı. | - |
| b1-obese | OBESE | O | STARTS | sağlığı tehlikeye atacak kadar çok kilolu | The doctor told him he was obese. / Doktor ona obez olduğunu söyledi. | OVERWEIGHT (kart var) daha hafif; clue sağlığı tehdit eden |
| b1-oily | OILY | O | STARTS | yağ içeren ya da yağ bulaşmış | I don't like oily food at night. / Geceleri yağlı yemek sevmem. | GREASY yakın; clue 'yağ içeren' |
| b1-outer | OUTER | O | STARTS | en dışta bulunan (sıfat; isimden önce gelir) | The outer door was painted green. / Dış kapı yeşile boyanmıştı. | clue: isimden önce kullanılan sıfat |
| b1-oversleep | OVERSLEEP | O | STARTS | alarma rağmen planladığından geç uyanmak | I often oversleep on Sundays. / Pazar günleri sık sık fazla uyurum. | - |
| b1-occasionally | OCCASIONALLY | O | STARTS | düzensiz aralıklarla, arada bir (zarf) | We occasionally eat out on Sundays. / Pazar günleri ara sıra dışarıda yeriz. | clue: düzensiz aralık; OCCASION isim ayrı |
| b1-optimist | OPTIMIST | O | STARTS | olayların iyi biteceğine inanan kişi (isim) | My brother is an optimist and never worries. / Kardeşim iyimserdir ve hiç endişelenmez. | clue: isim/kişi; OPTIMISTIC sıfat ayrı |
| b1-outing | OUTING | O | STARTS | eğlenmek için yapılan kısa gezi | The class had an outing to the zoo. / Sınıf hayvanat bahçesine bir gezi yaptı. | TRIP/EXCURSION sinonim; clue kısa eğlence gezisi |
| b1-teamwork | TEAMWORK | T | STARTS | bir hedef için birlikte çalışma | Good teamwork helped us win the game. / İyi bir takım çalışması maçı kazanmamıza yardım etti. | - |
| b1-tangerine | TANGERINE | T | STARTS | portakala benzeyen küçük, kolay soyulan meyve | She peeled a tangerine for dessert. / Tatlı olarak bir mandalina soydu. | ORANGE ayrı |
| b1-thunderstorm | THUNDERSTORM | T | STARTS | şimşek, gök gürültüsü ve yağmurlu fırtına | We stayed inside during the thunderstorm. / Gök gürültülü fırtına sırasında içeride kaldık. | STORM genel |
| b1-toddler | TODDLER | T | STARTS | yeni yürümeye başlayan 1-3 yaş arası çocuk | The toddler is learning to walk. / Küçük çocuk yürümeyi öğreniyor. | BABY/CHILD ayrı |
| b1-unfortunately | UNFORTUNATELY | U | STARTS | ne yazık ki; kötü haber verirken (zarf) | Unfortunately, the store was closed. / Maalesef mağaza kapalıydı. | clue: cümle başı zarf; 'shop' → 'store' |
| b1-underwater | UNDERWATER | U | STARTS | suyun altında olan | We took photos of underwater fish. / Su altındaki balıkların fotoğraflarını çektik. | adj/adv; clue su altı |
| b1-undo | UNDO | U | STARTS | yapılmış bir işlemi geri almak | Click this button to undo the last change. / Son değişikliği geri almak için bu tuşa bas. | Bağını çözmek anlamı da var; teknoloji bağlamı |
| b1-uneven | UNEVEN | U | STARTS | düz olmayan, engebeli | The road was uneven and full of holes. / Yol engebeliydi ve çukurlarla doluydu. | - |
| b1-unlimited | UNLIMITED | U | STARTS | sınırı olmayan | The plan includes unlimited internet. / Pakette sınırsız internet var. | LIMITED (kart var) karşıtı |
| b1-unnecessary | UNNECESSARY | U | STARTS | gerekli olmayan, lüzumsuz | Do not buy unnecessary things. / Gereksiz şeyler satın alma. | NECESSARY kartı var (karşıtı) |
| b1-unsafe | UNSAFE | U | STARTS | insanı tehlikeye atabilecek, güvenli olmayan | The old bridge is unsafe to cross. / Eski köprüden geçmek güvenli değil. | clue: tehlikeye atabilecek (UNPLEASANT/UNLUCKY ayrı) |
| b1-unwrap | UNWRAP | U | STARTS | paketin sarmalını açmak | The kids unwrap their gifts on New Year's Day. / Çocuklar yılbaşında hediyelerini açar. | WRAP (kart var) karşıtı |
| b1-wallpaper | WALLPAPER | W | STARTS | odanın duvarına yapıştırılan desenli kâğıt | Mom peeled the old wallpaper off the hallway wall. / Annem koridor duvarındaki eski duvar kâğıdını söktü. | ipucu 'duvar kâğıdı'; örnek ve çeviri uyumlu |
| b1-waterproof | WATERPROOF | W | STARTS | su geçirmeyen | My jacket is waterproof, so I stay dry. / Montum su geçirmez, o yüzden kuru kalırım. | - |
| b1-weekly | WEEKLY | W | STARTS | haftada bir olan | We have a weekly meeting on Mondays. / Pazartesi günleri haftalık toplantımız var. | MONTHLY kart var; clue haftada bir |
| b1-western | WESTERN | W | STARTS | batıda olan, batıya ait | They live in the western part of the city. / Şehrin batı kesiminde yaşıyorlar. | NORTHERN kartı var |
| b1-witch | WITCH | W | STARTS | masallarda süpürgeyle uçan, büyü yapan kadın | The witch in the story had a black cat. / Hikâyedeki cadının kara bir kedisi vardı. | WIZARD erkek büyücü |
| b1-worm | WORM | W | STARTS | toprakta yaşayan uzun, ayaksız hayvan | A bird pulled a worm out of the ground. / Bir kuş yerden bir solucan çıkardı. | - |
| b1-earphones | EARPHONES | E | STARTS | kulağa takılan küçük müzik dinleme aygıtı | I forgot my earphones at home. / Kulaklıklarımı evde unuttum. | HEADPHONES (başlıklı) kart var; clue kulağa takılan |
| b1-eastern | EASTERN | E | STARTS | doğuda olan, doğuya ait | They live in the eastern part of the city. / Şehrin doğu kesiminde yaşıyorlar. | WESTERN/NORTHERN ile karşıt |
| b1-echo | ECHO | E | STARTS | dağda bağırınca duyulan geri dönen ses | We heard our echo in the cave. / Mağarada kendi yankımızı duyduk. | - |
| b1-edition | EDITION | E | STARTS | bir kitabın belirli basımı | This is the second edition of the book. / Bu, kitabın ikinci baskısı. | - |
| b1-eggplant | EGGPLANT | E | STARTS | mor renkli, uzun sebze | My mother cooks eggplant with tomatoes. / Annem patlıcanı domatesle pişirir. | AmE (BrE aubergine) |
| b1-emotional | EMOTIONAL | E | STARTS | çabuk duygulanan ya da duygu yüklü (sıfat) | It was an emotional goodbye at the airport. / Havalimanında duygusal bir veda oldu. | clue: sıfat; EMOTION isim ayrı |
| b1-energetic | ENERGETIC | E | STARTS | çok enerjisi olan, hareketli | My grandfather is still very energetic. / Dedem hâlâ çok enerjik. | ACTIVE yakın |
| b1-episode | EPISODE | E | STARTS | bir dizinin tek bölümü | I stayed up late to watch the final episode of the show. / Programın son bölümünü izlemek için geç saate kadar uyanık kaldım. | örnek bağlamlandı (R5) |
| b1-ladybug | LADYBUG | L | STARTS | kırmızı üzerine siyah benekli küçük böcek | A ladybug landed on the leaf. / Yaprağın üstüne bir uğur böceği kondu. | AmE (BrE ladybird) |
| b1-lantern | LANTERN | L | STARTS | kamp yaparken taşınan, mum ya da ampullü fener | They lit a lantern in the tent. / Çadırda bir fener yaktılar. | - |
| b1-lamb | LAMB | L | STARTS | yavru koyun; eti de böyle anılır | We had roast lamb for dinner. / Akşam yemeğinde kuzu kızartması yedik. | clue: yavru koyun |
| b1-lasagna | LASAGNA | L | STARTS | kat kat makarna, kıyma ve peynirle yapılan İtalyan yemeği | My dad makes great lasagna. / Babam harika lazanya yapar. | AmE yazım |
| b1-leak | LEAK | L | STARTS | borudan ya da çatıdan sıvının sızması | There is a leak under the kitchen sink. / Mutfak lavabosunun altında bir sızıntı var. | Fiil de olur |
| b1-leopard | LEOPARD | L | STARTS | benekli tüylü büyük yırtıcı kedi | The leopard climbed up a tree. / Leopar bir ağaca tırmandı. | CHEETAH/TIGER farklı; clue benekli |
| b1-leggings | LEGGINGS | L | STARTS | bacağa tam oturan esnek spor tayt | She wears leggings to the gym. / Spor salonuna tayt giyer. | - |
| b1-lightbulb | LIGHTBULB | L | STARTS | lambaya takılan, ışık veren cam parça | We need a new lightbulb in the hall. / Koridora yeni bir ampul lazım. | - |
| b1-loaf | LOAF | L | STARTS | bir bütün halinde pişmiş ekmek | She bought a loaf of bread. / Bir somun ekmek aldı. | BREAD ayrı; clue bir bütün |
| b1-laughter | LAUGHTER | L | STARTS | gülen insanların çıkardığı ses | The room was full of laughter. / Oda kahkahayla doluydu. | LAUGH fiil; GIGGLE ayrı |
| b1-librarian | LIBRARIAN | L | STARTS | kütüphanede çalışan görevli | The librarian helped me find the book. / Kütüphaneci kitabı bulmama yardım etti. | - |
| b1-macaroni | MACARONI | M | STARTS | kısa, içi boş kıvrık makarna | We had macaroni and cheese for lunch. / Öğle yemeğinde peynirli makarna yedik. | PASTA genel |
| b1-mailbox | MAILBOX | M | STARTS | postacının mektup bıraktığı kutu | There was a letter in the mailbox. / Posta kutusunda bir mektup vardı. | AmE; LETTER ayrı |
| b1-makeup | MAKEUP | M | STARTS | yüze sürülen ruj, far gibi güzellik ürünleri | She put on some makeup before the party. / Partiden önce biraz makyaj yaptı. | - |
| b1-marble | MARBLE | M | STARTS | heykel ve yer döşemesinde kullanılan parlak taş | The floor of the hotel was made of marble. / Otelin zemini mermerden yapılmıştı. | çeviri düzeltildi (R6) |
| b1-mermaid | MERMAID | M | STARTS | masallarda yarı insan yarı balık deniz kızı | The film is about a young mermaid. / Film genç bir deniz kızı hakkında. | - |
| b1-midnight | MIDNIGHT | M | STARTS | gece saat on iki | We watched the fireworks at midnight. / Havai fişekleri gece yarısı izledik. | - |
| b1-mitten | MITTEN | M | STARTS | parmakları ayrı olmayan kış eldiveni | She lost one mitten in the snow. / Karda tek eldivenini kaybetti. | GLOVE ayrı parmak; clue parmaksız ayrım |
