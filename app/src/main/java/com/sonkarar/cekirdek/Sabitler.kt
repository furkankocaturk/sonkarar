package com.sonkarar.cekirdek

object Sabitler {
    const val KOLEKSIYON_KULLANICILAR = "kullanicilar"
    const val KOLEKSIYON_SINERJILER = "sinerjiler"
    const val ALT_KOLEKSIYON_HAVUZ = "havuz"

    const val VARSAYILAN_AGIRLIK = 10
    const val ZAMAN_CEZASI_TUR_SAYISI = 3          // Son 3 tur kontrol edilir
    const val ZAMAN_CEZASI_ORANI = 0.20            // %80 düşür => %20'si kalır
    const val ENJEKTE_EDILECEK_ONERI_SAYISI = 3
    const val CARK_GECMISI_LIMITI = 100            // Firestore'da tutulan azami kayıt

    // Çevrimdışı (anahtarsız) tek kişi modu için sabit kimlikler.
    const val YEREL_KULLANICI_ID = "yerel_kullanici"
    const val YEREL_SINERJI_ID = "yerel_oda"
}
