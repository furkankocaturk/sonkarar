package com.sonkarar.presentation.cark

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Çark dönerken ince, hoş bir dokunsal geri bildirim; sonuçta ise yumuşak bir
 * "kazandı" tınısı üretir. Rahatsız edici sürekli bip sesi KULLANILMAZ.
 * Donanım desteği olmayan cihazlarda sessizce yok sayılır (çökme olmaz).
 */
class HaptikVeSes(private val baglam: Context) {

    private var sonTikZamani = 0L

    private val titresim: Vibrator? by lazy {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val yonetici = baglam.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as? VibratorManager
                yonetici?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                baglam.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }.getOrNull()
    }

    private val tonUretici: ToneGenerator? = runCatching {
        // Düşük ses seviyesi: rahatsız etmesin.
        ToneGenerator(AudioManager.STREAM_MUSIC, 35)
    }.getOrNull()

    /** Çark bir dilimden geçerken: kısa, hafif titreşim (zaman aralığıyla kısılır). */
    fun tik() {
        val simdi = System.currentTimeMillis()
        if (simdi - sonTikZamani < 45L) return
        sonTikZamani = simdi
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                titresim?.vibrate(VibrationEffect.createOneShot(10, 60))
            } else {
                @Suppress("DEPRECATION")
                titresim?.vibrate(10)
            }
        }
    }

    /** Sonuç anında: yumuşak, kısa bir onay tınısı + hafif titreşim. */
    fun kazanildi() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                titresim?.vibrate(VibrationEffect.createOneShot(40, 120))
            }
            tonUretici?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
        }
    }

    fun serbestBirak() {
        runCatching { tonUretici?.release() }
    }
}
