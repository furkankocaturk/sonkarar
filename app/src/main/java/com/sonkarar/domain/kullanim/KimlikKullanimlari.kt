package com.sonkarar.domain.kullanim

import com.sonkarar.cekirdek.Sonuc
import com.sonkarar.domain.model.Kullanici
import com.sonkarar.domain.repository.KimlikRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AktifKullaniciyiGozlemleKullanimi @Inject constructor(
    private val kimlikRepository: KimlikRepository
) {
    operator fun invoke(): Flow<Kullanici?> = kimlikRepository.aktifKullaniciyiGozlemle()
}

class GoogleIleGirisYapKullanimi @Inject constructor(
    private val kimlikRepository: KimlikRepository
) {
    suspend operator fun invoke(kimlikJetonu: String): Sonuc<Kullanici> {
        if (kimlikJetonu.isBlank()) {
            return Sonuc.Hata("Google kimlik bilgisi alınamadı. Lütfen tekrar deneyin.")
        }
        return kimlikRepository.googleIleGirisYap(kimlikJetonu)
    }
}

class OturumuKapatKullanimi @Inject constructor(
    private val kimlikRepository: KimlikRepository
) {
    suspend operator fun invoke(): Sonuc<Unit> = kimlikRepository.oturumuKapat()
}
