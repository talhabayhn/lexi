# FB-A2-1 — fill-the-blank rewrite batch (A2, 150 `rewrite` cards)

Selection: `FILL-BLANK-REVIEW.csv`, level A2, action `rewrite` (196 cards), ordered by failing-rule count (most first), then id; first 150. Only `example_en` / `example_tr` change. `content/cards` is **not** edited yet (apply on "uygula").

Rules: American spelling/words; Turkish is a faithful translation of the English (nothing added or swapped to satisfy the scanner); answer is a whole word in the sentence; ≥ 5 words; no other A2 answer as a whole word except `content/leak-exempt.txt`. Table, CSV block and lists below are generated from one source list (id → sentence, translation).

Checks (scratch copy of `content/` with all 150 applied, current tools): `qa.py` 0 errors / 0 warnings; `fill_blank_scan.py` 149/150 clean, 1 flagged (a2-indoors). A1 cards byte-identical to the repo (FB-A1-1 rows untouched). Table ↔ CSV block compared automatically: identical.

| id | old sentence | new sentence | new translation | note |
|---|---|---|---|---|
| a2-happen | What will happen next? | Nothing will happen to you if you stay calm. | Sakin kalırsan sana hiçbir şey olmayacak. |  |
| a2-height | What is your height? | The nurse wrote down Mert's height on a card. | Hemşire Mert'in boyunu bir karta yazdı. |  |
| a2-hold | Hold the bag. | Please hold my bag so I can tie my shoes. | Ayakkabılarımı bağlayabilmem için lütfen çantamı tut. |  |
| a2-silly | Do not be silly. | The kids made silly faces and giggled all day. | Çocuklar saçma suratlar yapıp bütün gün kıkırdadı. |  |
| a2-untidy | My room is untidy. | Her desk is untidy, with papers everywhere. | Masası düzensiz, her yerde kâğıtlar var. |  |
| a2-almost | We are almost home. | We are almost at the top of the hill. | Tepenin tepesine neredeyse ulaştık. |  |
| a2-asleep | The baby is asleep. | The baby fell asleep in the car. | Bebek arabada uyuyakaldı. |  |
| a2-awful | The food was awful. | The soup tasted awful, so I did not finish it. | Çorbanın tadı berbattı, bu yüzden bitirmedim. |  |
| a2-beside | Sit beside me. | Mert sat beside his grandfather at the table. | Mert masada dedesinin yanına oturdu. |  |
| a2-bite | Does your dog bite? | Do not pet that dog; it may bite you. | O köpeği sevme; seni ısırabilir. |  |
| a2-blanket | The blanket is soft. | She pulled a warm blanket over her legs. | Bacaklarının üzerine sıcak bir battaniye çekti. |  |
| a2-blow | Blow out the candles. | Blow gently on your soup; it is very hot. | Çorbana yavaşça üfle; çok sıcak. |  |
| a2-candle | She lit a candle. | Dad lit a candle when the lights went out. | Işıklar sönünce babam bir mum yaktı. |  |
| a2-cheerful | He is always cheerful. | The cheerful waiter smiled at everyone in the cafe. | Neşeli garson kafedeki herkese gülümsedi. |  |
| a2-correct | Your answer is correct. | Only one answer on the list is correct. | Listedeki cevaplardan yalnızca biri doğru. |  |
| a2-dead | The flowers are dead. | The plant is dead because we forgot to water it. | Bitki ölmüş çünkü sulamayı unuttuk. |  |
| a2-earring | She lost an earring. | Selin lost an earring at the beach. | Selin plajda bir küpesini kaybetti. |  |
| a2-easily | She won easily. | Mert lifted the heavy box easily. | Mert ağır kutuyu kolayca kaldırdı. |  |
| a2-enough | We have enough food. | We have enough chairs for all the guests. | Tüm misafirler için yeterince sandalyemiz var. |  |
| a2-eraser | I need an eraser. | The pencil has a small eraser on the end. | Kalemin ucunda küçük bir silgi var. |  |
| a2-fresh | The bread is fresh. | We bought fresh bread from the market this morning. | Bu sabah marketten taze ekmek aldık. |  |
| a2-guess | Guess my age! | Can you guess what is inside the box? | Kutunun içinde ne olduğunu tahmin edebilir misin? |  |
| a2-hate | I hate mushrooms. | I hate waiting in long lines at the bank. | Bankada uzun kuyruklarda beklemekten nefret ederim. |  |
| a2-headphones | I forgot my headphones. | He wore his headphones to listen to music on the bus. | Otobüste müzik dinlemek için kulaklığını taktı. |  |
| a2-immediately | Call me immediately. | When the teacher called my name, I answered immediately. | Öğretmen adımı söyleyince hemen cevap verdim. |  |
| a2-large | This room is large. | We ordered a large pizza for six people. | Altı kişi için büyük bir pizza sipariş ettik. |  |
| a2-lucky | You are so lucky! | I was lucky to catch the last bus home. | Eve giden son otobüsü yakaladığım için şanslıydım. |  |
| a2-miss | I miss Ali. | I miss my grandmother when she is away. | Büyükannem uzaktayken onu özlüyorum. |  |
| a2-narrow | The street is narrow. | The street is too narrow for two cars. | Sokak iki araba için fazla dar. |  |
| a2-nationality | What is your nationality? | The man asked about her name and nationality. | Adam onun adını ve uyruğunu sordu. |  |
| a2-naughty | The puppy is naughty. | The naughty puppy chewed my new shoes. | Yaramaz köpek yavrusu yeni ayakkabılarımı çiğnedi. |  |
| a2-nearly | It is nearly midnight. | It is nearly eight o'clock, so we must go. | Saat neredeyse sekiz, gitmemiz gerek. |  |
| a2-online | I shop online. | Selin bought her new shoes online. | Selin yeni ayakkabılarını çevrimiçi aldı. |  |
| a2-outfit | I like your outfit. | She chose a blue outfit for the party. | Parti için mavi bir kıyafet seçti. |  |
| a2-pick | Pick a card. | Let us pick apples from the tree today. | Bugün ağaçtan elma toplayalım. |  |
| a2-popular | This game is popular. | This game is very popular with young people. | Bu oyun gençler arasında çok seviliyor. |  |
| a2-race | He won the race. | Ali ran fast and won the race. | Ali hızlı koştu ve yarışı kazandı. |  |
| a2-reason | Tell me the reason. | He gave a good reason for being late. | Geç kaldığı için iyi bir sebep gösterdi. |  |
| a2-runner | The runner finished first. | The runner stopped to drink some water. | Koşucu biraz su içmek için durdu. |  |
| a2-salary | His salary is good. | Her new job has a much better salary. | Yeni işinin maaşı çok daha iyi. |  |
| a2-score | What is the score? | The score was two to one at halftime. | Devre arasında skor ikiye birdi. |  |
| a2-seat | Is this seat free? | Please give your seat to an older person. | Lütfen yaşlı birine yerinizi verin. |  |
| a2-speaker | Turn on the speaker. | The speaker on my phone is too quiet. | Telefonumdaki hoparlör çok kısık. |  |
| a2-sunset | The sunset was beautiful. | We watched the sunset from the roof. | Gün batımını çatıdan izledik. |  |
| a2-total | What is the total? | The total on the receipt was twenty dollars. | Fişteki toplam yirmi dolardı. |  |
| a2-true | Is this story true? | Is it true that you speak three languages? | Üç dil konuştuğun doğru mu? |  |
| a2-vacation | Our vacation was lovely. | We are going on vacation to Italy next week. | Gelecek hafta İtalya'ya tatile gidiyoruz. |  |
| a2-verb | Run is a verb. | Run is a verb and table is a noun. | Koşmak bir fiil, masa ise bir isimdir. |  |
| a2-voice | Your voice is soft. | The singer has a beautiful voice. | Şarkıcının güzel bir sesi var. |  |
| a2-vowel | A is a vowel. | The word apple starts with a vowel. | Apple sözcüğü bir sesli harfle başlar. |  |
| a2-waste | Do not waste water. | Do not waste food, because many people are hungry. | Yiyecekleri israf etme çünkü birçok insan aç. |  |
| a2-whose | Whose bag is this? | Whose umbrella is this on the table? | Masanın üstündeki bu şemsiye kimin? |  |
| a2-yogurt | Yogurt is sour. | Mom adds honey to her yogurt. | Annem yoğurduna bal ekler. |  |
| a2-yummy | This soup is yummy. | The cookies were yummy, so I ate five. | Kurabiyeler nefisti, bu yüzden beş tane yedim. |  |
| a2-actor | The actor is on TV. | The actor learned all his lines for the movie. | Oyuncu film için bütün repliklerini öğrendi. |  |
| a2-afraid | Are you afraid of dogs? | The little boy is afraid of the dark. | Küçük çocuk karanlıktan korkuyor. |  |
| a2-alive | The fish is still alive. | The doctor saw that the man was still alive. | Doktor adamın hâlâ yaşadığını gördü. |  |
| a2-allow | They do not allow dogs in the shop. | My school does not allow phones in class. | Okulum sınıfta telefona izin vermiyor. |  |
| a2-amazing | Ali's painting is amazing. | Our trip to the mountains was amazing. | Dağlara yaptığımız gezi harikaydı. |  |
| a2-argue | My parents never argue. | Please do not argue about the TV again. | Lütfen yine televizyon yüzünden tartışmayın. |  |
| a2-army | His brother is in the army. | My uncle joined the army last year. | Amcam geçen yıl orduya katıldı. |  |
| a2-average | She is an average student. | The average age of the players is twelve. | Oyuncuların yaş ortalaması on iki. |  |
| a2-awake | I was awake all night. | The coffee kept me awake all night. | Kahve beni bütün gece uyanık tuttu. |  |
| a2-beef | We had beef with rice. | Dad grilled beef and vegetables for dinner. | Babam akşam yemeğinde dana eti ve sebze ızgara yaptı. |  |
| a2-bell | The school bell rang. | The bell rings at nine every morning. | Zil her sabah dokuzda çalar. |  |
| a2-bill | Ali paid the bill. | Dad asked the waiter for the bill. | Babam garsondan hesabı istedi. |  |
| a2-biology | We have biology on Wednesdays. | Selin has a biology test on Friday. | Selin'in cuma günü biyoloji sınavı var. |  |
| a2-bored | Selin looks bored. | I was bored, so I called my friend. | Canım sıkıldı, bu yüzden arkadaşımı aradım. |  |
| a2-borrow | Can I borrow your pen? | May I borrow your dictionary for a minute? | Sözlüğünü bir dakikalığına ödünç alabilir miyim? |  |
| a2-boss | My boss is very kind. | Her boss gave everyone a day off. | Patronu herkese bir gün izin verdi. |  |
| a2-bottom | The key is at the bottom of the bag. | The coins fell to the bottom of the well. | Bozuk paralar kuyunun dibine düştü. |  |
| a2-bracelet | She gave me a bracelet. | She wears a thin bracelet on her arm. | Kolunda ince bir bilezik takıyor. |  |
| a2-brake | The brake does not work. | He pressed the brake when the light turned red. | Işık kırmızıya dönünce frene bastı. |  |
| a2-campsite | The campsite has a shop. | The campsite is near a quiet lake. | Kamp alanı sessiz bir gölün yakınında. |  |
| a2-check | Please check your answers. | Please check the time of the next train. | Lütfen bir sonraki trenin saatini kontrol edin. |  |
| a2-choose | You can choose one cake. | You can choose one gift from the table. | Masadan bir hediye seçebilirsin. |  |
| a2-clever | She is a clever girl. | The clever dog opened the door by himself. | Zeki köpek kapıyı kendi başına açtı. |  |
| a2-coach | Our coach is very tough. | The coach told the team to run faster. | Antrenör takıma daha hızlı koşmasını söyledi. |  |
| a2-coast | We drove by the coast. | Many small towns lie on the coast. | Birçok küçük kasaba kıyıda yer alır. |  |
| a2-comb | Her comb is in her bag. | He used a comb on his long curly hair. | Uzun kıvırcık saçında tarak kullandı. |  |
| a2-comfortable | This sofa is very comfortable. | These new shoes are very comfortable to wear. | Bu yeni ayakkabılar giymek için çok rahat. |  |
| a2-compass | The compass points north. | The hikers used a compass to find north. | Yürüyüşçüler kuzeyi bulmak için pusula kullandı. |  |
| a2-continue | Please continue reading. | After a short break, we will continue the lesson. | Kısa bir aradan sonra derse devam edeceğiz. |  |
| a2-corridor | The classroom is at the end of the corridor. | Students wait in the corridor before class. | Öğrenciler dersten önce koridorda bekler. |  |
| a2-deep | The lake is very deep. | The river is deep here, so do not swim. | Nehir burada derin, o yüzden yüzme. |  |
| a2-describe | Can you describe your teacher? | Can you describe the man you saw? | Gördüğün adamı tarif edebilir misin? |  |
| a2-dessert | We had fruit for dessert. | For dessert, we ate chocolate cake with cream. | Tatlı olarak kremalı çikolatalı kek yedik. |  |
| a2-diet | My mother is on a diet. | The doctor told him to follow a healthy diet. | Doktor ona sağlıklı bir diyet uygulamasını söyledi. |  |
| a2-direction | Which direction is the station? | The wind changed direction during the night. | Rüzgâr gece boyunca yön değiştirdi. |  |
| a2-disco | We danced at the disco. | They danced all night at the disco. | Bütün gece diskoda dans ettiler. |  |
| a2-drawer | The keys are in the drawer. | Mert put the keys in the top drawer. | Mert anahtarları üst çekmeceye koydu. |  |
| a2-drop | Do not drop the glass. | Do not drop the eggs on the kitchen floor. | Yumurtaları mutfak zeminine düşürme. |  |
| a2-earn | How much do you earn? | He works two jobs to earn more money. | Daha fazla para kazanmak için iki işte çalışıyor. |  |
| a2-electricity | We have no electricity tonight. | We had no electricity for two hours. | İki saat elektriğimiz yoktu. |  |
| a2-energy | I have no energy today. | Children have a lot of energy in the morning. | Çocukların sabahları çok enerjisi vardır. |  |
| a2-engineer | My father is an engineer. | The engineer built a new bridge over the river. | Mühendis nehrin üzerine yeni bir köprü yaptı. |  |
| a2-enter | Do not enter the kitchen. | Please take off your shoes before you enter. | Girmeden önce lütfen ayakkabılarını çıkar. |  |
| a2-entrance | The entrance is at the back. | Let us meet at the park entrance at noon. | Öğlen park girişinde buluşalım. |  |
| a2-fantastic | We had a fantastic trip. | We had a fantastic time at the party last night. | Dün gece partide harika vakit geçirdik. |  |
| a2-fashion | She loves fashion magazines. | Her sister works in the fashion industry. | Kız kardeşi moda sektöründe çalışıyor. |  |
| a2-final | This is the final question. | This is the final question on the test. | Bu, testteki son soru. |  |
| a2-finally | The bus finally arrived. | After two hours, the train finally arrived. | İki saat sonra tren sonunda geldi. |  |
| a2-freezer | The fish is in the freezer. | Put the peas in the freezer, please. | Lütfen bezelyeleri dondurucuya koy. |  |
| a2-gentleman | He is a real gentleman. | The gentleman opened the door for her. | Beyefendi onun için kapıyı açtı. |  |
| a2-giant | The giant was very tall. | The giant in the story lived on a hill. | Hikâyedeki dev bir tepede yaşıyordu. |  |
| a2-ground | He sat on the ground. | The apples fell to the ground in the wind. | Elmalar rüzgârda yere düştü. |  |
| a2-grow | Plants grow in spring. | Tomatoes grow best in warm weather. | Domatesler en iyi sıcak havada büyür. |  |
| a2-hamster | My sister has a hamster. | Her hamster sleeps all day and plays at night. | Hamsteri bütün gün uyur, geceleri oynar. |  |
| a2-handsome | Her brother is very handsome. | My uncle is tall and very handsome. | Amcam uzun boylu ve çok yakışıklı. |  |
| a2-human | Every human needs water. | A human has two arms and two legs. | Bir insanın iki kolu ve iki bacağı vardır. |  |
| a2-indoors | We play indoors when it rains. | It is raining, so the kids stay indoors. | Yağmur yağıyor, bu yüzden çocuklar içeride kalıyor. | scan R6 (clue/translation stem: descriptive clue, faithful translation kept) |
| a2-injury | He has a leg injury. | The player missed the game because of an injury. | Oyuncu bir sakatlık yüzünden maçı kaçırdı. |  |
| a2-instrument | Can you play an instrument? | Piano is a beautiful musical instrument. | Piyano güzel bir müzik çalgısıdır. |  |
| a2-international | It is an international school. | Istanbul has a big international airport. | İstanbul'da büyük bir uluslararası havalimanı var. |  |
| a2-jelly | Children love strawberry jelly. | The red jelly wobbled on the plate. | Kırmızı jöle tabakta sallandı. | US 'jelly' = jam; clue and context (wobbled on the plate) keep the UK dessert meaning |
| a2-judge | The judge listened carefully. | The judge asked the man to stand up. | Hâkim adamdan ayağa kalkmasını istedi. |  |
| a2-karate | My cousin learns karate. | She wears a white belt in karate class. | Karate dersinde beyaz kuşak takıyor. |  |
| a2-keep | You can keep this badge. | You can keep the book for a week. | Kitabı bir hafta boyunca tutabilirsin. |  |
| a2-knot | Can you tie a knot? | He tied a knot in his shoelace. | Ayakkabı bağına bir düğüm attı. |  |
| a2-lawyer | My aunt is a lawyer. | The lawyer read the contract very carefully. | Avukat sözleşmeyi çok dikkatli okudu. |  |
| a2-lose | Do not lose the receipt. | Try not to lose your ticket on the trip. | Gezide biletini kaybetmemeye çalış. |  |
| a2-loud | The music is too loud. | The music was too loud last night. | Müzik dün gece çok gürültülüydü. |  |
| a2-luggage | My luggage is very heavy. | Our luggage was very heavy at the airport. | Havalimanında bagajımız çok ağırdı. |  |
| a2-manager | The manager is in her office. | The store manager thanked all the workers. | Mağaza müdürü tüm çalışanlara teşekkür etti. |  |
| a2-marry | They will marry in the spring. | They want to marry on a beach. | Bir kumsalda evlenmek istiyorlar. |  |
| a2-mattress | This mattress is very hard. | I bought a new mattress for my bed. | Yatağım için yeni bir şilte aldım. |  |
| a2-mean | What does this word mean? | What does this word mean in Turkish? | Bu sözcük Türkçede ne anlama geliyor? |  |
| a2-meeting | Mert missed the meeting. | The meeting starts at ten in the morning. | Toplantı sabah onda başlıyor. |  |
| a2-member | He is a member of the music club. | She became a member of the school club. | Okul kulübünün üyesi oldu. |  |
| a2-mess | My room is a mess. | The kids left a big mess in the kitchen. | Çocuklar mutfakta büyük bir dağınıklık bıraktı. |  |
| a2-moment | Wait a moment, please. | Please wait a moment, I will be right back. | Lütfen bir an bekle, hemen döneceğim. |  |
| a2-navy | His father is in the navy. | My grandfather served in the navy for years. | Dedem yıllarca donanmada görev yaptı. |  |
| a2-neighbour | Our neighbor is very kind. | Our neighbor waters the flowers every day. | Komşumuz her gün çiçekleri suluyor. |  |
| a2-neighbourhood | This is a quiet neighborhood. | Our neighborhood has a small park and a library. | Mahallemizde küçük bir park ve bir kütüphane var. |  |
| a2-nephew | My nephew plays football. | My nephew is learning to ride a bike. | Yeğenim bisiklete binmeyi öğreniyor. |  |
| a2-nobody | Nobody knows the answer. | Nobody was home when the mailman came. | Postacı geldiğinde evde kimse yoktu. |  |
| a2-normal | It was a normal day. | It is normal to feel tired after a long trip. | Uzun bir geziden sonra yorgun hissetmek normaldir. |  |
| a2-note | I left you a note. | I left a note on the fridge door. | Buzdolabının kapağına bir not bıraktım. |  |
| a2-novel | She is reading a novel. | She read a long novel on the plane. | Uçakta uzun bir roman okudu. |  |
| a2-nowhere | There is nowhere to sit. | I looked in every room, but my keys are nowhere. | Her odaya baktım ama anahtarlarım hiçbir yerde yok. |  |
| a2-orchestra | The orchestra plays tonight. | The orchestra played music for the dancers. | Orkestra dansçılar için müzik çaldı. |  |
| a2-organise | I will organize the party. | We need to organize a party for Mom. | Annem için bir parti düzenlememiz gerek. |  |
| a2-outdoor | Mert loves outdoor sports. | We bought a new outdoor table for the garden. | Bahçe için yeni bir açık hava masası aldık. |  |
| a2-outdoors | The children play outdoors. | On sunny days, we love to eat outdoors. | Güneşli günlerde dışarıda yemek yemeyi seviyoruz. |  |
| a2-passport | Don't forget your passport. | Show your passport at the airport desk. | Havalimanındaki gişede pasaportunu göster. |  |
| a2-peach | This peach is very sweet. | She cut a ripe peach into small pieces. | Olgun bir şeftaliyi küçük parçalara kesti. |  |
| a2-personal | This is a personal question. | Please do not ask me personal questions. | Lütfen bana kişisel sorular sorma. |  |
| a2-point | We won by one point. | Our team won a point in the last minute. | Takımımız son dakikada bir puan kazandı. |  |
| a2-power | The king has a lot of power. | The king had great power over the people. | Kralın halk üzerinde büyük bir gücü vardı. |  |
| a2-prince | The prince is ten years old. | The young prince is the king's only son. | Genç prens kralın tek oğlu. |  |

