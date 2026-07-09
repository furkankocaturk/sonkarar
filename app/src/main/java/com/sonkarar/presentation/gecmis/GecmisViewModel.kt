package com.sonkarar.presentation.gecmis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.AktifKullaniciyiGozlemleKullanimi
import com.sonkarar.domain.kullanim.SinerjiyiGozlemleKullanimi
import com.sonkarar.domain.model.CarkGecmisiKaydi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GecmisArayuzDurumu(
    val kayitlar: List<CarkGecmisiKaydi> = emptyList(),
    val yukleniyor: Boolean = true
)

@HiltViewModel
class GecmisViewModel @Inject constructor(
    aktifKullaniciyiGozlemle: AktifKullaniciyiGozlemleKullanimi,
    private val sinerjiyiGozlemle: SinerjiyiGozlemleKullanimi
) : ViewModel() {

    private val _durum = MutableStateFlow(GecmisArayuzDurumu())
    val durum = _durum.asStateFlow()

    private var baslatildi = false

    init {
        viewModelScope.launch {
            aktifKullaniciyiGozlemle()
                .filterNotNull()
                .collect { kullanici ->
                    if (kullanici.sinerjiId.isNotBlank() && !baslatildi) {
                        baslatildi = true
                        gecmisiGozlemle(kullanici.sinerjiId)
                    }
                }
        }
    }

    private fun gecmisiGozlemle(sinerjiId: String) {
        viewModelScope.launch {
            sinerjiyiGozlemle(sinerjiId).collect { sonuc ->
                if (sonuc is Sonuc.Basarili) {
                    _durum.update {
                        it.copy(
                            kayitlar = sonuc.veri.carkGecmisi.sortedByDescending { k -> k.zamanDamgasi },
                            yukleniyor = false
                        )
                    }
                }
            }
        }
    }
}
