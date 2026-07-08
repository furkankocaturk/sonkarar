package com.sonkarar.presentation.cark

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.tema.CarkPaleti
import com.sonkarar.presentation.tema.MetinBeyaz
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

const val CARK_ANIMASYON_SURESI_MS = 4000

@Composable
fun CarkCizimi(
    ogeler: List<HavuzOgesi>,
    hedefAci: Float,
    donuyorMu: Boolean,
    cizgiGecildi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aci = remember { Animatable(0f) }
    val guncelCizgiGecildi = rememberUpdatedState(cizgiGecildi)
    val gecilenCizgiSayaci = remember { intArrayOf(0) }

    LaunchedEffect(donuyorMu, hedefAci) {
        if (donuyorMu && ogeler.isNotEmpty()) {
            aci.snapTo(aci.value % 360f)
            gecilenCizgiSayaci[0] = 0
            aci.animateTo(
                targetValue = hedefAci,
                animationSpec = tween(
                    durationMillis = CARK_ANIMASYON_SURESI_MS,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Canvas(modifier = modifier) {
        val dilimSayisi = ogeler.size
        if (dilimSayisi == 0) return@Canvas

        val dilimAcisi = 360f / dilimSayisi
        val yaricap = size.minDimension / 2f
        val merkez = Offset(size.width / 2f, size.height / 2f)
        val ustSol = Offset(merkez.x - yaricap, merkez.y - yaricap)
        val cizimBoyutu = Size(yaricap * 2f, yaricap * 2f)
        val mevcutAci = aci.value

        // Haptik: kaç çizgi sınırından geçildiğini takip et.
        val gecilen = floor(mevcutAci / dilimAcisi).toInt()
        if (donuyorMu && gecilen > gecilenCizgiSayaci[0]) {
            gecilenCizgiSayaci[0] = gecilen
            guncelCizgiGecildi.value.invoke()
        }

        ogeler.forEachIndexed { indeks, oge ->
            val baslangicAci = mevcutAci + indeks * dilimAcisi - 90f
            drawArc(
                color = CarkPaleti[indeks % CarkPaleti.size],
                startAngle = baslangicAci,
                sweepAngle = dilimAcisi,
                useCenter = true,
                topLeft = ustSol,
                size = cizimBoyutu
            )
        }

        // Dilim isimlerini native canvas ile çiz.
        drawIntoCanvas { tuval ->
            val boyaci = android.graphics.Paint().apply {
                color = MetinBeyaz.toArgb()
                textSize = yaricap * 0.09f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            ogeler.forEachIndexed { indeks, oge ->
                val ortaAci = mevcutAci + indeks * dilimAcisi + dilimAcisi / 2f - 90f
                val radyan = Math.toRadians(ortaAci.toDouble())
                val x = merkez.x + (yaricap * 0.6f) * cos(radyan).toFloat()
                val y = merkez.y + (yaricap * 0.6f) * sin(radyan).toFloat()
                val yerelTuval = tuval.nativeCanvas
                yerelTuval.save()
                yerelTuval.rotate(ortaAci + 90f, x, y)
                val etiket = if (oge.isim.length > 12) oge.isim.take(11) + "…" else oge.isim
                yerelTuval.drawText(etiket, x, y + boyaci.textSize / 3f, boyaci)
                yerelTuval.restore()
            }
        }
    }
}
