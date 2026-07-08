# SonKarar — Proje Yapısı ve Mimari

> Bu döküman, Clean Architecture temelli paket yapısını, katman sorumluluklarını
> ve Hilt bağımlılık enjeksiyonunu tanımlar. Tüm paket ve sınıf isimleri Türkçedir.

---

## 1. Mimari Genel Bakış

Uygulama tek Gradle modüllüdür (`:app`) fakat **katmanları paketlerle** ayrılır.
Bağımlılık yönü daima içe (domain'e) doğrudur:

```
  presentation  ─────►  domain  ◄─────  data
   (Compose,             (saf Kotlin,      (Firestore, Room,
    ViewModel)            iş kuralları)      TMDB, eşleyiciler)
```

**Kurallar:**

- `domain` katmanı hiçbir Android/Firebase/Room sınıfına bağımlı **olamaz** (saf Kotlin).
- `data` ve `presentation`, `domain`'e bağımlıdır; birbirlerine doğrudan bağımlı değildir.
- Veri akışı tek yönlüdür (UDF): `ViewModel` → `UseCase` → `Repository` → `Kaynak`.
- UI durumu tek bir immutable `data class` ile temsil edilir (`...ArayuzDurumu`).

---

## 2. Paket Yapısı

Ana paket: `com.sonkarar`

```
com.sonkarar
├── SonKararUygulamasi.kt                # @HiltAndroidApp Application
├── MainActivity.kt                      # @AndroidEntryPoint tek Activity
│
├── cekirdek/                            # Ortak yardımcılar (katmanlar arası)
│   ├── Sonuc.kt                         # Sealed class: Basarili / Hata / Yukleniyor
│   ├── HataYonetimi.kt                  # İstisna -> Türkçe mesaj eşlemesi
│   ├── Kategori.kt                      # enum: YEMEK, IZLENECEK
│   └── Sabitler.kt                      # Koleksiyon adları, varsayılan değerler
│
├── domain/                             # SAF KOTLIN — Android'e bağımlı DEĞİL
│   ├── model/
│   │   ├── Kullanici.kt
│   │   ├── Sinerji.kt
│   │   ├── HavuzOgesi.kt
│   │   ├── CarkGecmisiKaydi.kt
│   │   └── CarkDurumu.kt                # Senkronizasyon durumu modeli
│   ├── repository/                      # SÖZLEŞMELER (arayüzler)
│   │   ├── KimlikRepository.kt
│   │   ├── SinerjiRepository.kt
│   │   ├── HavuzRepository.kt
│   │   └── OneriRepository.kt
│   └── kullanim/                        # UseCase'ler (tek sorumluluk)
│       ├── GoogleIleGirisYapKullanimi.kt
│       ├── OturumuKapatKullanimi.kt
│       ├── AktifKullaniciyiGozlemleKullanimi.kt
│       ├── OdaOlusturVeyaKatilKullanimi.kt
│       ├── HavuzuGozlemleKullanimi.kt
│       ├── HavuzaOgeEkleKullanimi.kt
│       ├── HavuzdanOgeSilKullanimi.kt
│       ├── CarkDurumunuGozlemleKullanimi.kt
│       ├── CarkiCevirKullanimi.kt        # Ağırlıklı seçim + ceza + enjeksiyon
│       └── AgirlikliSecimYap.kt          # Saf algoritma (test edilebilir)
│
├── data/                               # Uygulama detayları
│   ├── kimlik/
│   │   ├── KimlikRepositoryImpl.kt
│   │   └── GoogleGirisYardimcisi.kt     # Credential Manager sarmalayıcı
│   ├── firestore/
│   │   ├── SinerjiRepositoryImpl.kt
│   │   ├── HavuzRepositoryImpl.kt
│   │   └── dto/                          # Firestore belge modelleri
│   │       ├── KullaniciDto.kt
│   │       ├── SinerjiDto.kt
│   │       └── HavuzOgesiDto.kt
│   ├── yerel/                            # Room
│   │   ├── SonKararVeriTabani.kt
│   │   ├── HavuzDao.kt
│   │   └── varlik/
│   │       └── HavuzOgesiVarligi.kt
│   ├── uzak/                             # TMDB
│   │   ├── TmdbServisi.kt                # Retrofit arayüzü
│   │   ├── OneriRepositoryImpl.kt
│   │   └── dto/
│   │       ├── TmdbYanitDto.kt
│   │       └── TmdbFilmDto.kt
│   └── esleyici/                         # DTO <-> Domain dönüşümleri
│       ├── HavuzEsleyici.kt
│       ├── SinerjiEsleyici.kt
│       └── KullaniciEsleyici.kt
│
├── di/                                 # Hilt modülleri
│   ├── UygulamaModulu.kt
│   ├── FirebaseModulu.kt
│   ├── AgModulu.kt
│   ├── VeriTabaniModulu.kt
│   └── RepositoryModulu.kt
│
└── presentation/                       # Compose UI + ViewModel
    ├── navigasyon/
    │   ├── Rotalar.kt
    │   └── SonKararNavGrafi.kt
    ├── tema/
    │   ├── Renkler.kt
    │   ├── Tipografi.kt
    │   └── Tema.kt
    ├── ortak/                            # Yeniden kullanılabilir composable'lar
    │   ├── YuklemeGostergesi.kt
    │   ├── HataGorunumu.kt
    │   └── KonfetiEfekti.kt
    ├── giris/
    │   ├── GirisEkrani.kt
    │   ├── GirisArayuzDurumu.kt
    │   └── GirisViewModel.kt
    ├── eslesme/
    │   ├── EslesmeEkrani.kt
    │   ├── EslesmeArayuzDurumu.kt
    │   └── EslesmeViewModel.kt
    ├── havuz/
    │   ├── HavuzEkrani.kt
    │   ├── HavuzArayuzDurumu.kt
    │   └── HavuzViewModel.kt
    └── cark/
        ├── CarkEkrani.kt
        ├── CarkArayuzDurumu.kt
        ├── CarkViewModel.kt
        ├── CarkCizimi.kt                 # Canvas tabanlı çark bileşeni
        └── HaptikVeSes.kt                # Titreşim + ses efekti yardımcısı
```

---

## 3. Katman Sorumlulukları

### 3.1. `cekirdek` (Core)
Katmanlar arası paylaşılan, çerçeveye bağımsız yardımcılar. `Sonuc` sarmalayıcısı,
hata eşlemesi, `Kategori` enum'u ve sabitler burada bulunur.

### 3.2. `domain`
- **model:** İş modelleri (saf `data class`).
- **repository:** Yalnızca **arayüzler** (sözleşmeler). Uygulama `data` katmanındadır.
- **kullanim (UseCase):** Tek bir iş kuralını yürütür; `operator fun invoke(...)` deseni.

### 3.3. `data`
- Repository arayüzlerinin somut uygulamaları.
- Firestore/Room/TMDB kaynakları.
- DTO ↔ Domain eşleyicileri.
- **Room = tek gerçek kaynak (SSOT)** havuz için: Firestore dinleyicisi geldikçe
  Room güncellenir, UI Room'u gözlemler → çevrimdışı dayanıklılık.

### 3.4. `presentation`
- Compose ekranları, ViewModel'ler, navigasyon, tema.
- ViewModel yalnızca UseCase'lerle konuşur; Firestore/Room'a doğrudan erişmez.

---

## 4. Application ve Activity

```kotlin
// SonKararUygulamasi.kt
package com.sonkarar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SonKararUygulamasi : Application()
```

```kotlin
// MainActivity.kt
package com.sonkarar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sonkarar.presentation.navigasyon.SonKararNavGrafi
import com.sonkarar.presentation.tema.SonKararTemasi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonKararTemasi {
                SonKararNavGrafi()
            }
        }
    }
}
```

`AndroidManifest.xml` içinde `android:name=".SonKararUygulamasi"` tanımlanmalı ve
şu izinler eklenmelidir:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## 5. Hilt Modülleri

### 5.1. `FirebaseModulu.kt`

```kotlin
package com.sonkarar.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModulu {

    @Provides
    @Singleton
    fun kimlikSagla(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun firestoreSagla(): FirebaseFirestore = Firebase.firestore
}
```

### 5.2. `AgModulu.kt` (TMDB / Retrofit)

```kotlin
package com.sonkarar.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sonkarar.BuildConfig
import com.sonkarar.data.uzak.TmdbServisi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgModulu {

    private const val TMDB_TEMEL_URL = "https://api.themoviedb.org/3/"

    @Provides
    @Singleton
    fun jsonSagla(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun okHttpSagla(): OkHttpClient {
        val kayitci = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(kayitci)
            .build()
    }

    @Provides
    @Singleton
    fun retrofitSagla(istemci: OkHttpClient, json: Json): Retrofit {
        val icerikTipi = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(TMDB_TEMEL_URL)
            .client(istemci)
            .addConverterFactory(json.asConverterFactory(icerikTipi))
            .build()
    }

    @Provides
    @Singleton
    fun tmdbServisiSagla(retrofit: Retrofit): TmdbServisi =
        retrofit.create(TmdbServisi::class.java)
}
```

> **Not:** `retrofit2-kotlinx-serialization-converter` için import yolu
> `com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory`
> şeklindedir.

### 5.3. `VeriTabaniModulu.kt` (Room)

```kotlin
package com.sonkarar.di

import android.content.Context
import androidx.room.Room
import com.sonkarar.data.yerel.HavuzDao
import com.sonkarar.data.yerel.SonKararVeriTabani
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VeriTabaniModulu {

    @Provides
    @Singleton
    fun veriTabaniSagla(@ApplicationContext baglam: Context): SonKararVeriTabani =
        Room.databaseBuilder(
            baglam,
            SonKararVeriTabani::class.java,
            "sonkarar_veritabani"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun havuzDaoSagla(veriTabani: SonKararVeriTabani): HavuzDao =
        veriTabani.havuzDao()
}
```

### 5.4. `RepositoryModulu.kt` (Arayüz → Uygulama bağlama)

```kotlin
package com.sonkarar.di

import com.sonkarar.data.firestore.HavuzRepositoryImpl
import com.sonkarar.data.firestore.SinerjiRepositoryImpl
import com.sonkarar.data.kimlik.KimlikRepositoryImpl
import com.sonkarar.data.uzak.OneriRepositoryImpl
import com.sonkarar.domain.repository.HavuzRepository
import com.sonkarar.domain.repository.KimlikRepository
import com.sonkarar.domain.repository.OneriRepository
import com.sonkarar.domain.repository.SinerjiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModulu {

    @Binds
    @Singleton
    abstract fun kimlikRepositoryBagla(impl: KimlikRepositoryImpl): KimlikRepository

    @Binds
    @Singleton
    abstract fun sinerjiRepositoryBagla(impl: SinerjiRepositoryImpl): SinerjiRepository

    @Binds
    @Singleton
    abstract fun havuzRepositoryBagla(impl: HavuzRepositoryImpl): HavuzRepository

    @Binds
    @Singleton
    abstract fun oneriRepositoryBagla(impl: OneriRepositoryImpl): OneriRepository
}
```

### 5.5. `UygulamaModulu.kt` (Dağıtıcı / CoroutineDispatcher)

```kotlin
package com.sonkarar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GDagitici   // Giriş/çıkış (IO) dağıtıcısı

@Module
@InstallIn(SingletonComponent::class)
object UygulamaModulu {

    @Provides
    @GDagitici
    @Singleton
    fun ioDagiticiSagla(): CoroutineDispatcher = Dispatchers.IO
}
```

---

## 6. Sabitler ve Enum'lar (`cekirdek`)

```kotlin
// cekirdek/Kategori.kt
package com.sonkarar.cekirdek

enum class Kategori(val etiket: String) {
    YEMEK("Yemekler"),
    IZLENECEK("İzlenecekler");

    companion object {
        fun anahtardan(anahtar: String?): Kategori =
            entries.firstOrNull { it.name == anahtar } ?: YEMEK
    }
}
```

```kotlin
// cekirdek/Sabitler.kt
package com.sonkarar.cekirdek

object Sabitler {
    const val KOLEKSIYON_KULLANICILAR = "kullanicilar"
    const val KOLEKSIYON_SINERJILER = "sinerjiler"
    const val ALT_KOLEKSIYON_HAVUZ = "havuz"

    const val VARSAYILAN_AGIRLIK = 10
    const val ZAMAN_CEZASI_TUR_SAYISI = 3          // Son 3 tur kontrol edilir
    const val ZAMAN_CEZASI_ORANI = 0.20            // %80 düşür => %20'si kalır
    const val ENJEKTE_EDILECEK_ONERI_SAYISI = 3
    const val CARK_GECMISI_LIMITI = 100            // Firestore'da tutulan azami kayıt
}
```
