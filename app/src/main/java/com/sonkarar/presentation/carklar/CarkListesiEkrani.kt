package com.sonkarar.presentation.carklar

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonkarar.R
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.cekirdek.TemaModu
import com.sonkarar.domain.model.Cark
import com.sonkarar.presentation.ortak.GradyanZemin
import com.sonkarar.presentation.tema.CarkPaleti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarkListesiEkrani(
    carkaGit: (String) -> Unit,
    gecmiseGit: () -> Unit,
    cikisYapildi: () -> Unit,
    viewModel: CarkListesiViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val baglam = LocalContext.current
    var menuAcik by remember { mutableStateOf(false) }
    var sonGeri by remember { mutableStateOf(0L) }
    val cikmakMesaji = stringResource(R.string.cikmak_icin_tekrar)

    BackHandler {
        val simdi = System.currentTimeMillis()
        if (simdi - sonGeri < 2000L) {
            (baglam as? Activity)?.finish()
        } else {
            sonGeri = simdi
            Toast.makeText(baglam, cikmakMesaji, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(durum.cikisYapildi) {
        if (durum.cikisYapildi) cikisYapildi()
    }

    GradyanZemin {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.carklar_baslik)) },
                    actions = {
                        IconButton(onClick = gecmiseGit) {
                            Icon(Icons.Filled.History, stringResource(R.string.gecmise_git))
                        }
                        IconButton(onClick = { menuAcik = true }) {
                            Icon(Icons.Filled.MoreVert, stringResource(R.string.menu))
                        }
                        DropdownMenu(expanded = menuAcik, onDismissRequest = { menuAcik = false }) {
                            Text(
                                text = stringResource(R.string.tema),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            TemaModu.entries.forEach { mod ->
                                DropdownMenuItem(
                                    text = { Text(mod.etiket) },
                                    onClick = { menuAcik = false; viewModel.temaSec(mod) }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.cikis_yap)) },
                                onClick = { menuAcik = false; viewModel.cikisYap() }
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
            floatingActionButton = {
                FloatingActionButton(onClick = viewModel::eklemeyiAc) {
                    Icon(Icons.Filled.Add, stringResource(R.string.yeni_cark))
                }
            }
        ) { doldurma ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(doldurma)
                    .padding(12.dp)
            ) {
                items(durum.carklar, key = { it.carkId }) { cark ->
                    CarkKarti(
                        cark = cark,
                        tikla = { carkaGit(cark.carkId) },
                        sil = { viewModel.sil(cark.carkId) }
                    )
                }
            }
        }
    }

    if (durum.eklemeAcik) {
        YeniCarkDiyalogu(
            ad = durum.yeniAd,
            adGuncelle = viewModel::adGuncelle,
            olustur = viewModel::carkOlustur,
            kapat = viewModel::eklemeyiKapat
        )
    }
}

@Composable
private fun CarkKarti(cark: Cark, tikla: () -> Unit, sil: () -> Unit) {
    val renk = CarkPaleti[(cark.carkId.hashCode() and 0x7fffffff) % CarkPaleti.size]
    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clickable(onClick = tikla),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                        .background(
                            Brush.linearGradient(listOf(renk.copy(alpha = 0.85f), renk)),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Casino,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Text(
                    text = cark.ad,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            if (!cark.sistemMi) {
                IconButton(
                    onClick = sil,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.sil),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun YeniCarkDiyalogu(
    ad: String,
    adGuncelle: (String) -> Unit,
    olustur: () -> Unit,
    kapat: () -> Unit
) {
    AlertDialog(
        onDismissRequest = kapat,
        title = { Text(stringResource(R.string.yeni_cark)) },
        text = {
            Column {
                OutlinedTextField(
                    value = ad,
                    onValueChange = adGuncelle,
                    label = { Text(stringResource(R.string.cark_adi)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.yeni_cark_ipucu),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = olustur) { Text(stringResource(R.string.olustur)) }
        },
        dismissButton = {
            TextButton(onClick = kapat) { Text(stringResource(R.string.kapat)) }
        }
    )
}
