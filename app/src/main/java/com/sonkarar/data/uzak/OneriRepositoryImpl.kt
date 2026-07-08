package com.sonkarar.data.uzak

import com.sonkarar.BuildConfig
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.di.GDagitici
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.OneriRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OneriRepositoryImpl @Inject constructor(
    private val tmdbServisi: TmdbServisi,
    @GDagitici private val dagitici: CoroutineDispatcher
) : OneriRepository {

    // Türkçe tür adı -> TMDB tür kimliği (yaygın türler)
    private val turKimlikHaritasi = mapOf(
        "Bilim Kurgu" to 878,
        "Komedi" to 35,
        "Dram" to 18,
        "Aksiyon" to 28,
        "Korku" to 27,
        "Romantik" to 10749,
        "Animasyon" to 16,
        "Gerilim" to 53
    )

    // TMDB olmadan da çalışabilmek için hazır yemek önerileri.
    private val hazirYemekOnerileri = mapOf(
        "Uzak Doğu" to listOf("Suşi", "Ramen", "Pad Thai"),
        "İtalyan" to listOf("Makarna", "Pizza", "Risotto"),
        "Türk" to listOf("Mantı", "Kebap", "Pide"),
        "Fast Food" to listOf("Hamburger", "Tavuk Dürüm", "Nachos"),
        "Deniz Ürünleri" to listOf("Kalamar", "Levrek", "Midye Dolma")
    )

    override suspend fun oneriUret(
        kategori: Kategori,
        populerTurler: List<String>,
        adet: Int
    ): Sonuc<List<HavuzOgesi>> = when (kategori) {
        Kategori.IZLENECEK -> tmdbOnerileriUret(populerTurler, adet)
        Kategori.YEMEK -> Sonuc.Basarili(yemekOnerileriUret(populerTurler, adet))
    }

    private suspend fun tmdbOnerileriUret(
        populerTurler: List<String>,
        adet: Int
    ): Sonuc<List<HavuzOgesi>> {
        if (BuildConfig.TMDB_API_ANAHTARI.isBlank()) return Sonuc.Basarili(emptyList())

        return guvenliCagri {
            withContext(dagitici) {
                val turKimlikleri = populerTurler
                    .mapNotNull { turKimlikHaritasi[it] }
                    .joinToString(",")
                    .ifBlank { "878" } // varsayılan: Bilim Kurgu

                val yanit = tmdbServisi.filmKesfet(
                    apiAnahtari = BuildConfig.TMDB_API_ANAHTARI,
                    turKimlikleri = turKimlikleri
                )
                yanit.sonuclar
                    .filter { it.baslik.isNotBlank() || it.ad.isNotBlank() }
                    .shuffled()
                    .take(adet)
                    .map { film ->
                        val baslik = film.baslik.ifBlank { film.ad }
                        HavuzOgesi(
                            id = "oneri_${UUID.randomUUID()}",
                            kategori = Kategori.IZLENECEK,
                            isim = baslik,
                            tur = populerTurler.firstOrNull().orEmpty(),
                            ekleyenKullanici = "sistem",
                            agirlik = Sabitler.VARSAYILAN_AGIRLIK,
                            disOneriMi = true,
                            platform = "TMDB",
                            puan = film.puan,
                            posterUrl = film.posterYolu?.let { "https://image.tmdb.org/t/p/w500$it" }.orEmpty(),
                            detayUrl = "https://www.themoviedb.org/movie/${film.id}?language=tr-TR",
                            kaynakAdi = "TMDB"
                        )
                    }
            }
        }
    }

    private fun yemekOnerileriUret(populerTurler: List<String>, adet: Int): List<HavuzOgesi> {
        val kaynak = if (populerTurler.isNotEmpty()) {
            populerTurler.flatMap { hazirYemekOnerileri[it].orEmpty() }
        } else {
            hazirYemekOnerileri.values.flatten()
        }.ifEmpty { hazirYemekOnerileri.values.flatten() }

        return kaynak.shuffled().take(adet).map { isim ->
            HavuzOgesi(
                id = "oneri_${UUID.randomUUID()}",
                kategori = Kategori.YEMEK,
                isim = isim,
                tur = populerTurler.firstOrNull().orEmpty(),
                ekleyenKullanici = "sistem",
                agirlik = Sabitler.VARSAYILAN_AGIRLIK,
                disOneriMi = true,
                detayUrl = yemekTarifiUrlOlustur(isim),
                kaynakAdi = "Nefis Yemek Tarifleri"
            )
        }
    }

    private fun yemekTarifiUrlOlustur(isim: String): String {
        val arama = java.net.URLEncoder.encode(isim, Charsets.UTF_8.name())
        return "https://www.nefisyemektarifleri.com/arama/?s=$arama"
    }
}
