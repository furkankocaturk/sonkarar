package com.sonkarar.presentation.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val YerelZeminDegrade = staticCompositionLocalOf { KoyuZeminDegrade }

private val KoyuSema = darkColorScheme(
    primary = Mercan,
    onPrimary = Color.White,
    secondary = Teal,
    onSecondary = Color.White,
    tertiary = Amber,
    background = ZeminKoyu,
    onBackground = MetinAcik,
    surface = YuzeyKoyu,
    onSurface = MetinAcik,
    surfaceVariant = YuzeyKoyu2,
    onSurfaceVariant = MetinSolgunKoyu,
    error = HataKirmizi
)

private val AcikSema = lightColorScheme(
    primary = MercanKoyu,
    onPrimary = Color.White,
    secondary = Color(0xFF1E9F90),
    onSecondary = Color.White,
    tertiary = Color(0xFFDE8F3A),
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
    val sema = if (koyuTema) KoyuSema else AcikSema
    val zemin = if (koyuTema) KoyuZeminDegrade else AcikZeminDegrade
    CompositionLocalProvider(YerelZeminDegrade provides zemin) {
        MaterialTheme(
            colorScheme = sema,
            typography = SonKararTipografisi,
            content = icerik
        )
    }
}
