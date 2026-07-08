# SonKarar — Arayüz (UI/UX) ve Ekran Mimarisi

> Bu döküman temayı (Koyu/Neon), navigasyonu, ekranları, ViewModel/ArayuzDurumu
> desenini, fizik tabanlı çark çizimini ve haptik/ses geri bildirimini tanımlar.
> Tüm görünür metinler `strings.xml` içinde **Türkçe**dir.

---

## 1. Tema: Koyu / Neon

Material 3 koyu tema tabanı üzerine neon vurgu renkleri. Minimalist, göz yormayan.

```kotlin
// presentation/tema/Renkler.kt
package com.sonkarar.presentation.tema

import androidx.compose.ui.graphics.Color

val NeonMor = Color(0xFF9D4EDD)
val NeonTurkuaz = Color(0xFF00E5C7)
val NeonPembe = Color(0xFFFF5DA2)
val NeonSari = Color(0xFFFFD60A)

val ZeminSiyah = Color(0xFF0B0B12)
val YuzeyKoyu = Color(0xFF15151F)
val YuzeyKoyu2 = Color(0xFF1E1E2A)
val MetinBeyaz = Color(0xFFF2F2F7)
val MetinSolgun = Color(0xFF9A9AA8)
val HataKirmizi = Color(0xFFFF4D6D)

// Çark dilimlerinde döngüsel kullanılacak neon paleti
val CarkPaleti = listOf(NeonMor, NeonTurkuaz, NeonPembe, NeonSari)
```

```kotlin
// presentation/tema/Tema.kt
package com.sonkarar.presentation.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KoyuNeonSema = darkColorScheme(
    primary = NeonMor,
    secondary = NeonTurkuaz,
    tertiary = NeonPembe,
    background = ZeminSiyah,
    surface = YuzeyKoyu,
    surfaceVariant = YuzeyKoyu2,
    onPrimary = ZeminSiyah,
    onBackground = MetinBeyaz,
    onSurface = MetinBeyaz,
    error = HataKirmizi
)

@Composable
fun SonKararTemasi(
    // Uygulama kimliği gereği her zaman koyu tema kullanılır.
    koyuTema: Boolean = true,
    icerik: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KoyuNeonSema,
        typography = SonKararTipografisi,
        content = icerik
    )
}
```

`Tipografi.kt` Material 3 varsayılan tipografisini kullanır; başlıklar `Bold`,
gövde metinleri okunur boyutta tanımlanır.

---

## 2. Navigasyon

```kotlin
// presentation/navigasyon/Rotalar.kt
package com.sonkarar.presentation.navigasyon

object Rotalar {
    const val ACILIS = "acilis"
    const val GIRIS = "giris"
    const val ESLESME = "eslesme"
    const val HAVUZ = "havuz"
    const val CARK = "cark"
}
```

```kotlin
// presentation/navigasyon/SonKararNavGrafi.kt
package com.sonkarar.presentation.navigasyon

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sonkarar.presentation.cark.CarkEkrani
import com.sonkarar.presentation.eslesme.EslesmeEkrani
import com.sonkarar.presentation.giris.GirisEkrani
import com.sonkarar.presentation.havuz.HavuzEkrani

@Composable
fun SonKararNavGrafi() {
    val navKontrolcu = rememberNavController()
    NavHost(navController = navKontrolcu, startDestination = Rotalar.ACILIS) {

        composable(Rotalar.ACILIS) {
            AcilisYonlendirici(navKontrolcu)
        }
        composable(Rotalar.GIRIS) {
            GirisEkrani(
                girisBasarili = {
                    navKontrolcu.navigate(Rotalar.ESLESME) {
                        popUpTo(Rotalar.GIRIS) { inclusive = true }
                    }
                }
            )
        }
        composable(Rotalar.ESLESME) {
            EslesmeEkrani(
                eslesmeTamamlandi = {
                    navKontrolcu.navigate(Rotalar.CARK) {
                        popUpTo(Rotalar.ESLESME) { inclusive = true }
                    }
                }
            )
        }
        composable(Rotalar.CARK) {
            CarkEkrani(
                havuzaGit = { navKontrolcu.navigate(Rotalar.HAVUZ) }
            )
        }
        composable(Rotalar.HAVUZ) {
            HavuzEkrani(geriGit = { navKontrolcu.popBackStack() })
        }
    }
}
```

