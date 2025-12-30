# DeliveryCore - Sistem Desenleri

## Mimari Genel Bakış

### Handler Tabanlı Mimari
Plugin, handler sınıfları üzerine kurulu. Her handler tek bir sorumluluğa sahip.

```
DeliveryCorePlugin (Ana Sınıf - ~450 satır)
    │
    ├── ConfigManager          # Konfigürasyon yönetimi
    ├── LangManager            # Çoklu dil desteği
    │
    ├── Handler'lar
    │   ├── WebhookHandler     # Discord webhook işlemleri
    │   ├── DeliveryHandler    # Teslimat işlemleri
    │   └── EventListenerHandler # Bukkit event'leri
    │
    ├── Servisler
    │   ├── CategoryService    # Kategori işlemleri
    │   ├── DeliveryService    # Teslimat iş mantığı
    │   ├── SchedulerService   # Zamanlama
    │   ├── RewardService      # Ödül dağıtımı
    │   ├── SeasonService      # Sezonluk sistem
    │   └── CustomItemService  # Özel item desteği
    │
    ├── GUI
    │   ├── DeliveryGUI        # Ana GUI sınıfı
    │   └── GUIConfig          # gui.yml okuyucu
    │
    └── Placeholder
        └── DeliveryCorePlaceholders # PlaceholderAPI expansion
```

## Temel Tasarım Desenleri

### 1. Handler Pattern
İş mantığı handler sınıflarına ayrıldı:
```java
// WebhookHandler - Discord işlemleri
public class WebhookHandler {
    public void sendStartWebhook(...)
    public void sendEndWebhook(...)
    public void sendWarningWebhook(...)
    public void scheduleWarning(...)
}

// DeliveryHandler - Teslimat işlemleri
public class DeliveryHandler {
    public int deliverFromInventory(...)
    public int deliverFromChest(...)
}
```

### 2. Config-Driven Design
Tüm ayarlar YAML dosyalarından okunur:
```
config.yml      → Ana ayarlar
gui.yml         → GUI özelleştirme
discord.yml     → Webhook mesajları
lang/*.yml      → Dil mesajları
```

### 3. Callback Pattern
Servisler arası iletişim callback'ler ile:
```java
schedulerImpl.setEventStartCallback(this::handleEventStart);
schedulerImpl.setEventEndCallback(this::handleEventEnd);
```

### 4. Optional Pattern
Null güvenliği için Optional kullanımı:
```java
Optional<ActiveEvent> event = deliveryService.getActiveEvent(name);
event.ifPresent(e -> { /* işlem */ });
```

## Kritik Akışlar

### Etkinlik Başlatma
```
1. SchedulerService → callback tetikler
2. DeliveryCorePlugin.handleEventStart()
3. DeliveryService.startEvent()
4. Broadcast + Title + Ses
5. WebhookHandler.sendStartWebhook()
6. WebhookHandler.scheduleWarning() → Son dakika uyarısı zamanla
```

### Teslimat İşlemi
```
1. Oyuncu GUI'den veya komutla teslim eder
2. DeliveryGUI/DeliverCommand → DeliveryHandler
3. DeliveryHandler.deliverFromInventory/Chest()
4. DeliveryService.recordDelivery()
5. Oyuncuya geri bildirim
```

### Etkinlik Bitirme
```
1. SchedulerService → callback tetikler
2. DeliveryCorePlugin.handleEventEnd()
3. DeliveryService.endEvent()
4. RewardService.distributeRewards()
5. WebhookHandler.sendEndWebhook()
```

## GUI Sistemi

### GUIConfig
gui.yml dosyasını okur ve GUI'yi özelleştirir:
- Slot pozisyonları
- Head texture'ları
- İsim ve lore'lar
- ItemsAdder/Oraxen/Nexo desteği

```yaml
# gui.yml örnek
main-menu:
  items:
    leaderboard-button:
      slot: 47
      material: "itemsadder:mypack:trophy"
      name: "§bSıralama"
```

## Webhook Sistemi

### Ayrı Config Dosyası
```
config.yml          → webhook.enabled, webhook.url
webhooks/discord.yml → Mesaj şablonları
```

### Son Dakika Uyarısı
```java
// Etkinlik başlayınca warning zamanlanır
webhookHandler.scheduleWarning(event, deliveryService, ...);

// X dakika kala Discord'a bildirim
warning:
  enabled: true
  minutes-before: 5
  title: "⏰ {delivery} Bitiyor!"
```

## Thread Güvenliği
- Ana thread: Bukkit API çağrıları
- Async: Webhook gönderimi
- BukkitScheduler: Warning task'ları
