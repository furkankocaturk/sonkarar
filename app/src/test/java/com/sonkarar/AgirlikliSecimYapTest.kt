package com.sonkarar

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.kullanim.AgirlikliSecimYap
import com.sonkarar.domain.model.HavuzOgesi
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AgirlikliSecimYapTest {

    private val secim = AgirlikliSecimYap()

    private fun oge(isim: String, agirlik: Int) = HavuzOgesi(
        id = isim,
        kategori = Kategori.YEMEK,
        isim = isim,
        ekleyenKullanici = "test",
        agirlik = agirlik
    )

    @Test
    fun `bos liste null doner`() {
        assertNull(secim(emptyList()))
    }

    @Test
    fun `tek oge her zaman secilir`() {
        val ogeler = listOf(oge("Tek", 10))
        repeat(50) {
            val secilen = secim(ogeler, Random(it))
            assertTrue(secilen?.isim == "Tek")
        }
    }

    @Test
    fun `yuksek agirlik belirgin daha sik secilir`() {
        val ogeler = listOf(oge("A", 90), oge("B", 10))
        val sayac = mutableMapOf("A" to 0, "B" to 0)
        val rastgele = Random(42)
        repeat(10_000) {
            val secilen = secim(ogeler, rastgele)!!
            sayac[secilen.isim] = sayac.getValue(secilen.isim) + 1
        }
        assertTrue(
            "A (%90 agirlik) B'den en az 5 kat fazla secilmeli",
            sayac.getValue("A") > sayac.getValue("B") * 5
        )
    }
}
