# FB-A1-1 — fill-the-blank rewrite batch (A1, 149 `rewrite` cards)

Selection: `FILL-BLANK-REVIEW.csv`, level A1, action `rewrite`, ordered by failing-rule count (most first), then id; first 150 minus `a1-bedroom` (deferred until the leak-exempt list is applied). Only `example_en` / `example_tr` change. `content/cards` is **not** edited yet.

Rules for this batch: American spelling/words; Turkish is a faithful translation of the English (no words added to satisfy the scanner); leak check uses `content/leak-exempt.txt`.

Checks (scratch copy with all 149 applied, new tools): `qa.py` 0 errors / 0 warnings; `fill_blank_scan.py` 137/149 clean. The 12 remaining are **R6 on antonym/descriptive clues** (`zor olmayan`, `bulut rengi`, `köfteli sandviç`, …) where a faithful translation (`kolaydı`, `gri`, `hamburger`) cannot share a stem with the clue; accepted, not worked around.

| id | old sentence | new sentence | new translation | note |
|---|---|---|---|---|
| a1-bath | I have a bath. | Mara fills the tub and takes a relaxing bath. | Mara küveti doldurur ve rahatlatıcı bir banyo yapar. |  |
| a1-neck | My neck hurts. | She wears a gold chain around her neck. | Boynunda altın bir zincir takıyor. | rewritten in review round 2 |
| a1-back | My back hurts. | The backpack straps hurt her back. | Sırt çantasının askıları sırtını acıtıyor. |  |
| a1-bike | I have a bike. | Tom rides his bike to the bakery. | Tom bisikletiyle fırına gider. |  |
| a1-boring | This program is boring. | The trip was so boring that I almost fell asleep. | Gezi o kadar sıkıcıydı ki neredeyse uyuyakalacaktım. | rewritten in review round 2 |
| a1-brother | I have a brother. | My brother plays chess with his cousins. | Erkek kardeşim kuzenleriyle satranç oynar. |  |
| a1-brown | She has brown eyes. | A deer has soft brown fur. | Geyiğin yumuşak kahverengi tüyleri var. | rewritten in review round 2; scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-cheap | These shoes are cheap. | These pants are cheap, just a few dollars. | Bu pantolon ucuz, sadece birkaç dolar. | rewritten in review round 2; scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-clothes | My clothes are wet. | She hangs her wet clothes on the line. | Islak giysilerini ipe asıyor. |  |
| a1-cook | Can you cook? | My mom likes to cook, and her pies are tasty. | Annem pişirmeyi sever ve turtaları lezzetli. |  |
| a1-dark | The tunnel is dark. | It is dark and I cannot see anything. | Karanlık ve hiçbir şey göremiyorum. | rewritten in review round 2; scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-difficult | This puzzle is difficult. | This puzzle is too difficult for me. | Bu bulmaca benim için fazla zor. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-dirty | Your shoes are dirty. | His socks are dirty from the muddy path. | Çorapları çamurlu patikadan dolayı kirli. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-drink | I drink ayran. | Mert fills his mug with cocoa and starts to drink. | Mert kupasını kakaoyla doldurup içmeye başlıyor. |  |
| a1-easy | The test is easy. | That quiz was easy, and Selin finished in minutes. | Sınav kolaydı ve Selin dakikalar içinde bitirdi. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-foot | My foot hurts. | Anna stepped on a sharp shell with her bare foot. | Çıplak ayağıyla keskin bir deniz kabuğuna bastı. |  |
| a1-free | This concert is free. | Kids can enter the gallery for free. | Çocuklar galeriye parasız girebilir. |  |
| a1-funny | The clown is funny. | Ali's jokes are funny, so everybody giggles. | Ali'nin fıkraları komik, herkesi güldürüyor. |  |
| a1-grey | The pigeon is gray. | The wolf has thick gray fur. | Kurdun kalın gri tüyleri var. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-hair | She has curly hair. | The barber trims his hair and beard. | Berber saç ve sakalını kesiyor. |  |
| a1-hamburger | He ordered a hamburger. | He ordered a hamburger with fries and ketchup. | Patates kızartması ve ketçaplı bir hamburger sipariş etti. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-hard | The ground is hard. | The cushion is soft, but the bench is hard. | Minder yumuşak ama bank sert. |  |
| a1-head | My head hurts. | A helmet protects your head in a crash. | Kask, kazada başını korur. |  |
| a1-headache | I have a headache. | Loud noise gives me a terrible headache. | Yüksek ses bana korkunç bir baş ağrısı veriyor. |  |
| a1-holiday | We are on holiday. | We spent our Easter holiday at the seaside. | Paskalya tatilimizi deniz kenarında geçirdik. |  |
| a1-idea | I have an idea! | Selin thought of a clever idea for the project. | Selin proje için zekice bir fikir buldu. |  |
| a1-knee | My knee hurts. | Ali fell from his skateboard and hurt his knee. | Ali kaykayından düştü ve dizini yaraladı. |  |
| a1-know | I do not know. | I do not know the poem by heart yet. | Şiiri henüz ezbere bilmiyorum. |  |
| a1-light | This suitcase is light. | A feather is very light and floats gently. | Bir tüy çok hafiftir ve nazikçe süzülür. | rewritten in review round 2; scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-married | My neighbor is married. | Ali and Selin got married in a lovely church. | Ali ve Selin güzel bir kilisede evlendi. |  |
| a1-mouth | Cover your mouth. | He covered his mouth as he yawned. | Esnerken ağzını eliyle kapattı. |  |
| a1-move | Do not move! | Be still and do not move; the wasp is on you. | Hareketsiz dur ve kıpırdama; yaban arısı üzerinde. |  |
| a1-name | My name is Mert. | Selin glued a name tag to her lunchbox. | Selin beslenme çantasına bir isim etiketi yapıştırdı. |  |
| a1-near | Is it near? | The bakery is near the mosque. | Fırın camiye yakın. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-never | He never shouts. | Ali never eats onions, but he likes tomatoes. | Ali hiçbir zaman soğan yemez ama domatesi sever. |  |
| a1-office | My office is bright. | The manager has a meeting in her office. | Müdürün bürosunda bir toplantı var. |  |
| a1-okay | Are you okay? | Selin fell, but she is okay. | Selin düştü ama sorunu yok. |  |
| a1-question | I have a question. | The speaker answered Ali's question clearly. | Konuşmacı Ali'nin sorusunu net şekilde yanıtladı. |  |
| a1-read | I read stories. | Selin loves to read novels on the ferry. | Selin vapurda roman okumayı sever. |  |
| a1-salt | Pass the salt. | Fries taste better with a pinch of salt. | Patates kızartması bir tutam tuz ile daha lezzetli. |  |
| a1-skirt | Her skirt is lovely. | She wore a pleated skirt and a blouse. | Pliseli bir etek ve bluz giydi. |  |
| a1-slow | The lift is slow. | The ferry is slow, but the trip is relaxing. | Vapur yavaş ama yolculuk rahatlatıcı. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-start | Let us start! | The fireworks will start at midnight. | Havai fişekler gece yarısı başlayacak. |  |
| a1-station | The station is closed. | The station is crowded with travelers. | İstasyon yolcularla dolu. | scan R6 (clue/translation stem: antonym or descriptive clue) |
| a1-swim | I can swim. | Ducks can swim in the pond. | Ördekler gölette yüzebilir. |  |
| a1-tooth | My tooth hurts. | The wobbly tooth finally came out. | Oynayan diş sonunda düştü. |  |
| a1-understand | I do not understand. | I do not understand these instructions. | Bu talimatları anlayamıyorum. |  |
| a1-useful | This app is useful. | A calculator is useful for big numbers. | Hesap makinesi büyük sayılar için işe yarar. | rewritten in review round 2 |
| a1-wash | I wash my hands. | I wash the dishes while Selin dries them. | Ben bulaşıkları yıkarken Selin onları kurular. |  |
| a1-address | Is this your address? | The mailman lost the address on the envelope. | Postacı zarftaki adresi kaybetti. |  |
| a1-again | It is snowing again. | Selin missed the ferry again. | Selin vapuru bir kez daha kaçırdı. |  |
| a1-airport | The airport is huge. | Passengers form lines for flights at the airport. | Yolcular havalimanında uçuşlar için sıra oluşturuyor. |  |
| a1-also | She also speaks Arabic. | Ali plays the drums and also writes songs. | Ali davul çalar, bunun yanında şarkı da yazar. |  |
| a1-always | She always smiles. | The baker always wakes at dawn. | Fırıncı her zaman şafakta kalkar. |  |
| a1-america | She studied in America. | Hollywood films are made in America. | Hollywood filmleri Amerika'da çekilir. |  |
| a1-american | Grace is American. | Grace is American, but she lives in Paris. | Grace Amerikalı ama Paris'te yaşıyor. |  |
| a1-apple | This apple is crunchy. | Mom baked an apple pie with cinnamon. | Annem tarçınlı elmalı turta pişirdi. |  |
| a1-baby | The baby is sleeping. | The newborn baby sleeps in a cradle. | Yeni doğan bebek beşikte uyuyor. |  |
| a1-banana | She eats a banana. | Selin peeled a banana and ate it. | Selin bir muz soyup yedi. |  |
| a1-bathroom | The bathroom is tiny. | Shampoo and towels are in the bathroom. | Şampuan ve havlular banyoda. |  |
| a1-beach | The beach is crowded. | Children build sandcastles on the beach. | Çocuklar kumsalda kumdan kaleler yapıyor. |  |
| a1-bear | The bear is huge. | The bear caught a salmon in the stream. | Ayı derede bir somon yakaladı. |  |
| a1-beautiful | She is beautiful. | The sunset at the bay was beautiful. | Koydaki gün batımı çok güzeldi. |  |
| a1-beer | He ordered a beer. | Brewers brew beer from barley and hops. | Bira üreticileri birayı arpa ve şerbetçiotundan yapar. |  |
| a1-birthday | Is it your birthday? | Selin blew out the candles on her birthday. | Selin doğum gününde mumları üfledi. |  |
| a1-biscuit | This biscuit is crunchy. | She dunked a biscuit in her cocoa. | Kakaosuna bir bisküvi batırdı. |  |
| a1-blue | The bucket is blue. | Ali painted the fence a bright blue. | Ali çiti parlak maviye boyadı. |  |
| a1-book | This book is thick. | Selin turned the pages of her book slowly. | Selin kitabının sayfalarını yavaşça çevirdi. |  |
| a1-boots | Her boots are muddy. | Farmers trudge through the puddles in rubber boots. | Çiftçiler lastik çizmelerle su birikintilerinde yürüyor. |  |
| a1-breakfast | Breakfast is waiting for us. | Pancakes and cereal are popular for breakfast. | Krep ve gevrek sabah yemeği olarak popüler. |  |
| a1-bridge | The bridge is closed. | Ships sail beneath the suspension bridge. | Gemiler asma köprünün altından geçer. |  |
| a1-bring | Bring your friends. | Mert will bring snacks to the barbecue. | Mert barbekü partisine atıştırmalık getirecek. |  |
| a1-british | Tom is British. | Lucy is British and lives in London. | Lucy İngiliz ve Londra'da yaşıyor. |  |
| a1-butter | The butter is soft. | Melt the butter in a frying pan. | Tereyağını tavada eritin. |  |
| a1-cafe | This cafe is cozy. | Selin met Ali at the cafe for an espresso. | Selin espresso için Ali ile kafede buluştu. |  |
| a1-camera | Her camera is silver. | Tourists photographed the castle with a camera. | Turistler kaleyi fotoğraf makinesiyle çekti. |  |
| a1-carpet | The carpet is wet. | Mert vacuums the carpet on Saturdays. | Mert cumartesileri halıyı süpürür. |  |
| a1-catch | Catch the rope! | Ali, throw the frisbee and I will catch it. | Ali, frizbiyi at, ben yakalarım. |  |
| a1-chair | This chair is comfortable. | Grandpa dozed in the rocking chair. | Dedem sallanan sandalyede şekerleme yaptı. |  |
| a1-cheese | Is there any cheese? | Ali grated the cheese onto his omelet. | Ali omletinin üzerine peynir rendeledi. |  |
| a1-chicken | We cooked chicken. | Grandma roasted a chicken with potatoes. | Büyükannem patateslerle birlikte tavuk fırınladı. |  |
| a1-child | This child is shy. | A lost child cried at the entrance. | Kayıp bir çocuk girişte ağladı. |  |
| a1-chocolate | He likes chocolate. | Ali bought a bar of chocolate at the store. | Ali mağazadan bir tablet çikolata aldı. |  |
| a1-classmate | Selin is my classmate. | Ali borrowed a calculator from his classmate during the test. | Ali sınav sırasında sınıf arkadaşından hesap makinesi ödünç aldı. |  |
| a1-clean | Your hands are clean. | Mert scrubbed the tiles until they were clean. | Mert fayansları temiz olana kadar ovdu. |  |
| a1-coffee | Is the coffee strong? | The barista made a strong coffee with foam. | Barista köpüklü sert bir kahve yaptı. |  |
| a1-cold | My feet are cold. | The Arctic is freezing cold, so explorers pack parkas. | Kuzey Kutbu dondurucu soğuk, bu yüzden kaşifler parka alır. |  |
| a1-colour | Ali likes this color. | A chameleon changes its color to hide. | Bukalemun saklanmak için renk değiştirir. |  |
| a1-computer | The computer is broken. | Selin typed her essay on the computer. | Selin kompozisyonunu bilgisayarda yazdı. |  |
| a1-daughter | Our daughter is a vet. | Mr. Kaya proudly watched his daughter graduate. | Bay Kaya kızının mezuniyetini gururla izledi. |  |
| a1-december | It snows in December. | Santa Claus visits children in December. | Noel Baba Aralık'ta çocukları ziyaret eder. |  |
| a1-delicious | The lasagne is delicious. | The baklava was crispy and delicious. | Baklava çıtır çıtır ve lezzetliydi. |  |
| a1-dictionary | Her dictionary is thick. | I checked the meaning in a dictionary. | Anlamına sözlükten baktım. |  |
| a1-dinner | Dinner is waiting! | We lit candles for a romantic dinner. | Romantik bir akşam yemeği için mum yaktık. |  |
| a1-doll | She hugged her doll. | Selin sewed tiny pajamas for her doll. | Selin bebeği için minik pijamalar dikti. |  |
| a1-door | Knock on the door. | Someone knocked on the front door at midnight. | Gece yarısı ön kapı çalındı. |  |
| a1-downstairs | The children are downstairs. | Grandma sleeps downstairs, since the steps are steep. | Büyükannem alt katta uyuyor çünkü merdivenler dik. |  |
| a1-eighteen | Selin is eighteen. | Selin passed her driving test at eighteen. | Selin on sekiz yaşında direksiyon sınavını geçti. |  |
| a1-eighty | Our neighbor is eighty. | Grandpa Ali just turned eighty, and he still gardens. | Ali dede seksen yaşına girdi ama hâlâ bahçeyle uğraşıyor. |  |
| a1-eleven | He is eleven. | The church bell rang eleven times. | Kilise çanı on bir kez çaldı. |  |
| a1-email | Check your email. | Mert wrote an email to his boss. | Mert patronuna elektronik posta yazdı. |  |
| a1-empty | The shelves are empty. | The jar on the shelf is empty. | Raftaki kavanoz boş. |  |
| a1-english | She speaks English. | Our tour guide speaks fluent English. | Rehberimiz akıcı İngilizce konuşuyor. |  |
| a1-enjoy | They enjoy swimming. | Kids enjoy riding roller coasters at the fair. | Çocuklar lunaparkta hız trenine binmekten keyif alır. |  |
| a1-evening | See you this evening. | Bats emerge in the evening to hunt. | Yarasalar avlanmak için akşam çıkar. |  |
| a1-exam | I passed the exam. | Mert revised for the final exam with his notes. | Mert notlarıyla final sınavına çalıştı. |  |
| a1-farm | We have a farm. | The tractor is parked beside the barn on the farm. | Traktör çiftlikteki ahırın yanında duruyor. |  |
| a1-february | We ski in February. | Sweethearts swap Valentine cards in February. | Sevgililer şubatta Sevgililer Günü kartlarını birbirine verir. |  |
| a1-feel | I feel better. | Do you feel dizzy or sick, Selin? | Selin, kendini sersem veya hasta mı hissediyorsun? |  |
| a1-finger | I hurt my finger. | Mert pricked his finger with a needle. | Mert iğneyle parmağını batırdı. |  |
| a1-five | We have five apples. | A starfish has five arms. | Denizyıldızının beş kolu var. |  |
| a1-floor | The floor is wet. | Selin dropped her keys on the floor. | Selin anahtarlarını yere düşürdü. |  |
| a1-follow | Follow me, Ali. | Ducklings follow the leader in a line. | Yavru ördekler lideri sırayla takip eder. |  |
| a1-forget | Do not forget your passport. | Selin, do not forget to lock the gate. | Selin, kapıyı kilitlemeyi aklından çıkarma. |  |
| a1-fork | I dropped my fork. | Mert ate the pancakes with a fork. | Mert krepleri çatalla yedi. |  |
| a1-fourteen | There are fourteen chairs. | Fourteen players stood on the field. | Sahada on dört oyuncu duruyordu. |  |
| a1-fridge | The fridge is noisy. | Mert stored the leftovers in the fridge. | Mert artık yemekleri buzdolabına kaldırdı. |  |
| a1-friendly | Our neighbors are friendly. | The shopkeeper greets us with a friendly wave. | Dükkâncı bizi cana yakın bir el sallamayla karşılıyor. |  |
| a1-game | This game is exciting. | Mert and Ali finished the chess game quickly. | Mert ve Ali satranç oyununu hızlıca bitirdi. |  |
| a1-garage | Our garage is tidy. | Mert keeps his tools in the garage. | Mert aletlerini garajda tutuyor. |  |
| a1-girl | The girl is singing. | The girl in the raincoat jumped in puddles. | Yağmurluklu kız çocuğu su birikintilerine atladı. |  |
| a1-give | Give me the remote. | I will give Selin a surprise. | Selin'e bir sürpriz vereceğim. |  |
| a1-glass | This glass is broken. | Mert accidentally broke a glass and swept the pieces. | Mert yanlışlıkla bir bardak kırdı ve parçaları süpürdü. |  |
| a1-glove | I lost a glove. | The goalkeeper wore a padded glove to save penalties. | Kaleci penaltıları kurtarmak için dolgulu bir eldiven taktı. |  |
| a1-goodbye | She waved goodbye. | Selin hugged her grandpa goodbye at the harbor. | Selin büyükbabasına limanda sarılıp veda etti. |  |
| a1-grandmother | My grandmother is gentle. | Selin's grandmother knits woolly scarves for us. | Selin'in büyükannesi bize yünlü atkılar örer. |  |
| a1-grandparents | My grandparents are retired. | We spend Eid with my grandparents. | Bayramı büyükanne ve büyükbabamla geçiriyoruz. |  |
| a1-grape | This grape is sour. | A raisin is a dried grape. | Kuru üzüm, kurutulmuş üzümdür. |  |
| a1-grass | The grass is wet. | Mert mows the grass on Sundays. | Mert pazar günleri çimenleri biçer. |  |
| a1-great | The concert was great. | The chef's lasagna was absolutely great. | Şefin lazanyası kesinlikle harikaydı. |  |
| a1-green | The leaves are green. | Frogs and cucumbers are mostly green. | Kurbağalar ve salatalıklar çoğunlukla yeşildir. |  |
| a1-guitar | He plays the guitar. | Mert strummed his guitar by the campfire. | Mert kamp ateşinin başında gitar çaldı. |  |
| a1-hand | Raise your hand. | Ali held my hand tightly and grinned. | Ali elimi sıkıca tuttu ve sırıttı. |  |
| a1-happy | You seem happy! | Mert looked happy with his results. | Mert sonuçlarından mutlu görünüyordu. |  |
| a1-healthy | We are healthy. | Salmon and spinach are healthy choices. | Somon ve ıspanak sağlıklı seçimlerdir. |  |
| a1-hear | I cannot hear you. | I cannot hear you in this noisy hall. | Bu gürültülü salonda seni duyamıyorum. |  |
| a1-heavy | This suitcase is heavy. | The closet is too heavy to lift alone. | Dolap tek başına kaldırmak için fazla ağır. |  |
| a1-hello | Hello! I am Ali. | Selin smiled and said hello to the neighbors. | Selin gülümsedi ve komşulara merhaba dedi. |  |
| a1-help | Can you help me? | Firefighters came quickly to help the villagers. | İtfaiyeciler köylülere yardım etmek için hızla geldi. |  |
| a1-high | The ceiling is high. | The eagle flew high above the cliffs. | Kartal kayalıkların çok yukarısında uçtu. |  |
| a1-hobby | Reading is my hobby. | Collecting stamps is Mert's hobby. | Pul biriktirmek Mert'in hobisi. |  |
| a1-home | This is my home. | Ali finished his shift and walked home. | Ali vardiyasını bitirdi ve eve yürüdü. |  |
| a1-homework | Do you have homework? | Mom checked Ali's homework and signed it. | Annem Ali'nin ödevini kontrol edip imzaladı. |  |
| a1-hour | It takes an hour. | The ferry trip took almost an hour. | Vapur yolculuğu neredeyse bir saat sürdü. |  |
| a1-hungry | The children are hungry. | The hikers were so hungry that they ate quickly. | Yürüyüşçüler o kadar açtı ki hızla yediler. |  |
| a1-inside | Is she inside? | Thunder is loud, so the hens hide inside. | Gök gürültüsü çok, tavuklar içeride saklanıyor. |  |
| a1-interesting | This article is interesting. | The gallery has an interesting dinosaur exhibit. | Galerideki dinozor sergisi ilgimi çekiyor. |  |
| a1-iron | Mum has an iron. | The tailor pressed the wrinkles with a steam iron. | Terzi buruşukları buharlı ütüyle düzeltti. |  |
| a1-italy | Rome is in Italy. | Rome and Venice are cities in Italy. | Roma ve Venedik İtalya'daki şehirlerdir. |  |

