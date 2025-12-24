package com.deliverycore.config;

import com.deliverycore.model.ValidationError;

import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a configuration reload operation.
 * Contains success status and any validation errors encountered.
 * 
 * Requirements: 8.3, 8.4
 */
public record ReloadResult(
    boolean success,
    List<ValidationError> errors
) {
    /**
     * Creates a ReloadResult with validation.
     *
     * @param success whether the reload was successful
     * @param errors  the list of validation errors
     */
    public ReloadResult {
        errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }
    
    /**
     * Creates a successful reload result with no errors.
     *
     * @return a successful ReloadResult
     */
    public static ReloadResult ok() {
        return new ReloadResult(true, Collections.emptyList());
    }
    
    /**
     * Checks if the reload was successful.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Creates a successful reload result with warnings.
     *
     * @param warnings the list of warning-level errors
     * @return a successful ReloadResult with warnings
     */
    public static ReloadResult successWithWarnings(List<ValidationError> warnings) {
        return new ReloadResult(true, warnings);
    }
    
    /**
     * Creates a failed reload result with errors.
     *
     * @param errors the list of validation errors
     * @return a failed ReloadResult
     */
    public static ReloadResult failure(List<ValidationError> errors) {
        return new ReloadResult(false, errors);
    }
    
    /**
     * Checks if there are any errors (regardless of success status).
     *
     * @return true if there are errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Gets the count of errors.
     *
     * @return the number of errors
     */
    public int errorCount() {
        return errors.size();
    }
}
