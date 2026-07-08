package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.Sinerji
import com.sonkarar.domain.repository.SinerjiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OdaOlusturVeyaKatilKullanimi @Inject constructor(
    private val sinerjiRepository: SinerjiRepository
) {
    suspend operator fun invoke(esEposta: String): Sonuc<String> {
        val temiz = esEposta.trim()
        if (temiz.isBlank()) {
            return Sonuc.Hata("Lütfen eşinin e-posta adresini girin.")
        }
        return sinerjiRepository.odaOlusturVeyaKatil(temiz)
    }
}

class TekBasinaBaslatKullanimi @Inject constructor(
    private val sinerjiRepository: SinerjiRepository
) {
    suspend operator fun invoke(): Sonuc<String> = sinerjiRepository.tekBasinaBaslat()
}

class SinerjiyiGozlemleKullanimi @Inject constructor(
    private val sinerjiRepository: SinerjiRepository
) {
    operator fun invoke(sinerjiId: String): Flow<Sonuc<Sinerji>> =
        sinerjiRepository.sinerjiyiGozlemle(sinerjiId)
}

class CarkDurumunuGuncelleKullanimi @Inject constructor(
    private val sinerjiRepository: SinerjiRepository
) {
    suspend operator fun invoke(sinerjiId: String, durum: CarkDurumu): Sonuc<Unit> =
        sinerjiRepository.carkDurumunuGuncelle(sinerjiId, durum)
}

class GecmiseKayitEkleKullanimi @Inject constructor(
    private val sinerjiRepository: SinerjiRepository
) {
    suspend operator fun invoke(
        sinerjiId: String,
        kategori: Kategori,
        sonuc: String
    ): Sonuc<Unit> = sinerjiRepository.gecmiseKayitEkle(sinerjiId, kategori, sonuc)
}
