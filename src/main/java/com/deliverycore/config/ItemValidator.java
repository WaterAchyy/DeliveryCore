package com.deliverycore.config;

import java.util.regex.Pattern;

/**
 * Validates item names for Minecraft items.
 * Supports both vanilla item names and namespaced identifiers.
 */
public final class ItemValidator {
    
    // Pattern for vanilla Minecraft item names (e.g., DIAMOND, STONE, OAK_LOG)
    private static final Pattern VANILLA_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    
    // Pattern for namespaced identifiers (e.g., minecraft:diamond, custom:my_item)
    private static final Pattern NAMESPACED_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]*:[a-z][a-z0-9_/.-]*$");
    
    // Pattern for lowercase vanilla names (e.g., diamond, stone, oak_log)
    private static final Pattern LOWERCASE_VANILLA_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");
    
    private ItemValidator() {
        // Utility class
    }
    
    /**
     * Validates if the given item name is a valid Minecraft item identifier.
     * Accepts:
     * - Vanilla uppercase names: DIAMOND, STONE, OAK_LOG
     * - Vanilla lowercase names: diamond, stone, oak_log
     * - Namespaced identifiers: minecraft:diamond, custom:my_item
     *
     * @param itemName the item name to validate
     * @return true if the item name is valid, false otherwise
     */
    public static boolean isValidItemName(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return false;
        }
        
        return VANILLA_PATTERN.matcher(itemName).matches()
            || NAMESPACED_PATTERN.matcher(itemName).matches()
            || LOWERCASE_VANILLA_PATTERN.matcher(itemName).matches();
    }
}
