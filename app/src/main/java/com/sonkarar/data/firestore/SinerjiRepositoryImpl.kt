package com.sonkarar.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.cekirdek.turkceMesaj
import com.sonkarar.data.esleyici.domaineDonustur
import com.sonkarar.data.esleyici.dtoyaDonustur
import com.sonkarar.data.firestore.dto.CarkGecmisiKaydiDto
import com.sonkarar.data.firestore.dto.SinerjiDto
import com.sonkarar.domain.model.CarkDurumu
import com.sonkarar.domain.model.Sinerji
import com.sonkarar.domain.repository.SinerjiRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SinerjiRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val kimlik: FirebaseAuth
) : SinerjiRepository {

    override suspend fun odaOlusturVeyaKatil(esEposta: String): Sonuc<String> =
        guvenliCagri {
            val benimUid = kimlik.currentUser?.uid ?: error("Önce giriş yapmalısınız.")
            val benimEposta = (kimlik.currentUser?.email ?: "").lowercase()
            val kullanicilar = firestore.collection(Sabitler.KOLEKSIYON_KULLANICILAR)

            // Karşı taraf beni eşi olarak işaretlediyse onun odasına katıl.
            val karsiTaraf = kullanicilar
                .whereEqualTo("eposta", esEposta.trim().lowercase())
                .whereEqualTo("esEposta", benimEposta)
                .get().await().documents.firstOrNull()

            val sinerjiler = firestore.collection(Sabitler.KOLEKSIYON_SINERJILER)

            val karsiSinerji = karsiTaraf?.getString("sinerjiId")
            val sinerjiId: String = if (karsiTaraf != null && !karsiSinerji.isNullOrBlank()) {
                sinerjiler.document(karsiSinerji)
                    .update("uyeler", FieldValue.arrayUnion(benimUid)).await()
                karsiSinerji
            } else {
                val yeniBelge = sinerjiler.document()
                yeniBelge.set(SinerjiDto(uyeler = listOf(benimUid))).await()
                yeniBelge.id
            }

            kullanicilar.document(benimUid).update(
                mapOf(
                    "esEposta" to esEposta.trim().lowercase(),
                    "sinerjiId" to sinerjiId
                )
            ).await()
            sinerjiId
        }

    override fun sinerjiyiGozlemle(sinerjiId: String): Flow<Sonuc<Sinerji>> = callbackFlow {
        val dinleyici = firestore.collection(Sabitler.KOLEKSIYON_SINERJILER)
            .document(sinerjiId)
            .addSnapshotListener { anlik, hata ->
                if (hata != null) {
                    trySend(Sonuc.Hata(hata.turkceMesaj(), hata))
                    return@addSnapshotListener
                }
                val dto = anlik?.toObject(SinerjiDto::class.java)
                if (dto != null) {
                    trySend(Sonuc.Basarili(dto.domaineDonustur(sinerjiId)))
                }
            }
        awaitClose { dinleyici.remove() }
    }

    override suspend fun carkDurumunuGuncelle(
        sinerjiId: String,
        durum: CarkDurumu
    ): Sonuc<Unit> = guvenliCagri {
        firestore.collection(Sabitler.KOLEKSIYON_SINERJILER).document(sinerjiId)
            .update("carkDurumu", durum.dtoyaDonustur()).await()
    }

    override suspend fun gecmiseKayitEkle(
        sinerjiId: String,
        kategori: Kategori,
        sonuc: String
    ): Sonuc<Unit> = guvenliCagri {
        val kayit = CarkGecmisiKaydiDto(
            zamanDamgasi = System.currentTimeMillis(),
            kategori = kategori.name,
            sonuc = sonuc
        )
        firestore.collection(Sabitler.KOLEKSIYON_SINERJILER).document(sinerjiId)
            .update("carkGecmisi", FieldValue.arrayUnion(kayit)).await()
    }
}
