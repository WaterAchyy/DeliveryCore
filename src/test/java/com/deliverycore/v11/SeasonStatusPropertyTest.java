package com.deliverycore.v11;

import com.deliverycore.model.SeasonConfig;
import com.deliverycore.service.SeasonService;
import com.deliverycore.service.SeasonServiceImpl;
import net.jqwik.api.*;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property test: Season Status Accuracy
 * Verifies that season status calculations are accurate.
 */
class SeasonStatusPropertyTest {

    private final SeasonService seasonService = new SeasonServiceImpl();

    @Property
    void seasonStatusAccuracy(@ForAll("validSeasonConfig") SeasonConfig config) {
        ZonedDateTime now = ZonedDateTime.now();
        boolean isActive = seasonService.isSeasonActive(config);
        
        if (!config.enabled()) {
            // Disabled seasons should never be active
            assertThat(isActive).isFalse();
        } else if (config.startDate() != null && config.endDate() != null) {
            // Check if current time is within season bounds
            boolean withinDateRange = now.isAfter(config.startDate()) && now.isBefore(config.endDate());
            boolean dayIsActive = config.isDayActive(now.getDayOfWeek());
            
            assertThat(isActive).isEqualTo(withinDateRange && dayIsActive);
        }
    }

    @Property
    void disabledSeasonsAreNeverActive(@ForAll("disabledSeasonConfig") SeasonConfig config) {
        boolean isActive = seasonService.isSeasonActive(config);
        assertThat(isActive).isFalse();
    }

    @Provide
    Arbitraries<SeasonConfig> validSeasonConfig() {
        return Combinators.combine(
            Arbitraries.of(true, false),
            Arbitraries.of(ZoneId.of("UTC"), ZoneId.of("Europe/Istanbul")),
            Arbitraries.integers().between(-30, 30), // days offset from now
            Arbitraries.integers().between(1, 90),   // duration in days
            Arbitraries.of(true, false)
        ).as((enabled, zone, startOffset, duration, customDays) -> {
            ZonedDateTime now = ZonedDateTime.now(zone);
            ZonedDateTime start = now.plusDays(startOffset);
            ZonedDateTime end = start.plusDays(duration);
            
            List<DayOfWeek> activeDays = customDays ? 
                List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY) :
                List.of();
                
            return new SeasonConfig(enabled, start, end, activeDays, customDays);
        });
    }

    @Provide
    Arbitraries<SeasonConfig> disabledSeasonConfig() {
        return Arbitraries.just(SeasonConfig.disabled());
    }
}