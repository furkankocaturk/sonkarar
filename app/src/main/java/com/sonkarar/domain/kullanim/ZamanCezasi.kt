package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.domain.model.HavuzOgesi
import kotlin.math.roundToInt

object ZamanCezasi {
    /**
     * Son [Sabitler.ZAMAN_CEZASI_TUR_SAYISI] turda (ilgili çark için) çıkan
     * ögelerin ağırlığını [Sabitler.ZAMAN_CEZASI_ORANI] katına indirir (%80 düşür).
     */
    fun uygula(
        ogeler: List<HavuzOgesi>,
        gecmis: List<CarkGecmisiKaydi>,
        carkId: String
    ): List<HavuzOgesi> {
        val sonSonuclar = gecmis
            .filter { it.carkId == carkId }
            .sortedByDescending { it.zamanDamgasi }
            .take(Sabitler.ZAMAN_CEZASI_TUR_SAYISI)
            .map { it.sonuc }
            .toSet()

        if (sonSonuclar.isEmpty()) return ogeler

        return ogeler.map { oge ->
            if (oge.isim in sonSonuclar) {
                val cezali = (oge.agirlik * Sabitler.ZAMAN_CEZASI_ORANI).roundToInt()
                oge.copy(agirlik = cezali.coerceAtLeast(1))
            } else {
                oge
            }
        }
    }
}
