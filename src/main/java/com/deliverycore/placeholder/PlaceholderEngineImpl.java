package com.deliverycore.placeholder;

import com.deliverycore.model.PlaceholderContext;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of PlaceholderEngine.
 * Registers all standard DeliveryCore placeholders and supports custom placeholders.
 */
public class PlaceholderEngineImpl implements PlaceholderEngine {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Map<String, PlaceholderResolver> resolvers = new HashMap<>();
    
    /**
     * Creates a new PlaceholderEngineImpl with all standard placeholders registered.
     */
    public PlaceholderEngineImpl() {
        registerStandardPlaceholders();
    }
    
    private void registerStandardPlaceholders() {
        // Requirement 3.2: {category} placeholder
        registerPlaceholder("category", ctx -> 
            ctx.category() != null ? ctx.category() : "");
        
        // Requirement 3.3: {item} placeholder
        registerPlaceholder("item", ctx -> 
            ctx.item() != null ? ctx.item() : "");
        
        // Requirement 3.4: {player} placeholder
        registerPlaceholder("player", ctx -> 
            ctx.playerName() != null ? ctx.playerName() : "");
        
        // Requirement 3.5: {player_uuid} placeholder
        registerPlaceholder("player_uuid", ctx -> 
            ctx.playerUuid() != null ? ctx.playerUuid().toString() : "");
        
        // Requirement 3.6: {delivery_name} placeholder
        registerPlaceholder("delivery_name", ctx -> 
            ctx.deliveryName() != null ? ctx.deliveryName() : "");
        
        // Requirement 3.7: {start_time} placeholder
        registerPlaceholder("start_time", ctx -> 
            ctx.startTime() != null ? ctx.startTime().format(TIME_FORMATTER) : "");
        
        // Requirement 3.8: {end_time} placeholder
        registerPlaceholder("end_time", ctx -> 
            ctx.endTime() != null ? ctx.endTime().format(TIME_FORMATTER) : "");
        
        // Requirement 3.9: {timezone} placeholder
        registerPlaceholder("timezone", ctx -> 
            ctx.timezone() != null ? ctx.timezone().getId() : "");

        // Requirement 3.10: {winner_count} placeholder
        registerPlaceholder("winner_count", ctx -> 
            String.valueOf(ctx.winnerCount()));
        
        // Requirement 3.11: {winners} placeholder
        registerPlaceholder("winners", ctx -> 
            ctx.winners() != null && !ctx.winners().isEmpty() 
                ? String.join(", ", ctx.winners()) 
                : "");
        
        // Requirement 3.12: {delivery_amount} placeholder
        registerPlaceholder("delivery_amount", ctx -> 
            String.valueOf(ctx.deliveryAmount()));
        
        // {leaderboard} placeholder - Sıralı kazanan listesi (madalyalı, teslimat sayılı)
        registerPlaceholder("leaderboard", ctx -> {
            if (ctx.winnerDetails() == null || ctx.winnerDetails().isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (var winner : ctx.winnerDetails()) {
                String medal = switch (winner.rank()) {
                    case 1 -> "🥇";
                    case 2 -> "🥈";
                    case 3 -> "🥉";
                    default -> "#" + winner.rank();
                };
                sb.append(medal)
                  .append(" ")
                  .append(winner.playerName())
                  .append(" - ")
                  .append(winner.deliveryCount())
                  .append(" teslimat\n");
            }
            return sb.toString().trim();
        });
    }
    
    @Override
    public String resolve(String text, PlaceholderContext context) {
        if (text == null || text.isEmpty()) {
            return text != null ? text : "";
        }
        
        if (context == null) {
            context = PlaceholderContext.empty();
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String key = matcher.group(1);
            PlaceholderResolver resolver = resolvers.get(key);
            
            // Requirement 3.13: Unknown placeholders return empty string
            String replacement = "";
            if (resolver != null) {
                replacement = resolver.resolve(context);
                if (replacement == null) {
                    replacement = "";
                }
            }
            
            // Escape special regex characters in replacement
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    @Override
    public void registerPlaceholder(String key, PlaceholderResolver resolver) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Placeholder key cannot be null or empty");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("Placeholder resolver cannot be null");
        }
        resolvers.put(key, resolver);
    }
    
    @Override
    public Set<String> getRegisteredPlaceholders() {
        return Collections.unmodifiableSet(new HashSet<>(resolvers.keySet()));
    }
    
    @Override
    public Set<String> extractPlaceholders(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> placeholders = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        
        return Collections.unmodifiableSet(placeholders);
    }
}
