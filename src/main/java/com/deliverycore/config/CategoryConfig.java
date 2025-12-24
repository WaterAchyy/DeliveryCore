package com.deliverycore.config;

import com.deliverycore.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for accessing category configuration data from categories.yml.
 * Provides methods to retrieve categories and their associated items.
 */
public interface CategoryConfig {
    
    /**
     * Gets all categories as a map.
     *
     * @return an unmodifiable map of category names to Category objects
     */
    Map<String, Category> getCategories();
    
    /**
     * Gets a specific category by name.
     *
     * @param name the category name
     * @return an Optional containing the category if found, empty otherwise
     */
    Optional<Category> getCategory(String name);
    
    /**
     * Gets all category names.
     *
     * @return an unmodifiable list of category names
     */
    List<String> getCategoryNames();
}