## Patch-ready CSV (id, example_en, example_tr)

```csv
id,example_en,example_tr
a1-bath,Mara fills the tub and takes a relaxing bath.,Mara küveti doldurur ve rahatlatıcı bir banyo yapar.
a1-neck,She wears a gold chain around her neck.,Boynunda altın bir zincir takıyor.
a1-back,The backpack straps hurt her back.,Sırt çantasının askıları sırtını acıtıyor.
a1-bike,Tom rides his bike to the bakery.,Tom bisikletiyle fırına gider.
a1-boring,The trip was so boring that I almost fell asleep.,Gezi o kadar sıkıcıydı ki neredeyse uyuyakalacaktım.
a1-brother,My brother plays chess with his cousins.,Erkek kardeşim kuzenleriyle satranç oynar.
a1-brown,A deer has soft brown fur.,Geyiğin yumuşak kahverengi tüyleri var.
a1-cheap,"These pants are cheap, just a few dollars.","Bu pantolon ucuz, sadece birkaç dolar."
a1-clothes,She hangs her wet clothes on the line.,Islak giysilerini ipe asıyor.
a1-cook,"My mom likes to cook, and her pies are tasty.",Annem pişirmeyi sever ve turtaları lezzetli.
a1-dark,It is dark and I cannot see anything.,Karanlık ve hiçbir şey göremiyorum.
a1-difficult,This puzzle is too difficult for me.,Bu bulmaca benim için fazla zor.
a1-dirty,His socks are dirty from the muddy path.,Çorapları çamurlu patikadan dolayı kirli.
a1-drink,Mert fills his mug with cocoa and starts to drink.,Mert kupasını kakaoyla doldurup içmeye başlıyor.
a1-easy,"That quiz was easy, and Selin finished in minutes.",Sınav kolaydı ve Selin dakikalar içinde bitirdi.
a1-foot,Anna stepped on a sharp shell with her bare foot.,Çıplak ayağıyla keskin bir deniz kabuğuna bastı.
a1-free,Kids can enter the gallery for free.,Çocuklar galeriye parasız girebilir.
a1-funny,"Ali's jokes are funny, so everybody giggles.","Ali'nin fıkraları komik, herkesi güldürüyor."
a1-grey,The wolf has thick gray fur.,Kurdun kalın gri tüyleri var.
a1-hair,The barber trims his hair and beard.,Berber saç ve sakalını kesiyor.
a1-hamburger,He ordered a hamburger with fries and ketchup.,Patates kızartması ve ketçaplı bir hamburger sipariş etti.
a1-hard,"The cushion is soft, but the bench is hard.",Minder yumuşak ama bank sert.
a1-head,A helmet protects your head in a crash.,"Kask, kazada başını korur."
a1-headache,Loud noise gives me a terrible headache.,Yüksek ses bana korkunç bir baş ağrısı veriyor.
a1-holiday,We spent our Easter holiday at the seaside.,Paskalya tatilimizi deniz kenarında geçirdik.
a1-idea,Selin thought of a clever idea for the project.,Selin proje için zekice bir fikir buldu.
a1-knee,Ali fell from his skateboard and hurt his knee.,Ali kaykayından düştü ve dizini yaraladı.
a1-know,I do not know the poem by heart yet.,Şiiri henüz ezbere bilmiyorum.
a1-light,A feather is very light and floats gently.,Bir tüy çok hafiftir ve nazikçe süzülür.
a1-married,Ali and Selin got married in a lovely church.,Ali ve Selin güzel bir kilisede evlendi.
a1-mouth,He covered his mouth as he yawned.,Esnerken ağzını eliyle kapattı.
a1-move,Be still and do not move; the wasp is on you.,Hareketsiz dur ve kıpırdama; yaban arısı üzerinde.
a1-name,Selin glued a name tag to her lunchbox.,Selin beslenme çantasına bir isim etiketi yapıştırdı.
a1-near,The bakery is near the mosque.,Fırın camiye yakın.
a1-never,"Ali never eats onions, but he likes tomatoes.",Ali hiçbir zaman soğan yemez ama domatesi sever.
a1-office,The manager has a meeting in her office.,Müdürün bürosunda bir toplantı var.
a1-okay,"Selin fell, but she is okay.",Selin düştü ama sorunu yok.
a1-question,The speaker answered Ali's question clearly.,Konuşmacı Ali'nin sorusunu net şekilde yanıtladı.
a1-read,Selin loves to read novels on the ferry.,Selin vapurda roman okumayı sever.
a1-salt,Fries taste better with a pinch of salt.,Patates kızartması bir tutam tuz ile daha lezzetli.
a1-skirt,She wore a pleated skirt and a blouse.,Pliseli bir etek ve bluz giydi.
a1-slow,"The ferry is slow, but the trip is relaxing.",Vapur yavaş ama yolculuk rahatlatıcı.
a1-start,The fireworks will start at midnight.,Havai fişekler gece yarısı başlayacak.
a1-station,The station is crowded with travelers.,İstasyon yolcularla dolu.
a1-swim,Ducks can swim in the pond.,Ördekler gölette yüzebilir.
a1-tooth,The wobbly tooth finally came out.,Oynayan diş sonunda düştü.
a1-understand,I do not understand these instructions.,Bu talimatları anlayamıyorum.
a1-useful,A calculator is useful for big numbers.,Hesap makinesi büyük sayılar için işe yarar.
a1-wash,I wash the dishes while Selin dries them.,Ben bulaşıkları yıkarken Selin onları kurular.
a1-address,The mailman lost the address on the envelope.,Postacı zarftaki adresi kaybetti.
a1-again,Selin missed the ferry again.,Selin vapuru bir kez daha kaçırdı.
a1-airport,Passengers form lines for flights at the airport.,Yolcular havalimanında uçuşlar için sıra oluşturuyor.
a1-also,Ali plays the drums and also writes songs.,"Ali davul çalar, bunun yanında şarkı da yazar."
a1-always,The baker always wakes at dawn.,Fırıncı her zaman şafakta kalkar.
a1-america,Hollywood films are made in America.,Hollywood filmleri Amerika'da çekilir.
a1-american,"Grace is American, but she lives in Paris.",Grace Amerikalı ama Paris'te yaşıyor.
a1-apple,Mom baked an apple pie with cinnamon.,Annem tarçınlı elmalı turta pişirdi.
a1-baby,The newborn baby sleeps in a cradle.,Yeni doğan bebek beşikte uyuyor.
a1-banana,Selin peeled a banana and ate it.,Selin bir muz soyup yedi.
a1-bathroom,Shampoo and towels are in the bathroom.,Şampuan ve havlular banyoda.
a1-beach,Children build sandcastles on the beach.,Çocuklar kumsalda kumdan kaleler yapıyor.
a1-bear,The bear caught a salmon in the stream.,Ayı derede bir somon yakaladı.
a1-beautiful,The sunset at the bay was beautiful.,Koydaki gün batımı çok güzeldi.
a1-beer,Brewers brew beer from barley and hops.,Bira üreticileri birayı arpa ve şerbetçiotundan yapar.
a1-birthday,Selin blew out the candles on her birthday.,Selin doğum gününde mumları üfledi.
a1-biscuit,She dunked a biscuit in her cocoa.,Kakaosuna bir bisküvi batırdı.
a1-blue,Ali painted the fence a bright blue.,Ali çiti parlak maviye boyadı.
a1-book,Selin turned the pages of her book slowly.,Selin kitabının sayfalarını yavaşça çevirdi.
a1-boots,Farmers trudge through the puddles in rubber boots.,Çiftçiler lastik çizmelerle su birikintilerinde yürüyor.
a1-breakfast,Pancakes and cereal are popular for breakfast.,Krep ve gevrek sabah yemeği olarak popüler.
a1-bridge,Ships sail beneath the suspension bridge.,Gemiler asma köprünün altından geçer.
a1-bring,Mert will bring snacks to the barbecue.,Mert barbekü partisine atıştırmalık getirecek.
a1-british,Lucy is British and lives in London.,Lucy İngiliz ve Londra'da yaşıyor.
a1-butter,Melt the butter in a frying pan.,Tereyağını tavada eritin.
a1-cafe,Selin met Ali at the cafe for an espresso.,Selin espresso için Ali ile kafede buluştu.
a1-camera,Tourists photographed the castle with a camera.,Turistler kaleyi fotoğraf makinesiyle çekti.
a1-carpet,Mert vacuums the carpet on Saturdays.,Mert cumartesileri halıyı süpürür.
a1-catch,"Ali, throw the frisbee and I will catch it.","Ali, frizbiyi at, ben yakalarım."
a1-chair,Grandpa dozed in the rocking chair.,Dedem sallanan sandalyede şekerleme yaptı.
a1-cheese,Ali grated the cheese onto his omelet.,Ali omletinin üzerine peynir rendeledi.
a1-chicken,Grandma roasted a chicken with potatoes.,Büyükannem patateslerle birlikte tavuk fırınladı.
a1-child,A lost child cried at the entrance.,Kayıp bir çocuk girişte ağladı.
a1-chocolate,Ali bought a bar of chocolate at the store.,Ali mağazadan bir tablet çikolata aldı.
a1-classmate,Ali borrowed a calculator from his classmate during the test.,Ali sınav sırasında sınıf arkadaşından hesap makinesi ödünç aldı.
a1-clean,Mert scrubbed the tiles until they were clean.,Mert fayansları temiz olana kadar ovdu.
a1-coffee,The barista made a strong coffee with foam.,Barista köpüklü sert bir kahve yaptı.
a1-cold,"The Arctic is freezing cold, so explorers pack parkas.","Kuzey Kutbu dondurucu soğuk, bu yüzden kaşifler parka alır."
a1-colour,A chameleon changes its color to hide.,Bukalemun saklanmak için renk değiştirir.
a1-computer,Selin typed her essay on the computer.,Selin kompozisyonunu bilgisayarda yazdı.
a1-daughter,Mr. Kaya proudly watched his daughter graduate.,Bay Kaya kızının mezuniyetini gururla izledi.
a1-december,Santa Claus visits children in December.,Noel Baba Aralık'ta çocukları ziyaret eder.
a1-delicious,The baklava was crispy and delicious.,Baklava çıtır çıtır ve lezzetliydi.
a1-dictionary,I checked the meaning in a dictionary.,Anlamına sözlükten baktım.
a1-dinner,We lit candles for a romantic dinner.,Romantik bir akşam yemeği için mum yaktık.
a1-doll,Selin sewed tiny pajamas for her doll.,Selin bebeği için minik pijamalar dikti.
a1-door,Someone knocked on the front door at midnight.,Gece yarısı ön kapı çalındı.
a1-downstairs,"Grandma sleeps downstairs, since the steps are steep.",Büyükannem alt katta uyuyor çünkü merdivenler dik.
a1-eighteen,Selin passed her driving test at eighteen.,Selin on sekiz yaşında direksiyon sınavını geçti.
a1-eighty,"Grandpa Ali just turned eighty, and he still gardens.",Ali dede seksen yaşına girdi ama hâlâ bahçeyle uğraşıyor.
a1-eleven,The church bell rang eleven times.,Kilise çanı on bir kez çaldı.
a1-email,Mert wrote an email to his boss.,Mert patronuna elektronik posta yazdı.
a1-empty,The jar on the shelf is empty.,Raftaki kavanoz boş.
a1-english,Our tour guide speaks fluent English.,Rehberimiz akıcı İngilizce konuşuyor.
a1-enjoy,Kids enjoy riding roller coasters at the fair.,Çocuklar lunaparkta hız trenine binmekten keyif alır.
a1-evening,Bats emerge in the evening to hunt.,Yarasalar avlanmak için akşam çıkar.
a1-exam,Mert revised for the final exam with his notes.,Mert notlarıyla final sınavına çalıştı.
a1-farm,The tractor is parked beside the barn on the farm.,Traktör çiftlikteki ahırın yanında duruyor.
a1-february,Sweethearts swap Valentine cards in February.,Sevgililer şubatta Sevgililer Günü kartlarını birbirine verir.
a1-feel,"Do you feel dizzy or sick, Selin?","Selin, kendini sersem veya hasta mı hissediyorsun?"
a1-finger,Mert pricked his finger with a needle.,Mert iğneyle parmağını batırdı.
a1-five,A starfish has five arms.,Denizyıldızının beş kolu var.
a1-floor,Selin dropped her keys on the floor.,Selin anahtarlarını yere düşürdü.
a1-follow,Ducklings follow the leader in a line.,Yavru ördekler lideri sırayla takip eder.
a1-forget,"Selin, do not forget to lock the gate.","Selin, kapıyı kilitlemeyi aklından çıkarma."
a1-fork,Mert ate the pancakes with a fork.,Mert krepleri çatalla yedi.
a1-fourteen,Fourteen players stood on the field.,Sahada on dört oyuncu duruyordu.
a1-fridge,Mert stored the leftovers in the fridge.,Mert artık yemekleri buzdolabına kaldırdı.
a1-friendly,The shopkeeper greets us with a friendly wave.,Dükkâncı bizi cana yakın bir el sallamayla karşılıyor.
a1-game,Mert and Ali finished the chess game quickly.,Mert ve Ali satranç oyununu hızlıca bitirdi.
a1-garage,Mert keeps his tools in the garage.,Mert aletlerini garajda tutuyor.
a1-girl,The girl in the raincoat jumped in puddles.,Yağmurluklu kız çocuğu su birikintilerine atladı.
a1-give,I will give Selin a surprise.,Selin'e bir sürpriz vereceğim.
a1-glass,Mert accidentally broke a glass and swept the pieces.,Mert yanlışlıkla bir bardak kırdı ve parçaları süpürdü.
a1-glove,The goalkeeper wore a padded glove to save penalties.,Kaleci penaltıları kurtarmak için dolgulu bir eldiven taktı.
a1-goodbye,Selin hugged her grandpa goodbye at the harbor.,Selin büyükbabasına limanda sarılıp veda etti.
a1-grandmother,Selin's grandmother knits woolly scarves for us.,Selin'in büyükannesi bize yünlü atkılar örer.
a1-grandparents,We spend Eid with my grandparents.,Bayramı büyükanne ve büyükbabamla geçiriyoruz.
a1-grape,A raisin is a dried grape.,"Kuru üzüm, kurutulmuş üzümdür."
a1-grass,Mert mows the grass on Sundays.,Mert pazar günleri çimenleri biçer.
a1-great,The chef's lasagna was absolutely great.,Şefin lazanyası kesinlikle harikaydı.
a1-green,Frogs and cucumbers are mostly green.,Kurbağalar ve salatalıklar çoğunlukla yeşildir.
a1-guitar,Mert strummed his guitar by the campfire.,Mert kamp ateşinin başında gitar çaldı.
a1-hand,Ali held my hand tightly and grinned.,Ali elimi sıkıca tuttu ve sırıttı.
a1-happy,Mert looked happy with his results.,Mert sonuçlarından mutlu görünüyordu.
a1-healthy,Salmon and spinach are healthy choices.,Somon ve ıspanak sağlıklı seçimlerdir.
a1-hear,I cannot hear you in this noisy hall.,Bu gürültülü salonda seni duyamıyorum.
a1-heavy,The closet is too heavy to lift alone.,Dolap tek başına kaldırmak için fazla ağır.
a1-hello,Selin smiled and said hello to the neighbors.,Selin gülümsedi ve komşulara merhaba dedi.
a1-help,Firefighters came quickly to help the villagers.,İtfaiyeciler köylülere yardım etmek için hızla geldi.
a1-high,The eagle flew high above the cliffs.,Kartal kayalıkların çok yukarısında uçtu.
a1-hobby,Collecting stamps is Mert's hobby.,Pul biriktirmek Mert'in hobisi.
a1-home,Ali finished his shift and walked home.,Ali vardiyasını bitirdi ve eve yürüdü.
a1-homework,Mom checked Ali's homework and signed it.,Annem Ali'nin ödevini kontrol edip imzaladı.
a1-hour,The ferry trip took almost an hour.,Vapur yolculuğu neredeyse bir saat sürdü.
a1-hungry,The hikers were so hungry that they ate quickly.,Yürüyüşçüler o kadar açtı ki hızla yediler.
a1-inside,"Thunder is loud, so the hens hide inside.","Gök gürültüsü çok, tavuklar içeride saklanıyor."
a1-interesting,The gallery has an interesting dinosaur exhibit.,Galerideki dinozor sergisi ilgimi çekiyor.
a1-iron,The tailor pressed the wrinkles with a steam iron.,Terzi buruşukları buharlı ütüyle düzeltti.
a1-italy,Rome and Venice are cities in Italy.,Roma ve Venedik İtalya'daki şehirlerdir.
```

