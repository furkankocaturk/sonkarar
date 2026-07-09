package com.sonkarar.data.esleyici

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.data.yerel.varlik.CarkVarligi
import com.sonkarar.domain.model.Cark

fun CarkVarligi.domaineDonustur(): Cark = Cark(
    carkId = carkId,
    ad = ad,
    kategori = Kategori.anahtardan(kategoriTipi),
    sistemMi = sistemMi
)
