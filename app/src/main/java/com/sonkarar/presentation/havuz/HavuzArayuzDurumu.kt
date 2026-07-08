package com.sonkarar.presentation.havuz

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.model.HavuzOgesi

data class HavuzArayuzDurumu(
    val sinerjiId: String = "",
    val kullaniciId: String = "",
    val aktifKategori: Kategori = Kategori.YEMEK,
    val yemekler: List<HavuzOgesi> = emptyList(),
    val izlenecekler: List<HavuzOgesi> = emptyList(),
    val yeniOgeMetni: String = "",
    val hataMesaji: String? = null
) {
    val gorunenOgeler: List<HavuzOgesi>
        get() = if (aktifKategori == Kategori.YEMEK) yemekler else izlenecekler
}
