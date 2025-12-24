package com.deliverycore.service;

import com.deliverycore.model.PlaceholderContext;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an active delivery event with locked category/item and player tracking.
 * Once created, the resolved category and item remain immutable for the event duration.
 */
public class ActiveEvent {
    
    private final String deliveryName;
    private final String resolvedCategory;
    private final String resolvedItem;
    private final ZonedDateTime startTime;
    private ZonedDateTime endTime; // Mutable - manuel başlatmada değiştirilebilir
    private final ZoneId timezone;
    private final Map<UUID, Integer> playerDeliveries;
    private int winnerCount = 1; // Manuel başlatmada override edilebilir
    
    /**
     * Creates a new ActiveEvent with locked category and item.
     *
     * @param deliveryName     the delivery event identifier
     * @param resolvedCategory the resolved category name (locked)
     * @param resolvedItem     the resolved item name (locked)
     * @param startTime        the event start time
     * @param endTime          the event end time
     * @param timezone         the event timezone
     */
    public ActiveEvent(
            String deliveryName,
            String resolvedCategory,
            String resolvedItem,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            ZoneId timezone) {
        this.deliveryName = deliveryName;
        this.resolvedCategory = resolvedCategory;
        this.resolvedItem = resolvedItem;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timezone = timezone;
        this.playerDeliveries = new ConcurrentHashMap<>();
    }
    
    public String getDeliveryName() {
        return deliveryName;
    }
    
    public String getResolvedCategory() {
        return resolvedCategory;
    }
    
    public String getResolvedItem() {
        return resolvedItem;
    }
    
    public ZonedDateTime getStartTime() {
        return startTime;
    }
    
    public ZonedDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * Sets the end time for this event.
     * Used for manual event starts with custom duration.
     *
     * @param endTime the new end time
     */
    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }
    
    public ZoneId getTimezone() {
        return timezone;
    }
    
    public int getWinnerCount() {
        return winnerCount;
    }
    
    public void setWinnerCount(int winnerCount) {
        this.winnerCount = winnerCount;
    }

    /**
     * Records a delivery from a player.
     *
     * @param playerUuid the player's UUID
     * @param amount     the number of items delivered
     */
    public void recordDelivery(UUID playerUuid, int amount) {
        playerDeliveries.merge(playerUuid, amount, Integer::sum);
    }
    
    /**
     * Gets the delivery count for a specific player.
     *
     * @param playerUuid the player's UUID
     * @return the number of items delivered by this player
     */
    public int getPlayerDeliveryCount(UUID playerUuid) {
        return playerDeliveries.getOrDefault(playerUuid, 0);
    }
    
    /**
     * Gets all player deliveries as an unmodifiable map.
     *
     * @return map of player UUID to delivery count
     */
    public Map<UUID, Integer> getPlayerDeliveries() {
        return Collections.unmodifiableMap(playerDeliveries);
    }
    
    /**
     * Gets the total number of items delivered across all players.
     *
     * @return total delivery count
     */
    public int getTotalDeliveries() {
        return playerDeliveries.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
    }
    
    /**
     * Creates a PlaceholderContext for this event.
     *
     * @return a PlaceholderContext with event information
     */
    public PlaceholderContext toPlaceholderContext() {
        return PlaceholderContext.empty()
            .withEvent(resolvedCategory, resolvedItem, deliveryName)
            .withTiming(startTime, endTime, timezone);
    }
    
    /**
     * Checks if the event is currently active.
     *
     * @return true if current time is between start and end time
     */
    public boolean isActive() {
        ZonedDateTime now = ZonedDateTime.now(timezone);
        return !now.isBefore(startTime) && now.isBefore(endTime);
    }
}
