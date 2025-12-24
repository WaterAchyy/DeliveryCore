package com.deliverycore.service;

import com.deliverycore.config.CategoryConfig;
import com.deliverycore.model.Category;
import com.deliverycore.model.SelectionMode;
import net.jqwik.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for CategoryService.
 */
class CategoryServicePropertyTest {
    
    /**
     * Feature: delivery-core, Property 5: Random Category Selection Membership
     * For any set of available categories, when category mode is RANDOM,
     * the selected category should always be a member of the available categories set.
     * Validates: Requirement 2.2
     */
    @Property(tries = 100)
    void randomCategorySelectionMembership(
            @ForAll("nonEmptyCategoryConfig") Map<String, List<String>> categories,
            @ForAll Random testRandom) {
        
        CategoryConfig config = createCategoryConfig(categories);
        CategoryService service = new CategoryServiceImpl(config, testRandom);
        
        Category selected = service.resolveCategory(SelectionMode.RANDOM, null);
        
        assertThat(categories.keySet()).contains(selected.name());
        assertThat(categories.get(selected.name())).isEqualTo(selected.items());
    }
    
    /**
     * Feature: delivery-core, Property 6: Random Item Selection Membership
     * For any category with a non-empty item list, when item mode is RANDOM,
     * the selected item should always be a member of that category's item list.
     * Validates: Requirement 2.3
     */
    @Property(tries = 100)
    void randomItemSelectionMembership(
            @ForAll("nonEmptyCategory") Category category,
            @ForAll Random testRandom) {
        
        Map<String, List<String>> categories = Map.of(category.name(), new ArrayList<>(category.items()));
        CategoryConfig config = createCategoryConfig(categories);
        CategoryService service = new CategoryServiceImpl(config, testRandom);
        
        String selectedItem = service.resolveItem(category, SelectionMode.RANDOM, null);
        
        assertThat(category.items()).contains(selectedItem);
    }
    
    /**
     * Feature: delivery-core, Property 7: Fixed Selection Identity
     * For any selection in FIXED mode with a specified value,
     * the resolved value should be identical to the specified value.
     * Validates: Requirement 2.4
     */
    @Property(tries = 100)
    void fixedSelectionIdentity(
            @ForAll("nonEmptyCategoryConfig") Map<String, List<String>> categories,
            @ForAll("validItemName") String fixedItem) {
        
        CategoryConfig config = createCategoryConfig(categories);
        CategoryService service = new CategoryServiceImpl(config);
        
        // Pick a random category name from the config for FIXED category test
        String categoryName = categories.keySet().iterator().next();
        Category category = service.resolveCategory(SelectionMode.FIXED, categoryName);
        
        // Test FIXED category selection
        assertThat(category.name()).isEqualTo(categoryName);
        
        // Test FIXED item selection - should return exactly the specified value
        String resolvedItem = service.resolveItem(category, SelectionMode.FIXED, fixedItem);
        assertThat(resolvedItem).isEqualTo(fixedItem);
    }

    // ==================== Generators ====================
    
    @Provide
    Arbitrary<Map<String, List<String>>> nonEmptyCategoryConfig() {
        Arbitrary<String> categoryName = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
        Arbitrary<List<String>> items = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(30)
            .list()
            .ofMinSize(1)
            .ofMaxSize(20);
        
        return Combinators.combine(categoryName, items)
            .as((name, itemList) -> Map.of(name, itemList))
            .list()
            .ofMinSize(1)
            .ofMaxSize(10)
            .map(maps -> {
                Map<String, List<String>> result = new HashMap<>();
                for (Map<String, List<String>> m : maps) {
                    result.putAll(m);
                }
                return result;
            })
            .filter(m -> !m.isEmpty());
    }
    
    @Provide
    Arbitrary<Category> nonEmptyCategory() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
                .list().ofMinSize(1).ofMaxSize(20)
        ).as(Category::new);
    }
    
    @Provide
    Arbitrary<String> validItemName() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }
    
    // ==================== Helper Methods ====================
    
    private CategoryConfig createCategoryConfig(Map<String, List<String>> categories) {
        Map<String, Category> categoryMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            categoryMap.put(entry.getKey(), new Category(entry.getKey(), entry.getValue()));
        }
        
        return new CategoryConfig() {
            @Override
            public Map<String, Category> getCategories() {
                return Collections.unmodifiableMap(categoryMap);
            }
            
            @Override
            public Optional<Category> getCategory(String name) {
                return Optional.ofNullable(categoryMap.get(name));
            }
            
            @Override
            public List<String> getCategoryNames() {
                return List.copyOf(categoryMap.keySet());
            }
        };
    }
}
