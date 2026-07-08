# SonKarar — Repository, Domain ve Hata Yönetimi

> Bu döküman `Sonuc` hata sarmalayıcısını, Türkçe hata eşlemesini, repository
> sözleşmelerini ve somut uygulamalarını, ve UseCase'leri tanımlar.

---

## 1. `Sonuc` Sealed Class (Hata Yönetimi Çekirdeği)

Tüm işlemler bu sarmalayıcıyla döner. **İşlenmeyen istisna bırakılmaz.**

```kotlin
// cekirdek/Sonuc.kt
package com.sonkarar.cekirdek

sealed interface Sonuc<out T> {
    data class Basarili<out T>(val veri: T) : Sonuc<T>
    data class Hata(val mesaj: String, val istisna: Throwable? = null) : Sonuc<Nothing>
    data object Yukleniyor : Sonuc<Nothing>
}

inline fun <T, R> Sonuc<T>.donustur(donusum: (T) -> R): Sonuc<R> = when (this) {
    is Sonuc.Basarili -> Sonuc.Basarili(donusum(veri))
    is Sonuc.Hata -> this
    Sonuc.Yukleniyor -> Sonuc.Yukleniyor
}

inline fun <T> Sonuc<T>.basariliysa(islem: (T) -> Unit): Sonuc<T> {
    if (this is Sonuc.Basarili) islem(veri)
    return this
}
```

### 1.1. Güvenli çağrı yardımcısı

```kotlin
// cekirdek/HataYonetimi.kt
package com.sonkarar.cekirdek

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Bir istisnayı kullanıcıya gösterilecek Türkçe mesaja çevirir. */
fun Throwable.turkceMesaj(): String = when (this) {
    is FirebaseNetworkException ->
        "İnternet bağlantısı kurulamadı. Bağlantınızı kontrol edip tekrar deneyin."
    is IOException ->
        "Ağ hatası oluştu. Lütfen internet bağlantınızı kontrol edin."
    is FirebaseAuthException ->
        "Giriş yapılırken bir sorun oluştu. Lütfen tekrar deneyin."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Bu işlem için yetkiniz yok."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Sunucuya şu an ulaşılamıyor. Çevrimdışı verilerle devam ediliyor."
        else -> "Veri işlenirken bir hata oluştu. Lütfen tekrar deneyin."
    }
    else -> "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."
}

/** suspend blokları güvenle çalıştırır; CancellationException yeniden fırlatılır. */
suspend fun <T> guvenliCagri(blok: suspend () -> T): Sonuc<T> = try {
    Sonuc.Basarili(blok())
} catch (iptal: CancellationException) {
    throw iptal
} catch (hata: Throwable) {
    Sonuc.Hata(hata.turkceMesaj(), hata)
}
```

> **Kural:** `CancellationException` asla yutulmaz; coroutine iptali doğru çalışsın diye
> yeniden fırlatılır.

---

## 2. Repository Sözleşmeleri (`domain/repository`)

```kotlin
// domain/repository/KimlikRepository.kt
package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.Kullanici
import kotlinx.coroutines.flow.Flow

interface KimlikRepository {
    /** Aktif oturumu (ve eşleşme durumunu) gözlemler; oturum yoksa null yayar. */
    fun aktifKullaniciyiGozlemle(): Flow<Kullanici?>

    /** Google kimlik jetonu ile Firebase'e giriş yapar. */
    suspend fun googleIleGirisYap(kimlikJetonu: String): Sonuc<Kullanici>

    suspend fun oturumuKapat(): Sonuc<Unit>
}
```

