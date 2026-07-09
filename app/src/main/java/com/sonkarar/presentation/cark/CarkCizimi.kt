package com.sonkarar.presentation.cark

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import com.sonkarar.domain.kullanim.CarkGeometri
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.tema.CarkPaleti
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.abs

const val CARK_ANIMASYON_SURESI_MS = 4200

@Composable
fun CarkCizimi(
    ogeler: List<HavuzOgesi>,
    disHedefAci: Float?,
    disTur: Long,
    otomatikTetik: Long,
    yerelCevirmeEtkin: Boolean,
    tik: () -> Unit,
    yerelCevirmeBasladi: () -> Unit,
    yerelCevrildi: (kazananIndeks: Int, finalAci: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val aci = remember { Animatable(0f) }
    val kapsam = rememberCoroutineScope()
    val guncelTik = rememberUpdatedState(tik)
    val guncelBasladi = rememberUpdatedState(yerelCevirmeBasladi)
    val guncelCevrildi = rememberUpdatedState(yerelCevrildi)
    val guncelOgeler = rememberUpdatedState(ogeler)
    val guncelEtkin = rememberUpdatedState(yerelCevirmeEtkin)
    val sonDilim = remember { intArrayOf(-1) }

    val etiketBoyaci = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(6f, 0f, 2f, android.graphics.Color.argb(160, 0, 0, 0))
        }
    }

    fun agirliklar() = guncelOgeler.value.map { it.agirlik }

    suspend fun flingBaslat(hiz: Float) {
        if (aci.isRunning) return
        sonDilim[0] = -1
        guncelBasladi.value.invoke()
        aci.animateDecay(
            initialVelocity = hiz,
            animationSpec = exponentialDecay(frictionMultiplier = 0.5f)
        ) {
            val indeks = CarkGeometri.kazananIndeks(agirliklar(), value)
            if (indeks != sonDilim[0]) {
                sonDilim[0] = indeks
                guncelTik.value.invoke()
            }
        }
        val kazanan = CarkGeometri.kazananIndeks(agirliklar(), aci.value)
        if (kazanan >= 0) guncelCevrildi.value.invoke(kazanan, aci.value)
    }

    // Uzaktan (eş) çevirme: hedef açıya doğru yavaşlayarak dön.
    LaunchedEffect(disTur) {
        val hedef = disHedefAci
        if (disTur > 0 && hedef != null) {
            sonDilim[0] = -1
            aci.animateTo(
                targetValue = hedef,
                animationSpec = tween(CARK_ANIMASYON_SURESI_MS, easing = FastOutSlowInEasing)
            )
        }
    }

    // Otomatik çevir (buton): rastgele güçlü fırlatma.
    LaunchedEffect(otomatikTetik) {
        if (otomatikTetik > 0 && guncelOgeler.value.size >= 2) {
            val hiz = (900..2600).random().toFloat()
            flingBaslat(hiz)
        }
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            var sonAci = 0f
            var sonZaman = 0L
            var hiz = 0f
            detectDragGestures(
                onDragStart = { konum ->
                    sonAci = noktaAcisi(konum, size.width.toFloat(), size.height.toFloat())
                    sonZaman = System.currentTimeMillis()
                    hiz = 0f
                },
                onDrag = { change, _ ->
                    if (guncelEtkin.value) {
                        val a = noktaAcisi(change.position, size.width.toFloat(), size.height.toFloat())
                        var d = a - sonAci
                        if (d > 180f) d -= 360f
                        if (d < -180f) d += 360f
                        val simdi = System.currentTimeMillis()
                        val dt = (simdi - sonZaman).coerceAtLeast(1L)
                        hiz = 0.7f * (d / dt * 1000f) + 0.3f * hiz
                        sonAci = a
                        sonZaman = simdi
                        kapsam.launch { aci.snapTo(aci.value + d) }
                        change.consume()
                    }
                },
                onDragEnd = {
                    if (guncelEtkin.value && guncelOgeler.value.size >= 2) {
                        val yon = if (hiz >= 0f) 1f else -1f
                        val buyukluk = abs(hiz).coerceIn(700f, 3400f)
                        kapsam.launch { flingBaslat(yon * buyukluk) }
                    }
                }
            )
        }
    ) {
        val oge = guncelOgeler.value
        if (oge.isEmpty()) return@Canvas

        val agirlik = oge.map { it.agirlik }
        val sweepler = CarkGeometri.sweepler(agirlik)
        val baslangiclar = CarkGeometri.baslangiclar(sweepler)
        val R = aci.value

        val yaricap = size.minDimension / 2f * 0.94f
        val merkez = Offset(size.width / 2f, size.height / 2f)
        val ustSol = Offset(merkez.x - yaricap, merkez.y - yaricap)
        val boyut = Size(yaricap * 2f, yaricap * 2f)

        // 3B his için alt gölge.
        drawCircle(
            color = Color.Black.copy(alpha = 0.28f),
            radius = yaricap * 1.03f,
            center = Offset(merkez.x, merkez.y + yaricap * 0.04f)
        )

        // Dilimler (merkeze doğru açılan gradyanla dome hissi).
        oge.forEachIndexed { i, _ ->
            val taban = CarkPaleti[i % CarkPaleti.size]
            val acik = lerp(taban, Color.White, 0.22f)
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(acik, taban),
                    center = merkez,
                    radius = yaricap
                ),
                startAngle = R + baslangiclar[i] - 90f,
                sweepAngle = sweepler[i],
                useCenter = true,
                topLeft = ustSol,
                size = boyut
            )
            drawArc(
                color = Color.White.copy(alpha = 0.10f),
                startAngle = R + baslangiclar[i] - 90f,
                sweepAngle = sweepler[i],
                useCenter = true,
                topLeft = ustSol,
                size = boyut,
                style = Stroke(width = yaricap * 0.006f)
            )
        }

        // Kenar karartma (vignette) -> derinlik.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.28f)),
                center = merkez,
                radius = yaricap
            ),
            radius = yaricap,
            center = merkez
        )

        // Üstten gelen ışık yansıması.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(merkez.x, merkez.y - yaricap * 0.45f),
                radius = yaricap * 0.9f
            ),
            radius = yaricap,
            center = merkez
        )

        // Metalik dış jant (üst açık, alt koyu).
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.9f), Color.Black.copy(alpha = 0.5f)),
                startY = merkez.y - yaricap,
                endY = merkez.y + yaricap
            ),
            radius = yaricap,
            center = merkez,
            style = Stroke(width = yaricap * 0.045f)
        )

        // Etiketler: radyal (mile boyunca), sol yarıda 180° çevrik.
        etiketBoyaci.textSize = yaricap * 0.11f
        drawIntoCanvas { tuval ->
            val yerelTuval = tuval.nativeCanvas
            val etiketR = yaricap * 0.56f
            oge.forEachIndexed { i, oge ->
                val mid = baslangiclar[i] + sweepler[i] / 2f
                val A = ((R + mid) % 360f + 360f) % 360f
                yerelTuval.save()
                yerelTuval.rotate(A - 90f, merkez.x, merkez.y)
                val tx = merkez.x + etiketR
                val ty = merkez.y
                if (A in 90f..270f) {
                    yerelTuval.rotate(180f, tx, ty)
                }
                yerelTuval.drawText(
                    kisalt(oge.isim),
                    tx,
                    ty + etiketBoyaci.textSize / 3f,
                    etiketBoyaci
                )
                yerelTuval.restore()
            }
        }

        // 3B göbek.
        drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = yaricap * 0.16f, center = Offset(merkez.x, merkez.y + yaricap * 0.01f))
        drawCircle(color = Color.White, radius = yaricap * 0.15f, center = merkez, style = Fill)
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(lerp(CarkPaleti.first(), Color.White, 0.3f), CarkPaleti.first()),
                startY = merkez.y - yaricap * 0.12f,
                endY = merkez.y + yaricap * 0.12f
            ),
            radius = yaricap * 0.115f,
            center = merkez,
            style = Fill
        )
    }
}

private fun noktaAcisi(konum: Offset, genislik: Float, yukseklik: Float): Float {
    val dx = konum.x - genislik / 2f
    val dy = konum.y - yukseklik / 2f
    val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    return (deg + 360f) % 360f
}

private fun kisalt(isim: String): String =
    if (isim.length > 12) isim.take(11).trimEnd() + "…" else isim