## Denetim (D)

- **British words/spelling:** none left in sentences except the card words `holiday` and `biscuit` themselves (`a1-holiday`, `a1-biscuit`). Fixed this round: jumper, torch, mum→mom, pyjamas→pajamas, omelette→omelet, lasagne→lasagna, rucksack→backpack, wardrobe→closet, kiosk→store, railway→(station), queue→lines, pitch→field, tram→ferry, post→mail, "Mr"→"Mr.".
- **Translation additions removed (faithfulness):** difficult, dirty, easy, near, grey, hamburger, slow, wash, daughter, delicious, empty, back, cheese-/doll-/great-/fourteen-/station-/airport-/address-related wording. Word-count ratio outliers (<0.6 or >1.3) are only agglutinative-Turkish shortening, no extra content.
- **Sentences under 5 words:** none (149/149 ≥ 5).
- **≥ 2 words outside the A1 corpus** (heuristic: not in any other A1 answer/example): 71 sentences:

| id | non-A1 words |
|---|---|
| a1-glove | goalkeeper, wore, padded, save, penalties |
| a1-iron | tailor, pressed, wrinkles, steam |
| a1-high | eagle, flew, above, cliffs |
| a1-grandmother | s, knits, woolly, scarves |
| a1-enjoy | riding, roller, coasters, fair |
| a1-cold | arctic, freezing, explorers, pack |
| a1-tooth | wobbly, finally, out |
| a1-skirt | wore, pleated, blouse |
| a1-salt | fries, better, pinch |
| a1-light | feather, floats, gently |
| a1-interesting | gallery, dinosaur, exhibit |
| a1-inside | thunder, loud, hide |
| a1-head | helmet, protects, crash |
| a1-grey | wolf, gray, fur |
| a1-great | s, lasagna, absolutely |
| a1-funny | s, everybody, giggles |
| a1-friendly | shopkeeper, greets, wave |
| a1-february | sweethearts, swap, valentine |
| a1-drink | fills, mug, cocoa |
| a1-daughter | kaya, proudly, graduate |
| a1-colour | chameleon, color, hide |
| a1-clean | scrubbed, tiles, until |
| a1-bridge | sail, beneath, suspension |
| a1-boots | trudge, through, rubber |
| a1-beer | brewers, brew, barley |
| a1-bear | caught, salmon, stream |
| a1-bath | mara, fills, tub |
| a1-wash | while, dries |
| a1-start | fireworks, midnight |
| a1-question | s, clearly |
| a1-name | glued, tag |
| a1-mouth | covered, yawned |
| a1-italy | venice, cities |
| a1-idea | thought, project |
| a1-holiday | spent, seaside |
| a1-healthy | salmon, choices |
| a1-headache | loud, terrible |
| a1-hamburger | fries, ketchup |
| a1-hair | barber, trims |
| a1-guitar | strummed, campfire |
| a1-green | cucumbers, mostly |
| a1-grape | raisin, dried |
| a1-glass | accidentally, swept |
| a1-fridge | stored, leftovers |
| a1-free | enter, gallery |
| a1-follow | ducklings, leader |
| a1-exam | revised, final |
| a1-evening | bats, emerge |
| a1-eighteen | passed, driving |
| a1-door | knocked, midnight |
| a1-doll | sewed, pajamas |
| a1-dinner | lit, romantic |
| a1-delicious | baklava, crispy |
| a1-december | santa, claus |
| a1-computer | typed, essay |
| a1-coffee | barista, foam |
| a1-classmate | calculator, during |
| a1-child | cried, entrance |
| a1-cheese | grated, omelet |
| a1-catch | throw, frisbee |
| a1-camera | photographed, castle |
| a1-brown | deer, fur |
| a1-british | lucy, london |
| a1-bring | snacks, barbecue |
| a1-breakfast | pancakes, cereal |
| a1-biscuit | dunked, cocoa |
| a1-birthday | blew, out |
| a1-beach | build, sandcastles |
| a1-back | straps, hurt |
| a1-baby | newborn, cradle |
| a1-address | mailman, envelope |

