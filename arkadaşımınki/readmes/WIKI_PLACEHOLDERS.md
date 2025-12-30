# Placeholder System | Placeholder Sistemi

## English

DeliveryCore provides a comprehensive placeholder system that integrates with PlaceholderAPI and offers internal placeholders for use in messages, GUIs, and other plugins.

### PlaceholderAPI Integration

DeliveryCore automatically registers with PlaceholderAPI when available, providing placeholders with the `%deliverycore_` prefix.

#### Installation

1. Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
2. Restart server
3. DeliveryCore will automatically register placeholders
4. Use placeholders in any compatible plugin

### Available Placeholders

#### Player Statistics

```
%deliverycore_player_total_deliveries%
%deliverycore_player_total_wins%
%deliverycore_player_current_rank%
%deliverycore_player_points%
%deliverycore_player_level%
```

**Examples:**
- `%deliverycore_player_total_deliveries%` → `1,234`
- `%deliverycore_player_total_wins%` → `45`
- `%deliverycore_player_current_rank%` → `#3`

#### Active Events

```
%deliverycore_active_events_count%
%deliverycore_active_events_list%
%deliverycore_next_event_name%
%deliverycore_next_event_time%
```

**Examples:**
- `%deliverycore_active_events_count%` → `2`
- `%deliverycore_active_events_list%` → `Daily Farm, Weekly Ore`
- `%deliverycore_next_event_name%` → `Evening Special`
- `%deliverycore_next_event_time%` → `18:00`

#### Event Information

```
%deliverycore_event_<name>_status%
%deliverycore_event_<name>_participants%
%deliverycore_event_<name>_time_left%
%deliverycore_event_<name>_required_item%
%deliverycore_event_<name>_winners_count%
```

**Examples:**
- `%deliverycore_event_daily_status%` → `Active`
- `%deliverycore_event_daily_participants%` → `15`
- `%deliverycore_event_daily_time_left%` → `1h 23m`
- `%deliverycore_event_daily_required_item%` → `Wheat x64`

#### Leaderboard

```
%deliverycore_leaderboard_1_name%
%deliverycore_leaderboard_1_deliveries%
%deliverycore_leaderboard_2_name%
%deliverycore_leaderboard_2_deliveries%
%deliverycore_leaderboard_3_name%
%deliverycore_leaderboard_3_deliveries%
```

**Examples:**
- `%deliverycore_leaderboard_1_name%` → `PlayerName`
- `%deliverycore_leaderboard_1_deliveries%` → `1,234`

#### Server Statistics

```
%deliverycore_server_total_deliveries%
%deliverycore_server_total_events%
%deliverycore_server_active_players%
%deliverycore_server_top_category%
```

**Examples:**
- `%deliverycore_server_total_deliveries%` → `50,000`
- `%deliverycore_server_total_events%` → `150`
- `%deliverycore_server_active_players%` → `25`
- `%deliverycore_server_top_category%` → `Farm`

### Internal Placeholders

Use these placeholders within DeliveryCore configurations:

#### Player Placeholders
- `{player}` - Player name
- `{uuid}` - Player UUID
- `{displayname}` - Player display name
- `{rank}` - Player's current rank position
- `{total_deliveries}` - Player's total deliveries
- `{total_wins}` - Player's total wins

#### Event Placeholders
- `{delivery}` - Delivery event name
- `{category}` - Category display name
- `{item}` - Required item name and amount
- `{duration}` - Event duration (formatted)
- `{time_left}` - Remaining time (formatted)
- `{participants}` - Number of participants
- `{winners}` - Number of winners
- `{status}` - Event status (Active/Inactive/Starting/Ending)

#### Time Placeholders
- `{date}` - Current date (dd/MM/yyyy)
- `{time}` - Current time (HH:mm:ss)
- `{datetime}` - Full date and time
- `{timestamp}` - Unix timestamp

#### Leaderboard Placeholders
- `{leaderboard}` - Full formatted leaderboard
- `{top1}`, `{top2}`, `{top3}` - Individual top players
- `{top1_deliveries}` - Top player's delivery count
- `{player_position}` - Current player's position