## Patch-ready CSV (id, example_en, example_tr)

```csv
id,example_en,example_tr
a2-happen,Nothing will happen to you if you stay calm.,Sakin kalırsan sana hiçbir şey olmayacak.
a2-height,The nurse wrote down Mert's height on a card.,Hemşire Mert'in boyunu bir karta yazdı.
a2-hold,Please hold my bag so I can tie my shoes.,Ayakkabılarımı bağlayabilmem için lütfen çantamı tut.
a2-silly,The kids made silly faces and giggled all day.,Çocuklar saçma suratlar yapıp bütün gün kıkırdadı.
a2-untidy,"Her desk is untidy, with papers everywhere.","Masası düzensiz, her yerde kâğıtlar var."
a2-almost,We are almost at the top of the hill.,Tepenin tepesine neredeyse ulaştık.
a2-asleep,The baby fell asleep in the car.,Bebek arabada uyuyakaldı.
a2-awful,"The soup tasted awful, so I did not finish it.","Çorbanın tadı berbattı, bu yüzden bitirmedim."
a2-beside,Mert sat beside his grandfather at the table.,Mert masada dedesinin yanına oturdu.
a2-bite,Do not pet that dog; it may bite you.,O köpeği sevme; seni ısırabilir.
a2-blanket,She pulled a warm blanket over her legs.,Bacaklarının üzerine sıcak bir battaniye çekti.
a2-blow,Blow gently on your soup; it is very hot.,Çorbana yavaşça üfle; çok sıcak.
a2-candle,Dad lit a candle when the lights went out.,Işıklar sönünce babam bir mum yaktı.
a2-cheerful,The cheerful waiter smiled at everyone in the cafe.,Neşeli garson kafedeki herkese gülümsedi.
a2-correct,Only one answer on the list is correct.,Listedeki cevaplardan yalnızca biri doğru.
a2-dead,The plant is dead because we forgot to water it.,Bitki ölmüş çünkü sulamayı unuttuk.
a2-earring,Selin lost an earring at the beach.,Selin plajda bir küpesini kaybetti.
a2-easily,Mert lifted the heavy box easily.,Mert ağır kutuyu kolayca kaldırdı.
a2-enough,We have enough chairs for all the guests.,Tüm misafirler için yeterince sandalyemiz var.
a2-eraser,The pencil has a small eraser on the end.,Kalemin ucunda küçük bir silgi var.
a2-fresh,We bought fresh bread from the market this morning.,Bu sabah marketten taze ekmek aldık.
a2-guess,Can you guess what is inside the box?,Kutunun içinde ne olduğunu tahmin edebilir misin?
a2-hate,I hate waiting in long lines at the bank.,Bankada uzun kuyruklarda beklemekten nefret ederim.
a2-headphones,He wore his headphones to listen to music on the bus.,Otobüste müzik dinlemek için kulaklığını taktı.
a2-immediately,"When the teacher called my name, I answered immediately.",Öğretmen adımı söyleyince hemen cevap verdim.
a2-large,We ordered a large pizza for six people.,Altı kişi için büyük bir pizza sipariş ettik.
a2-lucky,I was lucky to catch the last bus home.,Eve giden son otobüsü yakaladığım için şanslıydım.
a2-miss,I miss my grandmother when she is away.,Büyükannem uzaktayken onu özlüyorum.
a2-narrow,The street is too narrow for two cars.,Sokak iki araba için fazla dar.
a2-nationality,The man asked about her name and nationality.,Adam onun adını ve uyruğunu sordu.
a2-naughty,The naughty puppy chewed my new shoes.,Yaramaz köpek yavrusu yeni ayakkabılarımı çiğnedi.
a2-nearly,"It is nearly eight o'clock, so we must go.","Saat neredeyse sekiz, gitmemiz gerek."
a2-online,Selin bought her new shoes online.,Selin yeni ayakkabılarını çevrimiçi aldı.
a2-outfit,She chose a blue outfit for the party.,Parti için mavi bir kıyafet seçti.
a2-pick,Let us pick apples from the tree today.,Bugün ağaçtan elma toplayalım.
a2-popular,This game is very popular with young people.,Bu oyun gençler arasında çok seviliyor.
a2-race,Ali ran fast and won the race.,Ali hızlı koştu ve yarışı kazandı.
a2-reason,He gave a good reason for being late.,Geç kaldığı için iyi bir sebep gösterdi.
a2-runner,The runner stopped to drink some water.,Koşucu biraz su içmek için durdu.
a2-salary,Her new job has a much better salary.,Yeni işinin maaşı çok daha iyi.
a2-score,The score was two to one at halftime.,Devre arasında skor ikiye birdi.
a2-seat,Please give your seat to an older person.,Lütfen yaşlı birine yerinizi verin.
a2-speaker,The speaker on my phone is too quiet.,Telefonumdaki hoparlör çok kısık.
a2-sunset,We watched the sunset from the roof.,Gün batımını çatıdan izledik.
a2-total,The total on the receipt was twenty dollars.,Fişteki toplam yirmi dolardı.
a2-true,Is it true that you speak three languages?,Üç dil konuştuğun doğru mu?
a2-vacation,We are going on vacation to Italy next week.,Gelecek hafta İtalya'ya tatile gidiyoruz.
a2-verb,Run is a verb and table is a noun.,"Koşmak bir fiil, masa ise bir isimdir."
a2-voice,The singer has a beautiful voice.,Şarkıcının güzel bir sesi var.
a2-vowel,The word apple starts with a vowel.,Apple sözcüğü bir sesli harfle başlar.
a2-waste,"Do not waste food, because many people are hungry.",Yiyecekleri israf etme çünkü birçok insan aç.
a2-whose,Whose umbrella is this on the table?,Masanın üstündeki bu şemsiye kimin?
a2-yogurt,Mom adds honey to her yogurt.,Annem yoğurduna bal ekler.
a2-yummy,"The cookies were yummy, so I ate five.","Kurabiyeler nefisti, bu yüzden beş tane yedim."
a2-actor,The actor learned all his lines for the movie.,Oyuncu film için bütün repliklerini öğrendi.
a2-afraid,The little boy is afraid of the dark.,Küçük çocuk karanlıktan korkuyor.
a2-alive,The doctor saw that the man was still alive.,Doktor adamın hâlâ yaşadığını gördü.
a2-allow,My school does not allow phones in class.,Okulum sınıfta telefona izin vermiyor.
a2-amazing,Our trip to the mountains was amazing.,Dağlara yaptığımız gezi harikaydı.
a2-argue,Please do not argue about the TV again.,Lütfen yine televizyon yüzünden tartışmayın.
a2-army,My uncle joined the army last year.,Amcam geçen yıl orduya katıldı.
a2-average,The average age of the players is twelve.,Oyuncuların yaş ortalaması on iki.
a2-awake,The coffee kept me awake all night.,Kahve beni bütün gece uyanık tuttu.
a2-beef,Dad grilled beef and vegetables for dinner.,Babam akşam yemeğinde dana eti ve sebze ızgara yaptı.
a2-bell,The bell rings at nine every morning.,Zil her sabah dokuzda çalar.
a2-bill,Dad asked the waiter for the bill.,Babam garsondan hesabı istedi.
a2-biology,Selin has a biology test on Friday.,Selin'in cuma günü biyoloji sınavı var.
a2-bored,"I was bored, so I called my friend.","Canım sıkıldı, bu yüzden arkadaşımı aradım."
a2-borrow,May I borrow your dictionary for a minute?,Sözlüğünü bir dakikalığına ödünç alabilir miyim?
a2-boss,Her boss gave everyone a day off.,Patronu herkese bir gün izin verdi.
a2-bottom,The coins fell to the bottom of the well.,Bozuk paralar kuyunun dibine düştü.
a2-bracelet,She wears a thin bracelet on her arm.,Kolunda ince bir bilezik takıyor.
a2-brake,He pressed the brake when the light turned red.,Işık kırmızıya dönünce frene bastı.
a2-campsite,The campsite is near a quiet lake.,Kamp alanı sessiz bir gölün yakınında.
a2-check,Please check the time of the next train.,Lütfen bir sonraki trenin saatini kontrol edin.
a2-choose,You can choose one gift from the table.,Masadan bir hediye seçebilirsin.
a2-clever,The clever dog opened the door by himself.,Zeki köpek kapıyı kendi başına açtı.
a2-coach,The coach told the team to run faster.,Antrenör takıma daha hızlı koşmasını söyledi.
a2-coast,Many small towns lie on the coast.,Birçok küçük kasaba kıyıda yer alır.
a2-comb,He used a comb on his long curly hair.,Uzun kıvırcık saçında tarak kullandı.
a2-comfortable,These new shoes are very comfortable to wear.,Bu yeni ayakkabılar giymek için çok rahat.
a2-compass,The hikers used a compass to find north.,Yürüyüşçüler kuzeyi bulmak için pusula kullandı.
a2-continue,"After a short break, we will continue the lesson.",Kısa bir aradan sonra derse devam edeceğiz.
a2-corridor,Students wait in the corridor before class.,Öğrenciler dersten önce koridorda bekler.
a2-deep,"The river is deep here, so do not swim.","Nehir burada derin, o yüzden yüzme."
a2-describe,Can you describe the man you saw?,Gördüğün adamı tarif edebilir misin?
a2-dessert,"For dessert, we ate chocolate cake with cream.",Tatlı olarak kremalı çikolatalı kek yedik.
a2-diet,The doctor told him to follow a healthy diet.,Doktor ona sağlıklı bir diyet uygulamasını söyledi.
a2-direction,The wind changed direction during the night.,Rüzgâr gece boyunca yön değiştirdi.
a2-disco,They danced all night at the disco.,Bütün gece diskoda dans ettiler.
a2-drawer,Mert put the keys in the top drawer.,Mert anahtarları üst çekmeceye koydu.
a2-drop,Do not drop the eggs on the kitchen floor.,Yumurtaları mutfak zeminine düşürme.
a2-earn,He works two jobs to earn more money.,Daha fazla para kazanmak için iki işte çalışıyor.
a2-electricity,We had no electricity for two hours.,İki saat elektriğimiz yoktu.
a2-energy,Children have a lot of energy in the morning.,Çocukların sabahları çok enerjisi vardır.
a2-engineer,The engineer built a new bridge over the river.,Mühendis nehrin üzerine yeni bir köprü yaptı.
a2-enter,Please take off your shoes before you enter.,Girmeden önce lütfen ayakkabılarını çıkar.
a2-entrance,Let us meet at the park entrance at noon.,Öğlen park girişinde buluşalım.
a2-fantastic,We had a fantastic time at the party last night.,Dün gece partide harika vakit geçirdik.
a2-fashion,Her sister works in the fashion industry.,Kız kardeşi moda sektöründe çalışıyor.
a2-final,This is the final question on the test.,"Bu, testteki son soru."
a2-finally,"After two hours, the train finally arrived.",İki saat sonra tren sonunda geldi.
a2-freezer,"Put the peas in the freezer, please.",Lütfen bezelyeleri dondurucuya koy.
a2-gentleman,The gentleman opened the door for her.,Beyefendi onun için kapıyı açtı.
a2-giant,The giant in the story lived on a hill.,Hikâyedeki dev bir tepede yaşıyordu.
a2-ground,The apples fell to the ground in the wind.,Elmalar rüzgârda yere düştü.
a2-grow,Tomatoes grow best in warm weather.,Domatesler en iyi sıcak havada büyür.
a2-hamster,Her hamster sleeps all day and plays at night.,"Hamsteri bütün gün uyur, geceleri oynar."
a2-handsome,My uncle is tall and very handsome.,Amcam uzun boylu ve çok yakışıklı.
a2-human,A human has two arms and two legs.,Bir insanın iki kolu ve iki bacağı vardır.
a2-indoors,"It is raining, so the kids stay indoors.","Yağmur yağıyor, bu yüzden çocuklar içeride kalıyor."
a2-injury,The player missed the game because of an injury.,Oyuncu bir sakatlık yüzünden maçı kaçırdı.
a2-instrument,Piano is a beautiful musical instrument.,Piyano güzel bir müzik çalgısıdır.
a2-international,Istanbul has a big international airport.,İstanbul'da büyük bir uluslararası havalimanı var.
a2-jelly,The red jelly wobbled on the plate.,Kırmızı jöle tabakta sallandı.
a2-judge,The judge asked the man to stand up.,Hâkim adamdan ayağa kalkmasını istedi.
a2-karate,She wears a white belt in karate class.,Karate dersinde beyaz kuşak takıyor.
a2-keep,You can keep the book for a week.,Kitabı bir hafta boyunca tutabilirsin.
a2-knot,He tied a knot in his shoelace.,Ayakkabı bağına bir düğüm attı.
a2-lawyer,The lawyer read the contract very carefully.,Avukat sözleşmeyi çok dikkatli okudu.
a2-lose,Try not to lose your ticket on the trip.,Gezide biletini kaybetmemeye çalış.
a2-loud,The music was too loud last night.,Müzik dün gece çok gürültülüydü.
a2-luggage,Our luggage was very heavy at the airport.,Havalimanında bagajımız çok ağırdı.
a2-manager,The store manager thanked all the workers.,Mağaza müdürü tüm çalışanlara teşekkür etti.
a2-marry,They want to marry on a beach.,Bir kumsalda evlenmek istiyorlar.
a2-mattress,I bought a new mattress for my bed.,Yatağım için yeni bir şilte aldım.
a2-mean,What does this word mean in Turkish?,Bu sözcük Türkçede ne anlama geliyor?
a2-meeting,The meeting starts at ten in the morning.,Toplantı sabah onda başlıyor.
a2-member,She became a member of the school club.,Okul kulübünün üyesi oldu.
a2-mess,The kids left a big mess in the kitchen.,Çocuklar mutfakta büyük bir dağınıklık bıraktı.
a2-moment,"Please wait a moment, I will be right back.","Lütfen bir an bekle, hemen döneceğim."
a2-navy,My grandfather served in the navy for years.,Dedem yıllarca donanmada görev yaptı.
a2-neighbour,Our neighbor waters the flowers every day.,Komşumuz her gün çiçekleri suluyor.
a2-neighbourhood,Our neighborhood has a small park and a library.,Mahallemizde küçük bir park ve bir kütüphane var.
a2-nephew,My nephew is learning to ride a bike.,Yeğenim bisiklete binmeyi öğreniyor.
a2-nobody,Nobody was home when the mailman came.,Postacı geldiğinde evde kimse yoktu.
a2-normal,It is normal to feel tired after a long trip.,Uzun bir geziden sonra yorgun hissetmek normaldir.
a2-note,I left a note on the fridge door.,Buzdolabının kapağına bir not bıraktım.
a2-novel,She read a long novel on the plane.,Uçakta uzun bir roman okudu.
a2-nowhere,"I looked in every room, but my keys are nowhere.",Her odaya baktım ama anahtarlarım hiçbir yerde yok.
a2-orchestra,The orchestra played music for the dancers.,Orkestra dansçılar için müzik çaldı.
a2-organise,We need to organize a party for Mom.,Annem için bir parti düzenlememiz gerek.
a2-outdoor,We bought a new outdoor table for the garden.,Bahçe için yeni bir açık hava masası aldık.
a2-outdoors,"On sunny days, we love to eat outdoors.",Güneşli günlerde dışarıda yemek yemeyi seviyoruz.
a2-passport,Show your passport at the airport desk.,Havalimanındaki gişede pasaportunu göster.
a2-peach,She cut a ripe peach into small pieces.,Olgun bir şeftaliyi küçük parçalara kesti.
a2-personal,Please do not ask me personal questions.,Lütfen bana kişisel sorular sorma.
a2-point,Our team won a point in the last minute.,Takımımız son dakikada bir puan kazandı.
a2-power,The king had great power over the people.,Kralın halk üzerinde büyük bir gücü vardı.
a2-prince,The young prince is the king's only son.,Genç prens kralın tek oğlu.
```

