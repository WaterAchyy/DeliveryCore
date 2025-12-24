package com.deliverycore.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a category of items that can be delivered.
 * Categories group related items together for delivery events.
 *
 * @param name   the unique name of the category
 * @param items  the list of item identifiers in this category
 * @param prices the price map for sellable items (item -> price)
 */
public record Category(
    String name,
    List<String> items,
    Map<String, Double> prices
) {
    /**
     * Creates a new Category with validation.
     *
     * @param name   the unique name of the category
     * @param items  the list of item identifiers in this category
     * @param prices the price map for sellable items
     * @throws NullPointerException if name or items is null
     */
    public Category {
        Objects.requireNonNull(name, "Category name cannot be null");
        Objects.requireNonNull(items, "Category items cannot be null");
        items = List.copyOf(items); // Defensive copy for immutability
        prices = prices != null ? Map.copyOf(prices) : Map.of();
    }
    
    /**
     * Creates a new Category without prices (backward compatibility).
     *
     * @param name  the unique name of the category
     * @param items the list of item identifiers in this category
     */
    public Category(String name, List<String> items) {
        this(name, items, Map.of());
    }
    
    /**
     * Gets the price of an item in this category.
     *
     * @param item the item name
     * @return the price, or 0 if not defined
     */
    public double getPrice(String item) {
        return prices.getOrDefault(item, 0.0);
    }
    
    /**
     * Checks if an item has a price defined.
     *
     * @param item the item name
     * @return true if price is defined
     */
    public boolean hasPrice(String item) {
        return prices.containsKey(item);
    }
}
