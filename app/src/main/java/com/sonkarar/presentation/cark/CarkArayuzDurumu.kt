package com.sonkarar.presentation.cark

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.model.HavuzOgesi

data class CarkArayuzDurumu(
    val sinerjiId: String = "",
    val kullaniciId: String = "",
    val aktifKategori: Kategori = Kategori.YEMEK,
    val yemekler: List<HavuzOgesi> = emptyList(),
    val izlenecekler: List<HavuzOgesi> = emptyList(),
    val carkOgeleri: List<HavuzOgesi> = emptyList(),
    val donuyorMu: Boolean = false,
    val hedefAci: Float = 0f,
    val benCeviriyorum: Boolean = false,
    val kazananIsim: String? = null,
    val sonucGosteriliyor: Boolean = false,
    val konfetiTetikleyici: Long = 0L,
    val hataMesaji: String? = null
) {
    val gorunenOgeler: List<HavuzOgesi>
        get() = if (aktifKategori == Kategori.YEMEK) yemekler else izlenecekler

    val cizilecekOgeler: List<HavuzOgesi>
        get() = carkOgeleri.ifEmpty { gorunenOgeler }

    val kazananOge: HavuzOgesi?
        get() = kazananIsim?.let { isim -> cizilecekOgeler.firstOrNull { it.isim == isim } }
}
