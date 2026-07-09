package com.sonkarar.cekirdek

enum class Kategori(val etiket: String) {
    YEMEK("Yemek"),
    IZLENECEK("İzlenecek"),
    GENEL("Genel");

    companion object {
        fun anahtardan(anahtar: String?): Kategori =
            entries.firstOrNull { it.name == anahtar } ?: YEMEK
    }
}
