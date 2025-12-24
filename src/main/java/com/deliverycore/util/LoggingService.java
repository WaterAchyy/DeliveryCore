package com.deliverycore.util;

import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.ValidationError;
import com.deliverycore.placeholder.PlaceholderEngine;
import com.deliverycore.webhook.WebhookResult;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logging service for DeliveryCore.
 * Provides structured logging for events, configuration errors, and webhook results.
 * Supports placeholder resolution in log messages.
 * 
 * Requirements: 12.1, 12.2, 12.3, 12.4
 */
public class LoggingService {
    
    private final Logger logger;
    private final PlaceholderEngine placeholderEngine;
    
    /**
     * Creates a LoggingService with the specified logger and placeholder engine.
     *
     * @param logger            the logger to use for output
     * @param placeholderEngine the engine for resolving placeholders (can be null)
     */
    public LoggingService(Logger logger, PlaceholderEngine placeholderEngine) {
        this.logger = logger != null ? logger : Logger.getLogger(LoggingService.class.getName());
        this.placeholderEngine = placeholderEngine;
    }
    
    /**
     * Creates a LoggingService with only a logger (no placeholder resolution).
     *
     * @param logger the logger to use for output
     */
    public LoggingService(Logger logger) {
        this(logger, null);
    }
    
    // ==================== Event Logging (Requirement 12.1) ====================
    
    /**
     * Logs the start of a delivery event.
     * Requirement 12.1: Log event details including delivery name, category, and item.
     *
     * @param deliveryName the name of the delivery
     * @param category     the resolved category name
     * @param item         the resolved item name
     */
    public void logEventStart(String deliveryName, String category, String item) {
        logger.info(String.format("[EVENT START] Delivery: %s | Category: %s | Item: %s",
            deliveryName, category, item));
    }

    /**
     * Logs the start of a delivery event with context for placeholder resolution.
     * Requirement 12.1, 12.4: Log event details with placeholder resolution.
     *
     * @param deliveryName the name of the delivery
     * @param category     the resolved category name
     * @param item         the resolved item name
     * @param context      the placeholder context for additional resolution
     */
    public void logEventStart(String deliveryName, String category, String item, PlaceholderContext context) {
        String message = String.format("[EVENT START] Delivery: %s | Category: %s | Item: %s",
            deliveryName, category, item);
        logger.info(resolvePlaceholders(message, context));
    }
    
    /**
     * Logs the end of a delivery event.
     * Requirement 12.1: Log event details including delivery name, category, and item.
     *
     * @param deliveryName the name of the delivery
     * @param category     the resolved category name
     * @param item         the resolved item name
     * @param winnerCount  the number of winners
     */
    public void logEventEnd(String deliveryName, String category, String item, int winnerCount) {
        logger.info(String.format("[EVENT END] Delivery: %s | Category: %s | Item: %s | Winners: %d",
            deliveryName, category, item, winnerCount));
    }
    
    /**
     * Logs the end of a delivery event with winner details.
     * Requirement 12.1: Log event details including delivery name, category, and item.
     *
     * @param deliveryName the name of the delivery
     * @param category     the resolved category name
     * @param item         the resolved item name
     * @param winners      the list of winner names
     */
    public void logEventEnd(String deliveryName, String category, String item, List<String> winners) {
        String winnerList = winners != null && !winners.isEmpty() 
            ? String.join(", ", winners) 
            : "none";
        logger.info(String.format("[EVENT END] Delivery: %s | Category: %s | Item: %s | Winners: %s",
            deliveryName, category, item, winnerList));
    }
    
    /**
     * Logs the end of a delivery event with context for placeholder resolution.
     * Requirement 12.1, 12.4: Log event details with placeholder resolution.
     *
     * @param deliveryName the name of the delivery
     * @param category     the resolved category name
     * @param item         the resolved item name
     * @param winnerCount  the number of winners
     * @param context      the placeholder context for additional resolution
     */
    public void logEventEnd(String deliveryName, String category, String item, int winnerCount, PlaceholderContext context) {
        String message = String.format("[EVENT END] Delivery: %s | Category: %s | Item: %s | Winners: %d",
            deliveryName, category, item, winnerCount);
        logger.info(resolvePlaceholders(message, context));
    }
    
    // ==================== Configuration Error Logging (Requirement 12.2) ====================
    
    /**
     * Logs a configuration error with file, field, and error details.
     * Requirement 12.2: Log specific file, field, and nature of the error.
     *
     * @param error the validation error to log
     */
    public void logConfigError(ValidationError error) {
        if (error == null) {
            return;
        }
        
        String fieldInfo = error.field() != null ? error.field() : "general";
        String message = String.format("[CONFIG %s] File: %s | Field: %s | Error: %s",
            error.severity().name(),
            error.file(),
            fieldInfo,
            error.message());
        
        switch (error.severity()) {
            case CRITICAL -> logger.severe(message);
            case ERROR -> logger.warning(message);
            case WARNING -> logger.info(message);
        }
    }
    
    /**
     * Logs multiple configuration errors.
     * Requirement 12.2: Log specific file, field, and nature of the error.
     *
     * @param errors the list of validation errors to log
     */
    public void logConfigErrors(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        
        for (ValidationError error : errors) {
            logConfigError(error);
        }
    }
    