### Usage Examples

#### In Chat Messages

```yaml
messages:
  event-start: "&a📦 {delivery} has started! Deliver {item} to win!"
  player-win: "&6🏆 Congratulations {player}! You placed #{rank}!"
  leaderboard-update: "&eTop 3: {top1} ({top1_deliveries}), {top2}, {top3}"
```

#### In GUI Items

```yaml
gui:
  items:
    player-stats:
      name: "&6Your Statistics"
      lore:
        - "&7Total Deliveries: &f{total_deliveries}"
        - "&7Total Wins: &f{total_wins}"
        - "&7Current Rank: &f{rank}"
        - "&7"
        - "&eClick to view detailed stats"
```

#### In Discord Webhooks

```yaml
webhook:
  start:
    title: "📦 {delivery} Started!"
    description: "**Required Item:** {item}\n**Duration:** {duration}\n**Winners:** {winners}"
```

#### In Scoreboard (with PlaceholderAPI)

```yaml
# Scoreboard plugin configuration
lines:
  - "&6&lDeliveryCore"
  - ""
  - "&eActive Events: &f%deliverycore_active_events_count%"
  - "&eYour Deliveries: &f%deliverycore_player_total_deliveries%"
  - "&eYour Rank: &f%deliverycore_player_current_rank%"
  - ""
  - "&6Top Player:"
  - "&f%deliverycore_leaderboard_1_name%"
  - "&7%deliverycore_leaderboard_1_deliveries% deliveries"
```

#### In Tab List

```yaml
# Tab list plugin configuration
header:
  - "&6&lMinecraft Server"
  - "&eDelivery Events: &f%deliverycore_active_events_count%"

footer:
  - "&eYour Stats: &f%deliverycore_player_total_deliveries% deliveries"
  - "&eRank: &f%deliverycore_player_current_rank%"
```

### Custom Placeholder Configuration

#### Formatting Options

```yaml
placeholders:
  formatting:
    numbers:
      large-numbers: true  # 1,234 instead of 1234
      decimal-places: 0
    
    time:
      format: "HH:mm:ss"
      timezone: "Europe/Istanbul"
    
    date:
      format: "dd/MM/yyyy"
      locale: "tr_TR"
```

#### Leaderboard Formatting

```yaml
placeholders:
  leaderboard:
    format: "{position}. {player} - {deliveries}"
    max-entries: 10
    empty-message: "No data available"
    separator: "\n"
```

### Plugin Integration Examples

#### EssentialsX Chat

```yaml
# EssentialsX config.yml
format: '{DISPLAYNAME}&r: {MESSAGE}'
# Use DeliveryCore placeholders in display name via other plugins
```

#### ChatColor+ / ChatControl

```yaml
# Chat format with DeliveryCore placeholders
format: "&7[%deliverycore_player_current_rank%&7] {player}: {message}"
```

#### ActionBar Messages

```yaml
# ActionBar plugin configuration
message: "&eDeliveries: &f%deliverycore_player_total_deliveries% &7| &eRank: &f%deliverycore_player_current_rank%"
```

#### Holographic Displays

```yaml
# Hologram configuration
lines:
  - "&6&lDelivery Leaderboard"
  - ""
  - "&e1st: &f%deliverycore_leaderboard_1_name%"
  - "&7%deliverycore_leaderboard_1_deliveries% deliveries"
  - ""
  - "&e2nd: &f%deliverycore_leaderboard_2_name%"
  - "&7%deliverycore_leaderboard_2_deliveries% deliveries"
```

### Troubleshooting

#### Common Issues

**Placeholders showing as text:**
- Ensure PlaceholderAPI is installed and loaded
- Check if DeliveryCore registered successfully: `/papi list`
- Verify placeholder syntax is correct
- Restart server if placeholders don't register

**Placeholders returning empty/null:**
- Check if player has any delivery data
- Verify event names are correct in event-specific placeholders
- Ensure player is online for player-specific placeholders

