package com.sonkarar.cekirdek

import java.net.URLEncoder

/**
 * Her zaman çalışan, güvenli arama tabanlı bağlantılar üretir.
 * Böylece sabit (ve bozulabilen) detay URL'lerine bağımlı kalınmaz.
 */
object LinkUretici {

    private fun kodla(metin: String): String =
        URLEncoder.encode(metin.trim(), Charsets.UTF_8.name())

    /** Yemek için tarif araması (Google). Her zaman dolu bir sonuç sayfası açar. */
    fun yemekTarifLinki(isim: String): String =
        "https://www.google.com/search?q=${kodla("$isim tarifi")}"

    /**
     * İzlenecek için JustWatch araması: hangi platformda olduğunu ve nereden
     * izlenebileceğini gösterir (Türkiye).
     */
    fun izlenecekLinki(isim: String): String =
        "https://www.justwatch.com/tr/arama?q=${kodla(isim)}"

    fun kategoriyeGoreLink(isim: String, yemekMi: Boolean): String =
        if (yemekMi) yemekTarifLinki(isim) else izlenecekLinki(isim)
}
