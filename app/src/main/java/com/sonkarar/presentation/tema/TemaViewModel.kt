package com.sonkarar.presentation.tema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.TemaModu
import com.sonkarar.domain.kullanim.TemaModunuGozlemleKullanimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TemaViewModel @Inject constructor(
    temaModunuGozlemle: TemaModunuGozlemleKullanimi
) : ViewModel() {
    val temaModu = temaModunuGozlemle().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TemaModu.SISTEM
    )
}
