# A1 batch 6 review (100 cards)

Judgement calls: new group of countries/continents (FRANCE, GERMANY, ITALY, SPAIN, CHINA, JAPAN, RUSSIA, AMERICA, BRAZIL, TURKEY, EUROPE, ASIA, AFRICA) and languages/nationalities (FRENCH, GERMAN, ITALIAN, SPANISH, CHINESE, JAPANESE, RUSSIAN, TURKISH, AMERICAN, BRITISH); all in `daily-life` (no geography category). Language words use pos noun with clue "X'da konuşulan dil". Very frequent words (HAVE, HERE, WHERE, FILM, SMALL, ...) were dropped to avoid leaking into ~170 old examples; 25 old examples rewritten, leak check = 0.
Skipped on purpose: multi-word answers containing another answer (ICE CREAM, LIVING ROOM, POST OFFICE, ALL RIGHT).
Doubtful CEFR level (owner may drop): TOOTHACHE, GOLF, FROG, COLLEGE, BEER, EXAMPLE, LADY, SHOPPING, BRAZIL, AFRICA/ASIA/EUROPE, BASKETBALL, MOTORBIKE, POSTCARD, ALPHABET.
Semantic flags: COOL "hafif soğuk, serin" vs COLD "soğuk"; number words use adj with the Turkish number as clue; CENTRE/COLLEGE Turkish clues are descriptive; TURKEY clue disambiguates country vs bird.

