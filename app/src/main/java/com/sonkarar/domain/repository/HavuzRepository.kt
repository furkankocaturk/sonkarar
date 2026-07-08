package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.HavuzOgesi
import kotlinx.coroutines.flow.Flow

interface HavuzRepository {
    /** Room'u kaynak alan çevrimdışı-dayanıklı akış. */
    fun havuzuGozlemle(sinerjiId: String, kategori: Kategori): Flow<List<HavuzOgesi>>

    /** Firestore dinleyicisini başlatır; gelen veriyi Room'a yansıtır. */
    fun senkronizasyonuBaslat(sinerjiId: String, kategori: Kategori): Flow<Sonuc<Unit>>

    suspend fun ogeEkle(sinerjiId: String, oge: HavuzOgesi): Sonuc<Unit>

    /** Var olan bir ögeyi (favori, ağırlık vb.) günceller. */
    suspend fun ogeGuncelle(sinerjiId: String, oge: HavuzOgesi): Sonuc<Unit>

    suspend fun ogeSil(sinerjiId: String, ogeId: String): Sonuc<Unit>

    /** Anlık ağırlıklı çark hesabı için tüm kategoriyi tek seferlik getirir. */
    suspend fun kategoriyiGetir(sinerjiId: String, kategori: Kategori): Sonuc<List<HavuzOgesi>>
}
