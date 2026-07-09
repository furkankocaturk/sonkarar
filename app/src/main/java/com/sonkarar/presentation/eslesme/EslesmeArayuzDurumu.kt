package com.sonkarar.presentation.eslesme

data class EslesmeArayuzDurumu(
    val esEposta: String = "",
    val yukleniyor: Boolean = false,
    val hataMesaji: String? = null,
    val tamamlandi: Boolean = false
)
