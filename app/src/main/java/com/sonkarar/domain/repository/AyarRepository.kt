package com.sonkarar.domain.repository

import com.sonkarar.cekirdek.TemaModu
import kotlinx.coroutines.flow.Flow

interface AyarRepository {
    fun temaModunuGozlemle(): Flow<TemaModu>
    fun temaAyarla(mod: TemaModu)
}
