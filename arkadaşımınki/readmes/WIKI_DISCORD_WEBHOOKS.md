# Discord Webhooks | Discord Webhook'ları

## English

DeliveryCore integrates seamlessly with Discord through webhooks, providing real-time notifications about delivery events.

### Setting Up Discord Webhook

#### Step 1: Create Webhook in Discord

1. Go to your Discord server
2. Right-click on the channel where you want notifications
3. Select **Edit Channel**
4. Go to **Integrations** tab
5. Click **Create Webhook**
6. Give it a name (e.g., "DeliveryCore")
7. Copy the **Webhook URL**

#### Step 2: Configure in DeliveryCore

Edit `config.yml`:

```yaml
webhook:
  enabled: true
  url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL_HERE"
  mention-everyone: false
  mention-role: ""  # Role ID to mention (optional)
```

### Webhook Features

#### Event Start Notifications

When a delivery event starts, Discord receives:

```json
{
  "embeds": [{
    "title": "📦 Daily Farm Event Started!",
    "description": "**🎯 Required Item:** Wheat x64\n**📁 Category:** Farm\n**⏰ Duration:** 2 hours\n**🏆 Winners:** 3 players",
    "color": 65280,
    "footer": {
      "text": "DeliveryCore"
    },
    "timestamp": "2024-01-15T18:00:00Z"
  }]
}
```

#### Event End Notifications

When a delivery event ends:

```json
{
  "embeds": [{
    "title": "🏆 Daily Farm Event Ended!",
    "description": "**📦 Item Delivered:** Wheat x64\n**👥 Participants:** 15 players\n**🏆 Winners:**\n🥇 PlayerName - 256 items\n🥈 Player2 - 192 items\n🥉 Player3 - 128 items",
    "color": 16766720,
    "footer": {
      "text": "DeliveryCore"
    },
    "timestamp": "2024-01-15T20:00:00Z"
  }]
}
```

#### Leaderboard Updates

Periodic leaderboard updates:

```json
{
  "embeds": [{
    "title": "📊 Weekly Leaderboard",
    "description": "🥇 PlayerName - 1,234 deliveries\n🥈 Player2 - 987 deliveries\n🥉 Player3 - 756 deliveries",
    "color": 3447003,
    "footer": {
      "text": "DeliveryCore • Weekly Stats"
    }
  }]
}
```

### Webhook Configuration Options

#### Basic Settings

```yaml
webhook:
  enabled: true
  url: "YOUR_WEBHOOK_URL"
  mention-everyone: false
  mention-role: "123456789012345678"  # Discord role ID
  username: "DeliveryCore"
  avatar-url: "https://example.com/avatar.png"
```

#### Event Start Embed

```yaml
webhook:
  start:
    enabled: true
    title: "📦 {delivery} Started!"
    description: "**🎯 Required Item:** {item}\n**📁 Category:** {category}\n**⏰ Duration:** {duration}\n**🏆 Winners:** {winners}"
    color: "#00FF00"  # Green
    footer: "DeliveryCore"
    thumbnail: true  # Show item thumbnail
```

#### Event End Embed

```yaml
webhook:
  end:
    enabled: true
    title: "🏆 {delivery} Ended!"
    description: "**📦 Item Delivered:** {item}\n**👥 Participants:** {participants}\n**🏆 Winners:**\n{leaderboard}"
    color: "#FFD700"  # Gold
    footer: "DeliveryCore"
    show-leaderboard: true
    max-leaderboard: 5
```

#### Leaderboard Embed

```yaml
webhook:
  leaderboard:
    enabled: true
    schedule: "every day at 00:00"  # Daily at midnight
    title: "📊 Daily Leaderboard"
    description: "{leaderboard}"
    color: "#3498DB"  # Blue
    footer: "DeliveryCore • Daily Stats"
    max-players: 10
```

### Available Placeholders

Use these placeholders in webhook messages:

