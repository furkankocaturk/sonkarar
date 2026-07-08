package com.sonkarar.presentation.eslesme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.OdaOlusturVeyaKatilKullanimi
import com.sonkarar.domain.kullanim.TekBasinaBaslatKullanimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EslesmeViewModel @Inject constructor(
    private val odaOlusturVeyaKatil: OdaOlusturVeyaKatilKullanimi,
    private val tekBasinaBaslat: TekBasinaBaslatKullanimi
) : ViewModel() {

    private val _durum = MutableStateFlow(EslesmeArayuzDurumu())
    val durum = _durum.asStateFlow()

    fun epostaGuncelle(yeni: String) = _durum.update { it.copy(esEposta = yeni) }

    fun odaOlustur() {
        _durum.update { it.copy(yukleniyor = true, hataMesaji = null) }
        viewModelScope.launch {
            when (val sonuc = odaOlusturVeyaKatil(_durum.value.esEposta)) {
                is Sonuc.Basarili ->
                    _durum.update { it.copy(yukleniyor = false, tamamlandi = true) }
                is Sonuc.Hata ->
                    _durum.update { it.copy(yukleniyor = false, hataMesaji = sonuc.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun tekBasinaKullan() {
        _durum.update { it.copy(yukleniyor = true, hataMesaji = null) }
        viewModelScope.launch {
            when (val sonuc = tekBasinaBaslat()) {
                is Sonuc.Basarili ->
                    _durum.update { it.copy(yukleniyor = false, tamamlandi = true) }
                is Sonuc.Hata ->
                    _durum.update { it.copy(yukleniyor = false, hataMesaji = sonuc.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun hatayiTemizle() = _durum.update { it.copy(hataMesaji = null) }
}
