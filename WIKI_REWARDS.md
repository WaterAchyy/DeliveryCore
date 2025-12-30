# Reward System | Ödül Sistemi

## English

DeliveryCore features a comprehensive reward system that supports both item rewards and command execution, with full offline player support.

### Reward Types

#### Item Rewards

Give physical items to winners:

```yaml
rewards:
  items:
    - material: DIAMOND
      amount: 5
      name: "&bDelivery Reward Diamond"
      lore:
        - "&7Earned from delivery event"
        - "&7{date}"
    - material: EMERALD
      amount: 10
```

#### Command Rewards

Execute commands for winners:

```yaml
rewards:
  commands:
    - "eco give {player} 1000"
    - "lp user {player} permission set vip.rank true"
    - "broadcast &a{player} won the delivery event!"
```

#### Experience Rewards

Give experience points:

```yaml
rewards:
  experience: 500  # XP points
  levels: 5        # XP levels
```

### Reward Configuration

#### Basic Reward Setup

```yaml
deliveries:
  daily-farm:
    name: "Daily Farm Event"
    category: "farm"
    duration: "2h"
    winners: 3
    
    rewards:
      # First place rewards
      1st:
        items:
          - material: DIAMOND_BLOCK
            amount: 3
            name: "&6🥇 First Place Diamond"
        commands:
          - "eco give {player} 5000"
          - "broadcast &6🥇 {player} won 1st place!"
        experience: 1000
        
      # Second place rewards  
      2nd:
        items:
          - material: EMERALD_BLOCK
            amount: 2
        commands:
          - "eco give {player} 3000"
        experience: 750
        
      # Third place rewards
      3rd:
        items:
          - material: GOLD_BLOCK
            amount: 1
        commands:
          - "eco give {player} 1500"
        experience: 500
        
      # Participation rewards (all participants)
      participation:
        items:
          - material: IRON_INGOT
            amount: 5
        experience: 100
```

#### Advanced Item Configuration

```yaml
rewards:
  items:
    - material: DIAMOND_SWORD
      amount: 1
      name: "&cDelivery Champion Sword"
      lore:
        - "&7A legendary sword for"
        - "&7delivery champions"
        - "&7"
        - "&eWon on: &f{date}"
        - "&eEvent: &f{delivery}"
      enchantments:
        SHARPNESS: 5
        UNBREAKING: 3
        MENDING: 1
      custom-model-data: 12345
      
    - material: PLAYER_HEAD
      amount: 1
      name: "&6Winner's Trophy"
      skull-owner: "{player}"  # Winner's head
      lore:
        - "&7{player}'s victory trophy"
```

### Offline Player Support

DeliveryCore automatically handles offline players:

#### Pending Rewards System

When a winner is offline:

1. **Rewards are queued** - Stored in `pending-rewards.yml`
2. **Notification sent** - Player notified when they join
3. **Auto-delivery** - Rewards given when player comes online
4. **Backup storage** - Items stored safely until claimed

#### Configuration

```yaml
rewards:
  offline-handling:
    enabled: true
    notify-on-join: true
    auto-give: true
    max-pending-days: 30  # Delete unclaimed rewards after 30 days
    
  notifications:
    title: "&6You have pending rewards!"
    subtitle: "&eType /delivery claim to collect"
    sound: "ENTITY_PLAYER_LEVELUP"
```

### Reward Placeholders

Use these placeholders in reward items and commands:

#### Player Placeholders
- `{player}` - Winner's username
- `{uuid}` - Winner's UUID
- `{rank}` - Winner's position (1st, 2nd, 3rd)
- `{position}` - Winner's position number (1, 2, 3)

#### Event Placeholders
- `{delivery}` - Delivery event name
- `{category}` - Category name
- `{item}` - Required item name
- `{amount}` - Items delivered by winner
- `{total}` - Total items delivered in event

#### Time Placeholders
- `{date}` - Current date (formatted)
- `{time}` - Current time (formatted)
- `{datetime}` - Full date and time
- `{duration}` - Event duration

### Reward Commands

#### Player Commands

```bash
# Check pending rewards
/delivery rewards
/teslimat oduller

# Claim pending rewards
/delivery claim
/teslimat al

# View reward history
/delivery history
/teslimat gecmis
```

#### Admin Commands

```bash
# Give rewards manually
/dc reward give <player> <delivery> <position>

# Check player's pending rewards
/dc reward check <player>

# Clear pending rewards
/dc reward clear <player>

# Reload reward configuration
/dc reward reload
```

