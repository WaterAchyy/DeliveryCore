package com.deliverycore.v11;

import com.deliverycore.model.SeasonConfig;
import com.deliverycore.service.SeasonService;
import com.deliverycore.service.SeasonServiceImpl;
import net.jqwik.api.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property test: Season Day Calculation
 * Verifies that remaining days calculation is accurate.
 */
class SeasonDayCalculationPropertyTest {

    private final SeasonService seasonService = new SeasonServiceImpl();

    @Property
    void seasonDayCalculation(@ForAll("activeSeasonConfig") SeasonConfig config) {
        if (!config.enabled() || config.endDate() == null) {
            return; // Skip disabled or invalid configs
        }

        int daysRemaining = seasonService.getDaysRemaining(config);
        ZonedDateTime now = ZonedDateTime.now();
        
        if (now.isAfter(config.endDate())) {
            // Season has ended
            assertThat(daysRemaining).isEqualTo(0);
        } else {
            // Calculate expected days
            long expectedDays = ChronoUnit.DAYS.between(now.toLocalDate(), config.endDate().toLocalDate());
            assertThat(daysRemaining).isEqualTo((int) Math.max(0, expectedDays));
        }
    }

    @Property
    void endedSeasonsHaveZeroDays(@ForAll("endedSeasonConfig") SeasonConfig config) {
        int daysRemaining = seasonService.getDaysRemaining(config);
        assertThat(daysRemaining).isEqualTo(0);
    }

    @Provide
    Arbitraries<SeasonConfig> activeSeasonConfig() {
        return Arbitraries.integers().between(1, 30).map(daysFromNow -> {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime start = now.minusDays(5);
            ZonedDateTime end = now.plusDays(daysFromNow);
            
            return new SeasonConfig(true, start, end, List.of(), false);
        });
    }

    @Provide
    Arbitraries<SeasonConfig> endedSeasonConfig() {
        return Arbitraries.integers().between(1, 30).map(daysAgo -> {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime start = now.minusDays(daysAgo + 10);
            ZonedDateTime end = now.minusDays(daysAgo);
            
            return new SeasonConfig(true, start, end, List.of(), false);
        });
    }
}