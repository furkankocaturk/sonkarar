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
    private val carkiHazirla: CarkiCevirKullanimi,
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
    private var listeHazirlaniyor = false

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
                _durum.update { it.copy(ogeSayisi = liste.size) }
                // Dönmüyorken çark listesini (ceza + öneri + karışım) tazele.
                if (!_durum.value.donuyorMu) listeyiTazele(sinerjiId)
            }
        }
        viewModelScope.launch {
            sinerjiyiGozlemle(sinerjiId).collect { sonuc ->
                if (sonuc is Sonuc.Basarili) {
                    carkGecmisi = sonuc.veri.carkGecmisi
                    uzaktanDurumIsle(sonuc.veri.carkDurumu)
                }
            }
        }
    }

    private fun listeyiTazele(sinerjiId: String) {
        val cark = _durum.value.cark ?: return
        if (listeHazirlaniyor) return
        listeHazirlaniyor = true
        viewModelScope.launch {
            when (val s = carkiHazirla(sinerjiId, cark, carkGecmisi)) {
                is Sonuc.Basarili -> _durum.update { it.copy(carkOgeleri = s.veri) }
                is Sonuc.Hata -> Unit
                Sonuc.Yukleniyor -> Unit
            }
            listeHazirlaniyor = false
        }
    }

    /** Eşin (uzaktaki cihazın) başlattığı çevirmeyi işle. */
    private fun uzaktanDurumIsle(carkDurumu: CarkDurumu) {
        if (carkDurumu.asama != CarkAsamasi.CEVRILIYOR) return
        if (carkDurumu.tur == islenenTur) return
        if (carkDurumu.carkId != carkId) return
        if (carkDurumu.ceviren == _durum.value.kullaniciId) return // kendi çevirmemiz
        islenenTur = carkDurumu.tur

        _durum.update {
            it.copy(
                donuyorMu = true,
                benCeviriyorum = false,
                carkOgeleri = carkDurumu.carkOgeleri.ifEmpty { it.carkOgeleri },
                kazananIsim = carkDurumu.kazananIsim,
                sonucGosteriliyor = false,
                disHedefAci = carkDurumu.finalAci.toFloat(),
                disTur = carkDurumu.tur
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
        }
    }

    /** Buton ile otomatik çevirme (rastgele fırlatma). */
    fun otomatikCevir() {
        if (!_durum.value.cevrilebilir) return
        _durum.update { it.copy(otomatikTetik = it.otomatikTetik + 1) }
    }

    /** Çevirme başladı (parmakla veya butonla). */
    fun cevirmeBasladi() {
        _durum.update { it.copy(donuyorMu = true, benCeviriyorum = true, sonucGosteriliyor = false) }
    }

    /** Yerel çevirme tamamlandı; göstergenin altındaki kazandı. */
    fun yerelCevrildi(kazananIndeks: Int, finalAci: Float) {
        val anlik = _durum.value
        val cark = anlik.cark ?: return
        val kazanan = anlik.carkOgeleri.getOrNull(kazananIndeks) ?: return
        val yeniTur = islenenTur + 1
        islenenTur = yeniTur

        _durum.update {
            it.copy(
                donuyorMu = false,
                kazananIsim = kazanan.isim,
                sonucGosteriliyor = true,
                konfetiTetikleyici = yeniTur
            )
        }
        viewModelScope.launch {
            gecmiseKayitEkle(anlik.sinerjiId, cark.carkId, cark.kategori, kazanan.isim)
            // Eş cihaza da bildir.
            carkDurumunuGuncelle(
                anlik.sinerjiId,
                CarkDurumu(
                    asama = CarkAsamasi.CEVRILIYOR,
                    ceviren = anlik.kullaniciId,
                    carkId = cark.carkId,
                    finalAci = finalAci.toDouble(),
                    kazananIsim = kazanan.isim,
                    kategori = cark.kategori,
                    tur = yeniTur,
                    carkOgeleri = anlik.carkOgeleri
                )
            )
            // Sonraki çevirme için listeyi tazele.
            listeyiTazele(anlik.sinerjiId)
        }
    }

    fun kazananiAzalt() {
        val anlik = _durum.value
        val oge = anlik.kazananOge ?: return
        if (oge.disOneriMi || oge.id.isBlank() || anlik.sinerjiId.isBlank()) return
        viewModelScope.launch {
            when (val sonuc = ogeyiAzalt(anlik.sinerjiId, oge)) {
                is Sonuc.Hata -> _durum.update { it.copy(hataMesaji = sonuc.mesaj) }
                else -> _durum.update { it.copy(bilgiMesaji = "Bu ögenin çıkma olasılığı azaltıldı") }
            }
        }
    }

    fun mesajlariTemizle() = _durum.update { it.copy(hataMesaji = null, bilgiMesaji = null) }
}
