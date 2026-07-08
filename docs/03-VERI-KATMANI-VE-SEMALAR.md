# SonKarar — Veri Katmanı ve Şemalar

> Bu döküman Firestore koleksiyon şemasını, Room yerel şemasını, DTO/domain
> modellerini, eşleyicileri ve Firestore güvenlik kurallarını tanımlar. Tüm alan
> isimleri **Türkçe**dir.

---

## 1. Firestore Koleksiyon Şeması

### 1.1. `/kullanicilar/{kullaniciId}`

| Alan | Tip | Açıklama |
| --- | --- | --- |
| `eposta` | String | Kullanıcının e-postası |
| `esEposta` | String | Eşinin girdiği e-posta (bekleme/eşleşme için) |
| `sinerjiId` | String | Eşleşen çiftlerin ortak oda kimliği (boşsa henüz eşleşmemiş) |

> `{kullaniciId}` = Firebase Auth `uid`.

### 1.2. `/sinerjiler/{sinerjiId}`

| Alan | Tip | Açıklama |
| --- | --- | --- |
| `uyeler` | Dizi\<String> | `[kullaniciId1, kullaniciId2]` |
| `carkGecmisi` | Dizi\<Harita> | `{ zamanDamgasi: Long, kategori: String, sonuc: String }` |
| `carkDurumu` | Harita | Gerçek zamanlı senkronizasyon durumu (aşağıda) |

`carkDurumu` alt yapısı:

| Alan | Tip | Açıklama |
| --- | --- | --- |
| `durum` | String | `"BOSTA"`, `"CEVRILIYOR"`, `"SONUC"` |
| `ceviren` | String | Çarkı başlatan `kullaniciId` |
| `hedefAci` | Double | Çarkın duracağı açı (senkron animasyon için) |
| `kazananIsim` | String | Sonuç ögesinin ismi |
| `kategori` | String | `"YEMEK"` / `"IZLENECEK"` |
| `tur` | Long | Artan tur sayacı (her çevirmede +1; dinleyici tetikleyici) |

### 1.3. `/sinerjiler/{sinerjiId}/havuz/{ogeId}`

| Alan | Tip | Varsayılan | Açıklama |
| --- | --- | --- | --- |
| `id` | String | — | Belge kimliği ile aynı |
| `kategori` | String | — | `"YEMEK"` veya `"IZLENECEK"` |
| `isim` | String | — | Örn: "Suşi", "Inception" |
| `tur` | String | `""` | Örn: "Uzak Doğu", "Bilim Kurgu" |
| `ekleyenKullanici` | String | — | `kullaniciId` |
| `agirlik` | Int | `10` | Ağırlıklı seçimde temel ağırlık |
| `disOneriMi` | Boolean | `false` | Sistem önerisi mi (TMDB/etiket) |