#### Event Placeholders
- `{delivery}` - Delivery event name
- `{item}` - Required item name and amount
- `{category}` - Category display name
- `{duration}` - Event duration (formatted)
- `{winners}` - Number of winners
- `{participants}` - Number of participants
- `{server}` - Server name
- `{time}` - Current time

#### Leaderboard Placeholders
- `{leaderboard}` - Formatted leaderboard list
- `{top1}`, `{top2}`, `{top3}` - Individual top players
- `{total-deliveries}` - Total deliveries made
- `{active-events}` - Number of active events

### Webhook Commands

Test and manage webhooks with admin commands:

```bash
# Test webhook connection
/dc webhook test

# Send test message
/dc webhook test "Hello from DeliveryCore!"

# Reload webhook configuration
/dc webhook reload

# Check webhook status
/dc webhook status
```

### Advanced Features

#### Role Mentions

Mention specific roles when events start:

```yaml
webhook:
  mention-role: "123456789012345678"  # Role ID
  start:
    description: "<@&123456789012345678> New delivery event started!\n**Item:** {item}"
```

#### Custom Avatars

Set custom avatar for webhook messages:

```yaml
webhook:
  username: "DeliveryBot"
  avatar-url: "https://example.com/deliverybot-avatar.png"
```

#### Conditional Webhooks

Send webhooks only for specific events:

```yaml
deliveries:
  special-event:
    webhook:
      enabled: true
      override-settings: true
      title: "🎉 Special Event Alert!"
      color: "#FF0000"
```

### Troubleshooting

#### Common Issues

**Webhook not sending:**
- Verify webhook URL is correct and active
- Check Discord channel permissions
- Ensure webhook is enabled in config
- Test with `/dc webhook test`

**Messages not formatted correctly:**
- Check JSON syntax in webhook configuration
- Verify placeholder names are correct
- Test embed formatting with Discord webhook tester

**Rate limiting:**
- Discord limits webhooks to 30 requests per minute
- DeliveryCore automatically handles rate limiting
- Reduce frequency of leaderboard updates if needed

#### Error Messages

**"Invalid webhook URL":**
- Check URL format: `https://discord.com/api/webhooks/ID/TOKEN`
- Ensure webhook wasn't deleted in Discord

**"Webhook rate limited":**
- Wait for rate limit to reset (usually 1 minute)
- Reduce webhook frequency in configuration

**"Embed too large":**
- Reduce leaderboard size (`max-leaderboard`)
- Shorten description text
- Remove unnecessary fields

### Security

#### Best Practices

- Keep webhook URLs private and secure
- Regularly rotate webhook URLs
- Use role-specific channels for sensitive information
- Monitor webhook usage for abuse

#### URL Protection

Never share webhook URLs publicly. If compromised:

1. Delete the old webhook in Discord
2. Create a new webhook
3. Update configuration with new URL
4. Restart server or reload config

---

## Türkçe

DeliveryCore, webhook'lar aracılığıyla Discord ile sorunsuz bir şekilde entegre olur ve teslimat etkinlikleri hakkında gerçek zamanlı bildirimler sağlar.

### Discord Webhook Kurulumu

#### Adım 1: Discord'da Webhook Oluşturma

1. Discord sunucunuza gidin
2. Bildirim almak istediğiniz kanala sağ tıklayın
3. **Kanalı Düzenle**'yi seçin
4. **Entegrasyonlar** sekmesine gidin
5. **Webhook Oluştur**'a tıklayın
6. Bir isim verin (örn. "DeliveryCore")
7. **Webhook URL**'sini kopyalayın

#### Adım 2: DeliveryCore'da Yapılandırma

`config.yml` dosyasını düzenleyin:

```yaml
webhook:
  enabled: true
  url: "https://discord.com/api/webhooks/WEBHOOK_URL_BURAYA"
  mention-everyone: false
  mention-role: ""  # Bahsedilecek rol ID'si (isteğe bağlı)
```

