package com.sonkarar.data.varsayilan

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.LinkUretici
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.domain.model.HavuzOgesi

object OntanimliHavuz {

    fun ogeleriOlustur(ekleyenKullanici: String): List<HavuzOgesi> =
        yemekler(ekleyenKullanici) + izlenecekler(ekleyenKullanici)

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

    private fun yemek(
        isim: String,
        tur: String,
        ekleyenKullanici: String
    ): HavuzOgesi = HavuzOgesi(
        id = "",
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
