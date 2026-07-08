package com.sonkarar.presentation.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KoyuNeonSema = darkColorScheme(
    primary = MarkaMor,
    secondary = MarkaTurkuaz,
    tertiary = MarkaPembe,
    background = ZeminSiyah,
    surface = YuzeyKoyu,
    surfaceVariant = YuzeyKoyu2,
    onPrimary = ZeminSiyah,
    onSecondary = ZeminSiyah,
    onBackground = MetinBeyaz,
    onSurface = MetinBeyaz,
    error = HataKirmizi
)

private val AcikSema = lightColorScheme(
    primary = MarkaMor,
    secondary = MarkaTurkuaz,
    tertiary = MarkaPembe,
    background = ZeminAcik,
    surface = YuzeyAcik,
    surfaceVariant = YuzeyAcik2,
    onPrimary = ZeminAcik,
    onSecondary = ZeminAcik,
    onBackground = MetinKoyu,
    onSurface = MetinKoyu,
    error = HataKirmizi
)

@Composable
fun SonKararTemasi(
    koyuTema: Boolean = isSystemInDarkTheme(),
    icerik: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (koyuTema) KoyuNeonSema else AcikSema,
        typography = SonKararTipografisi,
        content = icerik
    )
}
