package com.deliverycore.webhook;

import com.deliverycore.model.EmbedConfig;
import com.deliverycore.model.PlaceholderContext;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending Discord webhook notifications.
 * Handles embed building with placeholder resolution and HTTP POST requests.
 */
public interface WebhookService {
    
    /**
     * Sends an embed to the specified Discord webhook URL.
     *
     * @param url             the Discord webhook URL
     * @param embed           the embed to send
     * @param mentionEveryone whether to include @everyone mention
     * @return a CompletableFuture containing the result of the webhook request
     */
    CompletableFuture<WebhookResult> sendEmbed(String url, WebhookEmbed embed, boolean mentionEveryone);
    
    /**
     * Builds a WebhookEmbed from an EmbedConfig with placeholders resolved.
     *
     * @param config  the embed configuration with placeholder tokens
     * @param context the context for placeholder resolution
     * @return a WebhookEmbed with all placeholders resolved
     */
    WebhookEmbed buildEmbed(EmbedConfig config, PlaceholderContext context);
    
    /**
     * Parses a hex color string to a decimal integer.
     *
     * @param hexColor the hex color string (e.g., "#FF5733" or "FF5733")
     * @return the color as a decimal integer
     */
    int parseColor(String hexColor);
}