**Formatting issues:**
- Check locale and timezone settings
- Verify number formatting configuration
- Test with different placeholder formats

#### Debug Commands

```bash
# Test placeholder values
/papi parse <player> %deliverycore_player_total_deliveries%

# List all DeliveryCore placeholders
/papi list deliverycore

# Reload placeholder configuration
/dc placeholders reload
```

### Performance Considerations

#### Caching

DeliveryCore automatically caches placeholder values to improve performance:

```yaml
placeholders:
  cache:
    enabled: true
    duration: 30  # seconds
    max-size: 1000  # maximum cached entries
```

#### Optimization Tips

- **Use specific placeholders** - Avoid broad placeholders like full leaderboards in high-frequency updates
- **Cache frequently used data** - Enable caching for better performance
- **Limit leaderboard size** - Reduce `max-entries` for leaderboard placeholders
- **Monitor usage** - Check which placeholders are used most frequently

---

## Türkçe

DeliveryCore, PlaceholderAPI ile entegre olan ve mesajlarda, GUI'lerde ve diğer eklentilerde kullanım için dahili placeholder'lar sunan kapsamlı bir placeholder sistemi sağlar.

### PlaceholderAPI Entegrasyonu

DeliveryCore, mevcut olduğunda PlaceholderAPI ile otomatik olarak kaydolur ve `%deliverycore_` öneki ile placeholder'lar sağlar.

#### Kurulum

1. [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) yükleyin
2. Sunucuyu yeniden başlatın
3. DeliveryCore otomatik olarak placeholder'ları kaydedecek
4. Uyumlu herhangi bir eklentide placeholder'ları kullanın

### Mevcut Placeholder'lar

#### Oyuncu İstatistikleri

```
%deliverycore_player_total_deliveries%
%deliverycore_player_total_wins%
%deliverycore_player_current_rank%
%deliverycore_player_points%
%deliverycore_player_level%
```

**Örnekler:**
- `%deliverycore_player_total_deliveries%` → `1,234`
- `%deliverycore_player_total_wins%` → `45`
- `%deliverycore_player_current_rank%` → `#3`

#### Aktif Etkinlikler

```
%deliverycore_active_events_count%
%deliverycore_active_events_list%
%deliverycore_next_event_name%
%deliverycore_next_event_time%
```

**Örnekler:**
- `%deliverycore_active_events_count%` → `2`
- `%deliverycore_active_events_list%` → `Günlük Çiftlik, Haftalık Maden`
- `%deliverycore_next_event_name%` → `Akşam Özel`
- `%deliverycore_next_event_time%` → `18:00`

#### Etkinlik Bilgileri

```
%deliverycore_event_<isim>_status%
%deliverycore_event_<isim>_participants%
%deliverycore_event_<isim>_time_left%
%deliverycore_event_<isim>_required_item%
%deliverycore_event_<isim>_winners_count%
```

**Örnekler:**
- `%deliverycore_event_gunluk_status%` → `Aktif`
- `%deliverycore_event_gunluk_participants%` → `15`
- `%deliverycore_event_gunluk_time_left%` → `1s 23d`
- `%deliverycore_event_gunluk_required_item%` → `Buğday x64`

#### Sıralama

```
%deliverycore_leaderboard_1_name%
%deliverycore_leaderboard_1_deliveries%
%deliverycore_leaderboard_2_name%
%deliverycore_leaderboard_2_deliveries%
%deliverycore_leaderboard_3_name%
%deliverycore_leaderboard_3_deliveries%
```

**Örnekler:**
- `%deliverycore_leaderboard_1_name%` → `OyuncuAdi`
- `%deliverycore_leaderboard_1_deliveries%` → `1,234`

#### Sunucu İstatistikleri

```
%deliverycore_server_total_deliveries%
%deliverycore_server_total_events%
%deliverycore_server_active_players%
%deliverycore_server_top_category%
```

