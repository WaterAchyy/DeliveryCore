package com.deliverycore.service;

import com.deliverycore.model.PlaceholderContext;

/**
 * Service for managing multi-language messages with placeholder support.
 * Supports Turkish (tr) and English (en) languages with Turkish as fallback.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
public interface MessageService {
    
    /**
     * Gets a message by key for the specified locale.
     * Falls back to Turkish if the message is not found in the requested locale.
     *
     * @param key    the message key
     * @param locale the locale (tr or en)
     * @return the message, or the key itself if not found in any locale
     */
    String getMessage(String key, String locale);
    
    /**
     * Gets a message by key for the specified locale with placeholder resolution.
     * Falls back to Turkish if the message is not found in the requested locale.
     * All placeholders in the message are resolved using the provided context.
     *
     * @param key     the message key
     * @param locale  the locale (tr or en)
     * @param context the placeholder context for resolution
     * @return the message with placeholders resolved, or the key if not found
     */
    String getMessage(String key, String locale, PlaceholderContext context);
    
    /**
     * Sends a message to a player with placeholder resolution.
     * The message is retrieved using the specified key and locale,
     * placeholders are resolved, and the result is sent to the player.
     *
     * @param playerName the name of the player to send the message to
     * @param key        the message key
     * @param locale     the locale (tr or en)
     * @param context    the placeholder context for resolution
     */
    void sendMessage(String playerName, String key, String locale, PlaceholderContext context);
    
    /**
     * Sends a raw message to a player without placeholder resolution.
     *
     * @param playerName the name of the player to send the message to
     * @param message    the message to send
     */
    void sendRawMessage(String playerName, String message);
}