### Reward Categories

#### Tier-based Rewards

Set different rewards based on event importance:

```yaml
reward-tiers:
  common:
    items:
      - material: IRON_INGOT
        amount: 10
    commands:
      - "eco give {player} 500"
      
  rare:
    items:
      - material: DIAMOND
        amount: 5
    commands:
      - "eco give {player} 2000"
      
  legendary:
    items:
      - material: NETHERITE_INGOT
        amount: 1
    commands:
      - "eco give {player} 10000"
      - "lp user {player} permission set legendary.rank true"

deliveries:
  daily-event:
    reward-tier: "common"
    
  weekly-event:
    reward-tier: "rare"
    
  monthly-event:
    reward-tier: "legendary"
```

#### Random Rewards

Give random rewards from a pool:

```yaml
rewards:
  random-pool:
    enabled: true
    count: 2  # Give 2 random items
    items:
      - material: DIAMOND
        amount: 3
        weight: 10
      - material: EMERALD
        amount: 5
        weight: 15
      - material: NETHERITE_SCRAP
        amount: 1
        weight: 5
```

### Integration Support

#### Economy Plugins

```yaml
rewards:
  commands:
    # Vault/EssentialsX
    - "eco give {player} 1000"
    
    # PlayerPoints
    - "points give {player} 500"
    
    # TokenManager
    - "tm give {player} 100"
```

#### Permission Plugins

```yaml
rewards:
  commands:
    # LuckPerms
    - "lp user {player} permission set vip.rank true"
    - "lp user {player} parent add vip"
    
    # PermissionsEx
    - "pex user {player} add vip.rank"
    
    # GroupManager
    - "manuadd {player} vip"
```

#### Other Plugins

```yaml
rewards:
  commands:
    # McMMO
    - "addxp {player} mining 1000"
    
    # Jobs
    - "jobs fire {player} miner"
    
    # Crates
    - "crates give {player} vote 1"
```

### Troubleshooting

#### Common Issues

**Rewards not given:**
- Check player inventory space
- Verify command syntax in rewards
- Check console for error messages
- Ensure required plugins are installed

**Offline rewards not working:**
- Verify `offline-handling.enabled: true`
- Check `pending-rewards.yml` file exists
- Ensure player has joined since winning

**Commands not executing:**
- Test commands manually in console
- Check if required plugins are loaded
- Verify placeholder replacements
- Check command permissions

#### Error Messages

**"Inventory full":**
- Player inventory is full
- Items are stored as pending rewards
- Player will be notified to make space

**"Command failed":**
- Check command syntax
- Verify required plugin is installed
- Check console for detailed error

**"Reward expired":**
- Pending reward exceeded `max-pending-days`
- Reward was automatically deleted
- Check reward history for details

### Best Practices

#### Reward Balance

- **Don't over-reward** - Keep economy balanced
- **Scale with difficulty** - Harder events = better rewards
- **Consider server economy** - Match existing reward levels
- **Test thoroughly** - Verify all rewards work correctly

#### Performance

- **Limit command rewards** - Too many commands can cause lag
- **Use efficient items** - Avoid complex NBT data
- **Monitor pending rewards** - Clean up old rewards regularly
- **Batch operations** - Group similar rewards together

---

## Türkçe

DeliveryCore, hem eşya ödülleri hem de komut yürütmeyi destekleyen, tam çevrimdışı oyuncu desteği ile kapsamlı bir ödül sistemi sunar.

### Ödül Türleri

#### Eşya Ödülleri

Kazananlara fiziksel eşyalar verin:

```yaml
rewards:
  items:
    - material: DIAMOND
      amount: 5
      name: "&bTeslimat Ödülü Elmas"
      lore:
        - "&7Teslimat etkinliğinden kazanıldı"
        - "&7{date}"
    - material: EMERALD
      amount: 10
```

#### Komut Ödülleri

Kazananlar için komutları çalıştırın:

```yaml
rewards:
  commands:
    - "eco give {player} 1000"
    - "lp user {player} permission set vip.rank true"
    - "broadcast &a{player} teslimat etkinliğini kazandı!"
```

#### Deneyim Ödülleri

Deneyim puanları verin:

```yaml
rewards:
  experience: 500  # XP puanları
  levels: 5        # XP seviyeleri
```

### Ödül Yapılandırması

#### Temel Ödül Kurulumu

