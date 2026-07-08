package com.sonkarar.cekirdek

enum class Kategori(val etiket: String) {
    YEMEK("Yemekler"),
    IZLENECEK("İzlenecekler");

    companion object {
        fun anahtardan(anahtar: String?): Kategori =
            entries.firstOrNull { it.name == anahtar } ?: YEMEK
    }
}
