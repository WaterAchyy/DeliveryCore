# DeliveryCore - Aktif Bağlam

## Mevcut Durum
- **Versiyon**: 1.1.0
- **Minecraft**: 1.21.x
- **Java**: 21
- **Durum**: Aktif geliştirme
- **Son Güncelleme**: Aralık 2025

## Son Yapılan Değişiklikler (Aralık 2025)

### 1. Spigot API 1.21 Güncelleme
- pom.xml'de spigot-api 1.21-R0.1-SNAPSHOT'a güncellendi
- Java 21 kullanılıyor

### 2. Head Texture Fix (1.21 uyumlu)
- Eski GameProfile reflection kaldırıldı
- Yeni Bukkit PlayerProfile API kullanılıyor
- `DeliveryGUI.applyTexture()` tamamen yeniden yazıldı

### 3. GUI Özelleştirme Sistemi (YENİ)
- `gui.yml` dosyası eklendi
- `GUIConfig.java` sınıfı oluşturuldu
- Tüm GUI itemleri özelleştirilebilir
- ItemsAdder/Oraxen/Nexo desteği eklendi

```yaml
# gui.yml örnek kullanım
main-menu:
  items:
    leaderboard-button:
      slot: 47
      material: "itemsadder:namespace:item_id"
      # veya: "oraxen:item_id"
      # veya: "nexo:item_id"
      name: "§bSıralama"
```

### 4. Discord Webhook Geliştirmeleri
- `webhooks/discord.yml` dosyası eklendi
- Son dakika uyarısı özelliği eklendi
- config.yml'de sadece `webhook.enabled` ve `webhook.url` kaldı

```yaml
# webhooks/discord.yml
warning:
  enabled: true
  minutes-before: 5
  title: "⏰ {delivery} Bitiyor!"
```

### 5. PlaceholderAPI Entegrasyonu
- `DeliveryCorePlaceholders.java` oluşturuldu
- 10+ placeholder eklendi
- Hologram sistemi kaldırıldı (PlaceholderAPI ile değiştirildi)

### 6. Kod Refactoring
- DeliveryCorePlugin.java: 1271 → ~450 satır
- Handler sınıfları: WebhookHandler, DeliveryHandler, EventListenerHandler
- LangManager ile merkezi mesaj yönetimi

## Dosya Yapısı Değişiklikleri

### Yeni Dosyalar
- `src/main/resources/gui.yml`
- `src/main/resources/webhooks/discord.yml`
- `src/main/java/com/deliverycore/gui/GUIConfig.java`
- `src/main/java/com/deliverycore/placeholder/DeliveryCorePlaceholders.java`
- `src/main/java/com/deliverycore/handler/WebhookHandler.java`
- `src/main/java/com/deliverycore/handler/DeliveryHandler.java`
- `src/main/java/com/deliverycore/handler/EventListenerHandler.java`
- `src/main/java/com/deliverycore/util/LangManager.java`

### Silinen Dosyalar
- HologramService.java
- HologramServiceImpl.java
- HologramInfo.java

## Bilinen Sorunlar
- Yok (tüm bilinen sorunlar çözüldü)

## Sonraki Adımlar
1. Derleme testi (mvn clean package)
2. Sunucuda test
3. Kullanıcı geri bildirimi bekle

## Önemli Notlar

### Webhook Kullanımı
1. `config.yml`'de `webhook.enabled: true` yap
2. `webhook.url: https://discord.com/api/webhooks/...` ekle
3. `webhooks/discord.yml`'de mesajları özelleştir

### GUI Özelleştirme
1. `gui.yml` dosyasını düzenle
2. ItemsAdder için: `material: "itemsadder:namespace:item_id"`
3. Oraxen için: `material: "oraxen:item_id"`
4. Nexo için: `material: "nexo:item_id"`

### PlaceholderAPI
- Otomatik kayıt (PlaceholderAPI yüklüyse)
- Prefix: `%deliverycore_...%`
