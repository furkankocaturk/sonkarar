package com.sonkarar.data.tercih

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Basit kullanıcı tercihleri deposu (SharedPreferences).
 * Çevrimdışı (anahtarsız) mod bayrağını ve varsayılan havuz kurulum durumunu tutar.
 */
@Singleton
class AppTercihleri @Inject constructor(
    @ApplicationContext baglam: Context
) {
    private val tercihler: SharedPreferences =
        baglam.getSharedPreferences("sonkarar_tercihleri", Context.MODE_PRIVATE)

    var yerelModAktif: Boolean
        get() = tercihler.getBoolean(ANAHTAR_YEREL_MOD, false)
        set(deger) = tercihler.edit().putBoolean(ANAHTAR_YEREL_MOD, deger).apply()

    var varsayilanHavuzYazildiMi: Boolean
        get() = tercihler.getBoolean(ANAHTAR_VARSAYILAN_HAVUZ, false)
        set(deger) = tercihler.edit().putBoolean(ANAHTAR_VARSAYILAN_HAVUZ, deger).apply()

    /** Çevrimdışı mod bayrağı değiştikçe güncel değeri yayan akış. */
    fun yerelModAkisi(): Flow<Boolean> = callbackFlow {
        trySend(yerelModAktif)
        val dinleyici = SharedPreferences.OnSharedPreferenceChangeListener { _, anahtar ->
            if (anahtar == ANAHTAR_YEREL_MOD) trySend(yerelModAktif)
        }
        tercihler.registerOnSharedPreferenceChangeListener(dinleyici)
        awaitClose { tercihler.unregisterOnSharedPreferenceChangeListener(dinleyici) }
    }

    private companion object {
        const val ANAHTAR_YEREL_MOD = "yerel_mod_aktif"
        const val ANAHTAR_VARSAYILAN_HAVUZ = "varsayilan_havuz_yazildi"
    }
}
