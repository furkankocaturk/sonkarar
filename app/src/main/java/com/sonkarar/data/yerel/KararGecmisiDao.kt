package com.sonkarar.data.yerel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sonkarar.data.yerel.varlik.KararGecmisiVarligi
import kotlinx.coroutines.flow.Flow

@Dao
interface KararGecmisiDao {

    @Query("SELECT * FROM karar_gecmisi WHERE sinerjiId = :sinerjiId ORDER BY zamanDamgasi DESC")
    fun gozlemle(sinerjiId: String): Flow<List<KararGecmisiVarligi>>

    @Insert
    suspend fun ekle(kayit: KararGecmisiVarligi)
}
