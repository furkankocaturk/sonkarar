package com.sonkarar.presentation.navigasyon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.domain.kullanim.AktifKullaniciyiGozlemleKullanimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AcilisDurumu {
    data object Yukleniyor : AcilisDurumu
    data object GirisGerekli : AcilisDurumu
    data object EslesmeGerekli : AcilisDurumu
    data object Hazir : AcilisDurumu
}

@HiltViewModel
class AcilisViewModel @Inject constructor(
    aktifKullaniciyiGozlemle: AktifKullaniciyiGozlemleKullanimi
) : ViewModel() {

    val durum = aktifKullaniciyiGozlemle()
        .map { kullanici ->
            when {
                kullanici == null -> AcilisDurumu.GirisGerekli
                !kullanici.eslesmisMi -> AcilisDurumu.EslesmeGerekli
                else -> AcilisDurumu.Hazir
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AcilisDurumu.Yukleniyor
        )
}