## Audit

- **Needs decision (different answer/level):** none.
- **British words/spelling in sentences:** none. `a2-neighbour` / `a2-neighbourhood` / `a2-organise` keep their ids; the answers are already American (NEIGHBOR, NEIGHBORHOOD, ORGANIZE) and the sentences use them.
- **Sentences under 5 words:** none (150/150 ≥ 5).
- **Leaks (R9):** 0 after the final pass (many first drafts leaked A2 answers such as worry, weight, bench, careful, storm, fox, castle; all reworded).
- **Flagged, left as is:** see list below; faithful translation cannot share a stem with the clue without adding words.
- **İnceleme listesi (review list):** `a2-jelly` (US jelly = jam; context keeps the UK dessert sense), `a2-alive` (`yaşadığını` for 'alive'), `a2-hold` / `a2-keep` / `a2-mean` / `a2-point` (polysemous words, context chosen to match the clue).

## Flagged cards

| id | English | Turkish | note |
|---|---|---|---|
| a2-indoors | It is raining, so the kids stay indoors. | Yağmur yağıyor, bu yüzden çocuklar içeride kalıyor. | scan R6 (clue/translation stem: descriptive clue, faithful translation kept) |

## 15 weakest sentences (heuristic: fewest context words, then shortest)

| id | English | Turkish |
|---|---|---|
| a2-electricity | We had no electricity for two hours. | İki saat elektriğimiz yoktu. |
| a2-score | The score was two to one at halftime. | Devre arasında skor ikiye birdi. |
| a2-voice | The singer has a beautiful voice. | Şarkıcının güzel bir sesi var. |
| a2-amazing | Our trip to the mountains was amazing. | Dağlara yaptığımız gezi harikaydı. |
| a2-describe | Can you describe the man you saw? | Gördüğün adamı tarif edebilir misin? |
| a2-disco | They danced all night at the disco. | Bütün gece diskoda dans ettiler. |
| a2-gentleman | The gentleman opened the door for her. | Beyefendi onun için kapıyı açtı. |
| a2-handsome | My uncle is tall and very handsome. | Amcam uzun boylu ve çok yakışıklı. |
| a2-knot | He tied a knot in his shoelace. | Ayakkabı bağına bir düğüm attı. |
| a2-marry | They want to marry on a beach. | Bir kumsalda evlenmek istiyorlar. |
| a2-mean | What does this word mean in Turkish? | Bu sözcük Türkçede ne anlama geliyor? |
| a2-sunset | We watched the sunset from the roof. | Gün batımını çatıdan izledik. |
| a2-whose | Whose umbrella is this on the table? | Masanın üstündeki bu şemsiye kimin? |
| a2-bored | I was bored, so I called my friend. | Canım sıkıldı, bu yüzden arkadaşımı aradım. |
| a2-borrow | May I borrow your dictionary for a minute? | Sözlüğünü bir dakikalığına ödünç alabilir miyim? |

## 10 random sentences (seed 11)

| id | English | Turkish |
|---|---|---|
| a2-judge | The judge asked the man to stand up. | Hâkim adamdan ayağa kalkmasını istedi. |
| a2-outdoors | On sunny days, we love to eat outdoors. | Güneşli günlerde dışarıda yemek yemeyi seviyoruz. |
| a2-lawyer | The lawyer read the contract very carefully. | Avukat sözleşmeyi çok dikkatli okudu. |
| a2-moment | Please wait a moment, I will be right back. | Lütfen bir an bekle, hemen döneceğim. |
| a2-voice | The singer has a beautiful voice. | Şarkıcının güzel bir sesi var. |
| a2-verb | Run is a verb and table is a noun. | Koşmak bir fiil, masa ise bir isimdir. |
| a2-navy | My grandfather served in the navy for years. | Dedem yıllarca donanmada görev yaptı. |
| a2-loud | The music was too loud last night. | Müzik dün gece çok gürültülüydü. |
| a2-immediately | When the teacher called my name, I answered immediately. | Öğretmen adımı söyleyince hemen cevap verdim. |
| a2-jelly | The red jelly wobbled on the plate. | Kırmızı jöle tabakta sallandı. |
