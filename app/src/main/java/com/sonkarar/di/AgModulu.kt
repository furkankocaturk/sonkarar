package com.sonkarar.di

import com.sonkarar.BuildConfig
import com.sonkarar.data.uzak.TmdbServisi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgModulu {

    private const val TMDB_TEMEL_URL = "https://api.themoviedb.org/3/"

    @Provides
    @Singleton
    fun jsonSagla(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun okHttpSagla(): OkHttpClient {
        val kayitci = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(kayitci)
            .build()
    }

    @Provides
    @Singleton
    fun retrofitSagla(istemci: OkHttpClient, json: Json): Retrofit {
        val icerikTipi = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(TMDB_TEMEL_URL)
            .client(istemci)
            .addConverterFactory(json.asConverterFactory(icerikTipi))
            .build()
    }

    @Provides
    @Singleton
    fun tmdbServisiSagla(retrofit: Retrofit): TmdbServisi =
        retrofit.create(TmdbServisi::class.java)
}
