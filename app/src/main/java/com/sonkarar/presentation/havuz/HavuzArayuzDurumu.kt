package com.sonkarar.presentation.havuz

import com.sonkarar.domain.model.Cark
import com.sonkarar.domain.model.HavuzOgesi

data class HavuzArayuzDurumu(
    val sinerjiId: String = "",
    val kullaniciId: String = "",
    val cark: Cark? = null,
    val ogeler: List<HavuzOgesi> = emptyList(),
    val yeniOgeMetni: String = "",
    val yeniOgeTuru: String = "",
    val hataMesaji: String? = null
)
