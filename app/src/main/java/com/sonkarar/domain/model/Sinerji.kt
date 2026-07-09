package com.sonkarar.domain.model

data class Sinerji(
    val sinerjiId: String,
    val uyeler: List<String> = emptyList(),
    val carkGecmisi: List<CarkGecmisiKaydi> = emptyList(),
    val carkDurumu: CarkDurumu = CarkDurumu()
)
