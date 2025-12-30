package com.deliverycore.service;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Sezon yönetimi ve action bar gösterimi
 * Görseldeki gibi: "Ödüllü Teslimat sezonunun bitişine: (/teslimat) 44 gün 23 saat 23 dakika 18 saniye"
 */
public class SeasonManager {
    
    private final Plugin plugin;
    private final Logger logger;
    
    private boolean enabled = false;
    private String seasonName = "Teslimat Sezonu";
    private ZonedDateTime seasonStart;
    private ZonedDateTime seasonEnd;
    private BukkitTask actionBarTask;
    
    // Action bar formatı
    private String actionBarFormat = "&6• &eÖdüllü {season} bitişine: &7(/teslimat)&r {countdown}";
    
    public SeasonManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Config'den sezon ayarlarını yükler
     */
    public void loadConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) return;
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            
            enabled = config.getBoolean("season.enabled", false);
            seasonName = config.getString("season.name", "Teslimat Sezonu");
            
            String startStr = config.getString("season.start-date", null);
            String endStr = config.getString("season.end-date", null);
            
            ZoneId zone = ZoneId.of(config.getString("general.timezone", "Europe/Istanbul"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone);
            
            if (startStr != null && !startStr.isEmpty()) {
                try {
                    // Saat yoksa 00:00 ekle
                    if (!startStr.contains(" ")) startStr += " 00:00";
                    seasonStart = ZonedDateTime.parse(startStr, formatter);
                } catch (Exception e) {
                    logger.warning("[SEZON] Baslangic tarihi okunamadi: " + startStr);
                }
            }
            
            if (endStr != null && !endStr.isEmpty()) {
                try {
                    // Saat yoksa 23:59 ekle
                    if (!endStr.contains(" ")) endStr += " 23:59";
                    seasonEnd = ZonedDateTime.parse(endStr, formatter);
                } catch (Exception e) {
                    logger.warning("[SEZON] Bitis tarihi okunamadi: " + endStr);
                }
            }
            
            actionBarFormat = config.getString("season.action-bar-format", actionBarFormat);
            
            if (enabled && seasonEnd != null) {
                logger.info("[SEZON] Yuklendi: " + seasonName + " - Bitis: " + seasonEnd);
            }
            
        } catch (Exception e) {
            logger.warning("[SEZON] Config yuklenemedi: " + e.getMessage());
        }
    }
    
    /**
     * Action bar gösterimini başlatır
     */
    public void startActionBar() {
        stopActionBar();
        
        if (!enabled || seasonEnd == null) {
            return;
        }
        
        // Her saniye güncelle
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isSeasonActive()) {
                stopActionBar();
                return;
            }
            
            String message = formatActionBar();
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendActionBar(player, message);
            }
        }, 0L, 20L); // Her saniye
        
        logger.info("[SEZON] Action bar basladi");
    }
    
    /**
     * Action bar gösterimini durdurur
     */
    public void stopActionBar() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }
    
    /**
     * Sezonun aktif olup olmadığını kontrol eder
     */
    public boolean isSeasonActive() {
        if (!enabled || seasonEnd == null) return false;
        
        ZonedDateTime now = ZonedDateTime.now();
        
        // Başlangıç kontrolü
        if (seasonStart != null && now.isBefore(seasonStart)) {
            return false;
        }
        
        // Bitiş kontrolü
        return now.isBefore(seasonEnd);
    }
    
    /**
     * Sezon bitişine kalan süreyi formatlar
     */
    public String formatCountdown() {
        if (seasonEnd == null) return "Belirsiz";
        
        ZonedDateTime now = ZonedDateTime.now();
        if (now.isAfter(seasonEnd)) return "Bitti!";
        
        Duration duration = Duration.between(now, seasonEnd);
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        StringBuilder sb = new StringBuilder();
        
        if (days > 0) {
            sb.append("§a").append(days).append(" §egün ");
        }
        if (hours > 0 || days > 0) {
            sb.append("§a").append(hours).append(" §esaat ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append("§a").append(minutes).append(" §edakika ");
        }
        sb.append("§a").append(seconds).append(" §esaniye");
        
        return sb.toString();
    }
    
    /**
     * Action bar mesajını formatlar
     */
    private String formatActionBar() {
        return translateColors(actionBarFormat
            .replace("{season}", seasonName)
            .replace("{countdown}", formatCountdown()));
    }
    
    /**
     * Oyuncuya action bar mesajı gönderir
     */
    private void sendActionBar(Player player, String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        } catch (Exception e) {
            // Eski API fallback - sessizce geç
        }
    }
    
    /**
     * Renk kodlarını çevirir
     */
    private String translateColors(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public String getSeasonName() { return seasonName; }
    public ZonedDateTime getSeasonStart() { return seasonStart; }
    public ZonedDateTime getSeasonEnd() { return seasonEnd; }
    
    /**
     * Kalan gün sayısını döndürür
     */
    public long getDaysRemaining() {
        if (seasonEnd == null) return -1;
        ZonedDateTime now = ZonedDateTime.now();
        if (now.isAfter(seasonEnd)) return 0;
        return Duration.between(now, seasonEnd).toDays();
    }
}
