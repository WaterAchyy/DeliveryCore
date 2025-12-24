package com.deliverycore.model;

import java.util.Objects;

/**
 * Configuration for how a category or item is selected.
 *
 * @param mode  the selection mode (RANDOM or FIXED)
 * @param value the value to use when mode is FIXED, or null for RANDOM
 */
public record SelectionConfig(
    SelectionMode mode,
    String value
) {
    /**
     * Creates a new SelectionConfig with validation.
     *
     * @param mode  the selection mode
     * @param value the value for FIXED mode
     * @throws NullPointerException if mode is null
     */
    public SelectionConfig {
        Objects.requireNonNull(mode, "Selection mode cannot be null");
    }
    
    /**
     * Creates a RANDOM selection config.
     *
     * @return a new SelectionConfig with RANDOM mode
     */
    public static SelectionConfig random() {
        return new SelectionConfig(SelectionMode.RANDOM, null);
    }
    
    /**
     * Creates a FIXED selection config with the specified value.
     *
     * @param value the fixed value to use
     * @return a new SelectionConfig with FIXED mode
     */
    public static SelectionConfig fixed(String value) {
        return new SelectionConfig(SelectionMode.FIXED, value);
    }
}
