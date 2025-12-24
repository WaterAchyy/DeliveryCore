package com.deliverycore.webhook;

import com.deliverycore.model.EmbedConfig;
import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.Winner;
import com.deliverycore.placeholder.PlaceholderEngine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of WebhookService for Discord webhook integration.
 * Supports global config settings with per-delivery overrides.
 */
public class WebhookServiceImpl implements WebhookService {
    
    private static final Logger LOGGER = Logger.getLogger(WebhookServiceImpl.class.getName());
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int DEFAULT_COLOR = 0x000000;
    
    private final PlaceholderEngine placeholderEngine;
    private final HttpClient httpClient;
    
    // Global config settings
    private String globalUrl = "";
    private boolean globalEnabled = false;
    private boolean globalMentionEveryone = false;
    
    // Start embed config
    private String startTitle = "📦 {delivery_name} Başladı!";
    private String startDescription = "Yeni bir teslimat etkinliği başladı!";
    private String startColor = "#00FF00";
    private String startFooter = "DeliveryCore";
    private String startThumbnail = "";
    private String startAuthorName = "";
    private String startAuthorIcon = "";
    
    // End embed config
    private String endTitle = "🏆 {delivery_name} Sona Erdi!";
    private String endDescription = "Teslimat etkinliği tamamlandı!";
    private String endColor = "#FFD700";
    private String endFooter = "DeliveryCore";
    private String endThumbnail = "";
    
    // Winners config
    private String winnersTitle = "🎖️ Kazananlar";
    private String winnerFormat = "{medal} **{player}** - {count} teslimat";
    private String medalFirst = "🥇";
    private String medalSecond = "🥈";
    private String medalThird = "🥉";
    private String medalOther = "🏅";
    private String noWinnersMessage = "*Kimse katılmadı*";
    private int maxWinnersDisplay = 10;
    
    public WebhookServiceImpl(PlaceholderEngine placeholderEngine) {
        this.placeholderEngine = placeholderEngine;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    }
    
    public WebhookServiceImpl(PlaceholderEngine placeholderEngine, HttpClient httpClient) {
        this.placeholderEngine = placeholderEngine;
        this.httpClient = httpClient;
    }
    
    /**
     * Loads webhook configuration from a config map.
     */
    @SuppressWarnings("unchecked")
    public void loadConfig(Map<String, Object> webhookConfig) {
        if (webhookConfig == null) return;
        
        globalEnabled = getBoolean(webhookConfig, "enabled", false);
        globalUrl = getString(webhookConfig, "url", "");
        globalMentionEveryone = getBoolean(webhookConfig, "mention-everyone", false);
        
        // Start embed
        Map<String, Object> start = (Map<String, Object>) webhookConfig.get("start");
        if (start != null) {
            startTitle = getString(start, "title", startTitle);
            startDescription = getString(start, "description", startDescription);
            startColor = getString(start, "color", startColor);
            startFooter = getString(start, "footer", startFooter);
            startThumbnail = getString(start, "thumbnail", startThumbnail);
            
            Map<String, Object> author = (Map<String, Object>) start.get("author");
            if (author != null) {
                startAuthorName = getString(author, "name", "");
                startAuthorIcon = getString(author, "icon", "");
            }
        }
        
        // End embed
        Map<String, Object> end = (Map<String, Object>) webhookConfig.get("end");
        if (end != null) {
            endTitle = getString(end, "title", endTitle);
            endDescription = getString(end, "description", endDescription);
            endColor = getString(end, "color", endColor);
            endFooter = getString(end, "footer", endFooter);
            endThumbnail = getString(end, "thumbnail", endThumbnail);
            
            // Winners config
            Map<String, Object> winners = (Map<String, Object>) end.get("winners");
            if (winners != null) {
                winnersTitle = getString(winners, "title", winnersTitle);
                winnerFormat = getString(winners, "format", winnerFormat);
                noWinnersMessage = getString(winners, "no-winners", noWinnersMessage);
                maxWinnersDisplay = getInt(winners, "max-display", maxWinnersDisplay);
                
                Map<String, Object> medals = (Map<String, Object>) winners.get("medals");
                if (medals != null) {
                    medalFirst = getString(medals, "first", medalFirst);
                    medalSecond = getString(medals, "second", medalSecond);
                    medalThird = getString(medals, "third", medalThird);
                    medalOther = getString(medals, "other", medalOther);
                }
            }
        }
        
        LOGGER.info("Webhook config loaded. Enabled: " + globalEnabled);
    }
    
