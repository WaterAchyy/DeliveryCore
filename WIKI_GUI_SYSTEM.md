# GUI System | GUI Sistemi

## English

DeliveryCore features a beautiful and intuitive GUI system with custom head textures and smooth animations.

### Main Delivery GUI

Access the main GUI with `/teslimat` or `/delivery` command.

#### GUI Layout

```
┌─────────────────────────────────────────────────┐
│  [Info] [Active Events] [Leaderboard] [Settings] │
│                                                 │
│  [Event 1]  [Event 2]  [Event 3]  [Event 4]    │
│  [Event 5]  [Event 6]  [Event 7]  [Event 8]    │
│  [Event 9]  [Event 10] [Event 11] [Event 12]   │
│                                                 │
│  [Previous] [Refresh] [Close] [Next]            │
└─────────────────────────────────────────────────┘
```

#### GUI Elements

**Navigation Bar:**
- **Info Button** - Shows plugin information and statistics
- **Active Events** - Lists currently running events
- **Leaderboard** - Shows top players and rankings
- **Settings** - Player preferences and options

**Event Slots:**
- **Green Border** - Active event (can participate)
- **Red Border** - Inactive event
- **Yellow Border** - Event starting soon
- **Blue Border** - Event ending soon

**Control Bar:**
- **Previous/Next** - Navigate between pages
- **Refresh** - Update GUI content
- **Close** - Close the GUI

### Event Details GUI

Click on any event to open detailed information:

```
┌─────────────────────────────────────────────────┐
│              Daily Farm Event                   │
│                                                 │
│  📦 Required Item: Wheat (64x)                  │
│  ⏰ Time Remaining: 1h 23m 45s                  │
│  🏆 Winners: 3 players                          │
│  👥 Participants: 12/∞                          │
│                                                 │
│  🥇 1st: PlayerName (128 items)                 │
│  🥈 2nd: Player2 (96 items)                     │
│  🥉 3rd: Player3 (64 items)                     │
│                                                 │
│  [Deliver Items] [View Rewards] [Back]          │
└─────────────────────────────────────────────────┘
```

### Leaderboard GUI

Shows rankings for all events:

```
┌─────────────────────────────────────────────────┐
│                 Leaderboard                     │
│                                                 │
│  🥇 #1  PlayerName     1,234 deliveries        │
│  🥈 #2  Player2        987 deliveries          │
│  🥉 #3  Player3        756 deliveries          │
│  🏅 #4  Player4        543 deliveries          │
│  🏅 #5  Player5        432 deliveries          │
│                                                 │
│  [Daily] [Weekly] [Monthly] [All Time]         │
│  [Back] [Refresh]                              │
└─────────────────────────────────────────────────┘
```

### Custom Head Textures

DeliveryCore uses custom player head textures for enhanced visual experience:

#### Category Icons
- 🌾 **Farm** - Wheat head texture
- ⛏️ **Ore** - Diamond ore head texture
- 🧱 **Block** - Stone brick head texture
- 🍖 **Food** - Cooked beef head texture
- 🌳 **Wood** - Oak log head texture
- 💎 **Rare** - Diamond head texture
- 🔥 **Nether** - Netherrack head texture
- 🌌 **End** - End stone head texture

#### Status Icons
- ✅ **Active** - Green checkmark head
- ❌ **Inactive** - Red X head
- ⏰ **Starting** - Clock head texture
- 🏁 **Ending** - Flag head texture

### GUI Configuration

Customize GUI appearance in `config.yml`:

```yaml
gui:
  title: "DeliveryCore"
  size: 54  # 6 rows
  update-interval: 20  # ticks (1 second)
  
  sounds:
    click: "UI_BUTTON_CLICK"
    success: "ENTITY_PLAYER_LEVELUP"
    error: "BLOCK_NOTE_BLOCK_BASS"
  
  animations:
    enabled: true
    duration: 10  # ticks
```

### GUI Permissions

```yaml
deliverycore.gui.use: true      # Open main GUI
deliverycore.gui.admin: false   # Admin GUI features
deliverycore.gui.leaderboard: true  # View leaderboard
```

### GUI Commands

```bash
# Open main GUI
/teslimat
/delivery

# Open specific GUI sections
/teslimat bilgi     # Info page
/teslimat top       # Leaderboard
/delivery info      # Info page
/delivery top       # Leaderboard
```

### Troubleshooting

**GUI not opening:**
- Check permission: `deliverycore.gui.use`
- Verify player is not in creative mode (if restricted)
- Check for conflicting plugins

**Custom heads not showing:**
- Verify internet connection (heads load from Mojang)
- Check if server allows external connections
- Some heads may take time to load

**GUI lag or slow updates:**
- Increase `update-interval` in config
- Reduce number of concurrent events
- Check server performance

---

## Türkçe

DeliveryCore, özel kafa dokuları ve yumuşak animasyonlarla güzel ve sezgisel bir GUI sistemi sunar.

### Ana Teslimat GUI'si

Ana GUI'ye `/teslimat` veya `/delivery` komutuyla erişin.

#### GUI Düzeni

```
┌─────────────────────────────────────────────────┐
│  [Bilgi] [Aktif Etkinlikler] [Sıralama] [Ayarlar] │
│                                                 │
│  [Etkinlik 1] [Etkinlik 2] [Etkinlik 3] [Etkinlik 4] │
│  [Etkinlik 5] [Etkinlik 6] [Etkinlik 7] [Etkinlik 8] │
│  [Etkinlik 9] [Etkinlik 10] [Etkinlik 11] [Etkinlik 12] │
│                                                 │
│  [Önceki] [Yenile] [Kapat] [Sonraki]           │
└─────────────────────────────────────────────────┘
```

