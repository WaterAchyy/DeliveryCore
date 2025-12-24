package com.deliverycore.service;

import com.deliverycore.model.Category;
import com.deliverycore.model.SelectionMode;

import java.util.List;
import java.util.Optional;

/**
 * Service for category resolution and item selection.
 * Handles both random and fixed selection modes for categories and items.
 */
public interface CategoryService {
    
    /**
     * Resolves a category based on the selection mode.
     * For RANDOM mode, selects a random category from available categories.
     * For FIXED mode, returns the category with the specified name.
     *
     * @param mode  the selection mode (RANDOM or FIXED)
     * @param value the category name for FIXED mode, ignored for RANDOM mode
     * @return the resolved category
     * @throws IllegalStateException if no categories are available for RANDOM mode
     * @throws IllegalArgumentException if the specified category is not found for FIXED mode
     */
    Category resolveCategory(SelectionMode mode, String value);
    
    /**
     * Resolves an item from a category based on the selection mode.
     * For RANDOM mode, selects a random item from the category's item list.
     * For FIXED mode, returns the specified item value.
     *
     * @param category the category to select from (used for RANDOM mode)
     * @param mode     the selection mode (RANDOM or FIXED)
     * @param value    the item name for FIXED mode, ignored for RANDOM mode
     * @return the resolved item name
     * @throws IllegalStateException if the category has no items for RANDOM mode
     */
    String resolveItem(Category category, SelectionMode mode, String value);
    
    /**
     * Gets all items from a specific category.
     *
     * @param categoryName the name of the category
     * @return an unmodifiable list of item names, empty if category not found
     */
    List<String> getAllItems(String categoryName);
    
    /**
     * Gets a category by name.
     *
     * @param name the category name
     * @return an Optional containing the category if found
     */
    Optional<Category> getCategory(String name);
    
    /**
     * Gets all available category names.
     *
     * @return an unmodifiable list of category names
     */
    List<String> getAllCategoryNames();
}
