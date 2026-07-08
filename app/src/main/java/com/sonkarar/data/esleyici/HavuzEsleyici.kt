package com.sonkarar.data.esleyici

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.data.firestore.dto.HavuzOgesiDto
import com.sonkarar.data.yerel.varlik.HavuzOgesiVarligi
import com.sonkarar.domain.model.HavuzOgesi

fun HavuzOgesiDto.domaineDonustur(): HavuzOgesi = HavuzOgesi(
    id = id,
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
    kaynakAdi = kaynakAdi
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
    kaynakAdi = kaynakAdi
)

fun HavuzOgesi.varligaDonustur(sinerjiId: String): HavuzOgesiVarligi = HavuzOgesiVarligi(
    id = id,
    sinerjiId = sinerjiId,
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
    kaynakAdi = kaynakAdi
)

fun HavuzOgesiVarligi.domaineDonustur(): HavuzOgesi = HavuzOgesi(
    id = id,
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
    kaynakAdi = kaynakAdi
)
