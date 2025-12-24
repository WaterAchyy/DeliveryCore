package com.deliverycore.model;

import java.util.Objects;

/**
 * Configuration for event scheduling.
 *
 * @param start the start time expression (e.g., "every monday 20:00")
 * @param end   the end time expression
 */
public record ScheduleConfig(
    String start,
    String end
) {
    /**
     * Creates a new ScheduleConfig with validation.
     *
     * @param start the start time expression
     * @param end   the end time expression
     * @throws NullPointerException if start or end is null
     */
    public ScheduleConfig {
        Objects.requireNonNull(start, "Schedule start cannot be null");
        Objects.requireNonNull(end, "Schedule end cannot be null");
    }
}
