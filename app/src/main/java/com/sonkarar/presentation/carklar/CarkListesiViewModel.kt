package com.sonkarar.presentation.carklar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.TemaModu
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.CarkEkleKullanimi
import com.sonkarar.domain.kullanim.CarkSilKullanimi
import com.sonkarar.domain.kullanim.CarklariGozlemleKullanimi
import com.sonkarar.domain.kullanim.OturumuKapatKullanimi
import com.sonkarar.domain.kullanim.TemaAyarlaKullanimi
import com.sonkarar.domain.model.Cark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CarkListesiArayuzDurumu(
    val carklar: List<Cark> = emptyList(),
    val yukleniyor: Boolean = true,
    val eklemeAcik: Boolean = false,
    val yeniAd: String = "",
    val yeniKategori: Kategori = Kategori.GENEL,
    val hataMesaji: String? = null,
    val cikisYapildi: Boolean = false
)

@HiltViewModel
class CarkListesiViewModel @Inject constructor(
    carklariGozlemle: CarklariGozlemleKullanimi,
    private val carkEkle: CarkEkleKullanimi,
    private val carkSil: CarkSilKullanimi,
    private val oturumuKapat: OturumuKapatKullanimi,
    private val temaAyarla: TemaAyarlaKullanimi
) : ViewModel() {

    private val _durum = MutableStateFlow(CarkListesiArayuzDurumu())
    val durum = _durum.asStateFlow()

    init {
        viewModelScope.launch {
            carklariGozlemle().collect { liste ->
                _durum.update { it.copy(carklar = liste, yukleniyor = false) }
            }
        }
    }

    fun eklemeyiAc() = _durum.update { it.copy(eklemeAcik = true, yeniAd = "", yeniKategori = Kategori.GENEL) }
    fun eklemeyiKapat() = _durum.update { it.copy(eklemeAcik = false) }
    fun adGuncelle(yeni: String) = _durum.update { it.copy(yeniAd = yeni) }
    fun kategoriGuncelle(kategori: Kategori) = _durum.update { it.copy(yeniKategori = kategori) }

    fun carkOlustur() {
        val anlik = _durum.value
        viewModelScope.launch {
            when (val sonuc = carkEkle(anlik.yeniAd, anlik.yeniKategori)) {
                is Sonuc.Basarili -> _durum.update { it.copy(eklemeAcik = false, yeniAd = "") }
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = sonuc.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun sil(carkId: String) {
        viewModelScope.launch {
            when (val sonuc = carkSil(carkId)) {
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = sonuc.mesaj) }
                else -> Unit
            }
        }
    }

    fun temaSec(mod: TemaModu) = temaAyarla(mod)

    fun cikisYap() {
        viewModelScope.launch {
            oturumuKapat()
            _durum.update { it.copy(cikisYapildi = true) }
        }
    }

    fun hatayiTemizle() = _durum.update { it.copy(hataMesaji = null) }
}
