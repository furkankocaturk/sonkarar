package com.sonkarar.data.firestore.dto

data class CarkGecmisiKaydiDto(
    val zamanDamgasi: Long = 0L,
    val carkId: String = "",
    val kategori: String = "YEMEK",
    val sonuc: String = ""
)

data class CarkDurumuDto(
    val durum: String = "BOSTA",
    val ceviren: String = "",
    val carkId: String = "",
    val finalAci: Double = 0.0,
    val kazananIsim: String = "",
    val kategori: String = "YEMEK",
    val tur: Long = 0L,
    val carkOgeleri: List<HavuzOgesiDto> = emptyList()
)

data class SinerjiDto(
    val uyeler: List<String> = emptyList(),
    val carkGecmisi: List<CarkGecmisiKaydiDto> = emptyList(),
    val carkDurumu: CarkDurumuDto = CarkDurumuDto()
)