**Örnekler:**
- `%deliverycore_server_total_deliveries%` → `50,000`
- `%deliverycore_server_total_events%` → `150`
- `%deliverycore_server_active_players%` → `25`
- `%deliverycore_server_top_category%` → `Çiftlik`

### Dahili Placeholder'lar

DeliveryCore yapılandırmalarında bu placeholder'ları kullanın:

#### Oyuncu Placeholder'ları
- `{player}` - Oyuncu adı
- `{uuid}` - Oyuncu UUID'si
- `{displayname}` - Oyuncu görünen adı
- `{rank}` - Oyuncunun mevcut sıralama pozisyonu
- `{total_deliveries}` - Oyuncunun toplam teslimatları
- `{total_wins}` - Oyuncunun toplam kazanımları

#### Etkinlik Placeholder'ları
- `{delivery}` - Teslimat etkinliği adı
- `{category}` - Kategori görünen adı
- `{item}` - İstenen eşya adı ve miktarı
- `{duration}` - Etkinlik süresi (formatlanmış)
- `{time_left}` - Kalan süre (formatlanmış)
- `{participants}` - Katılımcı sayısı
- `{winners}` - Kazanan sayısı
- `{status}` - Etkinlik durumu (Aktif/Pasif/Başlıyor/Bitiyor)

#### Zaman Placeholder'ları
- `{date}` - Şu anki tarih (dd/MM/yyyy)
- `{time}` - Şu anki saat (HH:mm:ss)
- `{datetime}` - Tam tarih ve saat
- `{timestamp}` - Unix zaman damgası

#### Sıralama Placeholder'ları
- `{leaderboard}` - Tam formatlanmış sıralama
- `{top1}`, `{top2}`, `{top3}` - Bireysel en iyi oyuncular
- `{top1_deliveries}` - En iyi oyuncunun teslimat sayısı
- `{player_position}` - Mevcut oyuncunun pozisyonu

### Kullanım Örnekleri

#### Sohbet Mesajlarında

```yaml
messages:
  event-start: "&a📦 {delivery} başladı! Kazanmak için {item} teslim edin!"
  player-win: "&6🏆 Tebrikler {player}! #{rank} oldunuz!"
  leaderboard-update: "&eİlk 3: {top1} ({top1_deliveries}), {top2}, {top3}"
```

#### GUI Eşyalarında

```yaml
gui:
  items:
    player-stats:
      name: "&6İstatistikleriniz"
      lore:
        - "&7Toplam Teslimat: &f{total_deliveries}"
        - "&7Toplam Kazanım: &f{total_wins}"
        - "&7Mevcut Sıralama: &f{rank}"
        - "&7"
        - "&eDetaylı istatistikler için tıklayın"
```

#### Discord Webhook'larında

```yaml
webhook:
  start:
    title: "📦 {delivery} Başladı!"
    description: "**İstenen Eşya:** {item}\n**Süre:** {duration}\n**Kazananlar:** {winners}"
```

#### Skor Tablosunda (PlaceholderAPI ile)

```yaml
# Skor tablosu eklenti yapılandırması
lines:
  - "&6&lDeliveryCore"
  - ""
  - "&eAktif Etkinlikler: &f%deliverycore_active_events_count%"
  - "&eTeslimatlarınız: &f%deliverycore_player_total_deliveries%"
  - "&eSıralamanız: &f%deliverycore_player_current_rank%"
  - ""
  - "&6En İyi Oyuncu:"
  - "&f%deliverycore_leaderboard_1_name%"
  - "&7%deliverycore_leaderboard_1_deliveries% teslimat"
```

#### Tab Listesinde

```yaml
# Tab listesi eklenti yapılandırması
header:
  - "&6&lMinecraft Sunucusu"
  - "&eTeslimat Etkinlikleri: &f%deliverycore_active_events_count%"

footer:
  - "&eİstatistikleriniz: &f%deliverycore_player_total_deliveries% teslimat"
  - "&eSıralama: &f%deliverycore_player_current_rank%"
```

### Özel Placeholder Yapılandırması

