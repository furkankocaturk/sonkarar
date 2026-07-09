# SonKarar — Algoritmalar ve İş Mantığı

> Bu döküman ağırlıklı rastgele çark seçimini, zaman cezasını, dış öneri
> enjeksiyonunu (TMDB + yemek etiketleri) ve gerçek zamanlı senkronizasyon
> protokolünü **çalışır Kotlin kodu** ile tanımlar.

---

## 1. Ağırlıklı Rastgele Seçim (Saf Fonksiyon)

Test edilebilirlik için seçim algoritması Android/Firebase'den bağımsız, saf bir
sınıftır. Rastgelelik dışarıdan enjekte edilir (deterministik test için).

```kotlin
// domain/kullanim/AgirlikliSecimYap.kt
package com.sonkarar.domain.kullanim

import com.sonkarar.domain.model.HavuzOgesi
import kotlin.random.Random
import javax.inject.Inject

class AgirlikliSecimYap @Inject constructor() {

    /**
     * [ogeler] listesinden ağırlıkla orantılı biçimde rastgele bir öge seçer.
     * Ağırlıklar negatif olamaz; en az 1 olacak biçimde tabanlanır.
     * Boş liste durumunda null döner.
     */
    operator fun invoke(
        ogeler: List<HavuzOgesi>,
        rastgele: Random = Random.Default
    ): HavuzOgesi? {
        if (ogeler.isEmpty()) return null

        val guvenliAgirliklar = ogeler.map { it.agirlik.coerceAtLeast(1) }
        val toplam = guvenliAgirliklar.sum()
        if (toplam <= 0) return ogeler.random(rastgele)

        // 1..toplam arasında bir eşik seç; kümülatif toplamla eşleştir.
        var esik = rastgele.nextInt(1, toplam + 1)
        for (indeks in ogeler.indices) {
            esik -= guvenliAgirliklar[indeks]
            if (esik <= 0) return ogeler[indeks]
        }
        return ogeler.last()
    }
}
```

### 1.1. Çarkın açısal karşılığı

Seçilen ögenin çarktaki dilim indeksinden **hedef açı** hesaplanır. Çark bu açıda
durur; iki cihaz da aynı `hedefAci` değerini Firestore'dan okur (bkz. senkronizasyon).

```kotlin
// Seçilen indeks için hedef açı (derece). Çark saat yönünde döner,
// gösterge tepede (12 yönü) kabul edilir.
fun hedefAciHesapla(secilenIndeks: Int, toplamDilim: Int, turSayisi: Int = 5): Double {
    if (toplamDilim <= 0) return 0.0
    val dilimAcisi = 360.0 / toplamDilim
    val dilimMerkezi = secilenIndeks * dilimAcisi + dilimAcisi / 2.0
    // Göstergeye hizalamak için 360'tan çıkar; birkaç tam tur ekle.
    return turSayisi * 360.0 + (360.0 - dilimMerkezi)
}
```

---

## 2. Zaman Cezası

