# C1 batch 12 (8 kart)

Dosya: `content/cards/C1/batch12.csv`. B2 batch 13'ten C1'e taşınan 8 kart (owner kararı); id `c1-<cevap>`, örnekler C1'e uygun yeniden yazıldı, yinelenen id/cevap yok.

`qa.py` ve `fill_blank_scan.py` geçici kopyada çalıştırıldı: 0 hata / 0 uyarı; CONTAINS X/Q kartları R1 (tasarım gereği fill-blank dışı).

Harf sayıları (STARTS / CONTAINS): X 1 (0/1), Q 1 (0/1), Y 1 (1/0), J 1 (1/0), K 2 (2/0), G 2 (2/0)

C1 önce->sonra: X 12->13, Q 16->17, Y 14->15, J 12->13, K 15->17, G 17->19 | toplam 675->683

| id | answer | letter | rule | clue_tr | example | not |
|---|---|---|---|---|---|---|
| c1-reflex | REFLEX | X | CONTAINS | uyarana karşı istemsiz ve ani sinirsel tepki | The study measured how quickly the blink reflex responds to sudden light. / Çalışma, göz kırpma refleksinin ani ışığa ne kadar hızlı yanıt verdiğini ölçtü. |  |
| c1-jingle | JINGLE | J | STARTS | reklamlarda kullanılan kısa ve akılda kalan ezgi | The brand's jingle became so familiar that shoppers hummed it in the aisles. / Markanın reklam ezgisi o kadar tanıdık hale geldi ki müşteriler reyonlarda onu mırıldanıyordu. |  |
| c1-kernel | KERNEL | K | STARTS | ceviz, badem gibi sert kabuklu meyvenin yenen içi | Each almond kernel is protected by a hard shell. / Her badem içi sert bir kabukla korunur. |  |
| c1-keepsake | KEEPSAKE | K | STARTS | bir anı olarak saklanan eşya, yadigâr | She bought a small keepsake in every city she visited. / Gezdiği her şehirden küçük bir hatıra eşyası aldı. |  |
| c1-grudge | GRUDGE | G | STARTS | eski bir olay yüzünden beslenen kin | Years later, he still bore a grudge against the rival firm that had taken his clients. / Yıllar sonra hâlâ müşterilerini çalan rakip firmaya karşı kin besliyordu. |  |
| c1-gimmick | GIMMICK | G | STARTS | ilgi çekmek için kullanılan numara | Critics dismissed the 3D screen as a mere gimmick to boost ticket sales. / Eleştirmenler 3D ekranı yalnızca bilet satışını artırmak için bir numara olarak niteledi. |  |
| c1-uniquely | UNIQUELY | Q | CONTAINS | başka hiçbir şeye benzemeyecek biçimde, kendine özgü olarak | The island is uniquely suited to studying how species adapt in isolation. / Ada, türlerin yalıtılmış ortamda nasıl uyum sağladığını incelemek için eşsiz biçimde elverişlidir. |  |
| c1-yearbook | YEARBOOK | Y | STARTS | mezuniyette çıkarılan, öğrenci fotoğraflı okul kitabı | The editors compiled the yearbook from hundreds of photographs and tributes. / Editörler mezuniyet albümünü yüzlerce fotoğraf ve anma yazısından derledi. |  |
