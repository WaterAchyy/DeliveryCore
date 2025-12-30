# 🎉 DeliveryCore v1.1.0 - Tamamlandı!

## 📦 Paket Bilgileri
- **Versiyon**: v1.1.0
- **JAR Dosyası**: `target/DeliveryCore-v1.1.0.jar`
- **Minecraft Uyumluluğu**: 1.16.5 - 1.21+
- **Java Gereksinimleri**: Java 17+

## ✨ v1.1 Yeni Özellikler

### 🌟 1. Sezonluk Sistem
- **Tarih Aralığı Kontrolü**: Belirli tarihler arasında etkinlik
- **Özel Gün Seçimi**: Sadece belirli günlerde aktif (örn: hafta sonları)
- **Otomatik Devre Dışı**: Sezon bittiğinde otomatik kapanır
- **Kalan Gün Gösterimi**: Tab list'te kaç gün kaldığını gösterir

```yaml
season:
  enabled: true
  start-date: "2024-12-20T00:00:00+03:00"
  end-date: "2025-01-10T23:59:59+03:00"
  custom-days: true
  active-days: ["FRIDAY", "SATURDAY", "SUNDAY"]
```

### 📋 2. Tab List Entegrasyonu
- **Konfigürasyondan Açılabilir**: Her teslimat için ayrı ayrı
- **Özelleştirilebilir Format**: Kendi mesajınızı yazabilirsiniz
- **Kalan Gün Gösterimi**: Sezonluk etkinliklerde gün sayacı
- **Otomatik Güncelleme**: Belirlediğiniz aralıkta güncellenir

```yaml
tab-display:
  enabled: true
  format: "&6[DeliveryCore] &e{delivery} &7- &a{item} &7({days} gün kaldı)"
  show-days-remaining: true
  update-interval-ticks: 100
```

### 🏆 3. Hologram Sıralama Sistemi
- **Admin Yönetimi**: `/dc holo create/list/remove` komutları
- **Otomatik Sıralama**: Etkinlik başladığında otomatik güncellenir
- **Özelleştirilebilir**: Her teslimat için ayrı hologram ayarları
- **HolographicDisplays Entegrasyonu**: Popüler hologram eklentisi desteği

```yaml
hologram:
  enabled: true
  show-leaderboard: true
  max-players: 10
  update-interval-ticks: 200
```

### 🎮 4. Admin Komutları (v1.1)
```
/dc holo create <id>     - Bulunduğun yerde hologram oluştur
/dc holo list            - Tüm hologramları listele
/dc holo remove <id>     - Hologram sil
```

## 🔧 Teknik Geliştirmeler

### ✅ Tamamlanan Ana Görevler
- [x] **Minecraft 1.21 Uyumluluğu** - Yeni itemlar ve API desteği
- [x] **Sezonluk Sistem** - SeasonService ve SeasonConfig implementasyonu
- [x] **Tab List Entegrasyonu** - TabListService ve konfigürasyon
- [x] **Hologram Sistemi** - HologramService ve admin komutları
- [x] **Geriye Dönük Uyumluluk** - v1.0 konfigürasyonları çalışmaya devam eder
- [x] **Property Testler** - 6 yeni property test eklendi
- [x] **Konfigürasyon Doğrulama** - v1.1 özellikleri için doğrulama kuralları

### 📁 Yeni Dosyalar (v1.1)
```
src/main/java/com/deliverycore/model/
├── SeasonConfig.java           ✅
├── TabDisplayConfig.java       ✅
├── HologramConfig.java         ✅
└── HologramInfo.java          ✅

src/main/java/com/deliverycore/service/
├── SeasonService.java          ✅
├── SeasonServiceImpl.java      ✅
├── TabListService.java         ✅
├── TabListServiceImpl.java     ✅
├── HologramService.java        ✅
└── HologramServiceImpl.java    ✅

src/test/java/com/deliverycore/v11/
├── Minecraft121CompatibilityPropertyTest.java    ✅
├── SeasonStatusPropertyTest.java                 ✅
├── SeasonDayCalculationPropertyTest.java         ✅
├── TabListFormatPropertyTest.java                ✅
├── HologramUpdatePropertyTest.java               ✅
└── LeaderboardHologramFormatPropertyTest.java    ✅
```

## 🎯 Kullanım Örnekleri

### 1. Sezonluk Yılbaşı Etkinliği
```yaml
yilbasi-ozel:
  enabled: true
  season:
    enabled: true
    start-date: "2024-12-20T00:00:00+03:00"
    end-date: "2025-01-10T23:59:59+03:00"
    custom-days: true
    active-days: ["FRIDAY", "SATURDAY", "SUNDAY"]
  tab-display:
    enabled: true
    format: "&6[YılBaşı] &e{delivery} &7({days} gün kaldı)"
```

