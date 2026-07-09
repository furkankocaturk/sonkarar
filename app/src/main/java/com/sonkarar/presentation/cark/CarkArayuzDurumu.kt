package com.sonkarar.presentation.cark

import com.sonkarar.domain.model.Cark
import com.sonkarar.domain.model.HavuzOgesi

data class CarkArayuzDurumu(
    val sinerjiId: String = "",
    val kullaniciId: String = "",
    val cark: Cark? = null,
    val carkOgeleri: List<HavuzOgesi> = emptyList(),
    val ogeSayisi: Int = 0,
    val donuyorMu: Boolean = false,
    val benCeviriyorum: Boolean = false,
    val disHedefAci: Float? = null,
    val disTur: Long = 0L,
    val otomatikTetik: Long = 0L,
    val kazananIsim: String? = null,
    val sonucGosteriliyor: Boolean = false,
    val konfetiTetikleyici: Long = 0L,
    val hataMesaji: String? = null,
    val bilgiMesaji: String? = null
) {
    val kazananOge: HavuzOgesi?
        get() = kazananIsim?.let { isim -> carkOgeleri.firstOrNull { it.isim == isim } }

    val cevrilebilir: Boolean get() = carkOgeleri.size >= 2 && !donuyorMu
}
