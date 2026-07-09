package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.Kullanici
import kotlinx.coroutines.flow.Flow

interface KimlikRepository {
    /** Aktif oturumu (ve eşleşme durumunu) gözlemler; oturum yoksa null yayar. */
    fun aktifKullaniciyiGozlemle(): Flow<Kullanici?>

    /** Google kimlik jetonu ile Firebase'e giriş yapar. */
    suspend fun googleIleGirisYap(kimlikJetonu: String): Sonuc<Kullanici>

    suspend fun oturumuKapat(): Sonuc<Unit>

    /** Girişsiz, anahtarsız çevrimdışı tek kişi moduna geçer. */
    suspend fun yerelModaGec(): Sonuc<Unit>

    /** Çevrimdışı modun açık olup olmadığı. */
    fun yerelModAktif(): Boolean
}
