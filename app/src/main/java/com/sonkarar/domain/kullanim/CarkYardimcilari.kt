package com.sonkarar.domain.kullanim

import com.sonkarar.domain.model.HavuzOgesi
import kotlin.random.Random

/**
 * Seçilen dilim indeksinden çarkın duracağı hedef açıyı (derece) hesaplar.
 * Her çevirişte tur sayısı ve dilim içi konum rastgele değişir; böylece aynı
 * sonuç bile her seferinde FARKLI görünür ve gerçekten rastgele hissettirir.
 * Çark saat yönünde döner, gösterge tepede (12 yönü) kabul edilir.
 */
fun hedefAciHesapla(
    secilenIndeks: Int,
    toplamDilim: Int,
    rastgele: Random = Random.Default
): Double {
    if (toplamDilim <= 0) return 0.0
    val dilimAcisi = 360.0 / toplamDilim
    val dilimMerkezi = secilenIndeks * dilimAcisi + dilimAcisi / 2.0
    // Dilim içinde küçük bir sapma (merkeze çok yakın durmasın, ama dilimden çıkmasın).
    val jitter = (rastgele.nextDouble() - 0.5) * dilimAcisi * 0.6
    // Rastgele tam tur sayısı (4..7) -> dönüş miktarı her seferinde değişir.
    val turSayisi = rastgele.nextInt(4, 8)
    return turSayisi * 360.0 + (360.0 - dilimMerkezi + jitter)
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