#### GUI Öğeleri

**Navigasyon Çubuğu:**
- **Bilgi Butonu** - Eklenti bilgilerini ve istatistikleri gösterir
- **Aktif Etkinlikler** - Şu anda çalışan etkinlikleri listeler
- **Sıralama** - En iyi oyuncuları ve sıralamaları gösterir
- **Ayarlar** - Oyuncu tercihleri ve seçenekleri

**Etkinlik Slotları:**
- **Yeşil Kenarlık** - Aktif etkinlik (katılabilirsiniz)
- **Kırmızı Kenarlık** - Pasif etkinlik
- **Sarı Kenarlık** - Yakında başlayacak etkinlik
- **Mavi Kenarlık** - Yakında bitecek etkinlik

**Kontrol Çubuğu:**
- **Önceki/Sonraki** - Sayfalar arasında gezinme
- **Yenile** - GUI içeriğini güncelleme
- **Kapat** - GUI'yi kapatma

### Etkinlik Detayları GUI'si

Detaylı bilgi için herhangi bir etkinliğe tıklayın:

```
┌─────────────────────────────────────────────────┐
│              Günlük Çiftlik Etkinliği           │
│                                                 │
│  📦 İstenen Eşya: Buğday (64x)                  │
│  ⏰ Kalan Süre: 1s 23d 45sn                     │
│  🏆 Kazananlar: 3 oyuncu                        │
│  👥 Katılımcılar: 12/∞                          │
│                                                 │
│  🥇 1. PlayerName (128 eşya)                    │
│  🥈 2. Player2 (96 eşya)                        │
│  🥉 3. Player3 (64 eşya)                        │
│                                                 │
│  [Eşya Teslim Et] [Ödülleri Gör] [Geri]        │
└─────────────────────────────────────────────────┘
```

### Sıralama GUI'si

Tüm etkinlikler için sıralamaları gösterir:

```
┌─────────────────────────────────────────────────┐
│                   Sıralama                      │
│                                                 │
│  🥇 #1  PlayerName     1,234 teslimat          │
│  🥈 #2  Player2        987 teslimat            │
│  🥉 #3  Player3        756 teslimat            │
│  🏅 #4  Player4        543 teslimat            │
│  🏅 #5  Player5        432 teslimat            │
│                                                 │
│  [Günlük] [Haftalık] [Aylık] [Tüm Zamanlar]   │
│  [Geri] [Yenile]                               │
└─────────────────────────────────────────────────┘
```

### Özel Kafa Dokuları

DeliveryCore, gelişmiş görsel deneyim için özel oyuncu kafası dokularını kullanır:

#### Kategori İkonları
- 🌾 **Çiftlik** - Buğday kafası dokusu
- ⛏️ **Maden** - Elmas cevheri kafası dokusu
- 🧱 **Blok** - Taş tuğla kafası dokusu
- 🍖 **Yiyecek** - Pişmiş et kafası dokusu
- 🌳 **Odun** - Meşe kütük kafası dokusu
- 💎 **Nadir** - Elmas kafası dokusu
- 🔥 **Nether** - Netherrack kafası dokusu
- 🌌 **End** - End taşı kafası dokusu

#### Durum İkonları
- ✅ **Aktif** - Yeşil onay işareti kafası
- ❌ **Pasif** - Kırmızı X kafası
- ⏰ **Başlıyor** - Saat kafası dokusu
- 🏁 **Bitiyor** - Bayrak kafası dokusu

### GUI Yapılandırması

`config.yml` dosyasında GUI görünümünü özelleştirin:

```yaml
gui:
  title: "DeliveryCore"
  size: 54  # 6 sıra
  update-interval: 20  # tick (1 saniye)
  
  sounds:
    click: "UI_BUTTON_CLICK"
    success: "ENTITY_PLAYER_LEVELUP"
    error: "BLOCK_NOTE_BLOCK_BASS"
  
  animations:
    enabled: true
    duration: 10  # tick
```

### GUI İzinleri

```yaml
deliverycore.gui.use: true      # Ana GUI'yi açma
deliverycore.gui.admin: false   # Yönetici GUI özellikleri
deliverycore.gui.leaderboard: true  # Sıralamayı görüntüleme
```

### GUI Komutları

```bash
# Ana GUI'yi aç
/teslimat
/delivery

# Belirli GUI bölümlerini aç
/teslimat bilgi     # Bilgi sayfası
/teslimat top       # Sıralama
/delivery info      # Bilgi sayfası
/delivery top       # Sıralama
```

### Sorun Giderme

**GUI açılmıyor:**
- İzni kontrol edin: `deliverycore.gui.use`
- Oyuncunun yaratıcı modda olmadığını doğrulayın (kısıtlıysa)
- Çakışan eklentileri kontrol edin

**Özel kafalar görünmüyor:**
- İnternet bağlantısını doğrulayın (kafalar Mojang'dan yüklenir)
- Sunucunun dış bağlantılara izin verip vermediğini kontrol edin
- Bazı kafaların yüklenmesi zaman alabilir

**GUI gecikmesi veya yavaş güncellemeler:**
- Yapılandırmada `update-interval` değerini artırın
- Eş zamanlı etkinlik sayısını azaltın
- Sunucu performansını kontrol edin