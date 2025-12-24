package com.deliverycore.config;

import com.deliverycore.model.*;
import net.jqwik.api.*;
import net.jqwik.api.Combinators;

import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for DeliveryConfig.
 */
class DeliveryConfigPropertyTest {
    
    private static final List<String> VALID_TIMEZONES = List.of(
        "UTC", "Europe/Istanbul", "America/New_York", "Asia/Tokyo", "Europe/London"
    );
    
    /**
     * Feature: delivery-core, Property 4: Delivery Configuration Round-Trip
     * Validates: Requirements 2.1
     * 
     * For any valid delivery definition, serializing to YAML and parsing back
     * should produce an equivalent DeliveryDefinition with all fields preserved.
     */
    @Property(tries = 100)
    void deliveryConfigRoundTrip(
            @ForAll("validDeliveryDefinitions") List<DeliveryDefinition> definitions) {
        
        // Create config from definitions
        DeliveryConfigImpl original = new DeliveryConfigImpl(definitions);
        
        // Serialize to YAML
        String yaml = original.toYaml();
        
        // Parse back from YAML
        DeliveryConfigImpl parsed = DeliveryConfigImpl.fromYaml(yaml);
        
        // Verify all deliveries are preserved
        assertThat(parsed.getDeliveries().keySet())
            .containsExactlyInAnyOrderElementsOf(original.getDeliveries().keySet());
        
        // Verify each delivery has the same fields
        for (String name : original.getDeliveries().keySet()) {
            DeliveryDefinition orig = original.getDelivery(name).orElseThrow();
            DeliveryDefinition pars = parsed.getDelivery(name).orElseThrow();
            
            assertThat(pars.name()).isEqualTo(orig.name());
            assertThat(pars.enabled()).isEqualTo(orig.enabled());
            assertThat(pars.category().mode()).isEqualTo(orig.category().mode());
            assertThat(pars.category().value()).isEqualTo(orig.category().value());
            assertThat(pars.item().mode()).isEqualTo(orig.item().mode());
            assertThat(pars.item().value()).isEqualTo(orig.item().value());
            assertThat(pars.timezone()).isEqualTo(orig.timezone());
            assertThat(pars.schedule().start()).isEqualTo(orig.schedule().start());
            assertThat(pars.schedule().end()).isEqualTo(orig.schedule().end());
            assertThat(pars.winnerCount()).isEqualTo(orig.winnerCount());
            assertThat(pars.reward().type()).isEqualTo(orig.reward().type());
            assertThat(pars.reward().item()).isEqualTo(orig.reward().item());
            assertThat(pars.reward().itemAmount()).isEqualTo(orig.reward().itemAmount());
            assertThat(pars.reward().commands()).isEqualTo(orig.reward().commands());
            assertThat(pars.webhook().enabled()).isEqualTo(orig.webhook().enabled());
            assertThat(pars.webhook().url()).isEqualTo(orig.webhook().url());
        }
    }
    
    /**
     * Feature: delivery-core, Property 8: Disabled Delivery Filtering
     * Validates: Requirements 2.5
     * 
     * For any set of delivery definitions, getEnabledDeliveries should only return
     * deliveries with enabled=true and should not contain any disabled deliveries.
     */
    @Property(tries = 100)
    void disabledDeliveryFiltering(
            @ForAll("mixedEnabledDeliveries") List<DeliveryDefinition> definitions) {
        
        DeliveryConfigImpl config = new DeliveryConfigImpl(definitions);
        List<DeliveryDefinition> enabled = config.getEnabledDeliveries();
        
        // All returned deliveries should be enabled
        assertThat(enabled).allMatch(DeliveryDefinition::enabled);
        
        // Count should match
        long expectedCount = definitions.stream().filter(DeliveryDefinition::enabled).count();
        assertThat(enabled).hasSize((int) expectedCount);
        
        // No disabled deliveries should be present
        Set<String> enabledNames = new HashSet<>();
        for (DeliveryDefinition def : enabled) {
            enabledNames.add(def.name());
        }
        
        for (DeliveryDefinition def : definitions) {
            if (!def.enabled()) {
                assertThat(enabledNames).doesNotContain(def.name());
            }
        }
    }

    @Provide
    Arbitrary<List<DeliveryDefinition>> validDeliveryDefinitions() {
        return validDeliveryDefinition().list().ofMinSize(1).ofMaxSize(10)
            .map(this::ensureUniqueNames);
    }
    
