package com.deliverycore.service;

import net.jqwik.api.*;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for SchedulerService.
 */
class SchedulerServicePropertyTest {
    
    private final SchedulerService scheduler = new SchedulerServiceImpl();
    
    /**
     * Feature: delivery-core, Property 12: Schedule Expression Parsing
     * For any valid natural language schedule expression and timezone,
     * parsing should produce a valid ZonedDateTime representing the next occurrence.
     * Validates: Requirement 4.1
     */
    @Property(tries = 100)
    void scheduleExpressionParsing(
            @ForAll("validScheduleExpression") String expression,
            @ForAll("validTimezone") ZoneId timezone) {
        
        Optional<ZonedDateTime> result = scheduler.parseScheduleExpression(expression, timezone);
        
        assertThat(result).isPresent();
        ZonedDateTime parsed = result.get();
        
        // Result should be in the future or very close to now
        ZonedDateTime now = ZonedDateTime.now(timezone);
        assertThat(parsed).isAfterOrEqualTo(now.minusSeconds(1));
        
        // Result should be in the correct timezone
        assertThat(parsed.getZone()).isEqualTo(timezone);
    }
    
    /**
     * Feature: delivery-core, Property 13: Timezone Consistency
     * For any delivery with a specified timezone,
     * all time calculations should be in that timezone.
     * Validates: Requirement 4.2
     */
    @Property(tries = 100)
    void timezoneConsistency(
            @ForAll("validScheduleExpression") String expression,
            @ForAll("validTimezone") ZoneId timezone) {
        
        Optional<ZonedDateTime> result = scheduler.getNextOccurrence(expression, timezone);
        
        assertThat(result).isPresent();
        assertThat(result.get().getZone()).isEqualTo(timezone);
    }
    
    /**
     * Feature: delivery-core, Property 14: Invalid Schedule Handling
     * For any invalid schedule expression,
     * parsing should not throw an exception and should return an error result or empty optional.
     * Validates: Requirement 4.5
     */
    @Property(tries = 100)
    void invalidScheduleHandling(
            @ForAll("invalidScheduleExpression") String expression,
            @ForAll("validTimezone") ZoneId timezone) {
        
        // Should not throw exception
        Optional<ZonedDateTime> result = scheduler.parseScheduleExpression(expression, timezone);
        
        // Should return empty for invalid expressions
        assertThat(result).isEmpty();
        
        // isValidExpression should also return false
        assertThat(scheduler.isValidExpression(expression)).isFalse();
    }

    // ==================== Generators ====================
    
    @Provide
    Arbitrary<String> validScheduleExpression() {
        Arbitrary<String> dayOfWeek = Arbitraries.of(
            "monday", "tuesday", "wednesday", "thursday", 
            "friday", "saturday", "sunday", "day"
        );
        Arbitrary<Integer> hour = Arbitraries.integers().between(0, 23);
        Arbitrary<Integer> minute = Arbitraries.integers().between(0, 59);
        
        return Combinators.combine(dayOfWeek, hour, minute)
            .as((day, h, m) -> String.format("every %s %02d:%02d", day, h, m));
    }
    
    @Provide
    Arbitrary<String> invalidScheduleExpression() {
        return Arbitraries.oneOf(
            // Invalid day names
            Arbitraries.of(
                "every invalid 10:00",
                "every 10:00",
                "monday 10:00",
                "every monday",
                "every monday 25:00",
                "every monday 10:60",
                "every monday -1:00",
                "",
                "   ",
                "random text",
                "every monday ten:thirty",
                "every monday 10:0",
                "at monday 10:00"
            ),
            // Random garbage strings
            Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50)
                .filter(s -> !s.toLowerCase().startsWith("every"))
        );
    }
    
    @Provide
    Arbitrary<ZoneId> validTimezone() {
        return Arbitraries.of(
            ZoneId.of("UTC"),
            ZoneId.of("Europe/Istanbul"),
            ZoneId.of("America/New_York"),
            ZoneId.of("Asia/Tokyo"),
            ZoneId.of("Europe/London"),
            ZoneId.of("America/Los_Angeles")
        );
    }
}