### Webhook Özellikleri

#### Etkinlik Başlangıç Bildirimleri

Bir teslimat etkinliği başladığında Discord şunu alır:

```json
{
  "embeds": [{
    "title": "📦 Günlük Çiftlik Etkinliği Başladı!",
    "description": "**🎯 İstenen Eşya:** Buğday x64\n**📁 Kategori:** Çiftlik\n**⏰ Süre:** 2 saat\n**🏆 Kazananlar:** 3 oyuncu",
    "color": 65280,
    "footer": {
      "text": "DeliveryCore"
    },
    "timestamp": "2024-01-15T18:00:00Z"
  }]
}
```

#### Etkinlik Bitiş Bildirimleri

Bir teslimat etkinliği bittiğinde:

```json
{
  "embeds": [{
    "title": "🏆 Günlük Çiftlik Etkinliği Sona Erdi!",
    "description": "**📦 Teslim Edilen Eşya:** Buğday x64\n**👥 Katılımcılar:** 15 oyuncu\n**🏆 Kazananlar:**\n🥇 PlayerName - 256 eşya\n🥈 Player2 - 192 eşya\n🥉 Player3 - 128 eşya",
    "color": 16766720,
    "footer": {
      "text": "DeliveryCore"
    },
    "timestamp": "2024-01-15T20:00:00Z"
  }]
}
```

#### Sıralama Güncellemeleri

Periyodik sıralama güncellemeleri:

```json
{
  "embeds": [{
    "title": "📊 Haftalık Sıralama",
    "description": "🥇 PlayerName - 1,234 teslimat\n🥈 Player2 - 987 teslimat\n🥉 Player3 - 756 teslimat",
    "color": 3447003,
    "footer": {
      "text": "DeliveryCore • Haftalık İstatistikler"
    }
  }]
}
```

### Webhook Yapılandırma Seçenekleri

#### Temel Ayarlar

```yaml
webhook:
  enabled: true
  url: "WEBHOOK_URL_BURAYA"
  mention-everyone: false
  mention-role: "123456789012345678"  # Discord rol ID'si
  username: "DeliveryCore"
  avatar-url: "https://example.com/avatar.png"
```

#### Etkinlik Başlangıç Embed'i

```yaml
webhook:
  start:
    enabled: true
    title: "📦 {delivery} Başladı!"
    description: "**🎯 İstenen Eşya:** {item}\n**📁 Kategori:** {category}\n**⏰ Süre:** {duration}\n**🏆 Kazananlar:** {winners}"
    color: "#00FF00"  # Yeşil
    footer: "DeliveryCore"
    thumbnail: true  # Eşya küçük resmi göster
```

#### Etkinlik Bitiş Embed'i

```yaml
webhook:
  end:
    enabled: true
    title: "🏆 {delivery} Sona Erdi!"
    description: "**📦 Teslim Edilen Eşya:** {item}\n**👥 Katılımcılar:** {participants}\n**🏆 Kazananlar:**\n{leaderboard}"
    color: "#FFD700"  # Altın
    footer: "DeliveryCore"
    show-leaderboard: true
    max-leaderboard: 5
```

#### Sıralama Embed'i

```yaml
webhook:
  leaderboard:
    enabled: true
    schedule: "every day at 00:00"  # Her gün gece yarısı
    title: "📊 Günlük Sıralama"
    description: "{leaderboard}"
    color: "#3498DB"  # Mavi
    footer: "DeliveryCore • Günlük İstatistikler"
    max-players: 10
```

### Mevcut Placeholder'lar

Webhook mesajlarında bu placeholder'ları kullanın:

