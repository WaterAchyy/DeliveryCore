package com.deliverycore.placeholder;

import com.deliverycore.gui.DeliveryGUI;
import com.deliverycore.service.ActiveEvent;
import com.deliverycore.service.DeliveryService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PlaceholderAPI expansion for DeliveryCore
 * 
 * PLACEHOLDERS:
 * 
 * Genel:
 * %deliverycore_active%              - Aktif teslimat adı
 * %deliverycore_active_item%         - Aktif teslimat eşyası
 * %deliverycore_active_category%     - Aktif teslimat kategorisi
 * %deliverycore_total%               - Toplam teslimat sayısı
 * %deliverycore_participants%        - Katılımcı sayısı
 * %deliverycore_time_left%           - Kalan süre (formatlanmış)
 * %deliverycore_time_left_seconds%   - Kalan süre (saniye)
 * %deliverycore_time_left_minutes%   - Kalan süre (dakika)
 * %deliverycore_active_count%        - Aktif etkinlik sayısı
 * 
 * Oyuncu bazlı:
 * %deliverycore_player_count%        - Oyuncunun teslimat sayısı
 * %deliverycore_player_rank%         - Oyuncunun sırası
 * 
 * Sıralama (top):
 * %deliverycore_top_1_name%          - 1. sıradaki oyuncu adı
 * %deliverycore_top_1_count%         - 1. sıradaki teslimat sayısı
 * %deliverycore_top_2_name%          - 2. sıradaki oyuncu adı
 * %deliverycore_top_2_count%         - 2. sıradaki teslimat sayısı
 * ... (1-10 arası)
 * 
 * Belirli teslimat:
 * %deliverycore_<teslimat>_active%   - Teslimat aktif mi (true/false)
 * %deliverycore_<teslimat>_item%     - Teslimat eşyası
 * %deliverycore_<teslimat>_total%    - Teslimat toplam sayısı
 * %deliverycore_<teslimat>_time%     - Teslimat kalan süresi
 */
public class DeliveryCorePlaceholders extends PlaceholderExpansion {

    private final DeliveryService deliveryService;
    private final DeliveryGUI deliveryGUI;
    private final String language;

