package com.sonkarar.presentation.navigasyon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sonkarar.presentation.cark.CarkEkrani
import com.sonkarar.presentation.carklar.CarkListesiEkrani
import com.sonkarar.presentation.eslesme.EslesmeEkrani
import com.sonkarar.presentation.gecmis.GecmisEkrani
import com.sonkarar.presentation.giris.GirisEkrani
import com.sonkarar.presentation.havuz.HavuzEkrani
import com.sonkarar.presentation.ortak.YuklemeGostergesi

@Composable
fun SonKararNavGrafi() {
    val navKontrolcu = rememberNavController()
    NavHost(navController = navKontrolcu, startDestination = Rotalar.ACILIS) {

        composable(Rotalar.ACILIS) {
            AcilisYonlendirici(navKontrolcu)
        }
        composable(Rotalar.GIRIS) {
            GirisEkrani(
                girisBasarili = {
                    navKontrolcu.navigate(Rotalar.ACILIS) {
                        popUpTo(Rotalar.GIRIS) { inclusive = true }
                    }
                }
            )
        }
        composable(Rotalar.ESLESME) {
            EslesmeEkrani(
                eslesmeTamamlandi = {
                    navKontrolcu.navigate(Rotalar.CARKLAR) {
                        popUpTo(Rotalar.ESLESME) { inclusive = true }
                    }
                }
            )
        }
        composable(Rotalar.CARKLAR) {
            CarkListesiEkrani(
                carkaGit = { carkId -> navKontrolcu.navigate(Rotalar.cark(carkId)) },
                gecmiseGit = { navKontrolcu.navigate(Rotalar.GECMIS) },
                cikisYapildi = {
                    navKontrolcu.navigate(Rotalar.ACILIS) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Rotalar.CARK_ROTA,
            arguments = listOf(navArgument(Rotalar.ARG_CARK_ID) { type = NavType.StringType })
        ) {
            CarkEkrani(
                havuzaGit = { carkId -> navKontrolcu.navigate(Rotalar.havuz(carkId)) },
                geriGit = { navKontrolcu.popBackStack() }
            )
        }
        composable(
            route = Rotalar.HAVUZ_ROTA,
            arguments = listOf(navArgument(Rotalar.ARG_CARK_ID) { type = NavType.StringType })
        ) {
            HavuzEkrani(geriGit = { navKontrolcu.popBackStack() })
        }
        composable(Rotalar.GECMIS) {
            GecmisEkrani(geriGit = { navKontrolcu.popBackStack() })
        }
    }
}

@Composable
private fun AcilisYonlendirici(
    navKontrolcu: NavHostController,
    viewModel: AcilisViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()

    LaunchedEffect(durum) {
        val hedef = when (durum) {
            AcilisDurumu.GirisGerekli -> Rotalar.GIRIS
            AcilisDurumu.EslesmeGerekli -> Rotalar.ESLESME
            AcilisDurumu.Hazir -> Rotalar.CARKLAR
            AcilisDurumu.Yukleniyor -> null
        }
        if (hedef != null) {
            navKontrolcu.navigate(hedef) {
                popUpTo(Rotalar.ACILIS) { inclusive = true }
            }
        }
    }

    YuklemeGostergesi()
}
