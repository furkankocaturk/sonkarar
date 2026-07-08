package com.sonkarar.presentation.cark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.AktifKullaniciyiGozlemleKullanimi
import com.sonkarar.domain.kullanim.CarkDurumunuGuncelleKullanimi
import com.sonkarar.domain.kullanim.CarkiCevirKullanimi
import com.sonkarar.domain.kullanim.GecmiseKayitEkleKullanimi
import com.sonkarar.domain.kullanim.HavuzuGozlemleKullanimi
import com.sonkarar.domain.kullanim.OgeyiAzaltKullanimi
import com.sonkarar.domain.kullanim.OturumuKapatKullanimi
import com.sonkarar.domain.kullanim.SenkronizasyonuBaslatKullanimi
import com.sonkarar.domain.kullanim.SinerjiyiGozlemleKullanimi
import com.sonkarar.domain.model.CarkAsamasi
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.CarkGecmisiKaydi
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
    aktifKullaniciyiGozlemle: AktifKullaniciyiGozlemleKullanimi,
    private val havuzuGozlemle: HavuzuGozlemleKullanimi,
    private val senkronizasyonuBaslat: SenkronizasyonuBaslatKullanimi,
    private val sinerjiyiGozlemle: SinerjiyiGozlemleKullanimi,
    private val carkiCevir: CarkiCevirKullanimi,
    private val carkDurumunuGuncelle: CarkDurumunuGuncelleKullanimi,
    private val gecmiseKayitEkle: GecmiseKayitEkleKullanimi,
    private val ogeyiAzalt: OgeyiAzaltKullanimi,
    private val oturumuKapat: OturumuKapatKullanimi
) : ViewModel() {

    private val _durum = MutableStateFlow(CarkArayuzDurumu())
    val durum = _durum.asStateFlow()

    private var carkGecmisi: List<CarkGecmisiKaydi> = emptyList()
    private var islenenTur: Long = -1L
    private var akislarBaslatildi = false

    init {
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
        viewModelScope.launch { senkronizasyonuBaslat(sinerjiId, Kategori.YEMEK).collect {} }
        viewModelScope.launch { senkronizasyonuBaslat(sinerjiId, Kategori.IZLENECEK).collect {} }

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
        islenenTur = carkDurumu.tur

        val benimUid = _durum.value.kullaniciId
        _durum.update {
            it.copy(
                aktifKategori = carkDurumu.kategori,
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
            // Geçmiş kaydını yalnızca çeviren cihaz ekler (çift kayıt önlenir).
            if (carkDurumu.ceviren == benimUid) {
                gecmiseKayitEkle(
                    _durum.value.sinerjiId,
                    carkDurumu.kategori,
                    carkDurumu.kazananIsim
                )
            }
        }
    }

    fun kategoriDegistir(kategori: Kategori) {
        if (_durum.value.donuyorMu) return
        _durum.update {
            it.copy(
                aktifKategori = kategori,
                sonucGosteriliyor = false,
                kazananIsim = null,
                carkOgeleri = emptyList()
            )
        }
    }

    fun cevir() {
        val anlik = _durum.value
        if (anlik.donuyorMu || anlik.sinerjiId.isBlank()) return

        viewModelScope.launch {
            when (val sonuc = carkiCevir(anlik.sinerjiId, anlik.aktifKategori, carkGecmisi)) {
                is Sonuc.Basarili -> {
                    val veri = sonuc.veri
                    val guncelDurum = CarkDurumu(
                        asama = CarkAsamasi.CEVRILIYOR,
                        ceviren = anlik.kullaniciId,
                        hedefAci = veri.hedefAci,
                        kazananIsim = veri.kazanan.isim,
                        kategori = anlik.aktifKategori,
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

    fun cikisYap() {
        viewModelScope.launch {
            oturumuKapat()
            _durum.update { it.copy(cikisYapildi = true) }
        }
    }

    fun mesajlariTemizle() = _durum.update { it.copy(hataMesaji = null, bilgiMesaji = null) }

    fun hatayiTemizle() = _durum.update { it.copy(hataMesaji = null) }
}