| id | answer | clue | example |
|---|---|---|---|
| a1-away | AWAY | uzağa, başka bir yere | The thief ran away. / Hırsız koşarak kaçtı. |
| a1-apartment | APARTMENT | bir apartmandaki ev | Our apartment has a large balcony. / Dairemizin büyük bir balkonu var. |
| a1-art | ART | resim ve heykel gibi güzel sanatlar | He is interested in modern art. / O, modern sanatla ilgileniyor. |
| a1-alphabet | ALPHABET | harflerin belirli sırayla dizildiği takım | The alphabet starts with A. / Alfabe A ile başlar. |
| a1-america | AMERICA | başkenti Washington olan ülke | She was born in America. / O, Amerika'da doğdu. |
| a1-american | AMERICAN | Amerika'dan olan | Grace is American. / Grace Amerikalı. |
| a1-asia | ASIA | Çin ve Japonya'nın bulunduğu en büyük kıta | Asia has the highest mountains. / Asya'da en yüksek dağlar var. |
| a1-africa | AFRICA | Mısır ve Nijerya'nın bulunduğu kıta | Many lions hunt in Africa. / Afrika'da birçok aslan avlanır. |
| a1-beer | BEER | arpadan yapılan köpüklü, alkollü içki | He ordered a beer. / Bir bira sipariş etti. |
| a1-basketball | BASKETBALL | potaya top atılan takım sporu | Ayşe plays basketball well. / Ayşe çok iyi basketbol oynar. |
| a1-before | BEFORE | daha önce, geçmişte | I have seen this before. / Bunu daha önce gördüm. |
| a1-boots | BOOTS | bilekten yukarı çıkan sağlam ayakkabılar | Her boots are muddy. / Çizmeleri çamurlu. |
| a1-british | BRITISH | İngiltere'den olan | Tom is British. / Tom İngiliz. |
| a1-brazil | BRAZIL | Güney Amerika'nın en büyük ülkesi | Samba is popular in Brazil. / Samba Brezilya'da popülerdir. |
| a1-card | CARD | üzerinde yazı olan küçük karton parça | He sent a card from Paris. / Paris'ten bir kart gönderdi. |
| a1-cool | COOL | hafif soğuk, serin | It is cool in the shade. / Gölgede serin. |
| a1-classroom | CLASSROOM | derslerin işlendiği oda | Everyone is in the classroom. / Herkes sınıfta. |
| a1-chips | CHIPS | kızartılmış patates çubukları | Ayşe is eating chips. / Ayşe patates kızartması yiyor. |
| a1-centre | CENTRE | bir yerin tam orta kısmı | Ali works in the centre of Ankara. / Ali, Ankara'nın merkezinde çalışır. |
| a1-classmate | CLASSMATE | aynı sınıfta okuyan kişi | Selin is my classmate. / Selin benim sınıf arkadaşım. |
| a1-cap | CAP | siperlikli yumuşak başlık | This cap is too tight. / Bu şapka çok dar. |
| a1-china | CHINA | başkenti Pekin olan ülke | This silk is made in China. / Bu ipek Çin'de yapılmış. |
| a1-chinese | CHINESE | Çin'de konuşulan dil | Chinese has many characters. / Çincede çok karakter var. |
| a1-college | COLLEGE | liseden sonra gidilen yüksekokul | Ayşe goes to college in Ankara. / Ayşe, Ankara'da yüksekokula gidiyor. |
| a1-change | CHANGE | bir şeyi başka bir şeyle değiştirmek | She will change her plans. / Planlarını değiştirecek. |
| a1-doll | DOLL | çocukların oynadığı küçük insan figürü | She hugged her doll. / Bebeğine sarıldı. |
| a1-dollar | DOLLAR | Amerika'nın para birimi | She paid a dollar for it. / Bunun için bir dolar ödedi. |
| a1-dish | DISH | özel olarak pişirilmiş bir çeşit yemek | Manti is a famous dish. / Mantı ünlü bir yemektir. |
| a1-eighteen | EIGHTEEN | on sekiz sayısı | Selin is eighteen. / Selin on sekiz yaşında. |
| a1-eighty | EIGHTY | seksen sayısı | Our neighbour is eighty. / Komşumuz seksen yaşında. |
| a1-example | EXAMPLE | kuralı göstermek için verilen model, misal | Her poem is a lovely example. / Şiiri güzel bir örnek. |
| a1-europe | EUROPE | İtalya ve Fransa'nın bulunduğu kıta | Europe has many countries. / Avrupa'da çok ülke var. |
| a1-fourteen | FOURTEEN | on dört sayısı | There are fourteen chairs. / On dört sandalye var. |
| a1-forty | FORTY | kırk sayısı | The hall has forty seats. / Salonda kırk koltuk var. |
| a1-fifty | FIFTY | elli sayısı | It costs fifty lira. / Elli lira tutuyor. |
| a1-first | FIRST | sıralamada en başta gelen | This is my first trip. / Bu benim ilk gezim. |
| a1-feel | FEEL | kendini iyi ya da kötü hissetmek | I feel much better. / Kendimi çok daha iyi hissediyorum. |
| a1-fine | FINE | iyi, hoş; sağlıklı ya da memnun | I am fine, thank you. / İyiyim, teşekkür ederim. |
| a1-frog | FROG | gölde yaşayan, sıçrayan yeşil hayvan | The frog sat on a leaf. / Kurbağa bir yaprağın üstüne oturdu. |
| a1-france | FRANCE | başkenti Paris olan ülke | Paris is the capital of France. / Paris, Fransa'nın başkentidir. |
| a1-french | FRENCH | Fransa'da konuşulan dil | Ali is learning French. / Ali Fransızca öğreniyor. |
| a1-glasses | GLASSES | gözlük | Ali cleaned his glasses. / Ali gözlüğünü sildi. |
| a1-golf | GOLF | çimde küçük topu deliğe sokma sporu | We watched golf on TV. / Golfu televizyonda izledik. |
| a1-germany | GERMANY | başkenti Berlin olan ülke | Selin works in Germany. / Selin Almanya'da çalışıyor. |
| a1-german | GERMAN | Almanya'da konuşulan dil | Selin studies German online. / Selin internetten Almanca çalışıyor. |
| a1-half | HALF | bir bütünün iki eşit parçasından biri | Half of the students are absent. / Öğrencilerin yarısı gelmedi. |
| a1-in-front-of | IN FRONT OF | bir şeyin ön tarafında | Ali is in front of Selin. / Ali, Selin'in önünde. |
| a1-italy | ITALY | başkenti Roma olan ülke | Rome is in Italy. / Roma İtalya'dadır. |
| a1-italian | ITALIAN | İtalya'da konuşulan dil | Ali speaks Italian well. / Ali iyi İtalyanca konuşur. |
| a1-japan | JAPAN | başkenti Tokyo olan ada ülkesi | Japan has many islands. / Japonya'da çok ada var. |
| a1-japanese | JAPANESE | Japonya'da konuşulan dil | Selin reads Japanese comics. / Selin Japonca çizgi roman okur. |
| a1-laugh | LAUGH | komik bir şeye sesle gülmek | We laugh at his jokes. / Onun şakalarına gülüyoruz. |
| a1-last | LAST | sıranın en sonunda olan | She was the last in line. / Sırada sonuncu oydu. |
| a1-lady | LADY | kibar ve olgun kadın | The lady is waiting at the corner. / Hanım köşede bekliyor. |
| a1-language | LANGUAGE | bir milletin konuştuğu dil | Every language has its own words. / Her dilin kendine ait sözcükleri vardır. |
| a1-magazine | MAGAZINE | düzenli çıkan resimli yayın, dergi | This magazine has many pictures. / Bu dergide çok resim var. |
| a1-motorbike | MOTORBIKE | iki tekerlekli motorlu araç | Ali rides a motorbike. / Ali motosiklet sürüyor. |
| a1-nineteen | NINETEEN | on dokuz sayısı | The team has nineteen players. / Takımda on dokuz oyuncu var. |
| a1-ninety | NINETY | doksan sayısı | Mr Aksoy is ninety. / Bay Aksoy doksan yaşında. |
| a1-page | PAGE | kitabın bir yaprağı | A page is missing. / Bir sayfa eksik. |
| a1-pear | PEAR | sulu, aşağıya doğru sivrilen meyve | She ate a pear. / O bir armut yedi. |
| a1-plant | PLANT | toprakta yetişen, yeşil bitki | My neighbour gave me a plant. / Komşum bana bir bitki verdi. |
| a1-pepper | PEPPER | yemeğe acılık katan toz baharat | She added pepper to the sauce. / Sosa karabiber ekledi. |
| a1-pet | PET | evde bakılan hayvan | A parrot is her pet. / Papağan onun evcil hayvanı. |
| a1-piano | PIANO | tuşlara basılarak çalınan büyük çalgı | She plays the piano. / O piyano çalar. |
| a1-postcard | POSTCARD | postayla gönderilen resimli kart | I sent a postcard from Izmir. / İzmir'den bir kartpostal gönderdim. |
| a1-person | PERSON | tek bir insan | Every person needs friends. / Her insanın arkadaşlara ihtiyacı vardır. |
| a1-ruler | RULER | çizgi çizmek ve ölçmek için düz araç | Ali has a plastic ruler. / Ali'nin plastik bir cetveli var. |
| a1-repeat | REPEAT | aynı şeyi bir daha söylemek | Can you repeat that, Ali? / Bunu tekrar eder misin, Ali? |
| a1-russia | RUSSIA | başkenti Moskova olan dünyanın en büyük ülkesi | Selin has friends in Russia. / Selin'in Rusya'da arkadaşları var. |
| a1-russian | RUSSIAN | Rusya'da konuşulan dil | Ali's neighbour speaks Russian. / Ali'nin komşusu Rusça konuşur. |
| a1-shorts | SHORTS | yazın giyilen, dizin üstünde biten pantolon | Ali wears shorts for jogging. / Ali koşu için şort giyer. |
| a1-sell | SELL | malı para karşılığında başkasına vermek | They sell lemons at the stall. / Tezgâhta limon satıyorlar. |
| a1-sixty | SIXTY | altmış sayısı | She waited sixty seconds. / Altmış saniye bekledi. |
| a1-seventeen | SEVENTEEN | on yedi sayısı | There are seventeen eggs in the basket. / Sepette on yedi yumurta var. |
| a1-seventy | SEVENTY | yetmiş sayısı | The report has seventy pages. / Raporun yetmiş sayfası var. |
| a1-south | SOUTH | kuzeyin tersi yön | Antalya is in the south. / Antalya güneyde. |
| a1-science | SCIENCE | fizik, kimya ve biyoloji gibi doğa bilimleri | Ali enjoys science. / Ali fen bilgisinden hoşlanır. |
| a1-second | SECOND | dakikanın altmışta biri | I will be there in a second. / Bir saniye içinde orada olurum. |
| a1-spell | SPELL | bir sözcüğü harf harf söylemek | Can you spell your surname? / Soyadını harf harf söyler misin? |
| a1-star | STAR | geceleri gökyüzünde parlayan ışık noktası | A star is shining. / Bir yıldız parlıyor. |
| a1-same | SAME | birbirinin aynısı olan | Ali and Selin got the same mark. / Ali ile Selin aynı notu aldı. |
| a1-say | SAY | bir şeyi sözle ifade etmek | What did he say? / Ne dedi? |
| a1-spain | SPAIN | başkenti Madrid olan ülke | Madrid is the capital of Spain. / Madrid, İspanya'nın başkentidir. |
| a1-spanish | SPANISH | İspanya'da konuşulan dil | Our guide teaches Spanish. / Rehberimiz İspanyolca öğretiyor. |
| a1-show | SHOW | bir şeyi başkasına göstermek | Can you show me the way? / Bana yolu gösterebilir misin? |
| a1-stay | STAY | bir yerde kalmak | Stay with me. / Benimle kal. |
| a1-shopping | SHOPPING | mağazalardan bir şeyler satın alma işi | Ali hates shopping. / Ali alışverişten nefret eder. |
| a1-thirteen | THIRTEEN | on üç sayısı | There are thirteen candles on the shelf. / Rafta on üç mum var. |
| a1-trousers | TROUSERS | iki bacağı da örten uzun giysi | Ali bought trousers. / Ali pantolon aldı. |
| a1-toy | TOY | çocukların oynadığı eşya | This toy is for children. / Bu oyuncak çocuklar için. |
| a1-television | TELEVISION | yayınların izlendiği ekranlı cihaz | The television is broken. / Televizyon bozuk. |
| a1-telephone | TELEPHONE | uzaktaki biriyle konuşmaya yarayan aygıt | The telephone is ringing. / Telefon çalıyor. |
| a1-third | THIRD | ikinciden sonra gelen | It is on the third shelf. / Üçüncü rafta. |
| a1-thousand | THOUSAND | yüzün on katı | A thousand fans were at the match. / Maçta bin taraftar vardı. |
| a1-toothache | TOOTHACHE | dişte hissedilen ağrı | Ali has a toothache. / Ali'nin dişi ağrıyor. |
| a1-turkey | TURKEY | başkenti Ankara olan ülke | Ankara is the capital of Turkey. / Ankara, Türkiye'nin başkentidir. |
| a1-turkish | TURKISH | Türkiye'de konuşulan dil | Selin writes in Turkish. / Selin Türkçe yazar. |
| a1-west | WEST | güneşin battığı yön | Izmir is in the west. / İzmir batıdadır. |
| a1-website | WEBSITE | internette bir kuruluşun sayfaları | Ali made a website. / Ali bir web sitesi yaptı. |
