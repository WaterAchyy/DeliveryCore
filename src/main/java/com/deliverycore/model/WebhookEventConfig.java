package com.deliverycore.model;

import java.util.Objects;

/**
 * Configuration for a webhook event (start or end).
 *
 * @param mentionEveryone whether to include @everyone mention
 * @param embed           the embed configuration
 */
public record WebhookEventConfig(
    boolean mentionEveryone,
    EmbedConfig embed
) {
    /**
     * Creates a new WebhookEventConfig with validation.
     *
     * @param mentionEveryone whether to mention everyone
     * @param embed           the embed configuration
     * @throws NullPointerException if embed is null
     */
    public WebhookEventConfig {
        Objects.requireNonNull(embed, "Embed config cannot be null");
    }
}
