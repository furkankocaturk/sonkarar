package com.sonkarar.domain.model

data class Kullanici(
    val kullaniciId: String,
    val eposta: String,
    val esEposta: String = "",
    val sinerjiId: String = ""
) {
    val eslesmisMi: Boolean get() = sinerjiId.isNotBlank()
}
