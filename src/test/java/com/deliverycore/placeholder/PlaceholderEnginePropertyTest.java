package com.deliverycore.placeholder;

import com.deliverycore.model.PlaceholderContext;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PlaceholderEngine.
 */
class PlaceholderEnginePropertyTest {
    
    private final PlaceholderEngine engine = new PlaceholderEngineImpl();
    
    // Standard placeholder keys
    private static final Set<String> STANDARD_PLACEHOLDERS = Set.of(
        "category", "item", "player", "player_uuid", "delivery_name",
        "start_time", "end_time", "timezone", "winner_count", "winners", "delivery_amount"
    );
    
    /**
     * Feature: delivery-core, Property 9: Placeholder Resolution Completeness
     * For any text containing registered placeholder tokens and valid PlaceholderContext,
     * after resolution the result should not contain unresolved placeholder tokens
     * for which context values are present.
     * Validates: Requirements 3.1-3.12
     */
    @Property(tries = 100)
    void placeholderResolutionCompleteness(
            @ForAll("validPlaceholderContext") PlaceholderContext context,
            @ForAll("textWithStandardPlaceholders") String text) {
        
        String resolved = engine.resolve(text, context);
        
        // Check that all placeholders with non-null context values are resolved
        if (context.category() != null && !context.category().isEmpty()) {
            assertThat(resolved).doesNotContain("{category}");
        }
        if (context.item() != null && !context.item().isEmpty()) {
            assertThat(resolved).doesNotContain("{item}");
        }
        if (context.playerName() != null && !context.playerName().isEmpty()) {
            assertThat(resolved).doesNotContain("{player}");
        }
        if (context.playerUuid() != null) {
            assertThat(resolved).doesNotContain("{player_uuid}");
        }
        if (context.deliveryName() != null && !context.deliveryName().isEmpty()) {
            assertThat(resolved).doesNotContain("{delivery_name}");
        }
        if (context.startTime() != null) {
            assertThat(resolved).doesNotContain("{start_time}");
        }
        if (context.endTime() != null) {
            assertThat(resolved).doesNotContain("{end_time}");
        }
        if (context.timezone() != null) {
            assertThat(resolved).doesNotContain("{timezone}");
        }
        // winner_count and delivery_amount are always resolved (primitives)
        assertThat(resolved).doesNotContain("{winner_count}");
        assertThat(resolved).doesNotContain("{delivery_amount}");
        if (context.winners() != null && !context.winners().isEmpty()) {
            assertThat(resolved).doesNotContain("{winners}");
        }
    }
    
    /**
     * Feature: delivery-core, Property 10: Unknown Placeholder Safety
     * For any text containing unregistered placeholder tokens,
     * after resolution those tokens should be replaced with empty strings.
     * Validates: Requirement 3.13
     */
    @Property(tries = 100)
    void unknownPlaceholderSafety(
            @ForAll("textWithUnknownPlaceholders") String text,
            @ForAll("validPlaceholderContext") PlaceholderContext context) {
        
        String resolved = engine.resolve(text, context);
        
        // Extract unknown placeholders from original text
        Set<String> extractedPlaceholders = engine.extractPlaceholders(text);
        Set<String> unknownPlaceholders = extractedPlaceholders.stream()
            .filter(p -> !STANDARD_PLACEHOLDERS.contains(p))
            .collect(java.util.stream.Collectors.toSet());
        
        // Verify unknown placeholders are replaced (not present in result)
        for (String unknown : unknownPlaceholders) {
            assertThat(resolved).doesNotContain("{" + unknown + "}");
        }
    }
    
    /**
     * Feature: delivery-core, Property 11: Placeholder Text Round-Trip
     * For any text with placeholders and full PlaceholderContext,
     * resolving placeholders and then extracting resolved values
     * should produce values matching the original context.
     * Validates: Requirement 3.14
     */
    @Property(tries = 100)
    void placeholderTextRoundTrip(
            @ForAll("fullPlaceholderContext") PlaceholderContext context) {
        
        // Create text with all placeholders
        String text = "cat:{category} item:{item} player:{player} uuid:{player_uuid} " +
                     "delivery:{delivery_name} start:{start_time} end:{end_time} " +
                     "tz:{timezone} wc:{winner_count} winners:{winners} amount:{delivery_amount}";
        
        String resolved = engine.resolve(text, context);
        
        // Verify resolved values match context
        assertThat(resolved).contains("cat:" + context.category());
        assertThat(resolved).contains("item:" + context.item());
        assertThat(resolved).contains("player:" + context.playerName());
        assertThat(resolved).contains("uuid:" + context.playerUuid().toString());
        assertThat(resolved).contains("delivery:" + context.deliveryName());
        assertThat(resolved).contains("tz:" + context.timezone().getId());
        assertThat(resolved).contains("wc:" + context.winnerCount());
        assertThat(resolved).contains("amount:" + context.deliveryAmount());
        
        if (!context.winners().isEmpty()) {
            assertThat(resolved).contains("winners:" + String.join(", ", context.winners()));
        }
    }

