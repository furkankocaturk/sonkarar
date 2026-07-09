package com.sonkarar.presentation.cark

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.tema.CarkPaleti
import kotlin.math.floor

const val CARK_ANIMASYON_SURESI_MS = 4200

// Gerçekçi "yavaşlayarak duran" his için özel yumuşatma eğrisi.
private val CarkYavaslamaEasing = CubicBezierEasing(0.15f, 0.85f, 0.2f, 1f)

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

    // Metin boyacısı bir kez oluşturulur (her karede yeniden üretilmez -> takılma olmaz).
    val etiketBoyaci = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(6f, 0f, 2f, android.graphics.Color.argb(150, 0, 0, 0))
        }
    }

    LaunchedEffect(donuyorMu, hedefAci) {
        if (donuyorMu && ogeler.isNotEmpty()) {
            aci.snapTo(aci.value % 360f)
            gecilenCizgiSayaci[0] = 0
            aci.animateTo(
                targetValue = hedefAci,
                animationSpec = tween(
                    durationMillis = CARK_ANIMASYON_SURESI_MS,
                    easing = CarkYavaslamaEasing
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

        // Çizgi geçişinde haptik/ses tetikle (yalnızca sınır geçilince).
        val gecilen = floor(mevcutAci / dilimAcisi).toInt()
        if (donuyorMu && gecilen > gecilenCizgiSayaci[0]) {
            gecilenCizgiSayaci[0] = gecilen
            guncelCizgiGecildi.value.invoke()
        }

        // Dilimler: premium, her dilimde merkeze doğru koyulaşan hafif gradyan.
        ogeler.forEachIndexed { indeks, _ ->
            val temelRenk = CarkPaleti[indeks % CarkPaleti.size]
            val baslangicAci = mevcutAci + indeks * dilimAcisi - 90f
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(temelRenk.copy(alpha = 0.92f), temelRenk),
                    center = merkez,
                    radius = yaricap
                ),
                startAngle = baslangicAci,
                sweepAngle = dilimAcisi,
                useCenter = true,
                topLeft = ustSol,
                size = cizimBoyutu
            )
            // Dilim ayıraç çizgisi (ince, şık).
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = baslangicAci,
                sweepAngle = dilimAcisi,
                useCenter = true,
                topLeft = ustSol,
                size = cizimBoyutu,
                style = Stroke(width = yaricap * 0.006f)
            )
        }

        // Dış çerçeve (premium halka).
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = yaricap,
            center = merkez,
            style = Stroke(width = yaricap * 0.03f)
        )

        // Etiketler: her dilimin ORTASINA hizalı; alt yarıdaki yazılar 180° çevrilerek
        // her zaman DÜZ (baş aşağı olmadan) yazılır. Böylece göstergenin (tepe) altındaki
        // kazanan her zaman yatay ve net okunur.
        etiketBoyaci.textSize = yaricap * 0.125f
        val etiketY = merkez.y - yaricap * 0.60f
        drawIntoCanvas { tuval ->
            val yerelTuval = tuval.nativeCanvas
            ogeler.forEachIndexed { indeks, oge ->
                // Tepeden (saat yönünde) dilim orta açısı.
                val a = ((mevcutAci + indeks * dilimAcisi + dilimAcisi / 2f) % 360f + 360f) % 360f
                yerelTuval.save()
                yerelTuval.rotate(a, merkez.x, merkez.y)
                if (a > 90f && a < 270f) {
                    // Alt yarı: baş aşağı olmasın diye çevir.
                    yerelTuval.rotate(180f, merkez.x, etiketY)
                }
                yerelTuval.drawText(
                    kisalt(oge.isim),
                    merkez.x,
                    etiketY + etiketBoyaci.textSize / 3f,
                    etiketBoyaci
                )
                yerelTuval.restore()
            }
        }

        // Orta göbek.
        drawCircle(
            color = Color.White,
            radius = yaricap * 0.14f,
            center = merkez,
            style = Fill
        )
        drawCircle(
            color = CarkPaleti.first().copy(alpha = 0.9f),
            radius = yaricap * 0.10f,
            center = merkez,
            style = Fill
        )
    }
}

private fun kisalt(isim: String): String =
    if (isim.length > 12) isim.take(11).trimEnd() + "…" else isim
