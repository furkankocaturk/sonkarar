package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.Cark
import com.sonkarar.domain.repository.CarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CarklariGozlemleKullanimi @Inject constructor(
    private val carkRepository: CarkRepository
) {
    operator fun invoke(): Flow<List<Cark>> = carkRepository.carklariGozlemle()
}

class CarkGetirKullanimi @Inject constructor(
    private val carkRepository: CarkRepository
) {
    suspend operator fun invoke(carkId: String): Cark? = carkRepository.carkGetir(carkId)
}

class CarkEkleKullanimi @Inject constructor(
    private val carkRepository: CarkRepository
) {
    suspend operator fun invoke(ad: String, kategori: Kategori): Sonuc<String> =
        carkRepository.carkEkle(ad, kategori)
}

class CarkSilKullanimi @Inject constructor(
    private val carkRepository: CarkRepository
) {
    suspend operator fun invoke(carkId: String): Sonuc<Unit> = carkRepository.carkSil(carkId)
}
