package com.sonkarar.presentation.havuz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.AktifKullaniciyiGozlemleKullanimi
import com.sonkarar.domain.kullanim.CarkGetirKullanimi
import com.sonkarar.domain.kullanim.FavoriDegistirKullanimi
import com.sonkarar.domain.kullanim.HavuzaOgeEkleKullanimi
import com.sonkarar.domain.kullanim.HavuzdanOgeSilKullanimi
import com.sonkarar.domain.kullanim.HavuzuGozlemleKullanimi
import com.sonkarar.domain.kullanim.SenkronizasyonuBaslatKullanimi
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.navigasyon.Rotalar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HavuzViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    aktifKullaniciyiGozlemle: AktifKullaniciyiGozlemleKullanimi,
    private val carkGetir: CarkGetirKullanimi,
    private val havuzuGozlemle: HavuzuGozlemleKullanimi,
    private val senkronizasyonuBaslat: SenkronizasyonuBaslatKullanimi,
    private val havuzaOgeEkle: HavuzaOgeEkleKullanimi,
    private val havuzdanOgeSil: HavuzdanOgeSilKullanimi,
    private val favoriDegistirKullanimi: FavoriDegistirKullanimi
) : ViewModel() {

    private val carkId: String = savedStateHandle.get<String>(Rotalar.ARG_CARK_ID).orEmpty()

    private val _durum = MutableStateFlow(HavuzArayuzDurumu())
    val durum = _durum.asStateFlow()

    private var akislarBaslatildi = false

    init {
        viewModelScope.launch {
            _durum.update { it.copy(cark = carkGetir(carkId)) }
        }
        viewModelScope.launch {
            aktifKullaniciyiGozlemle()
                .filterNotNull()
                .collect { kullanici ->
                    _durum.update {
                        it.copy(
                            kullaniciId = kullanici.kullaniciId,
                            sinerjiId = kullanici.sinerjiId
                        )
                    }
                    if (kullanici.sinerjiId.isNotBlank() && !akislarBaslatildi) {
                        akislarBaslatildi = true
                        akislariBaslat(kullanici.sinerjiId)
                    }
                }
        }
    }

    private fun akislariBaslat(sinerjiId: String) {
        viewModelScope.launch { senkronizasyonuBaslat(sinerjiId, carkId).collect {} }
        viewModelScope.launch {
            havuzuGozlemle(sinerjiId, carkId).collect { liste ->
                _durum.update { it.copy(ogeler = liste) }
            }
        }
    }

    fun metniGuncelle(yeni: String) = _durum.update { it.copy(yeniOgeMetni = yeni) }
    fun turuGuncelle(yeni: String) = _durum.update { it.copy(yeniOgeTuru = yeni) }

    fun ogeEkle() {
        val anlik = _durum.value
        val kategori = anlik.cark?.kategori ?: Kategori.GENEL
        if (anlik.sinerjiId.isBlank()) return
        viewModelScope.launch {
            when (val sonuc = havuzaOgeEkle(
                sinerjiId = anlik.sinerjiId,
                carkId = carkId,
                kategori = kategori,
                isim = anlik.yeniOgeMetni,
                tur = anlik.yeniOgeTuru,
                ekleyenKullanici = anlik.kullaniciId
            )) {
                is Sonuc.Basarili -> _durum.update { it.copy(yeniOgeMetni = "", yeniOgeTuru = "") }
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = sonuc.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun ogeSil(ogeId: String) {
        val sinerjiId = _durum.value.sinerjiId
        if (sinerjiId.isBlank()) return
        viewModelScope.launch {
            when (val sonuc = havuzdanOgeSil(sinerjiId, ogeId)) {
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = sonuc.mesaj) }
                else -> Unit
            }
        }
    }

    fun favoriDegistir(oge: HavuzOgesi) {
        val sinerjiId = _durum.value.sinerjiId
        if (sinerjiId.isBlank()) return
        viewModelScope.launch { favoriDegistirKullanimi(sinerjiId, oge) }
    }

    fun hatayiTemizle() = _durum.update { it.copy(hataMesaji = null) }
}
