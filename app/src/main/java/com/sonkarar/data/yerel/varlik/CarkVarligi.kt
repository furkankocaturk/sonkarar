package com.sonkarar.data.yerel.varlik

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carklar")
data class CarkVarligi(
    @PrimaryKey val carkId: String,
    val sinerjiId: String,
    val ad: String,
    val kategoriTipi: String,
    val sistemMi: Boolean,
    val siraNo: Int
)
