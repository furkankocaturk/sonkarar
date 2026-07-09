package com.sonkarar.domain.model

import com.sonkarar.cekirdek.Kategori

/** Bir "çark" = bir konu/kategori (Yemek, İzlenecek, Evet/Hayır veya kullanıcı çarkı). */
data class Cark(
    val carkId: String,
    val ad: String,
    val kategori: Kategori,
    val sistemMi: Boolean = false
)
