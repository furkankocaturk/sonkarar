package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.Cark
import kotlinx.coroutines.flow.Flow

interface CarkRepository {
    /** Mevcut tüm çarkları gözlemler (varsayılanlar + kullanıcı çarkları). */
    fun carklariGozlemle(): Flow<List<Cark>>

    suspend fun carkGetir(carkId: String): Cark?

    /**
     * Yeni bir kullanıcı çarkı oluşturur (Genel tip) ve varsayılan olarak
     * "Evet" / "Hayır" seçenekleriyle doldurur. Oluşturulan carkId döner.
     */
    suspend fun carkEkle(ad: String): Sonuc<String>

    suspend fun carkSil(carkId: String): Sonuc<Unit>
}
