package com.sonkarar.presentation.tema

import androidx.compose.ui.graphics.Color

// Yumuşak, sıcak marka renkleri (mor yok)
val Mercan = Color(0xFFF4715E)
val MercanKoyu = Color(0xFFE85C48)
val Teal = Color(0xFF2BB3A3)
val Amber = Color(0xFFF3A64B)

// Koyu tema — sıcak, yumuşak antrasit (mavi/mor değil)
val ZeminKoyu = Color(0xFF14110F)
val ZeminKoyu2 = Color(0xFF1C1815)
val YuzeyKoyu = Color(0xFF211C19)
val YuzeyKoyu2 = Color(0xFF2B2521)
val MetinAcik = Color(0xFFF3EEE9)
val MetinSolgunKoyu = Color(0xFFAEA49B)

// Açık tema — krem, sakin
val ZeminAcik = Color(0xFFFBF7F2)
val ZeminAcik2 = Color(0xFFF3ECE3)
val YuzeyAcik = Color(0xFFFFFFFF)
val YuzeyAcik2 = Color(0xFFF2EBE2)
val MetinKoyu = Color(0xFF2A2521)
val MetinSolgunAcik = Color(0xFF7C7269)

val HataKirmizi = Color(0xFFE5484D)

// Çark dilim paleti — sıcaktan soğuğa yumuşak geçen uyumlu 8 ton
val CarkPaleti = listOf(
    Color(0xFFF4715E), // mercan
    Color(0xFFFF9F5A), // turuncu
    Color(0xFFFFC65A), // amber
    Color(0xFF9AD16E), // yeşil
    Color(0xFF33B7A6), // teal
    Color(0xFF4FA3E3), // gök mavisi
    Color(0xFF6D8BE0), // yumuşak mavi
    Color(0xFFF06E9E)  // pembe
)

// Arka plan degradeleri (yumuşak)
val KoyuZeminDegrade = listOf(Color(0xFF15110F), Color(0xFF1E1916), Color(0xFF15110F))
val AcikZeminDegrade = listOf(Color(0xFFFBF7F2), Color(0xFFF4EDE4), Color(0xFFFBF7F2))
