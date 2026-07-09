package com.sonkarar.data.yerel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sonkarar.data.yerel.varlik.CarkVarligi
import kotlinx.coroutines.flow.Flow

@Dao
interface CarkDao {

    @Query("SELECT * FROM carklar WHERE sinerjiId = :sinerjiId ORDER BY siraNo ASC")
    fun gozlemle(sinerjiId: String): Flow<List<CarkVarligi>>

    @Query("SELECT * FROM carklar WHERE carkId = :carkId LIMIT 1")
    suspend fun getir(carkId: String): CarkVarligi?

    @Query("SELECT COUNT(*) FROM carklar WHERE sinerjiId = :sinerjiId")
    suspend fun sayi(sinerjiId: String): Int

    @Query("SELECT COALESCE(MAX(siraNo), 0) FROM carklar WHERE sinerjiId = :sinerjiId")
    suspend fun enBuyukSira(sinerjiId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ekle(cark: CarkVarligi)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun topluEkle(carklar: List<CarkVarligi>)

    @Query("DELETE FROM carklar WHERE carkId = :carkId")
    suspend fun sil(carkId: String)

    @Query("DELETE FROM havuz_ogeleri WHERE carkId = :carkId")
    suspend fun carkOgeleriniSil(carkId: String)
}
