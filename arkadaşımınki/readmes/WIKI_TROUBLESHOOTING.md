e# Troubleshooting | Sorun Giderme

## English

This guide helps you diagnose and fix common issues with DeliveryCore.

### Common Issues

#### Plugin Not Loading

**Symptoms:**
- Plugin doesn't appear in `/plugins` list
- No DeliveryCore commands available
- Console shows loading errors

**Solutions:**

1. **Check Java Version**
   ```bash
   java -version
   ```
   - Requires Java 8 or higher
   - Recommended: Java 17+

2. **Verify Server Compatibility**
   - Spigot 1.16.5 - 1.20.4
   - Paper, Purpur, Bukkit supported
   - Check server version: `/version`

3. **Check Console Errors**
   - Look for red error messages during startup
   - Common errors and solutions below

4. **File Permissions**
   - Ensure JAR file has read permissions
   - Check plugins folder permissions

#### Commands Not Working

**Symptoms:**
- `/teslimat` or `/delivery` shows "Unknown command"
- Permission denied messages
- Commands exist but don't respond

**Solutions:**

1. **Check Plugin Status**
   ```bash
   /plugins
   ```
   - DeliveryCore should be green
   - If red, check console for errors

2. **Verify Permissions**
   ```bash
   # Basic permissions
   /lp user <player> permission set deliverycore.use true
   /lp user <player> permission set deliverycore.deliver true
   
   # Admin permissions
   /lp user <player> permission set deliverycore.admin true
   ```

3. **Check Command Aliases**
   - Turkish: `/teslimat`, `/teslim`
   - English: `/delivery`, `/deliver`
   - Admin: `/dc`, `/deliverycore`

#### GUI Not Opening

**Symptoms:**
- Commands work but GUI doesn't open
- Blank inventory opens
- Error messages in console

**Solutions:**

1. **Check Permissions**
   ```yaml
   deliverycore.use: true
   deliverycore.gui.use: true
   ```

2. **Inventory Space**
   - Player inventory must not be full
   - Close other GUIs before opening

3. **Plugin Conflicts**
   - Disable other GUI plugins temporarily
   - Check for inventory management conflicts

4. **Custom Heads**
   - Requires internet connection for head textures
   - May take time to load on first use

#### Events Not Starting

**Symptoms:**
- Scheduled events don't start automatically
- Manual start commands fail
- No event announcements

**Solutions:**

1. **Check Configuration**
   ```yaml
   # Verify schedule format
   schedule: "every day at 18:00"
   
   # Check timezone
   general:
     timezone: "Europe/Istanbul"
   ```

2. **Verify Categories**
   - Ensure category exists in `categories.yml`
   - Check category has items defined

3. **Check Server Time**
   ```bash
   /time
   /dc info  # Shows server timezone
   ```

4. **Console Errors**
   - Look for scheduling errors
   - Check category loading messages

#### Discord Webhook Issues

**Symptoms:**
- No Discord notifications
- Webhook errors in console
- Messages not formatted correctly

**Solutions:**

1. **Verify Webhook URL**
   ```yaml
   webhook:
     enabled: true
     url: "https://discord.com/api/webhooks/ID/TOKEN"
   ```

2. **Test Webhook**
   ```bash
   /dc webhook test
   /dc webhook test "Test message"
   ```

3. **Check Discord Permissions**
   - Webhook must have send message permissions
   - Channel must allow webhooks

4. **Rate Limiting**
   - Discord limits 30 requests per minute
   - Reduce webhook frequency if needed

### Error Messages

#### "Could not load categories.yml"

**Cause:** YAML syntax error in categories configuration

**Solution:**
1. Check YAML syntax with online validator
2. Verify indentation (use spaces, not tabs)
3. Check for special characters
4. Restore from backup if needed

```yaml
# Correct format
farm:
  display-name: "Farm"
  items:
    - WHEAT
    - CARROT
```

#### "No active delivery event"

**Cause:** No events are currently running

**Solution:**
1. Start event manually: `/dc start <name>`
2. Check event schedules in `deliveries.yml`
3. Verify events are enabled in config

