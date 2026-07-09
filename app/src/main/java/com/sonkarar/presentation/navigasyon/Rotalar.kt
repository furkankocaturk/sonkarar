package com.sonkarar.presentation.navigasyon

object Rotalar {
    const val ACILIS = "acilis"
    const val GIRIS = "giris"
    const val ESLESME = "eslesme"
    const val CARKLAR = "carklar"
    const val GECMIS = "gecmis"

    const val CARK_ROTA = "cark/{carkId}"
    const val HAVUZ_ROTA = "havuz/{carkId}"
    const val ARG_CARK_ID = "carkId"

    fun cark(carkId: String) = "cark/$carkId"
    fun havuz(carkId: String) = "havuz/$carkId"
}
