package com.sonkarar.presentation.giris

data class GirisArayuzDurumu(
    val yukleniyor: Boolean = false,
    val hataMesaji: String? = null,
    val girisBasarili: Boolean = false
)