```kotlin
// domain/repository/SinerjiRepository.kt
package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.Sinerji
import kotlinx.coroutines.flow.Flow

interface SinerjiRepository {
    /**
     * Eş e-postasıyla oda oluşturur veya var olan bekleyen odaya katılır.
     * Başarılıysa sinerjiId döner.
     */
    suspend fun odaOlusturVeyaKatil(esEposta: String): Sonuc<String>

    fun sinerjiyiGozlemle(sinerjiId: String): Flow<Sonuc<Sinerji>>

    suspend fun carkDurumunuGuncelle(sinerjiId: String, durum: CarkDurumu): Sonuc<Unit>

    suspend fun gecmiseKayitEkle(
        sinerjiId: String,
        kategori: Kategori,
        sonuc: String
    ): Sonuc<Unit>
}
```

```kotlin
// domain/repository/HavuzRepository.kt
package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.HavuzOgesi
import kotlinx.coroutines.flow.Flow

interface HavuzRepository {
    /** Room'u kaynak alan çevrimdışı-dayanıklı akış. */
    fun havuzuGozlemle(sinerjiId: String, kategori: Kategori): Flow<List<HavuzOgesi>>

    /** Firestore dinleyicisini başlatır; gelen veriyi Room'a yansıtır. */
    fun senkronizasyonuBaslat(sinerjiId: String, kategori: Kategori): Flow<Sonuc<Unit>>

    suspend fun ogeEkle(sinerjiId: String, oge: HavuzOgesi): Sonuc<Unit>

    suspend fun ogeSil(sinerjiId: String, ogeId: String): Sonuc<Unit>

    /** Anlık ağırlıklı çark hesabı için tüm kategoriyi tek seferlik getirir. */
    suspend fun kategoriyiGetir(sinerjiId: String, kategori: Kategori): Sonuc<List<HavuzOgesi>>
}
```

```kotlin
// domain/repository/OneriRepository.kt
package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.HavuzOgesi

interface OneriRepository {
    /**
     * Çiftin en sık tercih ettiği türlere göre [adet] kadar dış öneri üretir.
     * IZLENECEK -> TMDB, YEMEK -> hazır yemek etiketleri.
     * Hata durumunda boş liste döner (çark yine de çalışabilmeli).
     */
    suspend fun oneriUret(
        kategori: Kategori,
        populerTurler: List<String>,
        adet: Int
    ): Sonuc<List<HavuzOgesi>>
}
```

---

## 3. Somut Repository Uygulamaları

### 3.1. `KimlikRepositoryImpl`

```kotlin
// data/kimlik/KimlikRepositoryImpl.kt
package com.sonkarar.data.kimlik

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.data.firestore.dto.KullaniciDto
import com.sonkarar.domain.model.Kullanici
import com.sonkarar.domain.repository.KimlikRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KimlikRepositoryImpl @Inject constructor(
    private val kimlik: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : KimlikRepository {

    override fun aktifKullaniciyiGozlemle(): Flow<Kullanici?> = callbackFlow {
        val kullaniciBelgesiDinleyici = { uid: String ->
            firestore.collection(Sabitler.KOLEKSIYON_KULLANICILAR).document(uid)
                .addSnapshotListener { anlik, _ ->
                    val dto = anlik?.toObject(KullaniciDto::class.java)
                    trySend(
                        Kullanici(
                            kullaniciId = uid,
                            eposta = dto?.eposta ?: (kimlik.currentUser?.email ?: ""),
                            esEposta = dto?.esEposta ?: "",
                            sinerjiId = dto?.sinerjiId ?: ""
                        )
                    )
                }
        }

        var belgeDinleyici = kimlik.currentUser?.uid?.let(kullaniciBelgesiDinleyici)

        val oturumDinleyici = FirebaseAuth.AuthStateListener { auth ->
            belgeDinleyici?.remove()
            val uid = auth.currentUser?.uid
            belgeDinleyici = if (uid == null) {
                trySend(null); null
            } else {
                kullaniciBelgesiDinleyici(uid)
            }
        }
        kimlik.addAuthStateListener(oturumDinleyici)

        awaitClose {
            belgeDinleyici?.remove()
            kimlik.removeAuthStateListener(oturumDinleyici)
        }
    }

    override suspend fun googleIleGirisYap(kimlikJetonu: String): Sonuc<Kullanici> =
        guvenliCagri {
            val kimlikBilgisi = GoogleAuthProvider.getCredential(kimlikJetonu, null)
            val sonuc = kimlik.signInWithCredential(kimlikBilgisi).await()
            val kullanici = sonuc.user ?: error("Kullanıcı bilgisi alınamadı.")
            val belge = firestore.collection(Sabitler.KOLEKSIYON_KULLANICILAR)
                .document(kullanici.uid)
            val mevcut = belge.get().await()
            if (!mevcut.exists()) {
                belge.set(KullaniciDto(eposta = kullanici.email ?: "")).await()
            }
            Kullanici(
                kullaniciId = kullanici.uid,
                eposta = kullanici.email ?: "",
                sinerjiId = mevcut.getString("sinerjiId") ?: ""
            )
        }

    override suspend fun oturumuKapat(): Sonuc<Unit> = guvenliCagri {
        kimlik.signOut()
    }
}
```

