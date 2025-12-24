package com.deliverycore.config;

import com.deliverycore.model.Category;
import net.jqwik.api.*;
import net.jqwik.api.Combinators;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for CategoryConfig.
 */
class CategoryConfigPropertyTest {
    
    /**
     * Feature: delivery-core, Property 1: Category Configuration Round-Trip
     * Validates: Requirements 1.1
     * 
     * For any valid category configuration, serializing to YAML and parsing back
     * should produce an equivalent data structure with the same categories and items.
     */
    @Property(tries = 100)
    void categoryConfigRoundTrip(
            @ForAll("validCategoryConfig") Map<String, List<String>> categories) {
        
        // Create config from map
        CategoryConfigImpl original = new CategoryConfigImpl(categories);
        
        // Serialize to YAML
        String yaml = original.toYaml();
        
        // Parse back from YAML
        CategoryConfigImpl parsed = CategoryConfigImpl.fromYaml(yaml);
        
        // Verify all categories are preserved
        assertThat(parsed.getCategoryNames())
            .containsExactlyInAnyOrderElementsOf(original.getCategoryNames());
        
        // Verify each category has the same items
        for (String categoryName : original.getCategoryNames()) {
            Category originalCategory = original.getCategory(categoryName).orElseThrow();
            Category parsedCategory = parsed.getCategory(categoryName).orElseThrow();
            
            assertThat(parsedCategory.items())
                .containsExactlyInAnyOrderElementsOf(originalCategory.items());
        }
    }
    
    /**
     * Feature: delivery-core, Property 2: Item Name Validation
     * Validates: Requirements 1.3
     * 
     * For any valid vanilla Minecraft item name or namespace:item format,
     * the validator should accept the item as valid.
     */
    @Property(tries = 100)
    void validItemNamesAreAccepted(
            @ForAll("validItemNames") String itemName) {
        
        assertThat(ItemValidator.isValidItemName(itemName))
            .as("Item name '%s' should be valid", itemName)
            .isTrue();
    }
    
    /**
     * Feature: delivery-core, Property 2: Item Name Validation (negative)
     * Validates: Requirements 1.3
     * 
     * Invalid item names should be rejected.
     */
    @Property(tries = 100)
    void invalidItemNamesAreRejected(
            @ForAll("invalidItemNames") String itemName) {
        
        assertThat(ItemValidator.isValidItemName(itemName))
            .as("Item name '%s' should be invalid", itemName)
            .isFalse();
    }

    /**
     * Feature: delivery-core, Property 3: Category Reload Consistency
     * Validates: Requirements 1.4
     * 
     * For any category configuration, after a reload operation, the in-memory
     * category data should exactly match the newly loaded configuration.
     */
    @Property(tries = 100)
    void categoryReloadConsistency(
            @ForAll("validCategoryConfig") Map<String, List<String>> initialCategories,
            @ForAll("validCategoryConfig") Map<String, List<String>> newCategories) {
        
        // Create initial config
        CategoryConfigImpl initialConfig = new CategoryConfigImpl(initialCategories);
        
        // Simulate reload by creating new config from new categories
        CategoryConfigImpl reloadedConfig = new CategoryConfigImpl(newCategories);
        
        // Verify the reloaded config matches the new categories exactly
        assertThat(reloadedConfig.getCategoryNames())
            .as("Reloaded config should have all new category names")
            .containsExactlyInAnyOrderElementsOf(newCategories.keySet());
        
        // Verify each category has the correct items
        for (Map.Entry<String, List<String>> entry : newCategories.entrySet()) {
            String categoryName = entry.getKey();
            List<String> expectedItems = entry.getValue();
            
            Optional<Category> category = reloadedConfig.getCategory(categoryName);
            assertThat(category)
                .as("Category '%s' should exist after reload", categoryName)
                .isPresent();
            
            assertThat(category.get().items())
                .as("Category '%s' should have correct items after reload", categoryName)
                .containsExactlyInAnyOrderElementsOf(expectedItems);
        }
        
        // Verify old categories are not present if they were not in new config
        for (String oldCategoryName : initialCategories.keySet()) {
            if (!newCategories.containsKey(oldCategoryName)) {
                assertThat(reloadedConfig.getCategory(oldCategoryName))
                    .as("Old category '%s' should not exist after reload with new config", oldCategoryName)
                    .isEmpty();
            }
        }
    }
    
    @Provide
    Arbitrary<String> validItemNames() {
        // Vanilla uppercase names (e.g., DIAMOND, STONE, OAK_LOG)
        Arbitrary<String> vanillaUppercase = Arbitraries.strings()
            .withChars('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                       'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                       '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_')
            .ofMinLength(1)
            .ofMaxLength(30)
            .filter(s -> Character.isUpperCase(s.charAt(0)));
        
        // Vanilla lowercase names (e.g., diamond, stone, oak_log)
        Arbitrary<String> vanillaLowercase = Arbitraries.strings()
            .withChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                       'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                       '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_')
            .ofMinLength(1)
            .ofMaxLength(30)
            .filter(s -> Character.isLowerCase(s.charAt(0)));
        
        // Namespaced identifiers (e.g., minecraft:diamond, custom:my_item)
        Arbitrary<String> namespaced = Combinators.combine(
            Arbitraries.strings()
                .withChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                           'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                           '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '-')
                .ofMinLength(1)
                .ofMaxLength(15)
                .filter(s -> Character.isLowerCase(s.charAt(0))),
            Arbitraries.strings()
                .withChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                           'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                           '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '/', '.', '-')
                .ofMinLength(1)
                .ofMaxLength(30)
                .filter(s -> Character.isLowerCase(s.charAt(0)))
        ).as((namespace, name) -> namespace + ":" + name);
        
        return Arbitraries.oneOf(vanillaUppercase, vanillaLowercase, namespaced);
    }

    @Provide
    Arbitrary<String> invalidItemNames() {
        return Arbitraries.oneOf(
            // Empty string
            Arbitraries.just(""),
            // Starting with number
            Arbitraries.strings()
                .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                .ofLength(1)
                .flatMap(digit -> Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                    .map(rest -> digit + rest)),
            // Contains invalid characters
            Arbitraries.strings()
                .withChars('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', ' ')
                .ofMinLength(1)
                .ofMaxLength(10)
        );
    }
    
    @Provide
    Arbitrary<Map<String, List<String>>> validCategoryConfig() {
        // Generate valid category names (alphanumeric, starting with letter)
        Arbitrary<String> categoryNames = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(20);
        
        // Generate valid item names (alphanumeric with underscores)
        Arbitrary<String> itemNames = Arbitraries.strings()
            .withChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                       'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                       'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                       'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                       '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_')
            .ofMinLength(1)
            .ofMaxLength(30)
            .filter(s -> Character.isLetter(s.charAt(0)));
        
        // Generate list of items per category
        Arbitrary<List<String>> itemLists = itemNames.list()
            .ofMinSize(1)
            .ofMaxSize(50);
        
        // Generate map of categories
        return Arbitraries.maps(categoryNames, itemLists)
            .ofMinSize(1)
            .ofMaxSize(20);
    }
}