#### "Player inventory full"

**Cause:** Player has no space for reward items

**Solution:**
1. Items are stored as pending rewards
2. Player will be notified to make space
3. Use `/delivery claim` when inventory has space

#### "Webhook rate limited"

**Cause:** Too many Discord webhook requests

**Solution:**
1. Wait 1 minute for rate limit reset
2. Reduce webhook frequency in config
3. Disable unnecessary webhook events

#### "Invalid schedule format"

**Cause:** Incorrect schedule syntax in deliveries.yml

**Solution:**
```yaml
# Correct formats
schedule: "every day at 18:00"
schedule: "every 2 hours"
schedule: "every monday at 19:00"
schedule: "every hour from 10:00 to 22:00"
```

### Performance Issues

#### High Memory Usage

**Symptoms:**
- Server lag during events
- Out of memory errors
- Slow GUI responses

**Solutions:**

1. **Reduce Concurrent Events**
   ```yaml
   events:
     max-concurrent: 3  # Reduce from 5
   ```

2. **Optimize Leaderboard**
   ```yaml
   leaderboard:
     max-entries: 50  # Reduce from 100
     update-interval: 60  # Increase from 30
   ```

3. **Clean Old Data**
   ```bash
   /dc cleanup old-data
   /dc cleanup pending-rewards
   ```

#### Slow Database Operations

**Symptoms:**
- Delays when checking player stats
- Slow leaderboard updates
- Timeout errors

**Solutions:**

1. **Increase Auto-save Interval**
   ```yaml
   data:
     auto-save-interval: 10  # Increase from 5 minutes
   ```

2. **Optimize Data Storage**
   - Use SQLite for better performance
   - Regular database maintenance

3. **Reduce Data Retention**
   ```yaml
   data:
     keep-history-days: 30  # Reduce from 90
   ```

### Configuration Issues

#### YAML Syntax Errors

**Common Mistakes:**

1. **Incorrect Indentation**
   ```yaml
   # Wrong (tabs used)
   deliveries:
   	daily-farm:
   		name: "Daily Farm"
   
   # Correct (spaces used)
   deliveries:
     daily-farm:
       name: "Daily Farm"
   ```

2. **Missing Quotes**
   ```yaml
   # Wrong
   name: Daily Farm Event
   
   # Correct
   name: "Daily Farm Event"
   ```

3. **Invalid Characters**
   ```yaml
   # Wrong
   description: "Event with special chars: @#$%"
   
   # Correct
   description: "Event with special chars: @#$%"
   ```

#### Plugin Conflicts

**Common Conflicting Plugins:**

1. **Other Delivery Plugins**
   - Disable similar functionality
   - Check command conflicts

2. **GUI Plugins**
   - ChestCommands
   - DeluxeMenus
   - Custom inventory plugins

3. **Economy Plugins**
   - Ensure Vault compatibility
   - Check reward command syntax

### Debug Mode

Enable debug mode for detailed logging:

```yaml
general:
  debug: true
```

**Debug Information Includes:**
- Event scheduling details
- Player action logging
- Database operation timing
- Configuration loading steps

### Getting Help

#### Information to Provide

When asking for help, include:

1. **Server Information**
   ```bash
   /version
   /plugins
   ```

2. **Plugin Version**
   ```bash
   /dc version
   ```

3. **Configuration Files**
   - `config.yml`
   - `deliveries.yml` (relevant sections)
   - `categories.yml` (if category-related)

4. **Console Logs**
   - Startup logs
   - Error messages
   - Debug output (if enabled)

5. **Steps to Reproduce**
   - What you were trying to do
   - What happened instead
   - When the issue started

#### Support Channels

