package com.deliverycore.service;

import com.deliverycore.config.CategoryConfig;
import com.deliverycore.model.Category;
import com.deliverycore.model.SelectionMode;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Default implementation of CategoryService.
 * Provides category and item resolution with random selection support.
 */
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryConfig categoryConfig;
    private final Random random;
    
    /**
     * Creates a new CategoryServiceImpl with the given configuration.
     *
     * @param categoryConfig the category configuration provider
     */
    public CategoryServiceImpl(CategoryConfig categoryConfig) {
        this(categoryConfig, new Random());
    }
    
    /**
     * Creates a new CategoryServiceImpl with custom random source (for testing).
     *
     * @param categoryConfig the category configuration provider
     * @param random         the random number generator
     */
    public CategoryServiceImpl(CategoryConfig categoryConfig, Random random) {
        this.categoryConfig = categoryConfig;
        this.random = random;
    }
    
    @Override
    public Category resolveCategory(SelectionMode mode, String value) {
        if (mode == SelectionMode.FIXED) {
            return categoryConfig.getCategory(value)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Category not found: " + value));
        }
        
        // RANDOM mode
        List<String> categoryNames = categoryConfig.getCategoryNames();
        if (categoryNames.isEmpty()) {
            throw new IllegalStateException("No categories available for random selection");
        }
        
        int index = random.nextInt(categoryNames.size());
        String selectedName = categoryNames.get(index);
        return categoryConfig.getCategory(selectedName)
            .orElseThrow(() -> new IllegalStateException(
                "Category disappeared during selection: " + selectedName));
    }
    
    @Override
    public String resolveItem(Category category, SelectionMode mode, String value) {
        if (mode == SelectionMode.FIXED) {
            return value;
        }
        
        // RANDOM mode
        List<String> items = category.items();
        if (items.isEmpty()) {
            throw new IllegalStateException(
                "Category '" + category.name() + "' has no items for random selection");
        }
        
        int index = random.nextInt(items.size());
        return items.get(index);
    }
    
    @Override
    public List<String> getAllItems(String categoryName) {
        return categoryConfig.getCategory(categoryName)
            .map(Category::items)
            .orElse(List.of());
    }
    
    @Override
    public Optional<Category> getCategory(String name) {
        return categoryConfig.getCategory(name);
    }
    
    @Override
    public List<String> getAllCategoryNames() {
        return categoryConfig.getCategoryNames();
    }
}
