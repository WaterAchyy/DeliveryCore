package com.deliverycore.placeholder;

import com.deliverycore.model.PlaceholderContext;

/**
 * Functional interface for resolving a placeholder value from context.
 */
@FunctionalInterface
public interface PlaceholderResolver {
    /**
     * Resolves a placeholder value from the given context.
     *
     * @param context the placeholder context containing all available values
     * @return the resolved value, or empty string if the value is not available
     */
    String resolve(PlaceholderContext context);
}
