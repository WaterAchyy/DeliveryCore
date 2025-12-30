# DeliveryCore - Ürün Bağlamı

## Neden Bu Proje Var?

### Problem
Minecraft sunucularında oyuncu etkileşimini artırmak zor. Standart survival oynanışı zamanla sıkıcı hale geliyor. Sunucu sahipleri oyuncuları aktif tutmak için etkinlik sistemlerine ihtiyaç duyuyor.

### Çözüm
DeliveryCore, zamanlanmış teslimat etkinlikleri ile oyunculara rekabetçi bir deneyim sunar. Belirli eşyaları toplama ve teslim etme yarışması, oyuncuları motive eder.

## Nasıl Çalışır?

### Etkinlik Akışı
1. Zamanlayıcı etkinliği başlatır (örn: her gün 20:00)
2. Sistem rastgele veya sabit kategori/eşya seçer
3. Tüm oyunculara duyuru yapılır (broadcast + title + ses)
4. Discord'a başlangıç bildirimi gönderilir
5. Oyuncular `/teslimat` GUI'si veya `/teslim` komutu ile eşya teslim eder
6. Gerçek zamanlı sıralama güncellenir
7. Son X dakika kala Discord'a uyarı gönderilir
8. Süre dolunca etkinlik biter, kazananlar ödüllendirilir
9. Discord'a sonuçlar paylaşılır

### Teslimat Yöntemleri
- **GUI**: `/teslimat` → Etkinlik seç → Envanterden teslim
- **Hızlı**: `/teslim` → Otomatik envanter tarama
- **Sandık**: `/teslim sandik` → Sandığa tıkla, içindekiler teslim edilir

## Kullanıcı Deneyimi Hedefleri

### Oyuncular İçin
- Basit ve anlaşılır GUI
- Hızlı teslimat seçenekleri
- Anlık geri bildirim (ses, title, mesaj)
- Adil sıralama sistemi
- Çeşitli ödül seçenekleri

### Sunucu Sahipleri İçin
- Kolay kurulum (drag & drop)
- Esnek konfigürasyon
- Türkçe dökümantasyon
- Discord entegrasyonu
- Düşük kaynak kullanımı
- GUI özelleştirme (ItemsAdder/Oraxen/Nexo desteği)
- PlaceholderAPI ile scoreboard/hologram entegrasyonu

## Rekabet Avantajları
- Türkçe dil desteği (yerli pazar)
- Kapsamlı kategori sistemi
- Sezonluk etkinlik desteği
- Modern GUI tasarımı (özelleştirilebilir)
- ItemsAdder/Oraxen/Nexo entegrasyonu
- PlaceholderAPI desteği
- Aktif geliştirme
