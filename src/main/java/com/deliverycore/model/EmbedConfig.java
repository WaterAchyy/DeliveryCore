package com.deliverycore.model;

/**
 * Configuration for Discord webhook embeds.
 *
 * @param title       the embed title
 * @param description the embed description
 * @param color       the embed color in hex format (e.g., "#FF5733")
 */
public record EmbedConfig(
    String title,
    String description,
    String color
) {
    /**
     * Creates a new EmbedConfig with defaults for null values.
     *
     * @param title       the embed title
     * @param description the embed description
     * @param color       the embed color
     */
    public EmbedConfig {
        title = title != null ? title : "";
        description = description != null ? description : "";
        color = color != null ? color : "#000000";
    }
}
