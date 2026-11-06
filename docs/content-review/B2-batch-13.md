# B2 batch 13 (83 kart)

Dosya: `content/cards/B2/batch13.csv`. Durum: owner kararları uygulandı (LOSER, UNITY, UNCERTAIN çıkarıldı; 8 kart C1 batch 12'ye taşındı), commit bekliyor. Hiçbir seviyede/pakette tekrar eden cevap yok, Amerikan yazımı.

`qa.py` ve `fill_blank_scan.py` geçici kopyada çalıştırıldı: 0 hata / 0 uyarı; CONTAINS X/Q kartları R1 (tasarım gereği fill-blank dışı).

Harf sayıları (STARTS / CONTAINS): X 8 (0/8), Q 2 (1/1), J 4 (4/0), W 9 (9/0), L 8 (8/0), U 6 (6/0), N 8 (8/0), K 3 (3/0), T 8 (8/0), M 6 (6/0), O 6 (6/0), H 6 (6/0), E 5 (5/0), G 4 (4/0)

B2 önce->sonra: X 15->23, Q 17->19, J 19->23, W 20->29, L 21->29, U 22->28, N 23->31, K 24->27, T 28->36, M 35->41, O 36->42, H 25->31, E 30->35, G 28->32 | toplam 759->842

| id | answer | letter | rule | clue_tr | example | not |
|---|---|---|---|---|---|---|
| b2-explosive | EXPLOSIVE | X | CONTAINS | patlamaya yol açabilen, kolay patlayan | The gas is highly explosive, so keep it away from the flame. / Gaz son derece patlayıcıdır, bu yüzden alevden uzak tutun. |  |
| b2-exposure | EXPOSURE | X | CONTAINS | zararlı bir etkiye (güneş, gürültü) açık kalma durumu | Long exposure to loud noise can damage your hearing. / Yüksek sese uzun süre maruz kalmak işitmenize zarar verebilir. | netleştirildi |
| b2-exclaim | EXCLAIM | X | CONTAINS | şaşkınlık ya da öfkeyle ani ve yüksek sesle söz etmek | Visitors often exclaim in surprise when they first see the canyon. / Ziyaretçiler kanyonu ilk gördüklerinde çoğu zaman şaşkınlıkla haykırır. | explain/expect'e uymayacak şekilde netleştirildi |
| b2-existence | EXISTENCE | X | CONTAINS | bir şeyin var olması, gerçekten mevcut olma durumu | Many people still doubt the existence of life on other planets. / Birçok kişi diğer gezegenlerde yaşamın varlığından hâlâ şüphe ediyor. | netleştirildi |
| b2-exhaust | EXHAUST | X | CONTAINS | bir kişiyi çok yorup gücünü tüketmek | Do not exhaust yourself before the race even starts. / Yarış başlamadan kendini tüketme. |  |
| b2-expire | EXPIRE | X | CONTAINS | geçerlilik süresi dolmak | My passport will expire in March, so I need a new one soon. / Pasaportumun süresi mart ayında dolacak, bu yüzden yakında yenisini almam gerek. |  |
| b2-relaxation | RELAXATION | X | CONTAINS | dinlenip gerginlikten kurtulma | Yoga gives her a sense of calm and relaxation after work. / Yoga ona işten sonra huzur ve rahatlama hissi veriyor. |  |
| b2-exploration | EXPLORATION | X | CONTAINS | bilinmeyen yerleri araştırarak keşfetme | Early exploration of the Arctic cost many sailors their lives. / Kuzey Kutbu'nun ilk keşif seferleri birçok denizcinin hayatına mal oldu. |  |
| b2-squad | SQUAD | Q | CONTAINS | spor ya da polis gibi küçük bir ekip veya kadro | The national squad trains twice a day before the final. / Milli takım kadrosu final öncesi günde iki kez antrenman yapıyor. |  |
| b2-quake | QUAKE | Q | STARTS | isim: yer sarsıntısı, "earthquake"in kısa hali | The quake shook the whole region at dawn. / Deprem şafakta bütün bölgeyi salladı. | ipucuna sözcük türü/ayrıntı eklendi |
| b2-judgment | JUDGMENT | J | STARTS | iyi karar verebilme yeteneği, sağduyu | Good judgment comes from experience, not from books. / İyi muhakeme kitaplardan değil deneyimden gelir. |  |
| b2-jobless | JOBLESS | J | STARTS | çalışacak işi olmayan | After the factory closed, hundreds of workers were suddenly jobless. / Fabrika kapanınca yüzlerce işçi birdenbire işsiz kaldı. |  |
| b2-justly | JUSTLY | J | STARTS | haklı ve adil biçimde, hak ettiği gibi | The company was justly praised for its honest advertising. / Şirket, dürüst reklamcılığı nedeniyle haklı olarak övüldü. |  |
| b2-jet-lag | JET LAG | J | STARTS | uzun uçuşlarda saat farkından doğan yorgunluk | I still have jet lag after the long flight from Tokyo. / Tokyo'dan gelen uzun uçuştan sonra hâlâ jet lag yaşıyorum. |  |
| b2-warranty | WARRANTY | W | STARTS | üreticinin arıza durumunda verdiği onarım sözü | The laptop is still under warranty, so the repair is free. / Dizüstü bilgisayar hâlâ garanti kapsamında, bu yüzden onarım ücretsiz. |  |
| b2-wicked | WICKED | W | STARTS | çok kötü kalpli, zalim | The wicked queen locked the princess in a tower. / Kötü kalpli kraliçe prensesi bir kuleye kapattı. | seviye şüphesi, owner kabul etti |
| b2-workable | WORKABLE | W | STARTS | uygulanabilir, işe yarayan | We need a workable plan before the end of the week. / Haftanın sonuna kadar uygulanabilir bir plana ihtiyacımız var. |  |
| b2-workaholic | WORKAHOLIC | W | STARTS | sürekli çalışmadan duramayan kişi | My brother is such a workaholic that he answers emails on vacation. / Kardeşim tam bir işkolik; tatilde bile e-postalara cevap veriyor. |  |
| b2-wrestle | WRESTLE | W | STARTS | güreşmek; zor bir sorunla uğraşmak | He had to wrestle with a difficult decision for weeks. / Haftalarca zor bir kararla boğuşmak zorunda kaldı. | seviye şüphesi, owner kabul etti |
| b2-wellness | WELLNESS | W | STARTS | beden ve ruh sağlığının iyi olması | Many offices now offer yoga classes to improve staff wellness. / Birçok ofis artık çalışanların iyilik halini artırmak için yoga dersleri sunuyor. |  |
| b2-worsen | WORSEN | W | STARTS | daha kötü hale gelmek ya da getirmek | Heavy rain could worsen the flooding in the valley. / Şiddetli yağmur vadideki taşkını daha da kötüleştirebilir. |  |
| b2-wrapper | WRAPPER | W | STARTS | şeker ya da yiyecek paketinin dış kâğıdı | Please put the candy wrapper in the bin. / Lütfen şeker ambalajını çöp kutusuna at. |  |
| b2-wasteful | WASTEFUL | W | STARTS | gereksiz yere harcayan, israf eden | It is wasteful to leave the lights on all night. / Bütün gece ışıkları açık bırakmak israftır. |  |
| b2-lease | LEASE | L | STARTS | ev ya da iş yeri için yapılan uzun süreli kira sözleşmesi | Our lease ends in June, so we must find a new apartment. / Kira sözleşmemiz haziranda bitiyor, bu yüzden yeni bir daire bulmalıyız. |  |
| b2-linen | LINEN | L | STARTS | ketenden dokunan hafif kumaş | A white linen shirt is perfect for hot summer days. / Beyaz keten gömlek sıcak yaz günleri için mükemmeldir. |  |
| b2-leadership | LEADERSHIP | L | STARTS | bir grubu yönetme becerisi ya da yönetici konumu | Strong leadership helped the team through the crisis. / Güçlü liderlik ekibin krizi atlatmasına yardımcı oldu. |  |
| b2-lengthy | LENGTHY | L | STARTS | çok uzun süren, uzayıp giden | The lengthy meeting ended after midnight. / Uzayıp giden toplantı gece yarısından sonra bitti. |  |
| b2-lobster | LOBSTER | L | STARTS | büyük pençeli, kırmızı kabuklu deniz canlısı | We ordered grilled lobster at a restaurant by the sea. / Deniz kenarındaki bir restoranda ızgara istakoz sipariş ettik. |  |
| b2-loneliness | LONELINESS | L | STARTS | yalnız olmanın verdiği hüzün | Many older people feel deep loneliness after retirement. / Birçok yaşlı insan emekli olduktan sonra derin bir yalnızlık hisseder. |  |
| b2-loudspeaker | LOUDSPEAKER | L | STARTS | sesi yükselterek yayan cihaz, hoparlör | The announcement over the loudspeaker was hard to understand. / Hoparlörden yapılan anons anlaşılması zordu. |  |
| b2-literary | LITERARY | L | STARTS | edebiyatla ilgili | She won a major literary prize for her first novel. / İlk romanıyla önemli bir edebiyat ödülü kazandı. |  |
| b2-ultimate | ULTIMATE | U | STARTS | sıfat: en sonda gelen, bunun ötesi olmayan; nihai | Winning the title was her ultimate goal. / Şampiyonluğu kazanmak onun nihai hedefiydi. | ipucuna sözcük türü eklendi |
| b2-urge | URGE | U | STARTS | fiil: birine bir şey yapması için ısrarla yüklenmek | Doctors urge everyone to get a flu shot each autumn. / Doktorlar herkesi her sonbahar grip aşısı olmaya çağırıyor. | ipucuna sözcük türü eklendi |
| b2-underneath | UNDERNEATH | U | STARTS | bir şeyin hemen altında, onun örttüğü yerde | She lifted the rug and found a coin underneath. / Halıyı kaldırdı ve altında bir bozuk para buldu. | ipucu UNDER'dan ayrıştırıldı |
| b2-unbearable | UNBEARABLE | U | STARTS | katlanılamayacak kadar kötü | The heat in the tiny room was unbearable. / Küçücük odadaki sıcak dayanılmazdı. |  |
| b2-uncommon | UNCOMMON | U | STARTS | yaygın olmayan; seyrek rastlanan | It is uncommon to see snow in this part of the country. / Ülkenin bu bölgesinde kar görmek nadirdir. | ipucu UNUSUAL'dan ayrıştırıldı |
| b2-underpaid | UNDERPAID | U | STARTS | emeğinin karşılığından az maaş alan | Nurses are often underpaid for the long hours they work. / Hemşireler çalıştıkları uzun saatler için çoğu zaman düşük maaş alır. |  |
| b2-notify | NOTIFY | N | STARTS | resmen bilgi vermek, haber vermek | Please notify us if you change your address. / Adresinizi değiştirirseniz lütfen bize bildirin. |  |
| b2-nausea | NAUSEA | N | STARTS | mide bulantısı | The smell of the hospital gave him a feeling of nausea. / Hastanenin kokusu ona mide bulantısı hissettirdi. |  |
| b2-necessarily | NECESSARILY | N | STARTS | zorunlu olarak, mutlaka | Expensive does not necessarily mean better. / Pahalı olmak mutlaka daha iyi olmak demek değildir. |  |
| b2-naive | NAIVE | N | STARTS | deneyimsiz ve kolay kandırılan, saf | It was naive to believe everything he said. / Söylediği her şeye inanmak saflıktı. |  |
| b2-needy | NEEDY | N | STARTS | maddi olarak yardıma muhtaç | The charity gives hot meals to needy families. / Hayır kurumu yardıma muhtaç ailelere sıcak yemek veriyor. |  |
| b2-networking | NETWORKING | N | STARTS | iş için insanlarla tanışıp bağlantı kurma | Networking at conferences can help you find a new job. / Konferanslarda iş çevresi edinmek yeni bir iş bulmanıza yardımcı olabilir. |  |
| b2-nitrogen | NITROGEN | N | STARTS | havanın büyük kısmını oluşturan renksiz gaz | Nitrogen makes up about 78 percent of the air we breathe. / Azot, soluduğumuz havanın yaklaşık yüzde 78'ini oluşturur. |  |
| b2-nonfiction | NONFICTION | N | STARTS | gerçeklere dayanan, kurgu olmayan yazılar | She mostly reads nonfiction, such as biographies and history books. / Çoğunlukla biyografi ve tarih kitabı gibi kurgu olmayan eserler okuyor. |  |
| b2-kidnap | KIDNAP | K | STARTS | bir kişiyi zorla kaçırıp alıkoymak | Two men tried to kidnap the businessman outside his hotel. / İki adam iş insanını otelinin önünde kaçırmaya çalıştı. | tag: sensitive |
| b2-knit | KNIT | K | STARTS | yün ipliğini şişle örmek | My grandmother used to knit scarves for the whole family. / Büyükannem bütün aile için atkı örürdü. |  |
| b2-knowingly | KNOWINGLY | K | STARTS | bile bile, farkında olarak | He would never knowingly hurt anyone. / O, kimseyi bile bile asla incitmez. |  |
| b2-tenant | TENANT | T | STARTS | ev sahibine kira ödeyerek oturan kişi | The tenant paid the rent on the first day of every month. / Kiracı kirayı her ayın ilk günü ödüyordu. |  |
| b2-thrill | THRILL | T | STARTS | ani ve güçlü bir heyecan, haz | Riding the roller coaster gave him a thrill he would never forget. / Hız treni ona asla unutamayacağı bir heyecan yaşattı. |  |
| b2-tragedy | TRAGEDY | T | STARTS | çok insanın acı çektiği büyük felaket | The flood was a tragedy for the whole village. / Sel bütün köy için büyük bir facia oldu. |  |
| b2-tribe | TRIBE | T | STARTS | ortak kültürü olan geleneksel topluluk | The tribe lives deep in the rainforest and hunts with bows. / Kabile yağmur ormanının derinlerinde yaşar ve yaylarla avlanır. |  |
| b2-tuition | TUITION | T | STARTS | okul ya da üniversite için ödenen eğitim ücreti | Tuition at private universities rises every year. / Özel üniversitelerin öğrenim ücreti her yıl artıyor. |  |
| b2-temper | TEMPER | T | STARTS | kolay öfkelenmeye yatkın huy | He has a terrible temper and shouts when he loses. / Çok kötü bir huyu var ve kaybedince bağırıyor. |  |
| b2-therapist | THERAPIST | T | STARTS | ruhsal sorunlarda görüşme yapan uzman | Her therapist suggested keeping a daily diary. / Terapisti günlük tutmasını önerdi. |  |
| b2-tactic | TACTIC | T | STARTS | bir mücadelede kullanılan özel yöntem | The coach changed his tactic in the second half. / Antrenör ikinci yarıda taktiğini değiştirdi. |  |
| b2-marathon | MARATHON | M | STARTS | 42 kilometrelik uzun mesafe koşusu | He trained for months to run the marathon. / Maratonu koşmak için aylarca antrenman yaptı. |  |
| b2-mortgage | MORTGAGE | M | STARTS | ev almak için bankadan alınan uzun vadeli kredi | They took out a mortgage to buy their first home. / İlk evlerini almak için konut kredisi çektiler. |  |
| b2-mumble | MUMBLE | M | STARTS | anlaşılmayacak kadar alçak sesle konuşmak | Please do not mumble; I cannot hear a word you say. / Lütfen mırıldanma; söylediğin tek kelimeyi duyamıyorum. |  |
| b2-masterpiece | MASTERPIECE | M | STARTS | bir sanatçının en değerli büyük eseri | Critics call the film a masterpiece. / Eleştirmenler filme başyapıt diyor. |  |
| b2-miracle | MIRACLE | M | STARTS | bilimle açıklanamayan harika olay | It was a miracle that nobody was hurt in the crash. / Kazada kimsenin yaralanmaması bir mucizeydi. |  |
| b2-mysterious | MYSTERIOUS | M | STARTS | gizemli, nedeni ya da kaynağı anlaşılmayan | A mysterious package arrived at her door this morning. / Bu sabah kapısına gizemli bir paket geldi. |  |
| b2-organic | ORGANIC | O | STARTS | kimyasal gübre kullanılmadan yetiştirilen | We buy organic vegetables from the farmers' market. / Sebzeleri çiftçi pazarından organik olarak alıyoruz. |  |
| b2-offensive | OFFENSIVE | O | STARTS | insanları rahatsız eden, saygısız | His offensive jokes made several guests leave. / Saygısız şakaları birkaç konuğun ayrılmasına yol açtı. |  |
| b2-optional | OPTIONAL | O | STARTS | zorunlu olmayan, isteğe bağlı | Joining the workshop is optional. / Atölye çalışmasına katılmak isteğe bağlıdır. |  |
| b2-overflow | OVERFLOW | O | STARTS | dolup kenarlarından taşmak | After the storm, the river began to overflow. / Fırtınadan sonra nehir taşmaya başladı. |  |
| b2-overtake | OVERTAKE | O | STARTS | yolda önündeki aracı hızla geçmek | It is dangerous to overtake on a bend. / Virajda öndeki aracı geçmek tehlikelidir. |  |
| b2-odor | ODOR | O | STARTS | genellikle hoş olmayan koku | A strange odor was coming from the fridge. / Buzdolabından garip bir koku geliyordu. |  |
| b2-haggle | HAGGLE | H | STARTS | satıcıyla fiyat üzerinde tartışıp pazarlık etmek | Tourists often haggle with the sellers at the bazaar. / Turistler çarşıda satıcılarla sık sık pazarlık eder. |  |
| b2-hassle | HASSLE | H | STARTS | can sıkan zahmet, dert | Renewing a visa can be a real hassle. / Vize yenilemek gerçek bir dert olabilir. |  |
| b2-handbook | HANDBOOK | H | STARTS | bir konunun temel bilgilerini veren kılavuz kitap | Every new employee receives a handbook on the first day. / Her yeni çalışan ilk gün bir el kitabı alır. |  |
| b2-hilarious | HILARIOUS | H | STARTS | güldürmekten kırıp geçiren | The comedian was hilarious, and we laughed all night. / Komedyen çok komikti ve bütün gece güldük. | eş anlam riski: HUMOROUS (B2, H); owner kabul etti |
| b2-herbal | HERBAL | H | STARTS | bitkilerden elde edilen (çay, ilaç) | She drinks herbal tea before bed. / Yatmadan önce bitki çayı içer. |  |
| b2-hatred | HATRED | H | STARTS | birine duyulan derin nefret | Years of war left a deep hatred between the two peoples. / Yıllarca süren savaş iki halk arasında derin bir nefret bıraktı. |  |
| b2-eager | EAGER | E | STARTS | bir şeyi yapmayı çok isteyen, hevesli | The children were eager to open their presents. / Çocuklar hediyelerini açmak için sabırsızlanıyordu. |  |
| b2-effortless | EFFORTLESS | E | STARTS | hiç zorlanmadan yapılmış gibi görünen | Her dancing looked effortless, though she practiced daily. / Her gün çalışmasına rağmen dansı zahmetsiz görünüyordu. |  |
| b2-easygoing | EASYGOING | E | STARTS | rahat tavırlı, kolay kızmayan | My boss is easygoing and rarely gets upset. / Patronum rahat tavırlı biridir ve nadiren sinirlenir. |  |
| b2-elegant | ELEGANT | E | STARTS | şık ve incelikli, zarif | She wore an elegant black dress to the concert. / Konsere zarif siyah bir elbiseyle gitti. | seviye şüphesi, owner kabul etti |
| b2-embarrass | EMBARRASS | E | STARTS | birini başkalarının yanında mahcup etmek | Do not embarrass me in front of my friends. / Arkadaşlarımın önünde beni utandırma. |  |
| b2-gamble | GAMBLE | G | STARTS | şansa güvenip para oynamak, kumar oynamak | He lost his savings when he began to gamble at the casino. / Kumarhanede kumar oynamaya başlayınca birikimini kaybetti. |  |
| b2-glamorous | GLAMOROUS | G | STARTS | göz alıcı ve gösterişli | Not every actor lives a glamorous life. / Her oyuncu gösterişli bir hayat yaşamaz. |  |
| b2-graceful | GRACEFUL | G | STARTS | hareketleri akıcı ve narin | The dancer's graceful movements amazed the audience. / Dansçının zarif hareketleri seyirciyi hayran bıraktı. |  |
| b2-groan | GROAN | G | STARTS | acı ya da sıkıntıyla inler gibi ses çıkarmak | The students groan whenever the teacher mentions a test. / Öğretmen sınavdan söz ettiğinde öğrenciler inliyor. |  |

## Not

- Y STARTS ve Q STARTS için gerçek B2 aday kalmadı (başka seviyelerde var).
- B2 toplamı 759->842.