### 3.2. `SinerjiRepositoryImpl` (eşleşme + senkron)

```kotlin
// data/firestore/SinerjiRepositoryImpl.kt (özet — kritik metotlar)
package com.sonkarar.data.firestore

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.cekirdek.turkceMesaj
import com.sonkarar.data.esleyici.domaineDonustur
import com.sonkarar.data.firestore.dto.CarkDurumuDto
import com.sonkarar.data.firestore.dto.CarkGecmisiKaydiDto
import com.sonkarar.data.firestore.dto.SinerjiDto
import com.google.firebase.auth.FirebaseAuth
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.Sinerji
import com.sonkarar.domain.repository.SinerjiRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SinerjiRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val kimlik: FirebaseAuth
) : SinerjiRepository {

    override suspend fun odaOlusturVeyaKatil(esEposta: String): Sonuc<String> =
        guvenliCagri {
            val benimUid = kimlik.currentUser?.uid ?: error("Önce giriş yapmalısınız.")
            val benimEposta = kimlik.currentUser?.email ?: ""
            val kullanicilar = firestore.collection(Sabitler.KOLEKSIYON_KULLANICILAR)

            // Karşı taraf beni eşi olarak işaretlediyse onun odasına katıl.
            val karsiTaraf = kullanicilar
                .whereEqualTo("eposta", esEposta.trim().lowercase())
                .whereEqualTo("esEposta", benimEposta.lowercase())
                .get().await().documents.firstOrNull()

            val sinerjiler = firestore.collection(Sabitler.KOLEKSIYON_SINERJILER)

            val sinerjiId: String
            val karsiSinerji = karsiTaraf?.getString("sinerjiId")
            if (karsiTaraf != null && !karsiSinerji.isNullOrBlank()) {
                sinerjiId = karsiSinerji
                sinerjiler.document(sinerjiId)
                    .update("uyeler", FieldValue.arrayUnion(benimUid)).await()
            } else {
                val yeniBelge = sinerjiler.document()
                sinerjiId = yeniBelge.id
                yeniBelge.set(SinerjiDto(uyeler = listOf(benimUid))).await()
            }

            kullanicilar.document(benimUid).update(
                mapOf(
                    "esEposta" to esEposta.trim().lowercase(),
                    "sinerjiId" to sinerjiId
                )
            ).await()
            sinerjiId
        }

    override fun sinerjiyiGozlemle(sinerjiId: String): Flow<Sonuc<Sinerji>> = callbackFlow {
        val dinleyici = firestore.collection(Sabitler.KOLEKSIYON_SINERJILER)
            .document(sinerjiId)
            .addSnapshotListener { anlik, hata ->
                if (hata != null) {
                    trySend(Sonuc.Hata(hata.turkceMesaj(), hata)); return@addSnapshotListener
                }
                val dto = anlik?.toObject(SinerjiDto::class.java)
                if (dto != null) {
                    trySend(Sonuc.Basarili(dto.domaineDonustur(sinerjiId)))
                }
            }
        awaitClose { dinleyici.remove() }
    }

    override suspend fun carkDurumunuGuncelle(
        sinerjiId: String,
        durum: CarkDurumu
    ): Sonuc<Unit> = guvenliCagri {
        firestore.collection(Sabitler.KOLEKSIYON_SINERJILER).document(sinerjiId)
            .update(
                "carkDurumu",
                CarkDurumuDto(
                    durum = durum.asama.name,
                    ceviren = durum.ceviren,
                    hedefAci = durum.hedefAci,
                    kazananIsim = durum.kazananIsim,
                    kategori = durum.kategori.name,
                    tur = durum.tur
                )
            ).await()
    }

    override suspend fun gecmiseKayitEkle(
        sinerjiId: String,
        kategori: Kategori,
        sonuc: String
    ): Sonuc<Unit> = guvenliCagri {
        val kayit = CarkGecmisiKaydiDto(
            zamanDamgasi = System.currentTimeMillis(),
            kategori = kategori.name,
            sonuc = sonuc
        )
        firestore.collection(Sabitler.KOLEKSIYON_SINERJILER).document(sinerjiId)
            .update("carkGecmisi", FieldValue.arrayUnion(kayit)).await()
    }
}
```

