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

/** Çark çevirme hesabının çıktısı: nihai liste, kazanan ve hedef açı. */
data class CarkSonucu(
    val nihaiListe: List<HavuzOgesi>,
    val kazanan: HavuzOgesi,
    val kazananIndeks: Int,
    val hedefAci: Double
)

class CarkiCevirKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository,
    private val oneriRepository: OneriRepository,
    private val agirlikliSecimYap: AgirlikliSecimYap
) {
    suspend operator fun invoke(
        sinerjiId: String,
        cark: Cark,
        carkGecmisi: List<CarkGecmisiKaydi>,
        rastgele: Random = Random.Default
    ): Sonuc<CarkSonucu> {
        // 1) Havuzu çek
        val havuzSonuc = havuzRepository.ogeleriGetir(sinerjiId, cark.carkId)
        val temelHavuz = when (havuzSonuc) {
            is Sonuc.Basarili -> havuzSonuc.veri
            is Sonuc.Hata -> return havuzSonuc
            Sonuc.Yukleniyor -> return Sonuc.Yukleniyor
        }

        // 2) Zaman cezası uygula (bu çarka özel geçmişe göre)
        val cezaliHavuz = ZamanCezasi.uygula(temelHavuz, carkGecmisi, cark.carkId)

        // 3) Dış öneri enjeksiyonu (yalnızca Yemek/İzlenecek; yumuşak hata)
        val populerTurler = populerTurleriBul(temelHavuz)
        val oneriSonuc = oneriRepository.oneriUret(
            kategori = cark.kategori,
            populerTurler = populerTurler,
            adet = Sabitler.ENJEKTE_EDILECEK_ONERI_SAYISI
        )
        val oneriler = (oneriSonuc as? Sonuc.Basarili)?.veri ?: emptyList()

        val nihaiListe = cezaliHavuz + oneriler
        if (nihaiListe.isEmpty()) {
            return Sonuc.Hata("Havuzda hiç seçenek yok. Önce havuza içerik ekleyin.")
        }

        // 4) Ağırlıklı rastgele seçim (adalet için TÜM liste üzerinden)
        val kazanan = agirlikliSecimYap(nihaiListe, rastgele)
            ?: return Sonuc.Hata("Seçim yapılamadı. Lütfen tekrar deneyin.")

        // 5) Görsel çarkı okunur tutmak için dilim sayısını sınırla.
        //    Kazanan her zaman görsel listede yer alır; adalet tam listeden gelir.
        val gorselListe = gorselListeHazirla(nihaiListe, kazanan, rastgele)

        val kazananIndeks = gorselListe.indexOf(kazanan)
        val hedefAci = hedefAciHesapla(kazananIndeks, gorselListe.size, rastgele)

        return Sonuc.Basarili(
            CarkSonucu(
                nihaiListe = gorselListe,
                kazanan = kazanan,
                kazananIndeks = kazananIndeks,
                hedefAci = hedefAci
            )
        )
    }

    private fun gorselListeHazirla(
        tumListe: List<HavuzOgesi>,
        kazanan: HavuzOgesi,
        rastgele: Random
    ): List<HavuzOgesi> {
        // Dilim konumları her çevirişte değişsin diye HER ZAMAN karıştır.
        if (tumListe.size <= Sabitler.CARK_MAKS_DILIM) return tumListe.shuffled(rastgele)
        val digerleri = tumListe.filter { it.id != kazanan.id || it.isim != kazanan.isim }
            .shuffled(rastgele)
            .take(Sabitler.CARK_MAKS_DILIM - 1)
        return (digerleri + kazanan).shuffled(rastgele)
    }
}
