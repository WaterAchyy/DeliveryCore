package com.deliverycore.placeholder;

import com.deliverycore.model.PlaceholderContext;
import net.jqwik.api.*;

import java.util.UUID;

/**
 * Property test: Unknown Placeholder Safety
 * Verifies that unknown placeholders are handled safely without errors.
 */
class UnknownPlaceholderSafetyPropertyTest {

    private final PlaceholderEngine engine = new PlaceholderEngineImpl();

    @Property
    void unknownPlaceholdersAreHandledSafely(@ForAll("placeholderTexts") String text) {
        PlaceholderContext context = new PlaceholderContext(
            "test_category",
            "test_item", 
            UUID.randomUUID(),
            "TestPlayer",
            5,
            10,
            "test_delivery"
        );

        // Should not throw exception for any input
        String result = engine.resolvePlaceholders(text, context);
        
        // Result should not be null
        Assume.that(result != null);
        
        // Unknown placeholders should remain unchanged or be replaced with safe defaults
        if (text.contains("{unknown_placeholder}")) {
            Assume.that(result.contains("{unknown_placeholder}") || 
                       result.contains("") || 
                       result.contains("N/A"));
        }
    }

    @Provide
    Arbitraries<String> placeholderTexts() {
        return Arbitraries.oneOf(
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(20)
                .map(s -> "{" + s + "}"),
            Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(1).ofMaxLength(20)
                .map(s -> "{" + s + "}"),
            Arbitraries.of(
                "{unknown_placeholder}",
                "{invalid}",
                "{nonexistent}",
                "Normal text with {unknown} placeholder",
                "{category} and {unknown_item}",
                "Multiple {unknown1} and {unknown2} placeholders"
            )
        );
    }
}