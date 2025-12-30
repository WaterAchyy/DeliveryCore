package com.deliverycore.v11;

import com.deliverycore.service.HologramService;
import com.deliverycore.service.HologramServiceImpl;
import net.jqwik.api.*;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property test: Leaderboard Hologram Format
 * Verifies that leaderboard formatting is correct and consistent.
 */
class LeaderboardHologramFormatPropertyTest {

    private final HologramService hologramService = new HologramServiceImpl();

    @Property
    void leaderboardFormatConsistency(@ForAll("deliveryNames") String deliveryName,
                                     @ForAll("itemNames") String itemName,
                                     @ForAll("playerData") List<HologramService.PlayerLeaderboardEntry> players) {
        
        List<String> formatted1 = hologramService.formatLeaderboard(deliveryName, itemName, players);
        List<String> formatted2 = hologramService.formatLeaderboard(deliveryName, itemName, players);
        
        // Formatting should be consistent
        assertThat(formatted1).isEqualTo(formatted2);
        
        // Should contain header
        assertThat(formatted1).isNotEmpty();
        assertThat(formatted1.get(0)).contains("LEADERBOARD");
        
        if (deliveryName != null && itemName != null) {
            // Should contain delivery and item info
            boolean containsDelivery = formatted1.stream().anyMatch(line -> line.contains(deliveryName));
            boolean containsItem = formatted1.stream().anyMatch(line -> line.contains(itemName));
            
            assertThat(containsDelivery).isTrue();
            assertThat(containsItem).isTrue();
        }
    }

    @Property
    void emptyLeaderboardHandling(@ForAll("deliveryNames") String deliveryName,
                                 @ForAll("itemNames") String itemName) {
        
        List<String> formatted = hologramService.formatLeaderboard(deliveryName, itemName, List.of());
        
        assertThat(formatted).isNotEmpty();
        assertThat(formatted.get(0)).contains("LEADERBOARD");
        
        // Should indicate no deliveries
        boolean hasNoDeliveriesMessage = formatted.stream()
            .anyMatch(line -> line.toLowerCase().contains("no deliveries") || 
                             line.toLowerCase().contains("henüz kimse"));
        assertThat(hasNoDeliveriesMessage).isTrue();
    }

    @Property
    void nullInputHandling() {
        List<String> formatted = hologramService.formatLeaderboard(null, null, List.of());
        
        assertThat(formatted).isNotEmpty();
        assertThat(formatted.get(0)).contains("LEADERBOARD");
        
        // Should handle null inputs gracefully
        boolean hasNoEventMessage = formatted.stream()
            .anyMatch(line -> line.toLowerCase().contains("no active") || 
                             line.toLowerCase().contains("aktif değil"));
        assertThat(hasNoEventMessage).isTrue();
    }

    @Property
    void playerRankingOrder(@ForAll("sortedPlayerData") List<HologramService.PlayerLeaderboardEntry> players) {
        if (players.size() < 2) return;
        
        List<String> formatted = hologramService.formatLeaderboard("Test Event", "Diamond", players);
        
        // Find player lines (skip header lines)
        List<String> playerLines = formatted.stream()
            .filter(line -> line.matches(".*[①②③\\d+].*"))
            .toList();
        
        // Should have player entries
        assertThat(playerLines).isNotEmpty();
        
        // First player should have highest rank symbol or number
        if (!playerLines.isEmpty()) {
            String firstLine = playerLines.get(0);
            assertThat(firstLine).matches(".*(①|1\\.).*");
        }
    }

    @Provide
    Arbitraries<String> deliveryNames() {
        return Arbitraries.of(
            "Günlük Teslimat",
            "Haftalık Etkinlik",
            "Özel Görev",
            "Daily Delivery",
            "Weekly Event",
            null
        );
    }

    @Provide
    Arbitraries<String> itemNames() {
        return Arbitraries.of(
            "Diamond",
            "Iron Ingot", 
            "Gold Block",
            "Emerald",
            "Netherite Ingot",
            null
        );
    }

    @Provide
    Arbitraries<List<HologramService.PlayerLeaderboardEntry>> playerData() {
        return Arbitraries.integers().between(0, 10).flatMap(count -> {
            if (count == 0) {
                return Arbitraries.just(List.of());
            }
            
            return Arbitraries.integers().between(1, 1000)
                .list().ofSize(count)
                .map(deliveryCounts -> 
                    IntStream.range(0, count)
                        .mapToObj(i -> new HologramService.PlayerLeaderboardEntry(
                            "Player" + (i + 1),
                            deliveryCounts.get(i),
                            i + 1
                        ))
                        .toList()
                );
        });
    }

    @Provide
    Arbitraries<List<HologramService.PlayerLeaderboardEntry>> sortedPlayerData() {
        return playerData().map(players -> 
            players.stream()
                .sorted((a, b) -> Integer.compare(b.deliveryCount(), a.deliveryCount()))
                .toList()
        );
    }
}