    // ==================== Generators ====================
    
    @Provide
    Arbitrary<PlaceholderContext> validPlaceholderContext() {
        // Split into two parts since jqwik supports max 8 parameters
        Arbitrary<Object[]> firstPart = Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).injectNull(0.2),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).injectNull(0.2),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(16).injectNull(0.2),
            Arbitraries.create(UUID::randomUUID).injectNull(0.2),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).injectNull(0.2),
            zonedDateTimeArbitrary().injectNull(0.2),
            zonedDateTimeArbitrary().injectNull(0.2),
            zoneIdArbitrary().injectNull(0.2)
        ).as((cat, item, player, uuid, delivery, start, end, tz) ->
            new Object[]{cat, item, player, uuid, delivery, start, end, tz});
        
        Arbitrary<Object[]> secondPart = Combinators.combine(
            Arbitraries.integers().between(0, 100),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(16)
                .list().ofMinSize(0).ofMaxSize(10),
            Arbitraries.integers().between(0, 10000)
        ).as((wc, winners, amount) -> new Object[]{wc, winners, amount});
        
        return Combinators.combine(firstPart, secondPart).as((first, second) ->
            new PlaceholderContext(
                (String) first[0], (String) first[1], (String) first[2],
                (UUID) first[3], (String) first[4], (ZonedDateTime) first[5],
                (ZonedDateTime) first[6], (ZoneId) first[7],
                (Integer) second[0], (List<String>) second[1], List.of(), (Integer) second[2]
            ));
    }
    
    @Provide
    Arbitrary<PlaceholderContext> fullPlaceholderContext() {
        // Split into two parts since jqwik supports max 8 parameters
        Arbitrary<Object[]> firstPart = Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(16),
            Arbitraries.create(UUID::randomUUID),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            zonedDateTimeArbitrary(),
            zonedDateTimeArbitrary(),
            zoneIdArbitrary()
        ).as((cat, item, player, uuid, delivery, start, end, tz) ->
            new Object[]{cat, item, player, uuid, delivery, start, end, tz});
        
        Arbitrary<Object[]> secondPart = Combinators.combine(
            Arbitraries.integers().between(0, 100),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(16)
                .list().ofMinSize(1).ofMaxSize(5),
            Arbitraries.integers().between(0, 10000)
        ).as((wc, winners, amount) -> new Object[]{wc, winners, amount});
        
        return Combinators.combine(firstPart, secondPart).as((first, second) ->
            new PlaceholderContext(
                (String) first[0], (String) first[1], (String) first[2],
                (UUID) first[3], (String) first[4], (ZonedDateTime) first[5],
                (ZonedDateTime) first[6], (ZoneId) first[7],
                (Integer) second[0], (List<String>) second[1], List.of(), (Integer) second[2]
            ));
    }
    
    @Provide
    Arbitrary<String> textWithStandardPlaceholders() {
        Arbitrary<String> placeholderToken = Arbitraries.of(STANDARD_PLACEHOLDERS.toArray(new String[0]))
            .map(p -> "{" + p + "}");
        Arbitrary<String> plainText = Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(10);
        
        return Arbitraries.frequencyOf(
            Tuple.of(3, placeholderToken),
            Tuple.of(2, plainText)
        ).list().ofMinSize(1).ofMaxSize(10)
         .map(parts -> String.join(" ", parts));
    }
    
    @Provide
    Arbitrary<String> textWithUnknownPlaceholders() {
        Arbitrary<String> unknownPlaceholder = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(15)
            .filter(s -> !STANDARD_PLACEHOLDERS.contains(s))
            .map(p -> "{" + p + "}");
        Arbitrary<String> plainText = Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(10);
        
        return Arbitraries.frequencyOf(
            Tuple.of(3, unknownPlaceholder),
            Tuple.of(2, plainText)
        ).list().ofMinSize(1).ofMaxSize(10)
         .map(parts -> String.join(" ", parts));
    }
    
    private Arbitrary<ZonedDateTime> zonedDateTimeArbitrary() {
        return Arbitraries.longs()
            .between(0, 4102444800L) // 2000-2100
            .map(epoch -> ZonedDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(epoch),
                ZoneId.of("UTC")
            ));
    }
    
    private Arbitrary<ZoneId> zoneIdArbitrary() {
        return Arbitraries.of(
            ZoneId.of("UTC"),
            ZoneId.of("Europe/Istanbul"),
            ZoneId.of("America/New_York"),
            ZoneId.of("Asia/Tokyo")
        );
    }
}
