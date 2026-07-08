package com.sonkarar.data.uzak.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbYanitDto(
    @SerialName("results") val sonuclar: List<TmdbFilmDto> = emptyList()
)

@Serializable
data class TmdbFilmDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val baslik: String = "",
    @SerialName("genre_ids") val turKimlikleri: List<Int> = emptyList(),
    @SerialName("poster_path") val posterYolu: String? = null
)
