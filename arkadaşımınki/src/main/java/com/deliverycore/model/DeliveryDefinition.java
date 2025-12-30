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
 * @param season             the seasonal configuration (v1.1)
 * @param tabDisplay         the tab display configuration (v1.1)
 * @param hologram           the hologram configuration (v1.1)
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
    WebhookConfig webhook,
    SeasonConfig season,
    TabDisplayConfig tabDisplay,
    HologramConfig hologram
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
     * @param season             the season config (v1.1)
     * @param tabDisplay         the tab display config (v1.1)
     * @param hologram           the hologram config (v1.1)
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
        Objects.requireNonNull(season, "Season config cannot be null");
        Objects.requireNonNull(tabDisplay, "Tab display config cannot be null");
        Objects.requireNonNull(hologram, "Hologram config cannot be null");
        
        if (winnerCount < 1) {
            throw new IllegalArgumentException("Winner count must be at least 1");
        }
    }
    
    /**
     * Creates a DeliveryDefinition without v1.1 features (backward compatibility)
     */
    public static DeliveryDefinition withoutV11Features(
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
        return new DeliveryDefinition(
            name, enabled, visibleBeforeStart, category, item, timezone,
            schedule, winnerCount, reward, webhook, 
            SeasonConfig.disabled(),
            TabDisplayConfig.disabled(),
            HologramConfig.disabled()
        );
    }
    
    /**
     * Creates a DeliveryDefinition with only seasonal features (v1.1 partial)
     */
    public static DeliveryDefinition withSeasonOnly(
        String name,
        boolean enabled,
        boolean visibleBeforeStart,
        SelectionConfig category,
        SelectionConfig item,
        ZoneId timezone,
        ScheduleConfig schedule,
        int winnerCount,
        RewardConfig reward,
        WebhookConfig webhook,
        SeasonConfig season
    ) {
        return new DeliveryDefinition(
            name, enabled, visibleBeforeStart, category, item, timezone,
            schedule, winnerCount, reward, webhook, season,
            TabDisplayConfig.disabled(),
            HologramConfig.disabled()
        );
    }
}
