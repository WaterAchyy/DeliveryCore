package com.deliverycore.service;

import com.deliverycore.config.DeliveryConfig;
import com.deliverycore.model.Category;
import com.deliverycore.model.DeliveryDefinition;
import com.deliverycore.model.Winner;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DeliveryServiceImpl implements DeliveryService {
    
    private static final Logger LOGGER = Logger.getLogger(DeliveryServiceImpl.class.getName());
    
    private final DeliveryConfig deliveryConfig;
    private final CategoryService categoryService;
    private final SchedulerService schedulerService;
    private final Map<String, ActiveEvent> activeEvents = new ConcurrentHashMap<>();
    
    // v1.1 services
    private SeasonService seasonService;
    private TabListService tabListService;
    
    public DeliveryServiceImpl(DeliveryConfig deliveryConfig, CategoryService categoryService, SchedulerService schedulerService) {
        this.deliveryConfig = deliveryConfig;
        this.categoryService = categoryService;
        this.schedulerService = schedulerService;
    }
    
    /**
     * Sets the v1.1 services for enhanced functionality.
     */
    public void setV11Services(SeasonService seasonService, TabListService tabListService) {
        this.seasonService = seasonService;
        this.tabListService = tabListService;
    }
    
    @Override
    public Optional<ActiveEvent> startEvent(String deliveryName) {
        return startEvent(deliveryName, false);
    }
    
    @Override
    public Optional<ActiveEvent> startEvent(String deliveryName, boolean force) {
        Optional<DeliveryDefinition> defOpt = deliveryConfig.getDelivery(deliveryName);
        if (defOpt.isEmpty()) {
            LOGGER.warning("Delivery not found: " + deliveryName);
            return Optional.empty();
        }
        
        DeliveryDefinition def = defOpt.get();
        if (!force && !def.enabled()) {
            LOGGER.info("Delivery is disabled: " + deliveryName);
            return Optional.empty();
        }
        
        // Check seasonal restrictions (v1.1)
        if (def.season().enabled() && seasonService != null) {
            if (!seasonService.isSeasonActive(def.season())) {
                LOGGER.info("Delivery is not in season: " + deliveryName);
                return Optional.empty();
            }
        }
        
        try {
            Category category = categoryService.resolveCategory(def.category().mode(), def.category().value());
            if (category == null || category.items().isEmpty()) {
                LOGGER.warning("Category is empty for delivery: " + deliveryName);
                return Optional.empty();
            }
            
            String item = categoryService.resolveItem(category, def.item().mode(), def.item().value());
            if (item == null || item.isEmpty()) {
                LOGGER.warning("Item could not be resolved for delivery: " + deliveryName);
                return Optional.empty();
            }

            ZonedDateTime now = ZonedDateTime.now(def.timezone());
            ZonedDateTime endTime = schedulerService.getNextOccurrence(def.schedule().end(), def.timezone()).orElse(now.plusHours(1));
            
            ActiveEvent event = new ActiveEvent(deliveryName, category.name(), item, now, endTime, def.timezone());
            activeEvents.put(deliveryName, event);
            
            // Update v1.1 services
            updateV11Services(deliveryName, event, def);
            
            LOGGER.info("Started delivery: " + deliveryName + " [" + category.name() + ", " + item + "]");
            return Optional.of(event);
        } catch (Exception e) {
            LOGGER.warning("Failed to start delivery " + deliveryName + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<Winner> endEvent(String deliveryName) {
        ActiveEvent event = activeEvents.remove(deliveryName);
        if (event == null) return List.of();
        
        // Zamanlayıcıyı iptal et
        if (schedulerService != null) {
            schedulerService.cancelScheduledEvent(deliveryName);
        }
        
        // Kazanan sayısını önce event'ten al (manuel override), yoksa config'den
        int winnerCount = event.getWinnerCount();
        if (winnerCount <= 0) {
            winnerCount = deliveryConfig.getDelivery(deliveryName).map(DeliveryDefinition::winnerCount).orElse(1);
        }
        
        // Oyuncu isimlerini Bukkit API ile çöz
        List<Winner> winners = calculateWinners(event, winnerCount, uuid -> {
            org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName();
            return name != null ? name : "Player_" + uuid.toString().substring(0, 8);
        });
        
        // Clear v1.1 services for this delivery
        clearV11Services(deliveryName);
        
        LOGGER.info("Ended delivery: " + deliveryName + " with " + winners.size() + " winners");
        return winners;
    }
    
    @Override
    public boolean recordDelivery(UUID playerUuid, String deliveryName, int amount) {
        ActiveEvent event = activeEvents.get(deliveryName);
        if (event == null || !event.isActive()) return false;
        event.recordDelivery(playerUuid, amount);
        
        // Hologram devre dışı - v1.2'de eklenecek
        
        return true;
    }
    
    @Override
    public Optional<ActiveEvent> getActiveEvent(String deliveryName) {
        return Optional.ofNullable(activeEvents.get(deliveryName));
    }
    
    @Override
    public List<ActiveEvent> getAllActiveEvents() {
        return List.copyOf(activeEvents.values());
    }
    
    /**
     * Kaydedilmiş bir etkinliği geri yükler.
     * Sunucu yeniden başlatıldığında kullanılır.
     */
    public void restoreEvent(ActiveEvent event) {
        if (event != null && event.getDeliveryName() != null) {
            activeEvents.put(event.getDeliveryName(), event);
            LOGGER.info("Restored event: " + event.getDeliveryName());
            
            // v1.1 servislerini güncelle (hologram, tab list)
            var defOpt = deliveryConfig.getDelivery(event.getDeliveryName());
            if (defOpt.isPresent()) {
                updateV11Services(event.getDeliveryName(), event, defOpt.get());
            }
        }
    }
    
    @Override
    public List<Winner> calculateWinners(ActiveEvent event, int winnerCount, PlayerNameResolver nameResolver) {
        Map<UUID, Integer> deliveries = event.getPlayerDeliveries();
        if (deliveries.isEmpty()) return List.of();
        
        List<Map.Entry<UUID, Integer>> sorted = deliveries.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(winnerCount)
            .collect(Collectors.toList());
        
        List<Winner> winners = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : sorted) {
            winners.add(new Winner(entry.getKey(), nameResolver.resolve(entry.getKey()), entry.getValue(), rank++));
        }
        return winners;
    }
    
    /**
     * Updates v1.1 services when an event starts.
     */
    private void updateV11Services(String deliveryName, ActiveEvent event, DeliveryDefinition def) {
        try {
            // Update tab list - enable tab display for this delivery
            if (def.tabDisplay().enabled() && tabListService != null) {
                tabListService.enableTabDisplay(deliveryName);
                tabListService.updateAllTabLists();
            }
            
            // Hologram devre dışı - v1.2'de eklenecek
        } catch (Exception e) {
            LOGGER.warning("Failed to update v1.1 services for " + deliveryName + ": " + e.getMessage());
        }
    }
    
    /**
     * Clears v1.1 services when an event ends.
     */
    private void clearV11Services(String deliveryName) {
        try {
            // Clear tab list - disable tab display for this delivery
            if (tabListService != null) {
                tabListService.disableTabDisplay(deliveryName);
            }
            
            // Hologram devre dışı - v1.2'de eklenecek
        } catch (Exception e) {
            LOGGER.warning("Failed to clear v1.1 services for " + deliveryName + ": " + e.getMessage());
        }
    }
    
    /**
     * Creates leaderboard data from an active event.
     */
    private List<HologramService.PlayerLeaderboardEntry> createLeaderboardData(ActiveEvent event) {
        return event.getPlayerDeliveries().entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(10)
            .map(entry -> {
                int rank = calculatePlayerRank(event, entry.getKey());
                // Oyuncu ismini Bukkit API ile çöz
                org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(entry.getKey());
                String playerName = offlinePlayer.getName();
                if (playerName == null) {
                    playerName = "Player_" + entry.getKey().toString().substring(0, 8);
                }
                return new HologramService.PlayerLeaderboardEntry(playerName, entry.getValue(), rank);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calculates a player's rank in an event.
     */
    private int calculatePlayerRank(ActiveEvent event, UUID playerUuid) {
        int playerCount = event.getPlayerDeliveries().getOrDefault(playerUuid, 0);
        if (playerCount == 0) return 0;
        
        int rank = 1;
        for (int count : event.getPlayerDeliveries().values()) {
            if (count > playerCount) rank++;
        }
        return rank;
    }
}
