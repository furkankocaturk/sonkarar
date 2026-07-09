package com.sonkarar.cekirdek

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Bir istisnayı kullanıcıya gösterilecek Türkçe mesaja çevirir. */
fun Throwable.turkceMesaj(): String = when (this) {
    is FirebaseNetworkException ->
        "İnternet bağlantısı kurulamadı. Bağlantınızı kontrol edip tekrar deneyin."
    is IOException ->
        "Ağ hatası oluştu. Lütfen internet bağlantınızı kontrol edin."
    is FirebaseAuthException ->
        "Giriş yapılırken bir sorun oluştu. Lütfen tekrar deneyin."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Bu işlem için yetkiniz yok."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Sunucuya şu an ulaşılamıyor. Çevrimdışı verilerle devam ediliyor."
        else -> "Veri işlenirken bir hata oluştu. Lütfen tekrar deneyin."
    }
    else -> "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."
}

/**
 * suspend blokları güvenle çalıştırır; [CancellationException] yeniden fırlatılır
 * ki coroutine iptali doğru çalışsın.
 */
suspend fun <T> guvenliCagri(blok: suspend () -> T): Sonuc<T> = try {
    Sonuc.Basarili(blok())
} catch (iptal: CancellationException) {
    throw iptal
} catch (hata: Throwable) {
    Sonuc.Hata(hata.turkceMesaj(), hata)
}
