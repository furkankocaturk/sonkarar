# SonKarar — Ürün Gereksinim Dökümanı (PRD)

> Bu döküman, **SonKarar** Android uygulamasının ürün gereksinimlerini tanımlar.
> Tüm arayüz metinleri, değişken isimleri, veri tabanı alanları ve kullanıcıya
> gösterilen mesajlar **Türkçe** olacaktır.

---

## 1. Belge Bilgileri

| Alan | Değer |
| --- | --- |
| Ürün Adı | SonKarar |
| Platform | Android (yalnızca) |
| Belge Sürümü | 1.0 |
| Hedef Kitle | Çiftler (iki kişilik kapalı odalar) |
| Dil | Türkçe (tüm katmanlarda zorunlu) |
| Mimari Yaklaşım | Clean Architecture + MVVM + tek modüllü çok katmanlı yapı |

Bu belge seti, bir yapay zekâ kod ajanının (OpenAI Codex, Cursor vb.) **sıfır belirsizlikle** ve **derlenebilir** bir proje üretebilmesi için yazılmıştır. Ajan, `docs/` altındaki dosyaları sırasıyla okumalı ve `07-KOD-URETIM-TALIMATLARI.md` içindeki "Definition of Done" kontrol listesini karşılamalıdır.

### Döküman Haritası

| Sıra | Dosya | İçerik |
| --- | --- | --- |
| 00 | `00-URUN-GEREKSINIM-DOKUMANI.md` | Ürün vizyonu, kapsam, kullanıcı akışları, kabul kriterleri |
| 01 | `01-TEKNOLOJI-YIGINI-VE-SURUMLER.md` | Bağımlılıklar, sürümler, Gradle sürüm kataloğu |
| 02 | `02-PROJE-YAPISI-VE-MIMARI.md` | Paket yapısı, katmanlar, Hilt bağımlılık enjeksiyonu |
| 03 | `03-VERI-KATMANI-VE-SEMALAR.md` | Firestore şeması, Room şeması, veri modelleri, güvenlik kuralları |
| 04 | `04-REPOSITORY-VE-DOMAIN.md` | Repository sözleşmeleri, UseCase'ler, `Sonuc` hata yönetimi |
| 05 | `05-ALGORITMALAR-VE-IS-MANTIGI.md` | Ağırlıklı çark, zaman cezası, öneri enjeksiyonu, senkronizasyon |
| 06 | `06-ARAYUZ-VE-EKRAN-MIMARISI.md` | Navigasyon, ekranlar, tema, çark çizimi, haptik ve ses |
| 07 | `07-KOD-URETIM-TALIMATLARI.md` | Kod ajanı talimatları, kontrol listesi, kabul kriterleri |

---

## 2. Proje Özeti ve Hedef

**SonKarar**, çiftlerin günlük hayatta yaşadığı *"Ne yesek?"*, *"Ne izlesek?"* gibi kararsızlık krizlerini çözen, iki farklı Android cihazda **gerçek zamanlı (senkronize)** çalışan akıllı bir karar çarkı uygulamasıdır.

Uygulama:

1. Çiftlerin ortaklaşa beslediği bir **veri havuzundan** beslenir.
2. **Geçmiş seçimleri analiz eder** (zaman cezası ile monotonluğu engeller).
3. Dışarıdan (TMDB API / hazır yemek etiketleri) **yeni öneriler enjekte eder**.
4. **Ağırlıklı bir algoritmayla**, animasyonlu bir çark üzerinden kesin kararı verir.
5. Kararı her iki cihazda **aynı anda** konfeti efektiyle gösterir.

### Ürün Vizyonu

> "İki kişilik bir odada, adil ve eğlenceli biçimde kararı çarka bırakmak; monotonluğu kıran, geçmişi hatırlayan ve yeni fikirler öneren bir dijital hakem."

### Başarı Ölçütleri (KPI)

| Ölçüt | Hedef |
| --- | --- |
| Çark çevirme senkron gecikmesi | < 1 saniye (iki cihaz arası) |
| Çevrimdışı havuz görüntüleme | %100 (Room önbelleği ile) |
| Aynı ögenin arka arkaya çıkma oranı | Zaman cezası ile belirgin biçimde azalır |
| Çökme (crash) oranı | %0 hedefi — tüm ağ/servis hataları yakalanır |

