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
    
    public DeliveryServiceImpl(DeliveryConfig deliveryConfig, CategoryService categoryService, SchedulerService schedulerService) {
        this.deliveryConfig = deliveryConfig;
        this.categoryService = categoryService;
        this.schedulerService = schedulerService;
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
        
        List<Winner> winners = calculateWinners(event, winnerCount, uuid -> "Player_" + uuid.toString().substring(0, 8));
        LOGGER.info("Ended delivery: " + deliveryName + " with " + winners.size() + " winners");
        return winners;
    }
    
    @Override
    public boolean recordDelivery(UUID playerUuid, String deliveryName, int amount) {
        ActiveEvent event = activeEvents.get(deliveryName);
        if (event == null || !event.isActive()) return false;
        event.recordDelivery(playerUuid, amount);
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
}
