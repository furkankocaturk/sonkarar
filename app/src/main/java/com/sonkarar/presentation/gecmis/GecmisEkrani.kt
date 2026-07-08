package com.sonkarar.presentation.gecmis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonkarar.R
import com.sonkarar.cekirdek.Kategori
import com.sonkarar.presentation.ortak.GradyanZemin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GecmisEkrani(
    geriGit: () -> Unit,
    viewModel: GecmisViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val bicimlendirici = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")) }

    GradyanZemin {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gecmis_baslik)) },
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
        }
    ) { doldurma ->
        if (durum.kayitlar.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(doldurma),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.gecmis_bos),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(doldurma),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) {
                items(durum.kayitlar) { kayit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = kayit.sonuc,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val kategoriEtiket = if (kayit.kategori == Kategori.YEMEK) {
                                stringResource(R.string.kategori_yemek)
                            } else {
                                stringResource(R.string.kategori_izlenecek)
                            }
                            Text(
                                text = "$kategoriEtiket • ${bicimlendirici.format(Date(kayit.zamanDamgasi))}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