    public DeliveryCorePlaceholders(DeliveryService deliveryService, DeliveryGUI deliveryGUI, String language) {
        this.deliveryService = deliveryService;
        this.deliveryGUI = deliveryGUI;
        this.language = language;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "deliverycore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Maolide, 3Mustafa5";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (deliveryService == null) return "";
        
        var events = deliveryService.getAllActiveEvents();
        ActiveEvent event = events.isEmpty() ? null : events.get(0);
        
        // ═══════════════════════════════════════════════════════════════
        // GENEL PLACEHOLDERS
        // ═══════════════════════════════════════════════════════════════
        
        if (params.equals("active")) {
            if (event == null) return isTurkish() ? "Yok" : "None";
            return deliveryGUI != null ? deliveryGUI.getDeliveryDisplayName(event.getDeliveryName()) : event.getDeliveryName();
        }
        
        if (params.equals("active_item")) {
            if (event == null) return "-";
            return deliveryGUI != null ? deliveryGUI.getItemDisplayName(event.getResolvedItem()) : event.getResolvedItem();
        }
        
        if (params.equals("active_category")) {
            if (event == null) return "-";
            return deliveryGUI != null ? deliveryGUI.getCategoryDisplayName(event.getResolvedCategory()) : event.getResolvedCategory();
        }
        
        if (params.equals("total")) {
            if (event == null) return "0";
            return String.valueOf(event.getTotalDeliveries());
        }
        
        if (params.equals("participants")) {
            if (event == null) return "0";
            return String.valueOf(event.getPlayerDeliveries().size());
        }
        
        if (params.equals("active_count")) {
            return String.valueOf(events.size());
        }
        
        // ═══════════════════════════════════════════════════════════════
        // ZAMAN PLACEHOLDERS
        // ═══════════════════════════════════════════════════════════════
        
        if (params.equals("time_left")) {
            if (event == null || event.getEndTime() == null) return "-";
            return formatTimeLeft(event);
        }
        
        if (params.equals("time_left_seconds")) {
            if (event == null || event.getEndTime() == null) return "0";
            long seconds = Duration.between(ZonedDateTime.now(), event.getEndTime()).getSeconds();
            return String.valueOf(Math.max(0, seconds));
        }
        
        if (params.equals("time_left_minutes")) {
            if (event == null || event.getEndTime() == null) return "0";
            long minutes = Duration.between(ZonedDateTime.now(), event.getEndTime()).toMinutes();
            return String.valueOf(Math.max(0, minutes));
        }
        
        // ═══════════════════════════════════════════════════════════════
        // OYUNCU BAZLI PLACEHOLDERS
        // ═══════════════════════════════════════════════════════════════
        
        if (offlinePlayer != null) {
            UUID uuid = offlinePlayer.getUniqueId();
            
            if (params.equals("player_count")) {
                if (event == null) return "0";
                return String.valueOf(event.getPlayerDeliveries().getOrDefault(uuid, 0));
            }
            
            if (params.equals("player_rank")) {
                if (event == null) return "-";
                int rank = calculateRank(event, uuid);
                return rank > 0 ? String.valueOf(rank) : "-";
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // TOP PLACEHOLDERS (top_1_name, top_1_count, top_2_name, etc.)
        // ═══════════════════════════════════════════════════════════════
        
        if (params.startsWith("top_")) {
            return handleTopPlaceholder(event, params);
        }
        
        // ═══════════════════════════════════════════════════════════════
        // BELİRLİ TESLİMAT PLACEHOLDERS (<teslimat>_active, <teslimat>_item, etc.)
        // ═══════════════════════════════════════════════════════════════
        
        if (params.contains("_")) {
            return handleDeliverySpecificPlaceholder(params);
        }
        
        return null;
    }
    
    private String handleTopPlaceholder(ActiveEvent event, String params) {
        if (event == null) return "-";
        
        String[] parts = params.split("_");
        if (parts.length != 3) return null;
        
        int position;
        try {
            position = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        
        String type = parts[2];
        
        List<Map.Entry<UUID, Integer>> sorted = event.getPlayerDeliveries().entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());
        
        if (position < 1 || position > sorted.size()) {
            return type.equals("name") ? "-" : "0";
        }
        
        Map.Entry<UUID, Integer> entry = sorted.get(position - 1);
        
        if (type.equals("name")) {
            return resolvePlayerName(entry.getKey());
        } else if (type.equals("count")) {
            return String.valueOf(entry.getValue());
        }
        
        return null;
    }
    
    private String handleDeliverySpecificPlaceholder(String params) {
        // Format: <deliveryName>_<property>
        // Örnek: gunluk_active, gunluk_item, gunluk_total, gunluk_time
        
        int lastUnderscore = params.lastIndexOf("_");
        if (lastUnderscore == -1) return null;
        
        String deliveryName = params.substring(0, lastUnderscore);
        String property = params.substring(lastUnderscore + 1);
        
        Optional<ActiveEvent> eventOpt = deliveryService.getActiveEvent(deliveryName);
        
        switch (property) {
            case "active":
                return eventOpt.isPresent() ? "true" : "false";
            case "item":
                return eventOpt.map(e -> deliveryGUI != null ? deliveryGUI.getItemDisplayName(e.getResolvedItem()) : e.getResolvedItem()).orElse("-");
            case "category":
                return eventOpt.map(e -> deliveryGUI != null ? deliveryGUI.getCategoryDisplayName(e.getResolvedCategory()) : e.getResolvedCategory()).orElse("-");
            case "total":
                return eventOpt.map(e -> String.valueOf(e.getTotalDeliveries())).orElse("0");
            case "participants":
                return eventOpt.map(e -> String.valueOf(e.getPlayerDeliveries().size())).orElse("0");
            case "time":
                return eventOpt.map(this::formatTimeLeft).orElse("-");
            default:
                return null;
        }
    }
    
    private int calculateRank(ActiveEvent event, UUID playerUuid) {
        int playerCount = event.getPlayerDeliveries().getOrDefault(playerUuid, 0);
        if (playerCount == 0) return 0;
        int rank = 1;
        for (int count : event.getPlayerDeliveries().values()) {
            if (count > playerCount) rank++;
        }
        return rank;
    }
    
    private String resolvePlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) return player.getName();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        String name = offline.getName();
        return name != null ? name : "???";
    }
    
    private String formatTimeLeft(ActiveEvent event) {
        if (event.getEndTime() == null) return "-";
        
        Duration duration = Duration.between(ZonedDateTime.now(), event.getEndTime());
        if (duration.isNegative()) return isTurkish() ? "Bitti" : "Ended";
        
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private boolean isTurkish() {
        return "tr".equalsIgnoreCase(language);
    }
}
