package com.sonkarar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonkarar.cekirdek.TemaModu
import com.sonkarar.presentation.navigasyon.SonKararNavGrafi
import com.sonkarar.presentation.tema.SonKararTemasi
import com.sonkarar.presentation.tema.TemaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val temaViewModel: TemaViewModel = hiltViewModel()
            val temaModu by temaViewModel.temaModu.collectAsStateWithLifecycle()
            val koyuTema = when (temaModu) {
                TemaModu.KOYU -> true
                TemaModu.ACIK -> false
                TemaModu.SISTEM -> isSystemInDarkTheme()
            }
            SonKararTemasi(koyuTema = koyuTema) {
                SonKararNavGrafi()
            }
        }
    }
}
