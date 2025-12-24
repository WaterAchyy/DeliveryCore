package com.deliverycore.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a winner of a delivery event.
 *
 * @param playerUuid    the winner's UUID
 * @param playerName    the winner's display name
 * @param deliveryCount the number of items delivered
 * @param rank          the winner's rank (1 = first place)
 */
public record Winner(
    UUID playerUuid,
    String playerName,
    int deliveryCount,
    int rank
) {
    /**
     * Creates a new Winner with validation.
     *
     * @param playerUuid    the player's UUID
     * @param playerName    the player's name
     * @param deliveryCount the delivery count
     * @param rank          the rank
     * @throws NullPointerException     if playerUuid or playerName is null
     * @throws IllegalArgumentException if deliveryCount is negative or rank is less than 1
     */
    public Winner {
        Objects.requireNonNull(playerUuid, "Player UUID cannot be null");
        Objects.requireNonNull(playerName, "Player name cannot be null");
        
        if (deliveryCount < 0) {
            throw new IllegalArgumentException("Delivery count cannot be negative");
        }
        if (rank < 1) {
            throw new IllegalArgumentException("Rank must be at least 1");
        }
    }
}
