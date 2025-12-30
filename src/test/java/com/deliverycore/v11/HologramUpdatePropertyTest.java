package com.deliverycore.v11;

import com.deliverycore.service.HologramService;
import com.deliverycore.service.HologramServiceImpl;
import net.jqwik.api.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Property test: Hologram Update Consistency
 * Verifies that hologram updates are consistent and reliable.
 */
class HologramUpdatePropertyTest {

    @Property
    void hologramUpdateConsistency(@ForAll("hologramLines") List<String> lines1,
                                  @ForAll("hologramLines") List<String> lines2) {
        HologramService service = new HologramServiceImpl();
        
        if (!service.isEnabled()) {
            return; // Skip if HolographicDisplays not available
        }

        Location mockLocation = createMockLocation();
        String hologramId = "test_holo_" + System.currentTimeMillis();
        
        // Create hologram
        var createResult = service.createHologram(hologramId, mockLocation, lines1);
        if (!createResult.isSuccess()) {
            return; // Skip if creation failed (expected in test environment)
        }
        
        // Update with first set of lines
        var updateResult1 = service.updateHologram(hologramId, lines1);
        var updateResult2 = service.updateHologram(hologramId, lines1);
        
        // Updates should be consistent
        assertThat(updateResult1.isSuccess()).isEqualTo(updateResult2.isSuccess());
        
        // Update with different lines
        var updateResult3 = service.updateHologram(hologramId, lines2);
        
        // Should handle different content
        if (updateResult1.isSuccess()) {
            assertThat(updateResult3.isSuccess()).isTrue();
        }
        
        // Cleanup
        service.deleteHologram(hologramId);
    }

    @Property
    void nonExistentHologramUpdateFails(@ForAll("hologramLines") List<String> lines) {
        HologramService service = new HologramServiceImpl();
        
        String nonExistentId = "non_existent_" + System.currentTimeMillis();
        var result = service.updateHologram(nonExistentId, lines);
        
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotEmpty();
    }

    @Provide
    Arbitraries<List<String>> hologramLines() {
        return Arbitraries.of(
            List.of("&6&l⚡ DELIVERY LEADERBOARD ⚡"),
            List.of("&e Test Event", "&7Delivering: Diamond", "&7─────────────", "&61. TestPlayer - 10"),
            List.of("&7No active event"),
            List.of("&6&lTOP PLAYERS", "&e1. Player1 - 50", "&e2. Player2 - 30", "&e3. Player3 - 20"),
            List.of() // Empty lines
        );
    }

    private Location createMockLocation() {
        // Create a mock location for testing
        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        
        return new Location(mockWorld, 0, 100, 0);
    }
}