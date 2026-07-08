package com.sonkarar.presentation.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KoyuNeonSema = darkColorScheme(
    primary = NeonMor,
    secondary = NeonTurkuaz,
    tertiary = NeonPembe,
    background = ZeminSiyah,
    surface = YuzeyKoyu,
    surfaceVariant = YuzeyKoyu2,
    onPrimary = ZeminSiyah,
    onSecondary = ZeminSiyah,
    onBackground = MetinBeyaz,
    onSurface = MetinBeyaz,
    error = HataKirmizi
)

@Composable
fun SonKararTemasi(
    // Uygulama kimliği gereği her zaman koyu tema kullanılır.
    koyuTema: Boolean = true,
    icerik: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KoyuNeonSema,
        typography = SonKararTipografisi,
        content = icerik
    )
}
