package com.deliverycore.service;

import com.deliverycore.config.LanguageConfig;
import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.placeholder.PlaceholderEngine;

import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Implementation of MessageService that integrates LanguageConfig with PlaceholderEngine.
 * Supports Turkish (tr) and English (en) languages with Turkish as fallback.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
public class MessageServiceImpl implements MessageService {
    
    private final LanguageConfig languageConfig;
    private final PlaceholderEngine placeholderEngine;
    private final BiConsumer<String, String> messageSender;
    private final Logger logger;
    
    /**
     * Creates a MessageServiceImpl with the specified dependencies.
     *
     * @param languageConfig    the language configuration for message retrieval
     * @param placeholderEngine the placeholder engine for resolving placeholders
     * @param messageSender     a function that sends messages to players (playerName, message)
     * @param logger            the logger for logging operations
     */
    public MessageServiceImpl(
            LanguageConfig languageConfig,
            PlaceholderEngine placeholderEngine,
            BiConsumer<String, String> messageSender,
            Logger logger) {
        this.languageConfig = languageConfig;
        this.placeholderEngine = placeholderEngine;
        this.messageSender = messageSender;
        this.logger = logger;
    }
    
    /**
     * Creates a MessageServiceImpl with a no-op message sender (for testing).
     *
     * @param languageConfig    the language configuration for message retrieval
     * @param placeholderEngine the placeholder engine for resolving placeholders
     */
    public MessageServiceImpl(
            LanguageConfig languageConfig,
            PlaceholderEngine placeholderEngine) {
        this(languageConfig, placeholderEngine, (name, msg) -> {}, 
             Logger.getLogger(MessageServiceImpl.class.getName()));
    }
    
    @Override
    public String getMessage(String key, String locale) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        
        String effectiveLocale = normalizeLocale(locale);
        return languageConfig.getMessageWithFallback(key, effectiveLocale);
    }
    
    @Override
    public String getMessage(String key, String locale, PlaceholderContext context) {
        String message = getMessage(key, locale);
        
        if (message == null || message.isEmpty() || message.equals(key)) {
            return message;
        }
        
        PlaceholderContext effectiveContext = context != null ? context : PlaceholderContext.empty();
        return placeholderEngine.resolve(message, effectiveContext);
    }
    
    @Override
    public void sendMessage(String playerName, String key, String locale, PlaceholderContext context) {
        if (playerName == null || playerName.isEmpty()) {
            logger.warning("Cannot send message: player name is null or empty");
            return;
        }
        
        String message = getMessage(key, locale, context);
        
        if (message != null && !message.isEmpty()) {
            messageSender.accept(playerName, message);
            logger.fine(() -> String.format("Sent message to %s: %s", playerName, message));
        }
    }
    
    @Override
    public void sendRawMessage(String playerName, String message) {
        if (playerName == null || playerName.isEmpty()) {
            logger.warning("Cannot send message: player name is null or empty");
            return;
        }
        
        if (message != null && !message.isEmpty()) {
            messageSender.accept(playerName, message);
            logger.fine(() -> String.format("Sent raw message to %s: %s", playerName, message));
        }
    }
    
    /**
     * Normalizes the locale to a supported value.
     * Returns Turkish as default if locale is null or unsupported.
     *
     * @param locale the locale to normalize
     * @return the normalized locale
     */
    private String normalizeLocale(String locale) {
        if (locale == null || locale.isEmpty()) {
            return LanguageConfig.DEFAULT_LOCALE;
        }
        
        String normalized = locale.toLowerCase().trim();
        
        if (languageConfig.getSupportedLocales().contains(normalized)) {
            return normalized;
        }
        
        // Handle common locale variations
        if (normalized.startsWith("en")) {
            return LanguageConfig.ENGLISH;
        }
        if (normalized.startsWith("tr")) {
            return LanguageConfig.TURKISH;
        }
        
        return LanguageConfig.DEFAULT_LOCALE;
    }
}