## Gözden geçirilecek 15 cümle

| id | cümle | çeviri | neden özel |
|---|---|---|---|
| a1-beer | Brewers brew beer from barley and hops. | Bira üreticileri birayı arpa ve şerbetçiotundan yapar. | brewers, brew, barley, hops: far above A1 and an alcohol context for a learner app. |
| a1-glove | The goalkeeper wore a padded glove to save penalties. | Kaleci penaltıları kurtarmak için dolgulu bir eldiven taktı. | 5 non-A1 words (goalkeeper, padded, save, penalties); needs a plainer sentence. |
| a1-grey | The wolf has thick gray fur. | Kurdun kalın gri tüyleri var. | R6 (clue 'bulut rengi'); 'wolf' above A1; British 'grey' vs American 'gray' answer. |
| a1-light | A feather is very light and floats gently. | Bir tüy çok hafiftir ve nazikçe süzülür. | R6 (antonym clue); 'feather ... gently' is B1-ish. |
| a1-brown | A deer has soft brown fur. | Geyiğin yumuşak kahverengi tüyleri var. | R6 (clue 'toprak rengi' vs required 'kahverengi'); 'deer' is above A1. |
| a1-cold | The Arctic is freezing cold, so explorers pack parkas. | Kuzey Kutbu dondurucu soğuk, bu yüzden kaşifler parka alır. | Arctic, explorers, parkas; reads like B1. |
| a1-enjoy | Kids enjoy riding roller coasters at the fair. | Çocuklar lunaparkta hız trenine binmekten keyif alır. | roller coaster/fair vocabulary; clue 'zevk almak' vs 'keyif alır'. |
| a1-grandmother | Selin's grandmother knits woolly scarves for us. | Selin'in büyükannesi bize yünlü atkılar örer. | knits/woolly/scarves; 'Selin's' needs care in the blank. |
| a1-hamburger | He ordered a hamburger with fries and ketchup. | Patates kızartması ve ketçaplı bir hamburger sipariş etti. | non-A1 words: fries, ketchup ; scan R6 |
| a1-high | The eagle flew high above the cliffs. | Kartal kayalıkların çok yukarısında uçtu. | eagle, cliffs; 'flew above' is above A1. |
| a1-iron | The tailor pressed the wrinkles with a steam iron. | Terzi buruşukları buharlı ütüyle düzeltti. | tailor/wrinkles/steam are hard; 'steam iron' narrows the clue too much. |
| a1-mouth | He covered his mouth as he yawned. | Esnerken ağzını eliyle kapattı. | 'covered/yawned' above A1; Turkish now 'ağzını' (scan tolerates the consonant drop). |
| a1-name | Selin glued a name tag to her lunchbox. | Selin beslenme çantasına bir isim etiketi yapıştırdı. | 'name tag'/'glued' above A1; 'tag' frame may suggest other words. |
| a1-question | The speaker answered Ali's question clearly. | Konuşmacı Ali'nin sorusunu net şekilde yanıtladı. | 'Ali's ___' ; alternatives (problem/message) not excluded. |
| a1-bath | Mara fills the tub and takes a relaxing bath. | Mara küveti doldurur ve rahatlatıcı bir banyo yapar. | non-A1 words: mara, fills, tub |
