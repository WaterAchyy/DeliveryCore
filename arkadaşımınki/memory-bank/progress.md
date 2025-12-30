# DeliveryCore - İlerleme Durumu

## Tamamlanan Özellikler ✅

### Çekirdek Sistem
- [x] Plugin lifecycle (onEnable/onDisable)
- [x] Konfigürasyon yönetimi (config.yml, categories.yml, deliveries.yml)
- [x] Çoklu dil desteği (tr.yml, en.yml)
- [x] Komut sistemi (/dc, /teslimat, /teslim)
- [x] Yetki sistemi (deliverycore.*)
- [x] LangManager ile merkezi mesaj yönetimi

### Etkinlik Sistemi
- [x] Zamanlanmış etkinlikler
- [x] Manuel başlatma/durdurma
- [x] Rastgele kategori/eşya seçimi
- [x] Sabit kategori/eşya seçimi
- [x] Gerçek zamanlı sıralama
- [x] Etkinlik durumu kaydetme/yükleme

### Teslimat Sistemi
- [x] GUI tabanlı teslimat
- [x] Komut tabanlı teslimat (/teslim)
- [x] Sandık teslimatı
- [x] Envanter tarama

### Ödül Sistemi
- [x] Item ödülleri
- [x] Komut ödülleri
- [x] Sıralama bazlı ödüller (1., 2., 3., 4-10, default)
- [x] Offline oyuncu desteği (bekleyen ödüller)

### Entegrasyonlar
- [x] Discord webhook (başlangıç, bitiş, son dakika uyarısı)
- [x] Vault ekonomi (opsiyonel)
- [x] PlaceholderAPI entegrasyonu

### GUI Sistemi
- [x] Özelleştirilebilir GUI (gui.yml)
- [x] ItemsAdder desteği
- [x] Oraxen desteği
- [x] Nexo desteği
- [x] Custom head texture desteği (1.21 uyumlu)

### PlaceholderAPI Placeholders
- %deliverycore_active% - Aktif teslimat adı
- %deliverycore_active_item% - Aktif teslimat eşyası
- %deliverycore_active_category% - Aktif teslimat kategorisi
- %deliverycore_player_count% - Oyuncunun teslimat sayısı
- %deliverycore_player_rank% - Oyuncunun sırası
- %deliverycore_total% - Toplam teslimat sayısı
- %deliverycore_participants% - Katılımcı sayısı
- %deliverycore_top_X_name% - X. sıradaki oyuncu adı
- %deliverycore_top_X_count% - X. sıradaki teslimat sayısı
- %deliverycore_time_left% - Kalan süre

### v1.1 Özellikleri
- [x] Sezonluk sistem
- [x] CustomModelData desteği
- [x] ItemsAdder/Oraxen/Nexo entegrasyonu
- [x] Kod refactoring (Handler pattern)
- [x] gui.yml - GUI özelleştirme
- [x] webhooks/discord.yml - Ayrı webhook config
- [x] Son dakika uyarısı (warning webhook)

## Kaldırılan Özellikler ❌

### Hologram Sistemi
- Plugin içi hologram sistemi kaldırıldı
- PlaceholderAPI ile değiştirildi
- Silinen dosyalar: HologramService.java, HologramServiceImpl.java, HologramInfo.java

## Konfigürasyon Dosyaları

| Dosya | Amaç |
|-------|------|
| config.yml | Ana ayarlar, webhook URL, dil |
| categories.yml | Eşya kategorileri |
| deliveries.yml | Teslimat tanımları |
| items.yml | Eşya görünen isimleri |
| gui.yml | GUI özelleştirme |
| webhooks/discord.yml | Discord mesaj şablonları |
| lang/tr.yml | Türkçe mesajlar |
| lang/en.yml | İngilizce mesajlar |

## Yapılacaklar 📋

### Kısa Vadeli
- [ ] Derleme testi (mvn clean package)
- [ ] Sunucuda test
- [ ] Kullanıcı geri bildirimleri

### Orta Vadeli
- [ ] Daha fazla dil desteği
- [ ] MySQL/SQLite veritabanı desteği

### Uzun Vadeli
- [ ] Web panel
- [ ] API geliştirme
- [ ] Bungee/Velocity desteği

## Çözülen Sorunlar ✅

1. **Discord webhook çalışmıyor** → JSON escape, UTF-8, emoji encoding düzeltildi
2. **Oyuncu ismi UUID olarak görünüyor** → Bukkit.getPlayer/getOfflinePlayer ile çözüldü
3. **Head texture'lar Steve görünüyor** → PlayerProfile API ile 1.21 uyumlu hale getirildi

## Kod Refactoring (Aralık 2025)

- DeliveryCorePlugin.java: 1271 → ~450 satır
- Yeni handler sınıfları:
  - WebhookHandler.java
  - DeliveryHandler.java
  - EventListenerHandler.java
- Yeni config sınıfları:
  - GUIConfig.java
  - LangManager.java
- Yeni placeholder sınıfı:
  - DeliveryCorePlaceholders.java

## Sürüm Geçmişi

### v1.1.0 (Güncel - Aralık 2025)
- Minecraft 1.21 desteği
- Java 21
- Sezonluk sistem
- Custom item desteği (ItemsAdder/Oraxen/Nexo)
- GUI özelleştirme (gui.yml)
- Discord webhook geliştirmeleri (son dakika uyarısı)
- PlaceholderAPI entegrasyonu
- Hologram sistemi kaldırıldı
- Kod refactoring tamamlandı

### v1.0.0
- İlk stabil sürüm
- Temel etkinlik sistemi
- GUI ve komutlar
- Discord webhook
