package com.sonkarar.domain.kullanim

import com.sonkarar.domain.model.HavuzOgesi
import javax.inject.Inject
import kotlin.random.Random

class AgirlikliSecimYap @Inject constructor() {

    /**
     * [ogeler] listesinden ağırlıkla orantılı biçimde rastgele bir öge seçer.
     * Ağırlıklar en az 1 olacak biçimde tabanlanır. Boş liste durumunda null döner.
     */
    operator fun invoke(
        ogeler: List<HavuzOgesi>,
        rastgele: Random = Random.Default
    ): HavuzOgesi? {
        if (ogeler.isEmpty()) return null

        val guvenliAgirliklar = ogeler.map { it.agirlik.coerceAtLeast(1) }
        val toplam = guvenliAgirliklar.sum()
        if (toplam <= 0) return ogeler.random(rastgele)

        var esik = rastgele.nextInt(1, toplam + 1)
        for (indeks in ogeler.indices) {
            esik -= guvenliAgirliklar[indeks]
            if (esik <= 0) return ogeler[indeks]
        }
        return ogeler.last()
    }
}
