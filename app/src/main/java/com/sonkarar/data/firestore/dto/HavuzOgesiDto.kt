package com.sonkarar.data.firestore.dto

data class HavuzOgesiDto(
    val id: String = "",
    val kategori: String = "YEMEK",
    val isim: String = "",
    val tur: String = "",
    val ekleyenKullanici: String = "",
    val agirlik: Int = 10,
    val disOneriMi: Boolean = false,
    val platform: String = "",
    val puan: Double? = null,
    val posterUrl: String = "",
    val detayUrl: String = "",
    val kaynakAdi: String = ""
)
