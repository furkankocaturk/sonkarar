package com.sonkarar.presentation.havuz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.AktifKullaniciyiGozlemleKullanimi
import com.sonkarar.domain.kullanim.HavuzaOgeEkleKullanimi
import com.sonkarar.domain.kullanim.HavuzdanOgeSilKullanimi
import com.sonkarar.domain.kullanim.HavuzuGozlemleKullanimi
import com.sonkarar.domain.kullanim.SenkronizasyonuBaslatKullanimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HavuzViewModel @Inject constructor(
    aktifKullaniciyiGozlemle: AktifKullaniciyiGozlemleKullanimi,
    private val havuzuGozlemle: HavuzuGozlemleKullanimi,
    private val senkronizasyonuBaslat: SenkronizasyonuBaslatKullanimi,
    private val havuzaOgeEkle: HavuzaOgeEkleKullanimi,
    private val havuzdanOgeSil: HavuzdanOgeSilKullanimi
) : ViewModel() {

    private val _durum = MutableStateFlow(HavuzArayuzDurumu())
    val durum = _durum.asStateFlow()

    private var akislarBaslatildi = false

    init {
        viewModelScope.launch {
            aktifKullaniciyiGozlemle()
                .filterNotNull()
                .map { it.kullaniciId to it.sinerjiId }
                .collect { (kullaniciId, sinerjiId) ->
                    _durum.update { it.copy(kullaniciId = kullaniciId, sinerjiId = sinerjiId) }
                    if (sinerjiId.isNotBlank() && !akislarBaslatildi) {
                        akislarBaslatildi = true
                        akislariBaslat(sinerjiId)
                    }
                }
        }
    }

    private fun akislariBaslat(sinerjiId: String) {
        // Firestore -> Room senkronizasyonu (her iki kategori)
        viewModelScope.launch { senkronizasyonuBaslat(sinerjiId, Kategori.YEMEK).collect {} }
        viewModelScope.launch { senkronizasyonuBaslat(sinerjiId, Kategori.IZLENECEK).collect {} }

        // Room gözlemi -> UI
        viewModelScope.launch {
            havuzuGozlemle(sinerjiId, Kategori.YEMEK).collect { liste ->
                _durum.update { it.copy(yemekler = liste) }
            }
        }
        viewModelScope.launch {
            havuzuGozlemle(sinerjiId, Kategori.IZLENECEK).collect { liste ->
                _durum.update { it.copy(izlenecekler = liste) }
            }
        }
    }

    fun kategoriDegistir(kategori: Kategori) =
        _durum.update { it.copy(aktifKategori = kategori) }

    fun metniGuncelle(yeni: String) =
        _durum.update { it.copy(yeniOgeMetni = yeni) }

    fun ogeEkle() {
        val anlik = _durum.value
        if (anlik.sinerjiId.isBlank()) return
        viewModelScope.launch {
            when (val sonuc = havuzaOgeEkle(
                sinerjiId = anlik.sinerjiId,
                kategori = anlik.aktifKategori,
                isim = anlik.yeniOgeMetni,
                ekleyenKullanici = anlik.kullaniciId
            )) {
                is Sonuc.Basarili -> _durum.update { it.copy(yeniOgeMetni = "") }
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

    fun hatayiTemizle() = _durum.update { it.copy(hataMesaji = null) }
}
