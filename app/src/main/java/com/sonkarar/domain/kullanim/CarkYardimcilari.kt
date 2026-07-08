package com.sonkarar.domain.kullanim

import com.sonkarar.domain.model.HavuzOgesi

/**
 * Seçilen dilim indeksinden çarkın duracağı hedef açıyı (derece) hesaplar.
 * Çark saat yönünde döner, gösterge tepede (12 yönü) kabul edilir.
 */
fun hedefAciHesapla(secilenIndeks: Int, toplamDilim: Int, turSayisi: Int = 5): Double {
    if (toplamDilim <= 0) return 0.0
    val dilimAcisi = 360.0 / toplamDilim
    val dilimMerkezi = secilenIndeks * dilimAcisi + dilimAcisi / 2.0
    return turSayisi * 360.0 + (360.0 - dilimMerkezi)
}

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
