package com.sonkarar.data.varsayilan

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.domain.model.HavuzOgesi

object OntanimliHavuz {

    fun ogeleriOlustur(ekleyenKullanici: String): List<HavuzOgesi> =
        yemekler(ekleyenKullanici) + izlenecekler(ekleyenKullanici)

    private fun yemekler(ekleyenKullanici: String): List<HavuzOgesi> = listOf(
        yemek("Makarna", "İtalyan", "https://www.nefisyemektarifleri.com/arama/?s=makarna", ekleyenKullanici),
        yemek("Pizza", "İtalyan", "https://www.nefisyemektarifleri.com/arama/?s=pizza", ekleyenKullanici),
        yemek("Mantı", "Türk", "https://www.nefisyemektarifleri.com/arama/?s=mant%C4%B1", ekleyenKullanici),
        yemek("Köfte", "Türk", "https://www.nefisyemektarifleri.com/arama/?s=k%C3%B6fte", ekleyenKullanici),
        yemek("Ramen", "Uzak Doğu", "https://www.nefisyemektarifleri.com/arama/?s=ramen", ekleyenKullanici),
        yemek("Suşi", "Uzak Doğu", "https://www.nefisyemektarifleri.com/arama/?s=su%C5%9Fi", ekleyenKullanici),
        yemek("Hamburger", "Fast Food", "https://www.nefisyemektarifleri.com/arama/?s=hamburger", ekleyenKullanici),
        yemek("Tavuk Dürüm", "Fast Food", "https://www.nefisyemektarifleri.com/arama/?s=tavuk%20d%C3%BCr%C3%BCm", ekleyenKullanici)
    )

    private fun izlenecekler(ekleyenKullanici: String): List<HavuzOgesi> = listOf(
        izlenecek("Prens", "Komedi", "BluTV", 8.4, "https://www.themoviedb.org/tv/228853-prens", ekleyenKullanici),
        izlenecek("Inception", "Bilim Kurgu", "Netflix / Prime Video", 8.8, "https://www.themoviedb.org/movie/27205-inception", ekleyenKullanici),
        izlenecek("Interstellar", "Bilim Kurgu", "Prime Video", 8.7, "https://www.themoviedb.org/movie/157336-interstellar", ekleyenKullanici),
        izlenecek("The Office", "Komedi", "Netflix", 9.0, "https://www.themoviedb.org/tv/2316-the-office", ekleyenKullanici),
        izlenecek("Şahsiyet", "Dram", "GAİN", 9.0, "https://www.themoviedb.org/tv/81949-sahsiyet", ekleyenKullanici),
        izlenecek("Gibi", "Komedi", "Exxen", 9.0, "https://www.themoviedb.org/tv/120489-gibi", ekleyenKullanici),
        izlenecek("The Bear", "Dram", "Disney+", 8.5, "https://www.themoviedb.org/tv/136315-the-bear", ekleyenKullanici),
        izlenecek("Dune: Part Two", "Bilim Kurgu", "Max", 8.5, "https://www.themoviedb.org/movie/693134-dune-part-two", ekleyenKullanici)
    )

    private fun yemek(
        isim: String,
        tur: String,
        detayUrl: String,
        ekleyenKullanici: String
    ): HavuzOgesi = HavuzOgesi(
        id = "",
        kategori = Kategori.YEMEK,
        isim = isim,
        tur = tur,
        ekleyenKullanici = ekleyenKullanici,
        agirlik = Sabitler.VARSAYILAN_AGIRLIK,
        kaynakAdi = "Nefis Yemek Tarifleri",
        detayUrl = detayUrl
    )

    private fun izlenecek(
        isim: String,
        tur: String,
        platform: String,
        puan: Double,
        detayUrl: String,
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
        detayUrl = detayUrl,
        kaynakAdi = "TMDB"
    )
}
