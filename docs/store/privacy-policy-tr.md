# Lexi — Gizlilik Politikası ve KVKK Aydınlatma Metni

**Son güncelleme:** 3 Kasım 2026 · **Kapsam:** Android ve iOS için Lexi uygulaması, 1.0.5 ve sonrası sürümler (uygulama kimliği `dev.talhabayhan.lexi`)

## 1. Veri sorumlusu
Veri sorumlusu **Hüseyin Talha Bayhan**'dır ("biz"). İletişim: **talhabayhan.aps@gmail.com** · **https://talhabayhn.github.io/lexi**.

## 2. Özet
- Lexi çevrimdışı çalışır. **Hesap yoktur**, kayıt gerekmez.
- Oyun ilerlemeniz, ayarlarınız ve kelime defteriniz **yalnızca cihazınızda** saklanır; bize iletilmez.
- Uygulamadaki tek üçüncü taraf hizmet **Google Firebase**'dir: **Crashlytics** (çökme raporları, her zaman açık) ve **Analytics** (kullanım istatistikleri, **siz onaylayana kadar kapalı**).
- Hiçbir veri satılmaz. Hiçbir veri reklam için ya da sizi diğer uygulama ve sitelerde izlemek için kullanılmaz. Uygulamada **reklam yoktur**, **reklam kimliği** ve **konum** kullanılmaz.
- Uygulama **çocuklara yönelik değildir**.

## 3. Yalnızca cihazınızda tutulan veriler
İlerleme, puanlar, seriler, joker envanteri, kelime defteri, istatistikler ve ayarlar (dil, ses, titreşim, hatırlatıcı, günlük hedef). Bu verilere erişmeyiz. Uygulamayı silmek (veya uygulama verisini temizlemek) bunları siler. Android'de cihazın kendi yedeği (örneğin Google yedeği), yedekleme ayarlarınıza göre uygulama verisini kopyalayabilir.

## 4. Firebase'e gönderilen veriler
| Hizmet | Veri | Amaç | Ne zaman |
|---|---|---|---|
| Firebase Analytics | Rastgele bir uygulama örnek kimliği (iOS'ta ayrıca satıcı tanımlayıcısı), cihaz modeli, işletim sistemi ve uygulama sürümü, dil, ülke düzeyinde konum ve uygulama içi olaylar: uygulama açıldı, tanıtım bitti, seviye testi sonucu, oyun başladı/bitti (mod, seviye, doğru sayısı, süre), joker kullanıldı (tür), sonuç paylaşıldı, hatırlatıcı açıldı. Oturum ve ilk açılış sayımını Firebase kendisi yapar. Ad, serbest metin ya da reklam kimliği yoktur. | Uygulamanın nasıl kullanıldığını anlama ve geliştirme | **Yalnızca siz onaylarsanız**: son tanıtım adımındaki "Anonim kullanım istatistiklerini paylaş" kutusunu işaretlerseniz ya da **Ayarlar → Kullanım istatistikleri** anahtarını açarsanız. O zamana kadar hiçbir şey gönderilmez. Anahtarı istediğiniz zaman kapatabilirsiniz; toplama durur. |
| Firebase Crashlytics | Çökme ve hata kayıtları, neyin başarısız olduğuna dair kısa sabit bir açıklama (sizin girdiğiniz metin asla), cihaz modeli, işletim sistemi sürümü, uygulama sürümü, çökme anındaki uygulama durumu ve rastgele bir kurulum kimliği | Çökmeleri bulma ve giderme, kararlılık | Mağaza sürümünde her zaman. Kullanım istatistikleri anahtarıyla kontrol edilmez. |

Firebase, Google LLC / Google Ireland Limited tarafından kendi şartlarıyla işletilir. Uygulama Google'a bağlandığında Google sunucuları bağlantının doğası gereği IP adresinizi alır.

## 5. Yapmadıklarımız
- Reklam kimliği okunmaz (Android'de reklam kimliği izni uygulamadan çıkarılmıştır; iOS'ta reklam tanımlayıcısı kullanılmaz ve uygulama izleme izni istemez).
- Reklam kişiselleştirme ve reklam depolama, uygulamanın Firebase ayarlarında kapatılmıştır. Analytics verisi reklam hizmetleriyle paylaşılmaz ve reklam profili oluşturmak için kullanılmaz.
- Uygulama konum izni istemez, kişileri, fotoğrafları ya da dosyaları okumaz, ses kaydetmez.
- Hiçbir veri kimseye satılmaz veya kiralanmaz.

## 6. Bildirimler
Günlük hatırlatıcıyı açarsanız bildirimler **cihazınızda yerel olarak** planlanır. Push sunucusu kullanmayız. İzni sistem ayarlarından geri alabilirsiniz.

## 7. Saklama süresi
- Cihazdaki veriler: uygulamayı veya verisini silene kadar.
- Firebase Analytics olay verisi: Firebase ayarlarımızda 2 ay saklanır, ardından Google tarafından silinir.
- Crashlytics çökme verisi: Google tarafından yaklaşık 90 gün saklanır.

## 8. Yurt dışına aktarım
Google verileri yurt dışında (ör. ABD) işleyebilir. Google, standart sözleşme maddeleri ve/veya yeterlilik kararları gibi güvencelere dayanır.

## 9. Haklarınız ve silme talebi
**KVKK m.11** uyarınca: verilerin işlenip işlenmediğini öğrenme, bilgi talep etme, amacını ve alıcıları öğrenme, düzeltme/silme isteme, sonuca itiraz, zararın giderilmesini talep etme. **GDPR** uyarınca: erişim, düzeltme, silme, kısıtlama, taşınabilirlik, itiraz, rızayı dilediğiniz zaman geri çekme ve denetim otoritesine şikâyet. Başvuru: **talhabayhan.aps@gmail.com**; 30 gün içinde yanıtlarız.
- **Rızayı geri çekme:** Ayarlar → Kullanım istatistikleri anahtarını kapatın. Bu, sonraki analiz toplamasını durdurur.
- **Silme:** bize e-posta yazın. Sizinle ilgili ad, e-posta ya da hesap tutmuyoruz; yukarıdaki kimlikler rastgeledir ve uygulamada gösterilmez, bu yüzden sizi tek başına bulamayız. Uygulamayı silmek toplamayı sonlandırır; Firebase veriyi 7. bölümdeki sürelerden sonra siler.
- Kişisel Verileri Koruma Kurumu'na (KVKK) veya bulunduğunuz yerdeki denetim otoritesine de şikâyet edebilirsiniz.

## 10. Çocuklar
Lexi **çocuklara yönelik değildir**; 13 yaşın (veya ülkenizdeki dijital rıza yaşının) altındaki çocuklardan bilerek kişisel veri toplamayız. Bir çocuğun veri verdiğini düşünüyorsanız bize yazın, silinmesine yardımcı oluruz.

## 11. Güvenlik
Firebase'e gönderilen veri aktarım sırasında şifrelenir (HTTPS/TLS). Cihaz verisi uygulamanın özel alanında durur. Hiçbir yöntem tamamen güvenli değildir.

## 12. Değişiklikler
Bu politikayı güncelleyebiliriz; yukarıdaki tarih değişir. Önemli değişikliklerde (ör. reklam ya da satın alma gibi yeni hizmetler) bunlar yürürlüğe girmeden önce uygulama içinde bildiririz.
