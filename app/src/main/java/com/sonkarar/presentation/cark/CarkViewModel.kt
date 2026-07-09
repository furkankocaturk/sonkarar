package com.sonkarar.presentation.cark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.AktifKullaniciyiGozlemleKullanimi
import com.sonkarar.domain.kullanim.CarkDurumunuGuncelleKullanimi
import com.sonkarar.domain.kullanim.CarkGetirKullanimi
import com.sonkarar.domain.kullanim.CarkiCevirKullanimi
import com.sonkarar.domain.kullanim.GecmiseKayitEkleKullanimi
import com.sonkarar.domain.kullanim.HavuzuGozlemleKullanimi
import com.sonkarar.domain.kullanim.OgeyiAzaltKullanimi
import com.sonkarar.domain.kullanim.SenkronizasyonuBaslatKullanimi
import com.sonkarar.domain.kullanim.SinerjiyiGozlemleKullanimi
import com.sonkarar.domain.model.CarkAsamasi
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.presentation.navigasyon.Rotalar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    aktifKullaniciyiGozlemle: AktifKullaniciyiGozlemleKullanimi,
    private val carkGetir: CarkGetirKullanimi,
    private val havuzuGozlemle: HavuzuGozlemleKullanimi,
    private val senkronizasyonuBaslat: SenkronizasyonuBaslatKullanimi,
    private val sinerjiyiGozlemle: SinerjiyiGozlemleKullanimi,
    private val carkiCevir: CarkiCevirKullanimi,
    private val carkDurumunuGuncelle: CarkDurumunuGuncelleKullanimi,
    private val gecmiseKayitEkle: GecmiseKayitEkleKullanimi,
    private val ogeyiAzalt: OgeyiAzaltKullanimi
) : ViewModel() {

    private val carkId: String = savedStateHandle.get<String>(Rotalar.ARG_CARK_ID).orEmpty()

    private val _durum = MutableStateFlow(CarkArayuzDurumu())
    val durum = _durum.asStateFlow()

    private var carkGecmisi: List<CarkGecmisiKaydi> = emptyList()
    private var islenenTur: Long = -1L
    private var akislarBaslatildi = false

    init {
        viewModelScope.launch {
            val cark = carkGetir(carkId)
            _durum.update { it.copy(cark = cark) }
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
        viewModelScope.launch {
            sinerjiyiGozlemle(sinerjiId).collect { sonuc ->
                if (sonuc is Sonuc.Basarili) {
                    carkGecmisi = sonuc.veri.carkGecmisi
                    carkDurumunuIsle(sonuc.veri.carkDurumu)
                }
            }
        }
    }

    private fun carkDurumunuIsle(carkDurumu: CarkDurumu) {
        if (carkDurumu.asama != CarkAsamasi.CEVRILIYOR) return
        if (carkDurumu.tur == islenenTur) return
        // Yalnızca bu çarka ait durum güncellemesini işle.
        if (carkDurumu.carkOgeleri.isNotEmpty() &&
            carkDurumu.carkOgeleri.none { it.carkId == carkId || it.carkId.isBlank() }
        ) return
        islenenTur = carkDurumu.tur

        val benimUid = _durum.value.kullaniciId
        _durum.update {
            it.copy(
                donuyorMu = true,
                hedefAci = carkDurumu.hedefAci.toFloat(),
                benCeviriyorum = carkDurumu.ceviren == benimUid,
                kazananIsim = carkDurumu.kazananIsim,
                carkOgeleri = carkDurumu.carkOgeleri,
                sonucGosteriliyor = false
            )
        }

        viewModelScope.launch {
            delay(CARK_ANIMASYON_SURESI_MS.toLong())
            _durum.update {
                it.copy(
                    donuyorMu = false,
                    sonucGosteriliyor = true,
                    konfetiTetikleyici = carkDurumu.tur
                )
            }
            val cark = _durum.value.cark
            if (carkDurumu.ceviren == benimUid && cark != null) {
                gecmiseKayitEkle(
                    _durum.value.sinerjiId,
                    cark.carkId,
                    cark.kategori,
                    carkDurumu.kazananIsim
                )
            }
        }
    }

    fun cevir() {
        val anlik = _durum.value
        val cark = anlik.cark ?: return
        if (anlik.donuyorMu || anlik.sinerjiId.isBlank()) return

        viewModelScope.launch {
            when (val sonuc = carkiCevir(anlik.sinerjiId, cark, carkGecmisi)) {
                is Sonuc.Basarili -> {
                    val veri = sonuc.veri
                    val guncelDurum = CarkDurumu(
                        asama = CarkAsamasi.CEVRILIYOR,
                        ceviren = anlik.kullaniciId,
                        hedefAci = veri.hedefAci,
                        kazananIsim = veri.kazanan.isim,
                        kategori = cark.kategori,
                        tur = islenenTur + 1,
                        carkOgeleri = veri.nihaiListe
                    )
                    val yazma = carkDurumunuGuncelle(anlik.sinerjiId, guncelDurum)
                    if (yazma is Sonuc.Hata) {
                        _durum.update { it.copy(hataMesaji = yazma.mesaj) }
                    }
                }
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = sonuc.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun kazananiAzalt() {
        val anlik = _durum.value
        val oge = anlik.kazananOge ?: return
        if (oge.disOneriMi || oge.id.isBlank() || anlik.sinerjiId.isBlank()) return
        viewModelScope.launch {
            when (val sonuc = ogeyiAzalt(anlik.sinerjiId, oge)) {
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = sonuc.mesaj) }
                else -> _durum.update {
                    it.copy(bilgiMesaji = "Bu ögenin çıkma olasılığı azaltıldı")
                }
            }
        }
    }

    fun mesajlariTemizle() = _durum.update { it.copy(hataMesaji = null, bilgiMesaji = null) }
}
