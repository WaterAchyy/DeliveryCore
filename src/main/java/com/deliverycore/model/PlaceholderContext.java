package com.deliverycore.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Context object containing all values that can be used for placeholder resolution.
 *
 * @param category       the resolved category name
 * @param item           the resolved item name
 * @param playerName     the relevant player's name
 * @param playerUuid     the relevant player's UUID
 * @param deliveryName   the delivery event identifier
 * @param startTime      the event start time
 * @param endTime        the event end time
 * @param timezone       the configured timezone
 * @param winnerCount    the number of winners
 * @param winners        the list of winner names
 * @param winnerDetails  the list of Winner objects with full details (rank, count)
 * @param deliveryAmount the total items delivered
 */
public record PlaceholderContext(
    String category,
    String item,
    String playerName,
    UUID playerUuid,
    String deliveryName,
    ZonedDateTime startTime,
    ZonedDateTime endTime,
    ZoneId timezone,
    int winnerCount,
    List<String> winners,
    List<Winner> winnerDetails,
    int deliveryAmount
) {
    /**
     * Creates a new PlaceholderContext with defensive copy of winners list.
     */
    public PlaceholderContext {
        winners = winners != null ? List.copyOf(winners) : List.of();
        winnerDetails = winnerDetails != null ? List.copyOf(winnerDetails) : List.of();
    }
    
    /**
     * Creates an empty placeholder context with no values set.
     *
     * @return an empty PlaceholderContext
     */
    public static PlaceholderContext empty() {
        return new PlaceholderContext(
            null, null, null, null, null,
            null, null, null, 0, List.of(), List.of(), 0
        );
    }
    
    /**
     * Creates a new context with player information added.
     *
     * @param name the player's name
     * @param uuid the player's UUID
     * @return a new PlaceholderContext with player info
     */
    public PlaceholderContext withPlayer(String name, UUID uuid) {
        return new PlaceholderContext(
            category, item, name, uuid, deliveryName,
            startTime, endTime, timezone, winnerCount, winners, winnerDetails, deliveryAmount
        );
    }
    
    /**
     * Creates a new context with event information.
     *
     * @param category     the category name
     * @param item         the item name
     * @param deliveryName the delivery name
     * @return a new PlaceholderContext with event info
     */
    public PlaceholderContext withEvent(String category, String item, String deliveryName) {
        return new PlaceholderContext(
            category, item, playerName, playerUuid, deliveryName,
            startTime, endTime, timezone, winnerCount, winners, winnerDetails, deliveryAmount
        );
    }
    
    /**
     * Creates a new context with timing information.
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @param timezone  the timezone
     * @return a new PlaceholderContext with timing info
     */
    public PlaceholderContext withTiming(ZonedDateTime startTime, ZonedDateTime endTime, ZoneId timezone) {
        return new PlaceholderContext(
            category, item, playerName, playerUuid, deliveryName,
            startTime, endTime, timezone, winnerCount, winners, winnerDetails, deliveryAmount
        );
    }
    
    /**
     * Creates a new context with winner information.
     *
     * @param winnerCount    the number of winners
     * @param winners        the list of winner names
     * @param deliveryAmount the total delivery amount
     * @return a new PlaceholderContext with winner info
     */
    public PlaceholderContext withWinners(int winnerCount, List<String> winners, int deliveryAmount) {
        return new PlaceholderContext(
            category, item, playerName, playerUuid, deliveryName,
            startTime, endTime, timezone, winnerCount, winners, winnerDetails, deliveryAmount
        );
    }
    
    /**
     * Creates a new context with full winner details.
     *
     * @param winnerList the list of Winner objects with rank and delivery count
     * @return a new PlaceholderContext with winner details
     */
    public PlaceholderContext withWinnerDetails(List<Winner> winnerList) {
        List<String> names = winnerList != null 
            ? winnerList.stream().map(Winner::playerName).toList() 
            : List.of();
        int totalDeliveries = winnerList != null 
            ? winnerList.stream().mapToInt(Winner::deliveryCount).sum() 
            : 0;
        return new PlaceholderContext(
            category, item, playerName, playerUuid, deliveryName,
            startTime, endTime, timezone, 
            winnerList != null ? winnerList.size() : 0, 
            names, 
            winnerList != null ? winnerList : List.of(), 
            totalDeliveries
        );
    }
}
