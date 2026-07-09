# SonKarar

**SonKarar**, çiftlerin *"Ne yesek?"*, *"Ne izlesek?"* gibi kararsızlık krizlerini
çözen; iki Android cihazda **gerçek zamanlı senkronize** çalışan akıllı bir karar
çarkı uygulamasıdır. Ortak bir veri havuzundan beslenir, geçmiş seçimleri analiz
eder (zaman cezası), dışarıdan yeni öneriler enjekte eder (TMDB / yemek etiketleri)
ve **ağırlıklı bir algoritmayla** animasyonlu çark üzerinden kararı verir.

> Bu depo, bir yapay zekâ kod ajanının (OpenAI Codex, Cursor vb.) **sıfır hata ile**
> derlenebilir bir Android projesine dönüştürebileceği eksiksiz **Ürün Gereksinim
> Dökümanı (PRD)** ve **Mimari Şartname**yi içerir. Tüm içerik, değişken isimleri,
> veri tabanı şemaları ve arayüz metinleri **Türkçe**dir.

## Teknoloji Yığını

- **Dil / UI:** Kotlin + Jetpack Compose (Material 3)
- **Arka plan / Kimlik:** Firebase Auth (Google ile Giriş) + Firebase Firestore
- **Dış API:** TMDB (The Movie Database)
- **Yerel önbellek:** Room DB (çevrimdışı destek)
- **Mimari:** Clean Architecture + MVVM + Hilt

## Mimari Döküman Seti

Aşağıdaki belgeler sırayla okunmalıdır. Kod ajanı için giriş noktası
`docs/07-KOD-URETIM-TALIMATLARI.md` içindeki "Definition of Done" kontrol listesidir.

| Sıra | Belge | İçerik |
| --- | --- | --- |
| 00 | [Ürün Gereksinim Dökümanı](docs/00-URUN-GEREKSINIM-DOKUMANI.md) | Vizyon, kapsam, kullanıcı akışları, kabul kriterleri |
| 01 | [Teknoloji Yığını ve Sürümler](docs/01-TEKNOLOJI-YIGINI-VE-SURUMLER.md) | Bağımlılıklar, sürüm kataloğu, Gradle yapılandırması |
| 02 | [Proje Yapısı ve Mimari](docs/02-PROJE-YAPISI-VE-MIMARI.md) | Paket yapısı, katmanlar, Hilt modülleri |
| 03 | [Veri Katmanı ve Şemalar](docs/03-VERI-KATMANI-VE-SEMALAR.md) | Firestore/Room şemaları, DTO'lar, güvenlik kuralları |
| 04 | [Repository ve Domain](docs/04-REPOSITORY-VE-DOMAIN.md) | Repository sözleşmeleri, UseCase'ler, `Sonuc` hata yönetimi |
| 05 | [Algoritmalar ve İş Mantığı](docs/05-ALGORITMALAR-VE-IS-MANTIGI.md) | Ağırlıklı çark, zaman cezası, öneri enjeksiyonu, senkronizasyon |
| 06 | [Arayüz ve Ekran Mimarisi](docs/06-ARAYUZ-VE-EKRAN-MIMARISI.md) | Tema, navigasyon, çark çizimi, haptik/ses, Türkçe metinler |
| 07 | [Kod Üretim Talimatları](docs/07-KOD-URETIM-TALIMATLARI.md) | Adım adım talimatlar, kabul kriterleri, kontrol listesi |

## Öne Çıkan Özellikler

- **Ağırlıklı rastgele seçim:** Her ögenin ağırlığıyla orantılı adil seçim.
- **Zaman cezası:** Son 3 turda çıkan ögelerin ağırlığı %80 düşürülür (monotonluk kırılır).
- **Öneri enjeksiyonu:** Çiftin en sevdiği türlere göre her turda 3 yeni öneri (`disOneriMi = true`).
- **Gerçek zamanlı senkronizasyon:** Eş çarkı çevirdiğinde diğer cihazda anlık animasyon ve eşzamanlı sonuç + konfeti.
- **Çevrimdışı dayanıklılık:** Room, havuz için tek gerçek kaynak (SSOT).
- **Kararlı hata yönetimi:** `Sonuc` sealed class ile tüm hatalar Türkçe mesajla yakalanır; çökme yok.
- **Koyu/Neon tema:** Fizik tabanlı çark, haptik geri bildirim ve ses efektleri.

## Kurulum Notları (Uygulama Üretildikten Sonra)

1. Firebase konsolundan `google-services.json` alınıp `app/` altına konur.
2. `local.properties` içine `TMDB_API_ANAHTARI=...` eklenir (opsiyonel; yoksa uygulama yerel havuzla çalışır).
3. `firestore.rules` Firebase konsoluna yüklenir.
4. `./gradlew assembleDebug` ile derlenir, `./gradlew testDebugUnitTest` ile test edilir.
