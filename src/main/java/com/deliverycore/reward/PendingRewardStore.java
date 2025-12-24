package com.deliverycore.reward;

import java.util.List;
import java.util.UUID;

/**
 * Storage interface for pending rewards.
 * Manages rewards for offline players that need to be delivered when they come online.
 */
public interface PendingRewardStore {
    
    /**
     * Stores a pending reward for a player.
     *
     * @param reward the pending reward to store
     */
    void store(PendingReward reward);
    
    /**
     * Retrieves all pending rewards for a player.
     *
     * @param playerUuid the player's UUID
     * @return list of pending rewards, empty if none
     */
    List<PendingReward> getRewards(UUID playerUuid);
    
    /**
     * Removes all pending rewards for a player.
     *
     * @param playerUuid the player's UUID
     * @return the removed rewards
     */
    List<PendingReward> removeRewards(UUID playerUuid);
    
    /**
     * Checks if a player has pending rewards.
     *
     * @param playerUuid the player's UUID
     * @return true if the player has pending rewards
     */
    boolean hasPendingRewards(UUID playerUuid);
    
    /**
     * Gets the count of pending rewards for a player.
     *
     * @param playerUuid the player's UUID
     * @return the number of pending rewards
     */
    int getRewardCount(UUID playerUuid);
    
    /**
     * Gets all stored pending rewards.
     *
     * @return list of all pending rewards
     */
    List<PendingReward> getAllRewards();
    
    /**
     * Clears all pending rewards.
     */
    void clear();
}