`carkGecmisi` içindeki **son 3 tur** (kategori bazında) kontrol edilir. Bu turlarda
çıkan ögelerin ağırlığı **%80 düşürülür** (ağırlığın yalnızca %20'si kalır).

```kotlin
// domain/kullanim/ZamanCezasiUygula.kt (CarkiCevirKullanimi içinde de olabilir)
package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.domain.model.HavuzOgesi
import kotlin.math.roundToInt

object ZamanCezasi {
    /**
     * Son [Sabitler.ZAMAN_CEZASI_TUR_SAYISI] turda ([kategori] için) çıkan
     * ögelerin ağırlığını [Sabitler.ZAMAN_CEZASI_ORANI] katına indirir (%80 düşür).
     */
    fun uygula(
        ogeler: List<HavuzOgesi>,
        gecmis: List<CarkGecmisiKaydi>,
        kategori: Kategori
    ): List<HavuzOgesi> {
        val sonSonuclar = gecmis
            .filter { it.kategori == kategori }
            .sortedByDescending { it.zamanDamgasi }
            .take(Sabitler.ZAMAN_CEZASI_TUR_SAYISI)
            .map { it.sonuc }
            .toSet()

        if (sonSonuclar.isEmpty()) return ogeler

        return ogeler.map { oge ->
            if (oge.isim in sonSonuclar) {
                val cezali = (oge.agirlik * Sabitler.ZAMAN_CEZASI_ORANI).roundToInt()
                oge.copy(agirlik = cezali.coerceAtLeast(1))
            } else {
                oge
            }
        }
    }
}
```

---

## 3. Dış Öneri Enjeksiyonu

### 3.1. En popüler türlerin tespiti

Çiftin havuzda **en çok tercih ettiği türler**, havuzdaki ögelerin `tur` alanının
frekansından çıkarılır.

```kotlin
fun populerTurleriBul(ogeler: List<HavuzOgesi>, adet: Int = 3): List<String> =
    ogeler
        .map { it.tur }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(adet)
        .map { it.key }
```

### 3.2. TMDB Retrofit Servisi (İZLENECEK)

```kotlin
// data/uzak/TmdbServisi.kt
package com.sonkarar.data.uzak

import com.sonkarar.data.uzak.dto.TmdbYanitDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbServisi {
    // Türe göre keşif; tür kimlikleri TMDB'nin sabit tür kimlikleridir.
    @GET("discover/movie")
    suspend fun filmKesfet(
        @Query("api_key") apiAnahtari: String,
        @Query("with_genres") turKimlikleri: String,
        @Query("language") dil: String = "tr-TR",
        @Query("sort_by") siralama: String = "popularity.desc",
        @Query("page") sayfa: Int = 1
    ): TmdbYanitDto
}
```

```kotlin
// data/uzak/dto/TmdbYanitDto.kt
package com.sonkarar.data.uzak.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbYanitDto(
    @SerialName("results") val sonuclar: List<TmdbFilmDto> = emptyList()
)

@Serializable
data class TmdbFilmDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val baslik: String = "",
    @SerialName("genre_ids") val turKimlikleri: List<Int> = emptyList(),
    @SerialName("poster_path") val posterYolu: String? = null
)
```

### 3.3. Öneri Repository Uygulaması

```kotlin
// data/uzak/OneriRepositoryImpl.kt
package com.sonkarar.data.uzak

import com.sonkarar.BuildConfig
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.di.GDagitici
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.OneriRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OneriRepositoryImpl @Inject constructor(
    private val tmdbServisi: TmdbServisi,
    @GDagitici private val dagitici: CoroutineDispatcher
) : OneriRepository {

    // Türkçe tür adı -> TMDB tür kimliği (yaygın türler)
    private val turKimlikHaritasi = mapOf(
        "Bilim Kurgu" to 878,
        "Komedi" to 35,
        "Dram" to 18,
        "Aksiyon" to 28,
        "Korku" to 27,
        "Romantik" to 10749,
        "Animasyon" to 16,
        "Gerilim" to 53
    )

    // TMDB olmadan da çalışabilmek için hazır yemek önerileri.
    private val hazirYemekOnerileri = mapOf(
        "Uzak Doğu" to listOf("Suşi", "Ramen", "Pad Thai"),
        "İtalyan" to listOf("Makarna", "Pizza", "Risotto"),
        "Türk" to listOf("Mantı", "Kebap", "Pide"),
        "Fast Food" to listOf("Hamburger", "Tavuk Dürüm", "Nachos"),
        "Deniz Ürünleri" to listOf("Kalamar", "Levrek", "Midye Dolma")
    )

    override suspend fun oneriUret(
        kategori: Kategori,
        populerTurler: List<String>,
        adet: Int
    ): Sonuc<List<HavuzOgesi>> = when (kategori) {
        Kategori.IZLENECEK -> tmdbOnerileriUret(populerTurler, adet)
        Kategori.YEMEK -> Sonuc.Basarili(yemekOnerileriUret(populerTurler, adet))
    }

    private suspend fun tmdbOnerileriUret(
        populerTurler: List<String>,
        adet: Int
    ): Sonuc<List<HavuzOgesi>> {
        // Anahtar yoksa sessizce boş dön; çark yerel havuzla çalışmaya devam eder.
        if (BuildConfig.TMDB_API_ANAHTARI.isBlank()) return Sonuc.Basarili(emptyList())

        return guvenliCagri {
            withContext(dagitici) {
                val turKimlikleri = populerTurler
                    .mapNotNull { turKimlikHaritasi[it] }
                    .joinToString(",")
                    .ifBlank { "878" } // varsayılan: Bilim Kurgu

                val yanit = tmdbServisi.filmKesfet(
                    apiAnahtari = BuildConfig.TMDB_API_ANAHTARI,
                    turKimlikleri = turKimlikleri
                )
                yanit.sonuclar
                    .filter { it.baslik.isNotBlank() }
                    .shuffled()
                    .take(adet)
                    .map { film ->
                        HavuzOgesi(
                            id = "oneri_${UUID.randomUUID()}",
                            kategori = Kategori.IZLENECEK,
                            isim = film.baslik,
                            tur = populerTurler.firstOrNull().orEmpty(),
                            ekleyenKullanici = "sistem",
                            agirlik = Sabitler.VARSAYILAN_AGIRLIK,
                            disOneriMi = true
                        )
                    }
            }
        }
    }

    private fun yemekOnerileriUret(populerTurler: List<String>, adet: Int): List<HavuzOgesi> {
        val kaynak = if (populerTurler.isNotEmpty()) {
            populerTurler.flatMap { hazirYemekOnerileri[it].orEmpty() }
        } else {
            hazirYemekOnerileri.values.flatten()
        }.ifEmpty { hazirYemekOnerileri.values.flatten() }

        return kaynak.shuffled().take(adet).map { isim ->
            HavuzOgesi(
                id = "oneri_${UUID.randomUUID()}",
                kategori = Kategori.YEMEK,
                isim = isim,
                tur = populerTurler.firstOrNull().orEmpty(),
                ekleyenKullanici = "sistem",
                agirlik = Sabitler.VARSAYILAN_AGIRLIK,
                disOneriMi = true
            )
        }
    }
}
```

> **Kararlılık kuralı:** TMDB çağrısı başarısız olursa (`Sonuc.Hata`), `CarkiCevirKullanimi`
> bunu **yumuşak hata** kabul eder ve öneri listesini boş sayar; çark yerel havuzla
> mutlaka çalışır. Öneri enjeksiyonu asla çark akışını çökertmez.

---

## 4. Ana İş Akışı: `CarkiCevirKullanimi`

Adım adım PRD Bölüm 4.A'daki mantığı uygular.

```kotlin
// domain/kullanim/CarkiCevirKullanimi.kt
package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.HavuzRepository
import com.sonkarar.domain.repository.OneriRepository
import javax.inject.Inject
import kotlin.random.Random

/** Çark çevirme hesabının çıktısı: nihai liste, kazanan ve hedef açı. */
data class CarkSonucu(
    val nihaiListe: List<HavuzOgesi>,
    val kazanan: HavuzOgesi,
    val kazananIndeks: Int,
    val hedefAci: Double
)

class CarkiCevirKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository,
    private val oneriRepository: OneriRepository,
    private val agirlikliSecimYap: AgirlikliSecimYap
) {
    suspend operator fun invoke(
        sinerjiId: String,
        kategori: Kategori,
        carkGecmisi: List<CarkGecmisiKaydi>,
        rastgele: Random = Random.Default
    ): Sonuc<CarkSonucu> {
        // 1) Havuzu çek
        val havuzSonuc = havuzRepository.kategoriyiGetir(sinerjiId, kategori)
        val temelHavuz = when (havuzSonuc) {
            is Sonuc.Basarili -> havuzSonuc.veri
            is Sonuc.Hata -> return havuzSonuc
            Sonuc.Yukleniyor -> return Sonuc.Yukleniyor
        }

        // 2) Zaman cezası uygula
        val cezaliHavuz = ZamanCezasi.uygula(temelHavuz, carkGecmisi, kategori)

        // 3) Dış öneri enjeksiyonu (yumuşak hata: başarısızsa boş liste)
        val populerTurler = populerTurleriBul(temelHavuz)
        val oneriSonuc = oneriRepository.oneriUret(
            kategori = kategori,
            populerTurler = populerTurler,
            adet = Sabitler.ENJEKTE_EDILECEK_ONERI_SAYISI
        )
        val oneriler = (oneriSonuc as? Sonuc.Basarili)?.veri ?: emptyList()

        val nihaiListe = cezaliHavuz + oneriler
        if (nihaiListe.isEmpty()) {
            return Sonuc.Hata("Havuzda hiç seçenek yok. Önce havuza içerik ekleyin.")
        }

        // 4) Ağırlıklı rastgele seçim
        val kazanan = agirlikliSecimYap(nihaiListe, rastgele)
            ?: return Sonuc.Hata("Seçim yapılamadı. Lütfen tekrar deneyin.")

        val kazananIndeks = nihaiListe.indexOf(kazanan)
        val hedefAci = hedefAciHesapla(kazananIndeks, nihaiListe.size)

        return Sonuc.Basarili(
            CarkSonucu(
                nihaiListe = nihaiListe,
                kazanan = kazanan,
                kazananIndeks = kazananIndeks,
                hedefAci = hedefAci
            )
        )
    }
}
```

---

## 5. Gerçek Zamanlı Senkronizasyon Protokolü

İki cihaz `sinerjiler/{sinerjiId}` belgesindeki `carkDurumu` alanını dinler. Protokol
şu şekildedir:

### 5.1. Durum Makinesi

```
        çevir()                animasyon biter
BOSTA ───────────► CEVRILIYOR ───────────────► SONUC ──(3sn)──► BOSTA
```

### 5.2. Adımlar

1. **Çeviren cihaz** `CarkiCevirKullanimi` ile kazananı ve `hedefAci`'yı hesaplar.
2. Çeviren cihaz Firestore'a şunu yazar:
   ```
   carkDurumu = {
     durum: "CEVRILIYOR",
     ceviren: <benimUid>,
     hedefAci: <hesaplanan>,
     kazananIsim: <kazanan.isim>,
     kategori: <kategori>,
     tur: <önceki tur + 1>      // tur artışı dinleyiciyi kesin tetikler
   }
   ```
3. **Her iki cihaz** da snapshot dinleyicisinden yeni `tur` değerini görür ve
   çark animasyonunu **aynı `hedefAci`** ile başlatır. Diğer cihazda
   *"Eşiniz çarkı çeviriyor..."* metni gösterilir.
4. Animasyon süresi sabittir (örn. 4 sn). Süre sonunda her iki cihaz da `SONUC`
   aşamasına geçer, konfeti efekti tetiklenir ve kazanan gösterilir.
5. Çeviren cihaz `SONUC` aşamasında `gecmiseKayitEkle(...)` çağrısı ile
   `carkGecmisi`'ne kaydı **bir kez** ekler (yalnızca `ceviren == benimUid` ise —
   çift kayıt engellenir).
