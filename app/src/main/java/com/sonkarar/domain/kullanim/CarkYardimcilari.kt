package com.sonkarar.domain.kullanim

import com.sonkarar.domain.model.HavuzOgesi

/** Havuzdaki ögelerin `tur` frekansına göre en popüler türleri döner. */
fun populerTurleriBul(ogeler: List<HavuzOgesi>, adet: Int = 3): List<String> =
    ogeler
        .map { it.tur }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(adet)
        .map { it.key }
