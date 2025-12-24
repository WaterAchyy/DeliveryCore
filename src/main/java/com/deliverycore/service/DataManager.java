package com.deliverycore.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Veri kalıcılığı yöneticisi.
 * Aktif etkinlikleri, oyuncu teslimatlarını ve sıralamaları kaydeder/yükler.
 */
public class DataManager {
    
    private static final String DATA_FILE = "data.yml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    
    private final File dataFolder;
    private final Logger logger;
    private File dataFile;
    private YamlConfiguration data;
    
    public DataManager(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.dataFile = new File(dataFolder, DATA_FILE);
        loadData();
    }
    
    /**
     * Veri dosyasını yükler veya oluşturur.
     */
    public void loadData() {
        if (!dataFile.exists()) {
            data = new YamlConfiguration();
            saveData();
            logger.info("[DataManager] Yeni veri dosyası oluşturuldu: " + DATA_FILE);
        } else {
            data = YamlConfiguration.loadConfiguration(dataFile);
            logger.info("[DataManager] Veri dosyası yüklendi: " + DATA_FILE);
        }
    }
    
    /**
     * Veri dosyasını kaydeder.
     */
    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            logger.warning("[DataManager] Veri kaydedilemedi: " + e.getMessage());
        }
    }

    
    // ═══════════════════════════════════════════════════════════════
    // AKTİF ETKİNLİK KAYDETME/YÜKLEME
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Aktif etkinliği kaydeder.
     */
    public void saveActiveEvent(ActiveEvent event) {
        String path = "active-events." + event.getDeliveryName();
        
        data.set(path + ".category", event.getResolvedCategory());
        data.set(path + ".item", event.getResolvedItem());
        data.set(path + ".start-time", event.getStartTime() != null ? event.getStartTime().format(DATE_FORMAT) : null);
        data.set(path + ".end-time", event.getEndTime() != null ? event.getEndTime().format(DATE_FORMAT) : null);
        data.set(path + ".timezone", event.getTimezone() != null ? event.getTimezone().getId() : "Europe/Istanbul");
        
        // Oyuncu teslimatlarını kaydet
        ConfigurationSection deliveries = data.createSection(path + ".player-deliveries");
        for (Map.Entry<UUID, Integer> entry : event.getPlayerDeliveries().entrySet()) {
            deliveries.set(entry.getKey().toString(), entry.getValue());
        }
        
        saveData();
        logger.info("[DataManager] Etkinlik kaydedildi: " + event.getDeliveryName());
    }
    
    /**
     * Tüm aktif etkinlikleri kaydeder.
     */
    public void saveAllActiveEvents(List<ActiveEvent> events) {
        // Önce eski verileri temizle
        data.set("active-events", null);
        
        for (ActiveEvent event : events) {
            saveActiveEvent(event);
        }
        
        logger.info("[DataManager] " + events.size() + " aktif etkinlik kaydedildi.");
    }
    
    /**
     * Kaydedilmiş aktif etkinlikleri yükler.
     */
    public List<SavedEventData> loadActiveEvents() {
        List<SavedEventData> events = new ArrayList<>();
        
        ConfigurationSection section = data.getConfigurationSection("active-events");
        if (section == null) return events;
        
        for (String deliveryName : section.getKeys(false)) {
            try {
                String path = "active-events." + deliveryName;
                
                String category = data.getString(path + ".category");
                String item = data.getString(path + ".item");
                String startTimeStr = data.getString(path + ".start-time");
                String endTimeStr = data.getString(path + ".end-time");
                String timezoneStr = data.getString(path + ".timezone", "Europe/Istanbul");
                
                ZoneId timezone = ZoneId.of(timezoneStr);
                ZonedDateTime startTime = startTimeStr != null ? ZonedDateTime.parse(startTimeStr, DATE_FORMAT) : null;
                ZonedDateTime endTime = endTimeStr != null ? ZonedDateTime.parse(endTimeStr, DATE_FORMAT) : null;
                
                // Oyuncu teslimatlarını yükle
                Map<UUID, Integer> playerDeliveries = new HashMap<>();
                ConfigurationSection deliveriesSection = data.getConfigurationSection(path + ".player-deliveries");
                if (deliveriesSection != null) {
                    for (String uuidStr : deliveriesSection.getKeys(false)) {
                        try {
                            UUID uuid = UUID.fromString(uuidStr);
                            int count = deliveriesSection.getInt(uuidStr);
                            playerDeliveries.put(uuid, count);
                        } catch (Exception ignored) {}
                    }
                }
                
                events.add(new SavedEventData(deliveryName, category, item, startTime, endTime, timezone, playerDeliveries));
                logger.info("[DataManager] Etkinlik yüklendi: " + deliveryName);
                
            } catch (Exception e) {
                logger.warning("[DataManager] Etkinlik yüklenemedi: " + deliveryName + " - " + e.getMessage());
            }
        }
        
        return events;
    }
    
    /**
     * Aktif etkinliği siler.
     */
    public void removeActiveEvent(String deliveryName) {
        data.set("active-events." + deliveryName, null);
        saveData();
    }

    
    // ═══════════════════════════════════════════════════════════════
    // OYUNCU VERİLERİ
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Oyuncunun toplam teslimat sayısını günceller.
     */
    public void updatePlayerStats(UUID playerUuid, String playerName, int deliveryCount) {
        String path = "player-stats." + playerUuid.toString();
        
        int current = data.getInt(path + ".total-deliveries", 0);
        data.set(path + ".total-deliveries", current + deliveryCount);
        data.set(path + ".last-name", playerName);
        data.set(path + ".last-active", ZonedDateTime.now().format(DATE_FORMAT));
        
        saveData();
    }
    
    /**
     * Oyuncunun toplam teslimat sayısını döndürür.
     */
    public int getPlayerTotalDeliveries(UUID playerUuid) {
        return data.getInt("player-stats." + playerUuid.toString() + ".total-deliveries", 0);
    }
    
    /**
     * Tüm oyuncu istatistiklerini döndürür.
     */
    public Map<UUID, PlayerStats> getAllPlayerStats() {
        Map<UUID, PlayerStats> stats = new HashMap<>();
        
        ConfigurationSection section = data.getConfigurationSection("player-stats");
        if (section == null) return stats;
        
        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String path = "player-stats." + uuidStr;
                
                String name = data.getString(path + ".last-name", "Unknown");
                int total = data.getInt(path + ".total-deliveries", 0);
                
                stats.put(uuid, new PlayerStats(uuid, name, total));
            } catch (Exception ignored) {}
        }
        
        return stats;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // YARDIMCI SINIFLAR
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Kaydedilmiş etkinlik verisi.
     */
    public record SavedEventData(
        String deliveryName,
        String category,
        String item,
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        ZoneId timezone,
        Map<UUID, Integer> playerDeliveries
    ) {
        /**
         * Bu veriden ActiveEvent oluşturur.
         */
        public ActiveEvent toActiveEvent() {
            ActiveEvent event = new ActiveEvent(deliveryName, category, item, startTime, endTime, timezone);
            // Oyuncu teslimatlarını geri yükle
            for (Map.Entry<UUID, Integer> entry : playerDeliveries.entrySet()) {
                event.recordDelivery(entry.getKey(), entry.getValue());
            }
            return event;
        }
    }
    
    /**
     * Oyuncu istatistikleri.
     */
    public record PlayerStats(UUID uuid, String name, int totalDeliveries) {}
}
