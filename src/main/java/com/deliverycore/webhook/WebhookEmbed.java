package com.deliverycore.webhook;

/**
 * Represents a Discord webhook embed ready to be sent.
 *
 * @param title       the embed title (placeholders resolved)
 * @param description the embed description (placeholders resolved)
 * @param color       the embed color as decimal integer
 * @param footer      the embed footer text
 * @param thumbnail   the embed thumbnail URL
 * @param authorName  the embed author name
 * @param authorIcon  the embed author icon URL
 */
public record WebhookEmbed(
    String title,
    String description,
    int color,
    String footer,
    String thumbnail,
    String authorName,
    String authorIcon
) {
    /**
     * Creates a new WebhookEmbed with defaults for null values.
     */
    public WebhookEmbed {
        title = title != null ? title : "";
        description = description != null ? description : "";
        footer = footer != null ? footer : "";
        thumbnail = thumbnail != null ? thumbnail : "";
        authorName = authorName != null ? authorName : "";
        authorIcon = authorIcon != null ? authorIcon : "";
    }
    
    /**
     * Simple constructor for basic embeds.
     */
    public WebhookEmbed(String title, String description, int color) {
        this(title, description, color, "", "", "", "");
    }
}