---

## 3. Kişiler (Personas)

- **Ela (28):** Yoğun çalışan, akşam "ne yesek" tartışmasından bıkmış. Hızlı ve eğlenceli karar ister.
- **Deniz (30):** Ela'nın eşi. Yeni diziler keşfetmeyi sever; uygulamanın öneri getirmesini önemser.

Her iki kullanıcı da tek bir **sinerji odası** (ortak oda) içinde eşleşir.

---

## 4. Kapsam

### 4.1. Kapsam İçi (MVP)

- Google ile Firebase kimlik doğrulama.
- Eş e-postası ile ortak oda (sinerji) oluşturma / var olan odaya katılma.
- Ortak havuz yönetimi: iki kategori (YEMEK, IZLENECEK), ekleme, kaydırarak silme.
- Ağırlıklı rastgele çark seçimi + zaman cezası + dış öneri enjeksiyonu.
- Gerçek zamanlı senkronizasyon (eş çarkı çeviriyor durumu + ortak sonuç).
- Çevrimdışı okuma (Room önbelleği).
- Koyu/Neon tema, fizik tabanlı çark, haptik geri bildirim ve ses efekti.
- Türkçe hata mesajları ve kararlı hata yakalama (`Sonuc` sealed class).

### 4.2. Kapsam Dışı (Bu sürümde yapılmayacak)

- İkiden fazla üyeli odalar.
- iOS / Web istemcileri.
- Ödeme / abonelik.
- Bildirim (push) altyapısı (opsiyonel, ileride).
- Sosyal paylaşım.

---

## 5. Kullanıcı Akışları

### 5.1. İlk Giriş ve Eşleşme Akışı

```
[Uygulama Açılır]
      │
      ▼
[Oturum var mı?] ──Hayır──► [Giriş Ekranı] ──"Google ile Giriş Yap"──► [Firebase Auth]
      │Evet                                                                   │
      ▼                                                                       ▼
[Sinerji odası var mı?] ──Hayır──► [Eşleşme Ekranı: Eş e-postası gir]        │
      │Evet                             │                                     │
      ▼                                 ▼                                     │
[Karar Çarkı Ekranı] ◄──── [Ortak Oda Oluştur / Katıl] ◄─────────────────────┘
```

**Kurallar:**

1. Kullanıcı Google ile giriş yaptıktan sonra `kullanicilar/{kullaniciId}` belgesi yoksa oluşturulur.
2. Kullanıcı eş e-postasını girer:
   - Karşı taraf **bekleyen bir eşleşme** başlattıysa → aynı `sinerjiId` altında birleştirilir.
   - Başlatmadıysa → yeni `sinerjiId` oluşturulur, eş katılana kadar "Eş bekleniyor" durumu gösterilir.
3. Eşleşme tamamlandığında iki kullanıcının `sinerjiId` alanı aynı değere set edilir.

### 5.2. Havuz Besleme Akışı

```
[Ortak Havuz Ekranı]
   ├── Sekme: YEMEKLER
   ├── Sekme: İZLENECEKLER
   ├── Metin alanı + "Ekle" → /havuz altına yeni öge (agirlik=10, disOneriMi=false)
   ├── Öge kartında "Ekleyen: <ad/eposta>" bilgisi
   └── Sağa/sola kaydır → sil (onay Snackbar'ı ile geri al)
```

### 5.3. Karar Akışı

```
[Karar Çarkı Ekranı]
   │  Aktif kategori seç (Yemek / İzlenecek)
   ▼
["ÇEVİR" butonuna bas]
   │  1) Havuz + geçmiş çekilir
   │  2) Zaman cezası uygulanır (son 3 turda çıkanlar %80 düşer)
   │  3) 3 adet dış öneri enjekte edilir (disOneriMi=true)
   │  4) Ağırlıklı rastgele seçim → kazanan belirlenir
   │  5) Sinerji durumu "ÇEVRILIYOR" yapılır (eş ekranında animasyon)
   ▼
[Çark fizik tabanlı yavaşlar → kazananda durur]
   │  Haptik + ses efekti çizgi geçişlerinde tetiklenir
   ▼
[Kazanan konfeti ile gösterilir; carkGecmisi'ne kayıt eklenir]
```