1. **GitHub Issues**
   - [Create new issue](https://github.com/WaterAchyy/DeliveryCore/issues/new)
   - Search existing issues first

2. **Discord Support**
   - Join our Discord server
   - Use #support channel

3. **SpigotMC Discussion**
   - Plugin discussion page
   - Community help

### Maintenance Commands

#### Regular Maintenance

```bash
# Reload configuration
/dc reload

# Check system status
/dc info

# Clean old data
/dc cleanup

# Test webhook
/dc webhook test

# Backup data
/dc backup create
```

#### Emergency Commands

```bash
# Stop all events
/dc stop all

# Clear all pending rewards
/dc reward clear all

# Reset player data (use carefully!)
/dc reset player <name>

# Force save data
/dc save force
```

---

## Türkçe

Bu rehber DeliveryCore ile yaygın sorunları teşhis etmenize ve çözmenize yardımcı olur.

### Yaygın Sorunlar

#### Eklenti Yüklenmiyor

**Belirtiler:**
- Eklenti `/plugins` listesinde görünmüyor
- DeliveryCore komutları mevcut değil
- Konsol yükleme hataları gösteriyor

**Çözümler:**

1. **Java Sürümünü Kontrol Edin**
   ```bash
   java -version
   ```
   - Java 8 veya üstü gerekli
   - Önerilen: Java 17+

2. **Sunucu Uyumluluğunu Doğrulayın**
   - Spigot 1.16.5 - 1.20.4
   - Paper, Purpur, Bukkit destekleniyor
   - Sunucu sürümünü kontrol edin: `/version`

3. **Konsol Hatalarını Kontrol Edin**
   - Başlangıç sırasında kırmızı hata mesajlarını arayın
   - Yaygın hatalar ve çözümleri aşağıda

4. **Dosya İzinleri**
   - JAR dosyasının okuma izinleri olduğundan emin olun
   - Plugins klasörü izinlerini kontrol edin

#### Komutlar Çalışmıyor

**Belirtiler:**
- `/teslimat` veya `/delivery` "Bilinmeyen komut" gösteriyor
- İzin reddedildi mesajları
- Komutlar var ama yanıt vermiyor

**Çözümler:**

1. **Eklenti Durumunu Kontrol Edin**
   ```bash
   /plugins
   ```
   - DeliveryCore yeşil olmalı
   - Kırmızıysa konsol hatalarını kontrol edin

2. **İzinleri Doğrulayın**
   ```bash
   # Temel izinler
   /lp user <oyuncu> permission set deliverycore.use true
   /lp user <oyuncu> permission set deliverycore.deliver true
   
   # Yönetici izinleri
   /lp user <oyuncu> permission set deliverycore.admin true
   ```

3. **Komut Takma Adlarını Kontrol Edin**
   - Türkçe: `/teslimat`, `/teslim`
   - İngilizce: `/delivery`, `/deliver`
   - Yönetici: `/dc`, `/deliverycore`

#### GUI Açılmıyor

**Belirtiler:**
- Komutlar çalışıyor ama GUI açılmıyor
- Boş envanter açılıyor
- Konsolda hata mesajları

**Çözümler:**

1. **İzinleri Kontrol Edin**
   ```yaml
   deliverycore.use: true
   deliverycore.gui.use: true
   ```

2. **Envanter Alanı**
   - Oyuncu envanteri dolu olmamalı
   - GUI açmadan önce diğer GUI'leri kapatın

3. **Eklenti Çakışmaları**
   - Diğer GUI eklentilerini geçici olarak devre dışı bırakın
   - Envanter yönetimi çakışmalarını kontrol edin

4. **Özel Kafalar**
   - Kafa dokuları için internet bağlantısı gerekli
   - İlk kullanımda yüklenmesi zaman alabilir

#### Etkinlikler Başlamıyor

**Belirtiler:**
- Zamanlanmış etkinlikler otomatik başlamıyor
- Manuel başlatma komutları başarısız
- Etkinlik duyuruları yok

**Çözümler:**

1. **Yapılandırmayı Kontrol Edin**
   ```yaml
   # Zamanlama formatını doğrulayın
   schedule: "every day at 18:00"
   
   # Saat dilimini kontrol edin
   general:
     timezone: "Europe/Istanbul"
   ```

2. **Kategorileri Doğrulayın**
   - Kategorinin `categories.yml` dosyasında var olduğundan emin olun
   - Kategorinin tanımlanmış eşyaları olduğunu kontrol edin

3. **Sunucu Saatini Kontrol Edin**
   ```bash
   /time
   /dc info  # Sunucu saat dilimini gösterir
   ```

4. **Konsol Hataları**
   - Zamanlama hatalarını arayın
   - Kategori yükleme mesajlarını kontrol edin

#### Discord Webhook Sorunları

**Belirtiler:**
- Discord bildirimleri yok
- Konsolda webhook hataları
- Mesajlar doğru formatlanmıyor

**Çözümler:**

1. **Webhook URL'sini Doğrulayın**
   ```yaml
   webhook:
     enabled: true
     url: "https://discord.com/api/webhooks/ID/TOKEN"
   ```

2. **Webhook'u Test Edin**
   ```bash
   /dc webhook test
   /dc webhook test "Test mesajı"
   ```

3. **Discord İzinlerini Kontrol Edin**
   - Webhook'un mesaj gönderme izni olmalı
   - Kanal webhook'lara izin vermeli

4. **Hız Sınırlaması**
   - Discord dakikada 30 istekle sınırlar
   - Gerekirse webhook sıklığını azaltın

### Hata Mesajları

#### "Could not load categories.yml"

**Neden:** categories yapılandırmasında YAML sözdizimi hatası

**Çözüm:**
1. YAML sözdizimini çevrimiçi doğrulayıcı ile kontrol edin
2. Girintilemeyi doğrulayın (tab değil boşluk kullanın)
3. Özel karakterleri kontrol edin
4. Gerekirse yedekten geri yükleyin

```yaml
# Doğru format
farm:
  display-name: "Çiftlik"
  items:
    - WHEAT
    - CARROT
```

#### "No active delivery event"

**Neden:** Şu anda çalışan etkinlik yok

**Çözüm:**
1. Etkinliği manuel başlatın: `/dc start <isim>`
2. `deliveries.yml` dosyasındaki etkinlik zamanlamalarını kontrol edin
3. Etkinliklerin yapılandırmada etkin olduğunu doğrulayın

#### "Player inventory full"

**Neden:** Oyuncunun ödül eşyaları için yeri yok

**Çözüm:**
1. Eşyalar bekleyen ödüller olarak saklanır
2. Oyuncu yer açması için bilgilendirilir
3. Envanterde yer olduğunda `/delivery claim` kullanın

#### "Webhook rate limited"

**Neden:** Çok fazla Discord webhook isteği

**Çözüm:**
1. Hız sınırının sıfırlanması için 1 dakika bekleyin
2. Yapılandırmada webhook sıklığını azaltın
3. Gereksiz webhook etkinliklerini devre dışı bırakın

#### "Invalid schedule format"

**Neden:** deliveries.yml dosyasında yanlış zamanlama sözdizimi

**Çözüm:**
```yaml
# Doğru formatlar
schedule: "every day at 18:00"
schedule: "every 2 hours"
schedule: "every monday at 19:00"
schedule: "every hour from 10:00 to 22:00"
```

### Performans Sorunları

#### Yüksek Bellek Kullanımı

**Belirtiler:**
- Etkinlikler sırasında sunucu gecikmesi
- Bellek yetersizliği hataları
- Yavaş GUI yanıtları

**Çözümler:**

1. **Eş Zamanlı Etkinlikleri Azaltın**
   ```yaml
   events:
     max-concurrent: 3  # 5'ten azaltın
   ```

2. **Sıralamayı Optimize Edin**
   ```yaml
   leaderboard:
     max-entries: 50  # 100'den azaltın
     update-interval: 60  # 30'dan artırın
   ```

3. **Eski Verileri Temizleyin**
   ```bash
   /dc cleanup old-data
   /dc cleanup pending-rewards
   ```

#### Yavaş Veritabanı İşlemleri

**Belirtiler:**
- Oyuncu istatistiklerini kontrol ederken gecikmeler
- Yavaş sıralama güncellemeleri
- Zaman aşımı hataları

**Çözümler:**

1. **Otomatik Kaydetme Aralığını Artırın**
   ```yaml
   data:
     auto-save-interval: 10  # 5 dakikadan artırın
   ```

2. **Veri Depolamayı Optimize Edin**
   - Daha iyi performans için SQLite kullanın
   - Düzenli veritabanı bakımı

3. **Veri Saklama Süresini Azaltın**
   ```yaml
   data:
     keep-history-days: 30  # 90'dan azaltın
   ```

### Yapılandırma Sorunları

#### YAML Sözdizimi Hataları

**Yaygın Hatalar:**

1. **Yanlış Girintileme**
   ```yaml
   # Yanlış (tab kullanılmış)
   deliveries:
   	gunluk-ciftlik:
   		name: "Günlük Çiftlik"
   
   # Doğru (boşluk kullanılmış)
   deliveries:
     gunluk-ciftlik:
       name: "Günlük Çiftlik"
   ```

2. **Eksik Tırnak İşaretleri**
   ```yaml
   # Yanlış
   name: Günlük Çiftlik Etkinliği
   
   # Doğru
   name: "Günlük Çiftlik Etkinliği"
   ```

3. **Geçersiz Karakterler**
   ```yaml
   # Yanlış
   description: "Özel karakterli etkinlik: @#$%"
   
   # Doğru
   description: "Özel karakterli etkinlik: @#$%"
   ```

#### Eklenti Çakışmaları

**Yaygın Çakışan Eklentiler:**

1. **Diğer Teslimat Eklentileri**
   - Benzer işlevselliği devre dışı bırakın
   - Komut çakışmalarını kontrol edin

2. **GUI Eklentileri**
   - ChestCommands
   - DeluxeMenus
   - Özel envanter eklentileri

3. **Ekonomi Eklentileri**
   - Vault uyumluluğunu sağlayın
   - Ödül komut sözdizimini kontrol edin

### Hata Ayıklama Modu

Detaylı günlükleme için hata ayıklama modunu etkinleştirin:

```yaml
general:
  debug: true
```

**Hata Ayıklama Bilgileri İçerir:**
- Etkinlik zamanlama detayları
- Oyuncu eylem günlükleri
- Veritabanı işlem zamanlaması
- Yapılandırma yükleme adımları

### Yardım Alma

#### Sağlanacak Bilgiler

Yardım isterken şunları ekleyin:

1. **Sunucu Bilgileri**
   ```bash
   /version
   /plugins
   ```

2. **Eklenti Sürümü**
   ```bash
   /dc version
   ```

3. **Yapılandırma Dosyaları**
   - `config.yml`
   - `deliveries.yml` (ilgili bölümler)
   - `categories.yml` (kategori ile ilgiliyse)

4. **Konsol Günlükleri**
   - Başlangıç günlükleri
   - Hata mesajları
   - Hata ayıklama çıktısı (etkinse)

5. **Yeniden Üretme Adımları**
   - Ne yapmaya çalışıyordunuz
   - Bunun yerine ne oldu
   - Sorun ne zaman başladı

#### Destek Kanalları

1. **GitHub Issues**
   - [Yeni issue oluştur](https://github.com/WaterAchyy/DeliveryCore/issues/new)
   - Önce mevcut issue'ları arayın

2. **Discord Desteği**
   - Discord sunucumuza katılın
   - #support kanalını kullanın

3. **SpigotMC Tartışması**
   - Eklenti tartışma sayfası
   - Topluluk yardımı

### Bakım Komutları

#### Düzenli Bakım

```bash
# Yapılandırmayı yeniden yükle
/dc reload

# Sistem durumunu kontrol et
/dc info

# Eski verileri temizle
/dc cleanup

# Webhook'u test et
/dc webhook test

# Veri yedekle
/dc backup create
```

#### Acil Durum Komutları

```bash
# Tüm etkinlikleri durdur
/dc stop all

# Tüm bekleyen ödülleri temizle
/dc reward clear all

# Oyuncu verilerini sıfırla (dikkatli kullanın!)
/dc reset player <isim>

# Veriyi zorla kaydet
/dc save force
```