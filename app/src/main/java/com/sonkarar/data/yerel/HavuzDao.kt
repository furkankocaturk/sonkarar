package com.sonkarar.data.yerel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi
import kotlinx.coroutines.flow.Flow

@Dao
interface HavuzDao {

    @Query("SELECT * FROM havuz_ogeleri WHERE sinerjiId = :sinerjiId AND carkId = :carkId")
    fun ogeleriGozlemle(sinerjiId: String, carkId: String): Flow<List<HavuzOgesiVarligi>>

    @Query("SELECT * FROM havuz_ogeleri WHERE sinerjiId = :sinerjiId AND carkId = :carkId")
    suspend fun ogeleriGetir(sinerjiId: String, carkId: String): List<HavuzOgesiVarligi>

    @Query("SELECT COUNT(*) FROM havuz_ogeleri WHERE sinerjiId = :sinerjiId")
    suspend fun ogeSayisi(sinerjiId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ogeleriYaz(ogeler: List<HavuzOgesiVarligi>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ogeYaz(oge: HavuzOgesiVarligi)

    @Query("DELETE FROM havuz_ogeleri WHERE sinerjiId = :sinerjiId AND carkId = :carkId")
    suspend fun carkKategoriyiTemizle(sinerjiId: String, carkId: String)

    @Query("DELETE FROM havuz_ogeleri WHERE id = :ogeId")
    suspend fun ogeSil(ogeId: String)
}
