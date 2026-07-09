package com.sonkarar.cekirdek

enum class TemaModu(val etiket: String) {
    SISTEM("Sistem"),
    ACIK("Açık"),
    KOYU("Koyu");

    companion object {
        fun anahtardan(anahtar: String?): TemaModu =
            entries.firstOrNull { it.name == anahtar } ?: SISTEM
    }
}
