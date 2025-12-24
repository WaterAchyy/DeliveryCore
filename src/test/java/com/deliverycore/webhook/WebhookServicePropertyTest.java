package com.deliverycore.webhook;

import com.deliverycore.model.EmbedConfig;
import com.deliverycore.model.PlaceholderContext;
import com.deliverycore.placeholder.PlaceholderEngine;
import com.deliverycore.placeholder.PlaceholderEngineImpl;
import net.jqwik.api.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for WebhookService.
 */
class WebhookServicePropertyTest {
    
    private final PlaceholderEngine placeholderEngine = new PlaceholderEngineImpl();
    private final WebhookService webhookService = new WebhookServiceImpl(placeholderEngine);
    
    /**
     * Feature: delivery-core, Property 20: Webhook Embed Building
     * For any EmbedConfig with title, description, and color,
     * building a WebhookEmbed should produce a structure containing
     * all configured fields with placeholders resolved.
     * Validates: Requirements 7.2, 7.3
     */
    @Property(tries = 100)
    void webhookEmbedBuilding(
            @ForAll("validEmbedConfig") EmbedConfig config,
            @ForAll("fullPlaceholderContext") PlaceholderContext context) {
        
        WebhookEmbed embed = webhookService.buildEmbed(config, context);
        
        // Verify embed is not null
        assertThat(embed).isNotNull();
        
        // Verify title is resolved (no unresolved placeholders for present context values)
        String resolvedTitle = embed.title();
        assertThat(resolvedTitle).isNotNull();
        verifyPlaceholdersResolved(resolvedTitle, context);
        
        // Verify description is resolved
        String resolvedDescription = embed.description();
        assertThat(resolvedDescription).isNotNull();
        verifyPlaceholdersResolved(resolvedDescription, context);
        
        // Verify color is a valid integer (non-negative)
        assertThat(embed.color()).isGreaterThanOrEqualTo(0);
        
        // Verify color matches parsed hex value
        int expectedColor = webhookService.parseColor(config.color());
        assertThat(embed.color()).isEqualTo(expectedColor);
    }
    
    /**
     * Feature: delivery-core, Property 21: Everyone Mention Toggle
     * For any webhook send with mentionEveryone set to true,
     * the payload should include @everyone mention;
     * when false, it should not.
     * Validates: Requirement 7.4
     */
    @Property(tries = 100)
    void everyoneMentionToggle(
            @ForAll("validWebhookEmbed") WebhookEmbed embed,
            @ForAll boolean mentionEveryone) {
        
        // Access the buildJsonPayload method through the implementation
        WebhookServiceImpl impl = (WebhookServiceImpl) webhookService;
        String payload = impl.buildJsonPayload(embed, mentionEveryone);
        
        if (mentionEveryone) {
            // When mentionEveryone is true, payload should contain @everyone
            assertThat(payload).contains("@everyone");
            assertThat(payload).contains("\"content\":\"@everyone\"");
        } else {
            // When mentionEveryone is false, payload should NOT contain @everyone
            assertThat(payload).doesNotContain("@everyone");
            assertThat(payload).doesNotContain("\"content\"");
        }
        
        // Verify payload always contains embeds array
        assertThat(payload).contains("\"embeds\":[");
        assertThat(payload).contains("\"title\":");
        assertThat(payload).contains("\"description\":");
        assertThat(payload).contains("\"color\":");
    }
    
    /**
     * Additional property: Color parsing consistency
     * For any valid hex color string, parsing should produce a consistent integer value.
     */
    @Property(tries = 100)
    void colorParsingConsistency(@ForAll("validHexColor") String hexColor) {
        int color1 = webhookService.parseColor(hexColor);
        int color2 = webhookService.parseColor(hexColor);
        
        // Same input should always produce same output
        assertThat(color1).isEqualTo(color2);
        
        // Color should be non-negative
        assertThat(color1).isGreaterThanOrEqualTo(0);
        
        // Color should be within valid range (0x000000 to 0xFFFFFF)
        assertThat(color1).isLessThanOrEqualTo(0xFFFFFF);
    }
    