```yaml
deliveries:
  gunluk-ciftlik:
    name: "Günlük Çiftlik Etkinliği"
    category: "farm"
    duration: "2h"
    winners: 3
    
    rewards:
      # Birinci yer ödülleri
      1st:
        items:
          - material: DIAMOND_BLOCK
            amount: 3
            name: "&6🥇 Birinci Yer Elması"
        commands:
          - "eco give {player} 5000"
          - "broadcast &6🥇 {player} birinci oldu!"
        experience: 1000
        
      # İkinci yer ödülleri  
      2nd:
        items:
          - material: EMERALD_BLOCK
            amount: 2
        commands:
          - "eco give {player} 3000"
        experience: 750
        
      # Üçüncü yer ödülleri
      3rd:
        items:
          - material: GOLD_BLOCK
            amount: 1
        commands:
          - "eco give {player} 1500"
        experience: 500
        
      # Katılım ödülleri (tüm katılımcılar)
      participation:
        items:
          - material: IRON_INGOT
            amount: 5
        experience: 100
```

#### Gelişmiş Eşya Yapılandırması

```yaml
rewards:
  items:
    - material: DIAMOND_SWORD
      amount: 1
      name: "&cTeslimat Şampiyonu Kılıcı"
      lore:
        - "&7Teslimat şampiyonları için"
        - "&7efsanevi bir kılıç"
        - "&7"
        - "&eKazanıldığı tarih: &f{date}"
        - "&eEtkinlik: &f{delivery}"
      enchantments:
        SHARPNESS: 5
        UNBREAKING: 3
        MENDING: 1
      custom-model-data: 12345
      
    - material: PLAYER_HEAD
      amount: 1
      name: "&6Kazanan Kupası"
      skull-owner: "{player}"  # Kazananın kafası
      lore:
        - "&7{player}'nin zafer kupası"
```

### Çevrimdışı Oyuncu Desteği

DeliveryCore çevrimdışı oyuncuları otomatik olarak yönetir:

#### Bekleyen Ödüller Sistemi

Kazanan çevrimdışıyken:

1. **Ödüller sıraya alınır** - `pending-rewards.yml` dosyasında saklanır
2. **Bildirim gönderilir** - Oyuncu katıldığında bilgilendirilir
3. **Otomatik teslimat** - Oyuncu çevrimiçi olduğunda ödüller verilir
4. **Yedek depolama** - Eşyalar talep edilene kadar güvenle saklanır

#### Yapılandırma

```yaml
rewards:
  offline-handling:
    enabled: true
    notify-on-join: true
    auto-give: true
    max-pending-days: 30  # 30 gün sonra talep edilmeyen ödülleri sil
    
  notifications:
    title: "&6Bekleyen ödülleriniz var!"
    subtitle: "&eToplamak için /delivery claim yazın"
    sound: "ENTITY_PLAYER_LEVELUP"
```

### Ödül Placeholder'ları

Ödül eşyalarında ve komutlarında bu placeholder'ları kullanın:

#### Oyuncu Placeholder'ları
- `{player}` - Kazananın kullanıcı adı
- `{uuid}` - Kazananın UUID'si
- `{rank}` - Kazananın pozisyonu (1., 2., 3.)
- `{position}` - Kazananın pozisyon numarası (1, 2, 3)

#### Etkinlik Placeholder'ları
- `{delivery}` - Teslimat etkinliği adı
- `{category}` - Kategori adı
- `{item}` - İstenen eşya adı
- `{amount}` - Kazanan tarafından teslim edilen eşyalar
- `{total}` - Etkinlikte teslim edilen toplam eşyalar

#### Zaman Placeholder'ları
- `{date}` - Şu anki tarih (formatlanmış)
- `{time}` - Şu anki saat (formatlanmış)
- `{datetime}` - Tam tarih ve saat
- `{duration}` - Etkinlik süresi

### Ödül Komutları

#### Oyuncu Komutları

```bash
# Bekleyen ödülleri kontrol et
/delivery rewards
/teslimat oduller

# Bekleyen ödülleri al
/delivery claim
/teslimat al

# Ödül geçmişini görüntüle
/delivery history
/teslimat gecmis
```

#### Yönetici Komutları

```bash
# Manuel ödül ver
/dc reward give <oyuncu> <teslimat> <pozisyon>

# Oyuncunun bekleyen ödüllerini kontrol et
/dc reward check <oyuncu>

# Bekleyen ödülleri temizle
/dc reward clear <oyuncu>

# Ödül yapılandırmasını yeniden yükle
/dc reward reload
```

