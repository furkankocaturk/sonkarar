package com.sonkarar.presentation.giris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.kullanim.GoogleIleGirisYapKullanimi
import com.sonkarar.domain.kullanim.YerelModaGecKullanimi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GirisViewModel @Inject constructor(
    private val googleIleGirisYap: GoogleIleGirisYapKullanimi,
    private val yerelModaGec: YerelModaGecKullanimi
) : ViewModel() {

    private val _durum = MutableStateFlow(GirisArayuzDurumu())
    val durum = _durum.asStateFlow()

    fun girisYap(kimlikJetonu: String) {
        _durum.update { it.copy(yukleniyor = true, hataMesaji = null) }
        viewModelScope.launch {
            when (val sonuc = googleIleGirisYap(kimlikJetonu)) {
                is Sonuc.Basarili ->
                    _durum.update { it.copy(yukleniyor = false, girisBasarili = true) }
                is Sonuc.Hata ->
                    _durum.update { it.copy(yukleniyor = false, hataMesaji = sonuc.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun cevrimdisiDevamEt() {
        _durum.update { it.copy(yukleniyor = true, hataMesaji = null) }
        viewModelScope.launch {
            when (val sonuc = yerelModaGec()) {
                is Sonuc.Basarili ->
                    _durum.update { it.copy(yukleniyor = false, girisBasarili = true) }
                is Sonuc.Hata ->
                    _durum.update { it.copy(yukleniyor = false, hataMesaji = sonuc.mesaj) }
                Sonuc.Yukleniyor -> Unit
            }
        }
    }

    fun hataBildir(mesaj: String) {
        _durum.update { it.copy(yukleniyor = false, hataMesaji = mesaj) }
    }

    fun yuklemeBaslat() {
        _durum.update { it.copy(yukleniyor = true, hataMesaji = null) }
    }

    fun hatayiTemizle() {
        _durum.update { it.copy(hataMesaji = null) }
    }
}