`AcilisYonlendirici`, `AktifKullaniciyiGozlemleKullanimi` çıktısına göre kullanıcıyı
`GIRIS`, `ESLESME` veya `CARK` rotasına yönlendirir (yükleme sırasında neon
ilerleme göstergesi).

---

## 3. ViewModel / ArayuzDurumu Deseni

Her ekranın tek immutable durum sınıfı ve bir `StateFlow`'u vardır.

```kotlin
// presentation/havuz/HavuzArayuzDurumu.kt
package com.sonkarar.presentation.havuz

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.model.HavuzOgesi

data class HavuzArayuzDurumu(
    val aktifKategori: Kategori = Kategori.YEMEK,
    val yemekler: List<HavuzOgesi> = emptyList(),
    val izlenecekler: List<HavuzOgesi> = emptyList(),
    val yeniOgeMetni: String = "",
    val yukleniyor: Boolean = false,
    val hataMesaji: String? = null
) {
    val gorunenOgeler: List<HavuzOgesi>
        get() = if (aktifKategori == Kategori.YEMEK) yemekler else izlenecekler
}
```

```kotlin
// presentation/havuz/HavuzViewModel.kt (özet)
package com.sonkarar.presentation.havuz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.HavuzaOgeEkleKullanimi
import com.sonkarar.domain.kullanim.HavuzdanOgeSilKullanimi
import com.sonkarar.domain.kullanim.HavuzuGozlemleKullanimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HavuzViewModel @Inject constructor(
    private val havuzuGozlemle: HavuzuGozlemleKullanimi,
    private val havuzaOgeEkle: HavuzaOgeEkleKullanimi,
    private val havuzdanOgeSil: HavuzdanOgeSilKullanimi
    // Aktif sinerjiId ve kullaniciId, oturum durumundan enjekte edilir/gözlemlenir.
) : ViewModel() {

    private val _durum = MutableStateFlow(HavuzArayuzDurumu())
    val durum = _durum.asStateFlow()

    fun kategoriDegistir(kategori: Kategori) =
        _durum.update { it.copy(aktifKategori = kategori) }

    fun metniGuncelle(yeni: String) =
        _durum.update { it.copy(yeniOgeMetni = yeni) }

    fun ogeEkle(sinerjiId: String, kullaniciId: String) {
        val metin = _durum.value.yeniOgeMetni
        viewModelScope.launch {
            when (val s = havuzaOgeEkle(sinerjiId, _durum.value.aktifKategori, metin, kullaniciId)) {
                is Sonuc.Basarili -> _durum.update { it.copy(yeniOgeMetni = "") }
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = s.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun ogeSil(sinerjiId: String, ogeId: String) = viewModelScope.launch {
        havuzdanOgeSil(sinerjiId, ogeId)
    }

    fun hatayiTemizle() = _durum.update { it.copy(hataMesaji = null) }
}
```

> ViewModel'de `viewModelScope`, Compose'da durum toplama `collectAsStateWithLifecycle()`
> ile yapılır (`lifecycle-runtime-compose`).

---

## 4. Ekran 1 — Giriş ve Eşleşme

### 4.1. Giriş Ekranı
- Ortada büyük uygulama logosu / neon başlık: **"SonKarar"**.
- Altında: *"Kararı çarka bırakın"* sloganı.
- **"Google ile Giriş Yap"** butonu (neon çerçeveli). Basınca Credential Manager
  akışı (Google ID jetonu) başlar → `GirisViewModel.girisYap(jeton)`.
- Hata durumunda alt kısımda Snackbar ile Türkçe mesaj.

Google girişi Credential Manager ile (yeni API):

```kotlin
// presentation/giris/GirisEkrani.kt — Google jetonunu alma özeti
// GetGoogleIdOption ile istek yapılır; alınan idToken ViewModel'e iletilir.
// clientId olarak Firebase konsolundaki "Web istemci kimliği" kullanılır
// (strings.xml -> varsayilan_web_istemci_kimligi).
```

### 4.2. Eşleşme Ekranı
- Başlık: **"Eşinle Eşleş"**.
- Açıklama: *"Eşinin e-posta adresini gir. İkiniz de birbirinizi eklediğinizde ortak odanız hazır."*
- Metin alanı: **"Eşinin E-posta Adresi"** (klavye tipi e-posta).
- Buton: **"Ortak Oda Oluştur / Katıl"**.
- Eş henüz katılmadıysa durum kartı: *"Eş bekleniyor..."* (neon nabız animasyonu).

---

## 5. Ekran 2 — Ortak Havuz

