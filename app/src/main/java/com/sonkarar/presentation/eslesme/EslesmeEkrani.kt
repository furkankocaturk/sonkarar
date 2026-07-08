package com.sonkarar.presentation.eslesme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonkarar.R

@Composable
fun EslesmeEkrani(
    eslesmeTamamlandi: () -> Unit,
    viewModel: EslesmeViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val snackbarDurumu = remember { SnackbarHostState() }

    LaunchedEffect(durum.tamamlandi) {
        if (durum.tamamlandi) eslesmeTamamlandi()
    }

    LaunchedEffect(durum.hataMesaji) {
        durum.hataMesaji?.let {
            snackbarDurumu.showSnackbar(it)
            viewModel.hatayiTemizle()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarDurumu) }
    ) { doldurma ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(doldurma)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.eslesme_baslik),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.eslesme_aciklama),
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = durum.esEposta,
                onValueChange = viewModel::epostaGuncelle,
                label = { Text(stringResource(R.string.es_eposta_etiketi)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            if (durum.yukleniyor) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                Button(
                    onClick = viewModel::odaOlustur,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(stringResource(R.string.oda_olustur_katil))
                }
                TextButton(
                    onClick = viewModel::tekBasinaKullan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.tek_basina_kullan))
                }
            }
        }
    }
}