#### Formatlama Seçenekleri

```yaml
placeholders:
  formatting:
    numbers:
      large-numbers: true  # 1234 yerine 1,234
      decimal-places: 0
    
    time:
      format: "HH:mm:ss"
      timezone: "Europe/Istanbul"
    
    date:
      format: "dd/MM/yyyy"
      locale: "tr_TR"
```

#### Sıralama Formatlaması

```yaml
placeholders:
  leaderboard:
    format: "{position}. {player} - {deliveries}"
    max-entries: 10
    empty-message: "Veri mevcut değil"
    separator: "\n"
```

### Eklenti Entegrasyon Örnekleri

#### EssentialsX Sohbet

```yaml
# EssentialsX config.yml
format: '{DISPLAYNAME}&r: {MESSAGE}'
# Diğer eklentiler aracılığıyla görünen adda DeliveryCore placeholder'larını kullanın
```

#### ChatColor+ / ChatControl

```yaml
# DeliveryCore placeholder'ları ile sohbet formatı
format: "&7[%deliverycore_player_current_rank%&7] {player}: {message}"
```

#### ActionBar Mesajları

```yaml
# ActionBar eklenti yapılandırması
message: "&eTeslimatlar: &f%deliverycore_player_total_deliveries% &7| &eSıralama: &f%deliverycore_player_current_rank%"
```

#### Holographic Displays

```yaml
# Hologram yapılandırması
lines:
  - "&6&lTeslimat Sıralaması"
  - ""
  - "&e1.: &f%deliverycore_leaderboard_1_name%"
  - "&7%deliverycore_leaderboard_1_deliveries% teslimat"
  - ""
  - "&e2.: &f%deliverycore_leaderboard_2_name%"
  - "&7%deliverycore_leaderboard_2_deliveries% teslimat"
```

### Sorun Giderme

#### Yaygın Sorunlar

**Placeholder'lar metin olarak görünüyor:**
- PlaceholderAPI'nin yüklü ve çalışır durumda olduğundan emin olun
- DeliveryCore'un başarıyla kaydolduğunu kontrol edin: `/papi list`
- Placeholder sözdiziminin doğru olduğunu doğrulayın
- Placeholder'lar kaydolmazsa sunucuyu yeniden başlatın

**Placeholder'lar boş/null dönüyor:**
- Oyuncunun teslimat verisi olup olmadığını kontrol edin
- Etkinlik-özel placeholder'larda etkinlik adlarının doğru olduğunu doğrulayın
- Oyuncu-özel placeholder'lar için oyuncunun çevrimiçi olduğundan emin olun

**Formatlama sorunları:**
- Yerel ayar ve saat dilimi ayarlarını kontrol edin
- Sayı formatlama yapılandırmasını doğrulayın
- Farklı placeholder formatlarıyla test edin

#### Hata Ayıklama Komutları

```bash
# Placeholder değerlerini test et
/papi parse <oyuncu> %deliverycore_player_total_deliveries%

# Tüm DeliveryCore placeholder'larını listele
/papi list deliverycore

# Placeholder yapılandırmasını yeniden yükle
/dc placeholders reload
```

### Performans Değerlendirmeleri

#### Önbellekleme

DeliveryCore performansı artırmak için placeholder değerlerini otomatik olarak önbelleğe alır:

```yaml
placeholders:
  cache:
    enabled: true
    duration: 30  # saniye
    max-size: 1000  # maksimum önbellek girişi
```

#### Optimizasyon İpuçları

- **Belirli placeholder'lar kullanın** - Yüksek frekanslı güncellemelerde tam sıralama gibi geniş placeholder'lardan kaçının
- **Sık kullanılan verileri önbelleğe alın** - Daha iyi performans için önbelleklemeyi etkinleştirin
- **Sıralama boyutunu sınırlayın** - Sıralama placeholder'ları için `max-entries` değerini azaltın
- **Kullanımı izleyin** - Hangi placeholder'ların en sık kullanıldığını kontrol edin