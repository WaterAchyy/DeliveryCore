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

import java.util.*;
import java.util.stream.Collectors;

/**
 * PlaceholderAPI expansion for DeliveryCore
 * 
 * Placeholders:
 * %deliverycore_active% - Aktif teslimat adı (yoksa "Yok")
 * %deliverycore_active_item% - Aktif teslimat eşyası
 * %deliverycore_active_category% - Aktif teslimat kategorisi
 * %deliverycore_player_count% - Oyuncunun teslimat sayısı
 * %deliverycore_player_rank% - Oyuncunun sırası
 * %deliverycore_total% - Toplam teslimat sayısı
 * %deliverycore_participants% - Katılımcı sayısı
 * %deliverycore_top_1_name% - 1. sıradaki oyuncu adı
 * %deliverycore_top_1_count% - 1. sıradaki teslimat sayısı
 * %deliverycore_top_2_name% - 2. sıradaki oyuncu adı
 * %deliverycore_top_2_count% - 2. sıradaki teslimat sayısı
 * %deliverycore_top_3_name% - 3. sıradaki oyuncu adı
 * %deliverycore_top_3_count% - 3. sıradaki teslimat sayısı
 * %deliverycore_time_left% - Kalan süre (örn: "1s 30dk")
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
        return "Noramu";
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
        
        // Aktif teslimat bilgileri
        if (params.equals("active")) {
            if (event == null) return isTurkish() ? "Yok" : "None";
            return deliveryGUI.getDeliveryDisplayName(event.getDeliveryName());
        }
        
        if (params.equals("active_item")) {
            if (event == null) return "-";
            return deliveryGUI.getItemDisplayName(event.getResolvedItem());
        }
        
        if (params.equals("active_category")) {
            if (event == null) return "-";
            return deliveryGUI.getCategoryDisplayName(event.getResolvedCategory());
        }
        
        if (params.equals("total")) {
            if (event == null) return "0";
            return String.valueOf(event.getTotalDeliveries());
        }
        
        if (params.equals("participants")) {
            if (event == null) return "0";
            return String.valueOf(event.getPlayerDeliveries().size());
        }
        
        if (params.equals("time_left")) {
            if (event == null || event.getEndTime() == null) return "-";
            return formatTimeLeft(event);
        }
        
        // Oyuncu bazlı placeholders
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
        
        // Top placeholders: top_1_name, top_1_count, top_2_name, etc.
        if (params.startsWith("top_")) {
            return handleTopPlaceholder(event, params);
        }
        
        return null;
    }
    
    private String handleTopPlaceholder(ActiveEvent event, String params) {
        if (event == null) return "-";
        
        // Parse: top_1_name, top_1_count, top_2_name, etc.
        String[] parts = params.split("_");
        if (parts.length != 3) return null;
        
        int position;
        try {
            position = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        
        String type = parts[2]; // "name" or "count"
        
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
        
        long now = System.currentTimeMillis();
        long end = event.getEndTime().toInstant().toEpochMilli();
        long diff = end - now;
        
        if (diff <= 0) return isTurkish() ? "Bitti" : "Ended";
        
        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
        
        if (hours > 0) {
            return hours + (isTurkish() ? "s " : "h ") + minutes + (isTurkish() ? "dk" : "m");
        }
        return minutes + (isTurkish() ? " dk" : " min");
    }
    
    private boolean isTurkish() {
        return "tr".equals(language);
    }
}
