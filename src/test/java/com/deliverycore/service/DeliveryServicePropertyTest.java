package com.deliverycore.service;

import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.model.Winner;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for DeliveryService and ActiveEvent.
 */
class DeliveryServicePropertyTest {
    
    /**
     * Feature: delivery-core, Property 15: Event State Immutability
     * For any active event, once started the resolved category and item
     * should remain constant throughout the event duration regardless of config changes.
     * Validates: Requirement 5.2
     */
    @Property(tries = 100)
    void eventStateImmutability(
            @ForAll("validEventData") EventData data) {
        
        ActiveEvent event = new ActiveEvent(
            data.deliveryName,
            data.category,
            data.item,
            data.startTime,
            data.endTime,
            data.timezone
        );
        
        // Store original values
        String originalCategory = event.getResolvedCategory();
        String originalItem = event.getResolvedItem();
        
        // Simulate multiple deliveries (which shouldn't change category/item)
        for (int i = 0; i < 10; i++) {
            event.recordDelivery(UUID.randomUUID(), i + 1);
        }
        
        // Verify category and item remain unchanged
        assertThat(event.getResolvedCategory()).isEqualTo(originalCategory);
        assertThat(event.getResolvedItem()).isEqualTo(originalItem);
        assertThat(event.getResolvedCategory()).isEqualTo(data.category);
        assertThat(event.getResolvedItem()).isEqualTo(data.item);
    }
    
    /**
     * Feature: delivery-core, Property 16: Delivery Tracking Accumulation
     * For a sequence of delivery records for a player UUID,
     * the total delivery count should equal the sum of all individual delivery amounts.
     * Validates: Requirement 5.4
     */
    @Property(tries = 100)
    void deliveryTrackingAccumulation(
            @ForAll("validEventData") EventData data,
            @ForAll("deliveryAmounts") List<Integer> amounts) {
        
        ActiveEvent event = new ActiveEvent(
            data.deliveryName,
            data.category,
            data.item,
            data.startTime,
            data.endTime,
            data.timezone
        );
        
        UUID playerUuid = UUID.randomUUID();
        int expectedTotal = 0;
        
        for (int amount : amounts) {
            event.recordDelivery(playerUuid, amount);
            expectedTotal += amount;
        }
        
        assertThat(event.getPlayerDeliveryCount(playerUuid)).isEqualTo(expectedTotal);
    }

    /**
     * Feature: delivery-core, Property 17: Winner Calculation Ordering
     * For any set of player deliveries, calculated winners should be
     * sorted by delivery count in descending order and not exceed the configured winner count.
     * Validates: Requirement 5.6
     */
    @Property(tries = 100)
    void winnerCalculationOrdering(
            @ForAll("validEventData") EventData data,
            @ForAll("playerDeliveries") Map<UUID, Integer> deliveries,
            @ForAll @IntRange(min = 1, max = 10) int winnerCount) {
        
        ActiveEvent event = new ActiveEvent(
            data.deliveryName,
            data.category,
            data.item,
            data.startTime,
            data.endTime,
            data.timezone
        );
        
        // Record all deliveries
        for (Map.Entry<UUID, Integer> entry : deliveries.entrySet()) {
            event.recordDelivery(entry.getKey(), entry.getValue());
        }
        
        // Calculate winners
        DeliveryServiceImpl service = new DeliveryServiceImpl(null, null, null);
        List<Winner> winners = service.calculateWinners(
            event, 
            winnerCount, 
            uuid -> "Player_" + uuid.toString().substring(0, 8)
        );
        
        // Verify winner count doesn't exceed limit
        assertThat(winners.size()).isLessThanOrEqualTo(winnerCount);
        assertThat(winners.size()).isLessThanOrEqualTo(deliveries.size());
        
        // Verify descending order by delivery count
        for (int i = 1; i < winners.size(); i++) {
            assertThat(winners.get(i - 1).deliveryCount())
                .isGreaterThanOrEqualTo(winners.get(i).deliveryCount());
        }
        
        // Verify ranks are sequential starting from 1
        for (int i = 0; i < winners.size(); i++) {
            assertThat(winners.get(i).rank()).isEqualTo(i + 1);
        }
    }
    
    // ==================== Test Data Classes ====================
    
    record EventData(
        String deliveryName,
        String category,
        String item,
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        ZoneId timezone
    ) {}
    
    // ==================== Generators ====================
    
    @Provide
    Arbitrary<EventData> validEventData() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30),
            Arbitraries.of(ZoneId.of("UTC"), ZoneId.of("Europe/Istanbul"))
        ).as((name, cat, item, tz) -> {
            ZonedDateTime now = ZonedDateTime.now(tz);
            return new EventData(name, cat, item, now, now.plusHours(1), tz);
        });
    }
    
    @Provide
    Arbitrary<List<Integer>> deliveryAmounts() {
        return Arbitraries.integers()
            .between(1, 100)
            .list()
            .ofMinSize(1)
            .ofMaxSize(20);
    }
    
    @Provide
    Arbitrary<Map<UUID, Integer>> playerDeliveries() {
        return Arbitraries.integers()
            .between(1, 1000)
            .list()
            .ofMinSize(1)
            .ofMaxSize(20)
            .map(amounts -> {
                Map<UUID, Integer> map = new HashMap<>();
                for (Integer amount : amounts) {
                    map.put(UUID.randomUUID(), amount);
                }
                return map;
            });
    }
}