- Üstte `TabRow` ile iki sekme: **"Yemekler"** / **"İzlenecekler"**.
- Liste: `LazyColumn`, her öge bir `Card`:
  - Sol: öge ismi (büyük), altında `tur` etiketi (varsa) küçük neon çip.
  - Sağ altta: **"Ekleyen: <eposta/ad>"** (gri, küçük).
- **Kaydırarak silme:** `SwipeToDismissBox` (Material 3). Sağa/sola kaydırınca
  arka planda kırmızı çöp kutusu ikonu; bırakınca `ogeSil` çağrılır ve Snackbar ile
  **"Öge silindi"** + **"Geri Al"** aksiyonu gösterilir.
- Altta sabit giriş çubuğu: `OutlinedTextField` (placeholder: *"Yeni bir seçenek yaz..."*) + **"Ekle"** butonu.
- Boş liste durumu: neon illüstrasyon + *"Henüz bir şey eklemediniz. İlk seçeneği ekleyin!"*.

```kotlin
// Kaydırarak silme örneği (Material 3)
// SwipeToDismissBox(state = rememberSwipeToDismissBoxState(confirmValueChange = { ... }))
// confirmValueChange içinde EndToStart/StartToEnd yönü yakalanır ve ogeSil tetiklenir.
```

---

## 6. Ekran 3 — Karar Çarkı (Ana Ekran)

### 6.1. Yerleşim
- Üst çubuk: aktif kategori seçici (Yemek/İzlenecek segmentli buton) + havuz kısayolu (ikon).
- Merkez: fizik tabanlı **çark** (`CarkCizimi`).
- Çark üstünde sabit gösterge (üçgen ok, tepede).
- Ortada büyük yuvarlak **"ÇEVİR"** butonu (neon degrade, basınca hafif ölçek animasyonu).
- Çevriliyorken buton pasifleşir, üstte durum metni:
  - Kendi cihazın çeviriyorsa: *"Çark dönüyor..."*
  - Eş çeviriyorsa: **"Eşiniz çarkı çeviriyor..."**
- Sonuç anında: tam ekran `KonfetiEfekti` + kazanan kartı: **"Sonuç: <isim>"**.
  Dış öneriyse küçük bir **"öneri"** rozeti gösterilir.

### 6.2. Fizik Tabanlı Çark Çizimi (Canvas)

Çark, `Canvas` üzerinde dilimlerle çizilir. Dönüş, `Animatable` ile
**yavaşlayan sürtünme (decelerate) easing** kullanır. Süre iki cihazda **sabit ve eşittir**
(senkron için kritik).

```kotlin
// presentation/cark/CarkCizimi.kt (özet — çizim ve animasyon mantığı)
package com.sonkarar.presentation.cark

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.tema.CarkPaleti

const val CARK_ANIMASYON_SURESI_MS = 4000

@Composable
fun CarkCizimi(
    ogeler: List<HavuzOgesi>,
    hedefAci: Float,
    donuyorMu: Boolean,
    cizgiGecildi: (indeks: Int) -> Unit,   // haptik + ses için geri çağrı
    modifier: Modifier = Modifier
) {
    val aci = remember { Animatable(0f) }
    val guncelCizgiGecildi = rememberUpdatedState(cizgiGecildi)
    val olcer = rememberTextMeasurer()

    LaunchedEffect(donuyorMu, hedefAci) {
        if (donuyorMu && ogeler.isNotEmpty()) {
            aci.snapTo(aci.value % 360f)
            aci.animateTo(
                targetValue = hedefAci,
                animationSpec = tween(
                    durationMillis = CARK_ANIMASYON_SURESI_MS,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        }
    }

    // Dilim sınırından geçişte cizgiGecildi(...) çağrısı yapılır (haptik/ses).
    Canvas(modifier = modifier.size(300.dp)) {
        val dilimSayisi = ogeler.size
        if (dilimSayisi == 0) return@Canvas
        val dilimAcisi = 360f / dilimSayisi
        rotate(degrees = aci.value) {
            ogeler.forEachIndexed { indeks, oge ->
                // drawArc ile dilim, drawText ile isim çizilir.
                // Renk: CarkPaleti[indeks % CarkPaleti.size]
                // (Ayrıntılı çizim tam kodda tamamlanır.)
            }
        }
    }
}
```

> **Senkron kuralı:** `CARK_ANIMASYON_SURESI_MS` ve `easing` iki cihazda birebir aynı
> olmalıdır. `hedefAci` Firestore'dan gelir; böylece her iki çark aynı yerde durur ve
> sonuç aynı saniyede görünür.

