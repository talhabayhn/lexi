> **TASLAK — NİHAİ DEĞİL. Yayımlanmadan önce sahibi ve yetkin bir avukat tarafından incelenmelidir. Hukuki tavsiye değildir. Her "VERIFY" notu, hukuken belirsiz bir noktayı ya da son sürümde doğrulanacak bir bilgiyi gösterir.**

# Lexi — Gizlilik Politikası ve KVKK Aydınlatma Metni

**Son güncelleme:** [TARİH] · **Kapsam:** Android ve iOS için Lexi uygulaması (uygulama kimliği `dev.talhabayhan.lexi`)

## 1. Veri sorumlusu
Veri sorumlusu **[DEVELOPER NAME]**'dir ("biz"). İletişim: **[CONTACT EMAIL]** · **[WEBSITE]**. VERIFY: adres, VERBİS yükümlülüğü ve gerekiyorsa AB temsilcisi (bkz. §12).

## 2. Özet
- Lexi çevrimdışı çalışır. 1.0 sürümünde **hesap yoktur**, kayıt gerekmez.
- Oyun ilerlemeniz, ayarlarınız ve kelime defteriniz **yalnızca cihazınızda** saklanır; bize iletilmez.
- Kullandığımız üçüncü taraf hizmetler: **Google AdMob** (ödüllü reklam), **Google User Messaging Platform** (onay), **Firebase Analytics ve Crashlytics**, **RevenueCat** (satın alma) ve Apple App Store / Google Play ödeme sistemleri.
- Uygulama **çocuklara yönelik değildir**.

## 3. Yalnızca cihazınızda tutulan veriler
İlerleme, puanlar, seriler, joker envanteri, kelime defteri, istatistikler, ayarlar (dil, ses, titreşim, hatırlatıcı, günlük hedef) ve Pro durumu önbelleği. Bu verilere erişmeyiz. Uygulamayı silmek (veya uygulama verisini temizlemek) bunları siler. VERIFY: Google/Apple cihaz yedekleri, kendi yedekleme ayarlarınıza göre uygulama verisini kopyalayabilir.

## 4. Üçüncü taraf hizmetlerce işlenen veriler
| Hizmet | Veri (genellikle) | Amaç | Hukuki sebep (VERIFY) |
|---|---|---|---|
| Google AdMob | Reklam kimliği (GAID / izin verilirse IDFA), IP adresi, cihaz ve uygulama bilgisi, reklam etkileşimi | Ödüllü reklam gösterme ve ölçme, sahteciliği önleme | Açık rıza (KVKK m.5/1, m.6; GDPR m.6/1(a)) |
| Google UMP | Onay tercihi, tercihi saklamak için gereken cihaz tanımlayıcıları | Rıza toplama ve kaydetme | Hukuki yükümlülük / meşru menfaat |
| Firebase Analytics | Uygulama örnek kimliği, cihaz/işletim sistemi/uygulama sürümü, IP'den ülke düzeyi konum, uygulama içi olaylar (ör. çark tamamlandı, seviye, mod — serbest metin yok) | Kullanımı anlama, uygulamayı geliştirme | Gereken yerde rıza; VERIFY |
| Firebase Crashlytics | Çökme kayıtları, cihaz modeli/OS, uygulama sürümü, kurulum kimliği | Çökmeleri giderme, kararlılık | Meşru menfaat; VERIFY (rıza gerekir mi) |
| RevenueCat | Anonim kullanıcı kimliği, satın alma/hak durumu, mağaza makbuz verisi, ülke | Lexi Pro satın almalarını doğrulama ve geri yükleme | Sözleşmenin ifası |
| Apple / Google ödeme | Mağaza tarafından işlenir; kart bilgilerinizi görmeyiz | Satın alma ve iade | Sözleşmenin ifası |

Firebase ve AdMob Google LLC / Google Ireland Limited tarafından, RevenueCat ise RevenueCat, Inc. (ABD) tarafından kendi şartlarıyla işlenir. VERIFY: nihai SDK sürümleri ve veri işleme şartları.

