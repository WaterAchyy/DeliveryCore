# DeliveryCore - Teknik Bağlam

## Teknoloji Stack

### Dil & Platform
- **Java 21** (maven.compiler.source/target)
- **Spigot API 1.21-R0.1-SNAPSHOT**
- **Maven** build sistemi

### Bağımlılıklar

#### Runtime (provided)
```xml
- org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT
- me.clip:placeholderapi:2.11.6
```

#### Build Plugins
- maven-compiler-plugin 3.12.1
- maven-shade-plugin 3.5.2 (Fat JAR)

## Proje Yapısı

```
deliverycore/
├── src/main/java/com/deliverycore/
│   ├── DeliveryCorePlugin.java    # Ana plugin sınıfı (~450 satır)
│   ├── command/                    # Komut işleyicileri
│   │   ├── CommandHandler.java
│   │   ├── DeliverCommand.java
│   │   └── CustomItemCommand.java
│   ├── config/                     # Konfigürasyon yönetimi
│   ├── gui/                        # GUI sistemleri
│   │   ├── DeliveryGUI.java
│   │   └── GUIConfig.java          # YENİ - gui.yml okuyucu
│   ├── handler/                    # İş mantığı handler'ları
│   │   ├── WebhookHandler.java
│   │   ├── DeliveryHandler.java
│   │   └── EventListenerHandler.java
│   ├── model/                      # Veri modelleri
│   ├── placeholder/                # PlaceholderAPI
│   │   ├── PlaceholderEngine.java
│   │   └── DeliveryCorePlaceholders.java
│   ├── reward/                     # Ödül sistemi
│   ├── service/                    # İş mantığı servisleri
│   └── util/                       # Yardımcı sınıflar
│       └── LangManager.java
├── src/main/resources/
│   ├── plugin.yml                  # Plugin tanımı
│   ├── config.yml                  # Ana konfigürasyon
│   ├── categories.yml              # Eşya kategorileri
│   ├── deliveries.yml              # Teslimat tanımları
│   ├── items.yml                   # Eşya isimleri
│   ├── gui.yml                     # YENİ - GUI özelleştirme
│   ├── webhooks/
│   │   └── discord.yml             # YENİ - Discord mesaj ayarları
│   └── lang/                       # Dil dosyaları
│       ├── tr.yml
│       └── en.yml
└── pom.xml
```

## Build & Deploy

### Build Komutu
```bash
mvn clean package
```

### Çıktı
```
target/DeliveryCore-1.1.0.jar
```

### Kurulum
1. JAR'ı `plugins/` klasörüne kopyala
2. Sunucuyu yeniden başlat
3. `plugins/DeliveryCore/` klasöründe config düzenle

## Konfigürasyon Dosyaları

| Dosya | Amaç |
|-------|------|
| config.yml | Ana ayarlar, webhook URL, dil seçimi |
| categories.yml | Eşya kategorileri tanımları |
| deliveries.yml | Teslimat etkinlikleri tanımları |
| items.yml | Eşya görünen isimleri |
| gui.yml | GUI özelleştirme (slot, texture, isim) |
| webhooks/discord.yml | Discord mesaj şablonları |
| lang/tr.yml | Türkçe mesajlar |
| lang/en.yml | İngilizce mesajlar |

## Teknik Kısıtlamalar
- Minecraft 1.18.1+ (PlayerProfile API için)
- Java 21 runtime
- Senkron Bukkit API kullanımı (ana thread)
- Async webhook gönderimi