### 3.3. `HavuzRepositoryImpl` (Firestore → Room senkron)

```kotlin
// data/firestore/HavuzRepositoryImpl.kt (özet)
package com.sonkarar.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.cekirdek.turkceMesaj
import com.sonkarar.data.esleyici.domaineDonustur
import com.sonkarar.data.esleyici.dtoyaDonustur
import com.sonkarar.data.esleyici.varligaDonustur
import com.sonkarar.data.firestore.dto.HavuzOgesiDto
import com.sonkarar.data.yerel.HavuzDao
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.HavuzRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HavuzRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val havuzDao: HavuzDao
) : HavuzRepository {

    private fun havuzKoleksiyonu(sinerjiId: String) =
        firestore.collection(Sabitler.KOLEKSIYON_SINERJILER)
            .document(sinerjiId)
            .collection(Sabitler.ALT_KOLEKSIYON_HAVUZ)

    override fun havuzuGozlemle(sinerjiId: String, kategori: Kategori): Flow<List<HavuzOgesi>> =
        havuzDao.ogeleriGozlemle(sinerjiId, kategori.name)
            .map { liste -> liste.map { it.domaineDonustur() } }

    override fun senkronizasyonuBaslat(
        sinerjiId: String,
        kategori: Kategori
    ): Flow<Sonuc<Unit>> = callbackFlow {
        val dinleyici = havuzKoleksiyonu(sinerjiId)
            .whereEqualTo("kategori", kategori.name)
            .addSnapshotListener { anlik, hata ->
                if (hata != null) {
                    trySend(Sonuc.Hata(hata.turkceMesaj(), hata)); return@addSnapshotListener
                }
                val ogeler = anlik?.documents?.mapNotNull {
                    it.toObject(HavuzOgesiDto::class.java)?.domaineDonustur()
                } ?: emptyList()
                // Room'u tazele (SSOT). Basit yaklaşım: temizle + yaz.
                launchYansit(sinerjiId, kategori, ogeler)
                trySend(Sonuc.Basarili(Unit))
            }
        awaitClose { dinleyici.remove() }
    }

    private fun launchYansit(
        sinerjiId: String,
        kategori: Kategori,
        ogeler: List<HavuzOgesi>
    ) {
        // Not: callbackFlow üretici bağlamında suspend çağrı için ayrı bir
        // CoroutineScope kullanılır (uygulama tarafında @GDagitici ile enjekte edilir).
        // Basitlik için burada özetlenmiştir; tam uygulama repository'ye
        // enjekte edilen CoroutineScope ile Room'u günceller.
    }

    override suspend fun ogeEkle(sinerjiId: String, oge: HavuzOgesi): Sonuc<Unit> =
        guvenliCagri {
            val belge = havuzKoleksiyonu(sinerjiId).document()
            val kalici = oge.copy(id = belge.id)
            belge.set(kalici.dtoyaDonustur()).await()
        }

    override suspend fun ogeSil(sinerjiId: String, ogeId: String): Sonuc<Unit> =
        guvenliCagri {
            havuzKoleksiyonu(sinerjiId).document(ogeId).delete().await()
        }

    override suspend fun kategoriyiGetir(
        sinerjiId: String,
        kategori: Kategori
    ): Sonuc<List<HavuzOgesi>> = guvenliCagri {
        havuzKoleksiyonu(sinerjiId)
            .whereEqualTo("kategori", kategori.name)
            .get().await().documents.mapNotNull {
                it.toObject(HavuzOgesiDto::class.java)?.domaineDonustur()
            }
    }
}
```