## 5. Reklam kimliği ve onay
- Reklamlar **ödüllü**dür (joker kazanmak için izleyip izlemeyeceğinize siz karar verirsiniz). VERIFY: 1.0'da geçiş (interstitial) reklam varsa bu bölümü güncelleyin.
- AEA, Birleşik Krallık ve Türkiye'de (VERIFY kapsam) ilk açılışta onay formu (Google UMP) gösterilir; analiz ve kişiselleştirilmiş reklamlar yalnızca tercihinize göre çalışır. Tercihinizi **Ayarlar → Gizlilik** bölümünden değiştirebilirsiniz. VERIFY: bu menü sürümde olmalıdır.
- iOS'ta kişiselleştirilmiş reklam etkinse uygulama izleme izni (ATT) ister. VERIFY: bkz. `app-privacy-apple.md`.
- Reklam kimliğinizi cihaz ayarlarından sıfırlayabilir veya silebilirsiniz. Kişiselleştirilmiş reklamı reddederseniz kişiselleştirilmemiş reklam görebilirsiniz.
- Lexi Pro reklamları kaldırır.

## 6. Bildirimler
Günlük hatırlatıcıyı açarsanız bildirimler **cihazınızda yerel olarak** planlanır. 1.0'da push sunucusu kullanmayız. İzni sistem ayarlarından geri alabilirsiniz.

## 7. Saklama süresi
- Cihazdaki veriler: uygulamayı veya verisini silene kadar.
- Analiz/çökme verileri: Firebase ayarlarımıza göre Google tarafından saklanır (VERIFY: olay verisi için 2 veya 14 ay; Crashlytics yaklaşık 90 gün).
- RevenueCat: silme talebi olmadıkça müşteri kaydı süresince. VERIFY.
- Reklam verisi: Google politikalarına göre.

## 8. Yurt dışına aktarım
Google ve RevenueCat verileri yurt dışında (ör. ABD) işleyebilir. Sağlayıcıların standart sözleşme maddeleri ve/veya yeterlilik mekanizmalarına dayanırız. VERIFY: KVKK m.9 (2024 değişikliği sonrası) ve GDPR Bölüm V.

## 9. Haklarınız
**KVKK m.11** uyarınca: verilerin işlenip işlenmediğini öğrenme, bilgi talep etme, amacını öğrenme, aktarılan üçüncü kişileri bilme, düzeltme/silme isteme, otomatik analiz sonucuna itiraz, zararın giderilmesini talep etme. **GDPR** uyarınca: erişim, düzeltme, silme, kısıtlama, taşınabilirlik, itiraz, rızayı dilediğiniz zaman geri çekme ve denetim otoritesine şikâyet. Başvuru: **[CONTACT EMAIL]**; 30 gün içinde yanıtlarız (VERIFY: KVKK başvuru usulü, Veri Sorumlusuna Başvuru Usul ve Esasları Tebliği). Hesap ve oyun verisi tutmadığımız için reklam kimliğinizi / RevenueCat kullanıcı kimliğinizi (Ayarlar → Hakkında; VERIFY gösterilecek mi) istemek zorunda kalabiliriz. Google/RevenueCat'te tutulan veriye ilişkin talepler bu sağlayıcılara yönlendirilebilir. Şikâyet: Kişisel Verileri Koruma Kurumu (KVKK).

## 10. Çocuklar
Lexi **çocuklara yönelik değildir**; 13 yaşın (veya ülkenizdeki dijital rıza yaşının) altındaki çocuklardan bilerek kişisel veri toplamayız. VERIFY: Türkiye'de yaş eşiği ve reşit olmayanlar için açık rıza (veli onayı) konusu. Bir çocuğun veri verdiğini düşünüyorsanız bize yazın, silinmesine yardımcı oluruz. VERIFY: bkz. `app-privacy-apple.md` hedef kitle kararı.

## 11. Güvenlik
Uygulama ile üçüncü taraf hizmetler arasındaki aktarım HTTPS/TLS kullanır. Cihaz verisi uygulamanın özel alanında durur. Hiçbir yöntem tamamen güvenli değildir.

## 12. Veri sorumlusu bilgileri (KVKK / GDPR)
VERIFY: KVKK m.10 aydınlatma yükümlülüğü (sorumlunun kimliği, amaç, alıcılar, toplama yöntemi ve hukuki sebep, haklar) bu metinle karşılanıyor mu; VERBİS kayıt eşiği; AB'de yerleşik değilseniz GDPR m.27 temsilcisi.

## 13. Değişiklikler
Bu politikayı güncelleyebiliriz; yukarıdaki tarih değişir, önemli değişikliklerde uygulama içinde bildiririz.
