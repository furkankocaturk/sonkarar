package com.sonkarar

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.kullanim.ZamanCezasi
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.domain.model.HavuzOgesi
import org.junit.Assert.assertEquals
import org.junit.Test

class ZamanCezasiTest {

    private fun oge(isim: String, agirlik: Int = 10) = HavuzOgesi(
        id = isim,
        carkId = "YEMEK",
        kategori = Kategori.YEMEK,
        isim = isim,
        ekleyenKullanici = "test",
        agirlik = agirlik
    )

    private fun kayit(isim: String, zaman: Long, carkId: String = "YEMEK") =
        CarkGecmisiKaydi(
            zamanDamgasi = zaman,
            carkId = carkId,
            kategori = Kategori.YEMEK,
            sonuc = isim
        )

    @Test
    fun `son 3 turda cikan ogenin agirligi yuzde 20ye iner`() {
        val ogeler = listOf(oge("Suşi", 100), oge("Pizza", 100))
        val gecmis = listOf(
            kayit("Suşi", 3),
            kayit("Suşi", 2),
            kayit("Suşi", 1)
        )
        val sonuc = ZamanCezasi.uygula(ogeler, gecmis, "YEMEK")
        assertEquals(20, sonuc.first { it.isim == "Suşi" }.agirlik)
        assertEquals(100, sonuc.first { it.isim == "Pizza" }.agirlik)
    }

    @Test
    fun `cezali agirlik en az 1 olur`() {
        val ogeler = listOf(oge("Az", 2))
        val gecmis = listOf(kayit("Az", 1))
        val sonuc = ZamanCezasi.uygula(ogeler, gecmis, "YEMEK")
        assertEquals(1, sonuc.first().agirlik)
    }

    @Test
    fun `farkli cark gecmisi ceza tetiklemez`() {
        val ogeler = listOf(oge("Suşi", 100))
        val gecmis = listOf(kayit("Suşi", 1, carkId = "IZLENECEK"))
        val sonuc = ZamanCezasi.uygula(ogeler, gecmis, "YEMEK")
        assertEquals(100, sonuc.first().agirlik)
    }

    @Test
    fun `sadece son 3 tur dikkate alinir`() {
        val ogeler = listOf(oge("Eski", 100))
        val gecmis = listOf(
            kayit("A", 4),
            kayit("B", 3),
            kayit("C", 2),
            kayit("Eski", 1)
        )
        val sonuc = ZamanCezasi.uygula(ogeler, gecmis, "YEMEK")
        assertEquals(100, sonuc.first().agirlik)
    }
}
