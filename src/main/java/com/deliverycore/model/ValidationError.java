package com.deliverycore.model;

import java.util.Objects;

/**
 * Represents a configuration validation error.
 *
 * @param file     the configuration file where the error occurred
 * @param field    the specific field with the error
 * @param message  a descriptive error message
 * @param severity the error severity level
 */
public record ValidationError(
    String file,
    String field,
    String message,
    ErrorSeverity severity
) {
    /**
     * Creates a new ValidationError with validation.
     *
     * @param file     the file name
     * @param field    the field name (can be null for general errors)
     * @param message  the error message
     * @param severity the severity level
     * @throws NullPointerException if file, message, or severity is null
     */
    public ValidationError {
        Objects.requireNonNull(file, "File cannot be null");
        // field can be null for general file-level errors
        Objects.requireNonNull(message, "Message cannot be null");
        Objects.requireNonNull(severity, "Severity cannot be null");
    }
    
    /**
     * Creates a warning-level validation error.
     *
     * @param file    the file name
     * @param field   the field name
     * @param message the error message
     * @return a new ValidationError with WARNING severity
     */
    public static ValidationError warning(String file, String field, String message) {
        return new ValidationError(file, field, message, ErrorSeverity.WARNING);
    }
    
    /**
     * Creates an error-level validation error.
     *
     * @param file    the file name
     * @param field   the field name
     * @param message the error message
     * @return a new ValidationError with ERROR severity
     */
    public static ValidationError error(String file, String field, String message) {
        return new ValidationError(file, field, message, ErrorSeverity.ERROR);
    }
    
    /**
     * Creates a critical-level validation error.
     *
     * @param file    the file name
     * @param field   the field name
     * @param message the error message
     * @return a new ValidationError with CRITICAL severity
     */
    public static ValidationError critical(String file, String field, String message) {
        return new ValidationError(file, field, message, ErrorSeverity.CRITICAL);
    }
}
