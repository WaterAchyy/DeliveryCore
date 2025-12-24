package com.deliverycore.placeholder;

import com.deliverycore.model.PlaceholderContext;

import java.util.Set;

/**
 * Engine for resolving placeholder tokens in text strings.
 * Supports registration of custom placeholders and resolution
 * of all standard DeliveryCore placeholders.
 */
public interface PlaceholderEngine {
    
    /**
     * Resolves all placeholder tokens in the given text using the provided context.
     * Unknown placeholders are replaced with empty strings.
     *
     * @param text    the text containing placeholder tokens (e.g., "Hello {player}!")
     * @param context the context containing values for placeholder resolution
     * @return the text with all placeholders resolved
     */
    String resolve(String text, PlaceholderContext context);
    
    /**
     * Registers a custom placeholder resolver.
     *
     * @param key      the placeholder key without braces (e.g., "player" for {player})
     * @param resolver the resolver function
     */
    void registerPlaceholder(String key, PlaceholderResolver resolver);
    
    /**
     * Gets all registered placeholder keys.
     *
     * @return an unmodifiable set of registered placeholder keys
     */
    Set<String> getRegisteredPlaceholders();
    
    /**
     * Extracts all placeholder tokens from the given text.
     *
     * @param text the text to extract placeholders from
     * @return a set of placeholder keys found in the text (without braces)
     */
    Set<String> extractPlaceholders(String text);
}
