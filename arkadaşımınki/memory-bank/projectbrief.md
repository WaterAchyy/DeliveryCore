# DeliveryCore - Proje Özeti

## Proje Adı
DeliveryCore - Minecraft Teslimat Etkinlik Sistemi

## Versiyon
v1.1.0

## Açıklama
Minecraft sunucuları için profesyonel teslimat etkinlik sistemi. Oyuncular belirli eşyaları toplayıp teslim ederek yarışır, sıralamaya göre ödül kazanır.

## Temel Gereksinimler

### Fonksiyonel
- Zamanlanmış teslimat etkinlikleri (günlük, haftalık, aylık)
- Doğal dil zamanlama ("every day at 18:00")
- Kategori bazlı eşya sistemi (12+ kategori)
- Gerçek zamanlı sıralama ve liderboard
- Ödül sistemi (item + komut)
- Discord webhook bildirimleri (başlangıç, bitiş, son dakika uyarısı)
- Çoklu dil desteği (TR/EN)
- PlaceholderAPI entegrasyonu

### v1.1 Özellikleri
- Sezonluk etkinlik sistemi
- CustomModelData desteği
- ItemsAdder/Oraxen/Nexo entegrasyonu
- Özelleştirilebilir GUI sistemi (gui.yml)
- Ayrı webhook config dosyası (webhooks/discord.yml)

### Teknik
- Minecraft 1.21.x uyumluluk (Spigot API 1.21)
- Spigot/Paper/Purpur desteği
- Java 21
- Vault ekonomi entegrasyonu (opsiyonel)
- PlaceholderAPI entegrasyonu (opsiyonel)
- Offline oyuncu ödül desteği

## Hedef Kitle
- Minecraft sunucu sahipleri
- Türk ve uluslararası topluluklar

## Başarı Kriterleri
- Stabil çalışma (crash yok)
- Düşük TPS etkisi
- Kolay konfigürasyon
- Kapsamlı dökümantasyon