6. Kısa süre sonra (3 sn) çeviren cihaz durumu `BOSTA`'ya döndürür.

### 5.3. Eş Zamanlılık Notları

- `tur` alanı monotonik artan sayaçtır; iki cihazın aynı anda çevirmesi
  durumunda Firestore'un `update` sıralaması son yazanı kazandırır. UI, gelen son
  `tur` değerini esas alır (idempotent animasyon tetikleme).
- Animasyon süresi ve easing (yavaşlama eğrisi) **iki cihazda birebir aynı**
  olmalıdır ki sonuç aynı saniyede görünsün (bkz. `06` belge, `CarkCizimi`).
- Ağ gecikmesi < 1 sn hedeflenir; Firestore gerçek zamanlı dinleyicileri bunu
  tipik olarak karşılar.

### 5.4. Kayıt Tekilleştirme (idempotency)

`CarkViewModel`, işlenen en son `tur` değerini bellekte tutar. Aynı `tur` için
animasyon ve geçmiş kaydı **tekrar tetiklenmez**.

```kotlin
// CarkViewModel içinde (özet)
private var islenenTur: Long = -1L

private fun carkDurumunuIsle(durum: CarkDurumu, benimUid: String) {
    if (durum.tur == islenenTur) return          // idempotent
    islenenTur = durum.tur
    if (durum.asama == CarkAsamasi.CEVRILIYOR) {
        animasyonuBaslat(durum.hedefAci, durum.kazananIsim)
        // Geçmiş kaydını yalnızca çeviren cihaz ekler
        if (durum.ceviren == benimUid) {
            gecmiseKaydiEklePlanla(durum.kategori, durum.kazananIsim)
        }
    }
}
```
