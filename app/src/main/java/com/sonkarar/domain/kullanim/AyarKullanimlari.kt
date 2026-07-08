package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.TemaModu
import com.sonkarar.domain.repository.AyarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TemaModunuGozlemleKullanimi @Inject constructor(
    private val ayarRepository: AyarRepository
) {
    operator fun invoke(): Flow<TemaModu> = ayarRepository.temaModunuGozlemle()
}

class TemaAyarlaKullanimi @Inject constructor(
    private val ayarRepository: AyarRepository
) {
    operator fun invoke(mod: TemaModu) = ayarRepository.temaAyarla(mod)
}
