package com.sonkarar.data.ayar

import com.sonkarar.cekirdek.TemaModu
import com.sonkarar.data.tercih.AppTercihleri
import com.sonkarar.domain.repository.AyarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AyarRepositoryImpl @Inject constructor(
    private val tercihler: AppTercihleri
) : AyarRepository {

    override fun temaModunuGozlemle(): Flow<TemaModu> = tercihler.temaModuAkisi()

    override fun temaAyarla(mod: TemaModu) {
        tercihler.temaModu = mod
    }
}
