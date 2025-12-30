# Changelog

## [1.1.0] - 2024-12-30

### 🎉 v1.1 Yeni Özellikler
- **Minecraft 1.21.x Desteği**: Tüm yeni 1.21 itemları (Pale Oak, Resin Brick, Creaking Heart vb.)
- **PlaceholderAPI Entegrasyonu**: DecentHolograms, TAB ve diğer pluginlerle uyumlu 30+ placeholder
- **Discord Webhook Sistemi**: Başlangıç, bitiş ve uyarı bildirimleri (kazanan listesi ile)
- **Sezonluk Sistem**: Belirli tarih aralıklarında ve özel günlerde çalışan etkinlikler
- **Tab List Entegrasyonu**: Tab listesinde aktif teslimat ve kalan süre gösterimi
- **Özel Item Sistemi**: Eldeki item'ı kategoriye ekleme (`/dc additem`)
- **Sunucu Restart Desteği**: Aktif etkinlikler restart sonrası devam eder

### 🏷️ PlaceholderAPI Placeholder'ları
```
%deliverycore_active%              - Aktif teslimat adı
%deliverycore_active_item%         - Aktif teslimat eşyası
%deliverycore_time_left%           - Kalan süre (formatlanmış)
%deliverycore_player_count%        - Oyuncunun teslimat sayısı
%deliverycore_player_rank%         - Oyuncunun sırası
%deliverycore_top_1_name%          - 1. sıradaki oyuncu
%deliverycore_top_1_count%         - 1. sıradaki teslimat sayısı
... ve daha fazlası
```

### 📋 Yeni Admin Komutları
- `/dc tab <teslimat> on/off` - Tab gösterimini aç/kapat
- `/dc additem <kategori> <isim>` - Eldeki item'ı kategoriye ekle
- `/dc removeitem <kategori> <isim>` - Özel item'ı sil
- `/dc listcustom` - Özel itemları listele
- `/dc test webhook` - Webhook test et

### 🔧 Teknik Geliştirmeler
- Property-based testler eklendi (jqwik)
- WebhookHandler ile gelişmiş webhook yönetimi
- Geriye dönük uyumluluk korundu
- Dinamik yeniden yükleme desteği

### ⚠️ Kaldırılan Özellikler
- Hologram sistemi v1.2'ye ertelendi (PlaceholderAPI ile DecentHolograms kullanılabilir)

---

## [1.0.0] - 2024-12-23

### 🎉 Initial Release

#### Features
- Scheduled delivery events with natural language scheduling ("every day at 18:00")
- 17 item categories (Farm, Ore, Block, Food, Wood, Rare, Nether, End, Mob, Dye, Tool, Armor, Combat, Potion, Decoration, Redstone, Misc)
- Random or fixed category/item selection per delivery
- Beautiful GUI with custom head textures
- Real-time leaderboard system
- Deliver from inventory or chest
- Discord webhook integration (start/end notifications with winner leaderboard)
- Multi-language support (Turkish & English)
- Dynamic language switching without restart
- Reward system (items + commands) with pending rewards for offline players
- Admin commands for manual event control
- Full PlaceholderAPI-style placeholder system
- Data persistence across server restarts
- Protection plugin compatibility (WorldGuard, GriefPrevention, etc.)

#### Commands
- `/teslimat` - Opens delivery GUI
- `/teslim` - Quick deliver from inventory
- `/dc reload` - Reload all configurations
- `/dc start <name> [duration] [winners]` - Manual event start
- `/dc stop <name>` - Stop active event
- `/dc status [name]` - View event status
- `/dc list` - List all deliveries
- `/dc top` - View leaderboard

#### Supported Versions
- Minecraft: 1.16.5 - 1.20.4
- Servers: Spigot, Paper, Purpur, Bukkit

---

# Değişiklik Günlüğü

## [1.0.0] - 2024-12-23

### 🎉 İlk Sürüm

#### Özellikler
- Doğal dil ile zamanlanmış teslimat etkinlikleri ("her gün saat 18:00")
- 17 eşya kategorisi (Çiftlik, Maden, Blok, Yiyecek, Odun, Nadir, Nether, End, Mob, Boya, Alet, Zırh, Savaş, İksir, Dekorasyon, Kızıltaş, Çeşitli)
- Teslimat başına rastgele veya sabit kategori/eşya seçimi
- Özel kafa dokuları ile güzel GUI
- Gerçek zamanlı sıralama sistemi
- Envanterden veya sandıktan teslimat
- Discord webhook entegrasyonu (başlangıç/bitiş bildirimleri ve kazanan sıralaması)
- Çoklu dil desteği (Türkçe & İngilizce)
- Yeniden başlatma olmadan dinamik dil değiştirme
- Çevrimdışı oyuncular için bekleyen ödüllerle ödül sistemi (eşya + komut)
- Manuel etkinlik kontrolü için admin komutları
- Tam PlaceholderAPI tarzı placeholder sistemi
- Sunucu yeniden başlatmalarında veri kalıcılığı
- Koruma eklentisi uyumluluğu (WorldGuard, GriefPrevention, vb.)

#### Komutlar
- `/teslimat` - Teslimat GUI'sini açar
- `/teslim` - Envanterden hızlı teslimat
- `/dc reload` - Tüm yapılandırmaları yeniden yükle
- `/dc start <ad> [süre] [kazanan]` - Manuel etkinlik başlat
- `/dc stop <ad>` - Aktif etkinliği durdur
- `/dc status [ad]` - Etkinlik durumunu görüntüle
- `/dc list` - Tüm teslimatları listele
- `/dc top` - Sıralamayı görüntüle

#### Desteklenen Sürümler
- Minecraft: 1.16.5 - 1.20.4
- Sunucular: Spigot, Paper, Purpur, Bukkit
