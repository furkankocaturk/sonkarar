# SonKarar — Kod Üretim Talimatları (Yapay Zekâ Kod Ajanı İçin)

> Bu döküman, kod ajanının projeyi **sıfır belirsizlikle** ve **derlenebilir**
> biçimde üretmesi için adım adım talimatları, dosya oluşturma sırasını, kabul
> kriterlerini ve "Definition of Done" kontrol listesini içerir.

---

## 1. Genel İlkeler (Zorunlu)

1. **Dil:** Tüm paketler, sınıflar, değişkenler, Firestore alanları, arayüz metinleri
   ve hata mesajları **Türkçe** olacaktır. Framework anahtar kelimeleri (Kotlin/AndroidX)
   İngilizce kalır (zorunludur).
2. **Mimari:** `02-PROJE-YAPISI-VE-MIMARI.md` içindeki paket yapısına **birebir** uy.
3. **Sürümler:** `01-TEKNOLOJI-YIGINI-VE-SURUMLER.md` içindeki sürümleri değiştirme.
4. **Hata yönetimi:** Tüm ağ/servis çağrıları `guvenliCagri { }` veya `Sonuc` ile
   sarılacak. İşlenmeyen istisna kalmayacak. `CancellationException` yeniden fırlatılacak.
5. **Çevrimdışı:** Havuz UI'ı Room'u gözlemler; Firestore dinleyicisi Room'u besler.
6. **Derlenebilirlik:** Proje, `google-services.json` ve TMDB anahtarı olmadan da
   **derlenmeli**; bu durumda ilgili özellikler kibarca devre dışı kalmalı.

---

## 2. Dosya Oluşturma Sırası (Önerilen)

Kod ajanı aşağıdaki sırayı takip ederek her adımda derlenebilir bir ara duruma yakın kalmalıdır:

