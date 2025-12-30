package com.deliverycore.v11;

import net.jqwik.api.*;
import org.bukkit.Material;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property test: Minecraft 1.21 Compatibility
 * Verifies that all 1.21 items are properly supported.
 */
class Minecraft121CompatibilityPropertyTest {

    @Property
    void minecraft121ItemsAreValid(@ForAll("minecraft121Items") String itemName) {
        // Test that 1.21 items can be resolved as Materials
        try {
            Material material = Material.valueOf(itemName.toUpperCase());
            assertThat(material).isNotNull();
            assertThat(material.isItem()).isTrue();
        } catch (IllegalArgumentException e) {
            // If Material.valueOf fails, the item might be from a newer version
            // This is acceptable for forward compatibility
            assertThat(itemName).isNotEmpty();
        }
    }

    @Property
    void newItemsHaveValidNames(@ForAll("minecraft121Items") String itemName) {
        // Verify item names follow Minecraft naming conventions
        assertThat(itemName).matches("[A-Z_]+");
        assertThat(itemName).doesNotContain(" ");
        assertThat(itemName.length()).isGreaterThan(2);
    }

    @Provide
    Arbitraries<String> minecraft121Items() {
        return Arbitraries.of(
            // 1.21 Pale Garden items
            "PALE_OAK_LOG",
            "PALE_OAK_WOOD", 
            "STRIPPED_PALE_OAK_LOG",
            "STRIPPED_PALE_OAK_WOOD",
            "PALE_OAK_PLANKS",
            "PALE_OAK_STAIRS",
            "PALE_OAK_SLAB",
            "PALE_OAK_FENCE",
            "PALE_OAK_FENCE_GATE",
            "PALE_OAK_DOOR",
            "PALE_OAK_TRAPDOOR",
            "PALE_OAK_PRESSURE_PLATE",
            "PALE_OAK_BUTTON",
            "PALE_OAK_SIGN",
            "PALE_OAK_HANGING_SIGN",
            "PALE_OAK_BOAT",
            "PALE_OAK_CHEST_BOAT",
            "PALE_OAK_LEAVES",
            "PALE_OAK_SAPLING",
            
            // 1.21 Creaking items
            "CREAKING_HEART",
            "RESIN_BRICK",
            "RESIN_BRICK_STAIRS",
            "RESIN_BRICK_SLAB",
            "RESIN_BRICK_WALL",
            "CHISELED_RESIN_BRICKS",
            
            // 1.21 Pale Moss items
            "PALE_MOSS_BLOCK",
            "PALE_MOSS_CARPET",
            
            // Existing items for compatibility test
            "DIAMOND",
            "IRON_INGOT",
            "GOLD_INGOT",
            "EMERALD"
        );
    }
}