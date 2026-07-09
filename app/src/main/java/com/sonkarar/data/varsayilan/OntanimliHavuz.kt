package com.sonkarar.data.varsayilan

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.LinkUretici
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.domain.model.Cark
import com.sonkarar.domain.model.HavuzOgesi

object OntanimliHavuz {

    // Sabit çark kimlikleri
    const val CARK_YEMEK = "YEMEK"
    const val CARK_IZLENECEK = "IZLENECEK"
    const val CARK_EVET_HAYIR = "EVET_HAYIR"
    const val CARK_NE_ICELIM = "NE_ICELIM"
    const val CARK_KIM_ODESIN = "KIM_ODESIN"

    /** Uygulama ilk açıldığında oluşturulan hazır çarklar. */
    fun varsayilanCarklar(): List<Cark> = listOf(
        Cark(CARK_YEMEK, "Ne Yesek?", Kategori.YEMEK, sistemMi = true),
        Cark(CARK_IZLENECEK, "Ne İzlesek?", Kategori.IZLENECEK, sistemMi = true),
        Cark(CARK_EVET_HAYIR, "Evet / Hayır", Kategori.GENEL, sistemMi = false),
        Cark(CARK_NE_ICELIM, "Ne İçelim?", Kategori.GENEL, sistemMi = false),
        Cark(CARK_KIM_ODESIN, "Kim Ödesin?", Kategori.GENEL, sistemMi = false)
    )

    /** Belirli bir varsayılan çark için başlangıç ögeleri. */
    fun carkOgeleri(carkId: String, ekleyenKullanici: String): List<HavuzOgesi> = when (carkId) {
        CARK_YEMEK -> yemekler(ekleyenKullanici)
        CARK_IZLENECEK -> izlenecekler(ekleyenKullanici)
        CARK_EVET_HAYIR -> genelListe(carkId, listOf("Evet", "Hayır"), ekleyenKullanici)
        CARK_NE_ICELIM -> genelListe(
            carkId,
            listOf("Çay", "Kahve", "Su", "Ayran", "Kola", "Limonata"),
            ekleyenKullanici
        )
        CARK_KIM_ODESIN -> genelListe(carkId, listOf("Sen", "Ben"), ekleyenKullanici)
        else -> emptyList()
    }

    /** Tüm varsayılan çarkların tüm ögeleri (tek seferde tohumlama için). */
    fun tumOgeler(ekleyenKullanici: String): List<HavuzOgesi> =
        varsayilanCarklar().flatMap { carkOgeleri(it.carkId, ekleyenKullanici) }

    private fun yemekler(ekleyenKullanici: String): List<HavuzOgesi> = listOf(
        yemek("Makarna", "İtalyan", ekleyenKullanici),
        yemek("Pizza", "İtalyan", ekleyenKullanici),
        yemek("Mantı", "Türk", ekleyenKullanici),
        yemek("Köfte", "Türk", ekleyenKullanici),
        yemek("Ramen", "Uzak Doğu", ekleyenKullanici),
        yemek("Suşi", "Uzak Doğu", ekleyenKullanici),
        yemek("Hamburger", "Fast Food", ekleyenKullanici),
        yemek("Tavuk Dürüm", "Fast Food", ekleyenKullanici)
    )

    private fun izlenecekler(ekleyenKullanici: String): List<HavuzOgesi> = listOf(
        izlenecek("Prens", "Komedi", "BluTV", 8.4, ekleyenKullanici),
        izlenecek("Inception", "Bilim Kurgu", "Netflix", 8.8, ekleyenKullanici),
        izlenecek("Interstellar", "Bilim Kurgu", "Prime Video", 8.7, ekleyenKullanici),
        izlenecek("The Office", "Komedi", "Netflix", 9.0, ekleyenKullanici),
        izlenecek("Şahsiyet", "Dram", "GAİN", 9.0, ekleyenKullanici),
        izlenecek("Breaking Bad", "Dram", "Netflix", 9.5, ekleyenKullanici),
        izlenecek("The Bear", "Dram", "Disney+", 8.5, ekleyenKullanici),
        izlenecek("Dune: Part Two", "Bilim Kurgu", "Sinema/Dijital", 8.5, ekleyenKullanici)
    )

    private fun genelListe(
        carkId: String,
        isimler: List<String>,
        ekleyenKullanici: String
    ): List<HavuzOgesi> = isimler.map { isim ->
        HavuzOgesi(
            id = "",
            carkId = carkId,
            kategori = Kategori.GENEL,
            isim = isim,
            ekleyenKullanici = ekleyenKullanici,
            agirlik = Sabitler.VARSAYILAN_AGIRLIK
        )
    }

    private fun yemek(
        isim: String,
        tur: String,
        ekleyenKullanici: String
    ): HavuzOgesi = HavuzOgesi(
        id = "",
        carkId = CARK_YEMEK,
        kategori = Kategori.YEMEK,
        isim = isim,
        tur = tur,
        ekleyenKullanici = ekleyenKullanici,
        agirlik = Sabitler.VARSAYILAN_AGIRLIK,
        kaynakAdi = "Tarif ara",
        detayUrl = LinkUretici.yemekTarifLinki(isim)
    )

    private fun izlenecek(
        isim: String,
        tur: String,
        platform: String,
        puan: Double,
        ekleyenKullanici: String
    ): HavuzOgesi = HavuzOgesi(
        id = "",
        carkId = CARK_IZLENECEK,
        kategori = Kategori.IZLENECEK,
        isim = isim,
        tur = tur,
        ekleyenKullanici = ekleyenKullanici,
        agirlik = Sabitler.VARSAYILAN_AGIRLIK,
        platform = platform,
        puan = puan,
        detayUrl = LinkUretici.izlenecekLinki(isim),
        kaynakAdi = "Nerede izlenir?"
    )
}
