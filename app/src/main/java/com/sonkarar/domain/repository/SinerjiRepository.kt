package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.Sinerji
import kotlinx.coroutines.flow.Flow

interface SinerjiRepository {
    /**
     * Eş e-postasıyla oda oluşturur veya var olan bekleyen odaya katılır.
     * Başarılıysa sinerjiId döner.
     */
    suspend fun odaOlusturVeyaKatil(esEposta: String): Sonuc<String>

    fun sinerjiyiGozlemle(sinerjiId: String): Flow<Sonuc<Sinerji>>

    suspend fun carkDurumunuGuncelle(sinerjiId: String, durum: CarkDurumu): Sonuc<Unit>

    suspend fun gecmiseKayitEkle(
        sinerjiId: String,
        kategori: Kategori,
        sonuc: String
    ): Sonuc<Unit>
}
