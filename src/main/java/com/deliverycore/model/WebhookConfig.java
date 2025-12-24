package com.deliverycore.model;

import java.util.Objects;

/**
 * Configuration for Discord webhook integration.
 *
 * @param enabled whether webhook notifications are enabled
 * @param url     the webhook URL
 * @param start   the start event webhook configuration
 * @param end     the end event webhook configuration
 */
public record WebhookConfig(
    boolean enabled,
    String url,
    WebhookEventConfig start,
    WebhookEventConfig end
) {
    /**
     * Creates a disabled webhook configuration.
     *
     * @return a new disabled WebhookConfig
     */
    public static WebhookConfig disabled() {
        EmbedConfig emptyEmbed = new EmbedConfig("", "", "#000000");
        WebhookEventConfig emptyEvent = new WebhookEventConfig(false, emptyEmbed);
        return new WebhookConfig(false, "", emptyEvent, emptyEvent);
    }
}