> **Not:** `disOneriMi = true` olan anlık enjekte edilen öneriler Firestore'a
> kalıcı yazılmaz; yalnızca çark hesaplaması sırasında bellek içinde üretilir.
> Kullanıcı bir öneriyi "beğenip kalıcı ekle" derse `disOneriMi = false` ile yazılır
> (opsiyonel gelişmiş özellik; MVP'de anlık enjeksiyon yeterlidir).

---

## 2. Domain Modelleri (Saf Kotlin)

```kotlin
// domain/model/Kullanici.kt
package com.sonkarar.domain.model

data class Kullanici(
    val kullaniciId: String,
    val eposta: String,
    val esEposta: String = "",
    val sinerjiId: String = ""
) {
    val eslesmisMi: Boolean get() = sinerjiId.isNotBlank()
}
```

```kotlin
// domain/model/HavuzOgesi.kt
package com.sonkarar.domain.model

import com.sonkarar.cekirdek.Kategori

data class HavuzOgesi(
    val id: String,
    val kategori: Kategori,
    val isim: String,
    val tur: String = "",
    val ekleyenKullanici: String,
    val agirlik: Int = 10,
    val disOneriMi: Boolean = false
)
```

```kotlin
// domain/model/CarkGecmisiKaydi.kt
package com.sonkarar.domain.model

import com.sonkarar.cekirdek.Kategori

data class CarkGecmisiKaydi(
    val zamanDamgasi: Long,
    val kategori: Kategori,
    val sonuc: String
)
```

```kotlin
// domain/model/Sinerji.kt
package com.sonkarar.domain.model

data class Sinerji(
    val sinerjiId: String,
    val uyeler: List<String> = emptyList(),
    val carkGecmisi: List<CarkGecmisiKaydi> = emptyList(),
    val carkDurumu: CarkDurumu = CarkDurumu()
)
```

```kotlin
// domain/model/CarkDurumu.kt
package com.sonkarar.domain.model

import com.sonkarar.cekirdek.Kategori

enum class CarkAsamasi { BOSTA, CEVRILIYOR, SONUC }

data class CarkDurumu(
    val asama: CarkAsamasi = CarkAsamasi.BOSTA,
    val ceviren: String = "",
    val hedefAci: Double = 0.0,
    val kazananIsim: String = "",
    val kategori: Kategori = Kategori.YEMEK,
    val tur: Long = 0L
)
```

---

## 3. Firestore DTO'ları

Firestore, no-arg constructor ve `var` alanlar ister. DTO'lar `data` katmanındadır.

```kotlin
// data/firestore/dto/KullaniciDto.kt
package com.sonkarar.data.firestore.dto

data class KullaniciDto(
    val eposta: String = "",
    val esEposta: String = "",
    val sinerjiId: String = ""
)
```

```kotlin
// data/firestore/dto/HavuzOgesiDto.kt
package com.sonkarar.data.firestore.dto

data class HavuzOgesiDto(
    val id: String = "",
    val kategori: String = "YEMEK",
    val isim: String = "",
    val tur: String = "",
    val ekleyenKullanici: String = "",
    val agirlik: Int = 10,
    val disOneriMi: Boolean = false
)
```

```kotlin
// data/firestore/dto/SinerjiDto.kt
package com.sonkarar.data.firestore.dto

data class CarkGecmisiKaydiDto(
    val zamanDamgasi: Long = 0L,
    val kategori: String = "YEMEK",
    val sonuc: String = ""
)

data class CarkDurumuDto(
    val durum: String = "BOSTA",
    val ceviren: String = "",
    val hedefAci: Double = 0.0,
    val kazananIsim: String = "",
    val kategori: String = "YEMEK",
    val tur: Long = 0L
)

data class SinerjiDto(
    val uyeler: List<String> = emptyList(),
    val carkGecmisi: List<CarkGecmisiKaydiDto> = emptyList(),
    val carkDurumu: CarkDurumuDto = CarkDurumuDto()
)
```

---

## 4. Room Yerel Şeması (Çevrimdışı Havuz)

```kotlin
// data/yerel/varlik/HavuzOgesiVarligi.kt
package com.sonkarar.data.yerel.varlik

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "havuz_ogeleri")
data class HavuzOgesiVarligi(
    @PrimaryKey val id: String,
    val sinerjiId: String,
    val kategori: String,
    val isim: String,
    val tur: String,
    val ekleyenKullanici: String,
    val agirlik: Int,
    val disOneriMi: Boolean
)
```

```kotlin
// data/yerel/HavuzDao.kt
package com.sonkarar.data.yerel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi
import kotlinx.coroutines.flow.Flow

@Dao
interface HavuzDao {

    @Query("SELECT * FROM havuz_ogeleri WHERE sinerjiId = :sinerjiId AND kategori = :kategori")
    fun ogeleriGozlemle(sinerjiId: String, kategori: String): Flow<List<HavuzOgesiVarligi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ogeleriYaz(ogeler: List<HavuzOgesiVarligi>)

    @Query("DELETE FROM havuz_ogeleri WHERE sinerjiId = :sinerjiId AND kategori = :kategori")
    suspend fun kategoriyiTemizle(sinerjiId: String, kategori: String)

    @Query("DELETE FROM havuz_ogeleri WHERE id = :ogeId")
    suspend fun ogeSil(ogeId: String)
}
```

```kotlin
// data/yerel/SonKararVeriTabani.kt
package com.sonkarar.data.yerel

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi

@Database(
    entities = [HavuzOgesiVarligi::class],
    version = 1,
    exportSchema = false
)
abstract class SonKararVeriTabani : RoomDatabase() {
    abstract fun havuzDao(): HavuzDao
}
```

> **Senkronizasyon stratejisi:** `HavuzRepositoryImpl`, Firestore `havuz`
> alt-koleksiyonunu snapshot listener ile dinler; her güncellemede ilgili kategoriyi
> Room'da `kategoriyiTemizle` + `ogeleriYaz` ile tazeler. UI, **Room Flow'unu**
> gözlemler. Böylece çevrimdışıyken son bilinen havuz görüntülenir.

---

## 5. Eşleyiciler (DTO ↔ Domain ↔ Varlık)

```kotlin
// data/esleyici/HavuzEsleyici.kt
package com.sonkarar.data.esleyici

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.data.firestore.dto.HavuzOgesiDto
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi
import com.sonkarar.domain.model.HavuzOgesi

fun HavuzOgesiDto.domaineDonustur(): HavuzOgesi = HavuzOgesi(
    id = id,
    kategori = Kategori.anahtardan(kategori),
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi
)

fun HavuzOgesi.dtoyaDonustur(): HavuzOgesiDto = HavuzOgesiDto(
    id = id,
    kategori = kategori.name,
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi
)

fun HavuzOgesi.varligaDonustur(sinerjiId: String): HavuzOgesiVarligi = HavuzOgesiVarligi(
    id = id,
    sinerjiId = sinerjiId,
    kategori = kategori.name,
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi
)

fun HavuzOgesiVarligi.domaineDonustur(): HavuzOgesi = HavuzOgesi(
    id = id,
    kategori = Kategori.anahtardan(kategori),
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi
)
```

```kotlin
// data/esleyici/SinerjiEsleyici.kt
package com.sonkarar.data.esleyici

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.data.firestore.dto.CarkDurumuDto
import com.sonkarar.data.firestore.dto.CarkGecmisiKaydiDto
import com.sonkarar.data.firestore.dto.SinerjiDto
import com.sonkarar.domain.model.CarkAsamasi
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.domain.model.Sinerji

fun SinerjiDto.domaineDonustur(sinerjiId: String): Sinerji = Sinerji(
    sinerjiId = sinerjiId,
    uyeler = uyeler,
    carkGecmisi = carkGecmisi.map { it.domaineDonustur() },
    carkDurumu = carkDurumu.domaineDonustur()
)

fun CarkGecmisiKaydiDto.domaineDonustur(): CarkGecmisiKaydi = CarkGecmisiKaydi(
    zamanDamgasi = zamanDamgasi,
    kategori = Kategori.anahtardan(kategori),
    sonuc = sonuc
)

fun CarkDurumuDto.domaineDonustur(): CarkDurumu = CarkDurumu(
    asama = runCatching { CarkAsamasi.valueOf(durum) }.getOrDefault(CarkAsamasi.BOSTA),
    ceviren = ceviren,
    hedefAci = hedefAci,
    kazananIsim = kazananIsim,
    kategori = Kategori.anahtardan(kategori),
    tur = tur
)
```

---

## 6. Firestore Güvenlik Kuralları

`firestore.rules` (Firebase konsoluna yüklenir):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Kullanıcı yalnızca kendi belgesini okuyup yazabilir
    match /kullanicilar/{kullaniciId} {
      allow read, write: if request.auth != null && request.auth.uid == kullaniciId;
    }

    // Sinerji odasına yalnızca üyeler erişebilir
    match /sinerjiler/{sinerjiId} {
      allow read, write: if request.auth != null
        && request.auth.uid in resource.data.uyeler;
      // Yeni oda oluşturulurken kurucu üye listesinde olmalı
      allow create: if request.auth != null
        && request.auth.uid in request.resource.data.uyeler;

      match /havuz/{ogeId} {
        allow read, write: if request.auth != null
          && request.auth.uid in get(/databases/$(database)/documents/sinerjiler/$(sinerjiId)).data.uyeler;
      }
    }
  }
}
```

---

## 7. Firestore Dizinleri (Index)

MVP sorguları eşitlik filtreleri kullandığından bileşik dizin gerekmez. İleride
`kategori` + `disOneriMi` sıralaması eklenirse Firebase konsolunun önerdiği bileşik
dizin oluşturulmalıdır.
