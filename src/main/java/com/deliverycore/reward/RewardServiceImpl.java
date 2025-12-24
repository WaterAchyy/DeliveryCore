package com.deliverycore.reward;

import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.RewardConfig;
import com.deliverycore.model.RewardType;
import com.deliverycore.model.Winner;
import com.deliverycore.placeholder.PlaceholderEngine;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of RewardService.
 * Handles reward distribution with offline player support.
 */
public class RewardServiceImpl implements RewardService {
    
    private static final Logger LOGGER = Logger.getLogger(RewardServiceImpl.class.getName());
    
    private final PendingRewardStore pendingRewardStore;
    private final PlaceholderEngine placeholderEngine;
    
    /**
     * Creates a new RewardServiceImpl.
     *
     * @param pendingRewardStore the store for pending rewards
     * @param placeholderEngine  the placeholder engine for command resolution
     */
    public RewardServiceImpl(PendingRewardStore pendingRewardStore, 
                            PlaceholderEngine placeholderEngine) {
        this.pendingRewardStore = Objects.requireNonNull(pendingRewardStore, 
            "Pending reward store cannot be null");
        this.placeholderEngine = Objects.requireNonNull(placeholderEngine,
            "Placeholder engine cannot be null");
    }
    
    @Override
    public void distributeRewards(List<Winner> winners, RewardConfig reward,
                                 String deliveryName, PlaceholderContext context,
                                 PlayerResolver playerResolver) {
        Objects.requireNonNull(winners, "Winners list cannot be null");
        Objects.requireNonNull(reward, "Reward cannot be null");
        Objects.requireNonNull(deliveryName, "Delivery name cannot be null");
        Objects.requireNonNull(playerResolver, "Player resolver cannot be null");
        
        for (Winner winner : winners) {
            distributeToWinner(winner, reward, deliveryName, context, playerResolver);
        }
    }
    
    private void distributeToWinner(Winner winner, RewardConfig reward,
                                   String deliveryName, PlaceholderContext context,
                                   PlayerResolver playerResolver) {
        UUID playerUuid = winner.playerUuid();
        String playerName = winner.playerName();
        
        // Check if player is online
        if (!playerResolver.isOnline(playerUuid)) {
            // Store as pending reward for offline player
            storePendingReward(playerUuid, deliveryName, reward);
            LOGGER.info(() -> String.format(
                "Stored pending reward for offline player %s (%s) from delivery %s",
                playerName, playerUuid, deliveryName));
            return;
        }
        
        // Create context with player info
        PlaceholderContext playerContext = context != null 
            ? context.withPlayer(playerName, playerUuid)
            : PlaceholderContext.empty().withPlayer(playerName, playerUuid);
        
        // Distribute based on reward type
        if (reward.type() == RewardType.INVENTORY) {
            boolean success = giveInventoryReward(playerUuid, reward.item(), 
                reward.itemAmount(), playerResolver);
            if (!success) {
                // Player went offline, store as pending
                storePendingReward(playerUuid, deliveryName, reward);
            }
        }
        
        // Execute commands if configured
        if (reward.commands() != null && !reward.commands().isEmpty()) {
            executeRewardCommands(playerUuid, playerName, reward.commands(), 
                playerContext, cmd -> {
                    // Default no-op executor - actual implementation provided by caller
                    LOGGER.warning("No command executor provided for reward commands");
                });
        }
    }
    
    @Override
    public boolean giveInventoryReward(UUID playerUuid, String item, int amount, 
                                       PlayerResolver resolver) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        Objects.requireNonNull(resolver, "Player resolver cannot be null");
        
        if (item == null || item.isEmpty() || amount <= 0) {
            return true; // Nothing to give
        }
        
        if (!resolver.isOnline(playerUuid)) {
            return false;
        }
        
        try {
            return resolver.giveItem(playerUuid, item, amount);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, 
                "Failed to give inventory reward to " + playerUuid, e);
            return false;
        }
    }
    
    @Override
    public void executeRewardCommands(UUID playerUuid, String playerName,
                                     List<String> commands, PlaceholderContext context,
                                     CommandExecutor executor) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        Objects.requireNonNull(executor, "Command executor cannot be null");
        
        if (commands == null || commands.isEmpty()) {
            return;
        }
        
        PlaceholderContext cmdContext = context != null 
            ? context.withPlayer(playerName, playerUuid)
            : PlaceholderContext.empty().withPlayer(playerName, playerUuid);
        
        for (String command : commands) {
            if (command == null || command.isEmpty()) {
                continue;
            }
            
            // Resolve placeholders in command
            String resolvedCommand = placeholderEngine.resolve(command, cmdContext);
            
            try {
                executor.execute(resolvedCommand);
                LOGGER.fine(() -> String.format(
                    "Executed reward command for %s: %s", playerName, resolvedCommand));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, 
                    "Failed to execute reward command: " + resolvedCommand, e);
            }
        }
    }
    
    @Override
    public void storePendingReward(UUID playerUuid, String deliveryName, 
                                   RewardConfig reward) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        Objects.requireNonNull(deliveryName, "Delivery name cannot be null");
        Objects.requireNonNull(reward, "Reward cannot be null");
        
        PendingReward pending = PendingReward.create(playerUuid, deliveryName, reward);
        pendingRewardStore.store(pending);
        
        LOGGER.info(() -> String.format(
            "Stored pending reward for player %s from delivery %s",
            playerUuid, deliveryName));
    }
    
    @Override
    public int deliverPendingRewards(UUID playerUuid, PlayerResolver resolver,
                                    CommandExecutor executor, PlaceholderContext context) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        Objects.requireNonNull(resolver, "Player resolver cannot be null");
        Objects.requireNonNull(executor, "Command executor cannot be null");
        
        if (!resolver.isOnline(playerUuid)) {
            return 0;
        }
        
        List<PendingReward> rewards = pendingRewardStore.removeRewards(playerUuid);
        if (rewards.isEmpty()) {
            return 0;
        }
        
        final String playerName = resolver.getName(playerUuid);
        int delivered = 0;
        
        for (PendingReward pending : rewards) {
            RewardConfig reward = pending.reward();
            
            PlaceholderContext rewardContext = context != null
                ? context.withPlayer(playerName, playerUuid)
                    .withEvent(null, null, pending.deliveryName())
                : PlaceholderContext.empty()
                    .withPlayer(playerName, playerUuid)
                    .withEvent(null, null, pending.deliveryName());
            
            // Give inventory reward
            if (reward.type() == RewardType.INVENTORY) {
                giveInventoryReward(playerUuid, reward.item(), 
                    reward.itemAmount(), resolver);
            }
            
            // Execute commands
            if (reward.commands() != null && !reward.commands().isEmpty()) {
                executeRewardCommands(playerUuid, playerName, 
                    reward.commands(), rewardContext, executor);
            }
            
            delivered++;
        }
        
        final int finalDelivered = delivered;
        LOGGER.info(() -> String.format(
            "Delivered %d pending rewards to player %s", finalDelivered, playerName));
        
        return delivered;
    }
    
    @Override
    public boolean hasPendingRewards(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        return pendingRewardStore.hasPendingRewards(playerUuid);
    }
    
    @Override
    public PendingRewardStore getPendingRewardStore() {
        return pendingRewardStore;
    }
}