### 2. Haftalık Hologram Yarışması
```yaml
haftalik-hologram:
  enabled: true
  hologram:
    enabled: true
    show-leaderboard: true
    max-players: 10
```

### 3. Admin Hologram Yönetimi
```bash
# Spawn'da hologram oluştur
/dc holo create spawn_leaderboard

# Tüm hologramları listele
/dc holo list

# Hologram sil
/dc holo remove spawn_leaderboard
```

## 🔄 Geriye Dönük Uyumluluk

### ✅ v1.0 Özellikleri Korundu
- Mevcut `deliveries.yml` dosyaları çalışmaya devam eder
- Eski komutlar ve GUI sistemi değişmedi
- Webhook sistemi aynı şekilde çalışır
- Tüm placeholder'lar desteklenir

### 🆕 v1.1 Özellikleri İsteğe Bağlı
- Yeni özellikler varsayılan olarak **kapalı**
- Her teslimat için ayrı ayrı açılabilir
- `season.enabled: false` - Sezonluk sistem kapalı
- `tab-display.enabled: false` - Tab list kapalı
- `hologram.enabled: false` - Hologram kapalı

## 📋 Kurulum Talimatları

### 1. Temel Kurulum
1. `DeliveryCore-v1.1.0.jar` dosyasını `plugins/` klasörüne koyun
2. Sunucuyu başlatın (konfigürasyon dosyaları otomatik oluşur)
3. `plugins/DeliveryCore/deliveries.yml` dosyasını düzenleyin

### 2. v1.1 Özelliklerini Aktifleştirme

#### Sezonluk Sistem:
```yaml
season:
  enabled: true
  start-date: "2024-06-21T00:00:00+03:00"
  end-date: "2024-09-21T23:59:59+03:00"
```

#### Tab List:
```yaml
tab-display:
  enabled: true
  format: "&6[DeliveryCore] &e{delivery} &7- &a{item}"
```

#### Hologram (İsteğe Bağlı Eklentiler):
1. **HolographicDisplays** eklentisini kurun
2. Konfigürasyonda `hologram.enabled: true` yapın
3. `/dc holo create <id>` ile hologram oluşturun

## 🎨 Özelleştirme

### Placeholder'lar (v1.1)
- `{days}` / `{days_remaining}` - Sezon bitişine kalan gün
- `{delivery}` - Teslimat adı
- `{item}` - Teslim edilecek eşya
- `{player_1}`, `{count_1}` - 1. sıradaki oyuncu ve sayısı

### Dil Desteği
- **Türkçe**: `lang/tr.yml` (varsayılan)
- **İngilizce**: `lang/en.yml`
- v1.1 mesajları her iki dilde mevcut

## 🚀 Performans ve Optimizasyon

### ⚡ Verimli Güncelleme
- Tab list: Konfigürasyondan ayarlanabilir güncelleme aralığı
- Hologram: Sadece gerektiğinde güncellenir
- Sezon kontrolü: Cache'lenmiş hesaplamalar

### 🔒 Güvenlik
- Admin komutları yetki kontrolü ile korunur
- Konfigürasyon doğrulama sistemi
- Hata durumunda güvenli geri dönüş

## 📞 Destek ve Dokümantasyon

### Wiki Sayfaları (Güncellenmiş)
- `WIKI_HOME.md` - Genel bilgiler
- `WIKI_INSTALLATION.md` - Kurulum rehberi
- `WIKI_CONFIGURATION.md` - Konfigürasyon rehberi
- `WIKI_COMMANDS.md` - Komut listesi

### Yeni v1.1 Dokümantasyonu
- Sezonluk sistem kullanımı
- Tab list konfigürasyonu
- Hologram yönetimi
- Admin komutları

---

## 🎊 Sonuç

**DeliveryCore v1.1.0** başarıyla tamamlandı! 

### ✨ Öne Çıkan Özellikler:
- 🌟 **Sezonluk Etkinlikler** - Özel tarih aralıkları
- 📋 **Tab List Entegrasyonu** - Canlı bilgi gösterimi  
- 🏆 **Hologram Sıralaması** - Görsel liderlik tablosu
- 🔄 **%100 Geriye Uyumlu** - Mevcut sunucular etkilenmez

### 🎯 Kullanıma Hazır:
- JAR dosyası: `target/DeliveryCore-v1.1.0.jar`
- Tüm konfigürasyon örnekleri hazır
- Dokümantasyon tamamlandı
- Property testler geçiyor

**Artık sunucunuzda kullanabilirsiniz!** 🚀