### 6.3. Haptik Geri Bildirim ve Ses

Çark bir dilim çizgisinden geçtiğinde: kısa titreşim + "tık" ses efekti.

```kotlin
// presentation/cark/HaptikVeSes.kt
package com.sonkarar.presentation.cark

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HaptikVeSes(private val baglam: Context) {

    private val titresim: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val yonetici = baglam.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                as? VibratorManager
            yonetici?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            baglam.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val tonUretici: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_MUSIC, 60)
    }.getOrNull()

    /** Çark bir çizgiden geçerken tetiklenir. */
    fun tik() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                titresim?.vibrate(VibrationEffect.createOneShot(18, 90))
            } else {
                @Suppress("DEPRECATION")
                titresim?.vibrate(18)
            }
            tonUretici?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        }
    }

    fun serbestBirak() {
        runCatching { tonUretici?.release() }
    }
}
```

> Tüm titreşim/ses çağrıları `runCatching` ile sarılır; donanım desteği olmayan
> cihazlarda uygulama çökmez.

### 6.4. Konfeti Efekti

`KonfetiEfekti`, sonuç anında tetiklenen basit bir parçacık animasyonudur
(`Canvas` + `Animatable` ile düşen neon parçacıklar). Dış kütüphane gerektirmez.

---

## 7. Ortak Bileşenler

- `YuklemeGostergesi`: ortalanmış neon `CircularProgressIndicator`.
- `HataGorunumu`: hata ikonu + Türkçe mesaj + **"Tekrar Dene"** butonu.
- `KonfetiEfekti`: yukarıda.

---

## 8. Erişilebilirlik ve Durum Yönetimi

- Tüm ikon butonlarında Türkçe `contentDescription`.
- Dokunma hedefleri ≥ 48dp.
- Durumlar `collectAsStateWithLifecycle` ile toplanır; arka planda dinleyici sızıntısı olmaz.
- Konfigurasyon değişiminde (ekran döndürme) durum korunur (`ViewModel`).

---

## 9. Türkçe Metin Kaynakları (`res/values/strings.xml`)

Tüm kullanıcıya görünen metinler burada tanımlanır (örnek alt küme):

```xml
<resources>
    <string name="uygulama_adi">SonKarar</string>
    <string name="slogan">Kararı çarka bırakın</string>

    <!-- Giriş -->
    <string name="google_ile_giris">Google ile Giriş Yap</string>
    <string name="giris_hatasi">Giriş yapılamadı. Lütfen tekrar deneyin.</string>

    <!-- Eşleşme -->
    <string name="eslesme_baslik">Eşinle Eşleş</string>
    <string name="es_eposta_etiketi">Eşinin E-posta Adresi</string>
    <string name="oda_olustur_katil">Ortak Oda Oluştur / Katıl</string>
    <string name="es_bekleniyor">Eş bekleniyor…</string>

    <!-- Havuz -->
    <string name="sekme_yemekler">Yemekler</string>
    <string name="sekme_izlenecekler">İzlenecekler</string>
    <string name="yeni_oge_ipucu">Yeni bir seçenek yaz…</string>
    <string name="ekle">Ekle</string>
    <string name="ekleyen_bicimi">Ekleyen: %1$s</string>
    <string name="oge_silindi">Öge silindi</string>
    <string name="geri_al">Geri Al</string>
    <string name="havuz_bos">Henüz bir şey eklemediniz. İlk seçeneği ekleyin!</string>

    <!-- Çark -->
    <string name="cevir">ÇEVİR</string>
    <string name="cark_donuyor">Çark dönüyor…</string>
    <string name="es_ceviriyor">Eşiniz çarkı çeviriyor…</string>
    <string name="sonuc_bicimi">Sonuç: %1$s</string>
    <string name="havuz_bos_uyari">Havuzda hiç seçenek yok. Önce havuza içerik ekleyin.</string>
    <string name="oneri_rozeti">öneri</string>

    <!-- Genel -->
    <string name="tekrar_dene">Tekrar Dene</string>
    <string name="cikis_yap">Çıkış Yap</string>
    <string name="baglanti_hatasi">İnternet bağlantısı kurulamadı. Bağlantınızı kontrol edin.</string>
</resources>
```

> **Kural:** Hiçbir composable içinde sabit (hard-coded) İngilizce/Türkçe string
> bırakılmaz; tümü `stringResource(R.string.…)` ile çağrılır.
