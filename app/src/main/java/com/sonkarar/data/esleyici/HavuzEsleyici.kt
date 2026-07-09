package com.sonkarar.data.esleyici

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.data.firestore.dto.HavuzOgesiDto
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi
import com.sonkarar.domain.model.HavuzOgesi

fun HavuzOgesiDto.domaineDonustur(): HavuzOgesi = HavuzOgesi(
    id = id,
    carkId = kategori, // Bulut modunda çark kimliği kategori adıyla aynıdır.
    kategori = Kategori.anahtardan(kategori),
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi,
    platform = platform,
    puan = puan,
    posterUrl = posterUrl,
    detayUrl = detayUrl,
    kaynakAdi = kaynakAdi,
    favori = favori
)

fun HavuzOgesi.dtoyaDonustur(): HavuzOgesiDto = HavuzOgesiDto(
    id = id,
    kategori = kategori.name,
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi,
    platform = platform,
    puan = puan,
    posterUrl = posterUrl,
    detayUrl = detayUrl,
    kaynakAdi = kaynakAdi,
    favori = favori
)

fun HavuzOgesi.varligaDonustur(sinerjiId: String): HavuzOgesiVarligi = HavuzOgesiVarligi(
    id = id,
    sinerjiId = sinerjiId,
    carkId = carkId.ifBlank { kategori.name },
    kategori = kategori.name,
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi,
    platform = platform,
    puan = puan,
    posterUrl = posterUrl,
    detayUrl = detayUrl,
    kaynakAdi = kaynakAdi,
    favori = favori
)

fun HavuzOgesiVarligi.domaineDonustur(): HavuzOgesi = HavuzOgesi(
    id = id,
    carkId = carkId,
    kategori = Kategori.anahtardan(kategori),
    isim = isim,
    tur = tur,
    ekleyenKullanici = ekleyenKullanici,
    agirlik = agirlik,
    disOneriMi = disOneriMi,
    platform = platform,
    puan = puan,
    posterUrl = posterUrl,
    detayUrl = detayUrl,
    kaynakAdi = kaynakAdi,
    favori = favori
)