1. **Gradle iskeleti:** `settings.gradle.kts`, kök `build.gradle.kts`,
   `gradle/libs.versions.toml`, `app/build.gradle.kts`, `gradle.properties`,
   `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.9).
2. **Manifest + Application + Activity:** `AndroidManifest.xml`,
   `SonKararUygulamasi`, `MainActivity`.
3. **Çekirdek:** `Sonuc`, `HataYonetimi`, `Kategori`, `Sabitler`.
4. **Domain modelleri** → **repository arayüzleri** → **UseCase'ler**.
5. **Saf algoritma:** `AgirlikliSecimYap`, `ZamanCezasi`, `CarkiCevirKullanimi`.
6. **Data:** DTO'lar, eşleyiciler, Room (varlık/DAO/veritabanı), TMDB servisi,
   repository uygulamaları.
7. **DI modülleri:** `FirebaseModulu`, `AgModulu`, `VeriTabaniModulu`,
   `RepositoryModulu`, `UygulamaModulu`.
8. **Tema** → **navigasyon** → **ortak bileşenler**.
9. **Ekranlar:** Giriş → Eşleşme → Havuz → Çark (ViewModel + ArayuzDurumu + Composable).
10. **Kaynaklar:** `strings.xml` (Türkçe), tema kaynakları, ikon.
11. **Testler:** Saf algoritma birim testleri (bkz. Bölüm 5).

---

## 3. `AndroidManifest.xml` (Referans)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".SonKararUygulamasi"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/uygulama_adi"
        android:supportsRtl="true"
        android:theme="@style/Theme.SonKarar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SonKarar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

> `@style/Theme.SonKarar`, `res/values/themes.xml` içinde `Theme.Material3.DayNight.NoActionBar`
> tabanlı tanımlanır (Compose teması ayrıca `SonKararTemasi` ile uygulanır).

---

## 4. Örnek `google-services.json` (Yer Tutucu)

Gerçek dosya Firebase konsolundan alınır. Derlemenin bozulmaması için `app/` altına
aşağıdaki **yer tutucu** konabilir (gerçek değerlerle değiştirilmesi gerektiği not düşülür):

```json
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "sonkarar-ornek",
    "storage_bucket": "sonkarar-ornek.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000000000",
        "android_client_info": { "package_name": "com.sonkarar" }
      },
      "oauth_client": [],
      "api_key": [ { "current_key": "YER_TUTUCU_API_ANAHTARI" } ],
      "services": { "appinvite_service": { "other_platform_oauth_client": [] } }
    }
  ],
  "configuration_version": "1"
}
```

> **Uyarı:** Bu yer tutucu ile uygulama derlenir ama Firebase servisleri gerçek
> çalışmaz. Gerçek `google-services.json` `.gitignore`'a eklenmelidir.

---

## 5. Test Stratejisi

En az aşağıdaki **saf birim testleri** yazılmalıdır (Android'e bağımsız, hızlı):

- `AgirlikliSecimYapTest`:
  - Sabit tohumlu `Random` ile deterministik seçim doğrulanır.
  - Ağırlığı yüksek ögenin belirgin biçimde daha sık seçildiği istatistiksel test.
  - Boş liste `null` döndürür.
- `ZamanCezasiTest`:
  - Son 3 turda çıkan ögenin ağırlığının %20'ye indiği doğrulanır.
  - Ceza sonrası ağırlık en az 1 olur.
  - İlgisiz kategori kayıtları cezayı tetiklemez.
- `CarkiCevirKullanimiTest` (MockK ile repository sahteleri):
  - Boş havuz + başarısız öneri → `Sonuc.Hata` ("Havuzda hiç seçenek yok…").
  - TMDB hatası → çark yerel havuzla yine sonuç üretir (yumuşak hata).
  - Enjekte edilen öneriler `disOneriMi = true` içerir.
- `HataYonetimiTest`:
  - Bilinen istisnalar doğru Türkçe mesaja eşlenir.

Örnek test:

```kotlin
// test/AgirlikliSecimYapTest.kt
package com.sonkarar

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.kullanim.AgirlikliSecimYap
import com.sonkarar.domain.model.HavuzOgesi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class AgirlikliSecimYapTest {

    private val secim = AgirlikliSecimYap()

    private fun oge(isim: String, agirlik: Int) = HavuzOgesi(
        id = isim, kategori = Kategori.YEMEK, isim = isim,
        ekleyenKullanici = "t", agirlik = agirlik
    )

    @Test
    fun `bos liste null doner`() {
        assertNull(secim(emptyList()))
    }

    @Test
    fun `yuksek agirlik daha sik secilir`() {
        val ogeler = listOf(oge("A", 90), oge("B", 10))
        val sayac = mutableMapOf("A" to 0, "B" to 0)
        val rastgele = Random(42)
        repeat(10_000) {
            val secilen = secim(ogeler, rastgele)!!
            sayac[secilen.isim] = sayac.getValue(secilen.isim) + 1
        }
        // A yaklaşık %90 civarı seçilmeli
        assert(sayac.getValue("A") > sayac.getValue("B") * 5)
    }
}
```

---

## 6. Kabul Kriterleri (Test Edilebilir)

| Kod | Senaryo | Beklenen |
| --- | --- | --- |
| KK-01 | `./gradlew assembleDebug` çalıştırılır | Derleme başarılı |
| KK-02 | `./gradlew testDebugUnitTest` çalıştırılır | Tüm birim testleri geçer |
| KK-03 | İki cihaz aynı odaya girer, biri çevirir | Diğerinde "Eşiniz çarkı çeviriyor…" + aynı sonuç |
| KK-04 | Havuz boşken çevir | "Havuzda hiç seçenek yok…" uyarısı, çökme yok |
| KK-05 | İnternet kapalı, havuz açılır | Room'dan son veriler görünür, çökme yok |
| KK-06 | TMDB anahtarı boş | Uygulama çalışır, yalnızca yerel havuz/yemek etiketleri |
| KK-07 | Aynı öge 3 kez çıkar | 4. turda çıkma olasılığı belirgin azalır (ağırlık %20) |
| KK-08 | Öge sağa/sola kaydırılır | Silinir + "Geri Al" Snackbar'ı |
| KK-09 | Tüm ekranlar taranır | Sabit İngilizce metin yok; tümü Türkçe |
| KK-10 | Çark döner | Çizgi geçişinde haptik + ses; destek yoksa çökme yok |

---

## 7. Definition of Done (Bitmiş Tanımı) Kontrol Listesi

- [ ] Proje `assembleDebug` ile hatasız derleniyor.
- [ ] Tüm birim testleri geçiyor (`testDebugUnitTest`).
- [ ] Paket yapısı `02` belgesindeki ağaçla birebir uyuşuyor.
- [ ] Firestore alan/koleksiyon adları `03` belgesiyle birebir aynı ve Türkçe.
- [ ] `Sonuc` sealed class ile tutarlı hata yönetimi her repository'de mevcut.
- [ ] Havuz UI'ı Room'dan gözlemleniyor; çevrimdışı çalışıyor.
- [ ] Ağırlıklı seçim + zaman cezası + 3 öneri enjeksiyonu uygulanmış.
- [ ] Gerçek zamanlı senkronizasyon `carkDurumu.tur` sayacı ile idempotent.
- [ ] Koyu/Neon tema, fizik tabanlı çark, haptik + ses, konfeti mevcut.
- [ ] Tüm görünür metinler `strings.xml` içinde ve Türkçe.
- [ ] TMDB anahtarı `local.properties` → `BuildConfig` üzerinden; koda gömülü değil.
- [ ] `google-services.json` ve `local.properties` `.gitignore` içinde.
- [ ] Firestore güvenlik kuralları (`firestore.rules`) repoya eklenmiş.

---

## 8. `.gitignore` (Zorunlu Girdiler)

```gitignore
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build/
/captures/
.externalNativeBuild
.cxx/
/app/google-services.json
app/build/
```

---

## 9. Notlar ve Kısıtlar

- **Kotlin 2.0 + Compose:** Compose derleyicisi `org.jetbrains.kotlin.plugin.compose`
  eklentisiyle gelir; `kotlinCompilerExtensionVersion` **kullanılmaz**.
- **KSP** Hilt ve Room için kullanılır (kapt değil).
- **Credential Manager** ile Google girişinde `clientId` olarak Firebase "Web istemci
  kimliği" gerekir; `strings.xml` içine `varsayilan_web_istemci_kimligi` olarak konur.
- **Firestore çevrimdışı kalıcılığı** Android'de varsayılan açıktır; Room ek katman
  olarak SSOT sağlar.
- Kod ajanı, bu belge setinde verilen kod parçalarını **iskelet** kabul edip eksik
  kalan gövdeleri (örn. `CarkCizimi` çizim detayı, `HavuzRepositoryImpl.launchYansit`)
  aynı sözleşmelere sadık kalarak tamamlamalıdır.

---

## 10. Özet Akış (Kod Ajanı İçin Tek Cümlelik Hatırlatıcı)

> "Türkçe isimlendirmeyle, Clean Architecture + MVVM + Hilt kullanarak; Firestore'u
> gerçek zamanlı dinleyip Room ile çevrimdışı destekleyen; ağırlıklı + zaman cezalı +
> TMDB/yemek önerili çark algoritmasını uygulayan; koyu/neon temalı, fizik tabanlı ve
> haptik/ses/konfeti destekli, tüm hataları `Sonuc` ile yakalayan bir Android uygulaması üret."
