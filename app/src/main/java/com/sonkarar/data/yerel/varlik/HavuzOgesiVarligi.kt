package com.sonkarar.data.yerel.varlik

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "havuz_ogeleri")
data class HavuzOgesiVarligi(
    @PrimaryKey val id: String,
    val sinerjiId: String,
    val kategori: String,
    val isim: String,
    val tur: String,
    val ekleyenKullanici: String,
    val agirlik: Int,
    val disOneriMi: Boolean,
    val platform: String,
    val puan: Double?,
    val posterUrl: String,
    val detayUrl: String,
    val kaynakAdi: String
)