    @Provide
    Arbitrary<List<DeliveryDefinition>> mixedEnabledDeliveries() {
        return Combinators.combine(
            validDeliveryDefinition().map(d -> withEnabled(d, true)).list().ofMinSize(0).ofMaxSize(5),
            validDeliveryDefinition().map(d -> withEnabled(d, false)).list().ofMinSize(0).ofMaxSize(5)
        ).as((enabled, disabled) -> {
            List<DeliveryDefinition> all = new ArrayList<>();
            all.addAll(enabled);
            all.addAll(disabled);
            return ensureUniqueNames(all);
        }).filter(list -> !list.isEmpty());
    }
    
    private List<DeliveryDefinition> ensureUniqueNames(List<DeliveryDefinition> definitions) {
        Map<String, DeliveryDefinition> unique = new LinkedHashMap<>();
        int counter = 0;
        for (DeliveryDefinition def : definitions) {
            String name = def.name() + "_" + counter++;
            unique.put(name, new DeliveryDefinition(
                name, def.enabled(), def.visibleBeforeStart(), def.category(), def.item(),
                def.timezone(), def.schedule(), def.winnerCount(),
                def.reward(), def.webhook()
            ));
        }
        return new ArrayList<>(unique.values());
    }
    
    private DeliveryDefinition withEnabled(DeliveryDefinition def, boolean enabled) {
        return new DeliveryDefinition(
            def.name(), enabled, def.visibleBeforeStart(), def.category(), def.item(),
            def.timezone(), def.schedule(), def.winnerCount(),
            def.reward(), def.webhook()
        );
    }

    private Arbitrary<DeliveryDefinition> validDeliveryDefinition() {
        // Split into two combines since jqwik supports max 8 parameters
        Arbitrary<Object[]> firstPart = Combinators.combine(
            validName(),
            Arbitraries.of(true, false),
            Arbitraries.of(true, false),
            validSelectionConfig(),
            validSelectionConfig(),
            Arbitraries.of(VALID_TIMEZONES).map(ZoneId::of)
        ).as((name, enabled, visibleBeforeStart, category, item, timezone) -> 
            new Object[]{name, enabled, visibleBeforeStart, category, item, timezone});
        
        Arbitrary<Object[]> secondPart = Combinators.combine(
            validScheduleConfig(),
            Arbitraries.integers().between(1, 10),
            validRewardConfig(),
            validWebhookConfig()
        ).as((schedule, winners, reward, webhook) -> 
            new Object[]{schedule, winners, reward, webhook});
        
        return Combinators.combine(firstPart, secondPart).as((first, second) ->
            new DeliveryDefinition(
                (String) first[0],
                (Boolean) first[1],
                (Boolean) first[2],
                (SelectionConfig) first[3],
                (SelectionConfig) first[4],
                (ZoneId) first[5],
                (ScheduleConfig) second[0],
                (Integer) second[1],
                (RewardConfig) second[2],
                (WebhookConfig) second[3]
            )
        );
    }
    
    private Arbitrary<String> validName() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(20);
    }
    
    private Arbitrary<SelectionConfig> validSelectionConfig() {
        Arbitrary<SelectionConfig> random = Arbitraries.just(SelectionConfig.random());
        Arbitrary<SelectionConfig> fixed = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(20)
            .map(SelectionConfig::fixed);
        return Arbitraries.oneOf(random, fixed);
    }
    
    private Arbitrary<ScheduleConfig> validScheduleConfig() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
        ).as(ScheduleConfig::new);
    }

    private Arbitrary<RewardConfig> validRewardConfig() {
        Arbitrary<RewardConfig> inventory = Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.integers().between(1, 64)
        ).as((item, amount) -> RewardConfig.inventory(item, amount));
        
        Arbitrary<RewardConfig> command = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(50)
            .list()
            .ofMinSize(1)
            .ofMaxSize(5)
            .map(RewardConfig::command);
        
        return Arbitraries.oneOf(inventory, command);
    }
    
    private Arbitrary<WebhookConfig> validWebhookConfig() {
        return Combinators.combine(
            Arbitraries.of(true, false),
            Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(50),
            validWebhookEventConfig(),
            validWebhookEventConfig()
        ).as(WebhookConfig::new);
    }
    
    private Arbitrary<WebhookEventConfig> validWebhookEventConfig() {
        return Combinators.combine(
            Arbitraries.of(true, false),
            validEmbedConfig()
        ).as(WebhookEventConfig::new);
    }
    
    private Arbitrary<EmbedConfig> validEmbedConfig() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(30),
            Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(100),
            Arbitraries.of("#FF5733", "#00FF00", "#0000FF", "#000000", "#FFFFFF")
        ).as(EmbedConfig::new);
    }
}
