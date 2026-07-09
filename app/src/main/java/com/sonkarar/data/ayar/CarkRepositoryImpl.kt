package com.sonkarar.data.ayar

import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.Sabitler
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.cekirdek.guvenliCagri
import com.sonkarar.data.esleyici.domaineDonustur
import com.sonkarar.data.esleyici.varligaDonustur
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.data.tercih.AppTercihleri
import com.sonkarar.data.varsayilan.OntanimliHavuz
import com.sonkarar.data.yerel.CarkDao
import com.sonkarar.data.yerel.HavuzDao
import com.sonkarar.data.yerel.varlik.CarkVarligi
import com.sonkarar.domain.model.Cark
import com.sonkarar.domain.repository.CarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarkRepositoryImpl @Inject constructor(
    private val carkDao: CarkDao,
    private val havuzDao: HavuzDao,
    private val tercihler: AppTercihleri
) : CarkRepository {

    private val sinerjiId = Sabitler.YEREL_SINERJI_ID

    override fun carklariGozlemle(): Flow<List<Cark>> {
        if (!tercihler.yerelModAktif) {
            return flowOf(OntanimliHavuz.varsayilanCarklar().filter { it.sistemMi })
        }
        return flow {
            varsayilanlariTohumla()
            emitAll(
                carkDao.gozlemle(sinerjiId).map { liste -> liste.map { it.domaineDonustur() } }
            )
        }
    }

    override suspend fun carkGetir(carkId: String): Cark? {
        if (!tercihler.yerelModAktif) {
            return OntanimliHavuz.varsayilanCarklar().firstOrNull { it.carkId == carkId }
        }
        return carkDao.getir(carkId)?.domaineDonustur()
    }

    override suspend fun carkEkle(ad: String): Sonuc<String> = guvenliCagri {
        val temiz = ad.trim()
        if (temiz.isBlank()) error("Lütfen çark için bir isim girin.")
        val carkId = "cark_${UUID.randomUUID()}"
        val sira = carkDao.enBuyukSira(sinerjiId) + 1
        carkDao.ekle(
            CarkVarligi(
                carkId = carkId,
                sinerjiId = sinerjiId,
                ad = temiz,
                kategoriTipi = Kategori.GENEL.name,
                sistemMi = false,
                siraNo = sira
            )
        )
        // Varsayılan iki seçenek: Evet / Hayır.
        val varsayilan = listOf("Evet", "Hayır").map { isim ->
            HavuzOgesi(
                id = UUID.randomUUID().toString(),
                carkId = carkId,
                kategori = Kategori.GENEL,
                isim = isim,
                ekleyenKullanici = Sabitler.YEREL_KULLANICI_ID,
                agirlik = Sabitler.VARSAYILAN_AGIRLIK
            ).varligaDonustur(sinerjiId)
        }
        havuzDao.ogeleriYaz(varsayilan)
        carkId
    }

    override suspend fun carkSil(carkId: String): Sonuc<Unit> = guvenliCagri {
        val cark = carkDao.getir(carkId)
        if (cark?.sistemMi == true) error("Hazır çark silinemez.")
        carkDao.carkOgeleriniSil(carkId)
        carkDao.sil(carkId)
    }

    private suspend fun varsayilanlariTohumla() {
        if (carkDao.sayi(sinerjiId) > 0) {
            tercihler.varsayilanHavuzYazildiMi = true
            return
        }
        val carklar = OntanimliHavuz.varsayilanCarklar()
        carkDao.topluEkle(
            carklar.mapIndexed { indeks, cark ->
                CarkVarligi(
                    carkId = cark.carkId,
                    sinerjiId = sinerjiId,
                    ad = cark.ad,
                    kategoriTipi = cark.kategori.name,
                    sistemMi = cark.sistemMi,
                    siraNo = indeks
                )
            }
        )
        val ogeler = OntanimliHavuz.tumOgeler(Sabitler.YEREL_KULLANICI_ID)
            .map { it.copy(id = UUID.randomUUID().toString()).varligaDonustur(sinerjiId) }
        havuzDao.ogeleriYaz(ogeler)
        tercihler.varsayilanHavuzYazildiMi = true
    }
}
