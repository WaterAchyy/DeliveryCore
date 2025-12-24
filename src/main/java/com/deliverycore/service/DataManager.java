package com.deliverycore.service;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimize edilmiş Veri Yöneticisi.
 * - Asenkron otomatik kayıt (Auto-Save)
 * - Thread-safe veri yapıları
 * - Lag önleyici yapı
 */
public class DataManager {

    private static final String DATA_FILE = "data.yml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    private final JavaPlugin plugin;
    private final File dataFile;
    private final Logger logger;
    private YamlConfiguration data;

    private boolean isDirty = false;
    private final Object lock = new Object();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        loadData();
        startAutoSaveTask();
    }

    /**
     * Veri dosyasını yükler.
     */
    public void loadData() {
        synchronized (lock) {
            if (!dataFile.exists()) {
                try {
                    if (!dataFile.getParentFile().exists()) {
                        boolean ignored = dataFile.getParentFile().mkdirs();
                    }
                    boolean ignored = dataFile.createNewFile();
                    data = new YamlConfiguration();
                    saveDataSync();
                    logger.info("[DataManager] Yeni veri dosyası oluşturuldu: " + DATA_FILE);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Veri dosyası oluşturulamadı!", e);
                }
            } else {
                data = YamlConfiguration.loadConfiguration(dataFile);
                logger.info("[DataManager] Veri dosyası yüklendi: " + DATA_FILE);
            }
        }
    }

    /**
     * Otomatik kayıt görevini başlatır.
     * config.yml'den süre alınabilir, şimdilik sabit 5 dakika.
     */
    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (isDirty) {
                saveDataAsync();
            }
        }, 6000L, 6000L);
    }

    /**
     * Veriyi asenkron olarak kaydeder.
     */
    private void saveDataAsync() {
        synchronized (lock) {
            if (!isDirty) return;
            try {
                data.save(dataFile);
                isDirty = false;
            } catch (IOException e) {
                logger.warning("[DataManager] Asenkron kayıt hatası: " + e.getMessage());
            }
        }
    }

    /**
     * Veriyi senkron (anlık) olarak kaydeder.
     * Sunucu kapanırken çağrılmalı.
     */
    public void saveDataSync() {
        synchronized (lock) {
            try {
                data.save(dataFile);
                isDirty = false;
                logger.info("[DataManager] Veriler diske yazıldı.");
            } catch (IOException e) {
                logger.warning("[DataManager] Kayıt hatası: " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // AKTİF ETKİNLİK İŞLEMLERİ
    // ═══════════════════════════════════════════════════════════════

    public void saveActiveEvent(ActiveEvent event) {
        synchronized (lock) {
            String path = "active-events." + event.getDeliveryName();

            data.set(path + ".category", event.getResolvedCategory());
            data.set(path + ".item", event.getResolvedItem());
            data.set(path + ".start-time", event.getStartTime() != null ? event.getStartTime().format(DATE_FORMAT) : null);
            data.set(path + ".end-time", event.getEndTime() != null ? event.getEndTime().format(DATE_FORMAT) : null);
            data.set(path + ".timezone", event.getTimezone() != null ? event.getTimezone().getId() : "Europe/Istanbul");

            ConfigurationSection deliveries = data.createSection(path + ".player-deliveries");
            for (Map.Entry<UUID, Integer> entry : event.getPlayerDeliveries().entrySet()) {
                deliveries.set(entry.getKey().toString(), entry.getValue());
            }

            isDirty = true;
        }
    }

    public void saveAllActiveEvents(List<ActiveEvent> events) {
        synchronized (lock) {
            data.set("active-events", null);
            for (ActiveEvent event : events) {
                saveActiveEvent(event);
            }
        }
    }

    public List<SavedEventData> loadActiveEvents() {
        synchronized (lock) {
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
                } catch (Exception e) {
                    logger.warning("[DataManager] Etkinlik yüklenemedi: " + deliveryName + " - " + e.getMessage());
                }
            }
            return events;
        }
    }

    public void removeActiveEvent(String deliveryName) {
        synchronized (lock) {
            data.set("active-events." + deliveryName, null);
            isDirty = true;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // OYUNCU İSTATİSTİKLERİ
    // ═══════════════════════════════════════════════════════════════

    public void updatePlayerStats(UUID playerUuid, String playerName, int deliveryCount) {
        synchronized (lock) {
            String path = "player-stats." + playerUuid.toString();
            int current = data.getInt(path + ".total-deliveries", 0);

            data.set(path + ".total-deliveries", current + deliveryCount);
            data.set(path + ".last-name", playerName);
            data.set(path + ".last-active", ZonedDateTime.now().format(DATE_FORMAT));

            isDirty = true;
        }
    }

    public Map<UUID, PlayerStats> getAllPlayerStats() {
        synchronized (lock) {
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
    }

    public record SavedEventData(String deliveryName, String category, String item, ZonedDateTime startTime, ZonedDateTime endTime, ZoneId timezone, Map<UUID, Integer> playerDeliveries) {
        public ActiveEvent toActiveEvent() {
            ActiveEvent event = new ActiveEvent(deliveryName, category, item, startTime, endTime, timezone);
            playerDeliveries.forEach(event::recordDelivery);
            return event;
        }
    }
    public record PlayerStats(UUID uuid, String name, int totalDeliveries) {}
}