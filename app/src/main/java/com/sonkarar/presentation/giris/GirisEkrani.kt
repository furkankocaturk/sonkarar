package com.sonkarar.presentation.giris

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sonkarar.R
import kotlinx.coroutines.launch

@Composable
fun GirisEkrani(
    girisBasarili: () -> Unit,
    viewModel: GirisViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val baglam = LocalContext.current
    val kapsam = rememberCoroutineScope()
    val snackbarDurumu = remember { SnackbarHostState() }
    val genelHataMesaji = stringResource(R.string.giris_hatasi)

    LaunchedEffect(durum.girisBasarili) {
        if (durum.girisBasarili) girisBasarili()
    }

    LaunchedEffect(durum.hataMesaji) {
        durum.hataMesaji?.let {
            snackbarDurumu.showSnackbar(it)
            viewModel.hatayiTemizle()
        }
    }

    val webIstemciKimligi = stringResource(R.string.varsayilan_web_istemci_kimligi)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarDurumu) }
    ) { doldurma ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(doldurma)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.uygulama_adi),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.slogan),
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            if (durum.yukleniyor) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            } else {
                Button(
                    onClick = {
                        viewModel.yuklemeBaslat()
                        kapsam.launch {
                            try {
                                val secenek = GetGoogleIdOption.Builder()
                                    .setServerClientId(webIstemciKimligi)
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                                val istek = GetCredentialRequest.Builder()
                                    .addCredentialOption(secenek)
                                    .build()
                                val yonetici = CredentialManager.create(baglam)
                                val yanit = yonetici.getCredential(baglam, istek)
                                val kimlikBilgisi =
                                    GoogleIdTokenCredential.createFrom(yanit.credential.data)
                                viewModel.girisYap(kimlikBilgisi.idToken)
                            } catch (hata: Exception) {
                                viewModel.hataBildir(genelHataMesaji)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.google_ile_giris))
                }
                TextButton(
                    onClick = viewModel::cevrimdisiDevamEt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.girissiz_devam))
                }
            }
        }
    }
}
