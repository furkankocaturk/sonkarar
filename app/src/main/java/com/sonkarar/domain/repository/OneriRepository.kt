package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.HavuzOgesi

interface OneriRepository {
    /**
     * Çiftin en sık tercih ettiği türlere göre [adet] kadar dış öneri üretir.
     * IZLENECEK -> TMDB, YEMEK -> hazır yemek etiketleri.
     * Hata durumunda boş liste döner (çark yine de çalışabilmeli).
     */
    suspend fun oneriUret(
        kategori: Kategori,
        populerTurler: List<String>,
        adet: Int
    ): Sonuc<List<HavuzOgesi>>
}
