# Scheduling System | Zamanlama Sistemi

## English

The DeliveryCore scheduling system allows you to create automated delivery events using natural language expressions.

### Schedule Format

DeliveryCore uses a flexible scheduling format that supports:

- **Time-based**: `at 18:00`, `at 14:30`
- **Interval-based**: `every 2 hours`, `every 30 minutes`
- **Daily**: `every day at 20:00`
- **Weekly**: `every monday at 19:00`
- **Complex**: `every 3 hours from 09:00 to 21:00`

### Basic Examples

```yaml
# Every day at 6 PM
schedule: "every day at 18:00"

# Every 2 hours
schedule: "every 2 hours"

# Every Monday at 7 PM
schedule: "every monday at 19:00"

# Every 30 minutes during day
schedule: "every 30 minutes from 08:00 to 22:00"
```

### Advanced Scheduling

#### Multiple Times Per Day
```yaml
# Three times daily
schedule: "at 10:00, 16:00, 22:00"

# Every 4 hours starting at 8 AM
schedule: "every 4 hours from 08:00"
```

#### Weekday Scheduling
```yaml
# Weekdays only
schedule: "every weekday at 18:00"

# Weekends only
schedule: "every weekend at 15:00"

# Specific days
schedule: "every monday, wednesday, friday at 20:00"
```

#### Time Ranges
```yaml
# Only during evening hours
schedule: "every hour from 18:00 to 23:00"

# Business hours only
schedule: "every 2 hours from 09:00 to 17:00"
```

### Schedule Keywords

#### Time Units
- `minute`, `minutes`, `min`
- `hour`, `hours`, `h`
- `day`, `days`
- `week`, `weeks`

#### Days of Week
- `monday`, `tuesday`, `wednesday`, `thursday`, `friday`, `saturday`, `sunday`
- `weekday` (Monday-Friday)
- `weekend` (Saturday-Sunday)

#### Special Keywords
- `every` - Repeating interval
- `at` - Specific time
- `from` - Start time
- `to` - End time

### Configuration Example

```yaml
deliveries:
  daily-farm:
    name: "Daily Farm Event"
    schedule: "every day at 18:00"
    duration: "2h"
    category: "farm"
    winners: 3
    
  weekend-special:
    name: "Weekend Special"
    schedule: "every saturday, sunday at 15:00"
    duration: "3h"
    category: "rare"
    winners: 5
    
  hourly-quick:
    name: "Quick Delivery"
    schedule: "every hour from 10:00 to 22:00"
    duration: "30m"
    category: "block"
    winners: 1
```

### Timezone Support

All schedules use the timezone configured in `config.yml`:

```yaml
general:
  timezone: "Europe/Istanbul"
```

Common timezones:
- `Europe/Istanbul` - Turkey
- `Europe/London` - UK
- `America/New_York` - Eastern US
- `America/Los_Angeles` - Pacific US

### Manual Override

Admins can start events manually regardless of schedule:

```bash
/dc start daily-farm
/dc start daily-farm 1h30m
/dc start daily-farm 1h30m 5
```

### Troubleshooting

**Schedule not working:**
- Check timezone configuration
- Verify schedule syntax
- Use `/dc reload` after changes
- Check console for parsing errors

**Events starting at wrong time:**
- Verify server timezone
- Check system clock
- Confirm timezone in config.yml

---

## Türkçe

DeliveryCore zamanlama sistemi, doğal dil ifadeleri kullanarak otomatik teslimat etkinlikleri oluşturmanıza olanak tanır.

### Zamanlama Formatı

DeliveryCore esnek bir zamanlama formatı kullanır:

- **Saat bazlı**: `at 18:00`, `at 14:30`
- **Aralık bazlı**: `every 2 hours`, `every 30 minutes`
- **Günlük**: `every day at 20:00`
- **Haftalık**: `every monday at 19:00`
- **Karmaşık**: `every 3 hours from 09:00 to 21:00`

### Temel Örnekler

```yaml
# Her gün saat 18:00'de
schedule: "every day at 18:00"

# Her 2 saatte bir
schedule: "every 2 hours"

# Her Pazartesi saat 19:00'da
schedule: "every monday at 19:00"

# Gün içinde her 30 dakikada
schedule: "every 30 minutes from 08:00 to 22:00"
```

### Gelişmiş Zamanlama

#### Günde Birden Fazla Saat
```yaml
# Günde üç kez
schedule: "at 10:00, 16:00, 22:00"

# Sabah 8'den başlayarak her 4 saatte
schedule: "every 4 hours from 08:00"
```

#### Hafta İçi Zamanlaması
```yaml
# Sadece hafta içi
schedule: "every weekday at 18:00"

# Sadece hafta sonu
schedule: "every weekend at 15:00"

# Belirli günler
schedule: "every monday, wednesday, friday at 20:00"
```

#### Saat Aralıkları
```yaml
# Sadece akşam saatlerinde
schedule: "every hour from 18:00 to 23:00"

# Sadece iş saatlerinde
schedule: "every 2 hours from 09:00 to 17:00"
```

### Zamanlama Anahtar Kelimeleri

#### Zaman Birimleri
- `minute`, `minutes`, `min` - dakika
- `hour`, `hours`, `h` - saat
- `day`, `days` - gün
- `week`, `weeks` - hafta

#### Haftanın Günleri
- `monday` - Pazartesi
- `tuesday` - Salı
- `wednesday` - Çarşamba
- `thursday` - Perşembe
- `friday` - Cuma
- `saturday` - Cumartesi
- `sunday` - Pazar
- `weekday` - Hafta içi (Pazartesi-Cuma)
- `weekend` - Hafta sonu (Cumartesi-Pazar)

#### Özel Anahtar Kelimeler
- `every` - Tekrarlayan aralık
- `at` - Belirli saat
- `from` - Başlangıç saati
- `to` - Bitiş saati

### Yapılandırma Örneği

```yaml
deliveries:
  gunluk-ciftlik:
    name: "Günlük Çiftlik Etkinliği"
    schedule: "every day at 18:00"
    duration: "2h"
    category: "farm"
    winners: 3
    
  haftasonu-ozel:
    name: "Hafta Sonu Özel"
    schedule: "every saturday, sunday at 15:00"
    duration: "3h"
    category: "rare"
    winners: 5
    
  saatlik-hizli:
    name: "Hızlı Teslimat"
    schedule: "every hour from 10:00 to 22:00"
    duration: "30m"
    category: "block"
    winners: 1
```

### Saat Dilimi Desteği

Tüm zamanlamalar `config.yml` dosyasında yapılandırılan saat dilimini kullanır:

```yaml
general:
  timezone: "Europe/Istanbul"
```

Yaygın saat dilimleri:
- `Europe/Istanbul` - Türkiye
- `Europe/London` - İngiltere
- `America/New_York` - Doğu ABD
- `America/Los_Angeles` - Batı ABD

### Manuel Geçersiz Kılma

Yöneticiler zamanlamadan bağımsız olarak etkinlikleri manuel başlatabilir:

```bash
/dc start gunluk-ciftlik
/dc start gunluk-ciftlik 1h30m
/dc start gunluk-ciftlik 1h30m 5
```

### Sorun Giderme

**Zamanlama çalışmıyor:**
- Saat dilimi yapılandırmasını kontrol edin
- Zamanlama sözdizimini doğrulayın
- Değişikliklerden sonra `/dc reload` kullanın
- Ayrıştırma hataları için konsolu kontrol edin

**Etkinlikler yanlış saatte başlıyor:**
- Sunucu saat dilimini doğrulayın
- Sistem saatini kontrol edin
- config.yml'deki saat dilimini onaylayın