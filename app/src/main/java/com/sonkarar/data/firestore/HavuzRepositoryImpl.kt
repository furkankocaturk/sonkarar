package com.sonkarar.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.cekirdek.turkceMesaj
import com.sonkarar.data.esleyici.domaineDonustur
import com.sonkarar.data.esleyici.dtoyaDonustur
import com.sonkarar.data.esleyici.varligaDonustur
import com.sonkarar.data.firestore.dto.HavuzOgesiDto
import com.sonkarar.data.tercih.AppTercihleri
import com.sonkarar.data.yerel.HavuzDao
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.domain.repository.HavuzRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HavuzRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val havuzDao: HavuzDao,
    private val tercihler: AppTercihleri
) : HavuzRepository {

    private fun havuzKoleksiyonu(sinerjiId: String) =
        firestore.collection(Sabitler.KOLEKSIYON_SINERJILER)
            .document(sinerjiId)
            .collection(Sabitler.ALT_KOLEKSIYON_HAVUZ)

    override fun havuzuGozlemle(sinerjiId: String, carkId: String): Flow<List<HavuzOgesi>> =
        havuzDao.ogeleriGozlemle(sinerjiId, carkId)
            .map { liste -> liste.map { it.domaineDonustur() } }

    override fun senkronizasyonuBaslat(
        sinerjiId: String,
        carkId: String
    ): Flow<Sonuc<Unit>> {
        if (tercihler.yerelModAktif) {
            // Yerel modda Room tek kaynaktır; ek senkron gerekmez.
            return flow { emit(Sonuc.Basarili(Unit)) }
        }
        val kategori = Kategori.anahtardan(carkId)
        return callbackFlow {
            val dinleyici = havuzKoleksiyonu(sinerjiId)
                .whereEqualTo("kategori", kategori.name)
                .addSnapshotListener { anlik, hata ->
                    if (hata != null) {
                        trySend(Sonuc.Hata(hata.turkceMesaj(), hata))
                        return@addSnapshotListener
                    }
                    val ogeler = anlik?.documents?.mapNotNull { belge ->
                        belge.toObject(HavuzOgesiDto::class.java)
                            ?.copy(id = belge.id)
                            ?.domaineDonustur()
                    } ?: emptyList()

                    launch {
                        havuzDao.carkKategoriyiTemizle(sinerjiId, carkId)
                        havuzDao.ogeleriYaz(ogeler.map { it.varligaDonustur(sinerjiId) })
                    }
                    trySend(Sonuc.Basarili(Unit))
                }
            awaitClose { dinleyici.remove() }
        }
    }

    override suspend fun ogeEkle(sinerjiId: String, oge: HavuzOgesi): Sonuc<Unit> =
        guvenliCagri {
            if (tercihler.yerelModAktif) {
                val kalici = oge.copy(id = UUID.randomUUID().toString())
                havuzDao.ogeYaz(kalici.varligaDonustur(sinerjiId))
            } else {
                val belge = havuzKoleksiyonu(sinerjiId).document()
                val kalici = oge.copy(id = belge.id)
                belge.set(kalici.dtoyaDonustur()).await()
            }
        }

    override suspend fun ogeGuncelle(sinerjiId: String, oge: HavuzOgesi): Sonuc<Unit> =
        guvenliCagri {
            if (tercihler.yerelModAktif) {
                havuzDao.ogeYaz(oge.varligaDonustur(sinerjiId))
            } else {
                havuzKoleksiyonu(sinerjiId).document(oge.id).set(oge.dtoyaDonustur()).await()
                havuzDao.ogeYaz(oge.varligaDonustur(sinerjiId))
            }
        }

    override suspend fun ogeSil(sinerjiId: String, ogeId: String): Sonuc<Unit> =
        guvenliCagri {
            if (tercihler.yerelModAktif) {
                havuzDao.ogeSil(ogeId)
            } else {
                havuzKoleksiyonu(sinerjiId).document(ogeId).delete().await()
                havuzDao.ogeSil(ogeId)
            }
        }

    override suspend fun ogeleriGetir(
        sinerjiId: String,
        carkId: String
    ): Sonuc<List<HavuzOgesi>> = guvenliCagri {
        if (tercihler.yerelModAktif) {
            havuzDao.ogeleriGetir(sinerjiId, carkId).map { it.domaineDonustur() }
        } else {
            val kategori = Kategori.anahtardan(carkId)
            havuzKoleksiyonu(sinerjiId)
                .whereEqualTo("kategori", kategori.name)
                .get().await().documents.mapNotNull { belge ->
                    belge.toObject(HavuzOgesiDto::class.java)
                        ?.copy(id = belge.id)
                        ?.domaineDonustur()
                }
        }
    }
}
