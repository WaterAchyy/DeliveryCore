package com.deliverycore.util;

import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.ValidationError;
import com.deliverycore.placeholder.PlaceholderEngine;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logging service for DeliveryCore.
 * Provides structured logging for events and configuration errors.
 */
public class LoggingService {
    
    private final Logger logger;
    private final PlaceholderEngine placeholderEngine;
    
    public LoggingService(Logger logger, PlaceholderEngine placeholderEngine) {
        this.logger = logger != null ? logger : Logger.getLogger(LoggingService.class.getName());
        this.placeholderEngine = placeholderEngine;
    }
    
    public LoggingService(Logger logger) {
        this(logger, null);
    }
    
    public void logEventStart(String deliveryName, String category, String item) {
        logger.info(String.format("[EVENT START] Delivery: %s | Category: %s | Item: %s",
            deliveryName, category, item));
    }
    
    public void logEventEnd(String deliveryName, String category, String item, int winnerCount) {
        logger.info(String.format("[EVENT END] Delivery: %s | Category: %s | Item: %s | Winners: %d",
            deliveryName, category, item, winnerCount));
    }
    
    public void logConfigError(ValidationError error) {
        if (error == null) return;
        
        String fieldInfo = error.field() != null ? error.field() : "general";
        String message = String.format("[CONFIG %s] File: %s | Field: %s | Error: %s",
            error.severity().name(), error.file(), fieldInfo, error.message());
        
        switch (error.severity()) {
            case CRITICAL -> logger.severe(message);
            case ERROR -> logger.warning(message);
            case WARNING -> logger.info(message);
        }
    }
    
    public void logConfigErrors(List<ValidationError> errors) {
        if (errors == null || errors.isEmpty()) return;
        errors.forEach(this::logConfigError);
    }
    
    public void info(String message) {
        logger.info(message);
    }
    
    public void info(String message, PlaceholderContext context) {
        logger.info(resolvePlaceholders(message, context));
    }
    
    public void warning(String message) {
        logger.warning(message);
    }
    
    public void severe(String message) {
        logger.severe(message);
    }
    
    public void severe(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
    
    private String resolvePlaceholders(String message, PlaceholderContext context) {
        if (message == null) return "";
        if (placeholderEngine == null || context == null) return message;
        return placeholderEngine.resolve(message, context);
    }
    
    public Logger getLogger() {
        return logger;
    }
}
