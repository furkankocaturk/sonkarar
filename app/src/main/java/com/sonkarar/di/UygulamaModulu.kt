package com.sonkarar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GDagitici   // Giriş/çıkış (IO) dağıtıcısı

@Module
@InstallIn(SingletonComponent::class)
object UygulamaModulu {

    @Provides
    @GDagitici
    @Singleton
    fun ioDagiticiSagla(): CoroutineDispatcher = Dispatchers.IO
}