    /**
     * Additional property: Hex color with and without # prefix
     * Colors with and without # prefix should parse to the same value.
     */
    @Property(tries = 100)
    void hexColorPrefixEquivalence(@ForAll("hexColorWithoutPrefix") String colorWithoutPrefix) {
        String colorWithPrefix = "#" + colorWithoutPrefix;
        
        int withPrefix = webhookService.parseColor(colorWithPrefix);
        int withoutPrefix = webhookService.parseColor(colorWithoutPrefix);
        
        assertThat(withPrefix).isEqualTo(withoutPrefix);
    }
    
    // ==================== Helper Methods ====================
    
    private void verifyPlaceholdersResolved(String text, PlaceholderContext context) {
        // Verify standard placeholders are resolved when context values are present
        if (context.category() != null && !context.category().isEmpty()) {
            assertThat(text).doesNotContain("{category}");
        }
        if (context.item() != null && !context.item().isEmpty()) {
            assertThat(text).doesNotContain("{item}");
        }
        if (context.playerName() != null && !context.playerName().isEmpty()) {
            assertThat(text).doesNotContain("{player}");
        }
        if (context.playerUuid() != null) {
            assertThat(text).doesNotContain("{player_uuid}");
        }
        if (context.deliveryName() != null && !context.deliveryName().isEmpty()) {
            assertThat(text).doesNotContain("{delivery_name}");
        }
        if (context.startTime() != null) {
            assertThat(text).doesNotContain("{start_time}");
        }
        if (context.endTime() != null) {
            assertThat(text).doesNotContain("{end_time}");
        }
        if (context.timezone() != null) {
            assertThat(text).doesNotContain("{timezone}");
        }
        // winner_count and delivery_amount are always resolved (primitives)
        assertThat(text).doesNotContain("{winner_count}");
        assertThat(text).doesNotContain("{delivery_amount}");
    }
    
    // ==================== Generators ====================
    
    @Provide
    Arbitrary<EmbedConfig> validEmbedConfig() {
        return Combinators.combine(
            titleWithPlaceholders(),
            descriptionWithPlaceholders(),
            validHexColor()
        ).as(EmbedConfig::new);
    }
    
    @Provide
    Arbitrary<String> titleWithPlaceholders() {
        Arbitrary<String> plainTitle = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(50);
        
        Arbitrary<String> titleWithPlaceholder = Arbitraries.of(
            "Event: {delivery_name}",
            "Category: {category}",
            "Item: {item}",
            "{category} - {item}",
            "Delivery Event Started",
            "Winners: {winner_count}"
        );
        
        return Arbitraries.oneOf(plainTitle, titleWithPlaceholder);
    }
    
    @Provide
    Arbitrary<String> descriptionWithPlaceholders() {
        Arbitrary<String> plainDesc = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(200);
        
        Arbitrary<String> descWithPlaceholder = Arbitraries.of(
            "Deliver {item} from {category} category!",
            "Player {player} won the event!",
            "Event starts at {start_time} and ends at {end_time}",
            "Total deliveries: {delivery_amount}",
            "Winners: {winners}",
            "Timezone: {timezone}"
        );
        
        return Arbitraries.oneOf(plainDesc, descWithPlaceholder);
    }
    
    @Provide
    Arbitrary<String> validHexColor() {
        return Arbitraries.integers()
            .between(0, 0xFFFFFF)
            .map(i -> String.format("#%06X", i));
    }
    
    @Provide
    Arbitrary<String> hexColorWithoutPrefix() {
        return Arbitraries.integers()
            .between(0, 0xFFFFFF)
            .map(i -> String.format("%06X", i));
    }
    
    @Provide
    Arbitrary<WebhookEmbed> validWebhookEmbed() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(100),
            Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(500),
            Arbitraries.integers().between(0, 0xFFFFFF)
        ).as(WebhookEmbed::new);
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
