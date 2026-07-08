package com.sonkarar.presentation.havuz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonkarar.R
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.ortak.GradyanZemin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HavuzEkrani(
    geriGit: () -> Unit,
    viewModel: HavuzViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val snackbarDurumu = remember { SnackbarHostState() }
    val ogeSilindiMetni = stringResource(R.string.oge_silindi)

    LaunchedEffect(durum.hataMesaji) {
        durum.hataMesaji?.let {
            snackbarDurumu.showSnackbar(it)
            viewModel.hatayiTemizle()
        }
    }

    GradyanZemin {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.havuz_baslik)) },
                navigationIcon = {
                    IconButton(onClick = geriGit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.geri)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarDurumu) }
    ) { doldurma ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(doldurma)
        ) {
            val secilenSekme = if (durum.aktifKategori == Kategori.YEMEK) 0 else 1
            TabRow(selectedTabIndex = secilenSekme) {
                Tab(
                    selected = secilenSekme == 0,
                    onClick = { viewModel.kategoriDegistir(Kategori.YEMEK) },
                    text = { Text(stringResource(R.string.sekme_yemekler)) }
                )
                Tab(
                    selected = secilenSekme == 1,
                    onClick = { viewModel.kategoriDegistir(Kategori.IZLENECEK) },
                    text = { Text(stringResource(R.string.sekme_izlenecekler)) }
                )
            }

            if (durum.gorunenOgeler.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.havuz_bos),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(durum.gorunenOgeler, key = { it.id }) { oge ->
                        OgeSatiri(
                            oge = oge,
                            sil = { viewModel.ogeSil(oge.id) },
                            favoriDegistir = { viewModel.favoriDegistir(oge) },
                            silindiMetni = ogeSilindiMetni,
                            snackbarDurumu = snackbarDurumu
                        )
                    }
                }
            }

            EkleCubugu(
                metin = durum.yeniOgeMetni,
                metniGuncelle = viewModel::metniGuncelle,
                tur = durum.yeniOgeTuru,
                turuGuncelle = viewModel::turuGuncelle,
                ekle = viewModel::ogeEkle
            )
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OgeSatiri(
    oge: HavuzOgesi,
    sil: () -> Unit,
    favoriDegistir: () -> Unit,
    silindiMetni: String,
    snackbarDurumu: SnackbarHostState
) {
    val kaydirmaDurumu = rememberSwipeToDismissBoxState(
        confirmValueChange = { deger ->
            if (deger == SwipeToDismissBoxValue.EndToStart ||
                deger == SwipeToDismissBoxValue.StartToEnd
            ) {
                sil()
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(kaydirmaDurumu.currentValue) {
        if (kaydirmaDurumu.currentValue != SwipeToDismissBoxValue.Settled) {
            snackbarDurumu.showSnackbar(silindiMetni)
        }
    }

    SwipeToDismissBox(
        state = kaydirmaDurumu,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.sil),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = oge.isim,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                if (oge.tur.isNotBlank()) {
                    Text(
                        text = oge.tur,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (oge.platform.isNotBlank() || oge.puan != null) {
                    val bilgi = listOfNotNull(
                        oge.platform.takeIf { it.isNotBlank() },
                        oge.puan?.let { stringResource(R.string.puan_bicimi, it) }
                    ).joinToString(" • ")
                    Text(
                        text = bilgi,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                val ekleyen = if (oge.disOneriMi) {
                    stringResource(R.string.oneri_rozeti)
                } else {
                    oge.ekleyenKullanici
                }
                Text(
                    text = stringResource(R.string.ekleyen_bicimi, ekleyen),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
                IconButton(onClick = favoriDegistir) {
                    Icon(
                        imageVector = if (oge.favori) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (oge.favori) {
                            stringResource(R.string.favori_cikar)
                        } else {
                            stringResource(R.string.favori_ekle)
                        },
                        tint = if (oge.favori) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EkleCubugu(
    metin: String,
    metniGuncelle: (String) -> Unit,
    tur: String,
    turuGuncelle: (String) -> Unit,
    ekle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = metin,
                onValueChange = metniGuncelle,
                label = { Text(stringResource(R.string.yeni_oge_ipucu)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tur,
                onValueChange = turuGuncelle,
                label = { Text(stringResource(R.string.yeni_tur_ipucu)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
        TextButton(onClick = ekle) {
            Text(
                text = stringResource(R.string.ekle),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
