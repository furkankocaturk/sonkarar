package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
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
        kategori: Kategori,
        carkGecmisi: List<CarkGecmisiKaydi>,
        rastgele: Random = Random.Default
    ): Sonuc<CarkSonucu> {
        // 1) Havuzu çek
        val havuzSonuc = havuzRepository.kategoriyiGetir(sinerjiId, kategori)
        val temelHavuz = when (havuzSonuc) {
            is Sonuc.Basarili -> havuzSonuc.veri
            is Sonuc.Hata -> return havuzSonuc
            Sonuc.Yukleniyor -> return Sonuc.Yukleniyor
        }

        // 2) Zaman cezası uygula
        val cezaliHavuz = ZamanCezasi.uygula(temelHavuz, carkGecmisi, kategori)

        // 3) Dış öneri enjeksiyonu (yumuşak hata: başarısızsa boş liste)
        val populerTurler = populerTurleriBul(temelHavuz)
        val oneriSonuc = oneriRepository.oneriUret(
            kategori = kategori,
            populerTurler = populerTurler,
            adet = Sabitler.ENJEKTE_EDILECEK_ONERI_SAYISI
        )
        val oneriler = (oneriSonuc as? Sonuc.Basarili)?.veri ?: emptyList()

        val nihaiListe = cezaliHavuz + oneriler
        if (nihaiListe.isEmpty()) {
            return Sonuc.Hata("Havuzda hiç seçenek yok. Önce havuza içerik ekleyin.")
        }

        // 4) Ağırlıklı rastgele seçim
        val kazanan = agirlikliSecimYap(nihaiListe, rastgele)
            ?: return Sonuc.Hata("Seçim yapılamadı. Lütfen tekrar deneyin.")

        val kazananIndeks = nihaiListe.indexOf(kazanan)
        val hedefAci = hedefAciHesapla(kazananIndeks, nihaiListe.size)

        return Sonuc.Basarili(
            CarkSonucu(
                nihaiListe = nihaiListe,
                kazanan = kazanan,
                kazananIndeks = kazananIndeks,
                hedefAci = hedefAci
            )
        )
    }
}
