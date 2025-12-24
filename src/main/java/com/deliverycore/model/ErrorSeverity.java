package com.deliverycore.model;

/**
 * Defines the severity level of configuration validation errors.
 */
public enum ErrorSeverity {
    /**
     * Non-critical issue, operation can continue with defaults.
     */
    WARNING,
    
    /**
     * Error that affects functionality but doesn't prevent operation.
     */
    ERROR,
    
    /**
     * Critical error that prevents the affected component from operating.
     */
    CRITICAL
}
