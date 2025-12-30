package com.deliverycore.service;

import com.deliverycore.model.HologramInfo;
import com.deliverycore.util.Result;
import org.bukkit.Location;

import java.util.List;

/**
 * Service for managing holograms that display delivery leaderboards.
 * v1.1 feature - admin-managed holograms using DecentHolograms API.
 * 
 * Kullanım: /dc holo create <id> <teslimat> - Bulunduğun yerde hologram oluştur
 *           /dc holo delete <id> - Hologramı sil
 *           /dc holo list - Tüm hologramları listele
 *           /dc holo update - Tüm hologramları güncelle
 */
public interface HologramService {
    
    /**
     * Checks if the hologram system is enabled and available.
     * Requires DecentHolograms or HolographicDisplays plugin to be installed.
     */
    boolean isEnabled();
    
    /**
     * Gets the name of the hologram plugin being used.
     * @return "DecentHolograms", "HolographicDisplays", or "None"
     */
    String getHologramPluginName();
    
    /**
     * Creates a new hologram at the specified location for a specific delivery.
     * 
     * @param id the unique identifier for the hologram
     * @param location the location where the hologram should be created
     * @param deliveryName the delivery this hologram is linked to
     * @return Result indicating success or failure with error message
     */
    Result<Void> createHologram(String id, Location location, String deliveryName);
    
    /**
     * Creates a new hologram at the specified location.
     * 
     * @param id the unique identifier for the hologram
     * @param location the location where the hologram should be created
     * @param lines the initial lines to display
     * @return Result indicating success or failure with error message
     */
    Result<Void> createHologram(String id, Location location, List<String> lines);
    
    /**
     * Updates an existing hologram with new content.
     * 
     * @param id the hologram identifier
     * @param lines the new lines to display
     * @return Result indicating success or failure with error message
     */
    Result<Void> updateHologram(String id, List<String> lines);
    
    /**
     * Updates a hologram with leaderboard data for a specific delivery.
     * 
     * @param id the hologram identifier
     * @param deliveryName the delivery name
     * @param leaderboardData the leaderboard entries
     * @return Result indicating success or failure
     */
    Result<Void> updateHologramLeaderboard(String id, String deliveryName, List<PlayerLeaderboardEntry> leaderboardData);
    
    /**
     * Deletes a hologram.
     * 
     * @param id the hologram identifier
     * @return Result indicating success or failure with error message
     */
    Result<Void> deleteHologram(String id);
    
    /**
     * Lists all managed holograms.
     * 
     * @return list of hologram information
     */
    List<HologramInfo> listHolograms();
    
    /**
     * Gets holograms linked to a specific delivery.
     * 
     * @param deliveryName the delivery name
     * @return list of hologram information for that delivery
     */
    List<HologramInfo> getHologramsForDelivery(String deliveryName);
    
    /**
     * Checks if a hologram with the given ID exists.
     * 
     * @param id the hologram identifier
     * @return true if the hologram exists
     */
    boolean hologramExists(String id);
    
    /**
     * Updates all holograms linked to a specific delivery.
     * Called when delivery events start/end or leaderboard changes.
     * 
     * @param deliveryName the delivery name
     * @param leaderboardData the leaderboard data to display
     */
    void updateDeliveryHolograms(String deliveryName, List<PlayerLeaderboardEntry> leaderboardData);
    
    /**
     * Updates all holograms with current leaderboard data.
     * Called when delivery events start/end.
     * 
     * @param deliveryName the active delivery name, null if no active delivery
     * @param leaderboardData the leaderboard data to display
     */
    void updateAllHolograms(String deliveryName, List<String> leaderboardData);
    
    /**
     * Formats leaderboard data for hologram display.
     * 
     * @param deliveryName the delivery name
     * @param itemName the item being delivered
     * @param playerData list of player data (name, count, rank)
     * @return formatted lines for hologram display
     */
    List<String> formatLeaderboard(String deliveryName, String itemName, List<PlayerLeaderboardEntry> playerData);
    
    /**
     * Saves all holograms to file.
     */
    void saveHolograms();
    
    /**
     * Loads all holograms from file.
     */
    void loadHolograms();
    
    /**
     * Player leaderboard entry for hologram display.
     */
    record PlayerLeaderboardEntry(String playerName, int deliveryCount, int rank) {}
}