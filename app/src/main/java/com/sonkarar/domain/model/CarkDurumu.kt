package com.sonkarar.domain.model

import com.sonkarar.cekirdek.Kategori

enum class CarkAsamasi { BOSTA, CEVRILIYOR, SONUC }

data class CarkDurumu(
    val asama: CarkAsamasi = CarkAsamasi.BOSTA,
    val ceviren: String = "",
    val carkId: String = "",
    val finalAci: Double = 0.0,
    val kazananIsim: String = "",
    val kategori: Kategori = Kategori.YEMEK,
    val tur: Long = 0L,
    val carkOgeleri: List<HavuzOgesi> = emptyList()
)
