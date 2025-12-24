package com.deliverycore.reward;

import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.RewardConfig;
import com.deliverycore.model.Winner;

import java.util.List;
import java.util.UUID;

/**
 * Service for distributing rewards to event winners.
 * Handles inventory rewards, command execution, and offline player support.
 */
public interface RewardService {
    
    /**
     * Distributes rewards to all winners.
     *
     * @param winners        the list of winners
     * @param reward         the reward configuration
     * @param deliveryName   the delivery event name
     * @param context        the placeholder context for command resolution
     * @param playerResolver resolver to check if player is online and get Player instance
     */
    void distributeRewards(List<Winner> winners, RewardConfig reward, 
                          String deliveryName, PlaceholderContext context,
                          PlayerResolver playerResolver);
    
    /**
     * Gives an inventory reward to a player.
     *
     * @param playerUuid the player's UUID
     * @param item       the item to give
     * @param amount     the amount to give
     * @param resolver   the player resolver
     * @return true if the reward was given, false if player is offline
     */
    boolean giveInventoryReward(UUID playerUuid, String item, int amount, PlayerResolver resolver);
    
    /**
     * Executes reward commands for a player.
     *
     * @param playerUuid the player's UUID
     * @param playerName the player's name
     * @param commands   the commands to execute
     * @param context    the placeholder context
     * @param executor   the command executor
     */
    void executeRewardCommands(UUID playerUuid, String playerName, 
                              List<String> commands, PlaceholderContext context,
                              CommandExecutor executor);
    
    /**
     * Stores a pending reward for an offline player.
     *
     * @param playerUuid   the player's UUID
     * @param deliveryName the delivery event name
     * @param reward       the reward configuration
     */
    void storePendingReward(UUID playerUuid, String deliveryName, RewardConfig reward);
    
    /**
     * Delivers all pending rewards to a player.
     *
     * @param playerUuid the player's UUID
     * @param resolver   the player resolver
     * @param executor   the command executor
     * @param context    the placeholder context
     * @return the number of rewards delivered
     */
    int deliverPendingRewards(UUID playerUuid, PlayerResolver resolver, 
                             CommandExecutor executor, PlaceholderContext context);
    
    /**
     * Checks if a player has pending rewards.
     *
     * @param playerUuid the player's UUID
     * @return true if the player has pending rewards
     */
    boolean hasPendingRewards(UUID playerUuid);
    
    /**
     * Gets the pending reward store.
     *
     * @return the pending reward store
     */
    PendingRewardStore getPendingRewardStore();
    
    /**
     * Functional interface for resolving players.
     */
    interface PlayerResolver {
        /**
         * Checks if a player is online.
         *
         * @param uuid the player's UUID
         * @return true if online
         */
        boolean isOnline(UUID uuid);
        
        /**
         * Gets the player's name.
         *
         * @param uuid the player's UUID
         * @return the player's name, or null if not found
         */
        String getName(UUID uuid);
        
        /**
         * Gives items to a player's inventory.
         *
         * @param uuid   the player's UUID
         * @param item   the item identifier
         * @param amount the amount to give
         * @return true if successful
         */
        boolean giveItem(UUID uuid, String item, int amount);
    }
    
    /**
     * Functional interface for executing commands.
     */
    interface CommandExecutor {
        /**
         * Executes a command from the console.
         *
         * @param command the command to execute
         */
        void execute(String command);
    }
}