    /**
     * Logs a configuration parsing error.
     * Requirement 12.2: Log specific file, field, and nature of the error.
     *
     * @param file    the configuration file name
     * @param line    the line number (or -1 if unknown)
     * @param message the error message
     */
    public void logConfigParseError(String file, int line, String message) {
        String lineInfo = line >= 0 ? " (line " + line + ")" : "";
        logger.severe(String.format("[CONFIG PARSE ERROR] File: %s%s | Error: %s",
            file, lineInfo, message));
    }
    
    /**
     * Logs a configuration file not found warning.
     * Requirement 12.2: Log specific file, field, and nature of the error.
     *
     * @param file the missing configuration file name
     */
    public void logConfigFileNotFound(String file) {
        logger.warning(String.format("[CONFIG WARNING] File not found: %s - using defaults", file));
    }

    // ==================== Webhook Logging (Requirement 12.3) ====================
    
    /**
     * Logs a webhook result (success or failure).
     * Requirement 12.3: Log the result (success or failure with details).
     *
     * @param url    the webhook URL (will be partially masked for security)
     * @param result the webhook result
     */
    public void logWebhookResult(String url, WebhookResult result) {
        if (result == null) {
            return;
        }
        
        String maskedUrl = maskWebhookUrl(url);
        
        if (result.success()) {
            logger.info(String.format("[WEBHOOK SUCCESS] URL: %s | Status: %d",
                maskedUrl, result.statusCode()));
        } else {
            logger.warning(String.format("[WEBHOOK FAILURE] URL: %s | Status: %d | Error: %s",
                maskedUrl, result.statusCode(), result.message()));
        }
    }
    
    /**
     * Logs a webhook send attempt.
     * Requirement 12.3: Log webhook operations.
     *
     * @param url           the webhook URL (will be partially masked)
     * @param eventType     the type of event (start/end)
     * @param deliveryName  the delivery name
     */
    public void logWebhookSend(String url, String eventType, String deliveryName) {
        String maskedUrl = maskWebhookUrl(url);
        logger.info(String.format("[WEBHOOK SEND] Type: %s | Delivery: %s | URL: %s",
            eventType, deliveryName, maskedUrl));
    }
    
    /**
     * Logs a webhook connection error.
     * Requirement 12.3: Log the result (success or failure with details).
     *
     * @param url     the webhook URL (will be partially masked)
     * @param message the error message
     */
    public void logWebhookConnectionError(String url, String message) {
        String maskedUrl = maskWebhookUrl(url);
        logger.warning(String.format("[WEBHOOK CONNECTION ERROR] URL: %s | Error: %s",
            maskedUrl, message));
    }
    
    // ==================== General Logging with Placeholders (Requirement 12.4) ====================
    
    /**
     * Logs an info message with placeholder resolution.
     * Requirement 12.4: Resolve placeholders before writing to log.
     *
     * @param message the message (may contain placeholders)
     * @param context the placeholder context
     */
    public void info(String message, PlaceholderContext context) {
        logger.info(resolvePlaceholders(message, context));
    }
    
    /**
     * Logs an info message.
     *
     * @param message the message
     */
    public void info(String message) {
        logger.info(message);
    }
    
    /**
     * Logs a warning message with placeholder resolution.
     * Requirement 12.4: Resolve placeholders before writing to log.
     *
     * @param message the message (may contain placeholders)
     * @param context the placeholder context
     */
    public void warning(String message, PlaceholderContext context) {
        logger.warning(resolvePlaceholders(message, context));
    }
    
    /**
     * Logs a warning message.
     *
     * @param message the message
     */
    public void warning(String message) {
        logger.warning(message);
    }
    
    /**
     * Logs a severe error message with placeholder resolution.
     * Requirement 12.4: Resolve placeholders before writing to log.
     *
     * @param message the message (may contain placeholders)
     * @param context the placeholder context
     */
    public void severe(String message, PlaceholderContext context) {
        logger.severe(resolvePlaceholders(message, context));
    }
    
    /**
     * Logs a severe error message.
     *
     * @param message the message
     */
    public void severe(String message) {
        logger.severe(message);
    }
    
    /**
     * Logs a severe error message with exception.
     *
     * @param message   the message
     * @param throwable the exception
     */
    public void severe(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Resolves placeholders in a message using the placeholder engine.
     * Requirement 12.4: Resolve placeholders before writing to log.
     *
     * @param message the message with potential placeholders
     * @param context the placeholder context
     * @return the resolved message
     */
    private String resolvePlaceholders(String message, PlaceholderContext context) {
        if (message == null) {
            return "";
        }
        if (placeholderEngine == null || context == null) {
            return message;
        }
        return placeholderEngine.resolve(message, context);
    }
    
    /**
     * Masks a webhook URL for security in logs.
     * Shows only the first part of the URL to identify the service.
     *
     * @param url the full webhook URL
     * @return the masked URL
     */
    private String maskWebhookUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "[empty]";
        }
        
        // Show domain and mask the rest
        int protocolEnd = url.indexOf("://");
        if (protocolEnd < 0) {
            return url.length() > 20 ? url.substring(0, 20) + "..." : url;
        }
        
        int pathStart = url.indexOf("/", protocolEnd + 3);
        if (pathStart < 0) {
            return url;
        }
        
        String domain = url.substring(0, pathStart);
        return domain + "/***";
    }
    
    /**
     * Gets the underlying logger.
     *
     * @return the logger instance
     */
    public Logger getLogger() {
        return logger;
    }
}
