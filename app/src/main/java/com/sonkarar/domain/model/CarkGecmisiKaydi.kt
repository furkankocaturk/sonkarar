package com.sonkarar.domain.model

import com.sonkarar.cekirdek.Kategori

data class CarkGecmisiKaydi(
    val zamanDamgasi: Long,
    val carkId: String,
    val kategori: Kategori,
    val sonuc: String
)
