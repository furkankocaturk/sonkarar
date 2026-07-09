package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.Cark
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.HavuzRepository
import com.sonkarar.domain.repository.OneriRepository
import javax.inject.Inject
import kotlin.random.Random

/**
 * Çevirme için çarkın GÖRSEL listesini hazırlar (zaman cezası uygulanmış +
 * dış öneriler eklenmiş + okunurluk için sınırlanmış). Kazananı SEÇMEZ;
 * kazanan, fiziksel dönüş sonrası göstergenin altına denk gelen dilimdir.
 */
class CarkiCevirKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository,
    private val oneriRepository: OneriRepository
) {
    suspend operator fun invoke(
        sinerjiId: String,
        cark: Cark,
        carkGecmisi: List<CarkGecmisiKaydi>,
        rastgele: Random = Random.Default
    ): Sonuc<List<HavuzOgesi>> {
        val havuzSonuc = havuzRepository.ogeleriGetir(sinerjiId, cark.carkId)
        val temelHavuz = when (havuzSonuc) {
            is Sonuc.Basarili -> havuzSonuc.veri
            is Sonuc.Hata -> return havuzSonuc
            Sonuc.Yukleniyor -> return Sonuc.Yukleniyor
        }

        val cezaliHavuz = ZamanCezasi.uygula(temelHavuz, carkGecmisi, cark.carkId)

        val populerTurler = populerTurleriBul(temelHavuz)
        val oneriSonuc = oneriRepository.oneriUret(
            kategori = cark.kategori,
            populerTurler = populerTurler,
            adet = Sabitler.ENJEKTE_EDILECEK_ONERI_SAYISI
        )
        val oneriler = (oneriSonuc as? Sonuc.Basarili)?.veri ?: emptyList()

        val tumListe = cezaliHavuz + oneriler
        if (tumListe.isEmpty()) {
            return Sonuc.Hata("Bu çarkta hiç seçenek yok. Önce seçenek ekleyin.")
        }

        // Okunurluk için dilim sayısını sınırla; her çevirişte karıştır.
        val gorselListe = if (tumListe.size <= Sabitler.CARK_MAKS_DILIM) {
            tumListe.shuffled(rastgele)
        } else {
            tumListe.shuffled(rastgele).take(Sabitler.CARK_MAKS_DILIM)
        }
        return Sonuc.Basarili(gorselListe)
    }
}
