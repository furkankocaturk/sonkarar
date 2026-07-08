package com.sonkarar.presentation.cark

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Çark çizgiden geçerken kısa titreşim ve "tık" sesi üretir.
 * Donanım desteği olmayan cihazlarda sessizce yok sayılır (çökme olmaz).
 */
class HaptikVeSes(private val baglam: Context) {

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
        ToneGenerator(AudioManager.STREAM_MUSIC, 60)
    }.getOrNull()

    fun tik() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                titresim?.vibrate(VibrationEffect.createOneShot(18, 90))
            } else {
                @Suppress("DEPRECATION")
                titresim?.vibrate(18)
            }
            tonUretici?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        }
    }

    fun serbestBirak() {
        runCatching { tonUretici?.release() }
    }
}
