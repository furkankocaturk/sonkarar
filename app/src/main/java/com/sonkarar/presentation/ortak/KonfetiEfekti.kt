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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.sonkarar.presentation.tema.CarkPaleti
import kotlin.math.sin
import kotlin.random.Random

private data class Parcacik(
    val baslangicX: Float,
    val baslangicY: Float,
    val renk: Color,
    val yatayHiz: Float,
    val boyut: Float,
    val donusHizi: Float,
    val salinim: Float,
    val gecikme: Float
)

/**
 * Yerçekimi + hafif salınım + dönme ile daha doğal konfeti.
 * [tetikleyici] değiştikçe yeniden oynatılır.
 */
@Composable
fun KonfetiEfekti(tetikleyici: Any?, modifier: Modifier = Modifier) {
    val parcaciklar = remember(tetikleyici) {
        val r = Random(tetikleyici?.hashCode() ?: 0)
        List(90) {
            Parcacik(
                baslangicX = r.nextFloat(),
                baslangicY = -0.1f - r.nextFloat() * 0.2f,
                renk = CarkPaleti[r.nextInt(CarkPaleti.size)],
                yatayHiz = (r.nextFloat() - 0.5f) * 0.25f,
                boyut = 8f + r.nextFloat() * 10f,
                donusHizi = (r.nextFloat() - 0.5f) * 720f,
                salinim = 0.02f + r.nextFloat() * 0.05f,
                gecikme = r.nextFloat() * 0.25f
            )
        }
    }
    val ilerleme = remember(tetikleyici) { Animatable(0f) }

    LaunchedEffect(tetikleyici) {
        ilerleme.snapTo(0f)
        ilerleme.animateTo(1f, animationSpec = tween(2600, easing = LinearEasing))
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = ilerleme.value
        parcaciklar.forEach { p ->
            val yerelT = ((t - p.gecikme) / (1f - p.gecikme)).coerceIn(0f, 1f)
            if (yerelT <= 0f) return@forEach
            // Yerçekimi: düşüş hızlanır (t^2), yatay sürüklenme + salınım.
            val y = (p.baslangicY + yerelT * yerelT * 1.25f) * size.height
            val x = (p.baslangicX + p.yatayHiz * yerelT) * size.width +
                sin(yerelT * 12f) * p.salinim * size.width
            val alfa = (1f - yerelT).coerceIn(0f, 1f)
            rotate(degrees = p.donusHizi * yerelT, pivot = Offset(x, y)) {
                drawRect(
                    color = p.renk.copy(alpha = alfa),
                    topLeft = Offset(x - p.boyut / 2f, y - p.boyut / 2f),
                    size = Size(p.boyut, p.boyut * 0.6f)
                )
            }
        }
    }
}
