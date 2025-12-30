package com.deliverycore.service;

import com.deliverycore.model.SeasonConfig;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sezonluk etkinlik yönetimi servisi implementasyonu
 */
public class SeasonServiceImpl implements SeasonService {
    
    private final Logger logger;
    
    public SeasonServiceImpl(Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public boolean isSeasonActive(SeasonConfig season) {
        if (!season.enabled()) {
            return true; // Devre dışı sezonlar her zaman aktif sayılır
        }
        
        if (!isValidSeasonConfig(season)) {
            logger.warning("Geçersiz sezon konfigürasyonu: " + season);
            return false;
        }
        
        ZonedDateTime now = ZonedDateTime.now();
        return isTimeInSeason(season, now);
    }
    
    @Override
    public long getDaysRemaining(SeasonConfig season) {
        if (!season.enabled() || !isSeasonActive(season)) {
            return -1;
        }
        
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime endDate = season.endDate();
        
        if (endDate == null || now.isAfter(endDate)) {
            return 0;
        }
        
        return ChronoUnit.DAYS.between(now.toLocalDate(), endDate.toLocalDate());
    }
    
    @Override
    public ZonedDateTime getSeasonEndDate(SeasonConfig season) {
        return season.enabled() ? season.endDate() : null;
    }
    
    @Override
    public List<DayOfWeek> getActiveDays(SeasonConfig season) {
        if (!season.customDays()) {
            return List.of(DayOfWeek.values()); // Tüm günler aktif
        }
        
        return season.activeDays() != null ? season.activeDays() : List.of();
    }
    
    @Override
    public boolean isValidSeasonConfig(SeasonConfig season) {
        if (!season.enabled()) {
            return true; // Devre dışı sezonlar her zaman geçerli
        }
        
        if (season.startDate() == null || season.endDate() == null) {
            return false;
        }
        
        if (!season.startDate().isBefore(season.endDate())) {
            return false;
        }
        
        if (season.customDays() && (season.activeDays() == null || season.activeDays().isEmpty())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isDayActiveInSeason(SeasonConfig season, DayOfWeek day) {
        if (!season.enabled()) {
            return true;
        }
        
        return season.isDayActive(day);
    }
    
    @Override
    public boolean isTimeInSeason(SeasonConfig season, ZonedDateTime currentTime) {
        if (!season.enabled()) {
            return true;
        }
        
        ZonedDateTime startDate = season.startDate();
        ZonedDateTime endDate = season.endDate();
        
        if (startDate == null || endDate == null) {
            return false;
        }
        
        boolean inTimeRange = !currentTime.isBefore(startDate) && !currentTime.isAfter(endDate);
        
        if (!inTimeRange) {
            return false;
        }
        
        // Özel gün kontrolü
        if (season.customDays()) {
            DayOfWeek currentDay = currentTime.getDayOfWeek();
            return isDayActiveInSeason(season, currentDay);
        }
        
        return true;
    }
}