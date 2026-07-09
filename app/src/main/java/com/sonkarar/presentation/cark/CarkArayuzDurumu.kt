package com.sonkarar.presentation.cark

import com.sonkarar.domain.model.Cark
import com.sonkarar.domain.model.HavuzOgesi

data class CarkArayuzDurumu(
    val sinerjiId: String = "",
    val kullaniciId: String = "",
    val cark: Cark? = null,
    val ogeler: List<HavuzOgesi> = emptyList(),
    val donuyorMu: Boolean = false,
    val hedefAci: Float = 0f,
    val benCeviriyorum: Boolean = false,
    val kazananIsim: String? = null,
    val carkOgeleri: List<HavuzOgesi> = emptyList(),
    val sonucGosteriliyor: Boolean = false,
    val konfetiTetikleyici: Long = 0L,
    val hataMesaji: String? = null,
    val bilgiMesaji: String? = null
) {
    val cizilecekOgeler: List<HavuzOgesi>
        get() = carkOgeleri.ifEmpty { ogeler }

    val kazananOge: HavuzOgesi?
        get() = kazananIsim?.let { isim -> cizilecekOgeler.firstOrNull { it.isim == isim } }
}
