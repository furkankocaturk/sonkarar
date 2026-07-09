package com.sonkarar.presentation.cark

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sonkarar.R
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.ortak.GradyanZemin
import com.sonkarar.presentation.ortak.KonfetiEfekti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarkEkrani(
    havuzaGit: (String) -> Unit,
    geriGit: () -> Unit,
    viewModel: CarkViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val baglam = LocalContext.current
    val snackbarDurumu = remember { SnackbarHostState() }
    val haptik = remember { HaptikVeSes(baglam) }

    DisposableEffect(Unit) { onDispose { haptik.serbestBirak() } }

    LaunchedEffect(durum.sonucGosteriliyor) {
        if (durum.sonucGosteriliyor) haptik.kazanildi()
    }

    LaunchedEffect(durum.hataMesaji, durum.bilgiMesaji) {
        (durum.hataMesaji ?: durum.bilgiMesaji)?.let {
            snackbarDurumu.showSnackbar(it)
            viewModel.mesajlariTemizle()
        }
    }

    GradyanZemin {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(durum.cark?.ad ?: stringResource(R.string.cark_baslik)) },
                    navigationIcon = {
                        IconButton(onClick = geriGit) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.geri)
                            )
                        }
                    },
                    actions = {
                        val carkId = durum.cark?.carkId
                        IconButton(
                            onClick = { carkId?.let(havuzaGit) },
                            enabled = carkId != null
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(R.string.havuza_git)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
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
                    DurumMetni(durum)

                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(48.dp)
                            )
                        CarkCizimi(
                            ogeler = durum.carkOgeleri,
                            disHedefAci = durum.disHedefAci,
                            disTur = durum.disTur,
                            otomatikTetik = durum.otomatikTetik,
                            yerelCevirmeEtkin = durum.cevrilebilir,
                            tik = { haptik.tik() },
                            yerelCevirmeBasladi = viewModel::cevirmeBasladi,
                            yerelCevrildi = viewModel::yerelCevrildi,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    }
                    if (!durum.donuyorMu && !durum.sonucGosteriliyor && durum.carkOgeleri.size >= 2) {
                        Text(
                            text = stringResource(R.string.cevir_ipucu),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }

                    if (durum.sonucGosteriliyor && durum.kazananIsim != null) {
                        SonucKarti(oge = durum.kazananOge, azalt = viewModel::kazananiAzalt)
                    }

                    CevirButonu(etkin = durum.cevrilebilir, tikla = viewModel::otomatikCevir)
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

@Composable
private fun CevirButonu(etkin: Boolean, tikla: () -> Unit) {
    val olcek by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (etkin) 1f else 0.94f,
        label = "cevirOlcek"
    )
    Box(
        modifier = Modifier
            .size(132.dp)
            .graphicsLayer { scaleX = olcek; scaleY = olcek }
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
            .clickable(enabled = etkin, onClick = tikla),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.cevir),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

@Composable
private fun SonucKarti(oge: HavuzOgesi?, azalt: () -> Unit) {
    if (oge == null) return
    val uriAcici = LocalUriHandler.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (oge.posterUrl.isNotBlank()) {
                AsyncImage(
                    model = oge.posterUrl,
                    contentDescription = oge.isim,
                    modifier = Modifier.width(86.dp).height(124.dp)
                )
                Spacer(Modifier.width(16.dp))
            } else if (oge.kategori == Kategori.IZLENECEK) {
                Box(
                    modifier = Modifier.width(86.dp).height(124.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sonuc_bicimi, oge.isim),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                if (oge.tur.isNotBlank()) {
                    Text(
                        text = oge.tur,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (oge.platform.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.platform_bicimi, oge.platform),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (oge.puan != null) {
                    Text(
                        text = stringResource(R.string.puan_bicimi, oge.puan),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    if (oge.detayUrl.isNotBlank()) {
                        androidx.compose.material3.Button(onClick = { uriAcici.openUri(oge.detayUrl) }) {
                            val metin = if (oge.kategori == Kategori.YEMEK) {
                                stringResource(R.string.tarife_git)
                            } else {
                                stringResource(R.string.detaylara_git)
                            }
                            Text(metin)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (!oge.disOneriMi && oge.id.isNotBlank()) {
                        TextButton(onClick = azalt) {
                            Text(stringResource(R.string.bunu_azalt))
                        }
                    }
                }
            }
        }
    }
}