    /**
     * Sends event start notification.
     */
    public CompletableFuture<WebhookResult> sendStartNotification(
            String deliveryUrl, boolean deliveryEnabled, PlaceholderContext context) {
        
        if (!shouldSend(deliveryEnabled)) {
            return CompletableFuture.completedFuture(WebhookResult.success(0));
        }
        
        String url = resolveUrl(deliveryUrl);
        if (url.isBlank()) {
            return CompletableFuture.completedFuture(WebhookResult.failure(0, "No webhook URL configured"));
        }
        
        WebhookEmbed embed = buildStartEmbed(context);
        return sendEmbed(url, embed, globalMentionEveryone);
    }
    
    /**
     * Sends event end notification with winners leaderboard.
     */
    public CompletableFuture<WebhookResult> sendEndNotification(
            String deliveryUrl, boolean deliveryEnabled, PlaceholderContext context, List<Winner> winners) {
        
        if (!shouldSend(deliveryEnabled)) {
            return CompletableFuture.completedFuture(WebhookResult.success(0));
        }
        
        String url = resolveUrl(deliveryUrl);
        if (url.isBlank()) {
            return CompletableFuture.completedFuture(WebhookResult.failure(0, "No webhook URL configured"));
        }
        
        WebhookEmbed embed = buildEndEmbed(context, winners);
        return sendEmbed(url, embed, globalMentionEveryone);
    }
    
    private boolean shouldSend(boolean deliveryEnabled) {
        return globalEnabled && deliveryEnabled;
    }
    
    private String resolveUrl(String deliveryUrl) {
        if (deliveryUrl != null && !deliveryUrl.isBlank()) {
            return deliveryUrl;
        }
        return globalUrl;
    }
    
    private WebhookEmbed buildStartEmbed(PlaceholderContext context) {
        String title = resolvePlaceholders(startTitle, context);
        String description = resolvePlaceholders(startDescription, context);
        int color = parseColor(startColor);
        String footer = resolvePlaceholders(startFooter, context);
        
        return new WebhookEmbed(title, description, color, footer, startThumbnail, startAuthorName, startAuthorIcon);
    }
    
    private WebhookEmbed buildEndEmbed(PlaceholderContext context, List<Winner> winners) {
        String title = resolvePlaceholders(endTitle, context);
        String baseDescription = resolvePlaceholders(endDescription, context);
        int color = parseColor(endColor);
        String footer = resolvePlaceholders(endFooter, context);
        
        // Build winners leaderboard
        String winnersSection = buildWinnersSection(winners);
        String fullDescription = baseDescription + "\n\n" + winnersSection;
        
        return new WebhookEmbed(title, fullDescription, color, footer, endThumbnail, "", "");
    }
    
