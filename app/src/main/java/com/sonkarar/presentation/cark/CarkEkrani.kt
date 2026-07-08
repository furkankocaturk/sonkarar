package com.sonkarar.presentation.cark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonkarar.R
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.presentation.ortak.KonfetiEfekti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarkEkrani(
    havuzaGit: () -> Unit,
    viewModel: CarkViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val baglam = LocalContext.current
    val snackbarDurumu = remember { SnackbarHostState() }
    val haptik = remember { HaptikVeSes(baglam) }

    DisposableEffect(Unit) {
        onDispose { haptik.serbestBirak() }
    }

    LaunchedEffect(durum.hataMesaji) {
        durum.hataMesaji?.let {
            snackbarDurumu.showSnackbar(it)
            viewModel.hatayiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cark_baslik)) },
                actions = {
                    IconButton(onClick = havuzaGit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.havuza_git)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarDurumu) }
    ) { doldurma ->
        Box(modifier = Modifier.fillMaxSize().padding(doldurma)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KategoriSecici(
                    aktifKategori = durum.aktifKategori,
                    secildi = viewModel::kategoriDegistir,
                    etkin = !durum.donuyorMu
                )

                DurumMetni(durum)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Gösterge (tepede aşağıyı işaret eden ok)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        CarkCizimi(
                            ogeler = durum.gorunenOgeler,
                            hedefAci = durum.hedefAci,
                            donuyorMu = durum.donuyorMu,
                            cizgiGecildi = { haptik.tik() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                }

                if (durum.sonucGosteriliyor && durum.kazananIsim != null) {
                    Text(
                        text = stringResource(R.string.sonuc_bicimi, durum.kazananIsim!!),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = viewModel::cevir,
                    enabled = !durum.donuyorMu,
                    modifier = Modifier
                        .size(120.dp),
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.cevir),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            if (durum.sonucGosteriliyor) {
                KonfetiEfekti(
                    tetikleyici = durum.konfetiTetikleyici,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriSecici(
    aktifKategori: Kategori,
    secildi: (Kategori) -> Unit,
    etkin: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip(
            selected = aktifKategori == Kategori.YEMEK,
            onClick = { if (etkin) secildi(Kategori.YEMEK) },
            label = { Text(stringResource(R.string.kategori_yemek)) }
        )
        FilterChip(
            selected = aktifKategori == Kategori.IZLENECEK,
            onClick = { if (etkin) secildi(Kategori.IZLENECEK) },
            label = { Text(stringResource(R.string.kategori_izlenecek)) }
        )
    }
}

@Composable
private fun DurumMetni(durum: CarkArayuzDurumu) {
    val metin = when {
        durum.donuyorMu && durum.benCeviriyorum -> stringResource(R.string.cark_donuyor)
        durum.donuyorMu && !durum.benCeviriyorum -> stringResource(R.string.es_ceviriyor)
        else -> ""
    }
    if (metin.isNotBlank()) {
        Text(
            text = metin,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