---

## 6. Fonksiyonel Gereksinimler

| Kod | Gereksinim | Öncelik |
| --- | --- | --- |
| FG-01 | Kullanıcı Google ile giriş yapabilmeli | Zorunlu |
| FG-02 | Kullanıcı eş e-postası ile ortak oda oluşturabilmeli / katılabilmeli | Zorunlu |
| FG-03 | Kullanıcı havuza YEMEK/IZLENECEK ögesi ekleyebilmeli | Zorunlu |
| FG-04 | Kullanıcı ögeyi kaydırarak silebilmeli (geri al desteğiyle) | Zorunlu |
| FG-05 | Her öge kimin eklediğini göstermeli | Zorunlu |
| FG-06 | Çark ağırlıklı rastgele seçim yapmalı | Zorunlu |
| FG-07 | Son 3 turda çıkan ögelere %80 zaman cezası uygulanmalı | Zorunlu |
| FG-08 | Her turda 3 dış öneri enjekte edilmeli (`disOneriMi=true`) | Zorunlu |
| FG-09 | Eş çarkı çevirdiğinde diğer cihazda anlık durum görünmeli | Zorunlu |
| FG-10 | Kazanan sonuç iki cihazda konfeti ile eşzamanlı gösterilmeli | Zorunlu |
| FG-11 | Çark çevirme sonucu `carkGecmisi`'ne yazılmalı | Zorunlu |
| FG-12 | Çevrimdışıyken havuz Room'dan okunabilmeli | Zorunlu |
| FG-13 | Çark dönerken haptik + ses efekti tetiklenmeli | Zorunlu |
| FG-14 | Tüm hatalar Türkçe mesajla ele alınmalı, çökme olmamalı | Zorunlu |

---

## 7. Fonksiyonel Olmayan Gereksinimler

- **Kararlılık:** Tüm ağ ve servis çağrıları `try/catch` + `Sonuc` sealed class ile sarılır; işlenmeyen istisna olmaz.
- **Çevrimdışı Dayanıklılık:** Firestore çevrimdışı kalıcılığı açık; ek olarak Room "gerçek kaynak" (single source of truth) önbelleği tutulur.
- **Erişilebilirlik:** Tüm etkileşimli bileşenlerde `contentDescription`, minimum 48dp dokunma alanı.
- **Performans:** Çark animasyonu 60 FPS hedefiyle akıcı; `Canvas` üzerinde donanım hızlandırmalı çizim.
- **Güvenlik:** Firestore güvenlik kuralları yalnızca oda üyelerine erişim verir (bkz. `03-VERI-KATMANI-VE-SEMALAR.md`).
- **Gizlilik:** TMDB API anahtarı kod içine gömülmez; `local.properties` üzerinden `BuildConfig`'e aktarılır.

---

## 8. Ekran Envanteri (Özet)

| Ekran | Rota | Amaç |
| --- | --- | --- |
| Açılış / Yönlendirme | `acilis` | Oturum ve oda durumuna göre yönlendirme |
| Giriş | `giris` | Google ile giriş |
| Eşleşme | `eslesme` | Eş e-postası ile oda oluştur/katıl |
| Ortak Havuz | `havuz` | Yemek/İzlenecek sekmeleri, ekle/sil |
| Karar Çarkı | `cark` | Ana ekran, çark ve sonuç |

> Ekranların ayrıntılı bileşen tasarımı `06-ARAYUZ-VE-EKRAN-MIMARISI.md` içindedir.

---

## 9. Kabul Kriterleri (Üst Düzey)

1. Uygulama Firebase yapılandırması olmadan da **derlenebilir** olmalı (çalışma zamanında kibar hata verir).
2. İki emülatör/cihazda aynı odaya girildiğinde çark durumu senkron olmalı.
3. Havuz boşken çark çevirince Türkçe uyarı gösterilmeli ("Havuzda hiç seçenek yok...").
4. İnternet kapalıyken uygulama çökmemeli; havuz Room'dan görüntülenmeli.
5. Tüm görünür metinler Türkçe olmalı; sabit İngilizce metin bulunmamalı.

> Ayrıntılı ve test edilebilir kabul kriterleri `07-KOD-URETIM-TALIMATLARI.md` içinde listelenmiştir.
