package com.deliverycore.config;

import net.jqwik.api.*;
import net.jqwik.api.Combinators;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for LanguageConfig.
 */
class LanguageConfigPropertyTest {
    
    /**
     * Feature: delivery-core, Property 24: Language File Round-Trip
     * Validates: Requirements 9.4
     * 
     * For any language file content, serializing and deserializing
     * should preserve all message keys and values correctly.
     */
    @Property(tries = 100)
    void languageFileRoundTrip(
            @ForAll("validLanguageMessages") Map<String, String> messages) {
        
        // Create config with Turkish messages
        Map<String, Map<String, String>> languages = new LinkedHashMap<>();
        languages.put(LanguageConfig.TURKISH, messages);
        LanguageConfigImpl original = new LanguageConfigImpl(languages);
        
        // Serialize to YAML
        String yaml = original.toYaml(LanguageConfig.TURKISH);
        
        // Parse back from YAML
        LanguageConfigImpl parsed = LanguageConfigImpl.fromYaml(yaml, LanguageConfig.TURKISH);
        
        // Verify all messages are preserved
        Map<String, String> parsedMessages = parsed.getMessages(LanguageConfig.TURKISH);
        
        assertThat(parsedMessages.keySet())
            .containsExactlyInAnyOrderElementsOf(messages.keySet());
        
        for (String key : messages.keySet()) {
            assertThat(parsedMessages.get(key))
                .as("Message for key '%s' should be preserved", key)
                .isEqualTo(messages.get(key));
        }
    }

    /**
     * Feature: delivery-core, Property 25: Language Fallback
     * Validates: Requirements 9.3
     * 
     * For any message key that exists in Turkish but not in the requested locale,
     * getMessage should return the Turkish message (Turkish is the default).
     */
    @Property(tries = 100)
    void languageFallbackToTurkish(
            @ForAll("validMessageKey") String key,
            @ForAll("validMessageValue") String turkishValue) {
        
        // Create config with Turkish message only
        Map<String, Map<String, String>> languages = new LinkedHashMap<>();
        Map<String, String> turkishMessages = new LinkedHashMap<>();
        turkishMessages.put(key, turkishValue);
        languages.put(LanguageConfig.TURKISH, turkishMessages);
        languages.put(LanguageConfig.ENGLISH, Collections.emptyMap());
        
        LanguageConfigImpl config = new LanguageConfigImpl(languages);
        
        // Request English (which doesn't have the key)
        String result = config.getMessageWithFallback(key, LanguageConfig.ENGLISH);
        
        // Should fall back to Turkish
        assertThat(result)
            .as("Should fall back to Turkish when English message is missing")
            .isEqualTo(turkishValue);
    }
    
    /**
     * Feature: delivery-core, Property 26: Locale Message Selection
     * Validates: Requirements 9.2
     * 
     * For any message key that exists in both Turkish and English,
     * getMessage with English locale should return the English message.
     */
    @Property(tries = 100)
    void localeMessageSelection(
            @ForAll("validMessageKey") String key,
            @ForAll("validMessageValue") String turkishValue,
            @ForAll("validMessageValue") String englishValue) {
        
        // Create config with both Turkish and English messages
        Map<String, Map<String, String>> languages = new LinkedHashMap<>();
        Map<String, String> turkishMessages = new LinkedHashMap<>();
        turkishMessages.put(key, turkishValue);
        Map<String, String> englishMessages = new LinkedHashMap<>();
        englishMessages.put(key, englishValue);
        languages.put(LanguageConfig.TURKISH, turkishMessages);
        languages.put(LanguageConfig.ENGLISH, englishMessages);
        
        LanguageConfigImpl config = new LanguageConfigImpl(languages);
        
        // Request English
        String result = config.getMessageWithFallback(key, LanguageConfig.ENGLISH);
        
        // Should return English message
        assertThat(result)
            .as("Should return English message when requested")
            .isEqualTo(englishValue);
    }

    @Provide
    Arbitrary<Map<String, String>> validLanguageMessages() {
        // Generate flat message keys (e.g., "messages.event.start")
        Arbitrary<String> keys = Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
        ).as((a, b) -> a + "." + b);
        
        Arbitrary<String> values = Arbitraries.strings()
            .withChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                       'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                       'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                       'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                       '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', '.', '!', '?')
            .ofMinLength(1)
            .ofMaxLength(100);
        
        return Arbitraries.maps(keys, values)
            .ofMinSize(1)
            .ofMaxSize(20);
    }
    
    @Provide
    Arbitrary<String> validMessageKey() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
        ).as((a, b) -> a + "." + b);
    }
    
    @Provide
    Arbitrary<String> validMessageValue() {
        return Arbitraries.strings()
            .withChars('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                       'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                       'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                       'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                       '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ')
            .ofMinLength(1)
            .ofMaxLength(50);
    }
}