### Ödül Kategorileri

#### Seviye Bazlı Ödüller

Etkinlik önemine göre farklı ödüller ayarlayın:

```yaml
reward-tiers:
  common:
    items:
      - material: IRON_INGOT
        amount: 10
    commands:
      - "eco give {player} 500"
      
  rare:
    items:
      - material: DIAMOND
        amount: 5
    commands:
      - "eco give {player} 2000"
      
  legendary:
    items:
      - material: NETHERITE_INGOT
        amount: 1
    commands:
      - "eco give {player} 10000"
      - "lp user {player} permission set legendary.rank true"

deliveries:
  gunluk-etkinlik:
    reward-tier: "common"
    
  haftalik-etkinlik:
    reward-tier: "rare"
    
  aylik-etkinlik:
    reward-tier: "legendary"
```

#### Rastgele Ödüller

Bir havuzdan rastgele ödüller verin:

```yaml
rewards:
  random-pool:
    enabled: true
    count: 2  # 2 rastgele eşya ver
    items:
      - material: DIAMOND
        amount: 3
        weight: 10
      - material: EMERALD
        amount: 5
        weight: 15
      - material: NETHERITE_SCRAP
        amount: 1
        weight: 5
```

### Entegrasyon Desteği

#### Ekonomi Eklentileri

```yaml
rewards:
  commands:
    # Vault/EssentialsX
    - "eco give {player} 1000"
    
    # PlayerPoints
    - "points give {player} 500"
    
    # TokenManager
    - "tm give {player} 100"
```

#### İzin Eklentileri

```yaml
rewards:
  commands:
    # LuckPerms
    - "lp user {player} permission set vip.rank true"
    - "lp user {player} parent add vip"
    
    # PermissionsEx
    - "pex user {player} add vip.rank"
    
    # GroupManager
    - "manuadd {player} vip"
```

#### Diğer Eklentiler

```yaml
rewards:
  commands:
    # McMMO
    - "addxp {player} mining 1000"
    
    # Jobs
    - "jobs fire {player} miner"
    
    # Crates
    - "crates give {player} vote 1"
```

### Sorun Giderme

#### Yaygın Sorunlar

**Ödüller verilmiyor:**
- Oyuncu envanter alanını kontrol edin
- Ödüllerdeki komut sözdizimini doğrulayın
- Hata mesajları için konsolu kontrol edin
- Gerekli eklentilerin yüklü olduğundan emin olun

**Çevrimdışı ödüller çalışmıyor:**
- `offline-handling.enabled: true` olduğunu doğrulayın
- `pending-rewards.yml` dosyasının var olduğunu kontrol edin
- Oyuncunun kazandıktan sonra katıldığından emin olun

**Komutlar çalışmıyor:**
- Komutları konsolda manuel test edin
- Gerekli eklentilerin yüklendiğini kontrol edin
- Placeholder değişimlerini doğrulayın
- Komut izinlerini kontrol edin

#### Hata Mesajları

**"Envanter dolu":**
- Oyuncu envanteri dolu
- Eşyalar bekleyen ödüller olarak saklanır
- Oyuncu yer açması için bilgilendirilir

**"Komut başarısız":**
- Komut sözdizimini kontrol edin
- Gerekli eklentinin yüklü olduğunu doğrulayın
- Detaylı hata için konsolu kontrol edin

**"Ödül süresi doldu":**
- Bekleyen ödül `max-pending-days` süresini aştı
- Ödül otomatik olarak silindi
- Detaylar için ödül geçmişini kontrol edin

### En İyi Uygulamalar

#### Ödül Dengesi

- **Aşırı ödüllendirmeyin** - Ekonomiyi dengeli tutun
- **Zorlukla ölçeklendirin** - Zor etkinlikler = daha iyi ödüller
- **Sunucu ekonomisini düşünün** - Mevcut ödül seviyelerine uygun olun
- **Kapsamlı test edin** - Tüm ödüllerin çalıştığını doğrulayın

#### Performans

- **Komut ödüllerini sınırlayın** - Çok fazla komut gecikmeye neden olabilir
- **Verimli eşyalar kullanın** - Karmaşık NBT verilerinden kaçının
- **Bekleyen ödülleri izleyin** - Eski ödülleri düzenli temizleyin
- **Toplu işlemler** - Benzer ödülleri gruplandırın