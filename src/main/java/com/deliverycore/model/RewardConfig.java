package com.deliverycore.model;

import java.util.List;
import java.util.Objects;

/**
 * Configuration for event rewards.
 *
 * @param type       the type of reward (INVENTORY or COMMAND)
 * @param item       the item to give for INVENTORY rewards
 * @param itemAmount the amount of items to give
 * @param commands   the commands to execute for COMMAND rewards
 */
public record RewardConfig(
    RewardType type,
    String item,
    int itemAmount,
    List<String> commands
) {
    /**
     * Creates a new RewardConfig with validation.
     *
     * @param type       the type of reward
     * @param item       the item for INVENTORY rewards
     * @param itemAmount the amount of items
     * @param commands   the commands for COMMAND rewards
     * @throws NullPointerException if type is null
     */
    public RewardConfig {
        Objects.requireNonNull(type, "Reward type cannot be null");
        commands = commands != null ? List.copyOf(commands) : List.of();
    }
    
    /**
     * Creates an inventory reward configuration.
     *
     * @param item   the item to give
     * @param amount the amount to give
     * @return a new RewardConfig for inventory rewards
     */
    public static RewardConfig inventory(String item, int amount) {
        return new RewardConfig(RewardType.INVENTORY, item, amount, List.of());
    }
    
    /**
     * Creates a command reward configuration.
     *
     * @param commands the commands to execute
     * @return a new RewardConfig for command rewards
     */
    public static RewardConfig command(List<String> commands) {
        return new RewardConfig(RewardType.COMMAND, null, 0, commands);
    }
}
