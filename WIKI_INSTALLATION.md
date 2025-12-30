# Installation Guide | Kurulum Rehberi

## Requirements | Gereksinimler

- **Minecraft Server:** 1.16.5 - 1.20.4
- **Server Software:** Spigot, Paper, Purpur, or Bukkit
- **Java:** 8+ (recommended: 17+)
- **RAM:** Minimum 1GB available

## Step 1: Download | Adım 1: İndirme

### Option A: GitHub Releases | Seçenek A: GitHub Sürümleri
1. Go to [Releases](https://github.com/WaterAchyy/DeliveryCore/releases)
2. Download the latest `deliverycore-x.x.x.jar` file

### Option B: SpigotMC | Seçenek B: SpigotMC
1. Visit [SpigotMC Page](https://www.spigotmc.org/resources/deliverycore)
2. Click "Download Now"

### Option C: Modrinth | Seçenek C: Modrinth
1. Visit [Modrinth Page](https://modrinth.com/plugin/deliverycore)
2. Click "Download"

## Step 2: Installation | Adım 2: Kurulum

1. **Stop your server** | **Sunucunuzu durdurun**
2. **Place the JAR file** in your `plugins` folder | **JAR dosyasını** `plugins` klasörüne koyun
3. **Start your server** | **Sunucunuzu başlatın**
4. **Wait for first-time setup** | **İlk kurulum için bekleyin**

## Step 3: First Launch | Adım 3: İlk Başlatma

When you start the server for the first time, DeliveryCore will:

İlk kez sunucuyu başlattığınızda, DeliveryCore şunları yapacak:

- Create configuration files | Yapılandırma dosyalarını oluşturacak
- Generate default categories | Varsayılan kategorileri oluşturacak
- Set up language files | Dil dosyalarını kuracak
- Create example deliveries | Örnek teslimatlar oluşturacak

## Step 4: Basic Configuration | Adım 4: Temel Yapılandırma

1. **Stop the server** | **Sunucuyu durdurun**
2. **Navigate to** `plugins/DeliveryCore/` | `plugins/DeliveryCore/` **klasörüne gidin**
3. **Edit configuration files:** | **Yapılandırma dosyalarını düzenleyin:**
   - `config.yml` - Main settings | Ana ayarlar
   - `deliveries.yml` - Delivery events | Teslimat etkinlikleri
   - `categories.yml` - Item categories | Eşya kategorileri

## Step 5: Discord Setup (Optional) | Adım 5: Discord Kurulumu (İsteğe Bağlı)

1. **Create a Discord webhook:**
   - Go to your Discord server
   - Channel Settings → Integrations → Webhooks
   - Create New Webhook
   - Copy the webhook URL

2. **Configure webhook in DeliveryCore:**
   ```yaml
   webhook:
     enabled: true
     url: "YOUR_WEBHOOK_URL_HERE"
   ```

## Step 6: Start Server | Adım 6: Sunucuyu Başlatın

1. **Start your server** | **Sunucunuzu başlatın**
2. **Test the plugin:** | **Eklentiyi test edin:**
   - Run `/dc info` to check status | Durumu kontrol etmek için `/dc info` çalıştırın
   - Run `/teslimat` to open GUI | GUI'yi açmak için `/teslimat` çalıştırın

## Verification | Doğrulama

If installation was successful, you should see:

Kurulum başarılıysa, şunları görmelisiniz:

- Plugin loads without errors | Eklenti hatasız yüklenir
- Configuration files are created | Yapılandırma dosyları oluşturulur
- Commands work properly | Komutlar düzgün çalışır
- GUI opens correctly | GUI doğru açılır

## Troubleshooting | Sorun Giderme

### Common Issues | Yaygın Sorunlar

**Plugin doesn't load:**
- Check Java version (8+ required)
- Verify server software compatibility
- Check console for error messages

**Commands don't work:**
- Verify permissions are set correctly
- Check if plugin is enabled: `/plugins`
- Restart server after configuration changes

**GUI doesn't open:**
- Check for conflicting plugins
- Verify player has `deliverycore.use` permission
- Check console for GUI-related errors

### Getting Help | Yardım Alma

If you encounter issues:

Sorunlarla karşılaşırsanız:

1. Check the [Troubleshooting Guide](Troubleshooting)
2. Search existing [GitHub Issues](https://github.com/WaterAchyy/DeliveryCore/issues)
3. Create a new issue with:
   - Server version and software
   - Plugin version
   - Error logs
   - Steps to reproduce

## Next Steps | Sonraki Adımlar

- [Basic Configuration](Configuration)
- [Setting up Categories](Categories)
- [Creating Deliveries](Deliveries)
- [Discord Integration](Discord-Webhooks)