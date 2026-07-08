package com.sonkarar.presentation.cark

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
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.sonkarar.cekirdek.TemaModu
import com.sonkarar.domain.model.HavuzOgesi
import com.sonkarar.presentation.ortak.GradyanZemin
import com.sonkarar.presentation.ortak.KonfetiEfekti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarkEkrani(
    havuzaGit: () -> Unit,
    gecmiseGit: () -> Unit,
    cikisYapildi: () -> Unit,
    viewModel: CarkViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val baglam = LocalContext.current
    val snackbarDurumu = remember { SnackbarHostState() }
    val haptik = remember { HaptikVeSes(baglam) }
    var menuAcik by remember { mutableStateOf(false) }
    var sonGeriZamani by remember { mutableStateOf(0L) }
    val cikmakMesaji = stringResource(R.string.cikmak_icin_tekrar)

    DisposableEffect(Unit) {
        onDispose { haptik.serbestBirak() }
    }

    // Çift geri: ilk basışta uyarı, ikinci basışta çıkış (evrensel davranış).
    BackHandler {
        val simdi = System.currentTimeMillis()
        if (simdi - sonGeriZamani < 2000L) {
            (baglam as? Activity)?.finish()
        } else {
            sonGeriZamani = simdi
            Toast.makeText(baglam, cikmakMesaji, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(durum.sonucGosteriliyor) {
        if (durum.sonucGosteriliyor) haptik.kazanildi()
    }

    LaunchedEffect(durum.cikisYapildi) {
        if (durum.cikisYapildi) cikisYapildi()
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
                title = { Text(stringResource(R.string.cark_baslik)) },
                actions = {
                    IconButton(onClick = havuzaGit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.havuza_git)
                        )
                    }
                    IconButton(onClick = { menuAcik = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.menu)
                        )
                    }
                    DropdownMenu(
                        expanded = menuAcik,
                        onDismissRequest = { menuAcik = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.gecmise_git)) },
                            onClick = {
                                menuAcik = false
                                gecmiseGit()
                            }
                        )
                        HorizontalDivider()
                        Text(
                            text = stringResource(R.string.tema),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        TemaModu.entries.forEach { mod ->
                            DropdownMenuItem(
                                text = { Text(mod.etiket) },
                                onClick = {
                                    menuAcik = false
                                    viewModel.temaSec(mod)
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.cikis_yap)) },
                            onClick = {
                                menuAcik = false
                                viewModel.cikisYap()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
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
                            ogeler = durum.cizilecekOgeler,
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
                    SonucKarti(
                        oge = durum.kazananOge,
                        azalt = viewModel::kazananiAzalt
                    )
                }

                CevirButonu(
                    etkin = !durum.donuyorMu,
                    tikla = viewModel::cevir
                )
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
                    modifier = Modifier
                        .width(86.dp)
                        .height(124.dp)
                )
                Spacer(Modifier.width(16.dp))
            } else if (oge.kategori == Kategori.IZLENECEK) {
                Box(
                    modifier = Modifier
                        .width(86.dp)
                        .height(124.dp),
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
                if (oge.kaynakAdi.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.kaynak_bicimi, oge.kaynakAdi),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    if (oge.detayUrl.isNotBlank()) {
                        Button(onClick = { uriAcici.openUri(oge.detayUrl) }) {
                            val metin = if (oge.kategori == Kategori.YEMEK) {
                                stringResource(R.string.tarife_git)
                            } else {
                                stringResource(R.string.detaylara_git)
                            }
                            Text(metin)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (!oge.disOneriMi) {
                        TextButton(onClick = azalt) {
                            Text(stringResource(R.string.bunu_azalt))
                        }
                    }
                }
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
