package com.sonkarar.data.kimlik

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.data.firestore.dto.KullaniciDto
import com.sonkarar.data.tercih.AppTercihleri
import com.sonkarar.domain.model.Kullanici
import com.sonkarar.domain.repository.KimlikRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KimlikRepositoryImpl @Inject constructor(
    private val kimlik: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val tercihler: AppTercihleri
) : KimlikRepository {

    private val yerelKullanici = Kullanici(
        kullaniciId = Sabitler.YEREL_KULLANICI_ID,
        eposta = "cevrimdisi@sonkarar",
        esEposta = "",
        sinerjiId = Sabitler.YEREL_SINERJI_ID
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun aktifKullaniciyiGozlemle(): Flow<Kullanici?> =
        tercihler.yerelModAkisi().flatMapLatest { yerelMod ->
            if (yerelMod) flowOf(yerelKullanici) else firebaseKullaniciAkisi()
        }

    private fun firebaseKullaniciAkisi(): Flow<Kullanici?> = callbackFlow {
        var belgeDinleyici: ListenerRegistration? = null

        fun belgeyiDinle(uid: String) {
            belgeDinleyici?.remove()
            belgeDinleyici = firestore.collection(Sabitler.KOLEKSIYON_KULLANICILAR)
                .document(uid)
                .addSnapshotListener { anlik, _ ->
                    val dto = anlik?.toObject(KullaniciDto::class.java)
                    trySend(
                        Kullanici(
                            kullaniciId = uid,
                            eposta = dto?.eposta?.ifBlank { kimlik.currentUser?.email ?: "" }
                                ?: (kimlik.currentUser?.email ?: ""),
                            esEposta = dto?.esEposta ?: "",
                            sinerjiId = dto?.sinerjiId ?: ""
                        )
                    )
                }
        }

        kimlik.currentUser?.uid?.let { belgeyiDinle(it) } ?: trySend(null)

        val oturumDinleyici = FirebaseAuth.AuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                belgeDinleyici?.remove()
                belgeDinleyici = null
                trySend(null)
            } else {
                belgeyiDinle(uid)
            }
        }
        kimlik.addAuthStateListener(oturumDinleyici)

        awaitClose {
            belgeDinleyici?.remove()
            kimlik.removeAuthStateListener(oturumDinleyici)
        }
    }

    override suspend fun googleIleGirisYap(kimlikJetonu: String): Sonuc<Kullanici> =
        guvenliCagri {
            val kimlikBilgisi = GoogleAuthProvider.getCredential(kimlikJetonu, null)
            val sonuc = kimlik.signInWithCredential(kimlikBilgisi).await()
            val firebaseKullanici = sonuc.user ?: error("Kullanıcı bilgisi alınamadı.")
            val belge = firestore.collection(Sabitler.KOLEKSIYON_KULLANICILAR)
                .document(firebaseKullanici.uid)
            val mevcut = belge.get().await()
            if (!mevcut.exists()) {
                belge.set(KullaniciDto(eposta = firebaseKullanici.email?.lowercase() ?: "")).await()
            }
            tercihler.yerelModAktif = false
            Kullanici(
                kullaniciId = firebaseKullanici.uid,
                eposta = firebaseKullanici.email ?: "",
                esEposta = mevcut.getString("esEposta") ?: "",
                sinerjiId = mevcut.getString("sinerjiId") ?: ""
            )
        }

    override suspend fun oturumuKapat(): Sonuc<Unit> = guvenliCagri {
        tercihler.yerelModAktif = false
        kimlik.signOut()
    }

    override suspend fun yerelModaGec(): Sonuc<Unit> = guvenliCagri {
        tercihler.yerelModAktif = true
    }

    override fun yerelModAktif(): Boolean = tercihler.yerelModAktif
}
