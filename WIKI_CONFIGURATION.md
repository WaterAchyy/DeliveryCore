# Configuration Guide | Yapılandırma Rehberi

This guide covers the main configuration file (`config.yml`) and its options.

Bu rehber ana yapılandırma dosyasını (`config.yml`) ve seçeneklerini kapsar.

## config.yml Structure | config.yml Yapısı

```yaml
general:
  language: tr
  timezone: Europe/Istanbul
  debug: false

category-display-names:
  farm: "Çiftlik"
  ore: "Maden"
  # ... more categories

delivery-display-names:
  gunluk: "Günlük Teslimat"
  # ... more deliveries

events:
  max-concurrent: 5
  announcements:
    start: true
    end: true
  sounds:
    enabled: true
  title:
    enabled: true

webhook:
  enabled: true
  url: "YOUR_WEBHOOK_URL_HERE"
  # ... webhook settings

data:
  auto-save-interval: 5
  file: "data.yml"
```

## General Settings | Genel Ayarlar

### Language | Dil
```yaml
general:
  language: tr  # tr (Turkish) or en (English)
```

**Options | Seçenekler:**
- `tr` - Turkish interface | Türkçe arayüz
- `en` - English interface | İngilizce arayüz

### Timezone | Saat Dilimi
```yaml
general:
  timezone: Europe/Istanbul
```

**Common Timezones | Yaygın Saat Dilimleri:**
- `Europe/Istanbul` - Turkey | Türkiye
- `Europe/London` - UK | İngiltere
- `America/New_York` - Eastern US | Doğu ABD
- `America/Los_Angeles` - Pacific US | Pasifik ABD

### Debug Mode | Hata Ayıklama Modu
```yaml
general:
  debug: false  # true for detailed console logs
```

## Display Names | Görünen İsimler

### Category Display Names | Kategori Görünen İsimleri
```yaml
category-display-names:
  farm: "Çiftlik"
  ore: "Maden"
  block: "Blok"
  food: "Yiyecek"
  wood: "Odun"
  rare: "Nadir"
  nether: "Nether"
  end: "End"
  mob: "Mob Drop"
  dye: "Boya"
  tool: "Alet"
  armor: "Zırh"
  combat: "Savaş"
  potion: "İksir"
  decoration: "Dekorasyon"
  redstone: "Kızıltaş"
  misc: "Çeşitli"
```

### Delivery Display Names | Teslimat Görünen İsimleri
```yaml
delivery-display-names:
  gunluk: "Günlük Teslimat"
  maden: "Maden Teslimatı"
  blok: "Blok Teslimatı"
  elmas: "Elmas Teslimatı"
  ciftlik: "Çiftlik Teslimatı"
  buyuk-etkinlik: "Büyük Etkinlik"
```

## Event Settings | Etkinlik Ayarları

### Concurrent Events | Eş Zamanlı Etkinlikler
```yaml
events:
  max-concurrent: 5  # Maximum active events at once
```

### Announcements | Duyurular
```yaml
events:
  announcements:
    start: true  # Announce when events start
    end: true    # Announce when events end
```

### Sounds | Sesler
```yaml
events:
  sounds:
    enabled: true
    start: "ENTITY_PLAYER_LEVELUP"
    end: "UI_TOAST_CHALLENGE_COMPLETE"
```

**Available Sounds | Mevcut Sesler:**
- `ENTITY_PLAYER_LEVELUP` - Level up sound
- `UI_TOAST_CHALLENGE_COMPLETE` - Achievement sound
- `BLOCK_NOTE_BLOCK_PLING` - Note block sound
- `ENTITY_EXPERIENCE_ORB_PICKUP` - XP pickup sound

### Title Messages | Başlık Mesajları
```yaml
events:
  title:
    enabled: true
    fade-in: 10    # Fade in time (ticks)
    stay: 70       # Stay time (ticks)
    fade-out: 20   # Fade out time (ticks)
```

## Discord Webhook | Discord Webhook

### Basic Setup | Temel Kurulum
```yaml
webhook:
  enabled: true
  url: "YOUR_WEBHOOK_URL_HERE"
  mention-everyone: false
```

### Start Event Embed | Başlangıç Etkinlik Embed'i
```yaml
webhook:
  start:
    title: "{delivery} Başladı!"
    description: "**Teslim Edilecek:** {item}\\n**Kategori:** {category}"
    color: "#00FF00"
    footer: "DeliveryCore"
```

### End Event Embed | Bitiş Etkinlik Embed'i
```yaml
webhook:
  end:
    title: "{delivery} Sona Erdi!"
    description: "**Teslim Edilen:** {item}\\n**Katılımcı:** {participants}"
    color: "#FFD700"
    footer: "DeliveryCore"
```

### Available Placeholders | Mevcut Placeholder'lar
- `{delivery}` - Delivery name | Teslimat adı
- `{item}` - Required item | İstenen eşya
- `{category}` - Category name | Kategori adı
- `{duration}` - Event duration | Etkinlik süresi
- `{winners}` - Number of winners | Kazanan sayısı
- `{participants}` - Number of participants | Katılımcı sayısı
- `{total}` - Total deliveries | Toplam teslimat

## Data Settings | Veri Ayarları

```yaml
data:
  auto-save-interval: 5  # Auto-save interval in minutes
  file: "data.yml"       # Data file name
```

## Color Codes | Renk Kodları

For webhook embed colors | Webhook embed renkleri için:

```yaml
color: "#FF0000"  # Red | Kırmızı
color: "#00FF00"  # Green | Yeşil
color: "#0000FF"  # Blue | Mavi
color: "#FFFF00"  # Yellow | Sarı
color: "#FF00FF"  # Magenta | Magenta
color: "#00FFFF"  # Cyan | Camgöbeği
color: "#FFA500"  # Orange | Turuncu
color: "#800080"  # Purple | Mor
```

## Validation | Doğrulama

After editing the configuration:

Yapılandırmayı düzenledikten sonra:

1. **Save the file** | **Dosyayı kaydedin**
2. **Run `/dc reload`** | **`/dc reload` çalıştırın**
3. **Check for errors** in console | Konsolda **hataları kontrol edin**

## Common Issues | Yaygın Sorunlar

**Configuration not loading:**
- Check YAML syntax (indentation matters!)
- Verify file encoding (UTF-8)
- Look for syntax errors in console

**Webhook not working:**
- Verify webhook URL is correct
- Check Discord server permissions
- Test webhook with `/dc webhook test`

**Language not changing:**
- Restart server after language change
- Check if language files exist
- Verify language code is correct (tr/en)

## Next Steps | Sonraki Adımlar

- [Categories Configuration](Categories)
- [Deliveries Setup](Deliveries)
- [Discord Webhook Setup](Discord-Webhooks)
- [Language Customization](Language-Files)