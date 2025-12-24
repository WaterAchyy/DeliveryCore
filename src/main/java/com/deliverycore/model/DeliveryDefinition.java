package com.deliverycore.model;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Represents a complete delivery event definition from configuration.
 *
 * @param name               the unique identifier for this delivery
 * @param enabled            whether this delivery is active
 * @param visibleBeforeStart whether to show in GUI before start time
 * @param category           the category selection configuration
 * @param item               the item selection configuration
 * @param timezone           the timezone for scheduling
 * @param schedule           the schedule configuration
 * @param winnerCount        the number of winners to select
 * @param reward             the reward configuration
 * @param webhook            the webhook configuration
 */
public record DeliveryDefinition(
    String name,
    boolean enabled,
    boolean visibleBeforeStart,
    SelectionConfig category,
    SelectionConfig item,
    ZoneId timezone,
    ScheduleConfig schedule,
    int winnerCount,
    RewardConfig reward,
    WebhookConfig webhook
) {
    /**
     * Creates a new DeliveryDefinition with validation.
     *
     * @param name               the unique identifier
     * @param enabled            whether enabled
     * @param visibleBeforeStart whether visible before start
     * @param category           the category selection
     * @param item               the item selection
     * @param timezone           the timezone
     * @param schedule           the schedule
     * @param winnerCount        the winner count
     * @param reward             the reward config
     * @param webhook            the webhook config
     * @throws NullPointerException     if required fields are null
     * @throws IllegalArgumentException if winnerCount is less than 1
     */
    public DeliveryDefinition {
        Objects.requireNonNull(name, "Delivery name cannot be null");
        Objects.requireNonNull(category, "Category config cannot be null");
        Objects.requireNonNull(item, "Item config cannot be null");
        Objects.requireNonNull(timezone, "Timezone cannot be null");
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(reward, "Reward config cannot be null");
        Objects.requireNonNull(webhook, "Webhook config cannot be null");
        
        if (winnerCount < 1) {
            throw new IllegalArgumentException("Winner count must be at least 1");
        }
    }
}
