package com.sonkarar.cekirdek

/**
 * Tüm katmanlarda kullanılan hata güvenli sonuç sarmalayıcısı.
 */
sealed interface Sonuc<out T> {
    data class Basarili<out T>(val veri: T) : Sonuc<T>
    data class Hata(val mesaj: String, val istisna: Throwable? = null) : Sonuc<Nothing>
    data object Yukleniyor : Sonuc<Nothing>
}

inline fun <T, R> Sonuc<T>.donustur(donusum: (T) -> R): Sonuc<R> = when (this) {
    is Sonuc.Basarili -> Sonuc.Basarili(donusum(veri))
    is Sonuc.Hata -> this
    Sonuc.Yukleniyor -> Sonuc.Yukleniyor
}

inline fun <T> Sonuc<T>.basariliysa(islem: (T) -> Unit): Sonuc<T> {
    if (this is Sonuc.Basarili) islem(veri)
    return this
}
