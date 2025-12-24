package com.deliverycore.reward;

import com.deliverycore.model.RewardConfig;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a pending reward for an offline player.
 * Stores reward information until the player comes online.
 *
 * @param playerUuid   the player's UUID
 * @param deliveryName the delivery event name
 * @param reward       the reward configuration
 * @param earnedAt     when the reward was earned
 */
public record PendingReward(
    UUID playerUuid,
    String deliveryName,
    RewardConfig reward,
    ZonedDateTime earnedAt
) {
    /**
     * Creates a new PendingReward with validation.
     *
     * @param playerUuid   the player's UUID
     * @param deliveryName the delivery event name
     * @param reward       the reward configuration
     * @param earnedAt     when the reward was earned
     * @throws NullPointerException if any parameter is null
     */
    public PendingReward {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        Objects.requireNonNull(deliveryName, "Delivery name cannot be null");
        Objects.requireNonNull(reward, "Reward cannot be null");
        Objects.requireNonNull(earnedAt, "Earned time cannot be null");
    }
    
    /**
     * Creates a new PendingReward with the current time.
     *
     * @param playerUuid   the player's UUID
     * @param deliveryName the delivery event name
     * @param reward       the reward configuration
     * @return a new PendingReward
     */
    public static PendingReward create(UUID playerUuid, String deliveryName, RewardConfig reward) {
        return new PendingReward(playerUuid, deliveryName, reward, ZonedDateTime.now());
    }
}
