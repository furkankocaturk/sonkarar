package com.sonkarar.data.uzak

import com.sonkarar.data.uzak.dto.TmdbYanitDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbServisi {
    @GET("discover/movie")
    suspend fun filmKesfet(
        @Query("api_key") apiAnahtari: String,
        @Query("with_genres") turKimlikleri: String,
        @Query("language") dil: String = "tr-TR",
        @Query("sort_by") siralama: String = "popularity.desc",
        @Query("page") sayfa: Int = 1
    ): TmdbYanitDto
}
