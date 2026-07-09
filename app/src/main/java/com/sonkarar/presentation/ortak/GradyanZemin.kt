package com.sonkarar.presentation.ortak

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.sonkarar.presentation.tema.YerelZeminDegrade

/** Tüm ekranlarda ortak kullanılan, tema-duyarlı dikey gradyan arka plan. */
@Composable
fun GradyanZemin(
    modifier: Modifier = Modifier,
    icerik: @Composable () -> Unit
) {
    val renkler = YerelZeminDegrade.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(renkler))
    ) {
        icerik()
    }
}