    /**
     * Builds formatted winners leaderboard section.
     */
    public String buildWinnersSection(List<Winner> winners) {
        if (winners == null || winners.isEmpty()) {
            return "**" + winnersTitle + "**\n" + noWinnersMessage;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(winnersTitle).append("**\n");
        
        int displayCount = Math.min(winners.size(), maxWinnersDisplay);
        for (int i = 0; i < displayCount; i++) {
            Winner winner = winners.get(i);
            String medal = getMedal(winner.rank());
            
            String line = winnerFormat
                .replace("{medal}", medal)
                .replace("{rank}", String.valueOf(winner.rank()))
                .replace("{player}", winner.playerName())
                .replace("{count}", String.valueOf(winner.deliveryCount()));
            
            sb.append(line).append("\n");
        }
        
        if (winners.size() > maxWinnersDisplay) {
            sb.append("*...ve ").append(winners.size() - maxWinnersDisplay).append(" kişi daha*");
        }
        
        return sb.toString().trim();
    }
    
    private String getMedal(int rank) {
        return switch (rank) {
            case 1 -> medalFirst;
            case 2 -> medalSecond;
            case 3 -> medalThird;
            default -> medalOther;
        };
    }
    
    @Override
    public CompletableFuture<WebhookResult> sendEmbed(String url, WebhookEmbed embed, boolean mentionEveryone) {
        if (url == null || url.isBlank()) {
            return CompletableFuture.completedFuture(
                WebhookResult.failure(0, "Webhook URL is empty or null")
            );
        }
        
        try {
            String jsonPayload = buildJsonPayload(embed, mentionEveryone);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        LOGGER.info("Webhook sent successfully to " + url);
                        return WebhookResult.success(statusCode);
                    } else {
                        String errorMsg = "Webhook request failed with status " + statusCode;
                        LOGGER.warning(errorMsg + ": " + response.body());
                        return WebhookResult.failure(statusCode, errorMsg);
                    }
                })
                .exceptionally(ex -> {
                    String errorMsg = "Webhook request failed: " + ex.getMessage();
                    LOGGER.log(Level.WARNING, errorMsg, ex);
                    return WebhookResult.connectionError(errorMsg);
                });
        } catch (IllegalArgumentException e) {
            String errorMsg = "Invalid webhook URL: " + e.getMessage();
            LOGGER.warning(errorMsg);
            return CompletableFuture.completedFuture(
                WebhookResult.failure(0, errorMsg)
            );
        }
    }
    
    @Override
    public WebhookEmbed buildEmbed(EmbedConfig config, PlaceholderContext context) {
        if (config == null) {
            return new WebhookEmbed("", "", DEFAULT_COLOR);
        }
        
        String resolvedTitle = resolvePlaceholders(config.title(), context);
        String resolvedDescription = resolvePlaceholders(config.description(), context);
        int colorValue = parseColor(config.color());
        
        return new WebhookEmbed(resolvedTitle, resolvedDescription, colorValue);
    }
    
    @Override
    public int parseColor(String hexColor) {
        if (hexColor == null || hexColor.isBlank()) {
            return DEFAULT_COLOR;
        }
        
        try {
            String colorStr = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
            return Integer.parseInt(colorStr, 16);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid color format: " + hexColor + ", using default");
            return DEFAULT_COLOR;
        }
    }
    
    private String resolvePlaceholders(String text, PlaceholderContext context) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (placeholderEngine == null || context == null) {
            return text;
        }
        return placeholderEngine.resolve(text, context);
    }
    
    String buildJsonPayload(WebhookEmbed embed, boolean mentionEveryone) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        if (mentionEveryone) {
            json.append("\"content\":\"@everyone\",");
        }
        
        json.append("\"embeds\":[{");
        json.append("\"title\":").append(escapeJsonString(embed.title())).append(",");
        json.append("\"description\":").append(escapeJsonString(embed.description())).append(",");
        json.append("\"color\":").append(embed.color());
        
        // Footer
        if (!embed.footer().isEmpty()) {
            json.append(",\"footer\":{\"text\":").append(escapeJsonString(embed.footer())).append("}");
        }
        
        // Thumbnail
        if (!embed.thumbnail().isEmpty()) {
            json.append(",\"thumbnail\":{\"url\":").append(escapeJsonString(embed.thumbnail())).append("}");
        }
        
        // Author
        if (!embed.authorName().isEmpty()) {
            json.append(",\"author\":{\"name\":").append(escapeJsonString(embed.authorName()));
            if (!embed.authorIcon().isEmpty()) {
                json.append(",\"icon_url\":").append(escapeJsonString(embed.authorIcon()));
            }
            json.append("}");
        }
        
        json.append("}]");
        json.append("}");
        return json.toString();
    }
    
    private String escapeJsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
    
    // Helper methods
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return defaultValue;
    }
    
    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (NumberFormatException e) { return defaultValue; }
        }
        return defaultValue;
    }
    
    // Getters for testing
    public boolean isGlobalEnabled() { return globalEnabled; }
    public String getGlobalUrl() { return globalUrl; }
}
