package com.sonkarar.presentation.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Ekranların gradyan arka planı için tema-duyarlı renk listesi. */
val YerelZeminDegrade = staticCompositionLocalOf { KoyuZeminDegrade }

private val KoyuNeonSema = darkColorScheme(
    primary = MarkaMor,
    onPrimary = Color.White,
    secondary = MarkaTurkuaz,
    onSecondary = ZeminSiyah,
    tertiary = MarkaPembe,
    background = ZeminSiyah,
    onBackground = MetinBeyaz,
    surface = YuzeyKoyu,
    onSurface = MetinBeyaz,
    surfaceVariant = YuzeyKoyu2,
    onSurfaceVariant = MetinSolgun,
    error = HataKirmizi
)

private val AcikSema = lightColorScheme(
    primary = MarkaMor,
    onPrimary = Color.White,
    secondary = Color(0xFF12B3A6),
    onSecondary = Color.White,
    tertiary = Color(0xFFE85D93),
    background = ZeminAcik,
    onBackground = MetinKoyu,
    surface = YuzeyAcik,
    onSurface = MetinKoyu,
    surfaceVariant = YuzeyAcik2,
    onSurfaceVariant = MetinSolgunAcik,
    error = HataKirmizi
)

@Composable
fun SonKararTemasi(
    koyuTema: Boolean = isSystemInDarkTheme(),
    icerik: @Composable () -> Unit
) {
    val sema = if (koyuTema) KoyuNeonSema else AcikSema
    val zeminDegrade = if (koyuTema) KoyuZeminDegrade else AcikZeminDegrade
    CompositionLocalProvider(YerelZeminDegrade provides zeminDegrade) {
        MaterialTheme(
            colorScheme = sema,
            typography = SonKararTipografisi,
            content = icerik
        )
    }
}
