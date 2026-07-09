# SonKarar — Kurulabilir APK

Bu klasördeki `SonKarar-cevrimdisi-debug.apk` dosyası, uygulamanın **anahtarsız /
çevrimdışı** çalışan sürümüdür. Herhangi bir API anahtarı veya Firebase kurulumu
gerektirmeden Android cihazına kurulup kullanılabilir.

## Kurulum

1. `SonKarar-cevrimdisi-debug.apk` dosyasını Android telefonuna indir.
2. Telefonda **Ayarlar → Güvenlik → Bilinmeyen kaynaklara izin ver** (veya kurulum
   sırasında çıkan izni onayla).
3. APK'ye dokunup kur.
4. Uygulamayı aç ve giriş ekranında **"Girişsiz Devam Et (Çevrimdışı)"** butonuna bas.

## Çevrimdışı modda neler çalışır?

- Hazır yemek ve film/dizi havuzu otomatik yüklenir (silebilir, ekleyebilirsin).
- Ağırlıklı karar çarkı, zaman cezası ve yerel öneriler tamamen çalışır.
- Favoriler (yıldız), karar geçmişi ve "Bunu Azalt" özellikleri çalışır.
- Tüm veriler cihazda yerel (Room) veritabanında saklanır.
- Film/dizi sonuçlarında platform, puan ve detay linki; yemeklerde tarif linki gösterilir.

## Çift (iki cihaz) modu

Gerçek zamanlı çift modu için Firebase yapılandırması (google-services.json) ve
Google ile Giriş için Web istemci kimliği gerekir. Bu APK, yapılandırma olmadan da
çevrimdışı modda sorunsuz çalışır.

## Not

Bu bir **debug** APK'dir; hızlı kurulum ve deneme içindir. Mağaza yayını için imzalı
bir **release** derlemesi (kendi imza anahtarınla) alınması önerilir:
`./gradlew :app:assembleRelease` (imza yapılandırması ekledikten sonra).
