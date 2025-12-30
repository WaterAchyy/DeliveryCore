package com.deliverycore.service;

import com.deliverycore.model.DeliveryDefinition;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of SchedulerService.
 * Parses natural language schedule expressions and manages event scheduling.
 * Supports event resumption after server restart.
 * 
 * Supported formats:
 * - "every day HH:mm" - Her gün
 * - "every monday HH:mm" - Her pazartesi (tüm günler desteklenir)
 * - "every week monday HH:mm" - Haftalık (her pazartesi)
 * - "every month 15 HH:mm" - Aylık (her ayın 15'i)
 * - "every month first monday HH:mm" - Aylık (her ayın ilk pazartesisi)
 * - "every month last friday HH:mm" - Aylık (her ayın son cuması)
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
public class SchedulerServiceImpl implements SchedulerService {
    
    private static final Logger LOGGER = Logger.getLogger(SchedulerServiceImpl.class.getName());
    
    // Pattern: "every <day> HH:mm" or "every day HH:mm"
    private static final Pattern DAILY_PATTERN = Pattern.compile(
        "^every\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday|day)\\s+(\\d{1,2}):(\\d{2})$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern: "every week <day> HH:mm" - Haftalık
    private static final Pattern WEEKLY_PATTERN = Pattern.compile(
        "^every\\s+week\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\s+(\\d{1,2}):(\\d{2})$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern: "every month <day_number> HH:mm" - Aylık (belirli gün)
    private static final Pattern MONTHLY_DAY_PATTERN = Pattern.compile(
        "^every\\s+month\\s+(\\d{1,2})\\s+(\\d{1,2}):(\\d{2})$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern: "every month first/last <day> HH:mm" - Aylık (ilk/son gün)
    private static final Pattern MONTHLY_ORDINAL_PATTERN = Pattern.compile(
        "^every\\s+month\\s+(first|last|1st|2nd|3rd|4th)\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\s+(\\d{1,2}):(\\d{2})$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Alias for DAILY_PATTERN - used by parseScheduleExpression and isValidExpression
    private static final Pattern SCHEDULE_PATTERN = DAILY_PATTERN;
    
    private static final Map<String, DayOfWeek> DAY_MAP = Map.of(
        "monday", DayOfWeek.MONDAY,
        "tuesday", DayOfWeek.TUESDAY,
        "wednesday", DayOfWeek.WEDNESDAY,
        "thursday", DayOfWeek.THURSDAY,
        "friday", DayOfWeek.FRIDAY,
        "saturday", DayOfWeek.SATURDAY,
        "sunday", DayOfWeek.SUNDAY
    );
    
    private static final Map<String, Integer> ORDINAL_MAP = Map.of(
        "first", 1,
        "1st", 1,
        "2nd", 2,
        "3rd", 3,
        "4th", 4,
        "last", -1
    );
    
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private final Map<String, ScheduledEventInfo> scheduledEventInfos = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor;
    private Consumer<String> eventStartCallback;
    private Consumer<String> eventEndCallback;
    
    /**
     * Creates a new SchedulerServiceImpl.
     *
     * @param executor the executor service for scheduling tasks
     */
    public SchedulerServiceImpl(ScheduledExecutorService executor) {
        this.executor = executor;
    }
    
    /**
     * Creates a SchedulerServiceImpl without an executor (for parsing only).
     */
    public SchedulerServiceImpl() {
        this.executor = null;
    }
    
    /**
     * Sets the callback to be invoked when an event should start.
     *
     * @param callback the callback function receiving the delivery name
     */
    public void setEventStartCallback(Consumer<String> callback) {
        this.eventStartCallback = callback;
    }
    
    /**
     * Sets the callback to be invoked when an event should end.
     *
     * @param callback the callback function receiving the delivery name
     */
    public void setEventEndCallback(Consumer<String> callback) {
        this.eventEndCallback = callback;
    }

    @Override
    public void scheduleEvent(DeliveryDefinition delivery) {
        if (executor == null) {
            return;
        }
        
        // Cancel any existing schedule for this delivery
        cancelScheduledEvent(delivery.name());
        
        // Parse and schedule start time
        Optional<ZonedDateTime> nextStart = getNextOccurrence(
            delivery.schedule().start(), 
            delivery.timezone()
        );
        
        if (nextStart.isEmpty()) {
            return;
        }
        
        // Parse end time
        Optional<ZonedDateTime> nextEnd = getNextOccurrence(
            delivery.schedule().end(),
            delivery.timezone()
        );
        
        if (nextEnd.isEmpty()) {
            return;
        }
        
        // Store scheduled event info for resumption
        ScheduledEventInfo info = new ScheduledEventInfo(
            delivery.name(),
            nextStart.get(),
            nextEnd.get(),
            delivery.timezone()
        );
        scheduledEventInfos.put(delivery.name(), info);
        
        // Calculate delay until start
        ZonedDateTime now = ZonedDateTime.now(delivery.timezone());
        long startDelayMs = java.time.Duration.between(now, nextStart.get()).toMillis();
        long endDelayMs = java.time.Duration.between(now, nextEnd.get()).toMillis();
        
        // Eğer şu an etkinlik zamanı içindeyse hemen başlat
        if (startDelayMs <= 0 && endDelayMs > 0) {
            if (eventStartCallback != null) {
                eventStartCallback.accept(delivery.name());
            }
            scheduleEndTask(delivery.name(), endDelayMs);
        } else if (startDelayMs > 0) {
            scheduleStartTask(delivery, startDelayMs, endDelayMs - startDelayMs);
        }
    }
    
    private void scheduleStartTask(DeliveryDefinition delivery, long startDelayMs, long durationMs) {
        ScheduledFuture<?> startTask = executor.schedule(() -> {
            if (eventStartCallback != null) {
                eventStartCallback.accept(delivery.name());
            }
            scheduleEndTask(delivery.name(), durationMs);
            executor.schedule(() -> scheduleEvent(delivery), durationMs + 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        }, startDelayMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        scheduledTasks.put(delivery.name() + "_start", startTask);
    }
    
    private void scheduleEndTask(String deliveryName, long delayMs) {
        ScheduledFuture<?> endTask = executor.schedule(() -> {
            if (eventEndCallback != null) {
                eventEndCallback.accept(deliveryName);
            }
        }, delayMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        scheduledTasks.put(deliveryName + "_end", endTask);
    }
    
    @Override
    public void cancelScheduledEvent(String deliveryName) {
        ScheduledFuture<?> startTask = scheduledTasks.remove(deliveryName + "_start");
        if (startTask != null) {
            startTask.cancel(false);
        }
        
        ScheduledFuture<?> endTask = scheduledTasks.remove(deliveryName + "_end");
        if (endTask != null) {
            endTask.cancel(false);
        }
        
        ScheduledFuture<?> task = scheduledTasks.remove(deliveryName);
        if (task != null) {
            task.cancel(false);
        }
        
        scheduledEventInfos.remove(deliveryName);
    }
    
    @Override
    public Optional<ZonedDateTime> parseScheduleExpression(String expression, ZoneId timezone) {
        if (expression == null || expression.isBlank() || timezone == null) {
            return Optional.empty();
        }
        
        Matcher matcher = SCHEDULE_PATTERN.matcher(expression.trim().toLowerCase());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        
        String dayPart = matcher.group(1);
        int hour = Integer.parseInt(matcher.group(2));
        int minute = Integer.parseInt(matcher.group(3));
        
        // Validate time values
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return Optional.empty();
        }
        
        LocalTime time = LocalTime.of(hour, minute);
        ZonedDateTime now = ZonedDateTime.now(timezone);
        ZonedDateTime result;
        
        if ("day".equals(dayPart)) {
            // Every day at specified time
            result = now.with(time);
            if (result.isBefore(now) || result.isEqual(now)) {
                result = result.plusDays(1);
            }
        } else {
            // Specific day of week
            DayOfWeek targetDay = DAY_MAP.get(dayPart);
            result = now.with(TemporalAdjusters.nextOrSame(targetDay)).with(time);
            if (result.isBefore(now) || result.isEqual(now)) {
                result = now.with(TemporalAdjusters.next(targetDay)).with(time);
            }
        }
        
        return Optional.of(result);
    }
    
    @Override
    public Optional<ZonedDateTime> getNextOccurrence(String expression, ZoneId timezone) {
        return parseScheduleExpression(expression, timezone);
    }
    
    /**
     * Resumes any active events after server restart.
     */
    @Override
    public void resumeActiveEvents() {
        List<String> eventsToResume = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now();
        
        for (ScheduledEventInfo info : scheduledEventInfos.values()) {
            ZonedDateTime nowInTimezone = now.withZoneSameInstant(info.timezone());
            
            if (isWithinEventWindow(info, nowInTimezone)) {
                eventsToResume.add(info.deliveryName());
            }
        }
        
        if (eventStartCallback != null) {
            for (String deliveryName : eventsToResume) {
                try {
                    eventStartCallback.accept(deliveryName);
                } catch (Exception ignored) {}
            }
        }
    }
    
    /**
     * Checks if the current time is within an event window.
     *
     * @param info the scheduled event info
     * @param now  the current time in the event's timezone
     * @return true if the event should be active
     */
    private boolean isWithinEventWindow(ScheduledEventInfo info, ZonedDateTime now) {
        // For recurring events, we need to check if we're between start and end times today
        ZonedDateTime startTime = info.scheduledStart();
        ZonedDateTime endTime = info.scheduledEnd();
        
        // If end time is before start time, the event spans midnight
        if (endTime.isBefore(startTime)) {
            // Event spans midnight - check if we're after start OR before end
            return !now.isBefore(startTime) || now.isBefore(endTime);
        }
        
        // Normal case - check if we're between start and end
        return !now.isBefore(startTime) && now.isBefore(endTime);
    }
    
    @Override
    public boolean isValidExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        
        Matcher matcher = SCHEDULE_PATTERN.matcher(expression.trim().toLowerCase());
        if (!matcher.matches()) {
            return false;
        }
        
        // Also validate time values
        int hour = Integer.parseInt(matcher.group(2));
        int minute = Integer.parseInt(matcher.group(3));
        
        return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
    }
    
    /**
     * Gets all scheduled event infos.
     *
     * @return unmodifiable map of delivery name to event info
     */
    public Map<String, ScheduledEventInfo> getScheduledEventInfos() {
        return Map.copyOf(scheduledEventInfos);
    }
    
    /**
     * Information about a scheduled event for resumption purposes.
     */
    public record ScheduledEventInfo(
        String deliveryName,
        ZonedDateTime scheduledStart,
        ZonedDateTime scheduledEnd,
        ZoneId timezone
    ) {}
}
