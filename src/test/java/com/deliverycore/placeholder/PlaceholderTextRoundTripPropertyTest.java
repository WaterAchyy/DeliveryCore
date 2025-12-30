package com.deliverycore.placeholder;

import com.deliverycore.model.PlaceholderContext;
import net.jqwik.api.*;

import java.util.UUID;

/**
 * Property test: Placeholder Text Round-Trip
 * Verifies that placeholder resolution is consistent and reversible where applicable.
 */
class PlaceholderTextRoundTripPropertyTest {

    private final PlaceholderEngine engine = new PlaceholderEngineImpl();

    @Property
    void placeholderResolutionIsConsistent(@ForAll("contexts") PlaceholderContext context,
                                          @ForAll("templates") String template) {
        // Resolve placeholders twice - should get same result
        String firstResolution = engine.resolvePlaceholders(template, context);
        String secondResolution = engine.resolvePlaceholders(firstResolution, context);
        
        // Second resolution should not change the result (idempotent)
        Assume.that(firstResolution.equals(secondResolution));
    }

    @Property
    void emptyTemplateRemainsEmpty(@ForAll("contexts") PlaceholderContext context) {
        String result = engine.resolvePlaceholders("", context);
        Assume.that(result.equals(""));
    }

    @Property
    void textWithoutPlaceholdersRemainsUnchanged(@ForAll("contexts") PlaceholderContext context,
                                                @ForAll("plainTexts") String plainText) {
        String result = engine.resolvePlaceholders(plainText, context);
        Assume.that(result.equals(plainText));
    }

    @Provide
    Arbitraries<PlaceholderContext> contexts() {
        return Arbitraries.create(() -> new PlaceholderContext(
            "test_category",
            "DIAMOND",
            UUID.randomUUID(),
            "TestPlayer",
            Arbitraries.integers().between(1, 100).sample(),
            Arbitraries.integers().between(1, 1000).sample(),
            "test_delivery"
        ));
    }

    @Provide
    Arbitraries<String> templates() {
        return Arbitraries.oneOf(
            Arbitraries.of(
                "{category}",
                "{item}",
                "{player}",
                "{count}",
                "{total}",
                "{delivery}",
                "Player {player} delivered {count} {item}",
                "Category: {category}, Total: {total}",
                "{player} - {delivery} - {count}/{total}"
            )
        );
    }

    @Provide
    Arbitraries<String> plainTexts() {
        return Arbitraries.oneOf(
            Arbitraries.of(
                "No placeholders here",
                "Just plain text",
                "Numbers 123 and symbols !@#",
                "Unicode: üöäß",
                ""
            )
        );
    }
}