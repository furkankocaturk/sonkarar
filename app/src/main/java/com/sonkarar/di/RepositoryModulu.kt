package com.sonkarar.di

import com.sonkarar.data.ayar.AyarRepositoryImpl
import com.sonkarar.data.ayar.CarkRepositoryImpl
import com.sonkarar.data.firestore.HavuzRepositoryImpl
import com.sonkarar.data.firestore.SinerjiRepositoryImpl
import com.sonkarar.data.kimlik.KimlikRepositoryImpl
import com.sonkarar.data.uzak.OneriRepositoryImpl
import com.sonkarar.domain.repository.AyarRepository
import com.sonkarar.domain.repository.CarkRepository
import com.sonkarar.domain.repository.HavuzRepository
import com.sonkarar.domain.repository.KimlikRepository
import com.sonkarar.domain.repository.OneriRepository
import com.sonkarar.domain.repository.SinerjiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModulu {

    @Binds
    @Singleton
    abstract fun kimlikRepositoryBagla(impl: KimlikRepositoryImpl): KimlikRepository

    @Binds
    @Singleton
    abstract fun sinerjiRepositoryBagla(impl: SinerjiRepositoryImpl): SinerjiRepository

    @Binds
    @Singleton
    abstract fun havuzRepositoryBagla(impl: HavuzRepositoryImpl): HavuzRepository

    @Binds
    @Singleton
    abstract fun oneriRepositoryBagla(impl: OneriRepositoryImpl): OneriRepository

    @Binds
    @Singleton
    abstract fun ayarRepositoryBagla(impl: AyarRepositoryImpl): AyarRepository

    @Binds
    @Singleton
    abstract fun carkRepositoryBagla(impl: CarkRepositoryImpl): CarkRepository
}
