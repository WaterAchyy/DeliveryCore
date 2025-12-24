package com.deliverycore.service;

import com.deliverycore.model.DeliveryDefinition;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Service for scheduling delivery events with timezone support.
 * Parses natural language schedule expressions and manages event timing.
 */
public interface SchedulerService {
    
    /**
     * Schedules a delivery event based on its configuration.
     *
     * @param delivery the delivery definition containing schedule info
     */
    void scheduleEvent(DeliveryDefinition delivery);
    
    /**
     * Cancels a scheduled delivery event.
     *
     * @param deliveryName the name of the delivery to cancel
     */
    void cancelScheduledEvent(String deliveryName);
    
    /**
     * Parses a natural language schedule expression.
     * Supports formats like:
     * - "every monday 20:00"
     * - "every day 14:30"
     * - "every friday 18:00"
     *
     * @param expression the schedule expression
     * @param timezone   the timezone for time calculations
     * @return an Optional containing the parsed ZonedDateTime, empty if invalid
     */
    Optional<ZonedDateTime> parseScheduleExpression(String expression, ZoneId timezone);
    
    /**
     * Gets the next occurrence of a scheduled event.
     *
     * @param expression the schedule expression
     * @param timezone   the timezone for calculations
     * @return an Optional containing the next occurrence, empty if invalid
     */
    Optional<ZonedDateTime> getNextOccurrence(String expression, ZoneId timezone);
    
    /**
     * Resumes any active events after server restart.
     */
    void resumeActiveEvents();
    
    /**
     * Checks if a schedule expression is valid.
     *
     * @param expression the schedule expression to validate
     * @return true if the expression is valid, false otherwise
     */
    boolean isValidExpression(String expression);
}