> **Uygulama notu (kod ajanı için):** `HavuzRepositoryImpl`, Room güncellemesini
> yapabilmek için constructor'a `@GDagitici dagitici: CoroutineDispatcher` ve bir
> `CoroutineScope` enjekte etmelidir. Snapshot geldiğinde
> `scope.launch(dagitici) { havuzDao.kategoriyiTemizle(...); havuzDao.ogeleriYaz(...) }`
> çağrılır. Yukarıdaki `launchYansit` bu mantığı temsil eder ve tam kodda
> doldurulmalıdır.

---

## 4. UseCase'ler (`domain/kullanim`)

UseCase deseni: tek public `operator fun invoke(...)`.

```kotlin
// domain/kullanim/HavuzaOgeEkleKullanimi.kt
package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.HavuzRepository
import javax.inject.Inject

class HavuzaOgeEkleKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository
) {
    suspend operator fun invoke(
        sinerjiId: String,
        kategori: Kategori,
        isim: String,
        ekleyenKullanici: String
    ): Sonuc<Unit> {
        val temiz = isim.trim()
        if (temiz.isBlank()) {
            return Sonuc.Hata("Lütfen bir isim girin.")
        }
        val oge = HavuzOgesi(
            id = "",
            kategori = kategori,
            isim = temiz,
            ekleyenKullanici = ekleyenKullanici,
            agirlik = Sabitler.VARSAYILAN_AGIRLIK,
            disOneriMi = false
        )
        return havuzRepository.ogeEkle(sinerjiId, oge)
    }
}
```

```kotlin
// domain/kullanim/HavuzuGozlemleKullanimi.kt
package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.HavuzRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HavuzuGozlemleKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository
) {
    operator fun invoke(sinerjiId: String, kategori: Kategori): Flow<List<HavuzOgesi>> =
        havuzRepository.havuzuGozlemle(sinerjiId, kategori)
}
```

Diğer UseCase'ler benzer desende yazılır:

| UseCase | Sorumluluk |
| --- | --- |
| `GoogleIleGirisYapKullanimi` | Kimlik jetonuyla giriş |
| `OturumuKapatKullanimi` | Çıkış |
| `AktifKullaniciyiGozlemleKullanimi` | Oturum + eşleşme akışı |
| `OdaOlusturVeyaKatilKullanimi` | Eşleşme |
| `HavuzuGozlemleKullanimi` | Kategoriye göre havuz akışı |
| `HavuzaOgeEkleKullanimi` | Doğrulama + ekleme |
| `HavuzdanOgeSilKullanimi` | Silme |
| `CarkDurumunuGozlemleKullanimi` | Senkron durum akışı |
| `CarkiCevirKullanimi` | Ana algoritma (bkz. `05` belge) |
| `AgirlikliSecimYap` | Saf ağırlıklı seçim (bkz. `05` belge) |

> `CarkiCevirKullanimi` ve `AgirlikliSecimYap` detayları bir sonraki dökümandadır.
