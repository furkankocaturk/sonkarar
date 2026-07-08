package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.HavuzRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HavuzuGozlemleKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository
) {
    operator fun invoke(sinerjiId: String, kategori: Kategori): Flow<List<HavuzOgesi>> =
        havuzRepository.havuzuGozlemle(sinerjiId, kategori)
}

class SenkronizasyonuBaslatKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository
) {
    operator fun invoke(sinerjiId: String, kategori: Kategori): Flow<Sonuc<Unit>> =
        havuzRepository.senkronizasyonuBaslat(sinerjiId, kategori)
}

class HavuzaOgeEkleKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository
) {
    suspend operator fun invoke(
        sinerjiId: String,
        kategori: Kategori,
        isim: String,
        ekleyenKullanici: String
    ): Sonuc<Unit> {
        val temiz = isim.trim()
        if (temiz.isBlank()) {
            return Sonuc.Hata("Lütfen bir isim girin.")
        }
        val oge = HavuzOgesi(
            id = "",
            kategori = kategori,
            isim = temiz,
            ekleyenKullanici = ekleyenKullanici,
            agirlik = Sabitler.VARSAYILAN_AGIRLIK,
            disOneriMi = false
        )
        return havuzRepository.ogeEkle(sinerjiId, oge)
    }
}

class HavuzdanOgeSilKullanimi @Inject constructor(
    private val havuzRepository: HavuzRepository
) {
    suspend operator fun invoke(sinerjiId: String, ogeId: String): Sonuc<Unit> =
        havuzRepository.ogeSil(sinerjiId, ogeId)
}
