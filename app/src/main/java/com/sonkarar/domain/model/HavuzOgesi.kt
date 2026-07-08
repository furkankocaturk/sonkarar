package com.sonkarar.domain.model

import com.sonkarar.cekirdek.Kategori

data class HavuzOgesi(
    val id: String,
    val kategori: Kategori,
    val isim: String,
    val tur: String = "",
    val ekleyenKullanici: String,
    val agirlik: Int = 10,
    val disOneriMi: Boolean = false
)
