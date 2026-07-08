package com.sonkarar.presentation.ortak

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.sonkarar.presentation.tema.CarkPaleti
import kotlin.random.Random

private data class KonfetiParcacigi(
    val baslangicX: Float,
    val renk: Color,
    val yatayHiz: Float,
    val boyut: Float,
    val gecikme: Float
)

/**
 * Sonuç anında tetiklenen basit konfeti efekti. [tetikleyici] her değiştiğinde
 * (örneğin çark turu değiştiğinde) yeniden oynatılır. Dış kütüphane gerektirmez.
 */
@Composable
fun KonfetiEfekti(tetikleyici: Any?, modifier: Modifier = Modifier) {
    val parcaciklar = remember(tetikleyici) {
        val rastgele = Random(tetikleyici?.hashCode() ?: 0)
        List(80) {
            KonfetiParcacigi(
                baslangicX = rastgele.nextFloat(),
                renk = CarkPaleti[rastgele.nextInt(CarkPaleti.size)],
                yatayHiz = (rastgele.nextFloat() - 0.5f) * 0.3f,
                boyut = 6f + rastgele.nextFloat() * 10f,
                gecikme = rastgele.nextFloat() * 0.3f
            )
        }
    }
    val ilerleme = remember(tetikleyici) { Animatable(0f) }

    LaunchedEffect(tetikleyici) {
        ilerleme.snapTo(0f)
        ilerleme.animateTo(1f, animationSpec = tween(durationMillis = 2200, easing = LinearEasing))
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val p = ilerleme.value
        parcaciklar.forEach { parcacik ->
            val yerelP = ((p - parcacik.gecikme) / (1f - parcacik.gecikme)).coerceIn(0f, 1f)
            if (yerelP <= 0f) return@forEach
            val x = parcacik.baslangicX * size.width + parcacik.yatayHiz * size.width * yerelP
            val y = yerelP * size.height
            drawCircle(
                color = parcacik.renk.copy(alpha = (1f - yerelP)),
                radius = parcacik.boyut,
                center = Offset(x, y)
            )
        }
    }
}