#### Etkinlik Placeholder'ları
- `{delivery}` - Teslimat etkinliği adı
- `{item}` - İstenen eşya adı ve miktarı
- `{category}` - Kategori görünen adı
- `{duration}` - Etkinlik süresi (formatlanmış)
- `{winners}` - Kazanan sayısı
- `{participants}` - Katılımcı sayısı
- `{server}` - Sunucu adı
- `{time}` - Şu anki zaman

#### Sıralama Placeholder'ları
- `{leaderboard}` - Formatlanmış sıralama listesi
- `{top1}`, `{top2}`, `{top3}` - Bireysel en iyi oyuncular
- `{total-deliveries}` - Yapılan toplam teslimat
- `{active-events}` - Aktif etkinlik sayısı

### Webhook Komutları

Webhook'ları test edin ve yönetin:

```bash
# Webhook bağlantısını test et
/dc webhook test

# Test mesajı gönder
/dc webhook test "DeliveryCore'dan merhaba!"

# Webhook yapılandırmasını yeniden yükle
/dc webhook reload

# Webhook durumunu kontrol et
/dc webhook status
```

### Gelişmiş Özellikler

#### Rol Bahsetmeleri

Etkinlikler başladığında belirli rolleri bahsedin:

```yaml
webhook:
  mention-role: "123456789012345678"  # Rol ID'si
  start:
    description: "<@&123456789012345678> Yeni teslimat etkinliği başladı!\n**Eşya:** {item}"
```

#### Özel Avatar'lar

Webhook mesajları için özel avatar ayarlayın:

```yaml
webhook:
  username: "TeslimatBot"
  avatar-url: "https://example.com/teslimatbot-avatar.png"
```

#### Koşullu Webhook'lar

Sadece belirli etkinlikler için webhook gönder:

```yaml
deliveries:
  ozel-etkinlik:
    webhook:
      enabled: true
      override-settings: true
      title: "🎉 Özel Etkinlik Uyarısı!"
      color: "#FF0000"
```

### Sorun Giderme

#### Yaygın Sorunlar

**Webhook gönderilmiyor:**
- Webhook URL'sinin doğru ve aktif olduğunu doğrulayın
- Discord kanal izinlerini kontrol edin
- Webhook'un yapılandırmada etkin olduğundan emin olun
- `/dc webhook test` ile test edin

**Mesajlar doğru formatlanmıyor:**
- Webhook yapılandırmasında JSON sözdizimini kontrol edin
- Placeholder adlarının doğru olduğunu doğrulayın
- Discord webhook test aracıyla embed formatını test edin

**Hız sınırlaması:**
- Discord webhook'ları dakikada 30 istekle sınırlar
- DeliveryCore otomatik olarak hız sınırlamasını yönetir
- Gerekirse sıralama güncellemelerinin sıklığını azaltın

#### Hata Mesajları

**"Geçersiz webhook URL":**
- URL formatını kontrol edin: `https://discord.com/api/webhooks/ID/TOKEN`
- Webhook'un Discord'da silinmediğinden emin olun

**"Webhook hız sınırlandı":**
- Hız sınırının sıfırlanmasını bekleyin (genellikle 1 dakika)
- Yapılandırmada webhook sıklığını azaltın

**"Embed çok büyük":**
- Sıralama boyutunu azaltın (`max-leaderboard`)
- Açıklama metnini kısaltın
- Gereksiz alanları kaldırın

### Güvenlik

#### En İyi Uygulamalar

- Webhook URL'lerini özel ve güvenli tutun
- Webhook URL'lerini düzenli olarak değiştirin
- Hassas bilgiler için rol-özel kanallar kullanın
- Kötüye kullanım için webhook kullanımını izleyin

#### URL Koruması

Webhook URL'lerini asla herkese açık paylaşmayın. Ele geçirilirse:

1. Discord'da eski webhook'u silin
2. Yeni bir webhook oluşturun
3. Yapılandırmayı yeni URL ile güncelleyin
4. Sunucuyu yeniden başlatın veya yapılandırmayı yeniden yükleyin