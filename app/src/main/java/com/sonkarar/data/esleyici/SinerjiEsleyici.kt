package com.sonkarar.data.esleyici

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.data.firestore.dto.CarkDurumuDto
import com.sonkarar.data.firestore.dto.CarkGecmisiKaydiDto
import com.sonkarar.data.firestore.dto.SinerjiDto
import com.sonkarar.domain.model.CarkAsamasi
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.CarkGecmisiKaydi
import com.sonkarar.domain.model.Sinerji

fun SinerjiDto.domaineDonustur(sinerjiId: String): Sinerji = Sinerji(
    sinerjiId = sinerjiId,
    uyeler = uyeler,
    carkGecmisi = carkGecmisi.map { it.domaineDonustur() },
    carkDurumu = carkDurumu.domaineDonustur()
)

fun CarkGecmisiKaydiDto.domaineDonustur(): CarkGecmisiKaydi = CarkGecmisiKaydi(
    zamanDamgasi = zamanDamgasi,
    carkId = carkId.ifBlank { kategori },
    kategori = Kategori.anahtardan(kategori),
    sonuc = sonuc
)

fun CarkDurumuDto.domaineDonustur(): CarkDurumu = CarkDurumu(
    asama = runCatching { CarkAsamasi.valueOf(durum) }.getOrDefault(CarkAsamasi.BOSTA),
    ceviren = ceviren,
    hedefAci = hedefAci,
    kazananIsim = kazananIsim,
    kategori = Kategori.anahtardan(kategori),
    tur = tur,
    carkOgeleri = carkOgeleri.map { it.domaineDonustur() }
)

fun CarkDurumu.dtoyaDonustur(): CarkDurumuDto = CarkDurumuDto(
    durum = asama.name,
    ceviren = ceviren,
    hedefAci = hedefAci,
    kazananIsim = kazananIsim,
    kategori = kategori.name,
    tur = tur,
    carkOgeleri = carkOgeleri.map { it.dtoyaDonustur() }
)
