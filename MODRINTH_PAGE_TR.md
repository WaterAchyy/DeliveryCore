# DeliveryCore

**Minecraft Sunucuları için Profesyonel Teslimat Etkinlik Sistemi**

Sunucunuzu eğlenceli teslimat etkinlikleriyle dönüştürün! Oyuncular eşya toplayıp teslim ederek ödüller kazanır, sıralamalarda yarışır ve zamanlanmış veya manuel etkinliklere katılır.

---

## 📋 Desteklenen Loader'lar ve Sürümler

### Loader'lar
- **Spigot**
- **Paper**
- **Purpur**
- **Bukkit**

### Minecraft Sürümleri
- 1.16.5
- 1.17.1
- 1.18.2
- 1.19.4
- 1.20.1
- 1.20.2
- 1.20.4

### Gereksinimler
- **Java:** 17+
- **Opsiyonel:** Vault (ekonomi), PlaceholderAPI (placeholder'lar)

---

## ✨ Özellikler

### 📦 Teslimat Etkinlikleri
- **Zamanlanmış Etkinlikler** - Doğal dil ile zamanlama ("every day at 18:00", "every monday at 20:00")
- **Manuel Etkinlikler** - Komutlarla anında etkinlik başlatma
- **Çoklu Eşzamanlı Etkinlik** - Aynı anda 5'e kadar etkinlik
- **Rastgele Eşya Seçimi** - Kategorilerden rastgele eşya belirleme
- **Kazanan Sistemi** - Etkinlik başına ayarlanabilir kazanan sayısı

### 🎨 Güzel Arayüz
- **Özel Kafa Texture'ları** - Tüm menü öğeleri için benzersiz ikonlar
- **SmallCaps Tipografi** - Modern, şık metin formatı
- **Gerçek Zamanlı Güncelleme** - Canlı sıralama ve teslimat sayıları
- **Kolay Navigasyon** - Kullanımı kolay menü sistemi

### 💰 Ödül Sistemi
- **Envanter Ödülleri** - Oyunculara doğrudan eşya verme
- **Komut Ödülleri** - Ödül olarak herhangi bir komut çalıştırma
- **Ekonomi Entegrasyonu** - Eşyaları paraya satma (Vault desteği)
- **Bekleyen Ödüller** - Çevrimdışı oyuncular sonraki girişte ödül alır

### 🔔 Discord Webhook Entegrasyonu
- **Etkinlik Başlangıç Bildirimi** - Etkinlik başladığında duyuru
- **Etkinlik Bitiş Bildirimi** - Kazananları sıralamayla gösterme
- **Özelleştirilebilir Embed'ler** - Renk, başlık, açıklama tam kontrolü
- **Kazanan Madalyaları** - 🥇🥈🥉 sıralama gösterimi

### 🌍 Çoklu Dil Desteği
- **Türkçe (tr)** - Tam Türkçe çeviri
- **İngilizce (en)** - Tam İngilizce çeviri
- **Kolay Ekleme** - Basit YAML tabanlı dil dosyaları

### ⚙️ Tam Özelleştirilebilir
- **500+ Eşya** - Tüm Minecraft eşyaları Türkçe isim ve fiyatlarla
- **17 Kategori** - Çiftlik, Maden, Blok, Yiyecek, Odun, Alet, Zırh ve daha fazlası
- **Özel Teslimatlar** - Sınırsız teslimat etkinlik türü oluşturma
- **Görünen İsimler** - Tüm kategori ve teslimat isimlerini özelleştirme

### 📊 Gelişmiş Özellikler
- **PlaceholderAPI Desteği** - Diğer pluginler için 12+ placeholder
- **Sandıktan Teslimat** - Sandıklardan doğrudan teslimat
- **Envanterden Teslimat** - Tek tıkla tüm eşleşen eşyaları teslim etme
- **Veri Kalıcılığı** - Etkinlikler sunucu yeniden başlatmalarında korunur
- **Debug Modu** - Sorun giderme için detaylı loglama

---

## 📋 Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/dc` veya `/teslimat` | Teslimat GUI'sini aç | `deliverycore.use` |
| `/dc reload` | Tüm ayarları yeniden yükle | `deliverycore.admin` |
| `/dc start <isim>` | Manuel etkinlik başlat | `deliverycore.admin` |
| `/dc stop <isim>` | Aktif etkinliği durdur | `deliverycore.admin` |
| `/dc status` | Aktif etkinlikleri görüntüle | `deliverycore.admin` |
| `/dc webhooktest` | Discord webhook'u test et | `deliverycore.admin` |

---

## 🔧 Yetkiler

| Yetki | Açıklama | Varsayılan |
|-------|----------|------------|
| `deliverycore.use` | Teslimat GUI'sine erişim | true |
| `deliverycore.admin` | Admin komutları | op |
| `deliverycore.bypass.protection` | Sandık korumasını atla | op |

---

## 📁 Yapılandırma Dosyaları

- `config.yml` - Ana ayarlar, webhook yapılandırması, görünen isimler
- `categories.yml` - Ağırlıklı eşya kategorileri
- `deliveries.yml` - Teslimat etkinlik tanımları
- `items.yml` - 500+ eşya Türkçe isim ve fiyatlarla
- `lang/tr.yml` - Türkçe mesajlar
- `lang/en.yml` - İngilizce mesajlar

---

## 🎯 PlaceholderAPI Placeholder'ları

```
%deliverycore_active_count% - Aktif etkinlik sayısı
%deliverycore_active_names% - Aktif etkinlik isimleri
%deliverycore_player_total% - Oyuncunun toplam teslimatı
%deliverycore_player_rank% - Oyuncunun mevcut sırası
%deliverycore_event_item% - Mevcut etkinliğin istenen eşyası
%deliverycore_event_category% - Mevcut etkinliğin kategorisi
%deliverycore_event_remaining% - Kalan süre
%deliverycore_leaderboard% - En iyi oyuncular sıralaması
%deliverycore_top_1_name% - 1. oyuncu ismi
%deliverycore_top_1_count% - 1. oyuncu teslimat sayısı
```

---

## 📥 Kurulum

1. Plugin JAR dosyasını indirin
2. `plugins` klasörüne yerleştirin
3. Sunucuyu yeniden başlatın
4. `plugins/DeliveryCore/` içindeki ayarları düzenleyin
5. Değişiklikleri uygulamak için `/dc reload` kullanın

---

## 🔗 Gereksinimler

- **Minecraft:** 1.16.5 - 1.20.4
- **Java:** 17+
- **Opsiyonel:** Vault (ekonomi için), PlaceholderAPI (placeholder'lar için)

---

## 📸 Ekran Görüntüleri

*Yakında*

---

## 💬 Destek

- **Discord:** [Yakında]
- **GitHub Issues:** [Hataları buradan bildirin]

---

## 📜 Lisans

Bu proje MIT Lisansı altında lisanslanmıştır.

---

**Minecraft sunucuları için ❤️ ile yapıldı**
