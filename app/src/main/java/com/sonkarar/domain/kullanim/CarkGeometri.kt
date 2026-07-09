package com.sonkarar.domain.kullanim

import kotlin.random.Random

/**
 * Çark geometrisi: dilim boyutları ağırlıkla orantılıdır (ağırlığı büyük öge
 * daha büyük dilim = çıkma ihtimali daha yüksek). Gösterge tepededir (12 yönü);
 * çark [rotasyon] derece saat yönünde döndürülür.
 */
object CarkGeometri {

    fun sweepler(agirliklar: List<Int>): List<Float> {
        if (agirliklar.isEmpty()) return emptyList()
        val guvenli = agirliklar.map { it.coerceAtLeast(1) }
        val toplam = guvenli.sum().toFloat()
        return guvenli.map { 360f * it / toplam }
    }

    fun baslangiclar(sweepler: List<Float>): List<Float> {
        var acc = 0f
        return sweepler.map { s -> acc.also { acc += s } }
    }

    /** [rotasyon] derece dönmüş çarkta göstergenin (tepe) altındaki dilim indeksi. */
    fun kazananIndeks(agirliklar: List<Int>, rotasyon: Float): Int {
        val sweepler = sweepler(agirliklar)
        if (sweepler.isEmpty()) return -1
        val yerel = (((-rotasyon) % 360f) + 360f) % 360f
        var acc = 0f
        for (i in sweepler.indices) {
            acc += sweepler[i]
            if (yerel < acc) return i
        }
        return sweepler.lastIndex
    }

    /**
     * Uzaktaki cihazın da aynı sonuca dönmesi için: verilen [indeks]'i göstergenin
     * altına getirecek hedef açı (birkaç tam tur + dilim içinde küçük sapma).
     */
    fun indeksIcinAci(
        agirliklar: List<Int>,
        indeks: Int,
        rastgele: Random = Random.Default
    ): Float {
        val sweepler = sweepler(agirliklar)
        if (sweepler.isEmpty()) return 0f
        val baslangic = baslangiclar(sweepler)
        val merkez = baslangic[indeks] + sweepler[indeks] / 2f
        val jitter = (rastgele.nextFloat() - 0.5f) * sweepler[indeks] * 0.6f
        val turSayisi = rastgele.nextInt(4, 8)
        return turSayisi * 360f + (360f - merkez + jitter)
    }
}
