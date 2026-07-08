package com.sonkarar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sonkarar.presentation.navigasyon.SonKararNavGrafi
import com.sonkarar.presentation.tema.SonKararTemasi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonKararTemasi {
                SonKararNavGrafi()
            }
        }
    }
}
