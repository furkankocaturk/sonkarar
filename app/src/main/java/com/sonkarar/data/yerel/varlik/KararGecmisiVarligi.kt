package com.sonkarar.data.yerel.varlik

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "karar_gecmisi")
data class KararGecmisiVarligi(
    @PrimaryKey(autoGenerate = true) val kayitId: Long = 0L,
    val sinerjiId: String,
    val carkId: String,
    val zamanDamgasi: Long,
    val kategori: String,
    val sonuc: String
)
