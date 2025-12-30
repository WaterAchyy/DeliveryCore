package com.deliverycore.v11;

import com.deliverycore.model.TabDisplayConfig;
import com.deliverycore.service.TabListService;
import com.deliverycore.service.TabListServiceImpl;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property test: Tab List Format Consistency
 * Verifies that tab list formatting is consistent and safe.
 */
class TabListFormatPropertyTest {

    private final TabListService tabListService = new TabListServiceImpl();

    @Property
    void tabListFormatConsistency(@ForAll("validTabConfig") TabDisplayConfig config,
                                 @ForAll("deliveryNames") String deliveryName,
                                 @ForAll("itemNames") String itemName) {
        
        if (!config.enabled()) {
            return; // Skip disabled configs
        }

        String formatted1 = tabListService.formatTabContent(deliveryName, itemName, 5, config);
        String formatted2 = tabListService.formatTabContent(deliveryName, itemName, 5, config);
        
        // Formatting should be consistent (idempotent)
        assertThat(formatted1).isEqualTo(formatted2);
        
        // Should contain the delivery name and item name
        assertThat(formatted1).contains(deliveryName);
        assertThat(formatted1).contains(itemName);
    }

    @Property
    void emptyConfigProducesEmptyResult(@ForAll("deliveryNames") String deliveryName,
                                       @ForAll("itemNames") String itemName) {
        TabDisplayConfig emptyConfig = TabDisplayConfig.disabled();
        String result = tabListService.formatTabContent(deliveryName, itemName, 5, emptyConfig);
        
        assertThat(result).isEmpty();
    }

    @Property
    void placeholderReplacementWorks(@ForAll("deliveryNames") String deliveryName,
                                    @ForAll("itemNames") String itemName,
                                    @ForAll("daysRemaining") int days) {
        TabDisplayConfig config = new TabDisplayConfig(
            true,
            "&e{delivery} &7- &a{item} &7({days} gün kaldı)",
            true,
            100L
        );
        
        String result = tabListService.formatTabContent(deliveryName, itemName, days, config);
        
        assertThat(result).contains(deliveryName);
        assertThat(result).contains(itemName);
        assertThat(result).contains(String.valueOf(days));
        assertThat(result).doesNotContain("{delivery}");
        assertThat(result).doesNotContain("{item}");
        assertThat(result).doesNotContain("{days}");
    }

    @Provide
    Arbitraries<TabDisplayConfig> validTabConfig() {
        return Combinators.combine(
            Arbitraries.of(true, false),
            Arbitraries.of(
                "&e{delivery} &7- &a{item}",
                "&6[DeliveryCore] &e{delivery} &7({days} gün)",
                "{delivery}: {item} - {days} days left",
                "§e{delivery} §7delivering §a{item}"
            ),
            Arbitraries.of(true, false),
            Arbitraries.longs().between(20L, 200L)
        ).as(TabDisplayConfig::new);
    }

    @Provide
    Arbitraries<String> deliveryNames() {
        return Arbitraries.of(
            "Günlük Teslimat",
            "Haftalık Etkinlik", 
            "Özel Görev",
            "Daily Delivery",
            "Weekly Event"
        );
    }

    @Provide
    Arbitraries<String> itemNames() {
        return Arbitraries.of(
            "Diamond",
            "Iron Ingot",
            "Gold Block",
            "Emerald",
            "Netherite Ingot"
        );
    }

    @Provide
    Arbitraries<Integer> daysRemaining() {
        return Arbitraries.integers().between(0, 30);
    }
}